/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail;

import java.text.ParseException;

import javax.mail.MessagingException;

/**
 * @author zhontao
 *
 */
public interface MailReceiver {

  /**
   * Connect to the mail server.
   * 
   * @param jsonParam
   * @throws Exception
   */
  public void open(String jsonParam) throws Exception;

  /**
   * Get all message IDs great than or equal to the given date. But it is only support IMAP, IMAPS. For POP3 and POP3S, it will get all the messages in INBOX.
   * 
   * @param date
   *          the given date. format: MM/dd/yyyy
   * @return
   * @throws MessagingException
   * @throws ParseException
   * @throws Exception
   */
  public String getNextMessageIdList(String date) throws MessagingException, ParseException, Exception;

  /**
   * Receive email message and save it as an eml file.
   * 
   * @param messageId
   *          message id
   * @return
   * @throws Exception
   */
  public String receive(String messageId) throws Exception;

  /**
   * Read email message header and body of specific mail in this disk from the given filePath.
   * 
   * @param filePath
   *          the given filePath
   * @return
   * @throws Exception
   */
  public String readMessage(String filePath) throws Exception;

  /**
   * Read attachments of specific mail in this disk from the given filePath.
   * 
   * @param filePath
   *          the given filePath
   * @return
   * @throws Exception
   */
  public String readEmailAttachments(String filePath) throws Exception;

  /**
   * Move a message to a specific folder. POP3 and POP3S do not support to move a message from a folder to another.
   * 
   * @param messageId
   * @throws Exception
   */
  public void moveMessage(String messageId) throws Exception;

  /**
   * Close the connection to the mail server.
   * 
   * @throws MessagingException
   * 
   */
  public void close() throws MessagingException;
}