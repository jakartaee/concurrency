/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
 * Triggers allow application developers to plug in rules for when 
 * and how often a task should run. The trigger can be as simple as 
 * a single, absolute date-time or can include Jakarta&trade; EE business 
 * calendar logic. A trigger implementation is created by the 
 * application developer (or may be supplied to the application 
 * externally) and is registered with a task when it is submitted 
 * to a {@link ManagedScheduledExecutorService} using any of the 
 * schedule methods. Each method will run with unspecified context. 
 * The methods can be made contextual through creating contextual
 * proxy objects using {@link ContextService}.
 * <p>
 * Each trigger instance will be invoked within the same process 
 * in which it was registered.
 * <p>
 * 
 * Example:
 * <pre>
 * &#47;**
 *  * A trigger that runs on the hour, Mon-Fri from 8am-8pm Central US time.
 *  *&#47;
 *  public class HourlyDuringBusinessHoursTrigger implements ZonedTrigger {
 *      static final ZoneId ZONE = ZoneId.of("America/Chicago");
 *
 *      public ZoneId getZoneId() {
 *          return ZONE;
 *      }
 *
 *      public ZonedDateTime getNextRunTime(LastExecution lastExec, ZonedDateTime taskScheduledTime) {
 *          ZonedDateTime prevTime = lastExec == null ? taskScheduledTime : lastExec.getRunEnd(ZONE);
 *          ZonedDateTime nextTime = prevTime.truncatedTo(ChronoUnit.HOURS).plusHours(1);
 *          DayOfWeek day = nextTime.getDayOfWeek();
 *          if (day.equals(DayOfWeek.SATURDAY) || day.equals(DayOfWeek.SUNDAY)) {
 *              nextTime = nextTime.truncatedTo(ChronoUnit.DAYS).plusDays(1).withHour(8);
 *          } else { // Mon-Fri 8am-8pm
 *              int hour = nextTime.getHour();
 *              if (hour{@literal <} 8)
 *                  nextTime = nextTime.plusHours(8 - hour);
 *              else if (hour{@literal >} 20)
 *                  nextTime = nextTime.truncatedTo(ChronoUnit.DAYS)
 *                                     .plusDays(day.equals(DayOfWeek.FRIDAY) ? 3 : 1)
 *                                     .withHour(8);
 *          }
 *          return nextTime;
 *      }
 *  }
 * </pre>
 * <P>
 *
 * @since 3.0
 */
public interface ZonedTrigger extends Trigger {
    /**
     * Retrieve the next time that the task should run after.
     *
     * @param lastExecutionInfo information about the last execution of the task. 
     *                          This value will be null if the task has not yet run.
     * @param taskScheduledTime the date/time at which the
     *                          {@code ManagedScheduledExecutorService.schedule}
     *                          method was invoked to schedule the task.
     * @return the date/time after which the next execution of the task should start.
     */
    public ZonedDateTime getNextRunTime(LastExecution lastExecutionInfo, ZonedDateTime taskScheduledTime);

    /**
     * Retrieve the next time that the task should run after.
     * <P>
     * This method is provided to maintain compatibility with {@link Trigger} and should not be
     * implemented. The default implementation delegates to the method signature that
     * accepts and returns <code>ZonedDateTime</code>.
     *
     * @param lastExecutionInfo information about the last execution of the task. 
     *                          This value will be null if the task has not yet run.
     * @param taskScheduledTime the date/time at which the
     *                          {@code ManagedScheduledExecutorService.schedule}
     *                          method was invoked to schedule the task.
     * @return the date/time after which the next execution of the task should start.
     * @throws IllegalArgumentException if the next run time is too large to represent as a <code>Date</code>.
     */
    public default Date getNextRunTime(LastExecution lastExecutionInfo, Date taskScheduledTime) {
        ZonedDateTime nextTime = getNextRunTime(lastExecutionInfo, taskScheduledTime.toInstant().atZone(getZoneId()));
        return Date.from(nextTime.toInstant());
    }

    /**
     * Returns the timezone to use for the
     * {@link java.time.ZonedDateTime ZonedDateTime} that is supplied to the
     * {@link #getNextRunTime(LastExecution, java.time.ZonedDateTime) getNextRunTime} and
     * {@link #skipRun(LastExecution, java.time.ZonedDateTime) skipRun} methods.
     * <P>
     * The default implementation returns the system default timezone
     * and should be overridden whenever there is chance that the server
     * might not be running with the same timezone for which the
     * business logic within this trigger is written.
     *
     * @return timezone to use for operations on this trigger.
     */
    public default ZoneId getZoneId() {
        return ZoneId.systemDefault();
    }

    /**
     * Return true if this run instance should be skipped.
     * <p>
     * This is useful if the task shouldn't run because it is late or if the task 
     * is paused or suspended.
     * <p>
     * Once this task is skipped, the state of its Future's result will throw a 
     * {@link SkippedException}. Unchecked exceptions will be wrapped in a 
     * <code>SkippedException</code>.
     * <p>
     * The default implementation returns <code>false</code>, making it optional to
     * implement this method if you do not require support for skipped executions.
     *
     * @param lastExecutionInfo information about the last execution of the task. 
     *                          This value will be null if the task has not yet run.
     * @param scheduledRunTime  the date/time after which the execution of the task
     *                          is scheduled to start.
     * @return true if the task should be skipped and rescheduled.
     */
    public default boolean skipRun(LastExecution lastExecutionInfo, ZonedDateTime scheduledRunTime) {
        return false;
    }

    /**
     * Return true if this run instance should be skipped.
     * <P>
     * This method is provided to maintain compatibility with {@link Trigger} and should not be
     * implemented. The default implementation delegates to the method signature that
     * accepts <code>ZonedDateTime</code>.
     *
     * @param lastExecutionInfo information about the last execution of the task. 
     *                          This value will be null if the task has not yet run.
     * @param scheduledRunTime  the date/time after which the execution of the task
     *                          is scheduled to start.
     * @return true if the task should be skipped and rescheduled.
     */
    public default boolean skipRun(LastExecution lastExecutionInfo, Date scheduledRunTime) {
        return skipRun(lastExecutionInfo, scheduledRunTime.toInstant().atZone(getZoneId()));
    }
}
