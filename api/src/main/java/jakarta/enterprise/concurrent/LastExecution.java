/*
 * Copyright (c) 2010, 2021 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.enterprise.concurrent;

import java.time.ZonedDateTime;
import java.time.ZoneId;
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
    * <p>
    * The default implementation delegates to the method signature that
    * accepts a <code>ZoneId</code>.
    *
    * @return The last date/time in which the task was scheduled to run.
    */
   public default Date getScheduledStart() {
       return Date.from(getScheduledStart(ZoneId.systemDefault()).toInstant());
   }

   /**
    * The time, in the specified time-zone, at which the
    * most recent execution of the task was expected to start, per its schedule.
    *
    * @param zone time-zone ID.
    * @return the date/time, in the specified time-zone, at which the
    * most recent execution of the task was expected to start, per its schedule.
    * @since 3.0
    */
   public ZonedDateTime getScheduledStart(ZoneId zone);

   /**
    * The last time in which the task started running.
    * <p>
    * The default implementation delegates to the method signature that
    * accepts a <code>ZoneId</code>.
    *
    * @return the last date/time in which the task started running, or
    *         null if the task was canceled before it was started.
    */
   public default Date getRunStart() {
       ZonedDateTime runStart = getRunStart(ZoneId.systemDefault());
       return runStart == null ? null : Date.from(runStart.toInstant());
   }

   /**
    * The time, in the specified time-zone, at which the
    * most recent execution of the task started running.
    *
    * @param zone time-zone ID.
    * @return the date/time, in the specified time-zone, at which the
    *         most recent execution of the task started running, or
    *         null if the task was canceled before it was started.
    * @since 3.0
    */
   public ZonedDateTime getRunStart(ZoneId zone);

   /**
    * The last time in which the task was completed.
    * <p>
    * The default implementation delegates to the method signature that
    * accepts a <code>ZoneId</code>.
    *
    * @return the last date/time in which the task was completed, or
    *         null if the task was canceled before it was completed.
    */
   public default Date getRunEnd() {
       ZonedDateTime runEnd = getRunEnd(ZoneId.systemDefault());
       return runEnd == null ? null : Date.from(runEnd.toInstant());
   }

   /**
    * The time, in the specified time-zone, at which the
    * most recent execution of the task completed running.
    *
    * @param zone time-zone ID.
    * @return the date/time, in the specified time-zone, at which the
    *         most recent execution of the task completed, or
    *         null if the task was canceled before it was completed.
    * @since 3.0
    */
   public ZonedDateTime getRunEnd(ZoneId zone);
}
