/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail.domain;

import java.util.Date;
import java.util.List;

/**
 * @author zhontao
 *
 */
public class MailMessage {
  private String msgId;

  private String uid;

  /* the address of email sender. */
  private List<MailAddress> from;

  /* the address of email receiver. */
  private List<MailAddress> to;

  /* the email's CC address. */
  private List<MailAddress> cc;

  /* the email's BCC address. */
  private List<MailAddress> bcc;

  /* a email's subject. */
  private String subject;

  /* a email's body, format is txt */
  private String txtBody;

  /* a email's body, format is html */
  private String htmlBody;

  /* the date of sending a email. */
  private Date sendDate;

  private boolean hasAttachments;

  /* a list of attachments */
  private List<Attachment> attachList;

  /* verify the email has a digital signature or not */
  private boolean hasSignature;

  /* verify the signature is valid and trusted or not */
  private boolean signaturePassed;

  /* the name of a principal */
  private String nameOfPrincipal;

  /* the type of a email message */
  private String contentType;

  private Enum<MailStatus> mailStatus;

  private String[] autoReply;

  public String getMsgId() {
    return this.msgId;
  }

  public void setMsgId(String msgId) {
    this.msgId = msgId;
  }

  public String getUid() {
    return this.uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public List<MailAddress> getFrom() {
    return this.from;
  }

  public void setFrom(List<MailAddress> from) {
    this.from = from;
  }

  public List<MailAddress> getTo() {
    return this.to;
  }

  public void setTo(List<MailAddress> to) {
    this.to = to;
  }

  public List<MailAddress> getCc() {
    return this.cc;
  }

  public void setCc(List<MailAddress> cc) {
    this.cc = cc;
  }

  public List<MailAddress> getBcc() {
    return this.bcc;
  }

  public void setBcc(List<MailAddress> bcc) {
    this.bcc = bcc;
  }

  public String getSubject() {
    return this.subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getTxtBody() {
    return this.txtBody;
  }

  public void setTxtBody(String txtBody) {
    this.txtBody = txtBody;
  }

  public String getHtmlBody() {
    return this.htmlBody;
  }

  public void setHtmlBody(String htmlBody) {
    this.htmlBody = htmlBody;
  }

  public Date getSendDate() {
    return this.sendDate;
  }

  public void setSendDate(Date sendDate) {
    this.sendDate = sendDate;
  }

  public boolean isHasAttachments() {
    return this.hasAttachments;
  }

  public void setHasAttachments(boolean hasAttachments) {
    this.hasAttachments = hasAttachments;
  }

  public List<Attachment> getAttachList() {
    return this.attachList;
  }

  public void setAttachList(List<Attachment> attachList) {
    this.attachList = attachList;
  }

  public boolean isHasSignature() {
    return this.hasSignature;
  }

  public void setHasSignature(boolean hasSignature) {
    this.hasSignature = hasSignature;
  }

  public boolean isSignaturePassed() {
    return this.signaturePassed;
  }

  public void setSignaturePassed(boolean signaturePassed) {
    this.signaturePassed = signaturePassed;
  }

  public String getNameOfPrincipal() {
    return this.nameOfPrincipal;
  }

  public void setNameOfPrincipal(String nameOfPrincipal) {
    this.nameOfPrincipal = nameOfPrincipal;
  }

  public String getContentType() {
    return this.contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public Enum<MailStatus> getMailStatus() {
    return this.mailStatus;
  }

  public void setMailStatus(Enum<MailStatus> mailStatus) {
    this.mailStatus = mailStatus;
  }

  public String[] getAutoReply() {
    return this.autoReply;
  }

  public void setAutoReply(String[] autoReply) {
    this.autoReply = autoReply;
  }
}
