/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

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
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

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
import com.java.mail.ExceedException;
import com.java.mail.MailUtil;
import com.java.mail.ReceiveMail;
import com.java.mail.UnknownMimeTypeException;
import com.java.mail.domain.Attachment;
import com.java.mail.domain.MailMessage;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.ComparisonMode;
import microsoft.exchange.webservices.data.core.enumeration.search.ContainmentMode;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceVersionException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.EmailAddressCollection;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author zhontao
 *
 */
public class ReceiveMailImpl implements ReceiveMail {

  private static final JLog LOG = new JLog(LogFactory.getLog(ReceiveMailImpl.class));

  private static final String PROVIDER_NAME = "BC";

  private static final int DEFAULT_MAX_ATTACHMENT_COUNT = 100;

  /* Unit is bit */
  private static final int DEFAULT_ATTACHMENT_SEGMENT_SIZE = 512000;

  /* the default value of the maximum quantity for receiving mails during one time */
  private static final int DEFAULT_MAX_MAIL_QUANTITY = 100;

  private static final String DEFAULT_SOURCE_FOLDER_NAME = "INBOX";

  private static final String DEFAULT_TO_FOLDER_NAME = "Deleted Items";

  /* Incoming Mail" server, eg. webmail.hp.com */
  private String host;

  /* eg. SSL ports are 993 for IMAP and 995 for POP3 */
  private String port;

  /* eg. true or false, whether Incoming Mail server needs authentication or not */
  private boolean auth;

  /* eg. POP3, POP3S, IMAP or IMAPS */
  private String protocol;

  /* eg. joe.smith@hp.com */
  private String username;

  private String password;

  /* the maximum quantity for receiving mails during one time */
  private int maxMailQuantity;

  /* the file suffix which can be saved, others will be ignored */
  private List<String> suffixList;

  private List<String> authorisedUserList;

  /* eg. true or false, whether Incoming Mail server needs authentication or not */
  private boolean proxySet;

  private String proxyHost;

  private String proxyPort;

  private String proxyUser;

  private String proxyPassword;

  private String proxyDomain;

  private String fromStringTerm;

  private String subjectTerm;

  private String sourceFolderName;

  private String toFolderName;

  private Session session;

  private Store store;

  private Folder sourceFolder;

  private Folder toFolder;

  private FetchProfile profile;

  private static int attachmentsegmentsize;

  private static int maxattachmentcount;

  private ExchangeService service;

  private String uri;

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


  /*
   * (non-Javadoc)
   * 
   * @see com.java.mail.ReceiveMail#initialize(java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public void initialize(String jsonParam) throws Exception {
    JSONObject jsonObject = JSONObject.fromObject(jsonParam);
    Map<String, Object> map = MailUtil.convertJsonToMap(jsonObject);
    if (map != null && !map.isEmpty()) {
      this.host = (String) map.get("host");
      this.port = (String) map.get("port");
      this.auth = true;
      this.protocol = (String) map.get("protocol");
      this.username = (String) map.get("username");
      this.password = (String) map.get("password");
      this.maxMailQuantity = (Integer) map.get("maxMailQuantity");
      this.suffixList = (List<String>) map.get("suffixList");
      this.authorisedUserList = (List<String>) map.get("authorisedUserList");
      this.proxySet = (Boolean) map.get("proxySet");
      if (this.protocol.equalsIgnoreCase("EWS")) {
        this.username = (String) map.get("username");
        this.password = (String) map.get("password");
        this.uri = (String) map.get("uri");
        this.service = new ExchangeService();
        ExchangeCredentials credentials = new WebCredentials(this.username, this.password);
        this.service.setCredentials(credentials);
        this.service.setUrl(new URI(this.uri));
        if (this.proxySet) {
          // For EWS proxy
          this.proxyHost = (String) map.get("proxyHost");
          this.proxyPort = (String) map.get("proxyPort");
          this.proxyUser = (String) map.get("proxyUser");
          this.proxyPassword = (String) map.get("proxyPassword");
          this.proxyDomain = (String) map.get("proxyDomain");
        }
      } else if (this.proxySet) {
        this.proxyHost = (String) map.get("proxyHost");
        this.proxyPort = (String) map.get("proxyPort");
      }
      // this.fromStringTerm = (String) map.get("fromStringTerm");
      // this.subjectTerm = (String) map.get("subjectTerm");
      this.sourceFolderName = (String) map.get("sourceFolderName");
      this.toFolderName = (String) map.get("toFolderName");

      if (isNull(this.protocol)) {
        String msg = "Missing mandatory values, please check that you have entered the protocol.";
        LOG.error(msg);
        throw new Exception(msg);
      } else if (this.protocol.equalsIgnoreCase("EWS")) {
        if (isNull(this.username) || isNull(this.password) || isNull(this.uri)) {
          String msg = "Missing mandatory values, please check that you have entered the username, password or uri.";
          LOG.error(msg);
          throw new Exception(msg);
        }
      } else if (isNull(this.host) || isNull(this.port) || isNull(this.username) || isNull(this.password)) {
        String msg = "Missing mandatory values, please check that you have entered the host, port, username or password.";
        LOG.error(msg);
        throw new Exception(msg);
      } else if (!isAuthorisedUsername(this.authorisedUserList, this.username)) {
        String msg = "The user name is not belong to authorised user domain!";
        LOG.error(msg);
        throw new Exception(msg);
      } else {
        if (this.maxMailQuantity == 0) {
          this.maxMailQuantity = DEFAULT_MAX_MAIL_QUANTITY;
        }
        if (isNull(this.sourceFolderName)) {
          this.sourceFolderName = DEFAULT_SOURCE_FOLDER_NAME;
        }
        if (isNull(this.toFolderName)) {
          this.toFolderName = DEFAULT_TO_FOLDER_NAME;
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.java.mail.ReceiveMail#open()
   */
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
    // String target_folder = "INBOX";
    // Folder defaultFolder = this.store.getFolder(target_folder);
    this.checkAndCreateFolder(defaultFolder, this.sourceFolderName);
    this.checkAndCreateFolder(defaultFolder, this.toFolderName);

    // open mail folder
    /* POP3Folder can only receive the mails in 'INBOX', IMAPFolder can receive the mails in all folders including created by user. */
    this.sourceFolder = this.openFolder(this.sourceFolderName, Folder.READ_WRITE);
    this.toFolder = this.openFolder(this.toFolderName, Folder.READ_ONLY);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.java.mail.ReceiveMail#receive()
   */
  public JSONArray receive(String fromStringTerm, String subjectTerm) throws Exception {
    // Only receive new mails.
    // FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.RECENT), true);
    SearchTerm st = new OrTerm(new FromStringTerm(fromStringTerm), new SubjectTerm(subjectTerm));
    Message[] messages = this.sourceFolder.search(st);

    // Get mails and UID
    this.sourceFolder.fetch(messages, this.profile);

    // restrict reading mail message size to 'maxMailSize'.
    List<MailMessage> msgList = null;
    int msgsLength = messages.length;
    if (msgsLength == 0) {
      return null;
    } else if (msgsLength > this.maxMailQuantity) {
      msgList = this.processMsg(messages, this.maxMailQuantity, this.sourceFolder, this.toFolder);
    } else {
      msgList = this.processMsg(messages, msgsLength, this.sourceFolder, this.toFolder);
    }

    JSONArray jsonArray = JSONArray.fromObject(msgList);

    return jsonArray;
  }

  public JSONArray receiveThroughEWS(String fromStringTerm, String subjectTerm, int pageSize) throws Exception {
    if (pageSize > this.maxMailQuantity) {
      pageSize = this.maxMailQuantity;
    }
    ItemView view = new ItemView(pageSize);
    // view.setPropertySet(new PropertySet(BasePropertySet.IdOnly, ItemSchema.Subject, ItemSchema.DateTimeReceived));

    SearchFilter.ContainsSubstring fromTermFilter = new SearchFilter.ContainsSubstring(EmailMessageSchema.From, fromStringTerm);
    SearchFilter.ContainsSubstring subjectFilter = new SearchFilter.ContainsSubstring(ItemSchema.Subject, subjectTerm, ContainmentMode.Substring, ComparisonMode.IgnoreCase);
    FindItemsResults<Item> findResults = this.service.findItems(WellKnownFolderName.Inbox, new SearchFilter.SearchFilterCollection(LogicalOperator.And, fromTermFilter, subjectFilter), view);

    System.out.println("Total number of items found: " + findResults.getTotalCount());
    List<MailMessage> msgList = new ArrayList<MailMessage>();
    for (Item item : findResults) {
      MailMessage mailMsg = this.readEmailItem(item.getId());
      msgList.add(mailMsg);
    }

    JSONArray jsonArray = JSONArray.fromObject(msgList);
    return jsonArray;
  }

  /**
   * Reading one email at a time. Using Item ID of the email.
   * Creating a message data map as a return value.
   */
  public MailMessage readEmailItem(ItemId itemId) {
    MailMessage mailMsg = new MailMessage();
    try {
      Item item = Item.bind(this.service, itemId, PropertySet.FirstClassProperties);
      EmailMessage emailMessage = EmailMessage.bind(this.service, item.getId());

      this.setMailMsgForBasicInfo(emailMessage, mailMsg);
      this.setMailMsgForSimpleMail(emailMessage, mailMsg);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return mailMsg;
  }

  /**
   * Process attachments.
   * 
   * @param part
   * @param mailMsg
   * @param attachList
   * @throws ServiceVersionException
   * @throws IOException
   */
  private void processEWSAttachment(FileAttachment fileAttachment, MailMessage mailMsg, List<Attachment> mailAttachList) throws ServiceVersionException, IOException {
    // generate a new file name with unique UUID.
    String fileName = fileAttachment.getName();
    UUID uuid = UUID.randomUUID();
    String prefix = fileName.substring(0, fileName.lastIndexOf(".") + 1);
    String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
    if (this.suffixList.contains(suffix.toLowerCase())) {
      String tempDir = System.getProperty("java.io.tmpdir");
      fileName = tempDir + prefix + uuid + "." + suffix;

      int fileSize = fileAttachment.getSize();
      com.java.mail.domain.Attachment mailAttachment = new com.java.mail.domain.Attachment();
      mailAttachment.setFileName(fileName);
      mailAttachment.setFileType(suffix);
      mailAttachment.setFileSize(fileSize);
      mailAttachList.add(mailAttachment);
      mailMsg.setAttachList(mailAttachList);
      this.saveByteFile(fileName, fileAttachment.getContent(), fileSize);
    }
  }

  private void saveByteFile(String fileName, byte[] buf, int fileSize) throws IOException {
    File file = new File(fileName);
    if (!file.exists()) {
      OutputStream out = null;
      try {
        out = new BufferedOutputStream(new FileOutputStream(file));
        if (buf != null && buf.length != 0) {
          out.write(buf);
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } finally {
        if (out != null) {
          out.close();
        }
      }
    }
  }

  /**
   * Set some basic information to MailMessage .
   * 
   * @param msg
   * @param mailMsg
   * @throws ServiceLocalException
   */
  private void setMailMsgForBasicInfo(EmailMessage emailMessage, MailMessage mailMsg) throws ServiceLocalException {
    String id = emailMessage.getId().toString();
    EmailAddress from = emailMessage.getFrom();
    EmailAddressCollection to = emailMessage.getToRecipients();
    EmailAddressCollection cc = emailMessage.getCcRecipients();
    EmailAddressCollection bcc = emailMessage.getBccRecipients();
    String subject = emailMessage.getSubject();
    Date sendDate = emailMessage.getDateTimeCreated();

    mailMsg.setId(id);
    mailMsg.setFrom(MailUtil.convertToMailAddress(from));
    mailMsg.setTo(MailUtil.convertToMailAddress(to));
    mailMsg.setCc(MailUtil.convertToMailAddress(cc));
    mailMsg.setBcc(MailUtil.convertToMailAddress(bcc));
    mailMsg.setSubject(subject);
    mailMsg.setSendDate(sendDate);
  }

  private MailMessage setMailMsgForSimpleMail(EmailMessage emailMessage, MailMessage mailMsg) throws ServiceVersionException, ServiceLocalException, Exception {
    if (!this.exceedMaxMsgSize(emailMessage.getSize())) {
      String emailBody = emailMessage.getBody().toString();
      if (emailMessage.getHasAttachments()) {
        List<microsoft.exchange.webservices.data.property.complex.Attachment> attachmentList = emailMessage.getAttachments().getItems();
        boolean exceedMaxAttachmentCount = false;
        if (attachmentList.size() > maxattachmentcount) {
          exceedMaxAttachmentCount = true;
        }
        if (!exceedMaxAttachmentCount) {
          List<Attachment> mailAttachList = new ArrayList<Attachment>();
          for (microsoft.exchange.webservices.data.property.complex.Attachment attachment : attachmentList) {
            if (attachment instanceof FileAttachment) {
              FileAttachment fileAttachment = (FileAttachment) attachment;
              fileAttachment.load();
              this.processEWSAttachment(fileAttachment, mailMsg, mailAttachList);
            }
          }
        }
      }
      mailMsg.setHtmlBody(emailBody);
    }
    return mailMsg;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.java.mail.ReceiveMail#moveMessage(javax.mail.Message)
   */
  public void moveMessage(Message msg) throws MessagingException {
    Folder sourceFolder = this.getExistingFolder(this.sourceFolderName);
    Folder toFolder = this.getExistingFolder(this.toFolderName);
    if ((sourceFolder != null && sourceFolder.isOpen()) && (toFolder != null && toFolder.isOpen())) {
      // Move message
      if (null != msg) {
        Message[] needCopyMsgs = new Message[1];
        needCopyMsgs[0] = msg;
        // Copy the msg to the specific folder
        sourceFolder.copyMessages(needCopyMsgs, toFolder);
        // delete the original msg
        // only add a delete flag on the message, it will not indeed to execute the delete operation.
        msg.setFlag(Flags.Flag.DELETED, true);
      }
    } else {
      String e = "The folder is null or closed!";
      LOG.error(e);
      throw new MessagingException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.java.mail.ReceiveMail#close()
   */
  public void close() throws MessagingException {
    // close the folder, true means that will indeed to delete the message, false means that will not delete the message.
    this.closeFolder(this.sourceFolderName, true);
    this.closeFolder(this.toFolderName, true);
    this.store.close();
  }




  /**
   * Process message according to specified MIME type.
   * 
   * @param messages
   * @param maxMailSize
   * @param sourceFolder
   * @param toFolderName
   * @return
   * @throws MessagingException
   * @throws IOException
   * @throws OperatorCreationException
   * @throws CMSException
   * @throws UnknownMimeTypeException
   * @throws CertificateException
   * @throws ExceedException
   */
  private List<MailMessage> processMsg(Message[] messages, int maxMailSize, Folder sourceFolder, Folder toFolder) throws MessagingException, IOException, OperatorCreationException, CMSException, UnknownMimeTypeException, CertificateException, ExceedException {
    List<MailMessage> msgList = new ArrayList<MailMessage>();
    MailMessage mailMsg = new MailMessage();
    for (int i = 0; i < maxMailSize; i++) {
      Message msg = messages[i];
      mailMsg.setContentType(msg.getContentType());

      if (msg.isMimeType("text/html") || msg.isMimeType("text/plain")) {
        // simple mail without attachment
        this.setMailMsgForSimpleMail(msg, mailMsg);
      } else if (msg.isMimeType("multipart/mixed")) {
        // simple mail with attachment
        this.setMailMsgForSimpleMail(msg, mailMsg);
      } else if (msg.isMimeType("multipart/signed")) {
        // signed mail with/without attachment
        this.validateSignedMail(msg, mailMsg);
        this.setMailMsgForSignedMail(msg, mailMsg);
      } else if (msg.isMimeType("application/pkcs7-mime") || msg.isMimeType("application/x-pkcs7-mime")) {
        mailMsg.setContentType(msg.getContentType());
        String e = "It's an encrypted mail. Can not handle the Message receiving from Mail Server.";
        LOG.error(e);
      } else {
        String e = "Message Content Type: " + msg.getContentType() + "Message Subject: " + msg.getSubject() + "Message Send Date: " + msg.getSentDate() + "Message From: " + msg.getFrom().toString() + "It is an unkonwn MIME type. Can not handle the Message receiving from Mail Server.";
        LOG.error(e);
        throw new UnknownMimeTypeException(e);
      }
      // this.moveMessage(msg, sourceFolder, toFolder);
      msgList.add(mailMsg);
    }
    return msgList;
  }

  /**
   * Set MailMessage for simple mail with/without attachment.
   * 
   * @param msg
   * @param mailMsg
   * @return
   * @throws IOException
   * @throws MessagingException
   * @throws ExceedException
   */
  private MailMessage setMailMsgForSimpleMail(Message msg, MailMessage mailMsg) throws IOException, MessagingException, ExceedException {
    List<Attachment> attachList = new ArrayList<Attachment>();

    mailMsg.setSignaturePassed(false);

    this.setMailMsgForBasicInfo(msg, mailMsg);

    if (msg.getContent() instanceof Multipart) {
      // Get the content of the messsage, it's an Multipart object like a package including all the email text and attachment.
      Multipart multi1 = (Multipart) msg.getContent();
      // First, verify the quantity and size of attachments. If the attachments exceed the maximum quantity and size, the code will throw an ExceedException.
      boolean isValidMailMsg = this.isValidMailMsg(multi1);
      if (isValidMailMsg) {
        // process each part in order.
        for (int i = 0, n = multi1.getCount(); i < n; i++) {
          // unpack, get each part of Multipart, part 0 may email text and part 1 may attachment. Or it is another embedded Multipart.
          Part part = multi1.getBodyPart(i);
          if (part.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
            mailMsg.setTxtBody(part.getContent().toString());
          } else if (part.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
            mailMsg.setHtmlBody(part.getContent().toString());
          } else {
            // Process the attachment if it is.
            this.processAttachment(part, mailMsg, attachList);
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
   * Set MailMessage for signed mail with/without attachment.
   * 
   * @param msg
   * @param mailMsg
   * @return
   * @throws IOException
   * @throws MessagingException
   * @throws ExceedException
   */
  private MailMessage setMailMsgForSignedMail(Message msg, MailMessage mailMsg) throws IOException, MessagingException, ExceedException {
    List<Attachment> attachList = new ArrayList<Attachment>();

    this.setMailMsgForBasicInfo(msg, mailMsg);

    // Get the content of the messsage, it's an Multipart object like a package including all the email text and attachment.
    Multipart multi1 = (Multipart) msg.getContent();
    // process each part in order.
    for (int i = 0, n = multi1.getCount(); i < n; i++) {
      // unpack, get each part of Multipart, part 0 may email text and part 1 may attachment. Or it is another embedded Multipart.
      Part part2 = multi1.getBodyPart(i);
      // determine Part is email text or Multipart.
      if (part2.getContent() instanceof Multipart) {
        Multipart multi2 = (Multipart) part2.getContent();
        // First, verify the quantity and size of attachments. If the attachments exceed the maximum quantity and size, the code will throw an ExceedException.
        boolean isValidMailMsg = this.isValidMailMsg(multi2);
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
                  this.processEmailBodyAttachment(part4, mailMsg, attachList);
                }
              }
            }
          } else if (part3.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part3.getDisposition())) {
            mailMsg.setTxtBody(part3.getContent().toString());
          } else if (part3.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part3.getDisposition())) {
            mailMsg.setHtmlBody(part3.getContent().toString());
          } else if (isValidMailMsg) {
            // Process the attachment.
            this.processAttachment(part3, mailMsg, attachList);
          }
        }
      } else {
        // Process the attachment.(This is a certificate file.)
        this.processAttachment(part2, mailMsg, attachList);
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
   * @throws CMSException
   * @throws IOException
   * @throws OperatorCreationException
   * @throws CertificateException
   */
  private boolean validateSignedMail(Message msg, MailMessage mailMsg) throws MessagingException, CMSException, IOException, OperatorCreationException, CertificateException {
    /*
     * Add a header to make a new message in order to fix the issue of Outlook
     * 
     * @see http://www.bouncycastle.org/wiki/display/JA1/CMS+and+SMIME+APIs
     * 
     * @see http://stackoverflow.com/questions/8590426/s-mime-verification-with-x509-certificate
     */
    MimeMessage newmsg = new MimeMessage((MimeMessage) msg);
    newmsg.setHeader("Nothing", "Add a header for verifying signature only.");
    newmsg.saveChanges();
    SMIMESigned signed = new SMIMESigned((MimeMultipart) newmsg.getContent());
    return this.isValid(signed, mailMsg);
  }

  /**
   * Verify the email is signed by the given certificate.
   * 
   * @param signedData
   * @param mailMsg
   * @return
   * @throws OperatorCreationException
   * @throws CMSException
   * @throws CertificateException
   */
  @SuppressWarnings({ "rawtypes" })
  private boolean isValid(CMSSignedData signedData, MailMessage mailMsg) throws OperatorCreationException, CMSException, CertificateException {
    SignerInformationStore signerStore = signedData.getSignerInfos();
    Iterator<SignerInformation> it = signerStore.getSigners().iterator();
    boolean verify = false;
    while (it.hasNext()) {
      SignerInformation signer = it.next();
      org.bouncycastle.util.Store store = signedData.getCertificates();
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
   * Set some basic information to MailMessage .
   * 
   * @param msg
   * @param mailMsg
   * @throws MessagingException
   */
  private void setMailMsgForBasicInfo(Message msg, MailMessage mailMsg) throws MessagingException {
    Address[] from = msg.getFrom();
    Address[] to = msg.getRecipients(RecipientType.TO);
    Address[] cc = msg.getRecipients(RecipientType.CC);
    Address[] bcc = msg.getRecipients(RecipientType.BCC);
    String subject = msg.getSubject();
    Date sendDate = msg.getSentDate();

    mailMsg.setFrom(MailUtil.convertToMailAddress(from));
    mailMsg.setTo(MailUtil.convertToMailAddress(to));
    mailMsg.setCc(MailUtil.convertToMailAddress(cc));
    mailMsg.setBcc(MailUtil.convertToMailAddress(bcc));
    mailMsg.setSubject(subject);
    mailMsg.setSendDate(sendDate);
  }

  /**
   * Verify the mail's size and the attachments exceeding the maximum quantity.
   * 
   * @param multi
   *          Multipart
   * @return
   * @throws MessagingException
   * @throws ExceedException
   * @throws IOException
   * @throws NumberFormatException
   */
  private boolean isValidMailMsg(Multipart multi) throws MessagingException, ExceedException, NumberFormatException, IOException {
    boolean isValid = false;
    boolean exceedMaxMailSize = false;
    int mailSize = 0;

    boolean exceedMaxAttachmentCount = this.exceedMaxAttachmentCount(multi);
    if (exceedMaxAttachmentCount) {
      String e = "The attachments' quantity exceed the maximum value!";
      LOG.error(e);
      throw new ExceedException(e);
    }

    mailSize = this.countMailSize(multi, 0);
    if (mailSize > attachmentsegmentsize * maxattachmentcount) {
      exceedMaxMailSize = true;
      String e = "The size of all the attachments exceed the maximum value!";
      LOG.error(e);
      throw new ExceedException(e);
    }

    if (!exceedMaxAttachmentCount && !exceedMaxMailSize) {
      isValid = true;
    }
    return isValid;
  }

  /**
   * Count the mail size, it is a total mail size, including email body and all attachments.
   * 
   * @param multi
   * @param mailSize
   * @return
   * @throws NumberFormatException
   * @throws MessagingException
   * @throws IOException
   */
  private int countMailSize(Multipart multi, int mailSize) throws NumberFormatException, MessagingException, IOException {
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
   * @throws ExceedException
   */
  private boolean exceedMaxMsgSize(int msgSize) throws ExceedException {
    boolean exceedMaxMsgSize = false;
    if (msgSize > attachmentsegmentsize * maxattachmentcount) {
      exceedMaxMsgSize = true;
      String e = "The size of all the attachments exceed the maximum value!";
      LOG.error(e);
      throw new ExceedException(e);
    }
    return exceedMaxMsgSize;
  }

  /**
   * Determine whether the attachment's quantity exceeds the max attachment count or not.
   * 
   * @param multi
   *          Multipart
   * @return true means exceed
   * @throws MessagingException
   */
  private boolean exceedMaxAttachmentCount(Multipart multi) throws MessagingException {
    boolean exceed = false;

    // Normally, only 1 BodyPart is email text, others are attachments. So the whole BodyPart minus 1 is the attachment quantity.
    int attachmentCount = multi.getCount();
    if (attachmentCount - 1 > maxattachmentcount) {
      exceed = true;
    }
    return exceed;
  }

  /**
   * Process attachments.
   * 
   * @param part
   * @param mailMsg
   * @param attachList
   * @throws IOException
   */
  private void processAttachment(Part part, MailMessage mailMsg, List<Attachment> attachList) throws IOException {
    String disposition = null;
    try {
      disposition = part.getDisposition();
      if ((disposition != null && Part.ATTACHMENT.equalsIgnoreCase(disposition))) {
        // generate a new file name with unique UUID.
        String fileName = part.getFileName();
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
          mailMsg.setAttachList(attachList);
          this.saveFile(fileName, part.getInputStream(), fileSize);
        }
      }
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
    }
  }

  /**
   * Process attachments.
   * 
   * @param part
   * @param mailMsg
   * @param attachList
   * @throws IOException
   */
  private void processEmailBodyAttachment(Part part, MailMessage mailMsg, List<Attachment> attachList) throws IOException {
    try {
      // generate a new file name with unique UUID.
      String fileName = part.getFileName();
      UUID uuid = UUID.randomUUID();
      String prefix = fileName.substring(0, fileName.lastIndexOf(".") + 1);
      String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
      if (this.suffixList.contains(suffix.toLowerCase())) {
        String tempDir = System.getProperty("java.io.tmpdir");
        fileName = tempDir + "emailBody." + prefix + uuid + "." + suffix;

        int fileSize = part.getSize();
        Attachment attachment = new Attachment();
        attachment.setFileName(fileName);
        attachment.setFileType(suffix);
        attachment.setFileSize(fileSize);
        attachList.add(attachment);
        mailMsg.setAttachList(attachList);
        this.saveFile(fileName, part.getInputStream(), fileSize);
      }
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
    }
  }

  /**
   * Save file to temp directory.
   * 
   * @throws IOException
   * @throws FileNotFoundException
   */
  private void saveFile(String fileName, InputStream in, int fileSize) throws IOException {
    File file = new File(fileName);
    if (!file.exists()) {
      OutputStream out = null;
      try {
        out = new BufferedOutputStream(new FileOutputStream(file));
        byte[] buf = new byte[fileSize];
        int len;
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
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
   * Check whether the given parameter is null or not.
   * 
   * @param s
   *          String
   * @return true means 's' is null
   */
  private static boolean isNull(String s) {
    if (s == null || s == "") {
      return true;
    } else {
      return false;
    }
  }

  /**
   * check whether the user name is belong to authorised user domain or not.
   * 
   * @param username
   * @return
   * @throws Exception
   */
  private static boolean isAuthorisedUsername(List<String> authorisedUserList, String username) throws Exception {
    if (authorisedUserList == null || authorisedUserList.isEmpty()) {
      String msg = "The authorised user domain list is empty!";
      LOG.error(msg);
      throw new Exception(msg);
    }
    for (String regex : authorisedUserList) {
      if (username.matches(".*" + regex)) {
        return true;
      }
    }
    return false;
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
    Folder folder = this.store.getFolder(folderName);
    folder.open(mode);
    return folder;
  }

  /**
   * Get a folder according to the folder name if it exists.
   * 
   * @param folderName
   * @return
   * @throws MessagingException
   */
  private Folder getExistingFolder(String folderName) throws MessagingException {
    Folder folder = this.store.getFolder(folderName);
    if (folder != null && folder.isOpen()) {
      return folder;
    }
    return null;
  }

  /**
   * Close a folder according to the folder name if it opens.
   * 
   * @param folderName
   * @param expunge
   *          expunges all deleted messages if this flag is true
   * @throws MessagingException
   */
  private void closeFolder(String folderName, boolean expunge) throws MessagingException {
    Folder folder = this.store.getFolder(folderName);
    if (folder != null && folder.isOpen()) {
      // close the folder, true means that will indeed to delete the message, false means that will not delete the message.
      folder.close(expunge);
    }
  }

  /**
   * Check the folder existing or not. If not, create a new folder.
   * 
   * @param parent
   * @param folderName
   * @return
   * @throws Exception
   */
  private boolean checkAndCreateFolder(Folder parent, String folderName) throws Exception {
    boolean isCreated = false;
    boolean folderExists = false;
    try {
      folderExists = this.store.getFolder(folderName).exists();
      if (!folderExists) {
        if (("imap".equalsIgnoreCase(this.protocol) || "imaps".equalsIgnoreCase(this.protocol))) {
          // If parent is not the root directory, the folder should be opened.
          // parent.open(Folder.READ_WRITE);
          System.out.println("creating a folder ....");
          Folder newFolder = parent.getFolder(folderName);

          isCreated = newFolder.create(Folder.HOLDS_MESSAGES);
          System.out.println("created: " + isCreated);
          // parent.close(true);
        } else {
          String msg = "If you want to assign a specific folder, the protocol should be IMAP or IMAPS.";
          LOG.error(msg);
          throw new Exception(msg);
        }
      }
    } catch (MessagingException e) {
      String msg = "The folder is created: " + isCreated + ". Messaging Exception: " + e.toString();
      LOG.error(msg);
    }
    return isCreated || folderExists;
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
    props.put("mail.smtp.auth", this.auth);
    props.put("mail.store.protocol", this.protocol);
    // Proxy
    if (this.proxySet) {
      props.put("proxySet", this.proxySet);
      props.put("http.proxyHost", this.proxyHost);
      props.put("http.proxyPort", this.proxyPort);
      // props.put("socksProxyHost", proxyHost);
      // props.put("socksProxyPort", proxyPort);
    }
    /*
     * General Issues with Multiparts
     * 
     * @see http://www.bouncycastle.org/wiki/display/JA1/CMS+and+SMIME+APIs
     */
    // props.put("mail.mime.cachemultipart", false);

    return props;
  }


}
