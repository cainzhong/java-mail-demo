/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
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

import microsoft.exchange.webservices.data.autodiscover.IAutodiscoverRedirectionUrl;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.WebProxy;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.MapiPropertyType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.service.ConflictResolutionMode;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceVersionException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.FolderSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.EmailAddressCollection;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.property.complex.InternetMessageHeader;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.property.definition.ExtendedPropertyDefinition;
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

  public static final int BUFFSIZE = 64 * 1024;

  private static final String EXCHANGE_WEB_SERVICES = "EWS";

  private static final String POP3 = "POP3";

  private static final String POP3S = "POP3S";

  private static final String IMAP = "IMAP";

  private static final String IMAPS = "IMAPS";

  private static final String PROVIDER_NAME = "BC";

  private static final int DEFAULT_MAX_ATTACHMENT_COUNT = 100;

  /* Unit is bit */
  private static final int DEFAULT_ATTACHMENT_SEGMENT_SIZE = 512000;

  /* the default value of the maximum quantity for receiving mails during one time */
  private static final int DEFAULT_MAX_MAIL_QUANTITY = 100;

  private static final String DEFAULT_SOURCE_FOLDER_NAME = "INBOX";

  private static final String DEFAULT_TO_FOLDER_NAME = "Deleted Items";

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

  protected Session session;

  protected Store store;

  protected Folder sourceFolder;

  protected Folder toFolder;

  protected FetchProfile profile;

  protected static int attachmentsegmentsize;

  protected static int maxattachmentcount;

  protected ExchangeService service;

  protected String uri;

  /* the maximum quantity for receiving mails during one time */
  protected int maxMailQuantity;

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
    @Override
    public boolean autodiscoverRedirectionUrlValidationCallback(String redirectionUrl) {
      return redirectionUrl.toLowerCase().startsWith("https://");
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void initialize(String jsonParam) throws Exception {
    JSONObject jsonObject = JSONObject.fromObject(jsonParam);
    Map<String, Object> map = MailUtil.convertJsonToMap(jsonObject);
    if (map != null && !map.isEmpty()) {
      this.host = (String) map.get("host");
      this.port = (String) map.get("port");
      this.protocol = (String) map.get("protocol");
      this.username = (String) map.get("username");
      this.password = (String) map.get("password");
      this.suffixList = (List<String>) map.get("suffixList");
      this.authorisedUserList = (List<String>) map.get("authorisedUserList");
      this.proxySet = (Boolean) map.get("proxySet");
      this.proxyHost = (String) map.get("proxyHost");
      this.proxyPort = (String) map.get("proxyPort");
      this.sourceFolderName = (String) map.get("sourceFolderName");
      this.toFolderName = (String) map.get("toFolderName");
      this.uri = (String) map.get("uri");
      this.maxMailQuantity = (Integer) map.get("maxMailQuantity");

      if (isNull(this.protocol)) {
        String e = "Missing mandatory values, please check that you have entered the protocol.";
        LOG.error(e);
        throw new Exception(e);
      } else if (isNull(this.username) || isNull(this.password)) {
        String e = "Missing mandatory values, please check that you have entered the username, password.";
        LOG.error(e);
        throw new Exception(e);
      } else if (isNull(this.host) || isNull(this.port) || isNull(this.username) || isNull(this.password)) {
        String e = "Missing mandatory values, please check that you have entered the host, port, username or password.";
        LOG.error(e);
        throw new Exception(e);
      } else if (!isAuthorisedUsername(this.authorisedUserList, this.username)) {
        String e = "The user name is not belong to authorised user domain.";
        LOG.error(e);
        throw new Exception(e);
      } else {
        if (isNull(this.sourceFolderName)) {
          this.sourceFolderName = DEFAULT_SOURCE_FOLDER_NAME;
        }
        if (isNull(this.toFolderName)) {
          this.toFolderName = DEFAULT_TO_FOLDER_NAME;
        }
        if (this.maxMailQuantity <= 0) {
          this.maxMailQuantity = DEFAULT_MAX_MAIL_QUANTITY;
        }
      }
    } else {
      String e = "May be the JSON Arguments is null.";
      LOG.error(e);
      throw new Exception(e);
    }
    LOG.info("Initialize successfully.");
  }

  @Override
  public void open() throws Exception {
    if (this.protocol.equalsIgnoreCase(EXCHANGE_WEB_SERVICES)) {
      // open connection for Exchange Web Services
      this.service = new ExchangeService();
      ExchangeCredentials credentials = new WebCredentials(this.username, this.password);
      this.service.setCredentials(credentials);
      if (isNull(this.uri)) {
        this.service.autodiscoverUrl(this.username, new RedirectionUrlCallback());
      } else {
        this.service.setUrl(new URI(this.uri));
      }
      this.service.setTraceEnabled(false);
      if (this.proxySet) {
        // For EWS proxy
        WebProxy proxy = new WebProxy(this.proxyHost, Integer.valueOf(this.proxyPort));
        this.service.setWebProxy(proxy);
      }
    } else {
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
  }

  @Override
  public JSONArray getMsgIdList() throws Exception {
    JSONArray jsonArray = null;
    List<String> msgIdList = new ArrayList<String>();

    if (this.protocol.equalsIgnoreCase(EXCHANGE_WEB_SERVICES)) {
      FolderId sourceFolderId = this.checkOrCreateEWSFolder(this.sourceFolderName);
      ItemView view = new ItemView(this.maxMailQuantity);
      FindItemsResults<Item> findResults = this.service.findItems(sourceFolderId, view);
      for (Item item : findResults) {
        msgIdList.add(item.getId().toString());
      }
    } else {
      Message[] msgs = this.sourceFolder.getMessages();
      // Get mails and UID
      this.sourceFolder.fetch(msgs, this.profile);
      // restrict reading mail message size to 'maxMailSize'.
      for (Message msg : msgs) {
        MimeMessage mmsg = (MimeMessage) msg;
        msgIdList.add(mmsg.getMessageID());
      }
    }
    jsonArray = JSONArray.fromObject(msgIdList);
    return jsonArray;
  }

  @Override
  public JSONArray receive(String messageId, boolean save) throws Exception {
    JSONArray jsonArray = null;
    if (EXCHANGE_WEB_SERVICES.equalsIgnoreCase(this.protocol)) {
      jsonArray = this.receiveViaEWS(messageId, save);
    } else {
      jsonArray = this.receiveViaExchangeServer(messageId, save);
    }
    return jsonArray;
  }

  @Override
  public JSONArray receiveAttachment(String messageId) throws Exception {
    JSONArray jsonArray = null;
    if (POP3.equalsIgnoreCase(this.protocol) || POP3S.equalsIgnoreCase(this.protocol) || IMAP.equalsIgnoreCase(this.protocol) || IMAPS.equalsIgnoreCase(this.protocol)) {
      jsonArray = this.receive(messageId, true);
    } else if (this.protocol.equalsIgnoreCase(EXCHANGE_WEB_SERVICES)) {
      jsonArray = this.receiveViaEWS(messageId, true);
    }
    return jsonArray;
  }

  @Override
  public void moveMessage(String messageId) throws Exception {
    if (POP3.equalsIgnoreCase(this.protocol) || POP3S.equalsIgnoreCase(this.protocol) || IMAP.equalsIgnoreCase(this.protocol) || IMAPS.equalsIgnoreCase(this.protocol)) {
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
    } else if (this.protocol.equalsIgnoreCase(EXCHANGE_WEB_SERVICES)) {
      EmailMessage emailMessage = EmailMessage.bind(this.service, new ItemId(messageId));

      FolderId folderId = this.checkOrCreateEWSFolder(this.toFolderName);
      emailMessage.move(folderId);
    }
  }

  @Override
  public void close() throws MessagingException {
    if (POP3.equalsIgnoreCase(this.protocol) || POP3S.equalsIgnoreCase(this.protocol) || IMAP.equalsIgnoreCase(this.protocol) || IMAPS.equalsIgnoreCase(this.protocol)) {
      // close the folder, true means that will indeed to delete the message,
      // false means that will not delete the message.
      this.closeFolder(this.sourceFolderName, true);
      this.closeFolder(this.toFolderName, true);
      this.store.close();
    }
  }

  @Override
  public void deleteAttachments(String path) {
    File file = new File(path);
    if (file.isFile() && file.exists()) {
      file.delete();
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
   * @param authorisedUserList
   * @param username
   * @return
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
   * Check the folder is existing or not. If not, create a new folder.
   * 
   * @param folderName
   * @return
   * @throws Exception
   */
  private FolderId checkOrCreateEWSFolder(String folderName) throws Exception {
    boolean folderExists = false;
    FolderId folderId = null;
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
    return folderId;
  }

  /**
   * Receive email via Exchange Web Service.
   * 
   * @param messageId
   *          message id
   * @param save
   *          true, attachments will be saved.
   * @return
   * @throws Exception
   */
  private JSONArray receiveViaEWS(String messageId, boolean save) throws Exception {
    JSONArray jsonArray = null;
    ItemId itemId = new ItemId(messageId);
    MailMessage mailMsg = this.readEmailItem(itemId, save);
    jsonArray = JSONArray.fromObject(mailMsg);
    return jsonArray;
  }

  /**
   * Gets a list of Internet headers for this item.
   * 
   * @param emailMessage
   * @param mailMsg
   * @throws Exception
   */
  private void getHeader(EmailMessage emailMessage, MailMessage mailMsg) throws Exception {
    String xAutoResponseSuppressHeaderName = "X-Auto-Response-Suppress";
    String xAutoReplyHeaderName = "X-Autoreply";
    String xAutoRespondHeaderName = "X-Autorespond";
    String xAutoSubmittedHeaderName = "auto-submitted";
    String contentTypeHeaderName = "Content-Type";

    String xAutoResponseSuppressVal = null;
    String xAutoReplyVal = null;
    String xAutoRespondVal = null;
    String xAutoSubmittedVal = null;
    String contentTypeVal = null;

    ExtendedPropertyDefinition prTransportMessageHeaders = new ExtendedPropertyDefinition(0x007D, MapiPropertyType.String);
    PropertySet propertySet = new PropertySet(BasePropertySet.FirstClassProperties, prTransportMessageHeaders);
    emailMessage.load(propertySet);

    List<InternetMessageHeader> internetMessageHeaderList = emailMessage.getInternetMessageHeaders().getItems();
    for (InternetMessageHeader header : internetMessageHeaderList) {
      String name = header.getName();
      if (xAutoResponseSuppressHeaderName.equalsIgnoreCase(name)) {
        xAutoResponseSuppressVal = header.getValue();
      } else if (xAutoReplyHeaderName.equalsIgnoreCase(name)) {
        xAutoReplyVal = header.getValue();
      } else if (xAutoRespondHeaderName.equalsIgnoreCase(name)) {
        xAutoRespondVal = header.getValue();
      } else if (xAutoSubmittedHeaderName.equalsIgnoreCase(name)) {
        xAutoSubmittedVal = header.getValue();
      } else if (contentTypeHeaderName.equalsIgnoreCase(name)) {
        contentTypeVal = header.getValue();
      }
    }
    // If any of those are present in an email, then that email is an auto-reply.
    String[] autoReplyArray = { xAutoResponseSuppressVal, xAutoReplyVal, xAutoRespondVal, xAutoSubmittedVal };
    mailMsg.setAutoReply(autoReplyArray);
    mailMsg.setContentType(contentTypeVal);
  }

  /**
   * Reading one email at a time using Item ID of the email.
   * 
   * @throws Exception
   */
  private MailMessage readEmailItem(ItemId itemId, boolean save) throws Exception {
    MailMessage mailMsg = new MailMessage();
    Item item = Item.bind(this.service, itemId, PropertySet.FirstClassProperties);
    EmailMessage emailMessage = EmailMessage.bind(this.service, item.getId());
    this.getHeader(emailMessage, mailMsg);
    this.processEWSMsg(emailMessage, mailMsg, save);
    // flag the email message as read.
    emailMessage.setIsRead(true);
    emailMessage.update(ConflictResolutionMode.AutoResolve);
    return mailMsg;
  }

  /**
   * Process the email message receiving from EWS.
   * 
   * @param emailMessage
   * @param mailMsg
   * @param save
   * @return
   * @throws ServiceVersionException
   * @throws ServiceLocalException
   * @throws Exception
   */
  private MailMessage processEWSMsg(EmailMessage emailMessage, MailMessage mailMsg, boolean save) throws ServiceVersionException, ServiceLocalException, Exception {
    this.setEWSBasicInfoForMailMsg(emailMessage, mailMsg);
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

  /**
   * Set some basic information from EWS to MailMessage .
   * 
   * @param msg
   * @param mailMsg
   * @throws ServiceLocalException
   */
  private void setEWSBasicInfoForMailMsg(EmailMessage emailMessage, MailMessage mailMsg) throws ServiceLocalException {
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

  /**
   * Process EWS attachments.
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
  private void processEWSAttachment(FileAttachment fileAttachment, MailMessage mailMsg, List<Attachment> mailAttachList) throws MessagingException, IOException, CMSException, ServiceVersionException {
    if ("multipart/signed".equalsIgnoreCase(fileAttachment.getContentType())) {
      String fileName = this.saveEWSAttachment(fileAttachment, mailMsg, mailAttachList);
      DataSource source = new FileDataSource(fileName);
      MimeMultipart multi1 = new MimeMultipart(source);
      for (int i = 0; i < multi1.getCount(); i++) {
        Part part1 = multi1.getBodyPart(i);
        if (part1.getContent() instanceof Multipart) {
          Multipart multi2 = (Multipart) part1.getContent();
          for (int j = 0; j < multi2.getCount(); j++) {
            Part part2 = multi2.getBodyPart(j);
            // generally if the content type is multipart/alternative, it is email text.
            if (part2.isMimeType("multipart/alternative")) {
              if (part2.getContent() instanceof Multipart) {
                Multipart multi3 = (Multipart) part2.getContent();
                for (int k = 0; k < multi3.getCount(); k++) {
                  Part part4 = multi3.getBodyPart(k);
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

  /**
   * Save EWS attachments.
   * 
   * @param fileAttachment
   * @param mailMsg
   * @param mailAttachList
   * @return
   * @throws ServiceVersionException
   * @throws IOException
   */
  private String saveEWSAttachment(FileAttachment fileAttachment, MailMessage mailMsg, List<Attachment> mailAttachList) throws ServiceVersionException, IOException {
    // generate a new file name with unique UUID.
    String fileName = fileAttachment.getName();
    UUID uuid = UUID.randomUUID();
    String prefix = fileName.substring(0, fileName.lastIndexOf(".") + 1);
    String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
    if (this.suffixList.contains(suffix.toLowerCase()) || "p7m".equalsIgnoreCase(suffix.toLowerCase())) {
      String tempDir = System.getProperty("java.io.tmpdir");
      fileName = tempDir + prefix + uuid + "." + suffix;

      int fileSize = fileAttachment.getSize();
      Attachment mailAttachment = new Attachment();
      mailAttachment.setFileName(fileName);
      mailAttachment.setFileType(suffix);
      mailAttachment.setFileSize(fileSize);
      mailAttachList.add(mailAttachment);
      mailMsg.setAttachList(mailAttachList);
      this.saveByteFile(fileName, fileAttachment.getContent());
    }
    return fileName;
  }

  /**
   * Save file to temp directory.
   * 
   * @param fileName
   * @param buf
   * @param fileSize
   * @throws IOException
   */
  private void saveByteFile(String fileName, byte[] buf) throws IOException {
    File file = new File(fileName);
    if (!file.exists()) {
      OutputStream out = null;
      try {
        out = new BufferedOutputStream(new FileOutputStream(file));
        if (buf != null && buf.length != 0) {
          out.write(buf);
        }
      } finally {
        if (out != null) {
          out.close();
        }
      }
    }
  }

  /**
   * Receive email via Exchange Server.
   * 
   * @param messageId
   *          message id
   * @param save
   *          true, attachments will be saved.
   * @return
   * @throws IOException
   * @throws CMSException
   * @throws MessagingException
   */
  private JSONArray receiveViaExchangeServer(String messageId, boolean save) throws IOException, CMSException, MessagingException {
    JSONArray jsonArray = null;
    // receive mails according to the message id.
    SearchTerm st = new MessageIDTerm(messageId);
    Message[] messages = this.sourceFolder.search(st);

    MailMessage mailMsg = new MailMessage();
    mailMsg.setMsgId(messageId);
    Message msg = messages[0];
    this.getHeader(msg, mailMsg);
    mailMsg = this.processMsg(msg, save);
    jsonArray = JSONArray.fromObject(mailMsg);
    return jsonArray;
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

    String xAutoResponseSuppressVal = msg.getHeader(xAutoResponseSuppressHeaderName).toString();
    String xAutoReplyVal = msg.getHeader(xAutoReplyHeaderName).toString();
    String xAutoRespondVal = msg.getHeader(xAutoRespondHeaderName).toString();
    String xAutoSubmittedVal = msg.getHeader(xAutoSubmittedHeaderName).toString();
    String contentType = msg.getContentType();

    // If any of those are present in an email, then that email is an auto-reply.
    String[] autoReplyArray = { xAutoResponseSuppressVal, xAutoReplyVal, xAutoRespondVal, xAutoSubmittedVal };
    mailMsg.setAutoReply(autoReplyArray);
    mailMsg.setContentType(contentType);
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
   */
  private MailMessage processMsg(Message message, boolean save) throws MessagingException, IOException, CMSException {
    MimeMessage msg = (MimeMessage) message;
    MailMessage mailMsg = new MailMessage();

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
      mailMsg.setMailStatus(MailStatus.Encrypted_Mail);
      String e = "It's an encrypted mail. Can not handle the Message receiving from Mail Server.";
      LOG.error(e);
    } else {
      mailMsg.setMailStatus(MailStatus.Unkonwn_MIME_Type);
      String e = "Unknown MIME Type | Message Content Type: " + msg.getContentType() + "Message Subject: " + msg.getSubject() + "Message Send Date: " + msg.getSentDate() + "Message From: " + msg.getFrom().toString();
      LOG.error(e);
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
   */
  private boolean validateSignedMail(Message msg, MailMessage mailMsg) throws MessagingException, CMSException, IOException {
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
                  this.processExchangeServerEmailBodyAttachment(part4, mailMsg, attachList, save);
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

    mailMsg.setFrom(MailUtil.convertToMailAddress(from));
    mailMsg.setTo(MailUtil.convertToMailAddress(to));
    mailMsg.setCc(MailUtil.convertToMailAddress(cc));
    mailMsg.setBcc(MailUtil.convertToMailAddress(bcc));
    mailMsg.setSubject(subject);
    mailMsg.setSendDate(sendDate);
  }

  /**
   * Process attachments.
   *
   * @param part
   * @param mailMsg
   * @param attachList
   * @param save
   * @throws MessagingException
   * @throws IOException
   */
  private void processExchangeServerEmailBodyAttachment(Part part, MailMessage mailMsg, List<Attachment> attachList, boolean save) throws IOException, MessagingException {
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
  }

  /**
   * Process attachments and save it.
   * 
   * @param part
   * @param mailMsg
   * @param attachList
   * @param save
   *          true indicates process the attachment and save it.
   * @throws MessagingException
   * @throws IOException
   */
  private void processAttachment(Part part, MailMessage mailMsg, List<Attachment> attachList, boolean save) throws MessagingException, IOException {
    String disposition = null;
    disposition = part.getDisposition();
    if ((disposition != null && Part.ATTACHMENT.equalsIgnoreCase(disposition))) {
      mailMsg.setHasAttachments(true);
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
          mailMsg.setMailStatus(MailStatus.Receive_Attachment_Successfully);
        } else {
          LOG.info(fileName + " is not a supported file. Ignore this file.");
        }
      }
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
   * @throws MessagingException
   */
  private int countMailAttachments(Multipart multi) throws MessagingException {
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
  private int countMailSize(Multipart multi, int mailSize) throws MessagingException, IOException {
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
  private boolean exceedMaxMsgSize(int msgSize) {
    boolean exceedMaxMsgSize = false;
    if (msgSize > attachmentsegmentsize * maxattachmentcount) {
      exceedMaxMsgSize = true;
      LOG.error("The size of all the attachments exceed the maximum value!");
    }
    return exceedMaxMsgSize;
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
