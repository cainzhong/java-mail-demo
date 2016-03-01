/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.main;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.java.mail.ReceiveMail;
import com.java.mail.impl.ReceiveMailImpl;

import net.sf.json.JSONArray;

/**
 * @author zhontao
 *
 */
public class ReceiveTest {

  public static void main(String args[]) {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    try {
      String sourceFolderName = "SmartEmail";
      String toFolderName = "Deleted Items";
      String feiHost = "15.107.4.68";
      String myVMHost = "192.168.220.130";

      Map<String, Object> paramMap = new HashMap<String, Object>();
      paramMap.put("host", "webmail.hp.com");
      paramMap.put("port", "995");
      paramMap.put("protocol", "imaps");
      paramMap.put("username", "tao.zhong@hpe.com");
      paramMap.put("password", "Cisco03#");
      /* EWS */
      // paramMap.put("protocol", "ews");
      // paramMap.put("username", "cainzhong@cainzhong.win");
      // paramMap.put("password", "Cisco01!");
      // paramMap.put("uri", "https://outlook.office365.com/EWS/Exchange.asmx");
      /* EWS */
      paramMap.put("maxMailQuantity", 100);
      List<String> suffixList = new ArrayList<String>();
      suffixList.add("css");
      suffixList.add("png");
      suffixList.add("jpg");
      suffixList.add("txt");
      // suffixList.add("p7m");
      paramMap.put("suffixList", suffixList);
      List<String> authorisedUserList = new ArrayList<String>();
      authorisedUserList.add("@rainy.com");
      authorisedUserList.add("@hpe.com");
      authorisedUserList.add("@cainzhong.win");
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
      ReceiveMail receive = new ReceiveMailImpl();
      receive.initialize(paramJson);
      receive.open();
      String result = "";
      String msgIdList = receive.getMsgIdList().toString();
      System.out.println("Message ID List: " + msgIdList);
      String noAttachment = "<5CAF9A738A54854FB156427742303E8501FA34@G4W3303.americas.hpqcorp.net>";
      String size298MB = "<5CAF9A738A54854FB156427742303E8501FA19@G4W3303.americas.hpqcorp.net>";

      System.out.println("Signed Mail without attachment");
      result = receive.receiveAttachment(noAttachment).toString();
      System.out.println(result);
      System.out.println("Simple Mail with one 2.98MB attachment");
      result = receive.receiveAttachment(size298MB).toString();
      System.out.println(result);
      receive.close();

      // https://15.107.4.68/owa
      // String noAttachment = "<b2378546905c47ddb76006ccdaad2dbd@WIN-1M8HSSSE95N.smtest.com>";
      // String size170kb = "<95a6bc3307d3424e84474d98b2e6adca@WIN-1M8HSSSE95N.smtest.com>";
      // String size398kb = "<d33b4923b59247f2a8b000034debf85e@WIN-1M8HSSSE95N.smtest.com>";
      // String size596kb = "<081d9e70a58e4a939c97f93f2bebc75f@WIN-1M8HSSSE95N.smtest.com>";
      // String size912kb = "<c1662f4abc6f47a198e574c48ac875ab@WIN-1M8HSSSE95N.smtest.com>";
      // String size298MB = "<504e6e53a80f4f32b907c54c1216c2e6@WIN-1M8HSSSE95N.smtest.com>";
      // System.out.println("Simple Mail without attachment");
      // result = receive.receiveAttachment("imaps", noAttachment).toString();
      // System.out.println("Simple Mail with one 170kb attachment");
      // String result1 = receive.receiveAttachment("imaps", size170kb).toString();
      // System.out.println("Simple Mail with one 398kb attachment");
      // String result2 = receive.receiveAttachment("imaps", size398kb).toString();
      // System.out.println("Simple Mail with one 596kb attachment");
      // String result3 = receive.receiveAttachment("imaps", size596kb).toString();
      // System.out.println("Simple Mail with one 912kb attachment");
      // String result4 = receive.receiveAttachment("imaps", size912kb).toString();
      // System.out.println("Simple Mail with one 2.98MB attachment");
      // String result5 = receive.receiveAttachment("imaps", size298MB).toString();
      // System.out.println(result);
      // System.out.println(result1);
      // System.out.println(result2);
      // System.out.println(result3);
      // System.out.println(result4);
      // System.out.println(result5);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
