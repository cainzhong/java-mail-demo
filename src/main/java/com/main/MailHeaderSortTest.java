package com.main;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MailHeaderSortTest {
  public static void main(String args[]) {
    String html = "First parse<a> Parsed HTML into a doc.";
    Document doc = Jsoup.parse(html);
    System.out.println(doc.text());

  }
}
