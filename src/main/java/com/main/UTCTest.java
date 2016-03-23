package com.main;

import java.text.ParseException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

public class UTCTest {

  public static void main(String args[]) throws ParseException {
    String html = "<!DOCTYPE html><html><head>    <title></title></head><body> <sdasd>   sdssd</body></html>";
    String jsoup = Jsoup.clean(html, Whitelist.relaxed());
    Document doc = Jsoup.parse(html);
    System.out.println(jsoup);
    System.out.println(doc.text());
  }
}
