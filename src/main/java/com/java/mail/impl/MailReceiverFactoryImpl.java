/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail.impl;

import org.apache.commons.logging.LogFactory;

import com.hp.ov.sm.common.core.JLog;

/**
 * @author zhontao
 *
 */
public class MailReceiverFactoryImpl implements MailReceiverFactory {
  static final JLog LOG = new JLog(LogFactory.getLog(MailReceiverFactoryImpl.class));

  static final String EXCHANGER_SERVER = "Exchange Server";

  static final String EXCHANGE_WEB_SERVICES = "EWS";

  private static final MailReceiverFactory INSTANCE = new MailReceiverFactoryImpl();

  public static final MailReceiverFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public AbstractMailReceiver create(String serverType) throws Exception {
    if (EXCHANGER_SERVER.equalsIgnoreCase(serverType)) {
      return new ExchangeServerMailReceiverImpl();
    } else if (EXCHANGE_WEB_SERVICES.equalsIgnoreCase(serverType)) {
      return new EWSMailReceiverImpl();
    } else {
      String e = "Can not identify the mail server.";
      LOG.info(e);
      throw new Exception(e);
    }
  }
}
