package com.java.mail.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.java.mail.MailReceiver;
import com.java.mail.MailReceiverFactory;

import net.sf.json.JSONArray;

/**
 * @author zhontao
 * 
 *         Unit Test for {@link com.hp.ov.sm.server.utility.htmlemail.impl.ExchangeServerMailReceiverImpl}
 *
 */
public class ExchangeServerMailReceiverImplTest {

  private static final String INSTANCE_NAME = "Exchange Server";

  @Test
  public void testInitializeSuccessfully() throws Exception {
    String sourceFolderName = "SmartEmail";

    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put("host", "webmail.hp.com");
    paramMap.put("port", "993");
    paramMap.put("protocol", "imaps");
    paramMap.put("username", "tao.zhong@hpe.com");
    paramMap.put("password", "password");
    paramMap.put("maxMailQuantity", 10);
    paramMap.put("maxMailSize", 30 * 1024 * 1024);
    paramMap.put("sourceFolderName", sourceFolderName);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    MailReceiverFactory factory = MailReceiverFactoryImpl.getInstance();
    MailReceiver receive = factory.create(INSTANCE_NAME);

    Method initialize = receive.getClass().getDeclaredMethod("initialize", String.class);
    initialize.setAccessible(true);
    initialize.invoke(receive, paramJson);
  }

  @Test(expected = Exception.class)
  public void testInitializeMissingValue() throws Exception {
    MailReceiverFactory factory = MailReceiverFactoryImpl.getInstance();
    MailReceiver receive = factory.create(INSTANCE_NAME);

    Method initialize = receive.getClass().getDeclaredMethod("initialize", String.class);
    initialize.setAccessible(true);
    initialize.invoke(receive, "");
  }

  @Test(expected = Exception.class)
  public void testInitializeUserMissing() throws Exception {
    String sourceFolderName = "SmartEmail";

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
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    MailReceiverFactory factory = MailReceiverFactoryImpl.getInstance();
    MailReceiver receive = factory.create(INSTANCE_NAME);

    Method initialize = receive.getClass().getDeclaredMethod("initialize", String.class);
    initialize.setAccessible(true);
    initialize.invoke(receive, paramJson);
  }

  @Test(expected = Exception.class)
  public void testInitializeNotAuthorisedUser() throws Exception {
    String sourceFolderName = "SmartEmail";

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
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    MailReceiverFactory factory = MailReceiverFactoryImpl.getInstance();
    MailReceiver receive = factory.create(INSTANCE_NAME);

    Method initialize = receive.getClass().getDeclaredMethod("initialize", String.class);
    initialize.setAccessible(true);
    initialize.invoke(receive, paramJson);
  }

  @Test
  public void testInitializeMaxMailQuantityIsEqualToZero() throws Exception {
    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put("host", "webmail.hp.com");
    paramMap.put("port", "993");
    paramMap.put("protocol", "imaps");
    paramMap.put("username", "tao.zhong@hpe.com");
    paramMap.put("password", "password");
    paramMap.put("maxMailQuantity", 0);
    paramMap.put("maxMailSize", 30 * 1024 * 1024);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    MailReceiverFactory factory = MailReceiverFactoryImpl.getInstance();
    MailReceiver receive = factory.create(INSTANCE_NAME);

    Method initialize = receive.getClass().getDeclaredMethod("initialize", String.class);
    initialize.setAccessible(true);
    initialize.invoke(receive, paramJson);
  }

  @Test
  public void testInitializeNoSourceFolderName() throws Exception {
    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put("host", "webmail.hp.com");
    paramMap.put("port", "993");
    paramMap.put("protocol", "imaps");
    paramMap.put("username", "tao.zhong@hpe.com");
    paramMap.put("password", "password");
    paramMap.put("maxMailQuantity", 10);
    paramMap.put("maxMailSize", 30 * 1024 * 1024);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    MailReceiverFactory factory = MailReceiverFactoryImpl.getInstance();
    MailReceiver receive = factory.create(INSTANCE_NAME);

    Method initialize = receive.getClass().getDeclaredMethod("initialize", String.class);
    initialize.setAccessible(true);
    initialize.invoke(receive, paramJson);
  }
}