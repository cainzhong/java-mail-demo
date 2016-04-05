package com.main;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Store;

public class MailConnectionTest2 {
  protected static final String POP3 = "POP3";

  protected static final String POP3S = "POP3S";

  protected static final String IMAP = "IMAP";

  protected static final String IMAPS = "IMAPS";

  public static void main(String args[]) throws Exception {
    String host = "imaps.163.com";
    String port = "143";
    String protocol = "imap";
    String username = "18616680719@163.com";
    String password = "wxj824188950";

    String socksHost = "142.159.244.217";
    String socksPort = "1080";

    // Properties props = new Properties();
    Properties props = System.getProperties();
    // props.setProperty("mail.debug", "true");
    props.setProperty("proxySet", "true");// does this line even do anything?
    props.setProperty("socksProxyHost", socksHost);
    props.setProperty("socksProxyPort", socksPort);
    props.setProperty("socksProxyVersion", "5");// or 4 if you want to use 4

    // props.put("mail.imap.port", port);
    // props.put("proxySet", "true");
    // props.put("mail.imap.socks.host", socksHost);
    // props.put("mail.imap.socks.port", socksPort);

    // Session session = Session.getDefaultInstance(props, null);
    Session session = Session.getInstance(props, null);
    String pack = Session.class.getPackage().toString();
    System.out.println(pack);

    Store store = session.getStore(protocol);
    testUri();
    store.connect(host, username, password);
    testUri();
    // System.out.println(doGet());
    System.out.println("Success");
    store.close();
  }

  public static void testUri() {
    URL url;
    try {
      url = new URL("http://www.baidu.com");
      InputStream in = url.openStream();
      System.out.println(in.toString());
      System.out.println("连接可用");
      in.close();
    } catch (Exception e1) {
      System.out.println("连接打不开!");
      url = null;
    }
  }

  public static String doGet() throws Exception {
    URL localURL = new URL("http://www.baidu.com/");
    URLConnection connection = localURL.openConnection();
    HttpURLConnection httpURLConnection = (HttpURLConnection) connection;

    httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
    httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

    InputStream inputStream = null;
    InputStreamReader inputStreamReader = null;
    BufferedReader reader = null;
    StringBuffer resultBuffer = new StringBuffer();
    String tempLine = null;

    if (httpURLConnection.getResponseCode() >= 300) {
      throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
    }

    try {
      inputStream = httpURLConnection.getInputStream();
      inputStreamReader = new InputStreamReader(inputStream);
      reader = new BufferedReader(inputStreamReader);

      while ((tempLine = reader.readLine()) != null) {
        resultBuffer.append(tempLine);
      }

    } finally {

      if (reader != null) {
        reader.close();
      }

      if (inputStreamReader != null) {
        inputStreamReader.close();
      }

      if (inputStream != null) {
        inputStream.close();
      }

    }

    return resultBuffer.toString();
  }
}
