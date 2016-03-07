package com.main;

import com.java.mail.ReceiveMail;
import com.java.mail.impl.ReceiveMailFactory;

public class DeleteAttachment {
  public static void main(String args[]) throws Exception {
    String path = "C:\\Users\\zhontao\\AppData\\Local\\Temp\\TODO.4826e03f-d683-4caa-ace3-c145ba046a3a.txt";
    ReceiveMail receive = new ReceiveMailFactory().getInstance("Exchange Server");
    receive.deleteAttachments(path);
  }
}
