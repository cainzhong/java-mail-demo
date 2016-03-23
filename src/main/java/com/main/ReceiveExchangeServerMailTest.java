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
      String sourceFolderName = "SmartEmail";
      String toFolderName = "Deleted Items";

      Map<String, Object> paramMap = new HashMap<String, Object>();
      String host = "16.187.191.13";
      // host = "192.168.244.128";
      // host = "webmail.hp.com";
      paramMap.put("host", host);
      paramMap.put("port", "143");
      paramMap.put("proxyHost", null);
      paramMap.put("proxyPort", null);
      paramMap.put("protocol", "imap");
      // paramMap.put("username", "tao.zhong@hpe.com");
      // paramMap.put("password", "Cisco03#");
      paramMap.put("username", "cainzhong@bing.com");
      paramMap.put("password", "Cisco01!");
      // paramMap.put("username", "tao.zhong@rainy.com");
      // paramMap.put("password", "Cisco01!");
      paramMap.put("maxMailSize", 30 * 1024 * 1024);
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
      String msgIdList = receiver.getMsgIdList("01/26/2016").toString();
      System.out.println("Message ID List: " + msgIdList);

      // Receive mail from mail server and save them in the disk.
      String result = "";

      // System.out.println("Simple mail with 2 attachments from bing.com");
      // String b1 = "<3ec3716e12b44bb6b7c5217c484df6bf@SGDLITVM0768.bing.com>";
      // result = receiver.receive(b1);
      // System.out.println(result);

      // System.out.println("Simple Mail without attachment");
      // String a1 = "<5CAF9A738A54854FB156427742303E8504A831@G9W0749.americas.hpqcorp.net>";
      // result = receiver.receive(a1);
      // System.out.println(result);
      //
      // System.out.println("Simple Mail with one 912 kb attachment");
      // String a2 = "<5CAF9A738A54854FB156427742303E8501FA03@G4W3303.americas.hpqcorp.net>";
      // result = receiver.receive(a2).toString();
      // System.out.println(result);
      //
      // System.out.println("Simple Mail with 1 embedded picture and 1 attachment");
      // String a3 = "<5CAF9A738A54854FB156427742303E8504A869@G9W0749.americas.hpqcorp.net>";
      // result = receiver.receive(a3).toString();
      // System.out.println(result);
      //
      // System.out.println("Simple Mail with 2 embedded picture and 2 attachments");
      // String a4 = "<5CAF9A738A54854FB156427742303E8504A88F@G9W0749.americas.hpqcorp.net>";
      // result = receiver.receive(a4).toString();
      // System.out.println(result);
      //
      // System.out.println("Simple Mail with 2 attachments");
      // String a5 = "<5CAF9A738A54854FB156427742303E8504A92F@G9W0749.americas.hpqcorp.net>";
      // result = receiver.receive(a5).toString();
      // System.out.println(result);
      //
      // System.out.println("Signed Mail without attachment");
      // String a6 = "<5CAF9A738A54854FB156427742303E8501FA34@G4W3303.americas.hpqcorp.net>";
      // result = receiver.receive(a6).toString();
      // System.out.println(result);
      //
      // System.out.println("Signed Mail with one 2.98 MB attachment");
      // String a7 = "<5CAF9A738A54854FB156427742303E85044714@G9W0749.americas.hpqcorp.net>";
      // result = receiver.receive(a7).toString();
      // System.out.println(result);
      //
      // System.out.println("Signed Mail with 1 embedded picture and 1 attachment");
      // String a8 = "<5CAF9A738A54854FB156427742303E85044731@G9W0749.americas.hpqcorp.net>";
      // result = receiver.receive(a8).toString();
      // System.out.println(result);
      //
      // System.out.println("Signed Mail with 2 embedded picture and 2 attachments");
      // String a9 = "<5CAF9A738A54854FB156427742303E85044781@G9W0749.americas.hpqcorp.net>";
      // result = receiver.receive(a9).toString();
      // System.out.println(result);
      //
      // System.out.println("Signed Mail with 2 attachments");
      // String a10 = "<5CAF9A738A54854FB156427742303E850446F4@G9W0749.americas.hpqcorp.net>";
      // result = receiver.receive(a10).toString();
      // System.out.println(result);
      //
      // System.out.println("Auto Replay Test");
      // String a11 = "<e9659ef198fa49f99b7eac28363678fb@G9W3614.americas.hpqcorp.net>";
      // result = receiver.receive(a11).toString();
      // System.out.println(result);

      // Read message
      System.out.println("Simple Mail without attachment");
      String a1 = "C:/Users/zhontao/AppData/Local/Temp//temp/b8f3519d-988c-4dd2-af2c-cb5fded81e6b.eml";
      result = receiver.readMessage(a1).toString();
      System.out.println(result);
      //
      // System.out.println("Simple Mail with one 912 kb attachment");
      // String a2 = "C:/Users/zhontao/AppData/Local/Temp//temp/9d0eccb4-30bd-48d8-b957-280b17a7e4c4.eml";
      // result = receiver.readAttachments(a2).toString();
      // System.out.println(result);
      //
      // System.out.println("Simple Mail with 1 embedded picture and 1 attachment");
      // String a3 = "C:/Users/zhontao/AppData/Local/Temp//temp/a0ce0dd3-fcad-4147-95c8-fe99d97409f4.eml";
      // result = receiver.readAttachments(a3).toString();
      // System.out.println(result);
      //
      // System.out.println("Simple Mail with 2 embedded picture and 2 attachments");
      // String a4 = "C:/Users/zhontao/AppData/Local/Temp//temp/d5abe48b-30bc-42be-8889-5b95df387b00.eml";
      // result = receiver.readAttachments(a4).toString();
      // System.out.println(result);
      //
      // System.out.println("Simple Mail with 2 attachments");
      // String a5 = "C:/Users/zhontao/AppData/Local/Temp//temp/201ef8d6-adbf-4282-8ac2-d8b16a5ccd9a.eml";
      // result = receiver.readAttachments(a5).toString();
      // System.out.println(result);
      //
      // System.out.println("Signed Mail without attachment");
      // String a6 = "C:/Users/zhontao/AppData/Local/Temp//temp/63cbdf9e-320c-4707-b08d-46c1cea4493c.eml";
      // result = receiver.readAttachments(a6).toString();
      // System.out.println(result);
      //
      // System.out.println("Signed Mail with one 2.98 MB attachment");
      // String a7 = "C:/Users/zhontao/AppData/Local/Temp//temp/5b6c8380-b70f-4e9e-8fcf-995a4436a67f.eml";
      // result = receiver.readAttachments(a7).toString();
      // System.out.println(result);
      //
      // System.out.println("Signed Mail with 1 embedded picture and 1 attachment");
      // String a8 = "C:/Users/zhontao/AppData/Local/Temp//temp/38f747fd-55e9-486f-8cf2-2d9e09b45da2.eml";
      // result = receiver.readAttachments(a8).toString();
      // System.out.println(result);
      //
      // System.out.println("Signed Mail with 2 embedded picture and 2 attachments");
      // String a9 = "C:/Users/zhontao/AppData/Local/Temp//temp/b963f455-01e2-4161-a288-0b476ccf8c51.eml";
      // result = receiver.readAttachments(a9).toString();
      // System.out.println(result);
      //
      // System.out.println("Signed Mail with 2 attachments");
      // String a10 = "C:/Users/zhontao/AppData/Local/Temp//temp/8731081e-0b95-4445-b96a-28e0c410bfb8.eml";
      // result = receiver.readAttachments(a10).toString();
      // System.out.println(result);
      //
      // System.out.println("Auto Replay Test");
      // String a11 = "C:/Users/zhontao/AppData/Local/Temp//temp/0d1a94cb-4d9f-49eb-be57-bf8e7225b869.eml";
      // result = receiver.readAttachments(a11).toString();
      // System.out.println(result);

      receiver.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
