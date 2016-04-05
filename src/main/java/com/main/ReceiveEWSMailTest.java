/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.main;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;

import com.java.mail.MailReceiver;
import com.java.mail.MailReceiverFactory;
import com.java.mail.impl.MailReceiverFactoryImpl;

import net.sf.json.JSONArray;

/**
 * @author zhontao
 *
 */
public class ReceiveEWSMailTest {

  public static void main(String args[]) {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    try {
      String sourceFolderName = "INBOX";
      String toFolderName = "Deleted Items";

      Map<String, Object> paramMap = new HashMap<String, Object>();
      paramMap.put("protocol", "ews");
      paramMap.put("username", "tao.zhong@hpe.com");
      paramMap.put("password", "Cisco03#");
      paramMap.put("uri", "https://outlook.office365.com/EWS/Exchange.asmx");
      /* EWS */
      paramMap.put("maxMailQuantity", 100);
      paramMap.put("maxMailSize", 30 * 1024 * 1024);
      paramMap.put("proxyHost", "");
      paramMap.put("proxyPort", "");
      paramMap.put("mailSizeCheck", false);
      paramMap.put("maxMailSize", 30);
      paramMap.put("sourceFolderName", sourceFolderName);
      paramMap.put("toFolderName", toFolderName);
      paramMap.put("errorFolder", null);
      JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

      String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);
      MailReceiverFactory factory = MailReceiverFactoryImpl.getInstance();
      MailReceiver receiver = factory.create("EWS");
      receiver.open(paramJson);
      String result = "";
      // System.out.println("Get All message id.");
      // String msgIdList = receiver.getMsgIdList("01/26/2016 09:00:00").toString();
      // System.out.println("Message ID List: " + msgIdList);

      System.out.println("Receive Mail:");
      String msgIdList = receiver.getMsgIdList("04/05/2016 09:00:00");
      // String receive = receiver.receive("AAMkADA2MTg3MTJjLTE4MzktNGVlMC1iMzYzLWE2M2U4ZTQ4ODY3MgBGAAAAAACrnKBohTrQTr55rMZpUsUQBwDlQ7zKWTCjQI2f2wNCI4+cAAKz0gA7AADlQ7zKWTCjQI2f2wNCI4+cAAK02BoKAAA=");
      System.out.println("Receive Mail: " + msgIdList);

      receiver.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
