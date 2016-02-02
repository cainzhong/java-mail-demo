package com.main;

import java.net.URI;

import microsoft.exchange.webservices.data.autodiscover.IAutodiscoverRedirectionUrl;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;

public class EWSConnectionTest {
  public static class RedirectionUrlCallback implements IAutodiscoverRedirectionUrl {
    public boolean autodiscoverRedirectionUrlValidationCallback(String redirectionUrl) {
      return redirectionUrl.toLowerCase().startsWith("https://");
    }
  }

  public static ExchangeService connectViaExchangeManually(String email, String password) throws Exception {
    ExchangeService service = new ExchangeService();
    ExchangeCredentials credentials = new WebCredentials(email, password);
    service.setUrl(new URI("https://pod51059.outlook.com/ews/exchange.asmx"));
    service.setCredentials(credentials);
    service.setTraceEnabled(true);
    Folder inbox = Folder.bind(service, WellKnownFolderName.Inbox);
    System.out.println("messages: " + inbox.getTotalCount());
    return service;
  }

  public static ExchangeService connectViaExchangeAutodiscover(String email, String password) throws Exception {
    ExchangeService service = new ExchangeService();
    service.setCredentials(new WebCredentials(email, password));
    service.autodiscoverUrl(email, new RedirectionUrlCallback());
    service.setTraceEnabled(true);
    Folder inbox = Folder.bind(service, WellKnownFolderName.Inbox);
    System.out.println("messages: " + inbox.getTotalCount());
    return service;
  }

  public static void main(String[] args) {
    String email = "cainzhong@cainzhong.win";
    String password = "Cisco01!";
    try {
      ExchangeService service = connectViaExchangeAutodiscover(email, password);
      // ExchangeService service = connectViaExchangeManually("<name>@<company>.onmicrosoft.com", "<password>");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
