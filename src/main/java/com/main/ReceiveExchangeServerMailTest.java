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
      paramMap.put("port", "995");
      paramMap.put("proxyHost", null);
      paramMap.put("proxyPort", null);
      paramMap.put("protocol", "pop3s");
      // String username = "18616680719@163.com";
      // String password = "wxj824188950";
      // String username = "cainzhong@163.com";
      // String password = "Cisco520";
      String username = "cainzhong@aliyun.com";
      String password = "Cainzhong520";
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

      // { "msgId": "msgId", "folder": "folder name" }
      Map<String, Object> moveMsgMap = new HashMap<String, Object>();
      String msgId = "<185ac3dc-ab05-4133-a1f5-8bc3db6997fd@xtinp1mta402.xt.local>";
      String folder = "Deleted Items";
      moveMsgMap.put("msgId", msgId);
      moveMsgMap.put("folder", folder);
      JSONArray moveMsgJsonArray = JSONArray.fromObject(moveMsgMap);

      String moveMsgJson = moveMsgJsonArray.toString().substring(1, moveMsgJsonArray.toString().length() - 1);

      // receiver.moveMessage(moveMsgJson);
      // receiver.deleteMessage(msgId);
      System.out.println("Moved");

      receiver.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
