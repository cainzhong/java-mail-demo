/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail;

/**
 * This exception indicates that we can not handle the Message receiving from Mail Server.
 * 
 * @author zhontao
 *
 */
public class UnknownMimeTypeException extends Exception {

  private static final long serialVersionUID = 5683704060365078730L;

  /**
   * Constructs an unknown mime type exception with no detail message. A detail
   * message is a String that describes this particular exception.
   */
  public UnknownMimeTypeException() {
    super();
  }

  /**
   * Constructs an unknown mime type exception with the given detail
   * message. A detail message is a String that describes this
   * particular exception.
   *
   * @param msg
   *          the detail message.
   */
  public UnknownMimeTypeException(String msg) {
    super(msg);
  }

  /**
   * Creates an <code>UnknownMimeTypeException</code> with the specified
   * detail message and cause.
   *
   * @param message
   *          the detail message (which is saved for later retrieval
   *          by the {@link #getMessage()} method).
   * @param cause
   *          the cause (which is saved for later retrieval by the
   *          {@link #getCause()} method). (A <tt>null</tt> value is permitted,
   *          and indicates that the cause is nonexistent or unknown.)
   */
  public UnknownMimeTypeException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates an <code>UnknownMimeTypeException</code> with the specified cause
   * and a detail message of <tt>(cause==null ? null : cause.toString())</tt>
   * (which typically contains the class and detail message of
   * <tt>cause</tt>).
   *
   * @param cause
   *          the cause (which is saved for later retrieval by the
   *          {@link #getCause()} method). (A <tt>null</tt> value is permitted,
   *          and indicates that the cause is nonexistent or unknown.)
   */
  public UnknownMimeTypeException(Throwable cause) {
    super(cause);
  }
}
