/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.java.mail.ReceiveMail;

import net.sf.json.JSONArray;

/**
 * @author zhontao
 *
 */
public class ReceiveTest {

  public static void main(String args[]) {
    try {
      String fromStringTerm = "tao.zhong@hpe.com";
      // String subjectTerm = "Simple Mail without attachment";
      // String subjectTerm = "Simple Mail With 2 attachments";
      // String subjectTerm = "Sign Mail without attachment";
      // String subjectTerm = "Sign Mail with 2 attachments";
      // String subjectTerm = "Multi Mail with 2 attachments";
      // String subjectTerm = "Multi Mail";
      // String subjectTerm = "27M attachments";
      String subjectTerm = "Multi-Thread Mail Test";

      String sourceFolderName = "SmartEmail";
      String toFolderName = "Deleted Items";

      Map<String, Object> paramMap = new HashMap<String, Object>();
      paramMap.put("host", "webmail.hp.com");
      paramMap.put("port", "993");
      paramMap.put("auth", null);
      // paramMap.put("protocol", "imaps");
      // paramMap.put("username", "tao.zhong@hpe.com");
      // paramMap.put("password", "Cisco01!");
      /* EWS */
      paramMap.put("protocol", "ews");
      paramMap.put("username", "cainzhong@cainzhong.win");
      paramMap.put("password", "Cisco01!");
      paramMap.put("uri", "https://outlook.office365.com/EWS/Exchange.asmx");
      paramMap.put("maxMailQuantity", 100);
      List<String> suffixList = new ArrayList<String>();
      suffixList.add("css");
      suffixList.add("png");
      suffixList.add("jpg");
      suffixList.add("txt");
      suffixList.add("p7m");
      paramMap.put("suffixList", suffixList);
      List<String> authorisedUserList = new ArrayList<String>();
      authorisedUserList.add("@hpe.com");
      authorisedUserList.add("@cainzhong.win");
      paramMap.put("authorisedUserList", authorisedUserList);
      paramMap.put("proxySet", false);
      paramMap.put("proxyHost", "");
      paramMap.put("proxyPort", "");
      paramMap.put("proxyUser", "");
      paramMap.put("proxyPassword", "");
      paramMap.put("proxyDomain", "");
      // paramMap.put("fromStringTerm", fromStringTerm);
      // paramMap.put("subjectTerm", subjectTerm);
      paramMap.put("sourceFolderName", sourceFolderName);
      paramMap.put("toFolderName", toFolderName);
      JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

      String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);
      ReceiveMail receive = new ReceiveMailImpl();
      receive.initialize(paramJson);
      String result = "";
      if (paramMap.get("protocol").toString().equalsIgnoreCase("ews")) {
        String subjectTermEWS = "Signed Mail with 2 attachments.";
        // String subjectTermEWS = "Simple Mail with 2 attachments.";
        // String subjectTermEWS = "Simple Mail without attachments.";
        result = receive.receiveThroughEWS(fromStringTerm, subjectTermEWS, 1).toString();
        System.out.println(result);
      } else {
        receive.open();
        result = receive.receive(fromStringTerm, subjectTerm).toString();
        System.out.println(result);

        // System.out.print("Simple Mail with one 170kb attachment: ");
        // begin = System.currentTimeMillis();
        // receive.receive(fromStringTerm, "Simple Mail with one 170kb attachment");
        // end = System.currentTimeMillis();
        // System.out.println((end - begin) / 1000 + " seconds.");
        //
        // System.out.print("Simple Mail with one 398kb attachment: ");
        // begin = System.currentTimeMillis();
        // receive.receive(fromStringTerm, "Simple Mail with one 398kb attachment");
        // end = System.currentTimeMillis();
        // System.out.println((end - begin) / 1000 + " seconds.");
        //
        // System.out.print("Simple Mail with one 596kb attachment: ");
        // begin = System.currentTimeMillis();
        // receive.receive(fromStringTerm, "Simple Mail with one 596kb attachment");
        // end = System.currentTimeMillis();
        // System.out.println((end - begin) / 1000 + " seconds.");
        //
        // System.out.print("Simple Mail with one 912kb attachment: ");
        // begin = System.currentTimeMillis();
        // receive.receive(fromStringTerm, "Simple Mail with one 912kb attachment");
        // end = System.currentTimeMillis();
        // System.out.println((end - begin) / 1000 + " seconds.");
        //
        // System.out.print("Simple Mail with one 2.98MB attachment: ");
        // begin = System.currentTimeMillis();
        // receive.receive(fromStringTerm, "Simple Mail with one 2.98MB attachment");
        // end = System.currentTimeMillis();
        // System.out.println((end - begin) / 1000 + " seconds.");
        //
        // System.out.print("Signed Mail without attachment: ");
        // begin = System.currentTimeMillis();
        // receive.receive(fromStringTerm, "Signed Mail without attachment");
        // end = System.currentTimeMillis();
        // System.out.println((end - begin) / 1000 + " seconds.");
        //
        // System.out.print("Signed Mail with one 170kb attachment: ");
        // begin = System.currentTimeMillis();
        // receive.receive(fromStringTerm, "Signed Mail with one 170kb attachment");
        // end = System.currentTimeMillis();
        // System.out.println((end - begin) / 1000 + " seconds.");
        //
        // System.out.print("Signed Mail with one 398kb attachment: ");
        // begin = System.currentTimeMillis();
        // receive.receive(fromStringTerm, "Signed Mail with one 398kb attachment");
        // end = System.currentTimeMillis();
        // System.out.println((end - begin) / 1000 + " seconds.");
        //
        // System.out.print("Signed Mail with one 596kb attachment: ");
        // begin = System.currentTimeMillis();
        // receive.receive(fromStringTerm, "Signed Mail with one 596kb attachment");
        // end = System.currentTimeMillis();
        // System.out.println((end - begin) / 1000 + " seconds.");
        //
        // System.out.print("Signed Mail with one 912kb attachment: ");
        // begin = System.currentTimeMillis();
        // receive.receive(fromStringTerm, "Signed Mail with one 912kb attachment");
        // end = System.currentTimeMillis();
        // System.out.println((end - begin) / 1000 + " seconds.");
        //
        // System.out.print("Signed Mail with one 2.98M attachment: ");
        // begin = System.currentTimeMillis();
        // receive.receive(fromStringTerm, "Signed Mail with one 2.98M attachment");
        // end = System.currentTimeMillis();
        // System.out.println((end - begin) / 1000 + " seconds.");

        receive.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
