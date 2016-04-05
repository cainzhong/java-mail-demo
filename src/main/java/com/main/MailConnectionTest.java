package com.main;

import java.io.IOException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

public class MailConnectionTest {

  protected static final String POP3 = "POP3";

  protected static final String POP3S = "POP3S";

  protected static final String IMAP = "IMAP";

  protected static final String IMAPS = "IMAPS";

  public static void main(String args[]) throws MessagingException, IOException {
    // System.setProperty("javax.net.ssl.trustStore", "C:\\Program Files\\Java\\jdk1.7.0_79\\jre\\lib\\security\\cacerts");
    String host = "imap.163.com";
    String port = "143";
    String protocol = "imap";
    String username = "18616680719@163.com";
    String password = "wxj824188950";

    String socksHost = "137.118.190.92";
    String socksPort = "10200";

    Properties props = MailConnectionTest.getProperties(protocol, port, socksHost, socksPort);
    Session session = Session.getInstance(props, null);

    Store store = session.getStore(protocol);
    store.connect(host, username, password);
    System.out.println("Success");
    store.close();
  }

  private static Properties getProperties(String protocol, String port, String socksHost, String socksPort) {
    Properties props = new Properties();
    if (POP3.equalsIgnoreCase(protocol)) {
      props.put("mail.pop3.port", port);
      props.put("mail.pop3.socks.host", socksHost);
      props.put("mail.pop3.socks.port", socksPort);
    } else if (POP3S.equalsIgnoreCase(protocol)) {
      props.put("mail.pop3s.port", port);
      props.put("mail.pop3s.socks.host", socksHost);
      props.put("mail.pop3s.socks.port", socksPort);
    } else if (IMAP.equalsIgnoreCase(protocol)) {
      props.put("mail.imap.port", port);
      props.setProperty("proxySet", "true");
      props.put("mail.imap.socks.host", socksHost);
      props.put("mail.imap.socks.port", socksPort);
    } else if (IMAPS.equalsIgnoreCase(protocol)) {
      props.put("mail.imaps.port", port);
      props.put("mail.imaps.socks.host", socksHost);
      props.put("mail.imaps.socks.port", socksPort);
    }
    props.put("mail.pop3.rsetbeforequit", "true");
    props.put("mail.pop3s.rsetbeforequit", "true");

    props.put("mail.imap.partialfetch", "false");
    props.put("mail.imaps.partialfetch", "false");

    props.put("mail.imap.fetchsize", "1048576");
    props.put("mail.imaps.fetchsize", "1048576");

    // props.put("mail.imap.starttls.enable", "true");
    // props.put("mail.imaps.starttls.enable", "true");

    props.put("mail.imap.compress.enable", "true");
    props.put("mail.imaps.compress.enable", "true");
    /*
     * General Issues with Multiparts
     * 
     * @see http://www.bouncycastle.org/wiki/display/JA1/CMS+and+SMIME+APIs
     */
    // props.put("mail.mime.cachemultipart", false);

    return props;
  }

}
