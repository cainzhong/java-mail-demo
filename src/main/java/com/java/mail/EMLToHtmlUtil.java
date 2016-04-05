/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.LogFactory;

import com.hp.ov.sm.common.core.JLog;
import com.java.mail.impl.MailReceiverFactoryImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author zhontao
 *
 */
public class EMLToHtmlUtil {
  private static final JLog LOG = new JLog(LogFactory.getLog(EMLToHtmlUtil.class));

  /**
   * Generate MailMessage and attachment list as a html file. Besides, MailMessage and attachment list are both are JSON string.
   * 
   * @param mailMessage
   *          get the value from invoking MailReceiver.readMessage(String filePath)
   * @param attachmentList
   *          get the value from invoking MailReceiver.readAttachments(String filePath)
   * @param htmlTemplateFilePath
   *          a html template file to generate html
   * @return
   * @throws Exception
   */
  public static String generateMailMessageAsHtml(String mailMessage, String attachmentList, String htmlTemplateFilePath) throws Exception {
    String htmlTemplate = readHtmlTemplate(htmlTemplateFilePath);
    String html = null;
    String folderPath;
    JSONObject mailMsgObj = JSONObject.fromObject(mailMessage);
    Map<String, Object> mailMsgMap = JSONUtil.convertJsonToMap(mailMsgObj);
    if (mailMsgMap != null && !mailMsgMap.isEmpty()) {
      JSONArray fromArray = (JSONArray) mailMsgMap.get("from");
      JSONArray toArray = (JSONArray) mailMsgMap.get("to");
      JSONArray ccArray = (JSONArray) mailMsgMap.get("cc");
      JSONArray bccArray = (JSONArray) mailMsgMap.get("bcc");
      String subject = (String) mailMsgMap.get("subject");
      String body = (String) mailMsgMap.get("body");
      String sendDate = (String) mailMsgMap.get("sendDate");

      html = htmlTemplate.replace("****title****", subject);
      html = html.replace("****subject****", subject);
      html = html.replace("****from****", convertMailAddrListToString(fromArray));
      html = html.replace("****to****", convertMailAddrListToString(toArray));
      html = html.replace("****cc****", convertMailAddrListToString(ccArray));
      html = html.replace("****bcc****", convertMailAddrListToString(bccArray));
      html = html.replace("****date****", sendDate);
      html = html.replace("****body****", body);

      // create folder, folder name is subject.
      String saveFileName = removeIllegalWords(subject);
      folderPath = createNewFolder(saveFileName);
      // copy attachments to the new folder, and add a hyperlink in the html.
      String attachmentLinks = generateAttachmentsLink(attachmentList, folderPath);
      html = html.replace("****attachments****", attachmentLinks);
      File f = new File(folderPath + "/" + saveFileName + ".html");
      BufferedWriter writer = new BufferedWriter(new FileWriter(f));
      writer.write(html);
      writer.close();
    } else {
      String e = "May be the JSON Arguments is null.";
      LOG.error(e);
      throw new Exception(e);
    }
    return folderPath;
  }

  /**
   * Read a file to a string.
   * 
   * @param path
   * @return
   * @throws IOException
   */
  private static String readHtmlTemplate(String path) throws IOException {
    StringBuilder sb = new StringBuilder();
    BufferedReader in = new BufferedReader(new FileReader(path));
    String str;
    while ((str = in.readLine()) != null) {
      sb.append(str);
    }
    in.close();
    return sb.toString();
  }

  /**
   * Create a new folder.
   * 
   * @param folderName
   * @return
   * @throws IOException
   */
  private static String createNewFolder(String folderName) throws IOException {
    String tempDir = System.getProperty("java.io.tmpdir");
    UUID uuid = UUID.randomUUID();
    String folderPath = tempDir + uuid + "-" + folderName;
    File file = new File(folderPath);
    if (!file.exists()) {
      if (file.mkdir()) {
        return folderPath;
      } else {
        String e = "Fail to create the folder.";
        LOG.error(e);
        throw new IOException(e);
      }
    } else {
      String e = "The folder already exists.";
      LOG.error(e);
      throw new IOException(e);
    }
  }

  private static String convertMailAddrListToString(JSONArray jsonArray) {
    StringBuffer sb = new StringBuffer();
    if (jsonArray != null && !jsonArray.isEmpty()) {
      for (int i = 0; i < jsonArray.size(); i++) {
        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
        String name = jsonObject.getString("name");
        String address = jsonObject.getString("address");
        if (i == jsonArray.size() - 1) {
          sb.append("\"").append(name).append("\"").append(" &lt;").append(address).append("&gt;");
        } else {
          sb.append("\"").append(name).append("\"").append(" &lt;").append(address).append("&gt;").append("; ");
        }
      }
      return sb.toString();
    } else {
      return "";
    }
  }

  /**
   * Remove illegal words from the given string
   * 
   * @param input
   *          the given string
   * @return a string without illegal words
   */
  private static String removeIllegalWords(String input) {
    String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
    Pattern p = Pattern.compile(regEx);
    Matcher m = p.matcher(input);
    return m.replaceAll("").trim();
  }

  private static void copyFile(String sourceFilePath, String destinationFilePath) throws IOException {
    InputStream inStream = null;
    OutputStream outStream = null;
    File sourceFile = new File(sourceFilePath);
    File destinationFile = new File(destinationFilePath);

    inStream = new FileInputStream(sourceFile);
    outStream = new FileOutputStream(destinationFile); // for override file content
    // outStream = new FileOutputStream(file2,<strong>true</strong>); // for append file content

    byte[] buffer = new byte[1024];
    int length;
    while ((length = inStream.read(buffer)) > 0) {
      outStream.write(buffer, 0, length);
    }

    if (inStream != null) {
      inStream.close();
    }
    if (outStream != null) {
      outStream.close();
    }
  }

  private static String generateAttachmentsLink(String attachmentList, String folderPath) throws IOException {
    if (!StringUtils.isEmpty(attachmentList)) {
      JSONArray attachmentArray = JSONArray.fromObject(attachmentList);
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < attachmentArray.size(); i++) {
        JSONObject attachmentObj = (JSONObject) attachmentArray.get(i);
        String fileName = attachmentObj.getString("fileName");
        String filePath = attachmentObj.getString("filePath");
        String htmlAttachmentFilePath = folderPath + "/" + fileName;
        copyFile(filePath, htmlAttachmentFilePath);
        sb.append("<a href=\"").append(fileName).append("\">").append(fileName).append("</a>").append("&#160;&#160;");
      }
      return sb.toString();
    } else {
      return "";
    }
  }

  public static void main(String args[]) throws Exception {
    String htmlTemplateFilePath = EMLToHtmlUtil.class.getResource("/smartEmailTemplate.html").getPath();
    if (htmlTemplateFilePath.startsWith("/")) {
      htmlTemplateFilePath = htmlTemplateFilePath.substring(1, htmlTemplateFilePath.length());
    }

    // String htmlTemplateFilePath = "C://Users//zhontao//Dessktop//smartEmailTemplate.html";

    MailReceiverFactory factory = MailReceiverFactoryImpl.getInstance();
    MailReceiver receiver = factory.create("Exchange Server");
    String filePath = "C://Users//zhontao//Desktop//201ef8d6-adbf-4282-8ac2-d8b16a5ccd9a.eml";
    String mailMsg = receiver.readMessage(filePath);
    String attachmentList = receiver.readAttachments(filePath);
    String folderPath = EMLToHtmlUtil.generateMailMessageAsHtml(mailMsg, attachmentList, htmlTemplateFilePath);
    System.out.println("Saved to: " + folderPath);
  }
}
