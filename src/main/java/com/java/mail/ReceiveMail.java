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
   * @return error code
   */
  public int initialize(String jsonParam);

  /**
   * Get message id in mail box.
   * 
   * @return
   * @throws Exception
   */
  public JSONArray getMsgIdList() throws Exception;

  /**
   * Connect to the mail server.
   * 
   * @throws MessagingException
   */
  public void open() throws MessagingException;

  /**
   * Receive mails via POP3, POP3S, IMAP, IMAPS.
   * 
   * @param messageId
   * @param save
   *          true indicates the attachment will be saved
   * @return
   */
  public JSONArray receive(String messageId, boolean save);

  /**
   * Receive attachments of specific mail.
   * 
   * @param protocol
   *          POP3, POP3S, IMAP, IMAPS or EWS
   * @param messageId
   * @return
   */
  public JSONArray receiveAttachment(String messageId);

  /**
   * Move a message to a specific folder.
   * 
   * @param protocol
   *          POP3, POP3S, IMAP, IMAPS or EWS
   * @param messageId
   * @return
   */
  public int moveMessage(String messageId);

  /**
   * Close the connection to the mail server.
   * 
   */
  public void close();

  /**
   * Delete attachments.
   * 
   * @param path
   * @return
   */
  public int deleteAttachments(String path);
}
