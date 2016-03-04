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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.logging.LogFactory;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;

import com.hp.ov.sm.common.core.Init;
import com.hp.ov.sm.common.core.JLog;
import com.java.mail.JSONUtil;
import com.java.mail.ReceiveMail;
import com.java.mail.domain.Attachment;
import com.java.mail.domain.MailMessage;
import com.java.mail.domain.MailStatus;

import net.sf.json.JSONObject;

/**
 * @author zhontao
 *
 */
public abstract class AbstractMailReceiver implements ReceiveMail {

  private static final JLog LOG = new JLog(LogFactory.getLog(AbstractMailReceiver.class));

  private static final int DEFAULT_MAX_ATTACHMENT_COUNT = 100;

  /* Unit is bit */
  private static final int DEFAULT_ATTACHMENT_SEGMENT_SIZE = 512000;

  /* the default value of the maximum quantity for receiving mails during one time */
  private static final int DEFAULT_MAX_MAIL_QUANTITY = 100;

  private static final String DEFAULT_SOURCE_FOLDER_NAME = "INBOX";

  private static final String DEFAULT_TO_FOLDER_NAME = "Deleted Items";

  private static final String PROVIDER_NAME = "BC";

  private static final int BUFFSIZE = 64 * 1024;

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
  @SuppressWarnings("unchecked")
  public void initialize(String jsonParam) throws Exception {
    JSONObject jsonObject = JSONObject.fromObject(jsonParam);
    Map<String, Object> map = JSONUtil.convertJsonToMap(jsonObject);
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
  private static boolean isAuthorisedUsername(List<String> authorisedUserList, String username) {
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
   * Verify the email is signed by the given certificate.
   * 
   * @param signedData
   * @param mailMsg
   * @return
   */
  @SuppressWarnings({ "rawtypes" })
  protected boolean isValid(CMSSignedData signedData, MailMessage mailMsg) {
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
  protected void processAttachment(Part part, MailMessage mailMsg, List<Attachment> attachList, boolean save) throws MessagingException, IOException {
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
