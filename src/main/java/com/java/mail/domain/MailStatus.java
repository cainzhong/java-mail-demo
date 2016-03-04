package com.java.mail.domain;

/**
 * @author zhontao
 *
 */
public enum MailStatus {
  No_Message("No Message in Mail Box", 100), Invalid_Mail_Message("1. The attachments' quantity may exceed the maximum value! 2. The size of all the attachments exceed the maximum value!", 101), Certificate_Error("Fail to verify the Certificate.", 102), Unkonwn_MIME_Type("It is an unkonwn MIME type. Can not handle the Message receiving from Mail Server.", 105), Encrypted_Mail("It's an encrypted mail. Can not handle the Message receiving from Mail Server.",
      106), Receive_Attachment_Successfully("Receive attachment successfully", 112), Fail_To_Receive_Attachment("Fail to receive attachment", 113), OK("OK", 116);
  private String desc;
  private int code;

  private MailStatus(String desc, int code) {
    this.desc = desc;
    this.code = code;
  }

  public String getDesc() {
    return this.desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public int getCode() {
    return this.code;
  }

  public void setCode(int code) {
    this.code = code;
  }
}
