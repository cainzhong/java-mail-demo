package com.http.proxy;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class ViaHTTP {
  public static void main(String[] arg) {

    String proxy_host;
    int proxy_port;

    try {
      JSch jsch = new JSch();

      String host = null;
      if (arg.length > 0) {
        host = arg[0];
      } else {
        host = JOptionPane.showInputDialog("Enter username@hostname", System.getProperty("user.name") + "@localhost");
      }
      String user = host.substring(0, host.indexOf('@'));
      host = host.substring(host.indexOf('@') + 1);

      Session session = jsch.getSession(user, host, 22);

      String proxy = JOptionPane.showInputDialog("Enter proxy server", "hostname:port");
      proxy_host = proxy.substring(0, proxy.indexOf(':'));
      proxy_port = Integer.parseInt(proxy.substring(proxy.indexOf(':') + 1));

      session.setProxy(new ProxyHTTP(proxy_host, proxy_port));

      // username and password will be given via UserInfo interface.
      UserInfo ui = new MyUserInfo();
      session.setUserInfo(ui);

      session.connect();

      Channel channel = session.openChannel("shell");

      channel.setInputStream(System.in);
      channel.setOutputStream(System.out);

      channel.connect();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public static class MyUserInfo implements UserInfo, UIKeyboardInteractive {
    @Override
    public String getPassword() {
      return this.passwd;
    }

    @Override
    public boolean promptYesNo(String str) {
      Object[] options = { "yes", "no" };
      int foo = JOptionPane.showOptionDialog(null, str, "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
      return foo == 0;
    }

    String passwd;
    JTextField passwordField = new JPasswordField(20);

    @Override
    public String getPassphrase() {
      return null;
    }

    @Override
    public boolean promptPassphrase(String message) {
      return true;
    }

    @Override
    public boolean promptPassword(String message) {
      Object[] ob = { this.passwordField };
      int result = JOptionPane.showConfirmDialog(null, ob, message, JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION) {
        this.passwd = this.passwordField.getText();
        return true;
      } else {
        return false;
      }
    }

    @Override
    public void showMessage(String message) {
      JOptionPane.showMessageDialog(null, message);
    }

    final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
    private Container panel;

    @Override
    public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
      this.panel = new JPanel();
      this.panel.setLayout(new GridBagLayout());

      this.gbc.weightx = 1.0;
      this.gbc.gridwidth = GridBagConstraints.REMAINDER;
      this.gbc.gridx = 0;
      this.panel.add(new JLabel(instruction), this.gbc);
      this.gbc.gridy++;

      this.gbc.gridwidth = GridBagConstraints.RELATIVE;

      JTextField[] texts = new JTextField[prompt.length];
      for (int i = 0; i < prompt.length; i++) {
        this.gbc.fill = GridBagConstraints.NONE;
        this.gbc.gridx = 0;
        this.gbc.weightx = 1;
        this.panel.add(new JLabel(prompt[i]), this.gbc);

        this.gbc.gridx = 1;
        this.gbc.fill = GridBagConstraints.HORIZONTAL;
        this.gbc.weighty = 1;
        if (echo[i]) {
          texts[i] = new JTextField(20);
        } else {
          texts[i] = new JPasswordField(20);
        }
        this.panel.add(texts[i], this.gbc);
        this.gbc.gridy++;
      }

      if (JOptionPane.showConfirmDialog(null, this.panel, destination + ": " + name, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
        String[] response = new String[prompt.length];
        for (int i = 0; i < prompt.length; i++) {
          response[i] = texts[i].getText();
        }
        return response;
      } else {
        return null; // cancel
      }
    }
  }

}