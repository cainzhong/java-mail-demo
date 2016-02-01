package com.runnable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.mail.MessagingException;
import javax.mail.Part;

import com.java.mail.domain.Attachment;

public class ProcessAttachment implements Callable {
  private Part part;
  private List<Attachment> attachList;
  private List<String> suffixList;

  public static final int BUFFSIZE = 180;

  public ProcessAttachment(Part part, List<Attachment> attachList, List<String> suffixList) {
    this.part = part;
    this.attachList = attachList;
    this.suffixList = suffixList;
  }

  public List<Attachment> call() throws Exception {
    String disposition = null;
    try {
      disposition = this.part.getDisposition();
      if ((disposition != null && Part.ATTACHMENT.equalsIgnoreCase(disposition))) {
        // generate a new file name with unique UUID.
        String fileName = this.part.getFileName();
        UUID uuid = UUID.randomUUID();
        String prefix = fileName.substring(0, fileName.lastIndexOf(".") + 1);
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (this.suffixList.contains(suffix.toLowerCase())) {
          String tempDir = System.getProperty("java.io.tmpdir");
          fileName = tempDir + prefix + uuid + "." + suffix;

          int fileSize = this.part.getSize();
          Attachment attachment = new Attachment();
          attachment.setFileName(fileName);
          attachment.setFileType(suffix);
          attachment.setFileSize(fileSize);
          this.attachList.add(attachment);
          this.saveFile(fileName, this.part.getInputStream());
        }
      }
    } catch (MessagingException e) {
      e.printStackTrace();
    } catch (IOException e) {
    }
    System.out.println("Thread");
    return this.attachList;
  }

  /**
   * Save file to temp directory.
   * 
   * @throws IOException
   * @throws FileNotFoundException
   */
  private void saveFile(String fileName, InputStream in) throws IOException {
    File file = new File(fileName);
    if (!file.exists()) {
      OutputStream out = null;
      try {
        out = new BufferedOutputStream(new FileOutputStream(file));
        in = new BufferedInputStream(in);
        byte[] buf = new byte[180];
        int len;
        long begin = System.currentTimeMillis();
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } finally {
        // close streams
        if (in != null) {
          in.close();
        }
        if (out != null) {
          out.close();
        }
      }
    }
    System.out.println("Thread");
  }

}
