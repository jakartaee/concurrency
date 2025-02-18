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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class ZonedTriggerTest {

    /**
     * Example trigger from the ZonedTrigger JavaDoc.
     * A trigger that runs on the hour, Mon-Fri from 8am-8pm Central US time.
     */
    static class HourlyDuringBusinessHoursTrigger implements ZonedTrigger {
        static final ZoneId ZONE = ZoneId.of("America/Chicago");

        @Override
        public ZoneId getZoneId() {
            return ZONE;
        }

        @Override
        public ZonedDateTime getNextRunTime(LastExecution lastExec, ZonedDateTime taskScheduledTime) {
            ZonedDateTime prevTime = lastExec == null ? taskScheduledTime : lastExec.getRunEnd(ZONE);
            ZonedDateTime nextTime = prevTime.truncatedTo(ChronoUnit.HOURS).plusHours(1);
            DayOfWeek day = nextTime.getDayOfWeek();
            if (day.equals(DayOfWeek.SATURDAY) || day.equals(DayOfWeek.SUNDAY)) {
                nextTime = nextTime.truncatedTo(ChronoUnit.DAYS).plusDays(1).withHour(8);
            } else { // Mon-Fri 8am-8pm
                int hour = nextTime.getHour();
                if (hour < 8)
                    nextTime = nextTime.plusHours(8 - hour);
                else if (hour > 20)
                    nextTime = nextTime.truncatedTo(ChronoUnit.DAYS)
                                       .plusDays(day.equals(DayOfWeek.FRIDAY) ? 3 : 1)
                                       .withHour(8);
            }
            return nextTime;
        }
    }

    static class LastExecutionImpl implements LastExecution {
        final Instant scheduledStart, startedAt, endedAt;

        LastExecutionImpl(Instant scheduledStart, long startDelayMS, long lengthInSeconds) {
            this.scheduledStart = scheduledStart;
            this.startedAt = scheduledStart.plusMillis(startDelayMS);
            this.endedAt = startedAt.plusSeconds(lengthInSeconds);
        }

        @Override
        public String getIdentityName() { return "MyTask"; }

        @Override
        public Object getResult() { return "MyResult"; }

        @Override
        public ZonedDateTime getScheduledStart(ZoneId zone) { return scheduledStart.atZone(zone); }

        @Override
        public ZonedDateTime getRunStart(ZoneId zone) { return startedAt.atZone(zone); }

        @Override
        public ZonedDateTime getRunEnd(ZoneId zone) { return endedAt.atZone(zone); }
    }

    /**
     * Verify that the example trigger from the ZonedTrigger JavaDoc works.
     */
    @Test
    void testExampleCodeFromZonedTriggerJavaDoc() {
        ZonedTrigger trigger = new HourlyDuringBusinessHoursTrigger();

        ZoneId central = ZoneId.of("America/Chicago");
        assertEquals(central, trigger.getZoneId());

        ZonedDateTime scheduledAtTime = ZonedDateTime.of(
                2021, 11, 4, // Thursday Nov 4, 2021
                19, 30, 21, 987654321, // 7:30:21.987654321 PM
                central);

        ZonedDateTime nextTime = trigger.getNextRunTime(null, scheduledAtTime);
        assertEquals(ZonedDateTime.of(2021, 11, 4, 20, 0, 0, 0, central), nextTime);

        // switch to Friday @8 AM
        nextTime = trigger.getNextRunTime(new LastExecutionImpl(nextTime.toInstant(), 350, 15), scheduledAtTime);
        assertEquals(ZonedDateTime.of(2021, 11, 5, 8, 0, 0, 0, central), nextTime);

        nextTime = trigger.getNextRunTime(new LastExecutionImpl(nextTime.toInstant(), 0, 0), scheduledAtTime);
        assertEquals(ZonedDateTime.of(2021, 11, 5, 9, 0, 0, 0, central), nextTime);

        // simulate being delayed by 8+ hours
        nextTime = trigger.getNextRunTime(new LastExecutionImpl(nextTime.toInstant(), 29000000, 18), scheduledAtTime);
        assertEquals(ZonedDateTime.of(2021, 11, 5, 18, 0, 0, 0, central), nextTime);

        nextTime = trigger.getNextRunTime(new LastExecutionImpl(nextTime.toInstant(), 1, 1), scheduledAtTime);
        assertEquals(ZonedDateTime.of(2021, 11, 5, 19, 0, 0, 0, central), nextTime);

        nextTime = trigger.getNextRunTime(new LastExecutionImpl(nextTime.toInstant(), 999, 0), scheduledAtTime);
        assertEquals(ZonedDateTime.of(2021, 11, 5, 20, 0, 0, 0, central), nextTime);

        // switch to Monday @8 AM (crossing over daylight savings change)
        nextTime = trigger.getNextRunTime(new LastExecutionImpl(nextTime.toInstant(), 123, 45), scheduledAtTime);
        assertEquals(ZonedDateTime.of(2021, 11, 8, 8, 0, 0, 0, central), nextTime);

        nextTime = trigger.getNextRunTime(new LastExecutionImpl(nextTime.toInstant(), 543, 21), scheduledAtTime);
        assertEquals(ZonedDateTime.of(2021, 11, 8, 9, 0, 0, 0, central), nextTime);

        assertFalse(trigger.skipRun(new LastExecutionImpl(nextTime.toInstant(), 987, 65), scheduledAtTime));
    }

    /**
     * Test the default implementation of ZonedTrigger.getNextRunTime(LastExecution, Date),
     * which ought to delegate to getNextRunTime(LastExecution, ZonedDateTime).
     */
    @Test
    void testGetNextRunTimeDefaultImplementation() {
        ZonedTrigger trigger = new HourlyDuringBusinessHoursTrigger();

        TimeZone central = TimeZone.getTimeZone("America/Chicago");

        Calendar cal = Calendar.getInstance(central);
        cal.set(2021, 3-1, 12, 17, 59, 59); // Friday March 12, 2021 @ 5:59:59 PM
        Date scheduledAtTime = cal.getTime();

        // clear milliseconds to avoid interference with asserts
        cal.set(Calendar.MILLISECOND, 0);

        Date nextTime = trigger.getNextRunTime(null, scheduledAtTime);
        cal.set(2021, 3-1, 12, 18, 0, 0);
        assertEquals(cal.getTime(), nextTime, cal.getTime().getTime() + " vs " + nextTime.getTime());

        nextTime = trigger.getNextRunTime(new LastExecutionImpl(nextTime.toInstant(), 190, 6), scheduledAtTime);
        cal.set(2021, 3-1, 12, 19, 0, 0);
        assertEquals(cal.getTime(), nextTime, cal.getTime().getTime() + " vs " + nextTime.getTime());

        nextTime = trigger.getNextRunTime(new LastExecutionImpl(nextTime.toInstant(), 200, 5), scheduledAtTime);
        cal.set(2021, 3-1, 12, 20, 0, 0);
        assertEquals(cal.getTime(), nextTime, cal.getTime().getTime() + " vs " + nextTime.getTime());

        // switch to Monday @8 AM (crossing over daylight savings change)
        nextTime = trigger.getNextRunTime(new LastExecutionImpl(nextTime.toInstant(), 0, 4), scheduledAtTime);
        cal.set(2021, 3-1, 15, 8, 0, 0);
        assertEquals(cal.getTime(), nextTime);

        nextTime = trigger.getNextRunTime(new LastExecutionImpl(nextTime.toInstant(), 900, 1), scheduledAtTime);
        cal.set(2021, 3-1, 15, 9, 0, 0);
        assertEquals(cal.getTime(), nextTime);

        nextTime = trigger.getNextRunTime(new LastExecutionImpl(nextTime.toInstant(), 100, 10), scheduledAtTime);
        cal.set(2021, 3-1, 15, 10, 0, 0);
        assertEquals(cal.getTime(), nextTime);

        // simulate being delayed by 2+ hours
        nextTime = trigger.getNextRunTime(new LastExecutionImpl(nextTime.toInstant(), 7500000, 13), scheduledAtTime);
        cal.set(2021, 3-1, 15, 13, 0, 0);
        assertEquals(cal.getTime(), nextTime);

        nextTime = trigger.getNextRunTime(new LastExecutionImpl(nextTime.toInstant(), 140, 4), scheduledAtTime);
        cal.set(2021, 3-1, 15, 14, 0, 0);
        assertEquals(cal.getTime(), nextTime);

        nextTime = trigger.getNextRunTime(new LastExecutionImpl(nextTime.toInstant(), 150, 5), scheduledAtTime);
        cal.set(2021, 3-1, 15, 15, 0, 0);
        assertEquals(cal.getTime(), nextTime);
    }

    /**
     * Test the default implementation of LastExecution methods for obtaining
     * the scheduled-at time, start time, and end time without a ZoneId parameter.
     */
    @Test
    void testLastExecutionDefaultImplementation() {
        ZoneId mtnTime = ZoneId.of("America/Denver");
        Instant scheduledAt = ZonedDateTime.of(2020, 7, 17, 3, 16, 30, 123000000, mtnTime).toInstant();

        LastExecution lastExec = new LastExecutionImpl(scheduledAt, 420, 15);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(mtnTime));

        cal.set(2020, 7-1, 17, 3, 16, 30);
        cal.set(Calendar.MILLISECOND, 123);
        assertEquals(cal.getTime(), lastExec.getScheduledStart());

        cal.set(2020, 7-1, 17, 3, 16, 30);
        cal.set(Calendar.MILLISECOND, 543);
        assertEquals(cal.getTime(), lastExec.getRunStart());

        cal.set(2020, 7-1, 17, 3, 16, 45);
        cal.set(Calendar.MILLISECOND, 543);
        assertEquals(cal.getTime(), lastExec.getRunEnd());

        LastExecution lastExecWithNulls = new LastExecution() {
            @Override
            public String getIdentityName() { return "SomeTask"; }

            @Override
            public Object getResult() { return "SomeResult"; }

            @Override
            public ZonedDateTime getScheduledStart(ZoneId zone) { return ZonedDateTime.now(zone); }

            @Override
            public ZonedDateTime getRunStart(ZoneId zone) { return null; }

            @Override
            public ZonedDateTime getRunEnd(ZoneId zone) { return null; }
        };

        assertNull(lastExecWithNulls.getRunStart());
        assertNull(lastExecWithNulls.getRunEnd());
        assertNotNull(lastExecWithNulls.getScheduledStart());
    }

    /**
     * Ensure that the default method for getNextRunTime(LastExecution, Date) can
     * cope with a null being returned by getNextRunTime(LastExecution, ZonedDateTime).
     */
    @Test
    void testNullNextRunTime() {
        ZonedTrigger trigger = (lastExec, taskScheduledTime) -> null;

        assertNull(trigger.getNextRunTime(null, new Date()));
    }

    /**
     * Test the default implementation of ZonedTrigger.skipRun(LastExecution, Date),
     * which ought to delegate to skipRun(LastExecution, ZonedDateTime).
     */
    @Test
    void testSkipRunDefaultImplementation() {
        // A trigger that skips executions for which the scheduled start
        // is overlapped by the end of a previous execution.
        ZonedTrigger trigger = new ZonedTrigger() {
            @Override
            public ZonedDateTime getNextRunTime(LastExecution lastExec, ZonedDateTime taskScheduledTime) {
                return ZonedDateTime.now(getZoneId());
            }

            @Override
            public boolean skipRun(LastExecution lastExec, ZonedDateTime scheduledStart) {
                return lastExec != null && lastExec.getRunEnd(getZoneId()).isAfter(scheduledStart);
            }
        };

        // Use the default skipRun implementation and see if its results are consistent with the above.

        Date now = new Date();
        assertFalse(trigger.skipRun(null, now));

        LastExecution lastExecEnded4minAgo = new LastExecutionImpl(
                now.toInstant().plusSeconds(TimeUnit.MINUTES.toSeconds(-5)),
                TimeUnit.SECONDS.toMillis(15), 45);
        assertFalse(trigger.skipRun(lastExecEnded4minAgo, now));

        Date start10minAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(10));
        assertTrue(trigger.skipRun(lastExecEnded4minAgo, start10minAgo));

        // Also cover getZoneId default implementation
        assertEquals(ZoneId.systemDefault(), trigger.getZoneId());
    }
}
