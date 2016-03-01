package com.java.mail.domain;

public enum MailStatus {
  No_Message("No Message in Mail Box", 100), Invalid_Mail_Message("1. The attachments' quantity may exceed the maximum value! 2. The size of all the attachments exceed the maximum value!", 101), Certificate_Error("Fail to verify the Certificate.",
      102), Move_Message_Successfully("Move message successfully", 103), Fail_To_Move_Message("Fail to move message, May the folder is null or closed.", 104), Unkonwn_MIME_Type("It is an unkonwn MIME type. Can not handle the Message receiving from Mail Server.",
          105), Encrypted_Mail(
              "It's an encrypted mail. Can not handle the Message receiving from Mail Server.",
              106), Protocol_Missing("Missing mandatory values, please check that you have entered the protocol.",
                  107), UP_Missing("Missing mandatory values, please check that you have entered the username, password.",
                      108), HPUP_Missing("Missing mandatory values, please check that you have entered the host, port, username or password.", 109), Not_Authorised_User(
                          "The user name is not belong to authorised user domain!", 110), Initialize_Successfully("Initialize successfully.",
                              111), Receive_Attachment_Successfully("Receive attachment successfully", 112), Fail_To_Receive_Attachment("Fail to receive attachment",
                                  113), Delete_Attachment_Successfully("Delete attachment successfully", 114), Fail_To_Delete_Attachment("Fail to delete attachment", 115), OK("OK", 116), Missing_Value("May be the JSON Arguments is null", 117);
  private String desc;
  private int code;
  private MailStatus(String desc,int code){
    this.desc=desc;
    this.code=code;
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
