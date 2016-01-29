package com.microsoft.ews;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.mail.smime.SMIMESigned;

public class Test {
  public static void main(String args[]) {
    try {
      DataSource source = new FileDataSource("C:/Users/zhontao/Desktop/smime.p7m");
      MimeMultipart multi = new MimeMultipart(source);
      SMIMESigned signedData = new SMIMESigned(multi);
    } catch (MessagingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (CMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
