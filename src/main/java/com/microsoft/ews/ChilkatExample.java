package com.microsoft.ews;

import com.chilkatsoft.CkCrypt2;

public class ChilkatExample {
  static {
    try {
      System.loadLibrary("chilkat");
    } catch (UnsatisfiedLinkError e) {
      System.err.println("Native code library failed to load.\n" + e);
      System.exit(1);
    }
  }

  public static void main(String argv[]) {
    CkCrypt2 crypt = new CkCrypt2();

    // Any string argument automatically begins the 30-day trial.
    boolean success = crypt.UnlockComponent("30-day trial");
    if (success != true) {
      System.out.println(crypt.lastErrorText());
      return;
    }

    String outputFile = "/Users/chilkat/testData/pdf/sample.pdf";
    String inFile = "/Users/chilkat/testData/p7m/sample.pdf.p7m";

    // Verify and restore the original file:
    success = crypt.VerifyP7M(inFile, outputFile);
    if (success == false) {
      System.out.println(crypt.lastErrorText());
      return;
    }

    System.out.println("Success!");

  }
}
