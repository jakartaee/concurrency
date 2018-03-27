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

import java.util.Date;

/**
 * Contains information about the last execution of a task. This is used by
 * the methods in the {@code Trigger} class to determine the next scheduled timeout
 * or whether a run should be skipped.
 *
 * @since 1.0
 */
public interface LastExecution {
    
  /**
   * The name or ID of the identifiable object, as specified in the
   * {@link ManagedTask#IDENTITY_NAME} execution property of the task if
   * it also implements the {@code ManagedTask} interface.
   * 
   * @return the name or ID of the identifiable object.
   */
  public String getIdentityName();

  /**
    * Result of the last execution.
    * 
    * @return The result of the last execution. It could return null if
    *         the last execution did not complete, or the result of the task
    *         was null.
    */
   public Object getResult();

   /**
    * The last time in which task was scheduled to run.
    * 
    * @return The last date/time in which the task was scheduled to run.
    */
   public Date getScheduledStart();

   /** 
    * The last time in which the task started running. 
    * 
    * @return the last date/time in which the task started running, or 
    *         null if the task was canceled before it was started.
    */
   public Date getRunStart();

   /** 
    * The last time in which the task was completed. 
    * 
    * @return the last date/time in which the task was completed, or 
    *         null if the task was canceled before it was completed.
    */
   public Date getRunEnd();
}
