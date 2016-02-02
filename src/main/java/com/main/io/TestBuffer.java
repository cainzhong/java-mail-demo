package com.main.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TestBuffer {

  public static void main(String args[]) throws IOException {
    String from = "C:/Users/zhontao/Pictures/2.74mb.jpg";
    System.out.println(from);
    long startTime1 = System.currentTimeMillis();
    String to = "C:/Users/zhontao/Pictures/2.74mb-Copy.jpg";
    TestBuffer.readWriteWithBuffer(from, to);
    long endTime1 = System.currentTimeMillis();
    System.out.println("使用缓冲区读�?�耗时：" + (endTime1 - startTime1) + "ms");
    long startTime = System.currentTimeMillis();
    String to1 = "C:/Users/zhontao/Pictures/2.74mb-Copy-1.jpg";
    TestBuffer.readWrite(from, to1);
    long endTime = System.currentTimeMillis();
    System.out.println("直接读�?�耗时：" + (endTime - startTime) + "ms");

  }

  public static void readWrite(String from, String to) throws IOException {
    InputStream in = null;
    OutputStream out = null;
    try {
      in = new FileInputStream(from);
      out = new FileOutputStream(to);
      while (true) {
        int data = in.read();
        if (data == -1) {
          break;
        }
        out.write(data);
      }
    } finally {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
    }
  }

  /***************************************************************************
   * 使用缓存区读写文件
   * 
   * @param from
   * @param to
   * @throws IOException
   */
  public static void readWriteWithBuffer(String from, String to) throws IOException {
    InputStream inBuffer = null;
    OutputStream outBuffer = null;
    try {
      inBuffer = new BufferedInputStream(new FileInputStream(from));
      outBuffer = new BufferedOutputStream(new FileOutputStream(to));
      while (true) {
        int data = inBuffer.read();
        if (data == -1) {
          break;
        }
        outBuffer.write(data);
      }
    } finally {
      if (inBuffer != null) {
        inBuffer.close();
      }
      if (outBuffer != null) {
        outBuffer.close();
      }
    }
  }

}