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
public class ReceiveEWSMailTest {

  public static void main(String args[]) {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    try {
      String sourceFolderName = "SmartEmail";
      String toFolderName = "Deleted Items";

      Map<String, Object> paramMap = new HashMap<String, Object>();
      paramMap.put("protocol", "ews");
      paramMap.put("username", "tao.zhong1@pactera.com");
      paramMap.put("password", "Cisco01!");
      paramMap.put("uri", "https://outlook.office365.com/EWS/Exchange.asmx");
      /* EWS */
      paramMap.put("maxMailQuantity", 100);
      List<String> suffixList = new ArrayList<String>();
      suffixList.add("css");
      suffixList.add("png");
      suffixList.add("jpg");
      suffixList.add("txt");
      paramMap.put("suffixList", suffixList);
      List<String> authorisedUserList = new ArrayList<String>();
      authorisedUserList.add("@rainy.com");
      authorisedUserList.add("@hpe.com");
      authorisedUserList.add("@cainzhong.win");
      authorisedUserList.add("@pactera.com");
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
      MailReceiverFactory factory = MailReceiverFactoryImpl.getInstance();
      MailReceiver receiver = factory.create("EWS");
      receiver.open(paramJson);
      String result = "";
      // System.out.println("Get All message id.");
      // String msgIdList = receiver.getMsgIdList("03/03/2016").toString();
      // System.out.println("Message ID List: " + msgIdList);

      String a1 = "AAMkAGQ3ZTFkMzNiLWZlMjQtNDc5Mi1iYWE4LWJlZDBlYWI4NzZkOABGAAAAAACvYXplambMRroXkSScSrxlBwC+v4q/kn/NQqjU3NR5Sn1UAABGtkcnAAC+v4q/kn/NQqjU3NR5Sn1UAABGvWZxAAA=";
      String a2 = "AAMkAGQ3ZTFkMzNiLWZlMjQtNDc5Mi1iYWE4LWJlZDBlYWI4NzZkOABGAAAAAACvYXplambMRroXkSScSrxlBwC+v4q/kn/NQqjU3NR5Sn1UAABGtkcnAAC+v4q/kn/NQqjU3NR5Sn1UAABGvWZwAAA=";
      String a3 = "AAMkAGQ3ZTFkMzNiLWZlMjQtNDc5Mi1iYWE4LWJlZDBlYWI4NzZkOABGAAAAAACvYXplambMRroXkSScSrxlBwC+v4q/kn/NQqjU3NR5Sn1UAABGtkcnAAC+v4q/kn/NQqjU3NR5Sn1UAABGvWZuAAA=";
      String a4 = "AAMkAGQ3ZTFkMzNiLWZlMjQtNDc5Mi1iYWE4LWJlZDBlYWI4NzZkOABGAAAAAACvYXplambMRroXkSScSrxlBwC+v4q/kn/NQqjU3NR5Sn1UAABGtkcnAAC+v4q/kn/NQqjU3NR5Sn1UAABGvWZtAAA=";
      String a5 = "AAMkAGQ3ZTFkMzNiLWZlMjQtNDc5Mi1iYWE4LWJlZDBlYWI4NzZkOABGAAAAAACvYXplambMRroXkSScSrxlBwC+v4q/kn/NQqjU3NR5Sn1UAABGtkcnAAC+v4q/kn/NQqjU3NR5Sn1UAABGvWZvAAA=";
      String a6 = "AAMkAGQ3ZTFkMzNiLWZlMjQtNDc5Mi1iYWE4LWJlZDBlYWI4NzZkOABGAAAAAACvYXplambMRroXkSScSrxlBwC+v4q/kn/NQqjU3NR5Sn1UAABGtkcnAAC+v4q/kn/NQqjU3NR5Sn1UAABGvWZsAAA=";
      String a7 = "AAMkAGQ3ZTFkMzNiLWZlMjQtNDc5Mi1iYWE4LWJlZDBlYWI4NzZkOABGAAAAAACvYXplambMRroXkSScSrxlBwC+v4q/kn/NQqjU3NR5Sn1UAABGtkcnAAC+v4q/kn/NQqjU3NR5Sn1UAABGvWZrAAA=";
      String a8 = "AAMkAGQ3ZTFkMzNiLWZlMjQtNDc5Mi1iYWE4LWJlZDBlYWI4NzZkOABGAAAAAACvYXplambMRroXkSScSrxlBwC+v4q/kn/NQqjU3NR5Sn1UAABGtkcnAAC+v4q/kn/NQqjU3NR5Sn1UAABGvWZqAAA=";
      String a9 = "AAMkAGQ3ZTFkMzNiLWZlMjQtNDc5Mi1iYWE4LWJlZDBlYWI4NzZkOABGAAAAAACvYXplambMRroXkSScSrxlBwC+v4q/kn/NQqjU3NR5Sn1UAABGtkcnAAC+v4q/kn/NQqjU3NR5Sn1UAABGvWZpAAA=";
      String a10 = "AAMkAGQ3ZTFkMzNiLWZlMjQtNDc5Mi1iYWE4LWJlZDBlYWI4NzZkOABGAAAAAACvYXplambMRroXkSScSrxlBwC+v4q/kn/NQqjU3NR5Sn1UAABGtkcnAAC+v4q/kn/NQqjU3NR5Sn1UAABGvWZoAAA=";
      String a11 = "AAMkAGQ3ZTFkMzNiLWZlMjQtNDc5Mi1iYWE4LWJlZDBlYWI4NzZkOABGAAAAAACvYXplambMRroXkSScSrxlBwC+v4q/kn/NQqjU3NR5Sn1UAABGtkcnAAC+v4q/kn/NQqjU3NR5Sn1UAABGvWZnAAA=";
      String a12 = "AAMkAGQ3ZTFkMzNiLWZlMjQtNDc5Mi1iYWE4LWJlZDBlYWI4NzZkOABGAAAAAACvYXplambMRroXkSScSrxlBwC+v4q/kn/NQqjU3NR5Sn1UAABGtkcnAAC+v4q/kn/NQqjU3NR5Sn1UAABGvWZmAAA=";
      String a13 = "AAMkAGQ3ZTFkMzNiLWZlMjQtNDc5Mi1iYWE4LWJlZDBlYWI4NzZkOABGAAAAAACvYXplambMRroXkSScSrxlBwC+v4q/kn/NQqjU3NR5Sn1UAABGtkcnAAC+v4q/kn/NQqjU3NR5Sn1UAABGtnIiAAA=";

      // Receive mail from mail server and save them in the disk.
      System.out.println("a1");
      result = receiver.receive(a1).toString();
      System.out.println(result);
      System.out.println("a2");
      result = receiver.receive(a2).toString();
      System.out.println(result);
      System.out.println("a3");
      result = receiver.receive(a3).toString();
      System.out.println(result);
      System.out.println("a4");
      result = receiver.receive(a4).toString();
      System.out.println(result);
      System.out.println("a5");
      result = receiver.receive(a5).toString();
      System.out.println(result);
      System.out.println("a6");
      result = receiver.receive(a6).toString();
      System.out.println(result);
      System.out.println("a7");
      result = receiver.receive(a7).toString();
      System.out.println(result);
      System.out.println("a8");
      result = receiver.receive(a8).toString();
      System.out.println(result);
      System.out.println("a9");
      result = receiver.receive(a9).toString();
      System.out.println(result);
      System.out.println("a10");
      result = receiver.receive(a10).toString();
      System.out.println(result);
      System.out.println("a11");
      result = receiver.receive(a11).toString();
      System.out.println(result);
      System.out.println("a12");
      result = receiver.receive(a12).toString();
      System.out.println(result);
      System.out.println("a13");
      result = receiver.receive(a13).toString();
      System.out.println(result);

      receiver.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
