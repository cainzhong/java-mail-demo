/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail;

import javax.mail.MessagingException;

import net.sf.json.JSONArray;

/**
 * @author zhontao
 *
 */
public interface ReceiveMail {

  /**
   * Initialize the incoming parameters, their format is JSON.
   * 
   * @param jsonParam
   * @throws Exception
   */
  public void initialize(String jsonParam) throws Exception;

  /**
   * Connect to the mail server.
   * 
   * @throws Exception
   */
  public void open() throws Exception;

  /**
   * Get message id in mail box.
   * 
   * @return
   * @throws Exception
   */
  public JSONArray getMsgIdList() throws Exception;

  /**
   * Receive mails via POP3, POP3S, IMAP, IMAPS.
   * 
   * @param messageId
   * @param save
   *          true indicates the attachment will be saved
   * @return
   * @throws Exception
   */
  public JSONArray receive(String messageId, boolean save) throws Exception;

  /**
   * Receive attachments of specific mail.
   * 
   * @param messageId
   * @return
   * @throws Exception
   */
  public JSONArray receiveAttachment(String messageId) throws Exception;

  /**
   * Move a message to a specific folder.
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

  /**
   * Delete attachments.
   * 
   * @param path
   * @return
   */
  public void deleteAttachments(String path);
}