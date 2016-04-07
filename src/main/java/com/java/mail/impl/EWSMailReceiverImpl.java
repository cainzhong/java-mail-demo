/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail.impl;

import java.io.FileOutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.mail.MessagingException;

import org.apache.commons.logging.LogFactory;

import com.hp.ov.sm.common.core.JLog;
import com.java.mail.JSONUtil;
import com.java.mail.domain.MailHeader;

import microsoft.exchange.webservices.data.autodiscover.IAutodiscoverRedirectionUrl;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.WebProxy;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.enumeration.service.DeleteMode;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.FolderSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.credential.WebProxyCredentials;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.property.complex.MimeContent;
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
  public void open(String jsonParam) throws Exception {
    this.initialize(jsonParam);

    this.service = new ExchangeService();
    ExchangeCredentials credentials = new WebCredentials(this.username, this.password);
    this.service.setCredentials(credentials);
    if (isNull(this.uri)) {
      this.service.autodiscoverUrl(this.username, new RedirectionUrlCallback());
    } else {
      this.service.setUrl(new URI(this.uri));
    }
    this.service.setTraceEnabled(false);
    if (!isNull(this.proxyHost) && !isNull(this.proxyPort)) {
      // For EWS proxy
      WebProxy proxy = new WebProxy(this.proxyHost, Integer.valueOf(this.proxyPort));
      if (!isNull(this.proxyUser) && !isNull(this.proxyPassword)) {
        WebProxyCredentials proxyCredentials = new WebProxyCredentials(this.proxyUser, this.proxyPassword, null);
        proxy = new WebProxy(this.proxyHost, Integer.valueOf(this.proxyPort), proxyCredentials);
      }
      this.service.setWebProxy(proxy);
    }
  }

  @Override
  public String getMsgIdList(String date) throws Exception {
    List<MailHeader> msgIdList = new ArrayList<MailHeader>();

    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_MM_DD_YYYY_HH_MM_SS);
    Date receivedDate = sdf.parse(date);

    sdf.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_UTC));
    String currentDateStr = sdf.format(new Date());
    Date currentDate = sdf.parse(currentDateStr);

    SearchFilter receivedDateFilter = new SearchFilter.IsGreaterThanOrEqualTo(ItemSchema.DateTimeReceived, receivedDate);
    SearchFilter currentDateFilter = new SearchFilter.IsLessThanOrEqualTo(ItemSchema.DateTimeReceived, currentDate);
    SearchFilter filter = new SearchFilter.SearchFilterCollection(LogicalOperator.And, receivedDateFilter, currentDateFilter);

    FolderId sourceFolderId = this.checkOrCreateEWSFolder(this.sourceFolderName);
    ItemView view = new ItemView(this.maxMailQuantity);
    FindItemsResults<Item> findResults = this.service.findItems(sourceFolderId, filter, view);
    for (Item item : findResults) {
      EmailMessage emailMessage = EmailMessage.bind(this.service, item.getId());
      String receivedUTCDate = sdf.format(emailMessage.getDateTimeReceived());
      MailHeader mailHeader = new MailHeader();
      mailHeader.setMsgId(emailMessage.getId().toString());
      mailHeader.setFrom(this.convertToMailAddress(emailMessage.getFrom()));
      mailHeader.setReceivedUTCDate(receivedUTCDate);
      mailHeader.setMailSize(emailMessage.getSize());
      msgIdList.add(mailHeader);
    }
    return JSONArray.fromObject(msgIdList).toString();
  }

  @Override
  public String receive(String messageId) throws Exception {
    String fileName = null;
    ItemId itemId = new ItemId(messageId);
    Item item = Item.bind(this.service, itemId, PropertySet.FirstClassProperties);
    if (item != null) {
      item.load(new PropertySet(ItemSchema.MimeContent));
      MimeContent mc = item.getMimeContent();

      UUID uuid = UUID.randomUUID();
      String tempDir = System.getProperty("java.io.tmpdir");
      fileName = tempDir + uuid + "." + "eml";
      FileOutputStream fs = new FileOutputStream(fileName);
      fs.write(mc.getContent(), 0, mc.getContent().length);
      fs.close();
    } else {
      String e = "The email with message id: " + messageId + " can not be found.";
      LOG.error(e);
      throw new Exception(e);
    }
    return fileName;
  }

  @Override
  public void moveMessage(String jsonParam) throws Exception {
    JSONObject jsonObject = JSONObject.fromObject(jsonParam);
    Map<String, Object> map = JSONUtil.convertJsonToMap(jsonObject);
    if (map != null && !map.isEmpty()) {
      String msgId = (String) map.get("msgId");
      String moveToFolderName = (String) map.get("folder");

      EmailMessage emailMessage = EmailMessage.bind(this.service, new ItemId(msgId));
      FolderId folderId = this.checkOrCreateEWSFolder(moveToFolderName);
      emailMessage.move(folderId);
    } else {
      String e = "May be the JSON Arguments is null.";
      LOG.error(e);
      throw new Exception(e);
    }
  }

  @Override
  public void deleteMessage(String messageId) throws Exception {
    EmailMessage emailMessage = EmailMessage.bind(this.service, new ItemId(messageId));
    emailMessage.delete(DeleteMode.MoveToDeletedItems);
  }

  @Override
  public void close() throws MessagingException {
    // Do Nothing.
  }

  private void initialize(String jsonParam) throws Exception {
    LOG.debug(jsonParam);
    JSONObject jsonObject = JSONObject.fromObject(jsonParam);
    Map<String, Object> map = JSONUtil.convertJsonToMap(jsonObject);
    if (map != null && !map.isEmpty()) {
      this.username = (String) map.get("username");
      this.password = (String) map.get("password");
      this.proxyHost = (String) map.get("proxyHost");
      this.proxyPort = (String) map.get("proxyPort");
      this.proxyUser = (String) map.get("proxyUser");
      this.proxyPassword = (String) map.get("proxyPassword");
      this.sourceFolderName = (String) map.get("sourceFolderName");
      this.uri = (String) map.get("uri");
      if (map.get("maxMailQuantity") == null) {
        this.maxMailQuantity = DEFAULT_MAX_MAIL_QUANTITY;
      } else {
        this.maxMailQuantity = (Integer) map.get("maxMailQuantity");
      }

      if (isNull(this.username) || isNull(this.password)) {
        String e = "Missing mandatory values, please check that you have entered the username, password.";
        LOG.error(e);
        throw new Exception(e);
      } else {
        if (isNull(this.sourceFolderName) || POP3.equalsIgnoreCase(this.protocol) || POP3S.equalsIgnoreCase(this.protocol)) {
          this.sourceFolderName = DEFAULT_SOURCE_FOLDER_NAME;
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
}