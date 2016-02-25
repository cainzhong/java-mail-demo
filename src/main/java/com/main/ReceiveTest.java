/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.main;

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
    try {
      String sourceFolderName = "SmartEmail";
      String toFolderName = "Deleted Items";

      Map<String, Object> paramMap = new HashMap<String, Object>();
      // paramMap.put("host", "192.168.220.130");
      // paramMap.put("host", "15.107.4.68");
      // paramMap.put("port", "995");
      // paramMap.put("auth", null);
      // paramMap.put("protocol", "imaps");
      // paramMap.put("username", "cainzhong");
      // paramMap.put("password", "Cisco01!");
      /* EWS */
      paramMap.put("protocol", "ews");
      paramMap.put("username", "cainzhong@cainzhong.win");
      paramMap.put("password", "Cisco01!");
      paramMap.put("uri", "https://outlook.office365.com/EWS/Exchange.asmx");
      /* EWS */
      paramMap.put("maxMailQuantity", 10);
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
      // paramMap.put("fromStringTerm", fromStringTerm);
      // paramMap.put("subjectTerm", subjectTerm);
      paramMap.put("sourceFolderName", sourceFolderName);
      paramMap.put("toFolderName", toFolderName);
      JSONArray paramJsonArray = JSONArray.fromObject(paramMap);

      String paramJson = paramJsonArray.toString().substring(1, paramJsonArray.toString().length() - 1);
      ReceiveMail receive = new ReceiveMailImpl();
      receive.initialize(paramJson);
      String result = "";
      String protocol = (String) paramMap.get("protocol");
      if (protocol.equalsIgnoreCase("ews")) {
        // result = receive.receiveViaEWS(null, false).toString();
        String noAttachment = "AAMkAGU4YmFmZDg3LWJmNzktNGFhYS05OWRmLWZmOGI4ZDc0MTRiMgBGAAAAAAB6fkMyFWApRKo+YfET7+zPBwCb6dlb7F5ESoU4htHysAseAAAJnr32AACb6dlb7F5ESoU4htHysAseAAAXk7YZAAA=";
        String size170kb = "AAMkAGU4YmFmZDg3LWJmNzktNGFhYS05OWRmLWZmOGI4ZDc0MTRiMgBGAAAAAAB6fkMyFWApRKo+YfET7+zPBwCb6dlb7F5ESoU4htHysAseAAAJnr32AACb6dlb7F5ESoU4htHysAseAAAXk7YaAAA=";
        String size398kb = "AAMkAGU4YmFmZDg3LWJmNzktNGFhYS05OWRmLWZmOGI4ZDc0MTRiMgBGAAAAAAB6fkMyFWApRKo+YfET7+zPBwCb6dlb7F5ESoU4htHysAseAAAJnr32AACb6dlb7F5ESoU4htHysAseAAAXk7YcAAA=";
        String size596kb = "AAMkAGU4YmFmZDg3LWJmNzktNGFhYS05OWRmLWZmOGI4ZDc0MTRiMgBGAAAAAAB6fkMyFWApRKo+YfET7+zPBwCb6dlb7F5ESoU4htHysAseAAAJnr32AACb6dlb7F5ESoU4htHysAseAAAXk7YbAAA=";
        String size912kb = "AAMkAGU4YmFmZDg3LWJmNzktNGFhYS05OWRmLWZmOGI4ZDc0MTRiMgBGAAAAAAB6fkMyFWApRKo+YfET7+zPBwCb6dlb7F5ESoU4htHysAseAAAJnr32AACb6dlb7F5ESoU4htHysAseAAAXk7YdAAA=";
        String size298MB = "AAMkAGU4YmFmZDg3LWJmNzktNGFhYS05OWRmLWZmOGI4ZDc0MTRiMgBGAAAAAAB6fkMyFWApRKo+YfET7+zPBwCb6dlb7F5ESoU4htHysAseAAAJnr32AACb6dlb7F5ESoU4htHysAseAAAXk7YeAAA=";

        System.out.println("Simple Mail without attachment");
        result = receive.receiveAttachment("ews", noAttachment).toString();
        System.out.println("Simple Mail with one 170kb attachment");
        String result1 = receive.receiveAttachment("ews", size170kb).toString();
        System.out.println("Simple Mail with one 398kb attachment");
        String result2 = receive.receiveAttachment("ews", size398kb).toString();
        System.out.println("Simple Mail with one 596kb attachment");
        String result3 = receive.receiveAttachment("ews", size596kb).toString();
        System.out.println("Simple Mail with one 912kb attachment");
        String result4 = receive.receiveAttachment("ews", size912kb).toString();
        System.out.println("Simple Mail with one 2.98MB attachment");
        String result5 = receive.receiveAttachment("ews", size298MB).toString();
        System.out.println(result);
        System.out.println(result1);
        System.out.println(result2);
        System.out.println(result3);
        System.out.println(result4);
        System.out.println(result5);

      } else if (protocol.equalsIgnoreCase("pop3") || protocol.equalsIgnoreCase("pop3s") || protocol.equalsIgnoreCase("imap") || protocol.equalsIgnoreCase("imaps")) {
        receive.open();
        result = receive.receive(null, false).toString();
        // String noAttachment = "<c81d36f9f4c748f4b0849cac91936d76@WIN-Q02NNP5BEJS.rainy.com>";
        // String size170kb = "<6987adf05bc54680bc851e1fc66d6740@WIN-Q02NNP5BEJS.rainy.com>";
        // String size398kb = "<9d51edf407d14c9687c53f532d64fd70@WIN-Q02NNP5BEJS.rainy.com>";
        // String size596kb = "<4b4c5cbb24914e2dab245eb05244fb73@WIN-Q02NNP5BEJS.rainy.com>";
        // String size912kb = "<6da2844e5aa34e09a5cce09eacc0f393@WIN-Q02NNP5BEJS.rainy.com>";
        // String size298MB = "<1db88187030f46fe9767779051d57395@WIN-Q02NNP5BEJS.rainy.com>";
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
        System.out.println(result);
        // System.out.println(result1);
        // System.out.println(result2);
        // System.out.println(result3);
        // System.out.println(result4);
        // System.out.println(result5);

        receive.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
