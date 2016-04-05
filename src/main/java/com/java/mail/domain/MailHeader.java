/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author zhontao
 *
 */
public class MailHeader {
  private String msgId;

  private List<MailAddress> from;

  private String receivedUTCDate;

  public String getMsgId() {
    return this.msgId;
  }

  public void setMsgId(String msgId) {
    this.msgId = msgId;
  }

  public List<MailAddress> getFrom() {
    return this.from;
  }

  public void setFrom(List<MailAddress> from) {
    this.from = from;
  }

  public String getReceivedUTCDate() {
    return this.receivedUTCDate;
  }

  public void setReceivedUTCDate(String receivedUTCDate) {
    this.receivedUTCDate = receivedUTCDate;
  }

  /* Comparator for sorting the list by receivedUTCDate */
  public static Comparator<MailHeader> MailHeaderComparator = new Comparator<MailHeader>() {
    @Override
    public int compare(MailHeader m1, MailHeader m2) {
      String receivedUTCDate1 = m1.getReceivedUTCDate().toLowerCase();
      String receivedUTCDate2 = m2.getReceivedUTCDate().toLowerCase();
      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
      try {
        Date receivedDate1 = sdf.parse(receivedUTCDate1);
        Date receivedDate2 = sdf.parse(receivedUTCDate2);
        return receivedDate1.compareTo(receivedDate2);
      } catch (ParseException e) {
        return receivedUTCDate1.compareTo(receivedUTCDate2);
      }
    }
  };
}
