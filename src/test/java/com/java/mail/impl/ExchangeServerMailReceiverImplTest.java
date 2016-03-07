package com.java.mail.impl;

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
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.java.mail.MailReceiver;
import com.java.mail.domain.MailMessage;

import net.sf.json.JSONArray;

/**
 * @author zhontao
 * 
 *         Unit Test for {@link com.hp.ov.sm.server.utility.htmlemail.impl.ExchangeServerMailReceiverImpl}
 *
 */
public class ExchangeServerMailReceiverImplTest {

  private Session sendMailSession;

  private String fromAddress;

  private String toAddress;

  private String subject;

  private static final String INSTANCE_NAME = "Exchange Server";

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
    List<String> authorisedUserList = new ArrayList<String>();
    authorisedUserList.add("@hpe.com");
    paramMap.put("authorisedUserList", authorisedUserList);
    paramMap.put("proxySet", false);
    paramMap.put("sourceFolderName", sourceFolderName);
    paramMap.put("toFolderName", toFolderName);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    MailReceiver receive = MailReceiverFactory.getInstance(INSTANCE_NAME);
    receive.initialize(paramJson);
  }

  @Test(expected = Exception.class)
  public void testInitializeMissingValue() throws Exception {
    MailReceiver receive = new ExchangeServerMailReceiverImpl();
    receive.initialize(null);
  }

  @Test(expected = Exception.class)
  public void testInitializeUserMissing() throws Exception {
    String sourceFolderName = "SmartEmail";
    String toFolderName = "Deleted Items";

    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put("host", "webmail.hp.com");
    paramMap.put("port", "993");
    paramMap.put("auth", null);
    paramMap.put("protocol", "imaps");
    paramMap.put("password", "password");
    paramMap.put("maxMailQuantity", 10);
    List<String> authorisedUserList = new ArrayList<String>();
    authorisedUserList.add("@hpe.com");
    paramMap.put("authorisedUserList", authorisedUserList);
    paramMap.put("proxySet", false);
    paramMap.put("sourceFolderName", sourceFolderName);
    paramMap.put("toFolderName", toFolderName);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    MailReceiver receive = MailReceiverFactory.getInstance(INSTANCE_NAME);
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
    List<String> authorisedUserList = new ArrayList<String>();
    authorisedUserList.add("@hpe.com");
    paramMap.put("authorisedUserList", authorisedUserList);
    paramMap.put("proxySet", false);
    paramMap.put("sourceFolderName", sourceFolderName);
    paramMap.put("toFolderName", toFolderName);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    MailReceiver receive = MailReceiverFactory.getInstance(INSTANCE_NAME);
    receive.initialize(paramJson);
  }

  @Test
  public void testInitializeMaxMailQuantityIsEqualToZero() throws Exception {
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
    List<String> authorisedUserList = new ArrayList<String>();
    authorisedUserList.add("@hpe.com");
    paramMap.put("authorisedUserList", authorisedUserList);
    paramMap.put("proxySet", false);
    paramMap.put("sourceFolderName", sourceFolderName);
    paramMap.put("toFolderName", toFolderName);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    MailReceiver receive = MailReceiverFactory.getInstance(INSTANCE_NAME);
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
    List<String> authorisedUserList = new ArrayList<String>();
    authorisedUserList.add("@hpe.com");
    paramMap.put("authorisedUserList", authorisedUserList);
    paramMap.put("proxySet", false);
    paramMap.put("sourceFolderName", sourceFolderName);
    paramMap.put("toFolderName", toFolderName);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    MailReceiver receive = MailReceiverFactory.getInstance(INSTANCE_NAME);
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
    List<String> authorisedUserList = new ArrayList<String>();
    authorisedUserList.add("@hpe.com");
    paramMap.put("authorisedUserList", authorisedUserList);
    paramMap.put("proxySet", false);
    paramMap.put("sourceFolderName", sourceFolderName);
    paramMap.put("toFolderName", toFolderName);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    MailReceiver receive = MailReceiverFactory.getInstance(INSTANCE_NAME);
    receive.initialize(paramJson);
  }

  @Test
  public void testProcessMsgTextPlain() throws Exception {
    Address from = new InternetAddress(this.fromAddress);
    Address to = new InternetAddress(this.toAddress);
    MailMessage mailMsg = new MailMessage();
    Message mimeMsg = new MimeMessage(this.sendMailSession);
    mimeMsg.setFrom(from);
    mimeMsg.setRecipient(Message.RecipientType.TO, to);
    mimeMsg.setSubject(this.subject);
    Date sendDate = new Date();
    mimeMsg.setSentDate(sendDate);
    String content = "TXT Mail Content";
    mimeMsg.setText(content);
    mimeMsg.saveChanges();
    MailReceiver receive = MailReceiverFactory.getInstance(INSTANCE_NAME);

    Method processMsg = receive.getClass().getDeclaredMethod("processMsg", Message.class, MailMessage.class, boolean.class);
    processMsg.setAccessible(true);
    mailMsg = (MailMessage) processMsg.invoke(receive, mimeMsg, mailMsg, false);
    Assert.assertNotNull(mailMsg);
    Assert.assertEquals(this.fromAddress, mailMsg.getFrom().get(0).getAddress());
    Assert.assertEquals(this.toAddress, mailMsg.getTo().get(0).getAddress());
    Assert.assertEquals(this.subject, mailMsg.getSubject());
    Assert.assertEquals(sendDate.toString(), mailMsg.getSendDate().toString());
    Assert.assertEquals(content, mailMsg.getTxtBody());
  }

  @Test
  public void testProcessMsgHTML() throws Exception {
    Address from = new InternetAddress(this.fromAddress);
    Address to = new InternetAddress(this.toAddress);
    MailMessage mailMsg = new MailMessage();
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
    MailReceiver receive = MailReceiverFactory.getInstance(INSTANCE_NAME);

    Method processMsg = receive.getClass().getDeclaredMethod("processMsg", Message.class, MailMessage.class, boolean.class);
    processMsg.setAccessible(true);
    mailMsg = (MailMessage) processMsg.invoke(receive, mimeMsg, mailMsg, false);
    Assert.assertNotNull(mailMsg);
    Assert.assertEquals(this.fromAddress, mailMsg.getFrom().get(0).getAddress());
    Assert.assertEquals(this.toAddress, mailMsg.getTo().get(0).getAddress());
    Assert.assertEquals(this.subject, mailMsg.getSubject());
    Assert.assertEquals(sendDate.toString(), mailMsg.getSendDate().toString());
    Assert.assertEquals(content, mailMsg.getHtmlBody());
  }
}
