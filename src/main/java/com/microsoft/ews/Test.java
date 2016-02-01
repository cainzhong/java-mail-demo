package com.microsoft.ews;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMultipart;

public class Test {
  public static void main(String args[]) {
    try {
      DataSource source = new FileDataSource("C:/Users/zhontao/Desktop/smime.p7m");
      MimeMultipart multi1 = new MimeMultipart(source);
      for (int i = 0; i < multi1.getCount(); i++) {
        Part part1 = multi1.getBodyPart(i);
        if (part1.getContent() instanceof Multipart) {
          Multipart multi2 = (Multipart) part1.getContent();
          for (int j = 0; j < multi2.getCount(); j++) {
            Part part2 = multi2.getBodyPart(j);
            String contentType = part2.getContentType();
            System.out.println(contentType);
            // generally if the content type multipart/alternative, it is email text.
            if (part2.isMimeType("multipart/alternative")) {
              if (part2.getContent() instanceof Multipart) {
                Multipart multi3 = (Multipart) part2.getContent();
                for (int k = 0; k < multi3.getCount(); k++) {
                  Part part4 = multi3.getBodyPart(k);
                  String contentType1 = part4.getContentType();
                  System.out.println(contentType1);
                  if (part4.isMimeType("text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part4.getDisposition())) {
                    System.out.println(part4.getContent().toString());
                  } else if (part4.isMimeType("text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part4.getDisposition())) {
                    System.out.println(part4.getContent().toString());
                  }
                }
              }
            }
          }
        } else {
          String contentType = part1.getContentType();
          System.out.println(contentType);
          String disposition = part1.getDisposition();
          System.out.println(disposition);
          System.out.println(part1.getFileName());
          saveFile(part1.getFileName(), part1.getInputStream());
        }
      }
    } catch (MessagingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Save file to temp directory.
   * 
   * @throws IOException
   * @throws FileNotFoundException
   */
  public static void saveFile(String fileName, InputStream in) throws IOException {
    File file = new File(fileName);
    if (!file.exists()) {
      OutputStream out = null;
      try {
        out = new BufferedOutputStream(new FileOutputStream(file));
        in = new BufferedInputStream(in);
        byte[] buf = new byte[180];
        int len;
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
  }
}
