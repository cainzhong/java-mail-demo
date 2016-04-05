package com.main;

import java.util.HashMap;
import java.util.Map;

import com.java.mail.MailReceiver;
import com.java.mail.MailReceiverFactory;
import com.java.mail.impl.MailReceiverFactoryImpl;

import net.sf.json.JSONArray;

public class ExchangeServerDeleteMsgTest {
  public static void main(String args[]) throws Exception {
    Map<String, Object> paramMap = new HashMap<String, Object>();
    String host = "imap.163.com";
    paramMap.put("host", host);
    paramMap.put("port", "110");
    paramMap.put("protocol", "imap");
    String username = "18616680719@163.com";
    String password = "wxj824188950";
    paramMap.put("username", username);
    paramMap.put("password", password);
    paramMap.put("mailSizeCheck", false);
    JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

    String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);

    MailReceiverFactory factory = MailReceiverFactoryImpl.getInstance();
    MailReceiver receiver = factory.create("Exchange Server");
    System.out.println(paramJson);
    receiver.open(paramJson);

    String msgId = "<CS1PR84MB0215EA5A0A985C88D7C66817F19A0@CS1PR84MB0215.NAMPRD84.PROD.OUTLOOK.COM>";
    receiver.deleteMessage(msgId);
    System.out.println("Deleted");
  }
}
