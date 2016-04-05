package com.http.proxy;

import java.io.IOException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

public class HttpMailConnectionTest {

  public static void main(String args[]) throws MessagingException, IOException, JSchException {
    // JSch jsch = new JSch();
    // com.jcraft.jsch.Session jschSession = jsch.getSession("web-proxy.sgp.hp.com", "web-proxy.sgp.hp.com", 8080);
    //
    // jschSession.setProxy(new ProxyHTTP("web-proxy.sgp.hp.com", 8080));
    //
    // // username and password will be given via UserInfo interface.
    // UserInfo ui = new MyUserInfo();
    // jschSession.setUserInfo(ui);
    //
    // jschSession.connect();
    //
    // Channel channel = jschSession.openChannel("shell");
    //
    // channel.setInputStream(System.in);
    // channel.setOutputStream(System.out);
    //
    // channel.connect();

    String username = "liubin011221085@163.com";
    String password = "1Qaz2wsx3edc&";

    JSch jsch = new JSch();
    com.jcraft.jsch.Session jschSession = jsch.getSession(username, "127.0.0.1");
    jschSession.setPassword(password);
    jschSession.connect();
    jschSession.setPortForwardingL(110, "web-proxy.sgp.hp.com", 8080);

    String host = "pop.163.com";
    String protocol = "pop3";
    Properties props = HttpMailConnectionTest.getProperties();
    Session session = Session.getInstance(props, null);

    Store store = session.getStore(protocol);
    store.connect(host, username, password);
    System.out.println("Success");
    // socket.close();
  }

  private static Properties getProperties() {
    Properties props = new Properties();

    // props.put("mail.debug", "true");
    props.put("mail.pop3.port", "110");
    props.put("mail.imap.port", "143");

    props.put("mail.pop3.rsetbeforequit", "true");
    props.put("mail.pop3s.rsetbeforequit", "true");

    return props;
  }

}
