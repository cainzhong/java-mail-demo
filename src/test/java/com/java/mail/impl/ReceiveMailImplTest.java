package com.java.mail.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.ov.sm.common.core.JLog;
import com.java.mail.ReceiveMail;
import com.java.mail.domain.MailMessage;

import net.sf.json.JSONArray;

public class ReceiveMailImplTest {

  private static final JLog LOG = new JLog(LogFactory.getLog(ReceiveMailImplTest.class));

  private Session sendMailSession;

  private String fromAddress;

  private String toAddress;

  private String subject;

  @Before
  public void setUp() {
    Properties props = new Properties();
    this.sendMailSession = Session.getDefaultInstance(props, null);
    this.fromAddress = "from@from.from";
    this.toAddress = "to@to.to";
    this.subject = "Subject";
  }

  @Test
  public void testInitializeSuccessfully() throws Exception {
    String sourceFolderName = "SmartEmail";
    String toFolderName = "Deleted Items";

    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put("host", "webmail.hp.com");
    paramMap.put("port", "993");
    paramMap.put("auth", null);
    paramMap.put("protocol", "imaps");
    paramMap.put("username", "tao.zhong@hpe.com");
    paramMap.put("password", "password");
    paramMap.put("maxMailQuantity", 10);
    List<String> suffixList = new ArrayList<String>();
    suffixList.add("css");
    suffixList.add("png");
    suffixList.add("jpg");
    suffixList.add("txt");
    paramMap.put("suffixList", suffixList);
    List<String> authorisedUserList = new ArrayList<String>();
    authorisedUserList.add("@hpe.com");
    paramMap.put("authorisedUserList", authorisedUserList);
    paramMap.put("proxySet", false);
    paramMap.put("proxyHost", "");
    paramMap.put("proxyPort", "");
    paramMap.put("proxyUser", "");
    paramMap.put("proxyPassword", "");
    paramMap.put("proxyDomain", "");
    paramMap.put("sourceFolderName", sourceFolderName);
    paramMap.put("toFolderName", toFolderName);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    ReceiveMail receive = new AbstractMailReceiver();
    receive.initialize(paramJson);
  }

  @Test(expected = Exception.class)
  public void testInitializeMissingValue() throws Exception {
    ReceiveMail receive = new AbstractMailReceiver();
    receive.initialize(null);
  }

  @Test(expected = Exception.class)
  public void testInitializeProtocolMissing() throws Exception {
    String sourceFolderName = "SmartEmail";
    String toFolderName = "Deleted Items";

    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put("host", "webmail.hp.com");
    paramMap.put("port", "993");
    paramMap.put("auth", null);
    paramMap.put("username", "tao.zhong@hpe.com");
    paramMap.put("password", "password");
    paramMap.put("maxMailQuantity", 10);
    List<String> suffixList = new ArrayList<String>();
    suffixList.add("css");
    suffixList.add("png");
    suffixList.add("jpg");
    suffixList.add("txt");
    paramMap.put("suffixList", suffixList);
    List<String> authorisedUserList = new ArrayList<String>();
    authorisedUserList.add("@hpe.com");
    paramMap.put("authorisedUserList", authorisedUserList);
    paramMap.put("proxySet", false);
    paramMap.put("proxyHost", "");
    paramMap.put("proxyPort", "");
    paramMap.put("proxyUser", "");
    paramMap.put("proxyPassword", "");
    paramMap.put("proxyDomain", "");
    paramMap.put("sourceFolderName", sourceFolderName);
    paramMap.put("toFolderName", toFolderName);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    ReceiveMail receive = new AbstractMailReceiver();
    receive.initialize(paramJson);
  }

  @Test(expected = Exception.class)
  public void testInitializeUPMissing() throws Exception {
    String sourceFolderName = "SmartEmail";
    String toFolderName = "Deleted Items";

    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put("host", "webmail.hp.com");
    paramMap.put("port", "993");
    paramMap.put("auth", null);
    paramMap.put("protocol", "imaps");
    paramMap.put("password", "password");
    paramMap.put("maxMailQuantity", 10);
    List<String> suffixList = new ArrayList<String>();
    suffixList.add("css");
    suffixList.add("png");
    suffixList.add("jpg");
    suffixList.add("txt");
    paramMap.put("suffixList", suffixList);
    List<String> authorisedUserList = new ArrayList<String>();
    authorisedUserList.add("@hpe.com");
    paramMap.put("authorisedUserList", authorisedUserList);
    paramMap.put("proxySet", false);
    paramMap.put("proxyHost", "");
    paramMap.put("proxyPort", "");
    paramMap.put("proxyUser", "");
    paramMap.put("proxyPassword", "");
    paramMap.put("proxyDomain", "");
    paramMap.put("sourceFolderName", sourceFolderName);
    paramMap.put("toFolderName", toFolderName);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    ReceiveMail receive = new AbstractMailReceiver();
    receive.initialize(paramJson);
  }

  @Test(expected = Exception.class)
  public void testInitializeHPUPMissing() throws Exception {
    String sourceFolderName = "SmartEmail";
    String toFolderName = "Deleted Items";

    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put("host", "webmail.hp.com");
    paramMap.put("auth", null);
    paramMap.put("protocol", "imaps");
    paramMap.put("username", "tao.zhong@hpe.com");
    paramMap.put("password", "password");
    paramMap.put("maxMailQuantity", 10);
    List<String> suffixList = new ArrayList<String>();
    suffixList.add("css");
    suffixList.add("png");
    suffixList.add("jpg");
    suffixList.add("txt");
    paramMap.put("suffixList", suffixList);
    List<String> authorisedUserList = new ArrayList<String>();
    authorisedUserList.add("@hpe.com");
    paramMap.put("authorisedUserList", authorisedUserList);
    paramMap.put("proxySet", false);
    paramMap.put("proxyHost", "");
    paramMap.put("proxyPort", "");
    paramMap.put("proxyUser", "");
    paramMap.put("proxyPassword", "");
    paramMap.put("proxyDomain", "");
    paramMap.put("sourceFolderName", sourceFolderName);
    paramMap.put("toFolderName", toFolderName);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    ReceiveMail receive = new AbstractMailReceiver();
    receive.initialize(paramJson);
  }

  @Test(expected = Exception.class)
  public void testInitializeNotAuthorisedUser() throws Exception {
    String sourceFolderName = "SmartEmail";
    String toFolderName = "Deleted Items";

    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put("host", "webmail.hp.com");
    paramMap.put("port", "993");
    paramMap.put("auth", null);
    paramMap.put("protocol", "imaps");
    paramMap.put("username", "tao.zhong@hpe1.com");
    paramMap.put("password", "password");
    paramMap.put("maxMailQuantity", 10);
    List<String> suffixList = new ArrayList<String>();
    suffixList.add("css");
    suffixList.add("png");
    suffixList.add("jpg");
    suffixList.add("txt");
    paramMap.put("suffixList", suffixList);
    List<String> authorisedUserList = new ArrayList<String>();
    authorisedUserList.add("@hpe.com");
    paramMap.put("authorisedUserList", authorisedUserList);
    paramMap.put("proxySet", false);
    paramMap.put("proxyHost", "");
    paramMap.put("proxyPort", "");
    paramMap.put("proxyUser", "");
    paramMap.put("proxyPassword", "");
    paramMap.put("proxyDomain", "");
    paramMap.put("sourceFolderName", sourceFolderName);
    paramMap.put("toFolderName", toFolderName);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    ReceiveMail receive = new AbstractMailReceiver();
    receive.initialize(paramJson);
  }

  @Test
  public void testInitializeMaxMailQuantity() throws Exception {
    String sourceFolderName = "SmartEmail";
    String toFolderName = "Deleted Items";

    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put("host", "webmail.hp.com");
    paramMap.put("port", "993");
    paramMap.put("auth", null);
    paramMap.put("protocol", "imaps");
    paramMap.put("username", "tao.zhong@hpe.com");
    paramMap.put("password", "password");
    paramMap.put("maxMailQuantity", 0);
    List<String> suffixList = new ArrayList<String>();
    suffixList.add("css");
    suffixList.add("png");
    suffixList.add("jpg");
    suffixList.add("txt");
    paramMap.put("suffixList", suffixList);
    List<String> authorisedUserList = new ArrayList<String>();
    authorisedUserList.add("@hpe.com");
    paramMap.put("authorisedUserList", authorisedUserList);
    paramMap.put("proxySet", false);
    paramMap.put("proxyHost", "");
    paramMap.put("proxyPort", "");
    paramMap.put("proxyUser", "");
    paramMap.put("proxyPassword", "");
    paramMap.put("proxyDomain", "");
    paramMap.put("sourceFolderName", sourceFolderName);
    paramMap.put("toFolderName", toFolderName);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    ReceiveMail receive = new AbstractMailReceiver();
    receive.initialize(paramJson);
  }

  @Test
  public void testInitializeMaxMailQuantityy() throws Exception {
    String sourceFolderName = "SmartEmail";
    String toFolderName = "Deleted Items";

    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put("host", "webmail.hp.com");
    paramMap.put("port", "993");
    paramMap.put("auth", null);
    paramMap.put("protocol", "imaps");
    paramMap.put("username", "tao.zhong@hpe.com");
    paramMap.put("password", "password");
    paramMap.put("maxMailQuantity", 10);
    List<String> suffixList = new ArrayList<String>();
    suffixList.add("css");
    suffixList.add("png");
    suffixList.add("jpg");
    suffixList.add("txt");
    paramMap.put("suffixList", suffixList);
    List<String> authorisedUserList = new ArrayList<String>();
    authorisedUserList.add("@hpe.com");
    paramMap.put("authorisedUserList", authorisedUserList);
    paramMap.put("proxySet", false);
    paramMap.put("proxyHost", "");
    paramMap.put("proxyPort", "");
    paramMap.put("proxyUser", "");
    paramMap.put("proxyPassword", "");
    paramMap.put("proxyDomain", "");
    paramMap.put("sourceFolderName", sourceFolderName);
    paramMap.put("toFolderName", toFolderName);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    ReceiveMail receive = new AbstractMailReceiver();
    receive.initialize(paramJson);
  }

  @Test
  public void testInitializeNoSourceFolderName() throws Exception {
    String sourceFolderName = "";
    String toFolderName = "Deleted Items";

    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put("host", "webmail.hp.com");
    paramMap.put("port", "993");
    paramMap.put("auth", null);
    paramMap.put("protocol", "imaps");
    paramMap.put("username", "tao.zhong@hpe.com");
    paramMap.put("password", "password");
    paramMap.put("maxMailQuantity", 10);
    List<String> suffixList = new ArrayList<String>();
    suffixList.add("css");
    suffixList.add("png");
    suffixList.add("jpg");
    suffixList.add("txt");
    paramMap.put("suffixList", suffixList);
    List<String> authorisedUserList = new ArrayList<String>();
    authorisedUserList.add("@hpe.com");
    paramMap.put("authorisedUserList", authorisedUserList);
    paramMap.put("proxySet", false);
    paramMap.put("proxyHost", "");
    paramMap.put("proxyPort", "");
    paramMap.put("proxyUser", "");
    paramMap.put("proxyPassword", "");
    paramMap.put("proxyDomain", "");
    paramMap.put("sourceFolderName", sourceFolderName);
    paramMap.put("toFolderName", toFolderName);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    ReceiveMail receive = new AbstractMailReceiver();
    receive.initialize(paramJson);
  }

  @Test
  public void testInitializeNoToFolderName() throws Exception {
    String sourceFolderName = "SmartEmail";
    String toFolderName = "";

    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put("host", "webmail.hp.com");
    paramMap.put("port", "993");
    paramMap.put("auth", null);
    paramMap.put("protocol", "imaps");
    paramMap.put("username", "tao.zhong@hpe.com");
    paramMap.put("password", "password");
    paramMap.put("maxMailQuantity", 10);
    List<String> suffixList = new ArrayList<String>();
    suffixList.add("css");
    suffixList.add("png");
    suffixList.add("jpg");
    suffixList.add("txt");
    paramMap.put("suffixList", suffixList);
    List<String> authorisedUserList = new ArrayList<String>();
    authorisedUserList.add("@hpe.com");
    paramMap.put("authorisedUserList", authorisedUserList);
    paramMap.put("proxySet", false);
    paramMap.put("proxyHost", "");
    paramMap.put("proxyPort", "");
    paramMap.put("proxyUser", "");
    paramMap.put("proxyPassword", "");
    paramMap.put("proxyDomain", "");
    paramMap.put("sourceFolderName", sourceFolderName);
    paramMap.put("toFolderName", toFolderName);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    ReceiveMail receive = new AbstractMailReceiver();
    receive.initialize(paramJson);
  }

  @Test
  public void testProcessMsgTextPlain() {
    try {
      Address from = new InternetAddress(this.fromAddress);
      Address to = new InternetAddress(this.toAddress);
      Message mimeMsg = new MimeMessage(this.sendMailSession);
      mimeMsg.setFrom(from);
      mimeMsg.setRecipient(Message.RecipientType.TO, to);
      mimeMsg.setSubject(this.subject);
      Date sendDate = new Date();
      mimeMsg.setSentDate(sendDate);
      String content = "TXT Mail Content";
      mimeMsg.setText(content);
      mimeMsg.saveChanges();
      Message[] messages = new MimeMessage[1];
      messages[0] = mimeMsg;
      ReceiveMail receive = new AbstractMailReceiver();

      Method processMsg = receive.getClass().getDeclaredMethod("processMsg", Message[].class, int.class, boolean.class);
      processMsg.setAccessible(true);
      int msgsLength = 1;
      List<MailMessage> msgList = (List<MailMessage>) processMsg.invoke(receive, messages, msgsLength, false);
      Assert.assertNotNull(msgList);
      for (MailMessage mailMsg : msgList) {
        Assert.assertTrue(mailMsg.getContentType().startsWith("text/plain"));
        Assert.assertEquals(this.fromAddress, mailMsg.getFrom().get(0).getAddress());
        Assert.assertEquals(this.toAddress, mailMsg.getTo().get(0).getAddress());
        Assert.assertEquals(this.subject, mailMsg.getSubject());
        Assert.assertEquals(sendDate.toString(), mailMsg.getSendDate().toString());
        Assert.assertEquals(content, mailMsg.getTxtBody());
      }
    } catch (MessagingException e) {
      LOG.error(e.toString());
    } catch (NoSuchMethodException e) {
      LOG.error(e.toString());
    } catch (SecurityException e) {
      LOG.error(e.toString());
    } catch (IllegalAccessException e) {
      LOG.error(e.toString());
    } catch (IllegalArgumentException e) {
      LOG.error(e.toString());
    } catch (InvocationTargetException e) {
      LOG.error(e.toString());
    }
  }

  @Test
  public void testProcessMsgHTML() {
    try {
      Address from = new InternetAddress(this.fromAddress);
      Address to = new InternetAddress(this.toAddress);
      Message mimeMsg = new MimeMessage(this.sendMailSession);
      mimeMsg.setFrom(from);
      mimeMsg.setRecipient(Message.RecipientType.TO, to);
      mimeMsg.setSubject(this.subject);
      Date sendDate = new Date();
      mimeMsg.setSentDate(sendDate);
      Multipart mainPart = new MimeMultipart();

      BodyPart html = new MimeBodyPart();
      String content = "<html><h2><font color=red>Hello World</font></h2></html>";
      html.setContent(content, "text/html;charset=gb2312");

      mainPart.addBodyPart(html);
      mimeMsg.setContent(mainPart);
      mimeMsg.saveChanges();
      Message[] messages = new MimeMessage[1];
      messages[0] = mimeMsg;
      ReceiveMail receive = new AbstractMailReceiver();

      Method processMsg = receive.getClass().getDeclaredMethod("processMsg", Message[].class, int.class, boolean.class);
      processMsg.setAccessible(true);
      int msgsLength = 1;
      List<MailMessage> msgList = (List<MailMessage>) processMsg.invoke(receive, messages, msgsLength, false);
      Assert.assertNotNull(msgList);
      for (MailMessage mailMsg : msgList) {
        Assert.assertTrue(mailMsg.getContentType().startsWith("multipart/mixed"));
        Assert.assertEquals(this.fromAddress, mailMsg.getFrom().get(0).getAddress());
        Assert.assertEquals(this.toAddress, mailMsg.getTo().get(0).getAddress());
        Assert.assertEquals(this.subject, mailMsg.getSubject());
        Assert.assertEquals(sendDate.toString(), mailMsg.getSendDate().toString());
        Assert.assertEquals(content, mailMsg.getHtmlBody());
      }
    } catch (MessagingException e) {
      LOG.error(e.toString());
    } catch (NoSuchMethodException e) {
      LOG.error(e.toString());
    } catch (SecurityException e) {
      LOG.error(e.toString());
    } catch (IllegalAccessException e) {
      LOG.error(e.toString());
    } catch (IllegalArgumentException e) {
      LOG.error(e.toString());
    } catch (InvocationTargetException e) {
      LOG.error(e.toString());
    }
  }

  @Test
  public void testProcessMsgAttachment() {
    try {
      Address from = new InternetAddress(this.fromAddress);
      Address to = new InternetAddress(this.toAddress);
      Message mimeMsg = new MimeMessage(this.sendMailSession);
      mimeMsg.setFrom(from);
      mimeMsg.setRecipient(Message.RecipientType.TO, to);
      mimeMsg.setSubject(this.subject);
      Date sendDate = new Date();
      mimeMsg.setSentDate(sendDate);
      Multipart mainPart = new MimeMultipart();

      BodyPart html = new MimeBodyPart();
      String content = "<html><h2><font color=red>Hello World</font></h2></html>";
      html.setContent(content, "text/html;charset=gb2312");

      BodyPart attachment = new MimeBodyPart();
      attachment.setDataHandler(null);
      attachment.setFileName("attachment");
      mainPart.addBodyPart(attachment);

      mainPart.addBodyPart(html);
      mimeMsg.setContent(mainPart);
      mimeMsg.saveChanges();
      Message[] messages = new MimeMessage[1];
      messages[0] = mimeMsg;
      ReceiveMail receive = new AbstractMailReceiver();

      Method processMsg = receive.getClass().getDeclaredMethod("processMsg", Message[].class, int.class, boolean.class);
      processMsg.setAccessible(true);
      int msgsLength = 1;
      List<MailMessage> msgList = (List<MailMessage>) processMsg.invoke(receive, messages, msgsLength, false);
      Assert.assertNotNull(msgList);
      for (MailMessage mailMsg : msgList) {
        Assert.assertTrue(mailMsg.getContentType().startsWith("multipart/mixed"));
        Assert.assertEquals(this.fromAddress, mailMsg.getFrom().get(0).getAddress());
        Assert.assertEquals(this.toAddress, mailMsg.getTo().get(0).getAddress());
        Assert.assertEquals(this.subject, mailMsg.getSubject());
        Assert.assertEquals(sendDate.toString(), mailMsg.getSendDate().toString());
        Assert.assertEquals(content, mailMsg.getHtmlBody());
        Assert.assertTrue(mailMsg.isHasAttachments());
      }
    } catch (MessagingException e) {
      LOG.error(e.toString());
    } catch (NoSuchMethodException e) {
      LOG.error(e.toString());
    } catch (SecurityException e) {
      LOG.error(e.toString());
    } catch (IllegalAccessException e) {
      LOG.error(e.toString());
    } catch (IllegalArgumentException e) {
      LOG.error(e.toString());
    } catch (InvocationTargetException e) {
      LOG.error(e.toString());
    }
  }
}