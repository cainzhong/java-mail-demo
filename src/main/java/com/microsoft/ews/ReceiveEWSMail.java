package com.microsoft.ews;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.BinaryCodec;

import com.java.mail.MailUtil;
import com.java.mail.domain.MailMessage;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.ComparisonMode;
import microsoft.exchange.webservices.data.core.enumeration.search.ContainmentMode;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.Attachment;
import microsoft.exchange.webservices.data.property.complex.AttachmentCollection;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.EmailAddressCollection;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;

public class ReceiveEWSMail {
  private static ExchangeService service;

  private String fromStringTerm;

  private String subjectTerm;

  public static void main(String[] args) throws Exception {
    String fromStringTerm = "tao.zhong@hpe.com";
    String subjectTerm = "Signed Mail with 2 attachments.";

    ReceiveEWSMail receive = new ReceiveEWSMail();
    receive.initialize();

    receive.findItems(fromStringTerm, subjectTerm, 1);
  }

  public void initialize() throws URISyntaxException {
    String username = "cainzhong@cainzhong.win";
    String password = "Cisco01!";
    String uri = "https://outlook.office365.com/EWS/Exchange.asmx";
    this.fromStringTerm = "tao.zhong@hpe.com";
    this.subjectTerm = "Signed Mail with 2 attachments.";

    service = new ExchangeService();
    ExchangeCredentials credentials = new WebCredentials(username, password);
    service.setCredentials(credentials);
    service.setUrl(new URI(uri));
    service.autodiscoverUrl(emailAddress);

  }

  public void findItems(String fromStringTerm, String subjectTerm, int pageSize) throws Exception {
    ItemView view = new ItemView(pageSize);
    // view.setPropertySet(new PropertySet(BasePropertySet.IdOnly, ItemSchema.Subject, ItemSchema.DateTimeReceived));

    SearchFilter.ContainsSubstring fromTermFilter = new SearchFilter.ContainsSubstring(EmailMessageSchema.From, fromStringTerm);
    SearchFilter.ContainsSubstring subjectFilter = new SearchFilter.ContainsSubstring(ItemSchema.Subject, subjectTerm, ContainmentMode.Substring, ComparisonMode.IgnoreCase);
    System.out.println(BinaryCodec.class.getProtectionDomain().getCodeSource().getLocation());
    FindItemsResults<Item> findResults = service.findItems(WellKnownFolderName.Inbox, new SearchFilter.SearchFilterCollection(LogicalOperator.And, fromTermFilter, subjectFilter), view);

    System.out.println("Total number of items found: " + findResults.getTotalCount());
    List<MailMessage> msgList = new ArrayList<MailMessage>();
    for (Item item : findResults) {
      MailMessage mailMsg = this.readEmailItem(item.getId());
      msgList.add(mailMsg);
      // Do something with the item.
    }
  }

  /**
   * Reading one email at a time. Using Item ID of the email.
   * Creating a message data map as a return value.
   */
  public MailMessage readEmailItem(ItemId itemId) {
    MailMessage mailMsg = new MailMessage();
    try {
      Item itm = Item.bind(service, itemId, PropertySet.FirstClassProperties);
      EmailMessage emailMessage = EmailMessage.bind(service, itm.getId());

      this.setMailMsgForBasicInfo(emailMessage, mailMsg);

      int size = emailMessage.getSize();
      String emailBody = emailMessage.getBody().toString();
      mailMsg.setTxtBody(emailBody);
      AttachmentCollection attachmentCollection = emailMessage.getAttachments();
      Iterator<Attachment> it = attachmentCollection.iterator();
      List<com.java.mail.domain.Attachment> mailAttachList = new ArrayList<com.java.mail.domain.Attachment>();
      while (it.hasNext()) {
        Attachment attachment = it.next();
        String fileName = attachment.getName();
        int fileSize = attachment.getSize();
        String suffix = attachment.getContentType();
        com.java.mail.domain.Attachment mailAttachment = new com.java.mail.domain.Attachment();
        mailAttachment.setFileName(fileName);
        mailAttachment.setFileType(suffix);
        mailAttachment.setFileSize(fileSize);
        mailAttachList.add(mailAttachment);
        mailMsg.setAttachList(mailAttachList);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return mailMsg;

  }

  /**
   * Set some basic information to MailMessage .
   * 
   * @param msg
   * @param mailMsg
   * @throws ServiceLocalException
   */
  private void setMailMsgForBasicInfo(EmailMessage emailMessage, MailMessage mailMsg) throws ServiceLocalException {
    String id = emailMessage.getId().toString();
    EmailAddress from = emailMessage.getFrom();
    EmailAddressCollection to = emailMessage.getToRecipients();
    EmailAddressCollection cc = emailMessage.getCcRecipients();
    EmailAddressCollection bcc = emailMessage.getBccRecipients();
    String subject = emailMessage.getSubject();
    Date sendDate = emailMessage.getDateTimeCreated();

    mailMsg.setId(id);
    mailMsg.setFrom(MailUtil.convertToMailAddress(from));
    mailMsg.setTo(MailUtil.convertToMailAddress(to));
    mailMsg.setCc(MailUtil.convertToMailAddress(cc));
    mailMsg.setBcc(MailUtil.convertToMailAddress(bcc));
    mailMsg.setSubject(subject);
    mailMsg.setSendDate(sendDate);
  }
}