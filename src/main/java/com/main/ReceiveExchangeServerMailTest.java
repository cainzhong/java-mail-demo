/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.main;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.java.mail.MailReceiver;
import com.java.mail.impl.MailReceiverFactory;
import com.java.mail.impl.MailReceiverFactoryImpl;

import net.sf.json.JSONArray;

/**
 * @author zhontao
 *
 */
public class ReceiveExchangeServerMailTest {

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
      authorisedUserList.add("@pactera.com");
      paramMap.put("authorisedUserList", authorisedUserList);
      paramMap.put("proxySet", false);
      paramMap.put("sourceFolderName", sourceFolderName);
      paramMap.put("toFolderName", toFolderName);
      JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

      String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);
      MailReceiverFactory factory = MailReceiverFactoryImpl.getInstance();
      MailReceiver receiver = factory.create("Exchange Server");
      System.out.println(paramJson);
      receiver.open(paramJson);
      String result = "";
      System.out.println("Get All message id.");
      String msgIdList = receiver.getMsgIdList("01/26/2016").toString();
      System.out.println("Message ID List: " + msgIdList);

      String a1 = "<5CAF9A738A54854FB156427742303E8501FA34@G4W3303.americas.hpqcorp.net>";
      String a2 = "<5CAF9A738A54854FB156427742303E8501FA19@G4W3303.americas.hpqcorp.net>";
      String a3 = "<5CAF9A738A54854FB156427742303E8501FA03@G4W3303.americas.hpqcorp.net>";
      String a4 = "<5CAF9A738A54854FB156427742303E8501F9E9@G4W3303.americas.hpqcorp.net>";
      String a5 = "<5CAF9A738A54854FB156427742303E8501F9D2@G4W3303.americas.hpqcorp.net>";
      String a6 = "<5CAF9A738A54854FB156427742303E8501F9B6@G4W3303.americas.hpqcorp.net>";
      String a7 = "<e9659ef198fa49f99b7eac28363678fb@G9W3614.americas.hpqcorp.net>";
      String a8 = "<5CAF9A738A54854FB156427742303E850446F4@G9W0749.americas.hpqcorp.net>";
      String a9 = "<5CAF9A738A54854FB156427742303E85044714@G9W0749.americas.hpqcorp.net>";
      String a10 = "<5CAF9A738A54854FB156427742303E85044731@G9W0749.americas.hpqcorp.net>";
      String a11 = "<5CAF9A738A54854FB156427742303E85044781@G9W0749.americas.hpqcorp.net>";

      // Receive mail from mail server and save them in the disk.
      // System.out.println("a1");
      // result = receiver.receive(a1).toString();
      // System.out.println(result);
      // System.out.println("a2");
      // result = receiver.receive(a2).toString();
      // System.out.println(result);
      // System.out.println("a3");
      // result = receiver.receive(a3).toString();
      // System.out.println(result);
      // System.out.println("a4");
      // result = receiver.receive(a4).toString();
      // System.out.println(result);
      // System.out.println("a5");
      // result = receiver.receive(a5).toString();
      // System.out.println(result);
      // System.out.println("a6");
      // result = receiver.receive(a6).toString();
      // System.out.println(result);
      // System.out.println("a7");
      // result = receiver.receive(a7).toString();
      // System.out.println(result);
      // System.out.println("a8");
      // result = receiver.receive(a8).toString();
      // System.out.println(result);
      // System.out.println("a9");
      // result = receiver.receive(a9).toString();
      // System.out.println(result);
      // System.out.println("a10");
      // result = receiver.receive(a10).toString();
      // System.out.println(result);
      // System.out.println("a11");
      // result = receiver.receive(a11).toString();
      // System.out.println(result);

      String path1 = "C:/Users/zhontao/AppData/Local/Temp/temp/6d1a7868-2e5c-4c83-a8c6-72181613c1db.eml";
      String path2 = "C:/Users/zhontao/AppData/Local/Temp/temp/026122fe-7fb2-44d9-9722-7cb14805f2f7.eml";
      String path3 = "C:/Users/zhontao/AppData/Local/Temp/temp/003ecb15-74df-4d45-b4ab-64f9fa21000f.eml";
      String path4 = "C:/Users/zhontao/AppData/Local/Temp/temp/142909e7-87b9-4aba-934e-d3fe9f7e680c.eml";
      String path5 = "C:/Users/zhontao/AppData/Local/Temp/temp/b6b98571-5f47-4345-bdf6-31ea8c446652.eml";
      String path6 = "C:/Users/zhontao/AppData/Local/Temp/temp/dae3d24b-ca43-4412-a2f7-c7420855fc53.eml";
      String path7 = "C:/Users/zhontao/AppData/Local/Temp/temp/cc6581c1-46cf-44b4-a142-609068669425.eml";
      String path8 = "C:/Users/zhontao/AppData/Local/Temp/temp/561ff113-505a-40db-afb1-d341e6dd3e09.eml";
      String path9 = "C:/Users/zhontao/AppData/Local/Temp/temp/b18d05d7-a86a-4301-b372-f8e87b32f40d.eml";
      String path10 = "C:/Users/zhontao/AppData/Local/Temp/temp/0c5a658a-9791-4e0a-a005-d0fa5c06b14f.eml";
      String path11 = "C:/Users/zhontao/AppData/Local/Temp/temp/dfe4986e-110f-4d47-b306-5ba66ef67032.eml";

      System.out.println("path1");
      result = receiver.readMessage(path1).toString();
      System.out.println(result);
      result = receiver.readAttachments(path1).toString();
      System.out.println(result);
      System.out.println("path2");
      result = receiver.readMessage(path2).toString();
      System.out.println(result);
      result = receiver.readAttachments(path2).toString();
      System.out.println(result);
      System.out.println("path3");
      result = receiver.readMessage(path3).toString();
      System.out.println(result);
      result = receiver.readAttachments(path3).toString();
      System.out.println(result);
      System.out.println("path4");
      result = receiver.readMessage(path4).toString();
      System.out.println(result);
      result = receiver.readAttachments(path4).toString();
      System.out.println(result);
      System.out.println("path5");
      result = receiver.readMessage(path5).toString();
      System.out.println(result);
      result = receiver.readAttachments(path5).toString();
      System.out.println(result);
      System.out.println("path6");
      result = receiver.readMessage(path6).toString();
      System.out.println(result);
      result = receiver.readAttachments(path6).toString();
      System.out.println(result);
      System.out.println("path7");
      result = receiver.readMessage(path7).toString();
      System.out.println(result);
      result = receiver.readAttachments(path7).toString();
      System.out.println(result);
      System.out.println("path8");
      result = receiver.readMessage(path8).toString();
      System.out.println(result);
      result = receiver.readAttachments(path8).toString();
      System.out.println(result);
      System.out.println("path9");
      result = receiver.readMessage(path9).toString();
      System.out.println(result);
      result = receiver.readAttachments(path9).toString();
      System.out.println(result);
      System.out.println("path10");
      result = receiver.readMessage(path10).toString();
      System.out.println(result);
      result = receiver.readAttachments(path10).toString();
      System.out.println(result);
      System.out.println("path11");
      result = receiver.readMessage(path11).toString();
      System.out.println(result);
      result = receiver.readAttachments(path11).toString();
      System.out.println(result);

      receiver.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
