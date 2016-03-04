package com.java.mail.impl;

import org.apache.commons.logging.LogFactory;

import com.hp.ov.sm.common.core.JLog;

/**
 * @author zhontao
 *
 */
public class ReceiveMailFactory {
  static final JLog LOG = new JLog(LogFactory.getLog(ReceiveMailFactory.class));

  private static final String EXCHANGER_SERVER = "Exchange Server";

  private static final String EXCHANGE_WEB_SERVICES = "EWS";

  public AbstractMailReceiver getInstance(String mailServer) throws Exception {
    if (EXCHANGER_SERVER.equalsIgnoreCase(mailServer)) {
      return new ExchangeServerMailReceiverImpl();
    } else if (EXCHANGE_WEB_SERVICES.equalsIgnoreCase(mailServer)) {
      return new EWSMailReceiverImpl();
    } else {
      String e = "Can not identify the mail server.";
      LOG.info(e);
      throw new Exception(e);
    }
  }
}
