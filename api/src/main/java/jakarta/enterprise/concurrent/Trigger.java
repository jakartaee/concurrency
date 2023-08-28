/*
 * Copyright (c) 2010, 2023 Oracle and/or its affiliates. All rights reserved.
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

/**
 * Triggers allow application developers to plug in rules for when
 * and how often a task should run. The trigger can be as simple as
 * a single, absolute date-time or can include Jakarta&trade; EE business
 * calendar logic. A Trigger implementation is created by the
 * application developer (or may be supplied to the application
 * externally) and is registered with a task when it is submitted
 * to a {@link ManagedScheduledExecutorService} using any of the
 * schedule methods. Each method will run with unspecified context.
 * The methods can be made contextual through creating contextual
 * proxy objects using {@link ContextService}.
 * <p>
 * Each Trigger instance will be invoked within the same process
 * in which it was registered.
 * <p>
 *
 * Example:
 * <pre>
 * &#47;**
 *  * A trigger that only returns a single date.
 *  *&#47;
 *  public class SingleDateTrigger implements Trigger {
 *      private Date fireTime;
 *
 *      public TriggerSingleDate(Date newDate) {
 *          fireTime = newDate;
 *      }
 *
 *      public Date getNextRunTime(
 *         LastExecution lastExecutionInfo, Date taskScheduledTime) {
 *
 *         if(taskScheduledTime.after(fireTime)) {
 *             return null;
 *         }
 *         return fireTime;
 *      }
 *
 *      public boolean skipRun(LastExecution lastExecutionInfo, Date scheduledRunTime) {
 *          return scheduledRunTime.after(fireTime);
 *      }
 *  }
 *
 * &#47;**
 *  * A fixed-rate trigger that will skip any runs if
 *  * the latencyAllowance threshold is exceeded (the task
 *  * ran too late).
 *  *&#47;
 *  public class TriggerFixedRateLatencySensitive implements Trigger {
 *      private Date startTime;
 *      private long delta;
 *      private long latencyAllowance;
 *
 *      public TriggerFixedRateLatencySensitive(Date startTime, long delta, long latencyAllowance) {
 *          this.startTime = startTime;
 *          this.delta = delta;
 *          this.latencyAllowance = latencyAllowance;
 *      }
 *
 *      public Date getNextRunTime(LastExecution lastExecutionInfo,
 *                                 Date taskScheduledTime) {
 *          if(lastExecutionInfo==null) {
 *              return startTime;
 *          }
 *          return new Date(lastExecutionInfo.getScheduledStart().getTime() + delta);
 *      }
 *
 *      public boolean skipRun(LastExecution lastExecutionInfo, Date scheduledRunTime) {
 *          return System.currentTimeMillis() - scheduledRunTime.getTime() &gt; latencyAllowance;
 *      }
 *  }
 *
 * </pre>
 * <P>
 *
 * @since 1.0
 */
public interface Trigger {

  /**
   * Retrieve the next time that the task should run after.
   *
   * @param lastExecutionInfo information about the last execution of the task.
   *                   This value will be null if the task has not yet run.
   * @param taskScheduledTime the date/time at which the
   *                          {@code ManagedScheduledExecutorService.schedule}
   *                          method was invoked to schedule the task.
   * @return the date/time after which the next execution of the task should start.
   */
  public java.util.Date getNextRunTime(LastExecution lastExecutionInfo,
                                       java.util.Date taskScheduledTime);
  
  /**
   * Return true if this run instance should be skipped.
   * <p>
   * This is useful if the task shouldn't run because it is late or if the task
   * is paused or suspended.
   * <p>
   * Once this task is skipped, the state of its Future's result will throw a
   * {@link SkippedException}. Unchecked exceptions will be wrapped in a
   * <code>SkippedException</code>.
   *
   * @param lastExecutionInfo information about the last execution of the task.
   *                   This value will be null if the task has not yet run.
   * @param scheduledRunTime the date/time after which this execution of the task
   *                         is scheduled to start.
   * @return true if the task should be skipped and rescheduled.
   */
  public boolean skipRun(LastExecution lastExecutionInfo,
                         java.util.Date scheduledRunTime);
  
}
