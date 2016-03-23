/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail.domain;

/**
 * @author zhontao
 *
 */
public class Attachment {
  /* an attachment's file name for a email. */
  private String fileName;

  /* an attachment's file path for a email. */
  private String filePath;

  /* an attachment's file type for a email. */
  private String fileType;

  /* an attachment's file size for a email. */
  private int fileSize;

  public String getFileName() {
    return this.fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFilePath() {
    return this.filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getFileType() {
    return this.fileType;
  }

  public void setFileType(String fileType) {
    this.fileType = fileType;
  }

  public int getFileSize() {
    return this.fileSize;
  }

  public void setFileSize(int fileSize) {
    this.fileSize = fileSize;
  }
}
