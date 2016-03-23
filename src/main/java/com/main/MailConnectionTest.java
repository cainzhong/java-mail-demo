package com.main;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

public class MailConnectionTest {

  public static void main(String args[]) throws MessagingException {
    // System.setProperty("javax.net.ssl.trustStore", "C:\\Program Files\\Java\\jdk1.7.0_79\\jre\\lib\\security\\cacerts");
    Properties props = System.getProperties();
    Session session = Session.getDefaultInstance(props, null);
    String protocol = "imap";
    String host = "16.187.191.13";
    String username = "pop3@bing.com";
    String password = "1Qaz2wsx3edc";
    // Wu, Fei's PC
    // String host = "15.107.4.68";
    // String username = "cainzhong";
    // String password = "Cisco01!";
    Store store = session.getStore(protocol);
    store.connect(host, username, password);
    System.out.println("Success");
  }

}
