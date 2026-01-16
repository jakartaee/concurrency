/*
 * Copyright (c) 2023,2026 Contributors to the Eclipse Foundation
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
import java.util.Arrays;

import jakarta.enterprise.util.AnnotationLiteral;

/**
 * <p>Defines a schedule that indicates when to run a method.</p>
 *
 * <p>For the scheduled asynchronous method to aim to run at a given day and time,
 * all of the criteria specified by the {@code Schedule} must match
 * or be disregarded according to the rules of each field.</p>
 *
 * <h2>Scheduled methods</h2>
 *
 * <p>When the {@code Schedule} annotation is applied to a CDI managed bean method,
 * the schedule defines the times after which to run the method. The method must
 * have a {@code void} return type and no parameters. The bean must not be a
 * Jakarta Enterprise Bean.</p>
 *
 * <p>Upon starting the application, the Jakarta EE Product Provider computes the
 * next time from the {@code Schedule} annotation and schedules a task that aims to
 * run at the computed time on the default {@link ManagedScheduledExecutorService}.
 * After it is time to run the task, the task first checks to ensure that the
 * schedule does not require {@linkplain #skipIfLateBy() skipping} the invocation
 * of the method. If method invocation is not skipped, the task obtains an instance
 * of the bean and invokes the method on the bean instance.</p>
 *
 * <p>After successful or skipped invocation of the method, the task uses the
 * {@code Schedule} annotation to compute the next time.
 * The computed next time is after the method completion time or the time of the skip.
 * The task schedules another task that aims to run after the computed next time.
 * This continues with each
 * invocation of the method until an invocation of the method raises an exception
 * or error or the application stops.
 * </p>
 *
 * <p>For example,</p>
 * <pre>
 *  {@literal @}Schedule(daysOfWeek = { DayOfWeek.SATURDAY, DayOfWeek.SUNDAY },
 *            hours = 8,
 *            minutes = 30,
 *            zone = "America/Chicago")
 *  public void weekendsAtEightThirtyAM() {
 *      System.out.println("Good morning. Today is " + ZonedDateTime.now());
 *  }
 * </pre>
 *
 * <h2>Asynchronous methods with a Schedule</h2>
 *
 * <p>When the {@code Schedule} annotation is used as a value of
 * {@link Asynchronous#runAt()}, the schedule defines the times after which to
 * run the asynchronous method.</p>
 *
 * <p>{@link Asynchronous} methods with a {@code Schedule} annotation can be
 * written to schedule automatically at application startup by observing the
 * application's {@code jakarta.enterprise.Startup} event. For example,
 * </p>
 * <pre>
 *  {@literal @}Asynchronous(runAt = {@literal @}Schedule(cron = "30 8 * * SAT,SUN",
 *                                  zone = "America/Chicago"))
 *  public void weekendsAtEightThirtyAM({@literal @}Observes Startup event) {
 *      System.out.println("Good morning. Today is " + ZonedDateTime.now());
 *  }
 * </pre>
 *
 * <p>Or, asynchronous methods can be written to require invocation in order to
 * apply the schedule. For example,
 * </p>
 * <pre>
 *  {@literal @}Asynchronous(runAt = {@literal @}Schedule(cron = "30 6 * * MON-FRI",
 *                                  zone = "America/Chicago"))
 *  public void weekdaysAtSixThirtyAM(String message) {
 *      System.out.println(message + " Today is " + ZonedDateTime.now());
 *  }
 * </pre>
 *
 * <p>The above method does not run even if a scheduled time is reached until
 * the application manually requests to schedule it by invoking the method,
 * </p>
 *
 * <pre>
 *  if (decidedToScheduleDailyMessage) {
 *      bean.weekdaysAtSixThirtyAM("Good morning!");
 *  }
 * </pre>
 *
 * @since 3.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
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
     *
     * @return the time zone id. Empty string for the system default.
     */
    String zone() default "";

    /**
     * Enables instances of the {@link Schedule} annotation to be created
     * at run time.
     */
    public static final class Literal
            extends AnnotationLiteral<Schedule>
            implements Schedule {

        /**
         * Instance of the {@link Schedule} annotation with all values
         * set to their defaults.
         */
        public static final Literal INSTANCE =
                new Literal("",               // cron not used
                            new Month[0],     // disregard the month
                            new int[0],       // disregard the day of month
                            new DayOfWeek[0], // disregard the day of the week
                            new int[] {0},    // first hour of day (Midnight)
                            new int[] {0},    // first minute of the hour
                            new int[] {0},    // first second of the minute
                            600L,             // skip if late by 600 seconds
                            ""                // system default time zone
                            );

        private static final long serialVersionUID = 1L;

        private final String cron;
        private final Month[] months;
        private final int[] daysOfMonth;
        private final DayOfWeek[] daysOfWeek;
        private final int[] hours;
        private final int[] minutes;
        private final int[] seconds;
        private final long skipIfLateBy;
        private final String zone;

        private Literal(final String cron,
                        final Month[] months,
                        final int[] daysOfMonth,
                        final DayOfWeek[] daysOfWeek,
                        final int[] hours,
                        final int[] minutes,
                        final int[] seconds,
                        final long skipIfLateBy,
                        final String zone) {
            this.cron = cron;
            this.months = months;
            this.daysOfMonth = daysOfMonth;
            this.daysOfWeek = daysOfWeek;
            this.hours = hours;
            this.minutes = minutes;
            this.seconds = seconds;
            this.skipIfLateBy = skipIfLateBy;
            this.zone = zone;
        }

        /**
         * Construct a new instance of the {@link Schedule} annotation.
         *
         * @param cron         Cron expression following the rules of
         *                     {@link CronTrigger}. Empty string to disregard.
         * @param months       Months in which the method aims to run.
         *                     Empty to disregard.
         * @param daysOfMonth  Days of the month (1 to 31) on which the method
         *                     aims to run. Empty to disregard.
         * @param daysOfWeek   Days of the week on which the method aims to run.
         *                     Empty to disregard.
         * @param hours        Hours of the day (0 to 23) at which the method
         *                     aims to run. Empty to disregard.
         * @param minutes      Minutes (0 to 59) at which the method aims to run.
         *                     Empty to disregard.
         * @param seconds      Seconds (0 to 59) at which the method aims to run.
         *                     Must not be empty.
         * @param skipIfLateBy Seconds after which an execution that is late to
         *                     start should be skipped instead of starting late.
         * @param zone         Time zone id, such as {@code America/Chicago},
         *                     of the schedule. Empty string indicates the
         *                     {@link java.time.ZoneId#systemDefault() default time zone}.
         * @return a new instance of the {@code Schedule} annotation.
         */
        public static Literal of(final String cron,
                                 final Month[] months,
                                 final int[] daysOfMonth,
                                 final DayOfWeek[] daysOfWeek,
                                 final int[] hours,
                                 final int[] minutes,
                                 final int[] seconds,
                                 final long skipIfLateBy,
                                 final String zone) {

            if (cron == null) {
                throw new IllegalArgumentException("cron: null");
            }

            if (months == null) {
                throw new IllegalArgumentException("months: null");
            }

            if (daysOfMonth == null) {
                throw new IllegalArgumentException("daysOfMonth: null");
            }

            if (daysOfWeek == null) {
                throw new IllegalArgumentException("daysOfWeek: null");
            }

            if (hours == null) {
                throw new IllegalArgumentException("hours: null");
            }

            if (minutes == null) {
                throw new IllegalArgumentException("minutes: null");
            }

            if (seconds == null) {
                throw new IllegalArgumentException("seconds: null");
            }

            if (zone == null) {
                throw new IllegalArgumentException("zone: null");
            }

            return new Literal(
                    cron,
                    months.length == 0
                            ? months
                            : Arrays.copyOf(months, months.length),
                    daysOfMonth.length == 0
                            ? daysOfMonth
                            : Arrays.copyOf(daysOfMonth, daysOfMonth.length),
                    daysOfWeek.length == 0
                            ? daysOfWeek
                            : Arrays.copyOf(daysOfWeek, daysOfWeek.length),
                    hours.length == 0
                            ? hours
                            : Arrays.copyOf(hours, months.length),
                    minutes.length == 0
                            ? minutes
                            : Arrays.copyOf(minutes, minutes.length),
                    seconds.length == 0
                            ? seconds
                            : Arrays.copyOf(seconds, seconds.length),
                    skipIfLateBy,
                    zone
                    );
        }

        /**
         * <p>Cron expression following the rules of {@link CronTrigger}.</p>
         *
         * @return cron expression indicating when to run the method.
         */
        @Override
        public String cron() {
            return cron;
        }

        /**
         * <p>Months in which the method aims to run.</p>
         *
         * @return list of months in which the method aims to run;
         *         An empty list disregards the month.
         */
        @Override
        public Month[] months() {
            return months.length == 0
                    ? months
                    : Arrays.copyOf(months, months.length);
        }

        /**
         * <p>Days of the month on which the method aims to run.
         * Values can range from 1 to 31.</p>
         *
         * @return list of days of the month on which the method aims to run;
         *         An empty list disregards the day of the month.
         */
        @Override
        public int[] daysOfMonth() {
            return daysOfMonth.length == 0
                    ? daysOfMonth
                    : Arrays.copyOf(daysOfMonth, daysOfMonth.length);
        }

        /**
         * <p>Days of the week on which the method aims to run.</p>
         *
         * @return list of days of the week on which the method aims to run;
         *         An empty list disregards the day of the week.
         */
        @Override
        public DayOfWeek[] daysOfWeek() {
            return daysOfWeek.length == 0
                    ? daysOfWeek
                    : Arrays.copyOf(daysOfWeek, daysOfWeek.length);
        }

        /**
         * <p>Hours of the day at which the method aims to run.
         * Values can range from 0 to 23.</p>
         *
         * @return list of hours at which the method aims to run;
         *         An empty list disregards the hour.
         */
        @Override
        public int[] hours() {
            return hours.length == 0
                    ? hours
                    : Arrays.copyOf(hours, hours.length);
        }

        /**
         * <p>Minutes at which the method aims to run.
         * Values can range from 0 to 59.</p>
         *
         * @return list of minutes at which the method aims to run;
         *         An empty list disregards the minutes.
         */
        @Override
        public int[] minutes() {
            return minutes.length == 0
                    ? minutes
                    : Arrays.copyOf(minutes, minutes.length);
        }

        /**
         * <p>Seconds at which the method aims to run.
         * Values can range from 0 to 59.</p>
         *
         * @return list of seconds at which the method aims to run.
         */
        @Override
        public int[] seconds() {
            return seconds.length == 0
                    ? seconds
                    : Arrays.copyOf(seconds, seconds.length);
        }

        /**
         * <p>Seconds after which an execution that is late to start should be
         * skipped rather than starting late. Values must be greater than 0.
         * </p>
         *
         * @return the threshold for skipping executions that are late to start.
         */
        @Override
        public long skipIfLateBy() {
            return skipIfLateBy;
        }

        /**
         * <p>Time zone id, such as {@code America/Chicago},
         * which identifies the time zone of the schedule.</p>
         *
         * @return the time zone id. Empty string indicates to use the
         *         {@link java.time.ZoneId#systemDefault() default time zone}.
         */
        @Override
        public String zone() {
            return zone;
        }
    }
}
