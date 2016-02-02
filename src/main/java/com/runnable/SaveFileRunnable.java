package com.runnable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SaveFileRunnable implements Runnable {

  private String fileName;
  private InputStream in;
  private String msgId;

  public static final int BUFFSIZE = 180;

  public SaveFileRunnable(String msgId, String fileName, InputStream in) {
    this.msgId = msgId;
    this.fileName = fileName;
    this.in = in;
  }

  public String call() throws Exception {
    File file = new File(this.fileName);
    if (!file.exists()) {
      OutputStream out = null;
      try {
        out = new BufferedOutputStream(new FileOutputStream(file));
        byte[] buf = new byte[BUFFSIZE];
        int len;
        while ((len = this.in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } finally {
        // close streams
        try {
          if (this.in != null) {
            this.in.close();
          }
          if (out != null) {
            out.close();
          }
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    return this.msgId;
  }

  public void run() {
    File file = new File(this.fileName);
    if (!file.exists()) {
      OutputStream out = null;
      try {
        out = new BufferedOutputStream(new FileOutputStream(file));
        byte[] buf = new byte[BUFFSIZE];
        int len;
        while ((len = this.in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } finally {
        // close streams
        try {
          if (this.in != null) {
            this.in.close();
          }
          if (out != null) {
            out.close();
          }
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }

}
