package com.http.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;

public class SocketTest {
  public static void main(String args[]) throws IOException {
    SocketAddress addr = new InetSocketAddress("137.118.190.92", 10200);
    // SocketAddress addr = new InetSocketAddress("socks.mydomain.com", 1080);
    Proxy proxy = new Proxy(Proxy.Type.SOCKS, addr);
    Socket socket = new Socket(proxy);
    InetSocketAddress dest = new InetSocketAddress("pop.163.com", 110);
    socket.connect(dest);
    System.out.println("Success");
  }

}
