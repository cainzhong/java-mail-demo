package com.main;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

public class CreateFolderTest {

  public static void main(String[] args) {
    CreateFolderTest createFolder = new CreateFolderTest();
    try {
      createFolder.createFolder("bbb");
    } catch (MessagingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public boolean createFolder(String folderName) throws MessagingException {
    String host = "webmail.hp.com";
    int port = 993;
    String user = "tao.zhong@hpe.com";
    String password = "Cisco01!";
    Properties props = System.getProperties();
    Session session = Session.getInstance(props, null);
    Store store = session.getStore("imaps");
    System.out.println("connecting store..");
    store.connect(host, port, user, password);
    System.out.println("connected !");
    // Folder defaultFolder = store.getDefaultFolder();
    String target_folder = "INBOX";
    Folder defaultFolder = store.getFolder(target_folder);
    return this.createFolder(defaultFolder, folderName);
  }

  private boolean createFolder(Folder parent, String folderName) {
    boolean isCreated = true;

    try {
      // parent.open(Folder.READ_WRITE); //根目录的时候不需要open,不是根目录，就要open
      System.out.println("creating a folder ....");
      Folder newFolder = parent.getFolder(folderName);

      isCreated = newFolder.create(Folder.HOLDS_MESSAGES);
      System.out.println("created: " + isCreated);
      // parent.close(true);
    } catch (Exception e) {
      System.out.println("Error creating folder: " + e.getMessage());
      isCreated = false;
    }
    return isCreated;
  }

}
