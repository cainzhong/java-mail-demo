/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
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
 *         Unit Test for {@link com.hp.ov.sm.server.utility.htmlemail.impl.EWSMailReceiverImpl}
 *
 */
public class EWSMailReceiverImplTest {

  private static final String INSTANCE_NAME = "EWS";

  @Test
  public void testInitializeSuccessfully() throws Exception {
    String sourceFolderName = "SmartEmail";
    String toFolderName = "Deleted Items";

    Map<String, Object> paramMap = new HashMap<String, Object>();
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
    String toFolderName = "Deleted Items";

    Map<String, Object> paramMap = new HashMap<String, Object>();
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

    MailReceiverFactory factory = MailReceiverFactoryImpl.getInstance();
    MailReceiver receive = factory.create(INSTANCE_NAME);

    Method initialize = receive.getClass().getDeclaredMethod("initialize", String.class);
    initialize.setAccessible(true);
    initialize.invoke(receive, paramJson);
  }

  @Test
  public void testInitializeMaxMailQuantityIsEqualToZero() throws Exception {
    String sourceFolderName = "SmartEmail";
    String toFolderName = "Deleted Items";

    Map<String, Object> paramMap = new HashMap<String, Object>();
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

    MailReceiverFactory factory = MailReceiverFactoryImpl.getInstance();
    MailReceiver receive = factory.create(INSTANCE_NAME);

    Method initialize = receive.getClass().getDeclaredMethod("initialize", String.class);
    initialize.setAccessible(true);
    initialize.invoke(receive, paramJson);
  }

  @Test
  public void testInitializeNoSourceFolderName() throws Exception {
    String sourceFolderName = "";
    String toFolderName = "Deleted Items";

    Map<String, Object> paramMap = new HashMap<String, Object>();
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

    MailReceiverFactory factory = MailReceiverFactoryImpl.getInstance();
    MailReceiver receive = factory.create(INSTANCE_NAME);

    Method initialize = receive.getClass().getDeclaredMethod("initialize", String.class);
    initialize.setAccessible(true);
    initialize.invoke(receive, paramJson);
  }

  @Test
  public void testInitializeNoToFolderName() throws Exception {
    String sourceFolderName = "SmartEmail";
    String toFolderName = "";

    Map<String, Object> paramMap = new HashMap<String, Object>();
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

    MailReceiverFactory factory = MailReceiverFactoryImpl.getInstance();
    MailReceiver receive = factory.create(INSTANCE_NAME);

    Method initialize = receive.getClass().getDeclaredMethod("initialize", String.class);
    initialize.setAccessible(true);
    initialize.invoke(receive, paramJson);
  }
}
