package com.main;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

public class MailConnectionTest {

  public static void main(String args[]) throws MessagingException {
    System.setProperty("javax.net.ssl.trustStore", "C:\\Program Files\\Java\\jdk1.7.0_79\\jre\\lib\\security\\cacerts");
    Properties props = MailConnectionTest.getProperties();
    Session session = Session.getDefaultInstance(props, null);
    String protocol = "imaps";
    String host = "15.107.4.68";
    // String host = "127.0.0.1";
    String username = "cainzhong";
    String password = "Cisco01!";
    Store store = session.getStore(protocol);
    store.connect(host, username, password);
    System.out.println("Success");
  }

  public static Properties getProperties() {
    String host = "15.107.4.68";
    // String host = "127.0.0.1";
    String port = "995";
    String protocol = "pop3s";
    Properties props = new Properties();
    props.put("mail.smtp.host", host);
    props.put("mail.smtp.port", port);
    props.put("mail.smtp.auth", true);
    props.put("mail.store.protocol", protocol);
    //    props.put("mail.imap.partialfetch", false);
    //    props.put("mail.imaps.partialfetch", false);
    //    props.put("mail.imap.fetchsize", "1048576");
    return props;
  }

}
