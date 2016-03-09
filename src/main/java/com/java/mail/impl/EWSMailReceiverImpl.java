/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.LogFactory;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.operator.OperatorCreationException;

import com.hp.ov.sm.common.core.JLog;
import com.java.mail.JSONUtil;
import com.java.mail.domain.Attachment;
import com.java.mail.domain.MailAddress;
import com.java.mail.domain.MailMessage;

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
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.EmailAddressCollection;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.property.complex.InternetMessageHeader;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.property.complex.MimeContent;
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
public class EWSMailReceiverImpl extends AbstractMailReceiver {
  private static final JLog LOG = new JLog(LogFactory.getLog(EWSMailReceiverImpl.class));

  private ExchangeService service;

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
    Map<String, Object> map = JSONUtil.convertJsonToMap(jsonObject);
    if (map != null && !map.isEmpty()) {
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

      if (isNull(this.username) || isNull(this.password)) {
        String e = "Missing mandatory values, please check that you have entered the username, password.";
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
  }

  @Override
  public JSONArray getMsgIdList() throws Exception {
    long begin = System.currentTimeMillis();
    JSONArray jsonArray = null;
    List<String> msgIdList = new ArrayList<String>();

    FolderId sourceFolderId = this.checkOrCreateEWSFolder(this.sourceFolderName);
    ItemView view = new ItemView(this.maxMailQuantity);
    FindItemsResults<Item> findResults = this.service.findItems(sourceFolderId, view);
    for (Item item : findResults) {
      msgIdList.add(item.getId().toString());
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
    ItemId itemId = new ItemId(messageId);
    MailMessage mailMsg = this.readEmailItem(itemId, save);
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
  public String saveMessage(String messageId) throws Exception {
    ItemId itemId = new ItemId(messageId);
    Item item = Item.bind(this.service, itemId, PropertySet.FirstClassProperties);
    String subject = item.getSubject();
    item.load(new PropertySet(ItemSchema.MimeContent));
    MimeContent mc = item.getMimeContent();

    UUID uuid = UUID.randomUUID();
    String tempDir = System.getProperty("java.io.tmpdir");
    String fileName = tempDir + subject + uuid + "." + "eml";
    FileOutputStream fs = new FileOutputStream(fileName);
    fs.write(mc.getContent(), 0, mc.getContent().length);
    fs.close();

    return fileName;
  }

  @Override
  public void moveMessage(String messageId) throws Exception {
    EmailMessage emailMessage = EmailMessage.bind(this.service, new ItemId(messageId));

    FolderId folderId = this.checkOrCreateEWSFolder(this.toFolderName);
    emailMessage.move(folderId);
  }

  @Override
  public void close() throws MessagingException {
    // Do Nothing.
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
    mailMsg.setFrom(this.convertToMailAddress(from));
    mailMsg.setTo(this.convertToMailAddress(to));
    mailMsg.setCc(this.convertToMailAddress(cc));
    mailMsg.setBcc(this.convertToMailAddress(bcc));
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
  private void processEWSAttachment(FileAttachment fileAttachment, MailMessage mailMsg, List<Attachment> mailAttachList) throws MessagingException, IOException, CMSException, ServiceVersionException, OperatorCreationException, CertificateException {
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
            } else if (part2.isMimeType("multipart/related")) {
              if (part2.getContent() instanceof Multipart) {
                Multipart multi3 = (Multipart) part2.getContent();
                for (int m = 0; m < multi3.getCount(); m++) {
                  Part part3 = multi3.getBodyPart(m);
                  if (part3.isMimeType("multipart/alternative")) {
                    if (part3.getContent() instanceof Multipart) {
                      Multipart multi4 = (Multipart) part3.getContent();
                      for (int p = 0; p < multi4.getCount(); p++) {
                        Part part5 = multi4.getBodyPart(p);
                        if (part5.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part5.getDisposition())) {
                          mailMsg.setTxtBody(part5.getContent().toString());
                        } else if (part5.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part5.getDisposition())) {
                          mailMsg.setHtmlBody(part5.getContent().toString());
                        }
                      }
                    }
                  } else {
                    this.processEmailBodyAttachment(part3, mailMsg, mailAttachList, true);
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
      if ("p7m".equalsIgnoreCase(fileName.substring(fileName.lastIndexOf(".") + 1))) {
        // this.deleteAttachments(fileName);
      }
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
    if (this.suffixList.contains(suffix.toLowerCase())) {
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
    } else if ("p7m".equalsIgnoreCase(suffix.toLowerCase())) {
      String tempDir = System.getProperty("java.io.tmpdir");
      fileName = tempDir + prefix + uuid + "." + suffix;
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
   * Convert a collection of e-mail addresses to a list of MailAddress.
   * 
   * @param emailAddressCollection
   *          EmailAddress is a type of <code>microsoft.exchange.webservices.data.property.complex.EmailAddress<code>.
   * @return List&lt;MailAddress&gt;
   */
  private List<MailAddress> convertToMailAddress(EmailAddressCollection emailAddressCollection) {
    List<MailAddress> addressList = new ArrayList<MailAddress>();
    if (emailAddressCollection != null) {
      Iterator<EmailAddress> it = emailAddressCollection.iterator();
      while (it.hasNext()) {
        EmailAddress emailAddress = it.next();

        MailAddress mailAddress = new MailAddress();
        mailAddress.setAddress(emailAddress.getAddress());
        mailAddress.setName(emailAddress.getName());
        addressList.add(mailAddress);
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
  private List<MailAddress> convertToMailAddress(EmailAddress emailAddress) {
    List<MailAddress> addressList = new ArrayList<MailAddress>();
    if (emailAddress != null) {
      MailAddress mailAddress = new MailAddress();
      mailAddress.setAddress(emailAddress.getAddress());
      mailAddress.setName(emailAddress.getName());
      addressList.add(mailAddress);
    }
    return addressList;
  }
}
