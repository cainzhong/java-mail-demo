package com.main;

import com.java.mail.MailReceiver;
import com.java.mail.impl.MailReceiverFactory;

public class DeleteAttachment {
  public static void main(String args[]) throws Exception {
    String path = "C:\\Users\\zhontao\\AppData\\Local\\Temp\\TODO.4826e03f-d683-4caa-ace3-c145ba046a3a.txt";
    MailReceiver receive = MailReceiverFactory.getInstance("Exchange Server");
    receive.deleteFile(path);
  }
}
