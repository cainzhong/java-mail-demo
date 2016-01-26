package com.microsoft.ews;

import java.net.URI;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.enumeration.search.SortDirection;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;

/**
 * @author Shantanu Sikdar
 *
 */
public class MSExchangeEmailService {
  public void receiveUsingEWS() throws Exception {
    ExchangeService service = new ExchangeService();

    /*
     * if ("true".equalsIgnoreCase(this.proxySet)) {
     * WebProxyCredentials proxyCredentials = new WebProxyCredentials("proxyServerUser", "proxyPassword", "domain");
     * WebProxy proxy = new WebProxy("proxyServerHostName", 80, proxyCredentials);
     * service.setWebProxy(proxy);
     * }
     */

    ExchangeCredentials credentials = new WebCredentials("cainzhong@cainzhong.win", "Cisco01!");
    service.setCredentials(credentials);
    service.setUrl(new URI("https://outlook.office365.com/EWS/Exchange.asmx"));
    // service.autodiscoverUrl("cainzhong@cainzhong.win");

    // EmailMessage msg = new EmailMessage(service);
    // msg.setSubject("EWS API!");
    // msg.setBody(MessageBody.getMessageBodyFromText("Sent using the EWS Java API."));
    // msg.getToRecipients().add("cainzhong@cainzhong.win");
    // msg.send();

    microsoft.exchange.webservices.data.core.service.folder.Folder inbox = microsoft.exchange.webservices.data.core.service.folder.Folder.bind(service, WellKnownFolderName.Inbox);
    this.findItems(service);

    System.out.println("end");
  }

  private void findItems(ExchangeService service) throws Exception {
    ItemView view = new ItemView(10);
    view.getOrderBy().add(ItemSchema.DateTimeReceived, SortDirection.Ascending);
    view.setPropertySet(new PropertySet(BasePropertySet.IdOnly, ItemSchema.Subject, ItemSchema.DateTimeReceived));

    FindItemsResults<Item> findResults = service.findItems(WellKnownFolderName.Inbox, new SearchFilter.SearchFilterCollection(LogicalOperator.Or, new SearchFilter.ContainsSubstring(ItemSchema.Subject, "EWS"), new SearchFilter.ContainsSubstring(ItemSchema.Subject, "API")), view);

    System.out.println("Total number of items found: " + findResults.getTotalCount());

    for (Item item : findResults) {
      System.out.println(item.getSubject());
      System.out.println(item.getBody());
      System.out.println(item.getDateTimeReceived());
      // Do something with the item.
    }
  }

  public static void main(String[] args) throws Exception {
    MSExchangeEmailService msees = new MSExchangeEmailService();
    msees.receiveUsingEWS();
    // msees.readAppointments();
  }
}