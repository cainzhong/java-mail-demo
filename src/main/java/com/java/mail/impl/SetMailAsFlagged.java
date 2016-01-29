package com.java.mail.impl;

import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

public class SetMailAsFlagged {
  public static void main(String args[]) throws MessagingException {
    String protocol ="imaps";
    String folderName = "SmartEmail";
    String host="webmail.hp.com";
    String username = "tao.zhong@hpe.com";
    String password="Cisco01!";

    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);
    Store store = session.getStore(protocol);
    store.connect(host, username, password);

    Folder folder = store.getFolder(folderName);
    folder.open( Folder.READ_WRITE);
    Message[] msgs = folder.getMessages();
    for (Message msg : msgs) {
      msg.setFlag(Flags.Flag.FLAGGED, true);
    }
    System.out.println(msgs.length + " mails have been set as flagged.");
  }
}