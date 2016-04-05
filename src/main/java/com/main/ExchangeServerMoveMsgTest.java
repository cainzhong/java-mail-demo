package com.main;

import java.util.HashMap;
import java.util.Map;

import com.java.mail.MailReceiver;
import com.java.mail.MailReceiverFactory;
import com.java.mail.impl.MailReceiverFactoryImpl;

import net.sf.json.JSONArray;

public class ExchangeServerMoveMsgTest {
  public static void main(String args[]) throws Exception {
    Map<String, Object> paramMap = new HashMap<String, Object>();
    String host = "imap.aliyun.com";
    paramMap.put("host", host);
    paramMap.put("port", "143");
    paramMap.put("protocol", "imap");
    String username = "cainzhong@aliyun.com";
    String password = "Cainzhong520";
    paramMap.put("username", username);
    paramMap.put("password", password);
    paramMap.put("mailSizeCheck", false);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    MailReceiverFactory factory = MailReceiverFactoryImpl.getInstance();
    MailReceiver receiver = factory.create("Exchange Server");
    System.out.println(paramJson);
    receiver.open(paramJson);
    String msgIdList = receiver.getMsgIdList("01/26/2016 09:00:00").toString();
    System.out.println("Message ID List: " + msgIdList);
    //
    // // { "msgId": "msgId", "folder": "folder name" }
    // Map<String, Object> moveMsgMap = new HashMap<String, Object>();
    // String msgId = "imap.163.com";
    // String folder = "Deleted Items";
    // paramMap.put("msgId", msgId);
    // paramMap.put("folder", folder);
    // JSONArray moveMsgJsonArray = JSONArray.fromObject(moveMsgMap);
    //
    // String moveMsgJson = paramJsonArray.toString().substring(1, moveMsgJsonArray.toString().length() - 1);
    //
    // receiver.moveMessage(moveMsgJson);
    // System.out.println("Moved");
  }
}
