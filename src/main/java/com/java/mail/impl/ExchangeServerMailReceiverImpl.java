/*
o * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.UUID;

import javax.mail.Authenticator;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeMessage;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.DateTerm;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;

import org.apache.commons.logging.LogFactory;

import com.hp.ov.sm.common.core.JLog;
import com.java.mail.JSONUtil;
import com.java.mail.domain.MailHeader;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author zhontao
 *
 */
public class ExchangeServerMailReceiverImpl extends AbstractMailReceiver {
  private static final JLog LOG = new JLog(LogFactory.getLog(ExchangeServerMailReceiverImpl.class));

  @Override
  public void open(String jsonParam) throws Exception {
    this.initialize(jsonParam);

    Properties props = this.getProperties();
    if (!isNull(this.proxyUser) && !isNull(this.proxyPassword)) {
      final String socksProxyUser = this.proxyUser;
      final String socksProxyPassword = this.proxyPassword;
      Authenticator authenticator = new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          PasswordAuthentication passwordAuthentication = new PasswordAuthentication(socksProxyUser, socksProxyPassword);
          return passwordAuthentication;
        }
      };
      this.session = Session.getDefaultInstance(props, authenticator);
    } else {
      this.session = Session.getDefaultInstance(props, null);
    }
    this.store = this.session.getStore(this.protocol);
    this.store.connect(this.host, this.username, this.password);
    this.profile = new FetchProfile();
    this.profile.add(UIDFolder.FetchProfileItem.UID);
    this.profile.add(FetchProfile.Item.ENVELOPE);

    // Check the folder existing or not. If not, create a new folder.
    Folder defaultFolder = this.store.getDefaultFolder();
    if (isIMAPorIMAPS(this.protocol)) {
      if (this.checkOrCreateExchangeServerFolder(defaultFolder, this.sourceFolderName)) {
        // open mail folder
        /* POP3Folder can only receive the mails in 'INBOX', IMAPFolder can receive the mails in all folders including created by user. */
        this.sourceFolder = this.openFolder(this.sourceFolderName, Folder.READ_WRITE);
      } else {
        String e = "Fail to create the folder.";
        LOG.error(e);
        throw new Exception(e);
      }
    } else {
      // POP3 or POP3S can only open the "INBOX".
      this.sourceFolder = this.openFolder(this.sourceFolderName, Folder.READ_WRITE);
    }
  }

  @Override
  public String getMsgIdList(String date) throws MessagingException, ParseException {
    List<MailHeader> msgIdList = new ArrayList<MailHeader>();
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_MM_DD_YYYY_HH_MM_SS);
    Date receivedDate = sdf.parse(date);

    sdf.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_UTC));
    String currentDateStr = sdf.format(new Date());
    Date currentDate = sdf.parse(currentDateStr);

    Message[] msgs = new Message[] {};
    if (isIMAPorIMAPS(this.protocol)) {
      DateTerm receivedDateTerm = new ReceivedDateTerm(ComparisonTerm.GE, receivedDate);
      DateTerm currentDateTerm = new ReceivedDateTerm(ComparisonTerm.LE, currentDate);
      AndTerm andTerm = new AndTerm(receivedDateTerm, currentDateTerm);
      msgs = this.sourceFolder.search(andTerm);
    } else if (isPOP3orPOP3S(this.protocol)) {
      msgs = this.sourceFolder.getMessages();
      // Get mails and UID
      this.sourceFolder.fetch(msgs, this.profile);
    }
    for (Message msg : msgs) {
      MimeMessage mmsg = (MimeMessage) msg;
      String receivedUTCDate = sdf.format(this.resolveReceivedDate(mmsg));
      MailHeader mailHeader = new MailHeader();
      mailHeader.setMsgId(mmsg.getMessageID());
      mailHeader.setFrom(this.convertToMailAddress(mmsg.getFrom()));
      mailHeader.setReceivedUTCDate(receivedUTCDate);
      mailHeader.setMailSize(mmsg.getSize());
      msgIdList.add(mailHeader);
    }
    Collections.sort(msgIdList, MailHeader.MailHeaderComparator);
    return JSONArray.fromObject(msgIdList).toString();
  }

  @Override
  public String receive(String messageId) throws Exception {
    SearchTerm st = new MessageIDTerm(messageId);
    Message[] messages = this.sourceFolder.search(st);
    String fileName = null;
    if (messages.length > 0) {
      MimeMessage msg = (MimeMessage) messages[0];
      UUID uuid = UUID.randomUUID();
      String tempDir = System.getProperty("java.io.tmpdir");
      fileName = tempDir + uuid + ".eml";
      File saveFile = new File(fileName);
      FileOutputStream fs = new FileOutputStream(saveFile);
      msg.writeTo(fs);
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
      if (isIMAPorIMAPS(this.protocol)) {
        if (this.checkOrCreateExchangeServerFolder(this.store.getDefaultFolder(), moveToFolderName)) {
          Folder moveToFolder = this.openFolder(moveToFolderName, Folder.READ_ONLY);
          if ((this.sourceFolder != null && this.sourceFolder.isOpen()) && moveToFolder != null) {
            // receive mails according to the message id.
            SearchTerm st = new MessageIDTerm(msgId);
            Message[] messages = this.sourceFolder.search(st);
            if (messages != null && messages.length != 0) {
              Message msg = messages[0];
              Message[] needCopyMsgs = new Message[1];
              needCopyMsgs[0] = msg;
              // Copy the msg to the specific folder
              this.sourceFolder.copyMessages(needCopyMsgs, moveToFolder);
              // delete the original msg only add a delete flag on the message, it will not indeed to execute the delete operation until close the folder.
              msg.setFlag(Flags.Flag.DELETED, true);
              moveToFolder.close(true);
            } else {
              String e = "No message find.";
              LOG.error(e);
              throw new MessagingException(e);
            }
          } else {
            String e = "The folder is null or closed.";
            LOG.error(e);
            throw new MessagingException(e);
          }
        } else {
          String e = "Fail to create the folder.";
          LOG.error(e);
          throw new MessagingException(e);
        }
      } else if (isPOP3orPOP3S(this.protocol)) {
        String e = "POP3 and POP3S do not support to move a message from a folder to another.";
        LOG.error(e);
        throw new MessagingException(e);
      }
    } else {
      String e = "May be the JSON Arguments is null.";
      LOG.error(e);
      throw new Exception(e);
    }
  }

  @Override
  public void deleteMessage(String messageId) throws MessagingException {
    if (isIMAPorIMAPS(this.protocol)) {
      if ((this.sourceFolder != null && this.sourceFolder.isOpen())) {
        // receive mails according to the message id.
        SearchTerm st = new MessageIDTerm(messageId);
        Message[] messages = this.sourceFolder.search(st);
        Message msg = messages[0];
        if (null != msg) {
          // delete the original msg only add a delete flag on the message, it will not indeed to execute the delete operation until close the folder.
          msg.setFlag(Flags.Flag.DELETED, true);
        } else {
          String e = "No message find.";
          LOG.error(e);
          throw new MessagingException(e);
        }
      } else {
        String e = "The folder is null or closed.";
        LOG.error(e);
        throw new MessagingException(e);
      }
    } else if (isPOP3orPOP3S(this.protocol)) {
      String e = "POP3 and POP3S do not support to delete a message.";
      LOG.error(e);
      throw new MessagingException(e);
    }
  }

  @Override
  public void close() throws MessagingException {
    // close the folder, true means that will indeed to delete the message, false means that will not delete the message.
    this.closeFolder(this.sourceFolderName, true);
    this.store.close();
  }

  /**
   * Initialize the incoming parameters, their format is JSON.
   * 
   * @param jsonParam
   * @throws Exception
   */
  private void initialize(String jsonParam) throws Exception {
    LOG.debug(jsonParam);
    JSONObject jsonObject = JSONObject.fromObject(jsonParam);
    Map<String, Object> map = JSONUtil.convertJsonToMap(jsonObject);
    if (map != null && !map.isEmpty()) {
      this.host = (String) map.get("host");
      this.port = (String) map.get("port");
      this.protocol = (String) map.get("protocol");
      this.username = (String) map.get("username");
      this.password = (String) map.get("password");
      this.proxyHost = (String) map.get("proxyHost");
      this.proxyPort = (String) map.get("proxyPort");
      this.proxyUser = (String) map.get("proxyUser");
      this.proxyPassword = (String) map.get("proxyPassword");
      this.sourceFolderName = (String) map.get("sourceFolderName");
      if (map.get("maxMailQuantity") == null) {
        this.maxMailQuantity = DEFAULT_MAX_MAIL_QUANTITY;
      } else {
        this.maxMailQuantity = (Integer) map.get("maxMailQuantity");
      }

      if (isNull(this.protocol) || isNull(this.host) || isNull(this.username) || isNull(this.password)) {
        String e = "Missing mandatory values, please check that you have entered the protocol, host, username or password.";
        LOG.error(e);
        throw new Exception(e);
      } else {
        if (isNull(this.sourceFolderName) || isPOP3orPOP3S(this.protocol)) {
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
   * Get properties for mail session.
   * 
   * @return Properties
   */
  private Properties getProperties() {
    Properties props = new Properties();
    if (POP3.equalsIgnoreCase(this.protocol)) {
      props.put("mail.pop3.port", this.port);
      props.put("mail.pop3.rsetbeforequit", "true");
      if (!isNull(this.proxyHost) && !isNull(this.proxyPort)) {
        props.put("mail.pop3.socks.host", this.proxyHost);
        props.put("mail.pop3.socks.port", this.proxyPort);
      }
    } else if (POP3S.equalsIgnoreCase(this.protocol)) {
      props.put("mail.pop3s.port", this.port);
      props.put("mail.pop3s.rsetbeforequit", "true");
      if (!isNull(this.proxyHost) && !isNull(this.proxyPort)) {
        props.put("mail.pop3s.socks.host", this.proxyHost);
        props.put("mail.pop3s.socks.port", this.proxyPort);
      }
    } else if (IMAP.equalsIgnoreCase(this.protocol)) {
      props.put("mail.imap.port", this.port);
      props.put("mail.imap.partialfetch", "false");
      props.put("mail.imap.fetchsize", "1048576");
      props.put("mail.imap.compress.enable", "true");
      // props.put("mail.imap.starttls.enable", "true");
      if (!isNull(this.proxyHost) && !isNull(this.proxyPort)) {
        props.put("mail.imap.socks.host", this.proxyHost);
        props.put("mail.imap.socks.port", this.proxyPort);
      }
    } else if (IMAPS.equalsIgnoreCase(this.protocol)) {
      props.put("mail.imaps.port", this.port);
      props.put("mail.imaps.partialfetch", "false");
      props.put("mail.imaps.fetchsize", "1048576");
      props.put("mail.imaps.compress.enable", "true");
      // props.put("mail.imaps.starttls.enable", "true");
      if (!isNull(this.proxyHost) && !isNull(this.proxyPort)) {
        props.put("mail.imaps.socks.host", this.proxyHost);
        props.put("mail.imaps.socks.port", this.proxyPort);
      }
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
   * Check the folder is existing or not. If not, create a new folder. Only supports IMAP & IMAPS.
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
      // If parent is not the root directory, the folder should be opened.
      // parent.open(Folder.READ_WRITE);
      Folder newFolder = parent.getFolder(folderName);
      isCreated = newFolder.create(Folder.HOLDS_MESSAGES);
      // parent.close(true);
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
   * Close a folder according to the folder name if it opens.
   * 
   * @param folderName
   * @param expunge
   * @throws MessagingException
   */
  private void closeFolder(String folderName, boolean expunge) throws MessagingException {
    Folder folder = this.store.getFolder(folderName);
    if (folder != null && folder.isOpen()) {
      // close the folder, true means that will indeed to delete the message, false means that will not delete the message.
      folder.close(expunge);
    }
  }
}
