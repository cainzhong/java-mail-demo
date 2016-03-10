/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail.impl;

import com.java.mail.MailReceiver;

/**
 * @author zhontao
 *
 */
public interface MailReceiverFactory {

  public MailReceiver create(String serverType) throws Exception;
}
