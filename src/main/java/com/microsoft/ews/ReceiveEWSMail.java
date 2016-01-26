package com.microsoft.ews;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.enumeration.search.SortDirection;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;

public class ReceiveEWSMail {
  private static ExchangeService service;
  private static Integer NUMBER_EMAILS_FETCH = 5; // only latest 5 emails/appointments are fetched.

  /**
   * Initialize the Exchange Credentials.
   * Firstly check, whether "https://webmail.xxxx.com/ews/Services.wsdl" and "https://webmail.xxxx.com/ews/Exchange.asmx"
   * is accessible, if yes that means the Exchange Webservice is enabled on your MS Exchange.
   */
  static {
    try {
      service = new ExchangeService();
      ExchangeCredentials credentials = new WebCredentials("cainzhong@cainzhong.win", "Cisco01!");
      service.setCredentials(credentials);
      service.setUrl(new URI("https://outlook.office365.com/EWS/Exchange.asmx"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws Exception {
    ReceiveEWSMail receive = new ReceiveEWSMail();
    // receive.findItems();
    receive.readEmails();
  }

  public void findItems() throws Exception {
    ItemView view = new ItemView(10);
    view.getOrderBy().add(ItemSchema.DateTimeReceived, SortDirection.Ascending);
    view.setPropertySet(new PropertySet(BasePropertySet.IdOnly, ItemSchema.Subject, ItemSchema.DateTimeReceived));

    FindItemsResults<Item> findResults = service.findItems(WellKnownFolderName.Inbox, new SearchFilter.SearchFilterCollection(LogicalOperator.Or, new SearchFilter.ContainsSubstring(ItemSchema.Subject, "EWS"), new SearchFilter.ContainsSubstring(ItemSchema.Subject, "API")), view);

    System.out.println("Total number of items found: " + findResults.getTotalCount());

    for (Item item : findResults) {
      System.out.println(item.getSubject());
      System.out.println(item.getBody());
      // Do something with the item.
    }
  }

  /**
   * Number of email we want to read is defined as NUMBER_EMAILS_FETCH,
   */

  public List readEmails() {
    List msgDataList = new ArrayList();
    try {
      // Folder folder = Folder.bind( service, WellKnownFolderName.Inbox );
      FindItemsResults<Item> results = service.findItems(WellKnownFolderName.Inbox, new ItemView(NUMBER_EMAILS_FETCH));
      int i = 1;
      for (Item item : results) {
        Map messageData = new HashMap();
        messageData = this.readEmailItem(item.getId());
        System.out.println("\nEmails #" + (i++) + ":");
        System.out.println("subject : " + messageData.get("subject").toString());
        System.out.println("Sender : " + messageData.get("senderName").toString());
        msgDataList.add(messageData);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return msgDataList;
  }

  /**
   * Reading one email at a time. Using Item ID of the email.
   * Creating a message data map as a return value.
   */
  public Map readEmailItem(ItemId itemId) {
    Map messageData = new HashMap();
    try {
      Item itm = Item.bind(service, itemId, PropertySet.FirstClassProperties);
      EmailMessage emailMessage = EmailMessage.bind(service, itm.getId());
      messageData.put("emailItemId", emailMessage.getId().toString());
      messageData.put("subject", emailMessage.getSubject().toString());
      messageData.put("fromAddress", emailMessage.getFrom().getAddress().toString());
      messageData.put("senderName", emailMessage.getSender().getName().toString());
      Date dateTimeCreated = emailMessage.getDateTimeCreated();
      messageData.put("SendDate", dateTimeCreated.toString());
      Date dateTimeRecieved = emailMessage.getDateTimeReceived();
      messageData.put("RecievedDate", dateTimeRecieved.toString());
      messageData.put("Size", emailMessage.getSize() + "");
      messageData.put("emailBody", emailMessage.getBody().toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return messageData;

  }
}