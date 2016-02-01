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
   * Get the incoming parameters, their format is JSON, transfer them to Map<String, Object> and initialize them.
   * 
   * @param jsonParam
   * @throws Exception
   */
  public void initialize(String jsonParam) throws Exception;

  /**
   * Connect to the mail server.
   * 
   * @throws MessagingException
   * @throws Exception
   */
  public void open() throws MessagingException, Exception;

  /**
   * Receive mails.
   * 
   * @return
   * @throws Exception
   */
  public JSONArray receive(boolean save) throws Exception;

  public String receiveAttachment(String messageId);

  public JSONArray receiveThroughEWS(String fromStringTerm, String subjectTerm, int pageSize) throws Exception;

  /**
   * Copy a message to a specific folder and delete the message in source folder.
   * 
   * @param msg
   * @throws MessagingException
   */
  public String moveMessage(String MessageID) throws MessagingException;

  /**
   * Close the connection to the mail server.
   * 
   * @throws MessagingException
   */
  public void close() throws MessagingException;
}
