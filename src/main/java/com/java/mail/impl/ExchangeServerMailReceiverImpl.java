package com.java.mail.impl;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.SearchTerm;

import org.apache.commons.logging.LogFactory;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.operator.OperatorCreationException;

import com.hp.ov.sm.common.core.JLog;
import com.java.mail.domain.Attachment;
import com.java.mail.domain.MailAddress;
import com.java.mail.domain.MailMessage;

import net.sf.json.JSONArray;

public class ExchangeServerMailReceiverImpl extends AbstractMailReceiver {
  private static final JLog LOG = new JLog(LogFactory.getLog(ExchangeServerMailReceiverImpl.class));

  @Override
  public void open() throws Exception {
    Properties props = this.getProperties();
    this.session = Session.getDefaultInstance(props, null);
    this.store = this.session.getStore(this.protocol);
    this.store.connect(this.host, this.username, this.password);
    this.profile = new FetchProfile();
    this.profile.add(UIDFolder.FetchProfileItem.UID);
    this.profile.add(FetchProfile.Item.ENVELOPE);

    // Check the folder existing or not. If not, create a new folder.
    Folder defaultFolder = this.store.getDefaultFolder();
    if (!this.checkOrCreateExchangeServerFolder(defaultFolder, this.sourceFolderName) && !this.checkOrCreateExchangeServerFolder(defaultFolder, this.toFolderName)) {
      String e = "Fail to create the folder.";
      LOG.error(e);
      throw new Exception(e);
    }

    // open mail folder
    /* POP3Folder can only receive the mails in 'INBOX', IMAPFolder can receive the mails in all folders including created by user. */
    this.sourceFolder = this.openFolder(this.sourceFolderName, Folder.READ_WRITE);
    this.toFolder = this.openFolder(this.toFolderName, Folder.READ_ONLY);
  }

  @Override
  public JSONArray getMsgIdList() throws Exception {
    long begin = System.currentTimeMillis();
    JSONArray jsonArray = null;
    List<String> msgIdList = new ArrayList<String>();

    Message[] msgs = this.sourceFolder.getMessages();
    // Get mails and UID
    this.sourceFolder.fetch(msgs, this.profile);
    // restrict reading mail message size to 'maxMailSize'.
    for (Message msg : msgs) {
      MimeMessage mmsg = (MimeMessage) msg;
      msgIdList.add(mmsg.getMessageID());
    }
    jsonArray = JSONArray.fromObject(msgIdList);
    long end = System.currentTimeMillis();
    System.out.println("getMsgIdList(): " + (end - begin) + " ms");
    return jsonArray;
  }

  @Override
  public JSONArray receive(String messageId, boolean save) throws Exception {
    long begin = System.currentTimeMillis();
    JSONArray jsonArray = null;
    // receive mails according to the message id.
    SearchTerm st = new MessageIDTerm(messageId);
    Message[] messages = this.sourceFolder.search(st);

    MailMessage mailMsg = new MailMessage();
    mailMsg.setMsgId(messageId);
    Message msg = messages[0];
    this.getHeader(msg, mailMsg);
    mailMsg = this.processMsg(msg, mailMsg, save);
    jsonArray = JSONArray.fromObject(mailMsg);
    long end = System.currentTimeMillis();
    System.out.println("receive(): " + (end - begin) + " ms");
    return jsonArray;
  }

  @Override
  public JSONArray receiveAttachment(String messageId) throws Exception {
    return this.receive(messageId, true);
  }

  @Override
  public void moveMessage(String messageId) throws Exception {
    if ((this.sourceFolder != null && this.sourceFolder.isOpen()) && this.toFolder != null) {
      // receive mails according to the message id.
      SearchTerm st = new MessageIDTerm(messageId);
      Message[] messages = this.sourceFolder.search(st);
      Message msg = messages[0];
      if (null != msg) {
        Message[] needCopyMsgs = new Message[1];
        needCopyMsgs[0] = msg;
        // Copy the msg to the specific folder
        this.sourceFolder.copyMessages(needCopyMsgs, this.toFolder);
        // delete the original msg
        // only add a delete flag on the message, it will not
        // indeed to execute the delete operation.
        msg.setFlag(Flags.Flag.DELETED, true);
      } else {
        String e = "No message find.";
        LOG.error(e);
        throw new Exception(e);
      }
    } else {
      String e = "The folder is null or closed.";
      LOG.error(e);
      throw new Exception(e);
    }
  }

  @Override
  public void close() throws MessagingException {
    // close the folder, true means that will indeed to delete the message,
    // false means that will not delete the message.
    this.closeFolder(this.sourceFolderName, true);
    this.closeFolder(this.toFolderName, true);
    this.store.close();
  }

  /**
   * Get properties for mail session.
   * 
   * @return Properties
   */
  private Properties getProperties() {
    Properties props = new Properties();
    props.put("mail.smtp.host", this.host);
    props.put("mail.smtp.port", this.port);
    props.put("mail.smtp.auth", "true");
    props.put("mail.store.protocol", this.protocol);

    props.put("mail.imap.partialfetch", "false");
    props.put("mail.imaps.partialfetch", "false");

    props.put("mail.imap.fetchsize", "1048576");
    props.put("mail.imaps.fetchsize", "1048576");

    props.put("mail.imap.starttls.enable", "true");
    props.put("mail.imaps.starttls.enable", "true");

    props.put("mail.imap.compress.enable", "true");
    props.put("mail.imaps.compress.enable", "true");
    // Proxy
    if (this.proxySet) {
      props.put("proxySet", "true");
      props.put("http.proxyHost", this.proxyHost);
      props.put("http.proxyPort", this.proxyPort);
    }
    /*
     * General Issues with Multiparts
     * 
     * @see http://www.bouncycastle.org/wiki/display/JA1/CMS+and+SMIME+APIs
     */
    // props.put("mail.mime.cachemultipart", false);

    return props;
  }

  /**
   * Check the folder is existing or not. If not, create a new folder.
   * 
   * @param parent
   * @param folderName
   * @return true if the creation succeeds or the folder exists, else false.
   * @throws Exception
   */
  private boolean checkOrCreateExchangeServerFolder(Folder parent, String folderName) throws Exception {
    boolean isCreated = false;
    boolean folderExists = false;
    folderExists = this.store.getFolder(folderName).exists();
    if (!folderExists) {
      if ((IMAP.equalsIgnoreCase(this.protocol) || IMAPS.equalsIgnoreCase(this.protocol))) {
        // If parent is not the root directory, the folder should be opened.
        // parent.open(Folder.READ_WRITE);
        Folder newFolder = parent.getFolder(folderName);
        isCreated = newFolder.create(Folder.HOLDS_MESSAGES);
        // parent.close(true);
      } else {
        String e = "If you want to assign a specific folder, the protocol should be IMAP or IMAPS.";
        LOG.error(e);
        throw new Exception(e);
      }
    }
    return isCreated || folderExists;
  }

  /**
   * Open a specific folder according to the folder name and assign the mode Folder READ_ONLY or READ_WRITE
   * 
   * @param folderName
   * @param mode
   * @return
   * @throws MessagingException
   */
  private Folder openFolder(String folderName, int mode) throws MessagingException {
    Folder folder = null;
    folder = this.store.getFolder(folderName);
    folder.open(mode);
    return folder;
  }

  /**
   * Get message header for MailMessage.
   * 
   * @param msg
   * @param mailMsg
   * @throws MessagingException
   */
  private void getHeader(Message msg, MailMessage mailMsg) throws MessagingException {
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
   * Process message according to specified MIME type.
   * 
   * @param messages
   * @param save
   * @return
   * @throws MessagingException
   * @throws IOException
   * @throws CMSException
   * @throws CertificateException
   * @throws OperatorCreationException
   */
  private MailMessage processMsg(Message message, MailMessage mailMsg, boolean save) throws MessagingException, IOException, CMSException, OperatorCreationException, CertificateException {
    MimeMessage msg = (MimeMessage) message;

    if (msg.isMimeType("text/html") || msg.isMimeType("text/plain")) {
      // simple mail without attachment
      this.setExchangeServerMailForMailMsg(msg, mailMsg, save);
    } else if (msg.isMimeType("multipart/mixed")) {
      // simple mail with attachment
      this.setExchangeServerMailForMailMsg(msg, mailMsg, save);
    } else if (msg.isMimeType("multipart/signed")) {
      // signed mail with/without attachment
      this.validateSignedMail(msg, mailMsg);
      this.setExchangeServerSignedMailForMailMsg(msg, mailMsg, save);
    } else if (msg.isMimeType("application/pkcs7-mime") || msg.isMimeType("application/x-pkcs7-mime")) {
      String e = "It's an encrypted mail. Can not handle the Message receiving from Mail Server.";
      LOG.error(e);
      throw new MessagingException(e);
    } else {
      String e = "Unknown MIME Type | Message Content Type: " + msg.getContentType() + "Message Subject: " + msg.getSubject() + "Message Send Date: " + msg.getSentDate() + "Message From: " + msg.getFrom().toString();
      LOG.error(e);
      throw new MessagingException(e);
    }
    return mailMsg;
  }

  /**
   * Set MailMessage for simple mail with/without attachment.
   * 
   * @param msg
   * @param mailMsg
   * @param save
   * @return
   * @throws MessagingException
   * @throws IOException
   */
  private MailMessage setExchangeServerMailForMailMsg(Message msg, MailMessage mailMsg, boolean save) throws IOException, MessagingException {
    List<Attachment> attachList = new ArrayList<Attachment>();
    mailMsg.setSignaturePassed(false);
    this.setExchangeServerBasicInfoForMailMsg(msg, mailMsg);

    if (msg.getContent() instanceof Multipart) {
      // Get the content of the messsage, it's an Multipart object like a package including all the email text and attachment.
      Multipart multi1 = (Multipart) msg.getContent();
      // First, verify the quantity and size of attachments.
      boolean isValidMailMsg = this.isValidMailMsg(multi1, mailMsg);
      if (isValidMailMsg) {
        // process each part in order.
        for (int i = 0, n = multi1.getCount(); i < n; i++) {
          // unpack, get each part of Multipart, part 0 may email
          // text and part 1 may attachment. Or it is another
          // embedded Multipart.
          Part part = multi1.getBodyPart(i);
          if (part.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
            mailMsg.setTxtBody(part.getContent().toString());
          } else if (part.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
            mailMsg.setHtmlBody(part.getContent().toString());
          } else {
            // Process the attachment if it is.
            this.processAttachment(part, mailMsg, attachList, save);
          }
        }
      }
    } else if (!this.exceedMaxMsgSize(msg.getSize())) {
      if (msg.isMimeType("text/plain")) {
        mailMsg.setTxtBody(msg.getContent().toString());
      } else if (msg.isMimeType("text/html")) {
        mailMsg.setHtmlBody(msg.getContent().toString());
      }
    }
    return mailMsg;
  }

  /**
   * Validate signed mail.
   * 
   * @param msg
   * @param mailMsg
   * @return
   * @throws MessagingException
   * @throws IOException
   * @throws CMSException
   * @throws CertificateException
   * @throws OperatorCreationException
   */
  private boolean validateSignedMail(Message msg, MailMessage mailMsg) throws MessagingException, CMSException, IOException, OperatorCreationException, CertificateException {
    boolean verify = false;
    /*
     * Add a header to make a new message in order to fix the issue of Outlook
     * 
     * @see http://www.bouncycastle.org/wiki/display/JA1/CMS+and+SMIME+APIs
     * 
     * @see http://stackoverflow.com/questions/8590426/s-mime-verification-with- x509-certificate
     */
    MimeMessage newmsg = new MimeMessage((MimeMessage) msg);
    newmsg.setHeader("Nothing", "Add a header for verifying signature only.");
    newmsg.saveChanges();
    SMIMESigned signed = new SMIMESigned((MimeMultipart) newmsg.getContent());
    verify = this.isValid(signed, mailMsg);
    return verify;
  }

  /**
   * Set MailMessage for signed mail with/without attachment.
   * 
   * @param msg
   * @param mailMsg
   * @param save
   * @return
   * @throws MessagingException
   * @throws IOException
   */
  private MailMessage setExchangeServerSignedMailForMailMsg(Message msg, MailMessage mailMsg, boolean save) throws IOException, MessagingException {
    List<Attachment> attachList = new ArrayList<Attachment>();

    this.setExchangeServerBasicInfoForMailMsg(msg, mailMsg);
    // Get the content of the messsage, it's an Multipart object like a package including all the email text and attachment.
    Multipart multi1 = (Multipart) msg.getContent();

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
          if (part3.isMimeType("multipart/alternative")) {
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
                        mailMsg.setTxtBody(part5.getContent().toString());
                      } else if (part5.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part5.getDisposition())) {
                        mailMsg.setHtmlBody(part5.getContent().toString());
                      }
                    }
                  }
                } else if (isValidMailMsg) {
                  // Process the attachment.
                  this.processEmailBodyAttachment(part4, mailMsg, attachList, save);
                }
              }
            }
          } else if (part3.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part3.getDisposition())) {
            mailMsg.setTxtBody(part3.getContent().toString());
          } else if (part3.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part3.getDisposition())) {
            mailMsg.setHtmlBody(part3.getContent().toString());
          } else if (isValidMailMsg) {
            // Process the attachment.
            this.processAttachment(part3, mailMsg, attachList, save);
          }
        }
      } else {
        // Process the attachment.(This is a certificate file.)
        this.processAttachment(part2, mailMsg, attachList, save);
      }
    }
    return mailMsg;
  }

  /**
   * Set some basic information to MailMessage .
   * 
   * @param msg
   * @param mailMsg
   * @throws MessagingException
   */
  private void setExchangeServerBasicInfoForMailMsg(Message msg, MailMessage mailMsg) throws MessagingException {
    Address[] from = msg.getFrom();
    Address[] to = msg.getRecipients(RecipientType.TO);
    Address[] cc = msg.getRecipients(RecipientType.CC);
    Address[] bcc = msg.getRecipients(RecipientType.BCC);
    String subject = msg.getSubject();
    Date sendDate = msg.getSentDate();

    mailMsg.setFrom(this.convertToMailAddress(from));
    mailMsg.setTo(this.convertToMailAddress(to));
    mailMsg.setCc(this.convertToMailAddress(cc));
    mailMsg.setBcc(this.convertToMailAddress(bcc));
    mailMsg.setSubject(subject);
    mailMsg.setSendDate(sendDate);
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
   * Close a folder according to the folder name if it opens.
   * 
   * @param folderName
   * @param expunge
   * @throws MessagingException
   */
  private void closeFolder(String folderName, boolean expunge) throws MessagingException {
    Folder folder = this.store.getFolder(folderName);
    if (folder != null && folder.isOpen()) {
      // close the folder, true means that will indeed to delete the
      // message, false means that will not delete the message.
      folder.close(expunge);
    }
  }

}
