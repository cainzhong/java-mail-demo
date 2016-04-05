package com.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

public class MailSocket {

  public static void main(String[] args) throws IOException {
    MailSocket mail = new MailSocket();
    mail.setSmtpServer("smtp.163.com");
    mail.setFromMail("liubin011221085@163.com");
    mail.addToMail("liubin011221085@163.com");
    mail.setUserName("liubin011221085@163.com");
    mail.setPassword("1Qaz2wsx3edc&");
    mail.setSubject("测试邮件");
    mail.setContent("<h1>你好</h1><br/><img src=\"https://www.baidu.com/img/baidu_jgylogo3.gif?v=39549282.gif\" />");
    mail.setShowLog(true);
    mail.send();
    System.out.println("程序结束");
  }

  /** 邮件主题 **/
  private String subject;
  /** 从此地址发出 **/
  private String fromMail;
  /** 用户名 **/
  private String userName;
  /** 登录密码 **/
  private String password;
  /** SMTP 服务器地址 **/
  private String smtpServer;
  /** SMTP 服务器端口（默认：25） **/
  private int smtpPort = 25;
  /** 发送到 toMail 中的所有地址 **/
  private List<String> toMail;
  /** 邮件内容 **/
  private String content;
  /** 是否显示日志 **/
  private boolean showLog;

  public void addToMail(String mail) {
    if (this.toMail == null)
      this.toMail = new ArrayList<String>();
    this.toMail.add(mail);
  }

  public void send() {
    if (this.smtpServer == null) {
      throw new RuntimeException("smtpServer 不能为空");
    }
    if (this.userName == null) {
      throw new RuntimeException("userName 不能为空");
    }
    if (this.password == null) {
      throw new RuntimeException("password 不能为空");
    }
    if (this.fromMail == null) {
      throw new RuntimeException("fromMail 不能为空");
    }
    if (this.toMail == null || this.toMail.isEmpty()) {
      throw new RuntimeException("toMail 不能为空");
    }
    if (this.content == null || this.toMail.isEmpty()) {
      throw new RuntimeException("content 不能为空");
    }

    Socket socket = null;
    InputStream in = null;
    OutputStream out = null;
    try {
      // socket = new Socket(this.smtpServer, this.smtpPort);

      String proxyHost = "web-proxy.sgp.hp.com";
      int proxyPort = 8080;
      SocketAddress addr = new InetSocketAddress(proxyHost, proxyPort);
      socket = new Socket(new Proxy(Proxy.Type.SOCKS, addr));
      socket.connect(addr);

      socket.setSoTimeout(3000);
      in = socket.getInputStream();
      out = socket.getOutputStream();
    } catch (IOException e) {
      throw new RuntimeException("连接到 " + this.smtpServer + ":" + this.smtpPort + " 失败", e);
    }

    BufferedReaderProxy reader = new BufferedReaderProxy(new InputStreamReader(in), this.showLog);
    PrintWriterProxy writer = new PrintWriterProxy(out, this.showLog);

    reader.showResponse();
    writer.println("HELO " + this.smtpServer);
    reader.showResponse();
    writer.println("AUTH LOGIN");
    reader.showResponse();
    writer.println(new String(Base64.encodeBase64(this.userName.getBytes())));
    reader.showResponse();
    writer.println(new String(Base64.encodeBase64(this.password.getBytes())));
    reader.showResponse();
    writer.println("MAIL FROM:" + this.fromMail);
    reader.showResponse();
    for (String mail : this.toMail) {
      writer.println("RCPT TO:" + mail);
      reader.showResponse();
    }
    writer.println("DATA");
    writer.println("Content-Type:text/html");
    if (this.subject != null) {
      writer.println("Subject:" + this.subject);
    }
    writer.println("From:" + this.fromMail);
    writer.print("To:");
    for (String mail : this.toMail) {
      writer.print(mail + "; ");
    }
    writer.println();
    writer.println();
    writer.println(this.content);
    writer.println(".");
    reader.showResponse();
    writer.println("QUIT");
    reader.showResponse();
    try {
      socket.close();
    } catch (IOException e) {
      System.err.println("发送邮件完成，关闭 Socket 出错：" + e.getMessage());
    }
  }

  public String getSubject() {
    return this.subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getFromMail() {
    return this.fromMail;
  }

  public void setFromMail(String fromMail) {
    this.fromMail = fromMail;
  }

  public String getSmtpServer() {
    return this.smtpServer;
  }

  public void setSmtpServer(String smtpServer) {
    this.smtpServer = smtpServer;
  }

  public int getSmtpPort() {
    return this.smtpPort;
  }

  public void setSmtpPort(int smtpPort) {
    this.smtpPort = smtpPort;
  }

  public String getContent() {
    return this.content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public List<String> getToMail() {
    return this.toMail;
  }

  public void setToMail(List<String> toMail) {
    this.toMail = toMail;
  }

  public String getUserName() {
    return this.userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean getShowLog() {
    return this.showLog;
  }

  public void setShowLog(boolean showLog) {
    this.showLog = showLog;
  }

  static class PrintWriterProxy extends PrintWriter {
    private boolean showRequest;

    public PrintWriterProxy(OutputStream out, boolean showRequest) {
      super(out, true);
      this.showRequest = showRequest;
    }

    @Override
    public void println() {
      if (this.showRequest)
        System.out.println();
      super.println();
    }

    @Override
    public void print(String s) {
      if (this.showRequest)
        System.out.print(s);
      super.print(s);
    }
  }

  static class BufferedReaderProxy extends BufferedReader {
    private boolean showResponse = true;

    public BufferedReaderProxy(Reader in, boolean showResponse) {
      super(in);
      this.showResponse = showResponse;
    }

    public void showResponse() {
      try {
        String line = this.readLine();
        String number = line.substring(0, 3);
        int num = -1;
        try {
          num = Integer.parseInt(number);
        } catch (Exception e) {
        }
        if (num == -1) {
          throw new RuntimeException("响应信息错误 : " + line);
        } else if (num >= 400) {
          throw new RuntimeException("发送邮件失败 : " + line);
        }
        if (this.showResponse) {
          System.out.println(line);
        }
      } catch (IOException e) {
        System.out.println("获取响应失败");
      }
    }

  }
}