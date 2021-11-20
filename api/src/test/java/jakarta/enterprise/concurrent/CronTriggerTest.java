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

import static org.junit.Assert.*;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.ZoneId;

import org.junit.Test;

public class CronTriggerTest {

    // LastExecution is used for invoking trigger.getNextRunTime and trigger.skipRun
    static class LastExecutionImpl implements LastExecution {
        final ZonedDateTime scheduledStart, startedAt, endedAt;

        LastExecutionImpl(int numExecutions, ZonedDateTime scheduledStart) {
            this.scheduledStart = scheduledStart;
            this.startedAt = numExecutions == 0 ? null : scheduledStart.plusSeconds(10);
            this.endedAt = numExecutions == 0 ? null : startedAt.plusSeconds(3);
        }

        @Override
        public String getIdentityName() {
            return "MyTask";
        }

        @Override
        public Object getResult() {
            return endedAt == null ? null : "MyResult";
        }

        @Override
        public ZonedDateTime getScheduledStart(ZoneId zone) {
            return scheduledStart.withZoneSameInstant(zone);
        }

        @Override
        public ZonedDateTime getRunStart(ZoneId zone) {
            return startedAt == null ? null : startedAt.withZoneSameInstant(zone);
        }

        @Override
        public ZonedDateTime getRunEnd(ZoneId zone) {
            return endedAt == null ? null : endedAt.withZoneSameInstant(zone);
        }
    }

    /**
     * Example from the CronTigger JavaDoc that combines two triggers.
     */
    static class PayrollTrigger extends CronTrigger {
        private final CronTrigger fridaysBeforeWeekendPayrollDay;

        PayrollTrigger() {
            // Every 15th and last day of the month that is a weekday,
            super("0 10 15,L * MON-FRI", ZoneId.of("America/Chicago"));

            // Every 13th & 14th and third-to-last and second-to-last day of the month that is a Friday,
            fridaysBeforeWeekendPayrollDay = new CronTrigger(
                    "0 10 13,14,3L,2L * FRI", getZoneId());
        }

        @Override
        public ZonedDateTime getNextRunTime(LastExecution lastExec, ZonedDateTime scheduledAt) {
            ZonedDateTime time1 = super.getNextRunTime(lastExec, scheduledAt);
            ZonedDateTime time2 = fridaysBeforeWeekendPayrollDay.getNextRunTime(lastExec, scheduledAt);
            return time1.isBefore(time2) ? time1 : time2;
        }
    }

    /**
     * Use a CronTrigger with a basic cron expression.
     */
    @Test
    public void testBasicCronExpression() {
        ZoneId Moscow = ZoneId.of("Europe/Moscow");
        CronTrigger trigger = new CronTrigger("0 9-17 * * MON-FRI", Moscow);

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2021, 7, 30, // Friday, July 30, 2021
                15, 0, 0, 0, // 3:00 PM
                trigger.getZoneId());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 7, 30, 15, 0, 0, 0, Moscow), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 7, 30, 16, 0, 0, 0, Moscow), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 7, 30, 17, 0, 0, 0, Moscow), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 2, 9, 0, 0, 0, Moscow), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 2, 10, 0, 0, 0, Moscow), time);
    }

    /**
     * Use a CronTrigger with a more complex cron expression consisting of 5 fields
     * with intervals and ordinal days of week.
     */
    @Test
    public void testCronExpression() {
        // 3 PM every third month starting with February, every Monday, on the second Tuesday,
        // on the third Wednesday, and on the last Saturday of the month.

        ZoneId Brazil = ZoneId.of("America/Sao_Paulo");
        CronTrigger trigger = new CronTrigger("0 15 * FEB/3 MON,TUE#2,WED#3,SAT#L", Brazil);

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2021, 1, 1, // Friday, Jan 1, 2021
                20, 0, 0, 0, // 8:00 PM
                trigger.getZoneId());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 1, 15, 0, 0, 0, Brazil), time); // Monday

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 8, 15, 0, 0, 0, Brazil), time); // Monday

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 9, 15, 0, 0, 0, Brazil), time); // Tuesday

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 15, 15, 0, 0, 0, Brazil), time); // Monday

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 17, 15, 0, 0, 0, Brazil), time); // Wednesday

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 22, 15, 0, 0, 0, Brazil), time); // Monday

        time = trigger.getNextRunTime(new LastExecutionImpl(6, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 27, 15, 0, 0, 0, Brazil), time); // Saturday

        time = trigger.getNextRunTime(new LastExecutionImpl(7, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 3, 15, 0, 0, 0, Brazil), time); // Monday

        time = trigger.getNextRunTime(new LastExecutionImpl(8, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 10, 15, 0, 0, 0, Brazil), time); // Monday

        time = trigger.getNextRunTime(new LastExecutionImpl(9, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 11, 15, 0, 0, 0, Brazil), time); // Tuesday

        time = trigger.getNextRunTime(new LastExecutionImpl(10, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 17, 15, 0, 0, 0, Brazil), time); // Monday

        time = trigger.getNextRunTime(new LastExecutionImpl(11, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 19, 15, 0, 0, 0, Brazil), time); // Wednesday

        time = trigger.getNextRunTime(new LastExecutionImpl(12, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 24, 15, 0, 0, 0, Brazil), time); // Monday

        time = trigger.getNextRunTime(new LastExecutionImpl(13, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 29, 15, 0, 0, 0, Brazil), time); // Saturday

        time = trigger.getNextRunTime(new LastExecutionImpl(14, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 31, 15, 0, 0, 0, Brazil), time); // Monday

        time = trigger.getNextRunTime(new LastExecutionImpl(15, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 2, 15, 0, 0, 0, Brazil), time); // Monday
    }

    /**
     * Use a CronTrigger with a cron expression consisting of 6 fields.
     */
    @Test
    public void testCronExpressionWithSeconds() {
        // 10:20:30 AM on the 27th through last days of February, March, and April.

        ZoneId Azores = ZoneId.of("Atlantic/Azores");
        CronTrigger trigger = new CronTrigger("30 20 10 27-L FEB-APR *", Azores);

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2023, 2, 2, // Thursday, Feb 2, 2023
                19, 30, 0, 0, // 7:30 PM
                trigger.getZoneId());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2023, 2, 27, 10, 20, 30, 0, Azores), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2023, 2, 28, 10, 20, 30, 0, Azores), time); // 28 day February

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2023, 3, 27, 10, 20, 30, 0, Azores), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2023, 3, 28, 10, 20, 30, 0, Azores), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2023, 3, 29, 10, 20, 30, 0, Azores), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2023, 3, 30, 10, 20, 30, 0, Azores), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(6, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2023, 3, 31, 10, 20, 30, 0, Azores), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(7, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2023, 4, 27, 10, 20, 30, 0, Azores), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(8, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2023, 4, 28, 10, 20, 30, 0, Azores), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(9, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2023, 4, 29, 10, 20, 30, 0, Azores), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(10, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2023, 4, 30, 10, 20, 30, 0, Azores), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(11, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2024, 2, 27, 10, 20, 30, 0, Azores), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(12, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2024, 2, 28, 10, 20, 30, 0, Azores), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(13, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2024, 2, 29, 10, 20, 30, 0, Azores), time); // 29 day February

        time = trigger.getNextRunTime(new LastExecutionImpl(14, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2024, 3, 27, 10, 20, 30, 0, Azores), time);
    }

    /**
     * Tests the example in CronTrigger JavaDoc that combines 2 CronTriggers
     * to simluate payroll on the 15th and last day of the month or the prior
     * Friday whenever the former fall on a weekend.
     */
    @Test
    public void testCronTriggerJavaDocExample() {
        CronTrigger trigger = new PayrollTrigger();
        ZoneId zone = trigger.getZoneId();

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2021, 1, 1, // Friday, Jan 1, 2021
                6, 0, 0, 0, // 6:00 AM
                zone);

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 1, 15, 10, 0, 0, 0, zone), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 1, 29, 10, 0, 0, 0, zone), time); // third-to-last

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 15, 10, 0, 0, 0, zone), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 26, 10, 0, 0, 0, zone), time); // third-to-last

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 15, 10, 0, 0, 0, zone), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 31, 10, 0, 0, 0, zone), time); // last

        time = trigger.getNextRunTime(new LastExecutionImpl(6, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 4, 15, 10, 0, 0, 0, zone), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(7, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 4, 30, 10, 0, 0, 0, zone), time); // last

        time = trigger.getNextRunTime(new LastExecutionImpl(8, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 14, 10, 0, 0, 0, zone), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(9, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 31, 10, 0, 0, 0, zone), time); // last

        time = trigger.getNextRunTime(new LastExecutionImpl(10, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 6, 15, 10, 0, 0, 0, zone), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(11, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 6, 30, 10, 0, 0, 0, zone), time); // last

        time = trigger.getNextRunTime(new LastExecutionImpl(12, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 7, 15, 10, 0, 0, 0, zone), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(13, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 7, 30, 10, 0, 0, 0, zone), time); // second-to-last

        time = trigger.getNextRunTime(new LastExecutionImpl(14, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 13, 10, 0, 0, 0, zone), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(15, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 31, 10, 0, 0, 0, zone), time); // last

        time = trigger.getNextRunTime(new LastExecutionImpl(16, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 9, 15, 10, 0, 0, 0, zone), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(17, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 9, 30, 10, 0, 0, 0, zone), time); // last
    }

    /**
     * Specify daysOfMonth as a cron expression.
     */
    @Test
    public void testDaysOfMonthCronExpression() {
        ZoneId Central = ZoneId.of("America/Chicago");

        CronTrigger trigger = new CronTrigger(Central)
                .daysOfMonth("L,7/9,2-4"); // equivalent to: 2,3,4,7,16,25,L

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2021, 1, 28, // Thursday, Jan 28, 2021
                7, 45, 0, 0, // 7:45 AM
                trigger.getZoneId());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 1, 31, 0, 0, 0, 0, Central), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 2, 0, 0, 0, 0, Central), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 3, 0, 0, 0, 0, Central), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 4, 0, 0, 0, 0, Central), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 7, 0, 0, 0, 0, Central), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 16, 0, 0, 0, 0, Central), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(6, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 25, 0, 0, 0, 0, Central), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(7, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 28, 0, 0, 0, 0, Central), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(8, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 2, 0, 0, 0, 0, Central), time);
    }

    /**
     * Specify daysOfMonth as a set of literals.
     */
    @Test
    public void testDaysOfMonthLiterals() {
        ZoneId Eastern = ZoneId.of("America/New_York");

        CronTrigger trigger = new CronTrigger(Eastern)
                .daysOfMonth(30, 7, 31, 3);

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2021, 9, 1, // Wednesday, Sept 1, 2021
                10, 30, 0, 0, // 10:30 AM
                trigger.getZoneId());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 9, 3, 0, 0, 0, 0, Eastern), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 9, 7, 0, 0, 0, 0, Eastern), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 9, 30, 0, 0, 0, 0, Eastern), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 10, 3, 0, 0, 0, 0, Eastern), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 10, 7, 0, 0, 0, 0, Eastern), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 10, 30, 0, 0, 0, 0, Eastern), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(6, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 10, 31, 0, 0, 0, 0, Eastern), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(7, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 3, 0, 0, 0, 0, Eastern), time);
    }

    /**
     * Specify daysOfWeek as a cron expression.
     */
    @Test
    public void testDaysOfWeekCronExpression() {
        ZoneId Mtn = ZoneId.of("America/Denver");

        CronTrigger trigger = new CronTrigger(Mtn)
                .daysOfWeek("SAT-MON,WED#L");

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2021, 4, 26, // Tuesday, Apr 27, 2021
                16, 10, 0, 0, // 4:10 PM
                trigger.getZoneId());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 4, 28, 0, 0, 0, 0, Mtn), time); // Wednesday (Last)

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 1, 0, 0, 0, 0, Mtn), time); // Saturday

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 2, 0, 0, 0, 0, Mtn), time); // Sunday

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 3, 0, 0, 0, 0, Mtn), time); // Monday

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 8, 0, 0, 0, 0, Mtn), time); // Saturday

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 9, 0, 0, 0, 0, Mtn), time); // Sunday

        time = trigger.getNextRunTime(new LastExecutionImpl(6, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 10, 0, 0, 0, 0, Mtn), time); // Monday

        time = trigger.getNextRunTime(new LastExecutionImpl(7, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 15, 0, 0, 0, 0, Mtn), time); // Saturday

        time = trigger.getNextRunTime(new LastExecutionImpl(8, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 16, 0, 0, 0, 0, Mtn), time); // Sunday

        time = trigger.getNextRunTime(new LastExecutionImpl(9, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 17, 0, 0, 0, 0, Mtn), time); // Monday

        time = trigger.getNextRunTime(new LastExecutionImpl(10, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 22, 0, 0, 0, 0, Mtn), time); // Saturday

        time = trigger.getNextRunTime(new LastExecutionImpl(11, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 23, 0, 0, 0, 0, Mtn), time); // Sunday

        time = trigger.getNextRunTime(new LastExecutionImpl(12, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 24, 0, 0, 0, 0, Mtn), time); // Monday

        time = trigger.getNextRunTime(new LastExecutionImpl(13, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 26, 0, 0, 0, 0, Mtn), time); // Wednesday (Last)

        time = trigger.getNextRunTime(new LastExecutionImpl(14, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 29, 0, 0, 0, 0, Mtn), time); // Saturday

        time = trigger.getNextRunTime(new LastExecutionImpl(15, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 30, 0, 0, 0, 0, Mtn), time); // Sunday

        time = trigger.getNextRunTime(new LastExecutionImpl(16, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 31, 0, 0, 0, 0, Mtn), time); // Monday

        time = trigger.getNextRunTime(new LastExecutionImpl(17, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 6, 5, 0, 0, 0, 0, Mtn), time); // Saturday
    }

    /**
     * Specify daysOfWeek as a set of literals.
     */
    @Test
    public void testDaysOfWeekLiterals() {
        ZoneId Pacific = ZoneId.of("America/Los_Angeles");

        CronTrigger trigger = new CronTrigger(Pacific)
                .daysOfWeek(DayOfWeek.SUNDAY, DayOfWeek.THURSDAY);

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2021, 3, 11, // Thursday, March 11, 2021
                13, 15, 0, 0, // 1:15 PM
                trigger.getZoneId());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 14, 0, 0, 0, 0, Pacific), time); // Sunday

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 18, 0, 0, 0, 0, Pacific), time); // Thursday

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 21, 0, 0, 0, 0, Pacific), time); // Sunday

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 25, 0, 0, 0, 0, Pacific), time); // Thursday

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 28, 0, 0, 0, 0, Pacific), time); // Sunday

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 4, 1, 0, 0, 0, 0, Pacific), time);  // Thursday
    }

    /**
     * Specify hours as a cron expression.
     * This also covers crossing out of Daylight Saving Time.
     */
    @Test
    public void testHoursCronExpression() {
        ZoneId Alaska = ZoneId.of("America/Anchorage");

        CronTrigger trigger = new CronTrigger(Alaska)
                .hours("23-4"); // 11 PM to 4 AM

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2021, 11, 6, // Saturday, Nov 7, 2021
                17, 30, 0, 0, // 5:30 PM
                trigger.getZoneId());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 6, 23, 0, 0, 0, Alaska), time); // 11 PM

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 7, 0, 0, 0, 0, Alaska), time); // 12 AM

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 7, 1, 0, 0, 0, Alaska), time); // 1 AM Daylight Savings Time

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 7, 1, 0, 0, 0, Alaska).plusHours(1), time); // 1 AM Standard Time

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 7, 2, 0, 0, 0, Alaska), time); // 2 AM

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 7, 3, 0, 0, 0, Alaska), time); // 3 AM

        time = trigger.getNextRunTime(new LastExecutionImpl(6, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 7, 4, 0, 0, 0, Alaska), time); // 4 AM

        time = trigger.getNextRunTime(new LastExecutionImpl(7, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 7, 23, 0, 0, 0, Alaska), time); // 11 PM

        time = trigger.getNextRunTime(new LastExecutionImpl(8, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 8, 0, 0, 0, 0, Alaska), time); // 12 AM

        time = trigger.getNextRunTime(new LastExecutionImpl(9, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 8, 1, 0, 0, 0, Alaska), time); // 1 AM

        time = trigger.getNextRunTime(new LastExecutionImpl(10, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 8, 2, 0, 0, 0, Alaska), time); // 2 AM

        time = trigger.getNextRunTime(new LastExecutionImpl(11, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 8, 3, 0, 0, 0, Alaska), time); // 3 AM

        time = trigger.getNextRunTime(new LastExecutionImpl(12, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 8, 4, 0, 0, 0, Alaska), time); // 4 AM

        time = trigger.getNextRunTime(new LastExecutionImpl(13, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 8, 23, 0, 0, 0, Alaska), time); // 11 PM
    }

    /**
     * Specify hours as a set of literals.
     * This also tests crossing over into Daylight Saving Time.
     */
    @Test
    public void testHoursLiterals() {
        ZoneId Aleutian = ZoneId.of("America/Adak");

        CronTrigger trigger = new CronTrigger(Aleutian)
                .hours(1,2,3);

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2021, 3, 13, // Saturday, March 13, 2021
                14, 25, 0, 0, // 2:25 PM
                trigger.getZoneId());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 14, 1, 0, 0, 0, Aleutian), time); // 1 AM

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 14, 3, 0, 0, 0, Aleutian), time); // 3 AM (no 2 AM because of Daylight Saving Time)

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 15, 1, 0, 0, 0, Aleutian), time); // 1 AM

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 15, 2, 0, 0, 0, Aleutian), time); // 2 AM

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 15, 3, 0, 0, 0, Aleutian), time); // 3 AM

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 16, 1, 0, 0, 0, Aleutian), time); // 1 AM

        time = trigger.getNextRunTime(new LastExecutionImpl(6, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 16, 2, 0, 0, 0, Aleutian), time); // 2 AM

        time = trigger.getNextRunTime(new LastExecutionImpl(7, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 16, 3, 0, 0, 0, Aleutian), time); // 3 AM
    }

    /**
     * Specify minutes as a cron expression.
     */
    @Test
    public void testMinutesCronExpression() {
        ZoneId NewZealand = ZoneId.of("Pacific/Auckland");

        CronTrigger trigger = new CronTrigger(NewZealand)
                .hours("*")
                .minutes("5-7,14/15,*/20"); // equivalent to: 0,5,6,7,14,20,29,40,44,59

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2021, 8, 10, // Tuesday, Aug 10, 2021
                6, 55, 0, 0, // 6:55 AM
                trigger.getZoneId());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 10, 6, 59, 0, 0, NewZealand), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 10, 7, 0, 0, 0, NewZealand), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 10, 7, 5, 0, 0, NewZealand), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 10, 7, 6, 0, 0, NewZealand), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 10, 7, 7, 0, 0, NewZealand), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 10, 7, 14, 0, 0, NewZealand), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(6, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 10, 7, 20, 0, 0, NewZealand), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(7, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 10, 7, 29, 0, 0, NewZealand), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(8, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 10, 7, 40, 0, 0, NewZealand), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(9, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 10, 7, 44, 0, 0, NewZealand), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(10, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 10, 7, 59, 0, 0, NewZealand), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(11, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 10, 8, 0, 0, 0, NewZealand), time);
    }

    /**
     * Specify minutes as a set of literals.
     */
    @Test
    public void testMinutesLiterals() {
        ZoneId Magadan = ZoneId.of("Asia/Magadan");

        CronTrigger trigger = new CronTrigger(Magadan)
                .hours("*")
                .minutes(38, 0, 59, 38, 17); // extra 38 gets ignored

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2021, 7, 31, // Saturday, July 31, 2021
                23, 58, 59, 0, // 11:58:59 PM
                trigger.getZoneId());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 7, 31, 23, 59, 0, 0, Magadan), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 1, 0, 0, 0, 0, Magadan), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 1, 0, 17, 0, 0, Magadan), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 1, 0, 38, 0, 0, Magadan), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 1, 0, 59, 0, 0, Magadan), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 1, 1, 0, 0, 0, Magadan), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(6, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 1, 1, 17, 0, 0, Magadan), time);
    }

    /**
     * Specify months as a cron expression.
     */
    @Test
    public void testMonthsCronExpression() {
        ZoneId EasternAUS = ZoneId.of("Australia/Sydney");

        CronTrigger trigger = new CronTrigger(EasternAUS)
                .months("OCT-MAR")
                .daysOfMonth("L");

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2019, 11, 6, // Wednesday, Nov 6, 2019
                22, 1, 0, 0, // 10:01 PM
                trigger.getZoneId());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2019, 11, 30, 0, 0, 0, 0, EasternAUS), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2019, 12, 31, 0, 0, 0, 0, EasternAUS), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2020, 1, 31, 0, 0, 0, 0, EasternAUS), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2020, 2, 29, 0, 0, 0, 0, EasternAUS), time); // 29 day February

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2020, 3, 31, 0, 0, 0, 0, EasternAUS), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2020, 10, 31, 0, 0, 0, 0, EasternAUS), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(6, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2020, 11, 30, 0, 0, 0, 0, EasternAUS), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(7, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2020, 12, 31, 0, 0, 0, 0, EasternAUS), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(8, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 1, 31, 0, 0, 0, 0, EasternAUS), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(9, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 28, 0, 0, 0, 0, EasternAUS), time); // 28 day February

        time = trigger.getNextRunTime(new LastExecutionImpl(10, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 31, 0, 0, 0, 0, EasternAUS), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(11, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 10, 31, 0, 0, 0, 0, EasternAUS), time);
    }

    /**
     * Specify months as a set of literals.
     */
    @Test
    public void testMonthsLiterals() {
        ZoneId Japan = ZoneId.of("Asia/Tokyo");

        CronTrigger trigger = new CronTrigger(Japan)
                .months(Month.SEPTEMBER, Month.FEBRUARY, Month.MAY)
                .daysOfMonth("2L"); // second-to-last day of month

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2023, 9, 20, // Wednesday, Sept 20, 2023
                12, 0, 0, 0, // Noon
                trigger.getZoneId());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2023, 9, 29, 0, 0, 0, 0, Japan), time); // September - 30 day month

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2024, 2, 28, 0, 0, 0, 0, Japan), time); // February - 29 day month

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2024, 5, 30, 0, 0, 0, 0, Japan), time); // May - 31 day month

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2024, 9, 29, 0, 0, 0, 0, Japan), time); // September - 30 day month

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2025, 2, 27, 0, 0, 0, 0, Japan), time); // February - 28 day month

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2025, 5, 30, 0, 0, 0, 0, Japan), time); // May - 31 day month
    }

    /**
     * Specify seconds as a cron expression.
     */
    @Test
    public void testSecondsCronExpression() {
        ZoneId China = ZoneId.of("Asia/Shanghai");

        CronTrigger trigger = new CronTrigger(China)
                .hours("*")
                .minutes("*")
                .seconds("3/15"); // equivalent to 3,18,33,48

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2021, 6, 16, // Wednesday, June 16, 2021
                10, 35, 47, 306114016, // 10:35:53.306114016 AM
                trigger.getZoneId());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 6, 16, 10, 35, 48, 0, China), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 6, 16, 10, 36, 3, 0, China), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 6, 16, 10, 36, 18, 0, China), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 6, 16, 10, 36, 33, 0, China), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 6, 16, 10, 36, 48, 0, China), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 6, 16, 10, 37, 3, 0, China), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(6, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 6, 16, 10, 37, 18, 0, China), time);
    }

    /**
     * Specify seconds as a set of literals.
     */
    @Test
    public void testSecondsLiterals() {
        ZoneId Indochina = ZoneId.of("Asia/Bangkok");

        CronTrigger trigger = new CronTrigger(Indochina)
                .hours("*")
                .minutes("*")
                .seconds(53, 34, 13);

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2021, 5, 4, // Tuesday, May 4, 2021
                5, 51, 34, 0, // 5:51:34 AM
                trigger.getZoneId());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 4, 5, 51, 34, 0, Indochina), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 4, 5, 51, 53, 0, Indochina), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 4, 5, 52, 13, 0, Indochina), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 4, 5, 52, 34, 0, Indochina), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 4, 5, 52, 53, 0, Indochina), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 5, 4, 5, 53, 13, 0, Indochina), time);
    }

    /**
     * Confirm proper advancement from 59 seconds into the next minute, and so forth.
     */
    @Test
    public void testTriggerAdvancesAcrossUnits() {
        ZoneId Hawaii = ZoneId.of("Pacific/Honolulu");

        CronTrigger trigger = new CronTrigger(Hawaii)
                .seconds("*/20")
                .minutes("*/15")
                .hours("*/12")
                .daysOfMonth("1/8")
                .months("JAN/6");

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2020, 12, 31, // Thursday, December 31, 2020
                23, 59, 59, 0, // 11:59:59 PM
                trigger.getZoneId());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, Hawaii), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 1, 1, 0, 0, 20, 0, Hawaii), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 1, 1, 0, 0, 40, 0, Hawaii), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 1, 1, 0, 15, 0, 0, Hawaii), time);
    }

    /**
     * Specify a cron expression that very infrequently matches, such as leap days that are Fridays.
     */
    @Test
    public void testVeryLowFrequencyCronExpression() {
        ZoneId CentralEU = ZoneId.of("Europe/Paris");

        CronTrigger trigger = new CronTrigger(CentralEU)
                .months(Month.FEBRUARY)
                .daysOfMonth(29)
                .daysOfWeek(DayOfWeek.FRIDAY)
                .hours(16);

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2020, 1, 1, // Wednesday, Jan 1, 2020
                10, 15, 0, 0, // 10:15 AM
                trigger.getZoneId());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2036, 2, 29, 16, 0, 0, 0, CentralEU), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2064, 2, 29, 16, 0, 0, 0, CentralEU), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2092, 2, 29, 16, 0, 0, 0, CentralEU), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2104, 2, 29, 16, 0, 0, 0, CentralEU), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2132, 2, 29, 16, 0, 0, 0, CentralEU), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2160, 2, 29, 16, 0, 0, 0, CentralEU), time);
    }

    /**
     * Specify day-of-week and month names fully spelled out and as mixed case abbreviations.
     */
    @Test
    public void testWeekDayAndMonthNamesInCronExpression() {
        ZoneId GMT = ZoneId.of("GMT");

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2019, 11, 25, // Monday, Nov 25, 2019
                14, 30, 0, 0, // 2:30 PM 
                GMT);

        CronTrigger trigger = new CronTrigger("0 6 L,10,20,30 February,Nov,August Sat-Tuesday", scheduledAt.getZone());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2019, 11, 30, 6, 0, 0, 0, GMT), time); // Saturday

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2020, 2, 10, 6, 0, 0, 0, GMT), time); // Monday

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2020, 2, 29, 6, 0, 0, 0, GMT), time); // Saturday

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2020, 8, 10, 6, 0, 0, 0, GMT), time); // Monday

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2020, 8, 30, 6, 0, 0, 0, GMT), time); // Sunday

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2020, 8, 31, 6, 0, 0, 0, GMT), time); // Monday

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2020, 11, 10, 6, 0, 0, 0, GMT), time); // Tuesday

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2020, 11, 30, 6, 0, 0, 0, GMT), time); // Monday

        time = trigger.getNextRunTime(new LastExecutionImpl(6, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 20, 6, 0, 0, 0, GMT), time); // Saturday

        time = trigger.getNextRunTime(new LastExecutionImpl(7, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 2, 28, 6, 0, 0, 0, GMT), time); // Sunday

        time = trigger.getNextRunTime(new LastExecutionImpl(8, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 8, 10, 6, 0, 0, 0, GMT), time); // Tuesday
    }

    /**
     * 0 and 7 can both be used for Sunday.
     */
    @Test
    public void testZeroAnd7bothSunday() {
        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2021, 4, 14, // Wednesday, Apr 14, 2021
                18, 5, 0, 0, // 6:05 PM
                ZoneId.of("America/Manaus"));

        ZonedDateTime time;
        time = new CronTrigger("30 7 * * 7", scheduledAt.getZone()).getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 4, 18, 7, 30, 0, 0, time.getZone()), time);

        time = new CronTrigger("0 9 * * 0", scheduledAt.getZone()).getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 4, 18, 9, 0, 0, 0, time.getZone()), time);

        CronTrigger trigger = new CronTrigger("45 10 * * 0,7", scheduledAt.getZone());
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 4, 18, 10, 45, 0, 0, time.getZone()), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 4, 25, 10, 45, 0, 0, time.getZone()), time);
    }

    /**
     * Specify a ZoneId that uses Daylight Saving Time.
     */
    @Test
    public void testZoneWithDaylightSavingTime() {
        ZoneId Newfoundland = ZoneId.of("America/St_Johns");

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2021, 3, 14, // Sunday, March 14, 2021
                0, 0, 0, 0, // Midnight
                Newfoundland);

        CronTrigger trigger = new CronTrigger("29/30 0-3 7,14 NOV,MAR *", scheduledAt.getZone());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 14, 0, 29, 0, 0, Newfoundland), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 14, 0, 59, 0, 0, Newfoundland), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 14, 1, 29, 0, 0, Newfoundland), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 14, 1, 59, 0, 0, Newfoundland), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt); // Standard --> DST
        assertEquals(ZonedDateTime.of(2021, 3, 14, 3, 29, 0, 0, Newfoundland), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 14, 3, 59, 0, 0, Newfoundland), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(6, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 7, 0, 29, 0, 0, Newfoundland), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(7, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 7, 0, 59, 0, 0, Newfoundland), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(8, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 7, 1, 29, 0, 0, Newfoundland), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(9, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 7, 1, 59, 0, 0, Newfoundland), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(10, time), scheduledAt); // DST --> Standard
        assertEquals(ZonedDateTime.of(2021, 11, 7, 1, 29, 0, 0, Newfoundland).plusHours(1), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(11, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 7, 1, 59, 0, 0, Newfoundland).plusHours(1), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(12, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 7, 2, 29, 0, 0, Newfoundland), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(13, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 7, 2, 59, 0, 0, Newfoundland), time);
    }

    /**
     * Specify a ZoneId that doesn't use Daylight Saving Time.
     */
    @Test
    public void testZoneWithoutDaylightSavingTime() {
        ZoneId CST = ZoneId.of("America/Regina");

        ZonedDateTime scheduledAt = ZonedDateTime.of(
                2021, 3, 14, // Sunday, March 14, 2021
                0, 0, 0, 0, // Midnight
                CST);

        CronTrigger trigger = new CronTrigger("0 0-3 7,14 NOV,MAR *", scheduledAt.getZone());

        ZonedDateTime time;
        time = trigger.getNextRunTime(null, scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 14, 0, 0, 0, 0, CST), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(1, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 14, 1, 0, 0, 0, CST), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(2, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 14, 2, 0, 0, 0, CST), time); // Standard --> DST ignored

        time = trigger.getNextRunTime(new LastExecutionImpl(3, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 3, 14, 3, 0, 0, 0, CST), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(4, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 7, 0, 0, 0, 0, CST), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(5, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 7, 1, 0, 0, 0, CST), time);

        time = trigger.getNextRunTime(new LastExecutionImpl(6, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 7, 2, 0, 0, 0, CST), time); // DST --> Standard ignored

        time = trigger.getNextRunTime(new LastExecutionImpl(7, time), scheduledAt);
        assertEquals(ZonedDateTime.of(2021, 11, 7, 3, 0, 0, 0, CST), time);
    }
}
