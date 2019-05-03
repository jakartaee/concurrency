/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package javax.enterprise.concurrent;

import java.util.concurrent.ExecutionException;

/**
 * Exception indicating that the result of a task cannot be retrieved 
 * because the task failed to run for some reason other than being cancelled.
 * <p>
 * Use the {@link Throwable#getCause()} method to determine why the task was aborted.
 *
 * @since 1.0
 */
public class AbortedException extends ExecutionException implements java.io.Serializable {

  private static final long serialVersionUID = -8248124070283019190L;

/**
   * Constructs an AbortedException with <code>null</code> as its detail message. 
   * 
   * The cause is not initialized, and may subsequently be initialized by a call to 
   * {@link Throwable#initCause(java.lang.Throwable)}.
   */
  public AbortedException() {
	super();
  }

  /**
   * Constructs an AbortedException exception with the specified detail message and cause.
   * <p>
   * Note that the detail message associated with cause is not automatically incorporated 
   * in this exception's detail message.
   *  
   * @param message the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()}
   *                method).
   * @param cause the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). 
   *              (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public AbortedException(String message, Throwable cause) {
	super(message, cause);  
  }
  
  /**
   * Constructs an AbortedException exception with the specified detail message.
   * <p>
   * The cause is not initialized, and may subsequently be initialized by a call to 
   * {@link Throwable#initCause(java.lang.Throwable)}.
   *  
   * @param message the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()}
   *                method).
   */
  public AbortedException(String message) {
	super(message);  
  }
  
  /**
   * Constructs an AbortedException exception with the specified cause and a detail message of 
   * (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause).
   * 
   * @param cause the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). 
   *              (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public AbortedException(Throwable cause) {
	super(cause);  
  }
}
