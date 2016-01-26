/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import com.java.mail.domain.MailAddress;

import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.EmailAddressCollection;
import net.sf.json.JSONObject;

/**
 * @author zhontao
 *
 */
public class MailUtil {
  /**
   * Convert JSONObject to Map<String, Object>.
   * 
   * @param jsonObject
   * @return a Map&lt;String, Object&gt;
   */
  public static Map<String, Object> convertJsonToMap(JSONObject jsonObject) {
    HashMap<String, Object> map = new HashMap<String, Object>();
    if (jsonObject != null && !jsonObject.isEmpty()) {
      Iterator<?> it = jsonObject.keys();
      while (it.hasNext()) {
        String key = String.valueOf(it.next());
        Object value = jsonObject.get(key);
        map.put(key, value);
      }
    }
    return map;
  }

  /**
   * Convert a collection of e-mail addresses to a list of MailAddress.
   * 
   * @param emailAddressCollection
   *          EmailAddress is a type of <code>microsoft.exchange.webservices.data.property.complex.EmailAddress<code>.
   * @return List&lt;MailAddress&gt;
   */
  public static List<MailAddress> convertToMailAddress(EmailAddressCollection emailAddressCollection) {
    List<MailAddress> addressList = new ArrayList<MailAddress>();
    if (emailAddressCollection != null) {
      Iterator<EmailAddress> it = emailAddressCollection.iterator();
      while (it.hasNext()) {
        EmailAddress emailAddress = it.next();

        MailAddress mailAddress = new MailAddress();
        mailAddress.setAddress(emailAddress.getAddress());
        mailAddress.setName(emailAddress.getName());
        addressList.add(mailAddress);
      }
    }
    return addressList;
  }

  /**
   * Convert an e-mail address to a list of MailAddress.
   * 
   * @param emailAddress
   *          EmailAddress is a type of <code>microsoft.exchange.webservices.data.property.complex.EmailAddress<code>.
   * @return List&lt;MailAddress&gt;
   */
  public static List<MailAddress> convertToMailAddress(EmailAddress emailAddress) {
    List<MailAddress> addressList = new ArrayList<MailAddress>();
    if (emailAddress != null) {
      MailAddress mailAddress = new MailAddress();
      mailAddress.setAddress(emailAddress.getAddress());
      mailAddress.setName(emailAddress.getName());
      addressList.add(mailAddress);
    }
    return addressList;
  }

  /**
   * Convert an array of addresses to a list of MailAddress.
   * 
   * @param addresses
   *          Address is a type of <code>javax.mail.Address<code>.
   * @return List&lt;MailAddress&gt;
   */
  public static List<MailAddress> convertToMailAddress(Address[] addresses) {
    List<MailAddress> addressList = new ArrayList<MailAddress>();
    if ((addresses != null && addresses.length != 0)) {
      if(addresses instanceof InternetAddress[]){
        InternetAddress[] internetAddresses = (InternetAddress[]) addresses;
        for (InternetAddress internetAddress : internetAddresses) {
          MailAddress mailAddress = new MailAddress();
          String personal = internetAddress.getPersonal();
          String address = internetAddress.getAddress();
          mailAddress.setName(personal);
          mailAddress.setAddress(address);
          addressList.add(mailAddress);
        }
      } else {
        for (Address address : addresses) {
          MailAddress mailAddress = new MailAddress();
          mailAddress.setAddress(address.toString());
          addressList.add(mailAddress);
        }
      }
    }
    return addressList;
  }
}
