/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;

import net.sf.json.JSONArray;

/**
 * @author zhontao
 *
 */
public interface ReceiveMail {

  /**
   * Get the incoming parameters, their format is JSON, transfer them to Map<String, Object> and initialize them.
   * 
   * @param jsonParam
   * @throws Exception
   */
  public int initialize(String jsonParam);

  /**
   * Connect to the mail server.
   * 
   * @throws NoSuchProviderException
   * 
   * @throws MessagingException
   * @throws Exception
   */
  public void open() throws MessagingException;

  /**
   * Receive mails.
   * 
   * @return
   * @throws Exception
   */
  public JSONArray receive(String messageId, boolean save);

  public JSONArray receiveAttachment(String protocol, String messageId);

  public JSONArray receiveViaEWS(String messageId, boolean save);

  /**
   * Copy a message to a specific folder and delete the message in source folder.
   * 
   * @param msg
   * @throws MessagingException
   */
  public int moveMessage(String protocol, String messageId);

  /**
   * Close the connection to the mail server.
   * 
   * @throws MessagingException
   */
  public void close();

  public int deleteAttachments(String path);
}
