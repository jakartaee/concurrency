/*
 * Copyright (c) 2023,2024 Contributors to the Eclipse Foundation
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
 * <p>For the scheduled asychronous method to aim to run at a given day and time,
 * all of the criteria specified by the {@code Schedule} must match
 * or be disregarded according to the rules of each field.</p>
 *
 * @since 3.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Schedule {
    /**
     * <p>Cron expression following the rules of {@link CronTrigger}.</p>
     *
     * <p>When a non-empty value is specified, it overrides all values
     * that are specified for
     * {@link months}, {@link daysOfMonth}, {@link daysOfWeek},
     * {@link hours}, {@link minutes}, and {@link seconds}.</p>
     *
     * <p>The default value is the empty string, indicating that
     * no cron expression is to be used.</p>
     *
     * @return cron expression indicating when to run the asynchronous method.
     */
    String cron() default "";

    /**
     * <p>Months in which the asynchronous method aims to run.</p>
     *
     * <p>The default value is an empty list, which means that the month is
     * not included in the criteria for deciding when to run the asynchronous method.</p>
     *
     * @return list of months in which the asynchronous method aims to run; An empty list disregards the month.
     */
    Month[] months() default {};

    /**
     * <p>Days of the month on which the asynchronous method aims to run. Values can range from 1 to 31.</p>
     *
     * <p>The default value is an empty list, which means that the day of the month is
     * not included in the criteria for deciding when to run the asynchronous method.</p>
     *
     * @return list of days of the month on which the asynchronous method aims to run; An empty list disregards the day of the month.
     */
    int[] daysOfMonth() default {};

    /**
     * <p>Days of the week on which the asynchronous method aims to run.</p>
     *
     * <p>The default value is an empty list, which means that the day of the week is
     * not included in the criteria for deciding when to run the asynchronous method.</p>
     *
     * @return list of days of the week on which the asynchronous method aims to run; An empty list disregards the day of the week.
     */
    DayOfWeek[] daysOfWeek() default {};

    /**
     * <p>Hours of the day at which the asynchronous method aims to run.</p>
     *
     * <p>Values can range from 0 to 23. A value of empty list indicates that the
     * hour is not included in the criteria for deciding when to run the asynchronous method.</p>
     *
     * <p>The default value is 0 (midnight).</p>
     *
     * @return list of hours at which the asynchronous method aims to run; An empty list disregards the hour.
     */
    int[] hours() default { 0 };

    /**
     * <p>Minutes at which the asynchronous method aims to run.</p>
     *
     * <p>Values can range from 0 to 59. A value of empty list indicates that the
     * minute is not included in the criteria for deciding when to run the asynchronous method.</p>
     *
     * <p>The default value is 0 (at the start of the hour).</p>
     *
     * @return list of minutes at which the asynchronous method aims to run; An empty list disregards the minutes.
     */
    int[] minutes() default { 0 };

    /**
     * <p>Seconds at which the asynchronous method aims to run.</p>
     *
     * <p>Values can range from 0 to 59. A value of empty list causes the asynchronous method
     * to raise {@link IllegalArgumentException}.</p>
     *
     * <p>The default value is 0 (at the start of the minute).</p>
     *
     * @return list of seconds at which the asynchronous method aims to run.
     */
    int[] seconds() default { 0 };

    /**
     * <p>Seconds after which an execution that is late to start should be skipped
     * rather than starting it late.</p>
     *
     * <p>Values must be greater than 0.
     * The default value is 600 seconds (10 minutes).
     * This differs from executions that are missed due to overlap, which are always skipped.</p>
     *
     * @return the threshold for skipping executions that are late to start.
     */
    long skipIfLateBy() default 600L;

    /**
     * <p>Time zone id, such as {@code America/Chicago} or {@code America/Los_Angeles},
     * which identifies the time zone of the schedule.</p>
     *
     * <p>The default value of empty string indicates to use the
     * {@link java.time.ZoneId#systemDefault() default time zone}
     * for the system.</p>
     */
    String zone() default "";
}
