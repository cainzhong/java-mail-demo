/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail;

/**
 * This exception indicates that something exceed the volume that our application can not handle.
 * 
 * @author zhontao
 *
 */
public class ExceedException extends Exception {

  private static final long serialVersionUID = -6697748962289002521L;

  /**
   * Constructs an exceed exception with no detail message. A detail
   * message is a String that describes this particular exception.
   */
  public ExceedException() {
    super();
  }

  /**
   * Constructs an exceed exception with the given detail
   * message. A detail message is a String that describes this
   * particular exception.
   *
   * @param msg
   *          the detail message.
   */
  public ExceedException(String msg) {
    super(msg);
  }

  /**
   * Creates a <code>ExceedException</code> with the specified
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
  public ExceedException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a <code>ExceedException</code> with the specified cause
   * and a detail message of <tt>(cause==null ? null : cause.toString())</tt>
   * (which typically contains the class and detail message of
   * <tt>cause</tt>).
   *
   * @param cause
   *          the cause (which is saved for later retrieval by the
   *          {@link #getCause()} method). (A <tt>null</tt> value is permitted,
   *          and indicates that the cause is nonexistent or unknown.)
   */
  public ExceedException(Throwable cause) {
    super(cause);
  }
}
