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

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

/**
 * Exception indicating that the result of a value-producing task cannot be 
 * retrieved because the task run was skipped. A task can be skipped if the 
 * {@link Trigger#skipRun(javax.enterprise.concurrent.LastExecution, java.util.Date)} 
 * method returns true or if it throws an unchecked exception.
 * <p>
 * Use the {@link Throwable#getCause()} method to determine if an unchecked 
 * exception was thrown from the Trigger.
 * 
 * @since 1.0
 */
public class SkippedException extends ExecutionException implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 6296866815328432550L;

  /**
   * Constructs an SkippedException with null as its detail message. The cause is not 
   * initialized, and may subsequently be initialized by a call to 
   * {@link Throwable#initCause(java.lang.Throwable)}. 
   */
  public SkippedException() {
	super();
  }
  
  /**
   * Constructs an SkippedException exception with the specified detail message.
   * <p>
   * The cause is not initialized, and may subsequently be initialized by a 
   * call to {@link Throwable#initCause(java.lang.Throwable)}.
   *  
   * @param message the detail message (which is saved for later retrieval by 
   *                the {@link Throwable#getMessage()} method).
   */
  public SkippedException(java.lang.String message) {
	super(message);
  }
  
  /**
   * Constructs an SkippedException exception with the specified detail message
   * and cause.
   * <p>
   * Note that the detail message associated with cause is not automatically 
   * incorporated in this exception's detail message. 
   * 
   * @param message the detail message (which is saved for later retrieval by 
   *                the {@link Throwable#getMessage()} method).
   * @param cause the cause (which is saved for later retrieval by the 
   *              {@link Throwable#getCause()} method). 
   *              (A null value is permitted, and indicates that the cause is 
   *              nonexistent or unknown.)
   */
  public SkippedException(java.lang.String message,
                          java.lang.Throwable cause) {
	super(message, cause);
  }

  /**
   * Constructs an SkippedException exception with the specified cause and a 
   * detail message of (cause==null ? null : cause.toString()) 
   * (which typically contains the class and detail message of cause).
   *  
   * @param cause the cause (which is saved for later retrieval by the 
   *              {@link Throwable#getCause()} method). 
   *              (A null value is permitted, and indicates that the cause is 
   *              nonexistent or unknown.)
   */
  public SkippedException(java.lang.Throwable cause) {
	super(cause);
  }

}
