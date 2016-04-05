package com.http.proxy;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class Tunnel {
  public static void main(String[] args) {
    Tunnel t = new Tunnel();
    try {
      t.go();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void go() throws Exception {
    String host = "pop.163.com";
    host = "127.0.0.1";
    String username = "liubin011221085@163.com";
    String password = "1Qaz2wsx3edc&";
    int port = 22;

    int tunnelLocalPort = 9080;
    String tunnelRemoteHost = "web-proxy.sgp.hp.com";
    int tunnelRemotePort = 8080;

    JSch jsch = new JSch();
    Session session = jsch.getSession(username, host, port);
    session.setPassword(password);
    localUserInfo lui = new localUserInfo();
    session.setUserInfo(lui);
    session.connect();
    session.setPortForwardingL(tunnelLocalPort, tunnelRemoteHost, tunnelRemotePort);
    System.out.println("Connected");

  }

  class localUserInfo implements UserInfo {
    String passwd;

    @Override
    public String getPassword() {
      return this.passwd;
    }

    @Override
    public boolean promptYesNo(String str) {
      return true;
    }

    @Override
    public String getPassphrase() {
      return null;
    }

    @Override
    public boolean promptPassphrase(String message) {
      return true;
    }

    @Override
    public boolean promptPassword(String message) {
      return true;
    }

    @Override
    public void showMessage(String message) {
    }
  }
}