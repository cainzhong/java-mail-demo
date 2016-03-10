/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.logging.LogFactory;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.operator.OperatorCreationException;

import com.hp.ov.sm.common.core.Init;
import com.hp.ov.sm.common.core.JLog;
import com.java.mail.MailReceiver;
import com.java.mail.domain.Attachment;
import com.java.mail.domain.MailAddress;
import com.java.mail.domain.MailMessage;

import net.sf.json.JSONArray;

/**
 * @author zhontao
 *
 */
public abstract class AbstractMailReceiver implements MailReceiver {
  private static final JLog LOG = new JLog(LogFactory.getLog(AbstractMailReceiver.class));

  private static final int DEFAULT_MAX_ATTACHMENT_COUNT = 100;

  /* Unit is bit */
  private static final int DEFAULT_ATTACHMENT_SEGMENT_SIZE = 512000;

  private static final String PROVIDER_NAME = "BC";

  private static final int BUFFSIZE = 64 * 1024;

  /* the default value of the maximum quantity for receiving mails during one time */
  protected static final int DEFAULT_MAX_MAIL_QUANTITY = 1000;

  protected static final String DEFAULT_SOURCE_FOLDER_NAME = "INBOX";

  protected static final String DEFAULT_TO_FOLDER_NAME = "Deleted Items";

  protected static final String POP3 = "POP3";

  protected static final String POP3S = "POP3S";

  protected static final String IMAP = "IMAP";

  protected static final String IMAPS = "IMAPS";

  protected Session session;

  protected Store store;

  protected FetchProfile profile;

  protected Folder sourceFolder;

  protected Folder toFolder;

  /* Incoming Mail" server, eg. webmail.hp.com */
  protected String host;

  /* eg. SSL ports are 993 for IMAP and 995 for POP3 */
  protected String port;

  /* eg. POP3, POP3S, IMAP or IMAPS */
  protected String protocol;

  /* eg. joe.smith@hp.com */
  protected String username;

  protected String password;

  /* the file suffix which can be saved, others will be ignored */
  protected List<String> suffixList;

  protected List<String> authorisedUserList;

  /* eg. true or false, whether Incoming Mail server needs authentication or not */
  protected boolean proxySet;

  protected String proxyHost;

  protected String proxyPort;

  protected String sourceFolderName;

  protected String toFolderName;

  protected static int attachmentsegmentsize;

  protected static int maxattachmentcount;

  /* the maximum quantity for receiving mails during one time */
  protected int maxMailQuantity;

  protected String uri;

  static {
    // read the attachment segment size from configuration file, if it is null, set as default value.
    String attachmentsegmentsizeStr = Init.getInstance().getProperty("attachmentsegmentsize");
    if (attachmentsegmentsizeStr == null || attachmentsegmentsizeStr.length() == 0) {
      attachmentsegmentsizeStr = String.valueOf(DEFAULT_ATTACHMENT_SEGMENT_SIZE);
    }
    attachmentsegmentsize = Integer.valueOf(attachmentsegmentsizeStr);

    // read the max attachment count from configuration file, if it is null, set as default value.
    String maxattachmentcountStr = Init.getInstance().getProperty("maxattachmentcount");
    if (maxattachmentcountStr == null || maxattachmentcountStr.length() == 0) {
      maxattachmentcountStr = String.valueOf(DEFAULT_MAX_ATTACHMENT_COUNT);
    }
    maxattachmentcount = Integer.valueOf(maxattachmentcountStr);
  }

  @Override
  public String readMessage(String filePath) throws Exception {
    MailMessage mailMsg = new MailMessage();
    MimeMessage mime = this.readEMLToMessage(filePath);
    this.getHeader(mime, mailMsg);
    return this.processMsg(mime, mailMsg, false);
  }

  @Override
  public String readEmailAttachments(String filePath) throws Exception {
    MailMessage mailMsg = new MailMessage();
    MimeMessage mime = this.readEMLToMessage(filePath);
    return this.processMsg(mime, mailMsg, true);
  }

  /**
   * Read an eml file from a specific file path and convert it to MimeMessage.
   * 
   * @param filePath
   * @return
   * @throws FileNotFoundException
   * @throws MessagingException
   */
  private MimeMessage readEMLToMessage(String filePath) throws FileNotFoundException, MessagingException {
    InputStream is = new FileInputStream(filePath);
    MimeMessage mime = new MimeMessage(null, is);
    return mime;
  }

  /**
   * Process message according to specified MIME type.
   * 
   * @param mime
   * @param mailMsg
   * @param save
   * @return
   * @throws MessagingException
   * @throws IOException
   * @throws CMSException
   * @throws OperatorCreationException
   * @throws CertificateException
   */
  protected String processMsg(MimeMessage mime, MailMessage mailMsg, boolean save) throws MessagingException, IOException, CMSException, OperatorCreationException, CertificateException {
    System.out.println("Size" + mime.getSize());
    String message;
    if (!this.exceedMaxMsgSize(mime.getSize())) {
      if (mime.isMimeType("text/html") || mime.isMimeType("text/plain") || mime.isMimeType("multipart/mixed")) {
        // simple mail without/with attachment
        message = this.processSimpleMail(mime, mailMsg, save);
      } else if (mime.isMimeType("multipart/signed")) {
        // signed mail with/without attachment
        if (!save) {
          this.validateSignedMail(mime, mailMsg);
        }
        message = this.processSignedMail(mime, mailMsg, save);
      } else if (mime.isMimeType("application/pkcs7-mime") || mime.isMimeType("application/x-pkcs7-mime")) {
        String e = "It's an encrypted mail. Can not handle the Message receiving from Mail Server.";
        LOG.error(e);
        throw new MessagingException(e);
      } else {
        String e = "Unknown MIME Type | Message Content Type: " + mime.getContentType() + "Message Subject: " + mime.getSubject() + "Message Send Date: " + mime.getSentDate() + "Message From: " + mime.getFrom().toString();
        LOG.error(e);
        throw new MessagingException(e);
      }
    }
    return message;
  }

  /**
   * Set MailMessage for simple mail with/without attachment.
   * 
   * @param mime
   * @param mailMsg
   * @param save
   * @return
   * @throws MessagingException
   * @throws IOException
   */
  private String processSimpleMail(MimeMessage mime, MailMessage mailMsg, boolean save) throws IOException, MessagingException {
    List<Attachment> attachList = new ArrayList<Attachment>();
    mailMsg.setHasSignature(false);
    mailMsg.setSignaturePassed(false);
    this.setMailBasicInfoForMailMsg(mime, mailMsg, save);

    if (!save && mime.isMimeType("text/plain")) {
      mailMsg.setTxtBody(mime.getContent().toString());
    } else if (!save && mime.isMimeType("text/html")) {
      mailMsg.setHtmlBody(mime.getContent().toString());
    } else if (mime.getContent() instanceof Multipart) {
      // Get the content of the messsage, it's an Multipart object like a package including all the email text and attachment.
      Multipart multi1 = (Multipart) mime.getContent();
      // First, verify the quantity and size of attachments.
      boolean isValidMailMsg = this.isValidMailMsg(multi1, mailMsg);
      if (isValidMailMsg) {
        // process each part in order.
        for (int i = 0, n = multi1.getCount(); i < n; i++) {
          // unpack, get each part of Multipart, part 0 may email text and part 1 may attachment. Or it is another embedded Multipart.
          Part part = multi1.getBodyPart(i);
          if (!save && part.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
            mailMsg.setTxtBody(part.getContent().toString());
          } else if (!save && part.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
            mailMsg.setHtmlBody(part.getContent().toString());
          } else {
            String disposition = part.getDisposition();
            if (disposition != null && Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
              mailMsg.setHasAttachments(true);
              if (save) {
                // Save the attachment if it is.
                this.saveAttachment(part, attachList);
              }
            }
          }
        }
      }
    }
    if (save) {
      return attachList.toString();
    } else {
      return JSONArray.fromObject(mailMsg).toString();
    }
  }

  /**
   * Validate signed mail.
   * 
   * @param mime
   * @param mailMsg
   * @return
   * @throws MessagingException
   * @throws IOException
   * @throws CMSException
   * @throws CertificateException
   * @throws OperatorCreationException
   */
  private boolean validateSignedMail(MimeMessage mime, MailMessage mailMsg) throws MessagingException, CMSException, IOException, OperatorCreationException, CertificateException {
    boolean verify = false;
    /*
     * Add a header to make a new message in order to fix the issue of Outlook
     * 
     * @see http://www.bouncycastle.org/wiki/display/JA1/CMS+and+SMIME+APIs
     * 
     * @see http://stackoverflow.com/questions/8590426/s-mime-verification-with- x509-certificate
     */
    MimeMessage newmsg = new MimeMessage(mime);
    newmsg.setHeader("Nothing", "Add a header for verifying signature only.");
    newmsg.saveChanges();
    SMIMESigned signed = new SMIMESigned((MimeMultipart) newmsg.getContent());
    verify = this.isValid(signed, mailMsg);
    return verify;
  }

  /**
   * Verify the email is signed by the given certificate.
   * 
   * @param signedData
   * @param mailMsg
   * @return
   * @throws CMSException
   * @throws OperatorCreationException
   * @throws CertificateException
   */
  @SuppressWarnings({ "rawtypes" })
  protected boolean isValid(CMSSignedData signedData, MailMessage mailMsg) throws OperatorCreationException, CMSException, CertificateException {
    boolean verify = false;
    SignerInformationStore signerStore = signedData.getSignerInfos();
    Iterator<SignerInformation> it = signerStore.getSigners().iterator();
    while (it.hasNext()) {
      SignerInformation signer = it.next();
      org.bouncycastle.util.Store store = signedData.getCertificates();
      @SuppressWarnings("unchecked")
      Collection certCollection = store.getMatches(signer.getSID());
      Iterator certIt = certCollection.iterator();
      X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();
      X509Certificate certificate = new JcaX509CertificateConverter().setProvider(PROVIDER_NAME).getCertificate(certHolder);
      verify = signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider(PROVIDER_NAME).build(certificate));
      mailMsg.setHasSignature(true);
      mailMsg.setSignaturePassed(verify);
      mailMsg.setNameOfPrincipal(certificate.getSubjectDN().getName());
    }
    return verify;
  }

  /**
   * Set MailMessage for signed mail with/without attachment.
   * 
   * @param mime
   * @param mailMsg
   * @param save
   * @return
   * @throws MessagingException
   * @throws IOException
   */
  private MailMessage processSignedMail(MimeMessage mime, MailMessage mailMsg, boolean save) throws IOException, MessagingException {
    List<Attachment> attachList = new ArrayList<Attachment>();

    this.setMailBasicInfoForMailMsg(mime, mailMsg, save);
    // Get the content of the messsage, it's an Multipart object like a package including all the email text and attachment.
    Multipart multi1 = (Multipart) mime.getContent();

    // process each part in order.
    for (int i = 0, n = multi1.getCount(); i < n; i++) {
      // unpack, get each part of Multipart, part 0 may email text and part 1 may attachment. Or it is another embedded Multipart.
      Part part2 = multi1.getBodyPart(i);
      // determine Part is email text or Multipart.
      if (part2.getContent() instanceof Multipart) {
        Multipart multi2 = (Multipart) part2.getContent();
        // First, verify the quantity and size of attachments.
        boolean isValidMailMsg = this.isValidMailMsg(multi2, mailMsg);
        // process the content in multi2.
        for (int j = 0; j < multi2.getCount(); j++) {
          Part part3 = multi2.getBodyPart(j);
          // generally if the content type multipart/alternative, it is email text.
          if (!save && part3.isMimeType("multipart/alternative")) {
            if (part3.getContent() instanceof Multipart) {
              Multipart multi3 = (Multipart) part3.getContent();
              for (int k = 0; k < multi3.getCount(); k++) {
                Part part4 = multi3.getBodyPart(k);
                if (part4.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part4.getDisposition())) {
                  mailMsg.setTxtBody(part4.getContent().toString());
                } else if (part4.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part4.getDisposition())) {
                  mailMsg.setHtmlBody(part4.getContent().toString());
                }
              }
            }
          } else if (part3.isMimeType("multipart/related")) {
            if (!save && part3.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part3.getDisposition())) {
              mailMsg.setTxtBody(part3.getContent().toString());
            } else if (!save && part3.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part3.getDisposition())) {
              mailMsg.setHtmlBody(part3.getContent().toString());
            } else if (part3.getContent() instanceof Multipart) {
              Multipart multi3 = (Multipart) part3.getContent();
              for (int m = 0; m < multi3.getCount(); m++) {
                Part part4 = multi3.getBodyPart(m);
                if (!save && part4.isMimeType("multipart/alternative")) {
                  if (part4.getContent() instanceof Multipart) {
                    Multipart multi4 = (Multipart) part4.getContent();
                    for (int p = 0; p < multi4.getCount(); p++) {
                      Part part5 = multi4.getBodyPart(p);
                      if (part5.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part5.getDisposition())) {
                        mailMsg.setTxtBody(part5.getContent().toString());
                      } else if (part5.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part5.getDisposition())) {
                        mailMsg.setHtmlBody(part5.getContent().toString());
                      }
                    }
                  }
                } else if (save && isValidMailMsg) {
                  // This is an embedded picture, save it.
                  this.saveAttachment(part4, attachList);
                }
              }
            }
          } else if (save && isValidMailMsg) {
            // Save the attachment.
            String disposition = part3.getDisposition();
            if (disposition != null && Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
              mailMsg.setHasAttachments(true);
              this.saveAttachment(part3, attachList);
            }
          }
        }
      } else {
        // Process the attachment.(This is a certificate file.)
        String disposition = part2.getDisposition();
        if (disposition != null && Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
          mailMsg.setHasAttachments(true);
          this.saveAttachment(part2, attachList);
        }
      }
    }
    return mailMsg;
  }

  /**
   * Save attachment.
   * 
   * @param part
   * @param attachList
   * @throws MessagingException
   * @throws IOException
   */
  protected void saveAttachment(Part part, List<Attachment> attachList) throws MessagingException, IOException {
    // generate a new file name with unique UUID.
    String fileName = part.getFileName();
    fileName = MimeUtility.decodeText(fileName);

    UUID uuid = UUID.randomUUID();
    String prefix = fileName.substring(0, fileName.lastIndexOf(".") + 1);
    String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
    if (this.suffixList.contains(suffix.toLowerCase())) {
      String tempDir = System.getProperty("java.io.tmpdir");
      fileName = tempDir + prefix + uuid + "." + suffix;

      int fileSize = part.getSize();
      Attachment attachment = new Attachment();
      attachment.setFileName(fileName);
      attachment.setFileType(suffix);
      attachment.setFileSize(fileSize);
      attachList.add(attachment);
      this.saveFile(fileName, part.getInputStream());
    } else {
      LOG.info(fileName + " is not a supported file. Ignore this file.");
    }
  }

  /**
   * Save file to temp directory.
   * 
   * @param fileName
   * @param in
   * @throws IOException
   */
  protected void saveFile(String fileName, InputStream in) throws IOException {
    File file = new File(fileName);
    if (!file.exists()) {
      OutputStream out = null;
      try {
        out = new BufferedOutputStream(new FileOutputStream(file));
        in = new BufferedInputStream(in);
        byte[] buf = new byte[BUFFSIZE];
        int len;
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
      } finally {
        // close streams
        if (in != null) {
          in.close();
        }
        if (out != null) {
          out.close();
        }
      }
    }
  }

  /**
   * Set some email's basic information to MailMessage .
   * 
   * @param mime
   * @param mailMsg
   * @param save
   * @throws MessagingException
   */
  private void setMailBasicInfoForMailMsg(MimeMessage mime, MailMessage mailMsg, boolean save) throws MessagingException {
    if (!save) {
      String msgId = mime.getMessageID();
      Address[] from = mime.getFrom();
      Address[] to = mime.getRecipients(RecipientType.TO);
      Address[] cc = mime.getRecipients(RecipientType.CC);
      Address[] bcc = mime.getRecipients(RecipientType.BCC);
      String subject = mime.getSubject();
      Date sendDate = mime.getSentDate();

      mailMsg.setMsgId(msgId);
      mailMsg.setFrom(this.convertToMailAddress(from));
      mailMsg.setTo(this.convertToMailAddress(to));
      mailMsg.setCc(this.convertToMailAddress(cc));
      mailMsg.setBcc(this.convertToMailAddress(bcc));
      mailMsg.setSubject(subject);
      mailMsg.setSendDate(sendDate);
    }
  }

  /**
   * Convert an array of addresses to a list of MailAddress.
   * 
   * @param addresses
   *          Address is a type of <code>javax.mail.Address<code>.
   * @return List&lt;MailAddress&gt;
   */
  private List<MailAddress> convertToMailAddress(Address[] addresses) {
    List<MailAddress> addressList = new ArrayList<MailAddress>();
    if ((addresses != null && addresses.length != 0)) {
      if (addresses instanceof InternetAddress[]) {
        InternetAddress[] internetAddresses = (InternetAddress[]) addresses;
        for (InternetAddress internetAddress : internetAddresses) {
          MailAddress mailAddress = new MailAddress();
          String personal = internetAddress.getPersonal();
          String address = internetAddress.getAddress();
          mailAddress.setName(personal);
          mailAddress.setAddress(address);
          addressList.add(mailAddress);
        }
      } else {
        for (Address address : addresses) {
          MailAddress mailAddress = new MailAddress();
          mailAddress.setAddress(address.toString());
          addressList.add(mailAddress);
        }
      }
    }
    return addressList;
  }

  /**
   * Get message header for MailMessage.
   * 
   * @param msg
   * @param mailMsg
   * @throws MessagingException
   */
  protected void getHeader(Message msg, MailMessage mailMsg) throws MessagingException {
    String xAutoResponseSuppressHeaderName = "X-Auto-Response-Suppress";
    String xAutoReplyHeaderName = "X-Autoreply";
    String xAutoRespondHeaderName = "X-Autorespond";
    String xAutoSubmittedHeaderName = "auto-submitted";

    String xAutoResponseSuppressVal = this.headerToString(msg.getHeader(xAutoResponseSuppressHeaderName));
    String xAutoReplyVal = this.headerToString(msg.getHeader(xAutoReplyHeaderName));
    String xAutoRespondVal = this.headerToString(msg.getHeader(xAutoRespondHeaderName));
    String xAutoSubmittedVal = this.headerToString(msg.getHeader(xAutoSubmittedHeaderName));
    String contentType = msg.getContentType();

    // If any of those are present in an email, then that email is an auto-reply.
    String[] autoReplyArray = { xAutoResponseSuppressVal, xAutoReplyVal, xAutoRespondVal, xAutoSubmittedVal };
    mailMsg.setAutoReply(autoReplyArray);
    mailMsg.setContentType(contentType);
  }

  /**
   * Convert array to string.
   * 
   * @param headerArray
   * @return
   */
  private String headerToString(String[] headerArray) {
    String headerStr = "";
    if (headerArray != null && headerArray.length > 0) {
      for (String header : headerArray) {
        headerStr = headerStr + header + ";";
      }
    }
    return headerStr;
  }

  /**
   * Check whether the given parameter is null or not.
   * 
   * @param s
   *          String
   * @return true means 's' is null
   */
  protected static boolean isNull(String s) {
    if (s == null || s == "") {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Check whether the user name is belong to authorised user domain or not.
   * 
   * @param authorisedUserList
   * @param username
   * @return
   */
  protected static boolean isAuthorisedUsername(List<String> authorisedUserList, String username) {
    if (authorisedUserList == null || authorisedUserList.isEmpty()) {
      String msg = "The authorised user domain list is empty!";
      LOG.info(msg);
    }
    for (String regex : authorisedUserList) {
      if (username.matches(".*" + regex)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Verify the mail's size and the attachments exceeding the maximum quantity.
   * 
   * @param multi
   * @param mailMsg
   * @return
   * @throws MessagingException
   * @throws IOException
   */
  private boolean isValidMailMsg(Multipart multi, MailMessage mailMsg) throws MessagingException, IOException {
    boolean isValid = false;
    boolean exceedMaxMailSize = false;
    boolean exceedMaxAttachmentCount = false;

    if (this.countMailAttachments(multi) > maxattachmentcount) {
      exceedMaxAttachmentCount = true;
      LOG.info("The attachments' quantity exceed the maximum value!");
    }

    int mailSize = this.countMailSize(multi, 0);
    if (mailSize > attachmentsegmentsize * maxattachmentcount) {
      exceedMaxMailSize = true;
      LOG.info("The size of all the attachments exceed the maximum value!");
    }

    if (!exceedMaxAttachmentCount && !exceedMaxMailSize) {
      isValid = true;
    }
    return isValid;
  }

  /**
   * Determine whether the attachment's quantity exceeds the max attachment count or not.
   * 
   * @param multi
   * @return
   * @throws MessagingException
   */
  protected int countMailAttachments(Multipart multi) throws MessagingException {
    int mailAttachments = 0;
    // Normally, only 1 BodyPart is email text, others are attachments.
    // So the whole BodyPart minus 1 is the attachment quantity.
    int attachmentCount = multi.getCount();
    mailAttachments = attachmentCount - 1;
    return mailAttachments;
  }

  /**
   * Count the mail size, it is a total mail size, including email body and all attachments.
   * 
   * @param multi
   * @param mailSize
   * @return
   * @throws MessagingException
   * @throws IOException
   */
  protected int countMailSize(Multipart multi, int mailSize) throws MessagingException, IOException {
    for (int i = 0, n = multi.getCount(); i < n; i++) {
      Part part = multi.getBodyPart(i);

      if (part.getContent() instanceof Multipart) {
        mailSize = this.countMailSize((Multipart) part.getContent(), mailSize);
      }
      int partSize = part.getSize();
      if (partSize != -1) {
        mailSize = mailSize + partSize;
      }
    }
    return mailSize;
  }

  /**
   * Determine whether the mail's size exceeds the max mail's size or not.
   * 
   * @param msgSize
   * @return
   */
  protected boolean exceedMaxMsgSize(int msgSize) {
    boolean exceedMaxMsgSize = false;
    if (msgSize > attachmentsegmentsize * maxattachmentcount) {
      exceedMaxMsgSize = true;
      LOG.error("The size of all the attachments exceed the maximum value!");
    }
    return exceedMaxMsgSize;
  }
}
