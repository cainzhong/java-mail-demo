/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail;

/**
 * @author zhontao
 *
 */
public interface MailReceiverFactory {

  public MailReceiver create(String serverType) throws Exception;
}
