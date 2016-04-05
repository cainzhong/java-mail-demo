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
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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

  /* Only for EWS, the default value of the maximum quantity for receiving mails during one time */
  protected static final int DEFAULT_MAX_MAIL_QUANTITY = 1000;

  protected static final String DEFAULT_SOURCE_FOLDER_NAME = "INBOX";

  protected static final String DEFAULT_TO_FOLDER_NAME = "Deleted Items";

  protected static final String POP3 = "POP3";

  protected static final String POP3S = "POP3S";

  protected static final String IMAP = "IMAP";

  protected static final String IMAPS = "IMAPS";

  protected static final String DATE_FORMAT_MM_DD_YYYY_HH_MM_SS = "MM/dd/yyyy HH:mm:ss";

  protected static final String TIME_ZONE_UTC = "UTC";

  protected Session session;

  protected Store store;

  protected FetchProfile profile;

  protected Folder sourceFolder;

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
  // protected List<String> suffixList;
  //
  // protected List<String> authorisedUserList;

  /* eg. true or false, whether Incoming Mail server needs authentication or not */
  protected boolean proxySet;

  protected String proxyHost;

  protected String proxyPort;

  protected String proxyUser;

  protected String proxyPassword;

  protected String sourceFolderName;

  protected static int attachmentsegmentsize;

  protected static int maxattachmentcount;

  /* the maximum quantity for receiving mails during one time */
  protected int maxMailQuantity;

  protected boolean mailSizeCheck = true;

  /* the maximum size for one single mail */
  protected int maxMailSize;

  protected String uri;

  static {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

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
    this.processHeader(mime, mailMsg);
    return this.processMessage(mime, mailMsg);
  }

  @Override
  public String readAttachments(String filePath) throws Exception {
    MimeMessage mime = this.readEMLToMessage(filePath);
    return this.processMessage(mime);
  }

  /**
   * Read an eml file from a specific file path and convert it to MimeMessage.
   * 
   * @param filePath
   * @return
   * @throws FileNotFoundException
   * @throws MessagingException
   * @throws IOException
   */
  private MimeMessage readEMLToMessage(String filePath) throws MessagingException, IOException {
    InputStream is = new FileInputStream(filePath);
    MimeMessage mime = new MimeMessage(null, is);
    if (is != null) {
      is.close();
    }
    return mime;
  }

  /**
   * Process message not including mail attachments according to specified MIME type.
   * 
   * @param mime
   * @param mailMsg
   * @return
   * @throws MessagingException
   * @throws IOException
   * @throws CMSException
   * @throws OperatorCreationException
   * @throws CertificateException
   * @throws ParseException
   */
  protected String processMessage(MimeMessage mime, MailMessage mailMsg) throws MessagingException, IOException, CMSException, OperatorCreationException, CertificateException, ParseException {
    String message = null;
    if (mime.isMimeType("text/html") || mime.isMimeType("text/plain") || mime.isMimeType("multipart/alternative") || mime.isMimeType("multipart/mixed")) {
      // simple mail with/without attachment
      message = this.processSimpleMail(mime, mailMsg);
    } else if (mime.isMimeType("multipart/signed")) {
      // signed mail with/without attachment
      this.validateSignedMail(mime, mailMsg);
      message = this.processSignedMail(mime, mailMsg);
    } else if (mime.isMimeType("application/pkcs7-mime") || mime.isMimeType("application/x-pkcs7-mime")) {
      String e = "It's an encrypted mail. Can not handle the Message receiving from Mail Server.";
      LOG.error(e);
      throw new MessagingException(e);
    } else {
      String e = "Unknown MIME Type | Message Content Type: " + mime.getContentType() + ", Message Subject: " + mime.getSubject() + ", Message Send Date: " + mime.getSentDate() + ", Message From: " + mime.getFrom().toString();
      LOG.error(e);
      throw new MessagingException(e);
    }
    return message;
  }

  /**
   * Process specified MIME type of MimeMessage to get attachments and save them to disk.
   * 
   * @param mime
   * @return
   * @throws MessagingException
   * @throws IOException
   * @throws CMSException
   * @throws OperatorCreationException
   * @throws CertificateException
   */
  protected String processMessage(MimeMessage mime) throws MessagingException, IOException, CMSException, OperatorCreationException, CertificateException {
    String attachList = null;
    if (mime.isMimeType("text/html") || mime.isMimeType("text/plain") || mime.isMimeType("multipart/mixed")) {
      // simple mail with attachment
      attachList = this.processAttachmentsOfSimpleMail(mime);
    } else if (mime.isMimeType("multipart/signed")) {
      // signed mail with attachment
      attachList = this.processAttachmentsOfSignedMail(mime);
    } else if (mime.isMimeType("application/pkcs7-mime") || mime.isMimeType("application/x-pkcs7-mime")) {
      String e = "It's an encrypted mail. Can not handle the Message receiving from Mail Server.";
      LOG.error(e);
      throw new MessagingException(e);
    } else {
      String e = "Unknown MIME Type | Message Content Type: " + mime.getContentType() + ", Message Subject: " + mime.getSubject() + ", Message Send Date: " + mime.getSentDate() + ", Message From: " + mime.getFrom().toString();
      LOG.error(e);
      throw new MessagingException(e);
    }
    return attachList;
  }

  /**
   * Process the MimeMessage for simple mail with/without attachment.
   * 
   * @param mime
   * @param mailMsg
   * @return
   * @throws IOException
   * @throws MessagingException
   * @throws ParseException
   */
  private String processSimpleMail(MimeMessage mime, MailMessage mailMsg) throws IOException, MessagingException, ParseException {
    mailMsg.setHasSignature(false);
    mailMsg.setSignaturePassed(false);
    this.setMailBasicInfoForMailMsg(mime, mailMsg);

    String txtBody = null;
    String htmlBody = null;
    if (mime.isMimeType("text/plain")) {
      txtBody = mime.getContent().toString();
    } else if (mime.isMimeType("text/html")) {
      htmlBody = mime.getContent().toString();
    } else if (mime.getContent() instanceof Multipart) {
      // Get the content of the messsage, it's an Multipart object like a package including all the email text and attachments.
      Multipart multi = (Multipart) mime.getContent();
      // First, verify the quantity and size of attachments.
      boolean isValidMailMsg = this.isValidMailMsg(multi);
      if (isValidMailMsg) {
        // process each part in order.
        for (int i = 0, n = multi.getCount(); i < n; i++) {
          // unpack, get each part of Multipart, part 0 may email text and part 1 may attachment. Or it is another embedded Multipart.
          Part part1 = multi.getBodyPart(i);
          if (part1.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part1.getDisposition())) {
            txtBody = part1.getContent().toString();
          } else if (part1.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part1.getDisposition())) {
            htmlBody = part1.getContent().toString();
          } else if (part1.isMimeType("multipart/alternative")) {
            // generally if the content type multipart/alternative, it is email text.
            if (part1.getContent() instanceof Multipart) {
              Multipart multi1 = (Multipart) part1.getContent();
              for (int k = 0; k < multi1.getCount(); k++) {
                Part part2 = multi1.getBodyPart(k);
                if (part2.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part2.getDisposition())) {
                  txtBody = part2.getContent().toString();
                } else if (part2.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part2.getDisposition())) {
                  htmlBody = part2.getContent().toString();
                }
              }
            }
          } else if (part1.isMimeType("multipart/related")) {
            if (part1.getContent() instanceof Multipart) {
              Multipart multi1 = (Multipart) part1.getContent();
              for (int m = 0; m < multi1.getCount(); m++) {
                Part part2 = multi1.getBodyPart(m);
                if (part2.isMimeType("multipart/alternative")) {
                  if (part2.getContent() instanceof Multipart) {
                    Multipart multi2 = (Multipart) part2.getContent();
                    for (int p = 0; p < multi2.getCount(); p++) {
                      Part part3 = multi2.getBodyPart(p);
                      if (part3.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part3.getDisposition())) {
                        txtBody = part3.getContent().toString();
                      } else if (part3.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part3.getDisposition())) {
                        htmlBody = part3.getContent().toString();
                      }
                    }
                  }
                } else if (part2.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part2.getDisposition())) {
                  txtBody = part2.getContent().toString();
                } else if (part2.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part2.getDisposition())) {
                  htmlBody = part2.getContent().toString();
                } else {
                  // This is an embedded picture, set it as an attachment.
                  mailMsg.setHasAttachments(true);
                }
              }
            }
          } else {
            String disposition = part1.getDisposition();
            if (disposition != null && Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
              mailMsg.setHasAttachments(true);
            }
          }
        }
      }
    }
    if (!isNull(txtBody)) {
      mailMsg.setBody(txtBody);
    } else {
      mailMsg.setBody(htmlBody);
    }
    return JSONObject.fromObject(mailMsg).toString();
  }

  /**
   * Process the MimeMessage from simple Mail to get attachments and save them to disk.
   * 
   * @param mime
   * @return
   * @throws IOException
   * @throws MessagingException
   */
  private String processAttachmentsOfSimpleMail(MimeMessage mime) throws IOException, MessagingException {
    List<Attachment> attachList = new ArrayList<Attachment>();

    if (mime.getContent() instanceof Multipart) {
      // Get the content of the messsage, it's an Multipart object like a package including all the email text and attachment.
      Multipart multi = (Multipart) mime.getContent();
      // First, verify the quantity and size of attachments.
      boolean isValidMailMsg = this.isValidMailMsg(multi);
      if (isValidMailMsg) {
        // process each part in order.
        for (int i = 0, n = multi.getCount(); i < n; i++) {
          // unpack, get each part of Multipart, part 0 may email text and part 1 may attachment. Or it is another embedded Multipart.
          Part part1 = multi.getBodyPart(i);
          if (part1.isMimeType("multipart/related")) {
            if (part1.getContent() instanceof Multipart) {
              Multipart multi1 = (Multipart) part1.getContent();
              for (int m = 0; m < multi1.getCount(); m++) {
                Part part2 = multi1.getBodyPart(m);
                if (!(part2.isMimeType("multipart/alternative") || part2.isMimeType("text/plain") || part2.isMimeType("text/html"))) {
                  // This is an embedded picture, set it as an attachment.
                  this.saveAttachment(part2, attachList);
                }
              }
            }
          } else {
            String disposition = part1.getDisposition();
            if (disposition != null && Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
              // Save the attachment if it is.
              this.saveAttachment(part1, attachList);
            }
          }
        }
      }
    }
    return JSONArray.fromObject(attachList).toString();
  }

  /**
   * Process the MimeMessage for signed mail with/without attachment.
   * 
   * @param mime
   * @param mailMsg
   * @return
   * @throws MessagingException
   * @throws IOException
   * @throws ParseException
   */
  private String processSignedMail(MimeMessage mime, MailMessage mailMsg) throws IOException, MessagingException, ParseException {
    this.setMailBasicInfoForMailMsg(mime, mailMsg);

    String txtBody = null;
    String htmlBody = null;
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
        boolean isValidMailMsg = this.isValidMailMsg(multi2);
        if (isValidMailMsg) {
          // process the content in multi2.
          for (int j = 0; j < multi2.getCount(); j++) {
            Part part3 = multi2.getBodyPart(j);
            if (part3.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part3.getDisposition())) {
              txtBody = part3.getContent().toString();
            } else if (part3.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part3.getDisposition())) {
              htmlBody = part3.getContent().toString();
            } else if (part3.isMimeType("multipart/alternative")) {
              // generally if the content type multipart/alternative, it is email text.
              if (part3.getContent() instanceof Multipart) {
                Multipart multi3 = (Multipart) part3.getContent();
                for (int k = 0; k < multi3.getCount(); k++) {
                  Part part4 = multi3.getBodyPart(k);
                  if (part4.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part4.getDisposition())) {
                    txtBody = part4.getContent().toString();
                  } else if (part4.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part4.getDisposition())) {
                    htmlBody = part4.getContent().toString();
                  }
                }
              }
            } else if (part3.isMimeType("multipart/related")) {
              if (part3.getContent() instanceof Multipart) {
                Multipart multi3 = (Multipart) part3.getContent();
                for (int m = 0; m < multi3.getCount(); m++) {
                  Part part4 = multi3.getBodyPart(m);
                  if (part4.isMimeType("multipart/alternative")) {
                    if (part4.getContent() instanceof Multipart) {
                      Multipart multi4 = (Multipart) part4.getContent();
                      for (int p = 0; p < multi4.getCount(); p++) {
                        Part part5 = multi4.getBodyPart(p);
                        if (part5.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part5.getDisposition())) {
                          txtBody = part5.getContent().toString();
                        } else if (part5.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part5.getDisposition())) {
                          htmlBody = part5.getContent().toString();
                        }
                      }
                    }
                  } else {
                    // This is an embedded picture, set it as an attachment.
                    mailMsg.setHasAttachments(true);
                  }
                }
              }
            } else {
              String disposition = part3.getDisposition();
              if (disposition != null && Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
                mailMsg.setHasAttachments(true);
              }
            }
          }
        }
      } else {
        // This is a certificate file, set it as an attachment
        String disposition = part2.getDisposition();
        if (disposition != null && Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
          mailMsg.setHasAttachments(true);
        }
      }
    }
    if (!isNull(txtBody)) {
      mailMsg.setBody(txtBody);
    } else {
      mailMsg.setBody(htmlBody);
    }
    return JSONObject.fromObject(mailMsg).toString();
  }

  /**
   * Process the MimeMessage from signed Mail to get attachments and save them to disk.
   * 
   * @param mime
   * @return
   * @throws MessagingException
   * @throws IOException
   */
  private String processAttachmentsOfSignedMail(MimeMessage mime) throws IOException, MessagingException {
    List<Attachment> attachList = new ArrayList<Attachment>();

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
        boolean isValidMailMsg = this.isValidMailMsg(multi2);
        if (isValidMailMsg) {
          // process the content in multi2.
          for (int j = 0; j < multi2.getCount(); j++) {
            Part part3 = multi2.getBodyPart(j);
            if (part3.isMimeType("multipart/related")) {
              if (part3.getContent() instanceof Multipart) {
                Multipart multi3 = (Multipart) part3.getContent();
                for (int m = 0; m < multi3.getCount(); m++) {
                  Part part4 = multi3.getBodyPart(m);
                  if (!part4.isMimeType("multipart/alternative")) {
                    // This is an embedded picture, save it.
                    this.saveAttachment(part4, attachList);
                  }
                }
              }
            } else {
              // Save the attachment.
              String disposition = part3.getDisposition();
              if (disposition != null && Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
                this.saveAttachment(part3, attachList);
              }
            }
          }
        }
      } else {
        // Process the attachment.(This is a certificate file.)
        String disposition = part2.getDisposition();
        if (disposition != null && Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
          this.saveAttachment(part2, attachList);
        }
      }
    }
    return JSONArray.fromObject(attachList).toString();
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
    Attachment attachment = new Attachment();
    attachment.setFileName(fileName);

    UUID uuid = UUID.randomUUID();
    String prefix = fileName.substring(0, fileName.lastIndexOf(".") + 1);
    String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
    String tempDir = System.getProperty("java.io.tmpdir");
    String filePath = tempDir + prefix + uuid + "." + suffix;

    int fileSize = part.getSize();
    attachment.setFilePath(filePath);
    attachment.setFileType(suffix);
    attachment.setFileSize(fileSize);
    attachList.add(attachment);
    this.saveFile(filePath, part.getInputStream());
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
    try {
      if (!file.exists()) {
        OutputStream out = null;
        out = new BufferedOutputStream(new FileOutputStream(file));
        in = new BufferedInputStream(in);
        byte[] buf = new byte[BUFFSIZE];
        int len;
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
        // close streams
        if (in != null) {
          in.close();
        }
        if (out != null) {
          out.close();
        }
      } else {
        String e = "Fail to save file. The " + fileName + " already exists.";
        LOG.error(e);
        throw new IOException(e);
      }
    } finally {
      // close streams
      if (in != null) {
        in.close();
      }
    }
  }

  /**
   * Set some email's basic information to MailMessage .
   * 
   * @param mime
   * @param mailMsg
   * @throws MessagingException
   * @throws ParseException
   */
  private void setMailBasicInfoForMailMsg(MimeMessage mime, MailMessage mailMsg) throws MessagingException, ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
    String msgId = mime.getMessageID();
    Address[] from = mime.getFrom();
    Address[] to = mime.getRecipients(RecipientType.TO);
    Address[] cc = mime.getRecipients(RecipientType.CC);
    Address[] bcc = mime.getRecipients(RecipientType.BCC);
    String subject = mime.getSubject();
    Date sendDate = mime.getSentDate();
    String receivedUTCDate = sdf.format(this.resolveReceivedDate(mime));

    mailMsg.setMsgId(msgId);
    mailMsg.setFrom(this.convertToMailAddress(from));
    mailMsg.setTo(this.convertToMailAddress(to));
    mailMsg.setCc(this.convertToMailAddress(cc));
    mailMsg.setBcc(this.convertToMailAddress(bcc));
    mailMsg.setSubject(subject);
    if (sendDate != null) {
      mailMsg.setSendDate(sdf.format(sendDate));
    }
    mailMsg.setReceivedDate(receivedUTCDate);
  }

  /**
   * Convert an array of addresses to a list of MailAddress.
   * 
   * @param addresses
   *          Address is a type of <code>javax.mail.Address<code>.
   * @return List&lt;MailAddress&gt;
   */
  protected List<MailAddress> convertToMailAddress(Address[] addresses) {
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
   * Convert an e-mail address to a list of MailAddress.
   * 
   * @param emailAddress
   *          EmailAddress is a type of <code>microsoft.exchange.webservices.data.property.complex.EmailAddress<code>.
   * @return List&lt;MailAddress&gt;
   */
  protected List<MailAddress> convertToMailAddress(EmailAddress emailAddress) {
    List<MailAddress> addressList = new ArrayList<MailAddress>();
    if (emailAddress != null) {
      MailAddress mailAddress = new MailAddress();
      mailAddress.setAddress(emailAddress.getAddress());
      mailAddress.setName(emailAddress.getName());
      addressList.add(mailAddress);
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
  protected void processHeader(Message msg, MailMessage mailMsg) throws MessagingException {
    String xAutoResponseSuppressHeaderName = "X-Auto-Response-Suppress";
    String xAutoReplyHeaderName = "X-Autoreply";
    String xAutoRespondHeaderName = "X-Autorespond";
    String xAutoSubmittedHeaderName = "auto-submitted";

    String xAutoResponseSuppressVal = Arrays.toString(msg.getHeader(xAutoResponseSuppressHeaderName));
    String xAutoReplyVal = Arrays.toString(msg.getHeader(xAutoReplyHeaderName));
    String xAutoRespondVal = Arrays.toString(msg.getHeader(xAutoRespondHeaderName));
    String xAutoSubmittedVal = Arrays.toString(msg.getHeader(xAutoSubmittedHeaderName));
    String contentType = msg.getContentType();

    // If any of those are present in an email, then that email is an auto-reply.
    String[] autoReplyArray = { xAutoResponseSuppressVal, xAutoReplyVal, xAutoRespondVal, xAutoSubmittedVal };
    mailMsg.setAutoReply(autoReplyArray);
    mailMsg.setContentType(contentType);
  }

  /**
   * Verify the mail's size and the attachments exceeding the maximum quantity.
   * 
   * @param multi
   * @return
   * @throws MessagingException
   * @throws IOException
   */
  private boolean isValidMailMsg(Multipart multi) throws MessagingException, IOException {
    boolean isValid = false;
    boolean exceedMaxMailSize = false;
    boolean exceedMaxAttachmentCount = false;

    if (this.countMailAttachments(multi) > maxattachmentcount) {
      exceedMaxAttachmentCount = true;
      LOG.info("The attachments' quantity exceed the maximum value!");
    }

    int mailAttachmentsSize = this.countMailAttachmentsSize(multi, 0);
    if (mailAttachmentsSize > attachmentsegmentsize * maxattachmentcount) {
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
   * Count the mail attachments' size, it is a total mail attachments' size.
   * 
   * @param multi
   * @param mailAttachmentsSize
   * @return
   * @throws MessagingException
   * @throws IOException
   */
  protected int countMailAttachmentsSize(Multipart multi, int mailAttachmentsSize) throws MessagingException, IOException {
    for (int i = 0, n = multi.getCount(); i < n; i++) {
      Part part = multi.getBodyPart(i);

      if (part.getContent() instanceof Multipart) {
        mailAttachmentsSize = this.countMailAttachmentsSize((Multipart) part.getContent(), mailAttachmentsSize);
      }
      int partSize = part.getSize();
      if (partSize != -1) {
        mailAttachmentsSize = mailAttachmentsSize + partSize;
      }
    }
    return mailAttachmentsSize;
  }

  /**
   * Check whether the given parameter is null or not.
   * 
   * @param s
   *          String
   * @return true means 's' is null or empty
   */
  protected static boolean isNull(String s) {
    if (s == null || "".equals(s)) {
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
   * Check the given protocol is IMAP or IMAP. If it is, return true.
   * 
   * @param protocol
   * @return
   */
  protected static boolean isIMAPorIMAPS(String protocol) {
    if (IMAP.equalsIgnoreCase(protocol) || IMAPS.equalsIgnoreCase(protocol)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Check the given protocol is POP3 or POP3S. If it is, return true.
   * 
   * @param protocol
   * @return
   */
  protected static boolean isPOP3orPOP3S(String protocol) {
    if (POP3.equalsIgnoreCase(protocol) || POP3S.equalsIgnoreCase(protocol)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Get the email received date from message header if it can not get directly.
   * 
   * @param message
   * @return
   * @throws MessagingException
   * @throws ParseException
   */
  protected Date resolveReceivedDate(MimeMessage message) throws MessagingException, ParseException {
    if (message.getReceivedDate() != null) {
      return message.getReceivedDate();
    }
    String[] receivedHeaders = message.getHeader("Received");
    if (receivedHeaders == null) {
      return Calendar.getInstance().getTime();
    }
    SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
    Date finalDate = Calendar.getInstance().getTime();
    finalDate.setTime(0l);
    boolean found = false;
    for (String receivedHeader : receivedHeaders) {
      Pattern pattern = Pattern.compile("^[^;]+;(.+)$");
      Matcher matcher = pattern.matcher(receivedHeader);
      if (matcher.matches()) {
        String regexpMatch = matcher.group(1);
        if (regexpMatch != null) {
          regexpMatch = regexpMatch.trim();
          Date parsedDate = sdf.parse(regexpMatch);
          if (parsedDate.after(finalDate)) {
            // finding the later date mentioned in received header
            finalDate = parsedDate;
            found = true;
          }
        } else {
          LOG.error("Unable to match received date in header string: " + receivedHeader);
        }
      }
    }
    return found ? finalDate : Calendar.getInstance().getTime();
  }
}