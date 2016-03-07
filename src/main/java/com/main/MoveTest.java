package com.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.java.mail.MailReceiver;
import com.java.mail.impl.MailReceiverFactory;

import net.sf.json.JSONArray;

public class MoveTest {
  public static void main(String args[]) throws Exception {
    // String sourceFolderName = "SmartEmail";
    String sourceFolderName = "收件箱";
    String toFolderName = "Deleted Itemsssss";

    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put("host", "webmail.hp.com");
    paramMap.put("port", "993");
    paramMap.put("auth", null);
    // paramMap.put("protocol", "imaps");
    // paramMap.put("username", "tao.zhong@hpe.com");
    paramMap.put("password", "Cisco01!");
    /* EWS */
    paramMap.put("protocol", "ews");
    paramMap.put("username", "cainzhong@cainzhong.win");
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
    MailReceiver receive = new MailReceiverFactory().getInstance("EWS");
    receive.initialize(paramJson);
    // receive.open();
    // String messageId =
    // "<5CAF9A738A54854FB156427742303E85020DF9@G4W3303.americas.hpqcorp.net>";
    String messageId = "AAMkAGU4YmFmZDg3LWJmNzktNGFhYS05OWRmLWZmOGI4ZDc0MTRiMgBGAAAAAAB6fkMyFWApRKo+YfET7+zPBwCb6dlb7F5ESoU4htHysAseAAAAAAEMAACb6dlb7F5ESoU4htHysAseAAAIQ0HqAAA=";
  }
}
