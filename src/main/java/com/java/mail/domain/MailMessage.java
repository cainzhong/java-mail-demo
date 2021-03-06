/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail.domain;

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

  /* a email's body */
  private String body;

  /* the date of sending a email. */
  private String sendDate;

  /* the date of receiving a email. */
  private String receivedDate;

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

  public String getBody() {
    return this.body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getSendDate() {
    return this.sendDate;
  }

  public void setSendDate(String sendDate) {
    this.sendDate = sendDate;
  }

  public String getReceivedDate() {
    return this.receivedDate;
  }

  public void setReceivedDate(String receivedDate) {
    this.receivedDate = receivedDate;
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

  public String[] getAutoReply() {
    return this.autoReply;
  }

  public void setAutoReply(String[] autoReply) {
    this.autoReply = autoReply;
  }
}
