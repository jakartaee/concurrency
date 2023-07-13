/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.DayOfWeek;
import java.time.Month;

/**
 * <p>Defines schedules for
 * {@link Asynchronous#runAt() scheduled asynchronous methods}.</p>
 *
 * @since 3.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Schedule {
    /**
     * Cron expression following the rules of {@link CronTrigger}.
     * When a non-empty value is specified, it overrides all values
     * that are specified for
     * month, dayOfMonth, dayOfWeek, hours, minutes, and seconds.
     * The default value is the empty-string, indicating that
     * no cron expression is to be used.
     *
     * @return cron expression indicating when to run the task.
     */
    String cron() default "";

    /**
     * Months in which the task aims to runs.
     * The default value is every month.
     *
     * @return list of months in which the task aims to run.
     */
    Month[] months() default {
        Month.JANUARY, Month.FEBRUARY, Month.MARCH,
        Month.APRIL, Month.MAY, Month.JUNE,
        Month.JULY, Month.AUGUST, Month.SEPTEMBER,
        Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER };

    /**
     * Days of the month on which the task aims to runs. Values can range from 1 to 31.
     * The default value is every day of the month.
     *
     * @return list of days of the month on which the task aims to run.
     */
    int[] daysOfMonth() default {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
        21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
        31
    };

    /**
     * Days of the week on which the task aims to runs.
     * The default value is every day of the week.
     *
     * @return list of days of the month on which the task aims to run.
     */
    DayOfWeek[] daysOfWeek() default {
        DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY
    };

    /**
     * Hours of the day at which the task aims to runs. Values can range from 0 to 23.
     * The default value is 0 (midnight).
     *
     * @return list of hours at which the task aims to run.
     */
    int[] hours() default { 0 };

    /**
     * Minutes at which the task aims to runs. Values can range from 0 to 59.
     * The default value is 0 (at the start of the hour).
     *
     * @return list of minutes at which the task aims to run.
     */
    int[] minutes() default { 0 };

    /**
     * Seconds at which the task aims to runs. Values can range from 0 to 59.
     * The default value is 0 (at the start of the minute).
     *
     * @return list of seconds at which the task aims to run.
     */
    int[] seconds() default { 0 };

    /**
     * Seconds after which an execution that is late to start should be skipped
     * rather than starting it late. Values must be greater than 0.
     * The default value is 600 seconds (10 minutes).
     * This differs from executions that are missed due to overlap, which are always skipped.
     *
     * @return the threshold for skipping executions that are late to start.
     */
    long skipIfLateBy() default 600L;

    /**
     * Time zone id, such as America/Chicago or America/Los_Angeles,
     * which identifies the time zone of the schedule.
     * The default value of empty string indicates to use the
     * {@link java.time.ZoneId#systemDefault() default time zone}
     * for the system.
     */
    String zone() default "";
}
