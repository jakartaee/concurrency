/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.DayOfWeek;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class LiteralsTest {

    /**
     * Test the literal default instance for Asynchronous.
     */
    @Test
    void testAsynchronousLiteralDefaultInstance() {
        assertEquals("java:comp/DefaultManagedExecutorService",
                     Asynchronous.Literal.INSTANCE.executor());
        assertEquals(0,
                     Asynchronous.Literal.INSTANCE.runAt().length);
    }

    /**
     * Test the literal of method for Asynchronous.
     */
    @Test
    void testAsynchronousLiteralOf() {
        Schedule[] schedules = new Schedule[] {
                Schedule.Literal.INSTANCE,
                Schedule.Literal.of("",
                                    new Month[] { Month.JULY },
                                    new int[] { 28 },
                                    new DayOfWeek[] { DayOfWeek.THURSDAY },
                                    new int[] { 9 },
                                    new int[] { 1 },
                                    new int[] { 0 },
                                    TimeUnit.MINUTES.toSeconds(3),
                                    "America/Chicago")
        };

        Asynchronous async = Asynchronous.Literal.of(
                "java:comp/concurrent/env/my-executor",
                schedules);

        // modification afterward should not impact the literal
        schedules[0] = null;
        schedules[1] = null;

        assertEquals("java:comp/concurrent/env/my-executor",
                     async.executor());
        assertEquals(2,
                     async.runAt().length);
        assertEquals(Schedule.Literal.INSTANCE,
                     async.runAt()[0]);
        assertEquals("",
                     async.runAt()[1].cron());
        assertEquals(List.of(Month.JULY),
                     List.of(async.runAt()[1].months()));
        assertEquals(List.of(28),
                     Arrays.stream(async.runAt()[1].daysOfMonth())
                           .boxed()
                           .toList());
        assertEquals(List.of(DayOfWeek.THURSDAY),
                     List.of(async.runAt()[1].daysOfWeek()));
        assertEquals(180L,
                     async.runAt()[1].skipIfLateBy());
    }

    /**
     * Test the literal default instance for Lock.
     */
    @Test
    void testLockLiteralDefaultInstance() {
        assertEquals(Lock.Type.WRITE,
                     Lock.Literal.INSTANCE.type());
        assertEquals(60L,
                     Lock.Literal.INSTANCE.accessTimeout());
        assertEquals(TimeUnit.SECONDS,
                     Lock.Literal.INSTANCE.unit());
    }

    /**
     * Test the literal of method for Lock.
     */
    @Test
    void testLockLiteralOf() {
        Lock lock = Lock.Literal.of(Lock.Type.READ,
                                    500,
                                    TimeUnit.MILLISECONDS);

        assertEquals(Lock.Type.READ,
                     lock.type());
        assertEquals(500L,
                     lock.accessTimeout());
        assertEquals(TimeUnit.MILLISECONDS,
                     lock.unit());
    }

    /**
     * Test the literal default instance for Schedule.
     */
    @Test
    void testScheduleLiteralDefaultInstance() {
        assertEquals("",
                     Schedule.Literal.INSTANCE.cron());
        assertEquals(0,
                     Schedule.Literal.INSTANCE.months().length);
        assertEquals(0,
                     Schedule.Literal.INSTANCE.daysOfMonth().length);
        assertEquals(0,
                     Schedule.Literal.INSTANCE.daysOfWeek().length);
        assertEquals(List.of(0),
                     Arrays.stream(Schedule.Literal.INSTANCE.hours())
                           .boxed()
                           .toList());
        assertEquals(List.of(0),
                     Arrays.stream(Schedule.Literal.INSTANCE.minutes())
                           .boxed()
                           .toList());
        assertEquals(List.of(0),
                     Arrays.stream(Schedule.Literal.INSTANCE.seconds())
                           .boxed()
                           .toList());
        assertEquals(600L,
                     Schedule.Literal.INSTANCE.skipIfLateBy());
        assertEquals("",
                     Schedule.Literal.INSTANCE.zone());
    }

    /**
     * Test the literal of method for Schedule.
     */
    @Test
    void testScheduleLiteralOf() {
        Month[]     months     = new Month[]     { Month.MARCH, Month.APRIL };
        int[]       days       = new int[]       { 1, 15 };
        DayOfWeek[] daysOfWeek = new DayOfWeek[] { DayOfWeek.MONDAY };
        int[]       hours      = new int[]       { 8, 20 };
        int[]       minutes    = new int[]       { 30 };
        int[]       seconds    = new int[]       { 0 };

        Schedule schedule = Schedule.Literal.of(
                "",
                months,
                days,
                daysOfWeek,
                hours,
                minutes,
                seconds,
                120L,
                "America/New_York");

        // modification afterward should not impact the literal
        months[0]     = null;
        days[0]       = -1;
        daysOfWeek[0] = null;
        hours[0]      = -1;
        minutes[0]    = -1;
        seconds[0]    = -1;

        assertEquals("",
                     schedule.cron());
        assertEquals(List.of(Month.MARCH, Month.APRIL),
                     List.of(schedule.months()));
        assertEquals(List.of(1, 15),
                     Arrays.stream(schedule.daysOfMonth())
                           .boxed()
                           .toList());
        assertEquals(List.of(DayOfWeek.MONDAY),
                     List.of(schedule.daysOfWeek()));
        assertEquals(List.of(8, 20),
                     Arrays.stream(schedule.hours())
                           .boxed()
                           .toList());
        assertEquals(List.of(30),
                     Arrays.stream(schedule.minutes())
                           .boxed()
                           .toList());
        assertEquals(List.of(0),
                     Arrays.stream(schedule.seconds())
                           .boxed()
                           .toList());
        assertEquals(120L,
                     schedule.skipIfLateBy());
        assertEquals("America/New_York",
                     schedule.zone());
    }

    /**
     * Test the literal of method for Schedule using a cron expression.
     */
    @Test
    void testScheduleLiteralOfCron() {
        Schedule schedule = Schedule.Literal.of(
                "0 9 * * MON-FRI",
                new Month[0],
                new int[0],
                new DayOfWeek[0],
                new int[0],
                new int[0],
                new int[] { 0 },
                300L,
                "America/Chicago");

        assertEquals("0 9 * * MON-FRI",
                     schedule.cron());
        assertEquals(0,
                     schedule.months().length);
        assertEquals(0,
                     schedule.daysOfMonth().length);
        assertEquals(0,
                     schedule.daysOfWeek().length);
        assertEquals(0,
                     schedule.hours().length);
        assertEquals(0,
                     schedule.minutes().length);
        assertEquals(List.of(0),
                     Arrays.stream(schedule.seconds())
                           .boxed()
                           .toList());
        assertEquals(300L,
                     schedule.skipIfLateBy());
        assertEquals("America/Chicago",
                     schedule.zone());
    }

}
