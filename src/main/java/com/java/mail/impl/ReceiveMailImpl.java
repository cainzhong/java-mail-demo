/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
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

import javax.activation.DataSource;
import javax.activation.FileDataSource;
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
import javax.mail.search.FlagTerm;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.SearchTerm;

import org.apache.commons.logging.LogFactory;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.operator.OperatorCreationException;

import com.hp.ov.sm.common.core.Init;
import com.hp.ov.sm.common.core.JLog;
import com.java.mail.MailUtil;
import com.java.mail.ReceiveMail;
import com.java.mail.domain.Attachment;
import com.java.mail.domain.MailMessage;
import com.java.mail.domain.MailStatus;
import com.sun.mail.pop3.POP3Folder;

import microsoft.exchange.webservices.data.autodiscover.IAutodiscoverRedirectionUrl;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.WebProxy;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.service.ConflictResolutionMode;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceVersionException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.core.service.schema.FolderSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.EmailAddressCollection;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.FolderView;
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

  public static final int BUFFSIZE = 180;

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

  public static class RedirectionUrlCallback implements IAutodiscoverRedirectionUrl {
    public boolean autodiscoverRedirectionUrlValidationCallback(String redirectionUrl) {
      return redirectionUrl.toLowerCase().startsWith("https://");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.java.mail.ReceiveMail#initialize(java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public int initialize(String jsonParam) {
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
        try {
          // this.service.autodiscoverUrl(this.username, new RedirectionUrlCallback());
          this.service.setUrl(new URI("https://outlook.office365.com/EWS/Exchange.asmx"));
        } catch (URISyntaxException e) {
          LOG.error(e.toString());
        } catch (Exception e) {
          LOG.error(e.toString());
        }
        this.service.setTraceEnabled(true);
        if (this.proxySet) {
          // For EWS proxy
          this.proxyHost = (String) map.get("proxyHost");
          this.proxyPort = (String) map.get("proxyPort");
          this.proxyUser = (String) map.get("proxyUser");
          this.proxyPassword = (String) map.get("proxyPassword");
          WebProxy proxy = new WebProxy(this.proxyHost, Integer.valueOf(this.proxyPort));
          this.service.setWebProxy(proxy);
        }
      } else if (this.proxySet) {
        this.proxyHost = (String) map.get("proxyHost");
        this.proxyPort = (String) map.get("proxyPort");
      }
      this.sourceFolderName = (String) map.get("sourceFolderName");
      this.toFolderName = (String) map.get("toFolderName");

      if (isNull(this.protocol)) {
        LOG.error("Missing mandatory values, please check that you have entered the protocol.");
        return MailStatus.Protocol_Missing.getCode();
      } else if (this.protocol.equalsIgnoreCase("EWS")) {
        if (isNull(this.username) || isNull(this.password)) {
          LOG.error("Missing mandatory values, please check that you have entered the username, password or uri.");
          return MailStatus.UP_Missing.getCode();
        }
      } else if (isNull(this.host) || isNull(this.port) || isNull(this.username) || isNull(this.password)) {
        LOG.error("Missing mandatory values, please check that you have entered the host, port, username or password.");
        return MailStatus.HPUP_Missing.getCode();
      } else if (!isAuthorisedUsername(this.authorisedUserList, this.username)) {
        LOG.error("The user name is not belong to authorised user domain!");
        return MailStatus.Not_Authorised_User.getCode();
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
    return MailStatus.Initialize_Successfully.getCode();
  }

  public void open() throws MessagingException {
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
  public JSONArray receive(String messageId, boolean save) {
    JSONArray jsonArray = null;
    try {
      SearchTerm st;
      if (!isNull(messageId)) {
        // receive mails according to the message id.
        st = new MessageIDTerm(messageId);
      } else {
        // Only receive new mails.
        st = new FlagTerm(new Flags(Flags.Flag.RECENT), true);
      }
      Message[] messages = this.sourceFolder.search(st);

      // Get mails and UID
      this.sourceFolder.fetch(messages, this.profile);

      // restrict reading mail message size to 'maxMailSize'.
      List<MailMessage> msgList = new ArrayList<MailMessage>();
      int msgsLength = messages.length;
      if (msgsLength == 0) {
        MailMessage mailMsg = new MailMessage();
        mailMsg.setMailStatus(MailStatus.No_Message);
        msgList.add(mailMsg);
      } else if (msgsLength > this.maxMailQuantity) {
        msgList = this.processMsg(messages, this.maxMailQuantity, this.sourceFolder, this.toFolder, save);
      } else {
        msgList = this.processMsg(messages, msgsLength, this.sourceFolder, this.toFolder, save);
      }
      jsonArray = JSONArray.fromObject(msgList);

    } catch (MessagingException e) {
      LOG.error(e.toString());
    }
    return jsonArray;
  }

  public JSONArray receiveAttachment(String protocol, String messageId) {
    protocol = protocol.trim();
    JSONArray json = null;
    if (protocol.equalsIgnoreCase("pop3") || protocol.equalsIgnoreCase("pop3s") || protocol.equalsIgnoreCase("imap") || protocol.equalsIgnoreCase("imaps")) {
      json = this.receive(messageId, true);
    } else if (protocol.equalsIgnoreCase("ews")) {
      json = this.receiveViaEWS(messageId, true);
    }
    MailMessage mailMsg = new MailMessage();
    if (json != null && !json.isEmpty()) {
      List<MailMessage> list = (List<MailMessage>) JSONArray.toCollection(json, MailMessage.class);
      if (list != null && list.size() == 1) {
        mailMsg = list.get(0);
        mailMsg.setMailStatus(MailStatus.Receive_Attachment_Successfully);
        return JSONArray.fromObject(mailMsg);
      }
    }
    mailMsg.setMailStatus(MailStatus.Fail_To_Receive_Attachment);
    return JSONArray.fromObject(mailMsg);
  }

  public JSONArray receiveViaEWS(String messageId, boolean save) {
    FolderId sourceFolderId = this.checkAndCreateEWSFolder(this.sourceFolderName);

    JSONArray jsonArray = null;
    List<MailMessage> msgList = new ArrayList<MailMessage>();
    try {
      if (!isNull(messageId)) {
        MailMessage mailMsg = new MailMessage();
        EmailMessage emailMessage = EmailMessage.bind(this.service, new ItemId(messageId));
        this.setMailMsgForEWSMail(emailMessage, mailMsg, save);
        msgList.add(mailMsg);
      } else {
        ItemView view = new ItemView(this.maxMailQuantity);
        // view.setPropertySet(new PropertySet(BasePropertySet.IdOnly, ItemSchema.Subject, ItemSchema.DateTimeReceived));
        String fromStringTerm = "tao.zhong@hpe.com";
        String subjectTerm = "Signed Mail with 2 attachments.";
        // SearchFilter.ContainsSubstring fromTermFilter = new SearchFilter.ContainsSubstring(EmailMessageSchema.From, fromStringTerm);
        // SearchFilter.ContainsSubstring subjectFilter = new SearchFilter.ContainsSubstring(ItemSchema.Subject, subjectTerm, ContainmentMode.Substring, ComparisonMode.IgnoreCase);
        SearchFilter filter = new SearchFilter.IsEqualTo(EmailMessageSchema.IsRead, false);
        FindItemsResults<Item> findResults = this.service.findItems(sourceFolderId, filter, view);
        // FindItemsResults<Item> findResults = this.service.findItems(WellKnownFolderName.Inbox, new SearchFilter.SearchFilterCollection(LogicalOperator.And, fromTermFilter, subjectFilter), view);
        for (Item item : findResults) {
          MailMessage mailMsg = this.readEmailItem(item.getId(), save);
          msgList.add(mailMsg);
        }
      }
      jsonArray = JSONArray.fromObject(msgList);
    } catch (Exception e) {
      LOG.error(e.toString());
    }
    return jsonArray;
  }

  /**
   * Reading one email at a time. Using Item ID of the email.
   * Creating a message data map as a return value.
   */
  public MailMessage readEmailItem(ItemId itemId, boolean save) {
    MailMessage mailMsg = new MailMessage();
    try {
      Item item = Item.bind(this.service, itemId, PropertySet.FirstClassProperties);
      EmailMessage emailMessage = EmailMessage.bind(this.service, item.getId());
      this.setMailMsgForBasicInfo(emailMessage, mailMsg);
      this.setMailMsgForEWSMail(emailMessage, mailMsg, save);
      // flag the email message as read.
      emailMessage.setIsRead(true);
      emailMessage.update(ConflictResolutionMode.AutoResolve);
    } catch (Exception e) {
      LOG.error(e.toString());
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
   * @throws CMSException
   * @throws CertificateException
   * @throws OperatorCreationException
   * @throws SMIMEException
   * @throws MessagingException
   */
  private void processEWSAttachment(FileAttachment fileAttachment, MailMessage mailMsg, List<Attachment> mailAttachList) throws ServiceVersionException, IOException, CMSException, OperatorCreationException, CertificateException, SMIMEException, MessagingException {
    if (fileAttachment.getContentType().equalsIgnoreCase("multipart/signed")) {
      // DataSource source = new FileDataSource("C:/Users/zhontao/Desktop/smime.p7m");
      String fileName = this.saveEWSAttachment(fileAttachment, mailMsg, mailAttachList);
      DataSource source = new FileDataSource(fileName);
      MimeMultipart multi1 = new MimeMultipart(source);
      for (int i = 0; i < multi1.getCount(); i++) {
        Part part1 = multi1.getBodyPart(i);
        if (part1.getContent() instanceof Multipart) {
          Multipart multi2 = (Multipart) part1.getContent();
          for (int j = 0; j < multi2.getCount(); j++) {
            Part part2 = multi2.getBodyPart(j);
            String contentType = part2.getContentType();
            // generally if the content type multipart/alternative, it is email text.
            if (part2.isMimeType("multipart/alternative")) {
              if (part2.getContent() instanceof Multipart) {
                Multipart multi3 = (Multipart) part2.getContent();
                for (int k = 0; k < multi3.getCount(); k++) {
                  Part part4 = multi3.getBodyPart(k);
                  String contentType1 = part4.getContentType();
                  if (part4.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part4.getDisposition())) {
                    mailMsg.setTxtBody(part4.getContent().toString());
                  } else if (part4.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part4.getDisposition())) {
                    mailMsg.setHtmlBody(part4.getContent().toString());
                  }
                }
              }
            } else {
              this.processAttachment(part2, mailMsg, mailAttachList, true);
            }
          }
        } else {
          this.processAttachment(part1, mailMsg, mailAttachList, true);
        }
      }
      SMIMESigned signedData = new SMIMESigned(multi1);
      this.isValid(signedData, mailMsg);
    } else {
      this.saveEWSAttachment(fileAttachment, mailMsg, mailAttachList);
    }
  }

  private String saveEWSAttachment(FileAttachment fileAttachment, MailMessage mailMsg, List<Attachment> mailAttachList) throws ServiceVersionException, IOException {
    // generate a new file name with unique UUID.
    String fileName = fileAttachment.getName();
    UUID uuid = UUID.randomUUID();
    String prefix = fileName.substring(0, fileName.lastIndexOf(".") + 1);
    String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
    if (this.suffixList.contains(suffix.toLowerCase()) || suffix.toLowerCase().equalsIgnoreCase("p7m")) {
      String tempDir = System.getProperty("java.io.tmpdir");
      fileName = tempDir + prefix + uuid + "." + suffix;

      int fileSize = fileAttachment.getSize();
      Attachment mailAttachment = new Attachment();
      mailAttachment.setFileName(fileName);
      mailAttachment.setFileType(suffix);
      mailAttachment.setFileSize(fileSize);
      mailAttachList.add(mailAttachment);
      mailMsg.setAttachList(mailAttachList);
      this.saveByteFile(fileName, fileAttachment.getContent(), fileSize);
    }
    return fileName;
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
        LOG.error(e.toString());
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
    String messageId = emailMessage.getId().toString();
    EmailAddress from = emailMessage.getFrom();
    EmailAddressCollection to = emailMessage.getToRecipients();
    EmailAddressCollection cc = emailMessage.getCcRecipients();
    EmailAddressCollection bcc = emailMessage.getBccRecipients();
    String subject = emailMessage.getSubject();
    Date sendDate = emailMessage.getDateTimeCreated();

    mailMsg.setMsgId(messageId);
    mailMsg.setFrom(MailUtil.convertToMailAddress(from));
    mailMsg.setTo(MailUtil.convertToMailAddress(to));
    mailMsg.setCc(MailUtil.convertToMailAddress(cc));
    mailMsg.setBcc(MailUtil.convertToMailAddress(bcc));
    mailMsg.setSubject(subject);
    mailMsg.setSendDate(sendDate);
  }

  private MailMessage setMailMsgForEWSMail(EmailMessage emailMessage, MailMessage mailMsg, boolean save) throws ServiceVersionException, ServiceLocalException, Exception {
    if (!this.exceedMaxMsgSize(emailMessage.getSize())) {
      String emailBody = emailMessage.getBody().toString();
      mailMsg.setHtmlBody(emailBody);
      if (emailMessage.getHasAttachments()) {
        mailMsg.setHasAttachments(true);
        if (save) {
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
      }
    }
    return mailMsg;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.java.mail.ReceiveMail#moveMessage(javax.mail.Message)
   */
  public int moveMessage(String protocol,String messageId) {
    int moveMsg = 100;
    protocol = protocol.trim();
    try {
      if (protocol.equalsIgnoreCase("pop3") || protocol.equalsIgnoreCase("pop3s") || protocol.equalsIgnoreCase("imap") || protocol.equalsIgnoreCase("imaps")) {
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
            // only add a delete flag on the message, it will not indeed to execute the delete operation.
            msg.setFlag(Flags.Flag.DELETED, true);
          } else {
            moveMsg = MailStatus.No_Message.getCode();
          }
        } else {
          moveMsg = MailStatus.Fail_To_Move_Message.getCode();
        }
      } else if (protocol.equalsIgnoreCase("ews")) {
        EmailMessage emailMessage = EmailMessage.bind(this.service, new ItemId(messageId));

        FolderId folderId = this.checkAndCreateEWSFolder(this.toFolderName);
        emailMessage.move(folderId);
        moveMsg = MailStatus.Move_Message_Successfully.getCode();
      } else {
        moveMsg = MailStatus.Fail_To_Move_Message.getCode();
      }
    } catch (MessagingException e) {
      LOG.error(e.toString());
      moveMsg = MailStatus.Fail_To_Move_Message.getCode();
    } catch (Exception e) {
      LOG.error(e.toString());
      moveMsg = MailStatus.Fail_To_Move_Message.getCode();
    }
    return moveMsg;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.java.mail.ReceiveMail#close()
   */
  public void close() {
    // close the folder, true means that will indeed to delete the message, false means that will not delete the message.
    this.closeFolder(this.sourceFolderName, true);
    this.closeFolder(this.toFolderName, true);
    try {
      this.store.close();
    } catch (MessagingException e) {
      LOG.error(e.toString());
    }
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
   * @throws InitializeException
   */
  private List<MailMessage> processMsg(Message[] messages, int maxMailSize, Folder sourceFolder, Folder toFolder, boolean save) {
    List<MailMessage> msgList = new ArrayList<MailMessage>();
    try {
      MailMessage mailMsg = new MailMessage();
      for (int i = 0; i < maxMailSize; i++) {
        MimeMessage msg = (MimeMessage) messages[i];
        if (this.sourceFolder instanceof POP3Folder) {
          POP3Folder pop3Folder = (POP3Folder) this.sourceFolder;
          String uid = pop3Folder.getUID(msg);
          mailMsg.setUid(uid);
          mailMsg.setUsername(this.username);
          // if the uid exists that means the mail has already been read. Jump to read next mail.
          if (this.checkUIDExists(uid)) {
            continue;
          }
        }
        mailMsg.setMsgId(msg.getMessageID());
        mailMsg.setContentType(msg.getContentType());

        if (msg.isMimeType("text/html") || msg.isMimeType("text/plain")) {
          // simple mail without attachment
          this.setMailMsgForSimpleMail(msg, mailMsg, save);
        } else if (msg.isMimeType("multipart/mixed")) {
          // simple mail with attachment
          this.setMailMsgForSimpleMail(msg, mailMsg, save);
        } else if (msg.isMimeType("multipart/signed")) {
          // signed mail with/without attachment
          this.validateSignedMail(msg, mailMsg);
          this.setMailMsgForSignedMail(msg, mailMsg, save);
        } else if (msg.isMimeType("application/pkcs7-mime") || msg.isMimeType("application/x-pkcs7-mime")) {
          mailMsg.setContentType(msg.getContentType());
          mailMsg.setMailStatus(MailStatus.Encrypted_Mail);
          String e = "It's an encrypted mail. Can not handle the Message receiving from Mail Server.";
          LOG.error(e);
        } else {
          mailMsg.setMailStatus(MailStatus.Unkonwn_MIME_Type);
          String e = "Unknown MIME Type | Message Content Type: " + msg.getContentType() + "Message Subject: " + msg.getSubject() + "Message Send Date: " + msg.getSentDate() + "Message From: " + msg.getFrom().toString();
          LOG.error(e);
        }
        msgList.add(mailMsg);
      }
    } catch (MessagingException e) {
      LOG.error(e.toString());
    }
    return msgList;
  }

  // TODO
  private boolean checkUIDExists(String uid) {
    // this.username;
    boolean exist = false;
    // result = select * from table where uid=, username=
    // result.size>0 exist = true;
    // else insert uid username into table; exist=fale;
    return exist;
  }

  /**
   * Set MailMessage for simple mail with/without attachment.
   * 
   * @param msg
   * @param mailMsg
   * @return
   * @throws IOException
   * @throws MessagingException
   * @throws InitializeException
   */
  private MailMessage setMailMsgForSimpleMail(Message msg, MailMessage mailMsg, boolean save) {
    List<Attachment> attachList = new ArrayList<Attachment>();
    try {
      mailMsg.setSignaturePassed(false);
      this.setMailMsgForBasicInfo(msg, mailMsg);

      if (msg.getContent() instanceof Multipart) {
        // Get the content of the messsage, it's an Multipart object like a package including all the email text and attachment.
        Multipart multi1 = (Multipart) msg.getContent();
        // First, verify the quantity and size of attachments. If the attachments exceed the maximum quantity and size, the code will throw an ExceedException.
        boolean isValidMailMsg = this.isValidMailMsg(multi1, mailMsg);
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
    } catch (MessagingException e) {
      LOG.error(e.toString());
    } catch (NumberFormatException e) {
      LOG.error(e.toString());
    } catch (IOException e) {
      LOG.error(e.toString());
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
   * @throws InitializeException
   */
  private MailMessage setMailMsgForSignedMail(Message msg, MailMessage mailMsg, boolean save) {
    List<Attachment> attachList = new ArrayList<Attachment>();

    this.setMailMsgForBasicInfo(msg, mailMsg);
    try {
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
    } catch (IOException e) {
      LOG.error(e.toString());
    } catch (MessagingException e) {
      LOG.error(e.toString());
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
  private boolean validateSignedMail(Message msg, MailMessage mailMsg) {
    boolean verify =false;
    /*
     * Add a header to make a new message in order to fix the issue of Outlook
     * 
     * @see http://www.bouncycastle.org/wiki/display/JA1/CMS+and+SMIME+APIs
     * 
     * @see http://stackoverflow.com/questions/8590426/s-mime-verification-with-x509-certificate
     */
    try {
      MimeMessage newmsg = new MimeMessage((MimeMessage) msg);
      newmsg.setHeader("Nothing", "Add a header for verifying signature only.");
      newmsg.saveChanges();
      SMIMESigned signed = new SMIMESigned((MimeMultipart) newmsg.getContent());
      verify = this.isValid(signed, mailMsg);
    } catch (MessagingException e) {
      LOG.error(e.toString());
    } catch (CMSException e) {
      LOG.error(e.toString());
    } catch (IOException e) {
      LOG.error(e.toString());
    }
    return verify;
  }

  /**
   * Verify the email is signed by the given certificate.
   * 
   * @param signedData
   * @param mailMsg
   * @return
   */
  @SuppressWarnings({ "rawtypes" })
  private boolean isValid(CMSSignedData signedData, MailMessage mailMsg) {
    boolean verify = false;
    try {
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
    } catch (CertificateException e) {
      mailMsg.setMailStatus(MailStatus.Certificate_Error);
      LOG.error(e.toString());
    } catch (OperatorCreationException e) {
      mailMsg.setMailStatus(MailStatus.Certificate_Error);
      LOG.error(e.toString());
    } catch (CMSException e) {
      mailMsg.setMailStatus(MailStatus.Certificate_Error);
      LOG.error(e.toString());
    }
    return verify;
  }

  /**
   * Set some basic information to MailMessage .
   * 
   * @param msg
   * @param mailMsg
   */
  private void setMailMsgForBasicInfo(Message msg, MailMessage mailMsg) {
    try {
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
    } catch (MessagingException e) {
      LOG.error(e.toString());
    }
  }

  /**
   * Verify the mail's size and the attachments exceeding the maximum quantity.
   * 
   * @param multi
   * @param mailMsg
   * @return
   */
  private boolean isValidMailMsg(Multipart multi, MailMessage mailMsg) {
    boolean isValid = false;
    boolean exceedMaxMailSize = false;
    boolean exceedMaxAttachmentCount = false;

    if (this.countMailAttachments(multi) > maxattachmentcount) {
      exceedMaxAttachmentCount = true;
      mailMsg.setMailStatus(MailStatus.Invalid_Mail_Message);
      LOG.info("The attachments' quantity exceed the maximum value!");
    }

    int mailSize = this.countMailSize(multi, 0);
    if (mailSize > attachmentsegmentsize * maxattachmentcount) {
      exceedMaxMailSize = true;
      mailMsg.setMailStatus(MailStatus.Invalid_Mail_Message);
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
   */
  private int countMailAttachments(Multipart multi) {
    int mailAttachments = 0;
    try {
      // Normally, only 1 BodyPart is email text, others are attachments. So the whole BodyPart minus 1 is the attachment quantity.
      int attachmentCount = multi.getCount();
      mailAttachments = attachmentCount - 1;
    } catch (MessagingException e) {
      LOG.error("May the Multipart is null. " + e.toString());
    }
    return mailAttachments;
  }

  /**
   * Count the mail size, it is a total mail size, including email body and all attachments.
   * 
   * @param multi
   * @param mailSize
   * @return
   */
  private int countMailSize(Multipart multi, int mailSize) {
    try {
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
    } catch (MessagingException e) {
      LOG.error("May the Multipart is null. " + e.toString());
    } catch (IOException e) {
      LOG.error("May the Bodypart is null. " + e.toString());
    }
    LOG.debug("Message Size: " + mailSize / 1024 + " KB");
    return mailSize;
  }

  /**
   * Determine whether the mail's size exceeds the max mail's size or not.
   * 
   * @param msgSize
   * @return
   */
  private boolean exceedMaxMsgSize(int msgSize) {
    boolean exceedMaxMsgSize = false;
    if (msgSize > attachmentsegmentsize * maxattachmentcount) {
      exceedMaxMsgSize = true;
      LOG.error("The size of all the attachments exceed the maximum value!");
    }
    return exceedMaxMsgSize;
  }

  /**
   * Process attachments and save it.
   * 
   * @param part
   * @param mailMsg
   * @param attachList
   * @param save
   *          true indicates process the attachment and save it.
   */
  private void processAttachment(Part part, MailMessage mailMsg, List<Attachment> attachList, boolean save) {
    String disposition = null;
    try {
      disposition = part.getDisposition();
      if ((disposition != null && Part.ATTACHMENT.equalsIgnoreCase(disposition))) {
        if (save) {
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
            this.saveFile(fileName, part.getInputStream());
          } else {
            LOG.info(fileName + " is not a supported file. Ignore this file.");
          }
        } else {
          mailMsg.setHasAttachments(true);
        }
      }
    } catch (MessagingException e) {
      LOG.error("May the Bodypart is null. " + e.toString());
    } catch (IOException e) {
      LOG.error("May can not close IO Stream. " + e.toString());
    }
  }

  /**
   * Process attachments.
   *
   * @param part
   * @param mailMsg
   * @param attachList
   * @param excute
   *          true indicates process the attachment and save it.
   * @throws IOException
   */
  private void processEmailBodyAttachment(Part part, MailMessage mailMsg, List<Attachment> attachList, boolean save) {
    try {
      if (save) {
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
          this.saveFile(fileName, part.getInputStream());
        } else {
          LOG.info(fileName + " is not a supported file. Ignore this file.");
        }
      } else {
        mailMsg.setHasAttachments(true);
      }
    } catch (MessagingException e) {
      LOG.error("May the Bodypart is null. " + e.toString());
    } catch (IOException e) {
      LOG.error("May can not close IO Stream. " + e.toString());
    }
  }

  /**
   * Save file to temp directory.
   * 
   * @param fileName
   * @param in
   * @throws IOException
   */
  private void saveFile(String fileName, InputStream in) throws IOException {
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
      } catch (FileNotFoundException e) {
        LOG.error(e.toString());
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
  private static boolean isAuthorisedUsername(List<String> authorisedUserList, String username) {
    if (authorisedUserList == null || authorisedUserList.isEmpty()) {
      String msg = "The authorised user domain list is empty!";
      LOG.error(msg);
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
   */
  private Folder openFolder(String folderName, int mode) {
    Folder folder = null;
    try {
      folder = this.store.getFolder(folderName);
      folder.open(mode);
    } catch (MessagingException e) {
      LOG.error(e.toString());
    }
    return folder;
  }

  /**
   * Close a folder according to the folder name if it opens.
   * 
   * @param folderName
   * @param expunge
   */
  private void closeFolder(String folderName, boolean expunge) {
    try {
      Folder folder = this.store.getFolder(folderName);
      if (folder != null && folder.isOpen()) {
        // close the folder, true means that will indeed to delete the message, false means that will not delete the message.
        folder.close(expunge);
      }
    } catch (MessagingException e) {
      LOG.error(e.toString());
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
  private boolean checkAndCreateFolder(Folder parent, String folderName) {
    boolean isCreated = false;
    boolean folderExists = false;
    try {
      folderExists = this.store.getFolder(folderName).exists();
      if (!folderExists) {
        if (("imap".equalsIgnoreCase(this.protocol) || "imaps".equalsIgnoreCase(this.protocol))) {
          // If parent is not the root directory, the folder should be opened.
          // parent.open(Folder.READ_WRITE);
          Folder newFolder = parent.getFolder(folderName);
          isCreated = newFolder.create(Folder.HOLDS_MESSAGES);
          // parent.close(true);
        } else {
          LOG.info("If you want to assign a specific folder, the protocol should be IMAP or IMAPS.");
        }
      }
    } catch (MessagingException e) {
      LOG.error("The folder is created: " + isCreated + ". Messaging Exception: " + e.toString());
    }
    return isCreated || folderExists;
  }

  private FolderId checkAndCreateEWSFolder(String folderName) {
    boolean folderExists = false;
    FolderId folderId = null;
    try {
      FolderView view = new FolderView(1);
      SearchFilter filter = new SearchFilter.IsEqualTo(FolderSchema.DisplayName, folderName);
      FindFoldersResults results = this.service.findFolders(WellKnownFolderName.MsgFolderRoot, filter, view);
      Iterator<microsoft.exchange.webservices.data.core.service.folder.Folder> it = results.iterator();
      while (it.hasNext()) {
        microsoft.exchange.webservices.data.core.service.folder.Folder folder = it.next();
        folderId = folder.getId();
        folderExists = true;
      }
      if (!folderExists) {
        microsoft.exchange.webservices.data.core.service.folder.Folder folder = new microsoft.exchange.webservices.data.core.service.folder.Folder(this.service);
        folder.setDisplayName(folderName);
        // creates the folder as a child of the MsgFolderRoot folder.
        folder.save(WellKnownFolderName.MsgFolderRoot);
        folderId = folder.getId();
      }
    } catch (Exception e) {
      LOG.error(e.toString());
    }
    return folderId;

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
    }
    /*
     * General Issues with Multiparts
     * 
     * @see http://www.bouncycastle.org/wiki/display/JA1/CMS+and+SMIME+APIs
     */
    // props.put("mail.mime.cachemultipart", false);

    return props;
  }

  public int deleteAttachments(String path) {
    File file = new File(path);
    if (file.isFile() && file.exists()) {
      file.delete();
    } else {
      return MailStatus.Fail_To_Delete_Attachment.getCode();
    }
    return MailStatus.Delete_Attachment_Successfully.getCode();
  }
}
