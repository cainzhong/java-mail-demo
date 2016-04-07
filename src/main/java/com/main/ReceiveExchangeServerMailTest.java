/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.main;

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
public class ReceiveExchangeServerMailTest {

  public static void main(String args[]) {
    // Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    try {
      String sourceFolderName = "INBOX";
      String toFolderName = "Deleted Items";

      Map<String, Object> paramMap = new HashMap<String, Object>();
      String host = "imap-mail.outlook.com";
      host = "pop.163.com";
      paramMap.put("host", host);
      paramMap.put("port", "110");
      paramMap.put("proxyHost", null);
      paramMap.put("proxyPort", null);
      paramMap.put("protocol", "pop3");
      // String username = "18616680719@163.com";
      // String password = "wxj824188950";
      // String username = "cainzhong@163.com";
      // String password = "Cisco520";
      String username = "18971546556@163.com";
      String password = "89055465lixue";
      username = "cainzhong@outlook.com";
      password = "Cisco01!";
      username = "18616680719@163.com";
      password = "wxj824188950";
      paramMap.put("username", username);
      paramMap.put("password", password);
      paramMap.put("mailSizeCheck", false);
      paramMap.put("maxMailSize", 30);
      paramMap.put("sourceFolderName", sourceFolderName);
      paramMap.put("toFolderName", toFolderName);
      paramMap.put("errorFolder", null);
      JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

      String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

      MailReceiverFactory factory = MailReceiverFactoryImpl.getInstance();
      MailReceiver receiver = factory.create("Exchange Server");
      System.out.println(paramJson);
      receiver.open(paramJson);
      System.out.println("Get All message id.");
      String msgIdList = receiver.getMsgIdList("01/14/2016 09:00:00").toString();
      System.out.println("Message ID List: " + msgIdList);
      String msgId = "<tencent_144D1A265E880B4B6AFB439A@qq.com>";
      String mailMsg = receiver.receive(msgId);
      System.out.println(mailMsg);
      receiver.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
