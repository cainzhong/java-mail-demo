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
      paramMap.put("protocol", "imaps");
      paramMap.put("username", "tao.zhong@hpe.com");
      paramMap.put("password", "Cisco01!");
      /* EWS */
      // paramMap.put("protocol", "ews");
      // paramMap.put("username", "cainzhong@cainzhong.win");
      // paramMap.put("password", "Cisco01!");
      // paramMap.put("uri", "https://outlook.office365.com/EWS/Exchange.asmx");
      /* EWS */
      paramMap.put("maxMailQuantity", 1);
      List<String> suffixList = new ArrayList<String>();
      suffixList.add("css");
      suffixList.add("png");
      suffixList.add("jpg");
      suffixList.add("txt");
      // suffixList.add("p7m");
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
        result = receive.receiveThroughEWS(null, false).toString();
        System.out.println(result);
      } else {
        receive.open();
        String messageId = "<5CAF9A738A54854FB156427742303E85020DF9@G4W3303.americas.hpqcorp.net>";
        result = receive.receive(messageId, true).toString();
        System.out.println(result);

        receive.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
