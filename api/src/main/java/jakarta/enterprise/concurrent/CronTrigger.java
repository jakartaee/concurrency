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

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.Year;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * <p>Cron-based {@link Trigger} implementation, which supports 5 or 6 fields
 * delimited by a single space character, plus a {@link ZoneId}.
 * Basic cron syntax is supported. For more advanced scenarios, you can
 * subclass this implementation or combine multiple <code>CronTrigger</code>
 * instances in a <code>Trigger</code> implementation of your own.
 * </p>
 * <table width="90%">
 * <caption><b>Cron Expression Fields</b></caption>
 * <tr valign="top"><td>seconds (optional)</td><td>0-59, *. When absent, 0 is assumed</td></tr>
 * <tr valign="top"><td>minutes</td><td>0-59, *</td></tr>
 * <tr valign="top"><td>hours</td><td>0-23, *</td></tr>
 * <tr valign="top"><td>dayOfMonth</td><td>0-31, *, L</td></tr>
 * <tr valign="top"><td>month</td><td>1-12, JAN-DEC, January-December, *</td></tr>
 * <tr valign="top"><td>dayOfWeek</td><td>SUN-SAT, Sunday-Saturday, 0-7, *.
 *         0 and 7 both represent Sunday: 0 to designate the first day of the week,
 *         and 7 for consistency with {@link java.time.DayOfWeek}.</td></tr>
 * </table>
 * <br>
 * <table width="90%">
 * <caption><b>Cron Expression Syntax</b></caption>
 * <tr valign="top"><td><code>,</code></td><td>delimits lists for all fields. For example, <code>MON,WED,FRI</code> or <code>MAY,SEP</code></td></tr>
 * <tr valign="top"><td><code>-</code></td><td>delimits ranges for all fields. For example, <code>MON-FRI</code> or <code>9-17</code></td></tr>
 * <tr valign="top"><td><code>/</code></td><td>specifies a repeating increment for all fields except dayOfWeek.
 *     For example, <code>6/7</code> for the <code>hours</code> field equates to <code>6,13,20</code>.</td></tr>
 * <tr valign="top"><td><code>#</code></td><td>specifies an ordinal day of week. For example,
 *     <code>FRI#1,SAT#L</code> is the first Friday and last Saturday of the month.
 *     <br><code>#</code> cannot be used within ranges (<code>-</code>) and increments (<code>/</code>).</td></tr>
 * <tr valign="top"><td><code>*</code></td><td>indicates any value is permitted.</td></tr>
 * <tr valign="top"><td><code>L</code></td><td>indicates the last day of the month.
 *         <code>2L</code> indicates the second-to-last day, and so forth.</td></tr>
 * </table>
 * <br>
 * <table width="90%">
 * <caption><b>Cron Expression Examples</b></caption>
 * <tr valign="top"><td><code>0 * * * *</code></td><td>every hour at the top of the hour</td></tr>
 * <tr valign="top"><td><code>0 9-17 * * MON-FRI</code></td><td>weekdays from 9am to 5pm, at the top of each hour</td></tr>
 * <tr valign="top"><td><code>0 13/3 * MAY-SEP SAT,SUN</code></td><td>weekends from May to September, every 3 hours, starting at 1pm</td></tr>
 * <tr valign="top"><td><code>30 10 * APR,AUG TUE#2,TUE#L</code></td><td>second and last Tuesdays of April and August at 10:30am</td></tr>
 * <tr valign="top"><td><code>15 22 4 10,20,L * *</code></td><td>4:22:15 AM on the 10th, 20th, and last day of every month</td></tr>
 * <tr valign="top"><td><code>0 8-11,13-16 2L JAN-MAR *</code></td><td>8AM-11AM and 1PM-4PM on the second-to-last day of January, February, and March</td></tr>
 * </table>
 * <p>A constructor is provided that accepts a cron expression such as the above and a timezone id. For example,
 * <pre>
 * trigger = new CronTrigger("0 7 * SEP-MAY MON-FRI", ZoneId.of("America/New_York"));
 * </pre>
 * <p>Another constructor allows cron fields to be specified in a fluent manner, in any order. For example,
 * <pre>
 * trigger = new CronTrigger(ZoneId.of("America/Los_Angeles"))
 *           .months(Month.DECEMBER)
 *           .daysOfMonth(24)
 *           .hours(16, 18);
 * </pre>
 * <p>
 * The {@link #getNextRunTime(LastExecution, ZonedDateTime) getNextRunTime} method of this trigger
 * determines the next run time based on the cron schedule.
 * The {@link #skipRun(LastExecution, ZonedDateTime) skipRun} method always returns false
 * unless overridden by a subclass.
 * <p>
 * Methods of this class that configure the cron expression fields are not thread safe. It is the
 * responsibility of the caller to ensure that initialization of the <code>CronTrigger</code>
 * happens before it is supplied to a {@link ManagedScheduledExecutorService} and that the
 * <code>CronTrigger</code> is not subsequently modified.
 * <p>
 * You can subclass <code>CronTrigger</code> to provide for more complex logic, such as in the following
 * example of combining two triggers to schedule twice-a-month payroll on the 15th and last day of month
 * or the prior Fridays when the former fall on a weekend:
 * <pre>
 * public class PayrollTrigger extends CronTrigger {
 *     private final CronTrigger fridaysBeforeWeekendPayrollDay;
 *
 *     PayrollTrigger() {
 *         // Every 15th and last day of the month that is a weekday,
 *         super("0 10 15,L * MON-FRI", ZoneId.of("America/Chicago"));
 *
 *         // Every 13th, 14th, third-to-last, and second-to-last day of the month that is a Friday,
 *         fridaysBeforeWeekendPayrollDay = new CronTrigger(
 *                 "0 10 13,14,3L,2L * FRI", getZoneId());
 *     }
 *
 *     public ZonedDateTime getNextRunTime(LastExecution lastExec, ZonedDateTime scheduledAt) {
 *         ZonedDateTime time1 = super.getNextRunTime(lastExec, scheduledAt);
 *         ZonedDateTime time2 = fridaysBeforeWeekendPayrollDay.getNextRunTime(lastExec, scheduledAt);
 *         return time1.isBefore(time2) ? time1 : time2;
 *     }
 * }
 * </pre>
 *
 * @since 3.0
 */
public class CronTrigger implements ZonedTrigger {
    private static final Map<String, Integer> DAYS_OF_WEEK = new HashMap<String, Integer>(7);
    private static final Map<String, Integer> MONTHS = new HashMap<String, Integer>(12);
    static {
        for (DayOfWeek day : DayOfWeek.values())
            DAYS_OF_WEEK.put(day.name().substring(0, 3), day.getValue());
        for (Month month : Month.values())
            MONTHS.put(month.name().substring(0, 3), month.getValue());
    }

    private static final int[] ALL_DAYS_OF_MONTH = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31 };
    private static final int[] ALL_DAYS_OF_WEEK = new int[] { 1, 2, 3, 4, 5, 6, 7 };
    private static final int[] ALL_MONTHS = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
    private static final int LAST = -1;
    private static final int[] ZERO = new int[] { 0 };

    // cron expression fields are parsed into lists
    private int[] daysOfMonth = ALL_DAYS_OF_MONTH;
    private int[] daysOfWeek = ALL_DAYS_OF_WEEK;
    private int[] hours = ZERO;
    private int[] minutes = ZERO;
    private int[] months = ALL_MONTHS;
    private int[] seconds = ZERO;
    private final ZoneId zone;

    /**
     * Constructor that accepts a cron expression.
     *
     * @param cron cron expression.
     * @param zone timezone ID to use for {@link java.time.ZonedDateTime} that is supplied to
     *        {@link #getNextRunTime(LastExecution, ZonedDateTime) getNextRunTime} and
     *        {@link #skipRun(LastExecution, ZonedDateTime) skipRun} methods.
     *        Null indicates to use the system default.
     */
    public CronTrigger(String cron, ZoneId zone) {
        this(zone);
        String[] c = cron.split(" ");
        if (c.length == 5)
            minutes(c[0]).hours(c[1]).daysOfMonth(c[2]).months(c[3]).daysOfWeek(c[4]);
        else if (c.length == 6)
            seconds(c[0]).minutes(c[1]).hours(c[2]).daysOfMonth(c[3]).months(c[4]).daysOfWeek(c[5]);
        else
            throw new IllegalArgumentException(cron);
    }

    /**
     * Constructor for the fluent configuration pattern.
     * Seconds, minutes, and hours default to 0. The remaining fields default to *.
     *
     * @param zone timezone ID to use for {@link java.time.ZonedDateTime} that is supplied to
     *        {@link #getNextRunTime(LastExecution, ZonedDateTime) getNextRunTime} and
     *        {@link #skipRun(LastExecution, ZonedDateTime) skipRun} methods.
     *        Null indicates to use the system default.
     */
    public CronTrigger(ZoneId zone) {
        this.zone = zone == null ? ZoneId.systemDefault() : zone;
    }

    /**
     * Using the cron schedule, and based on the end of the most recent execution
     * (or absent that, the initial scheduling time), retrieve the next time
     * that the task should run after.
     *
     * @param lastExecutionInfo information about the last execution of the task. 
     *                          This value will be null if the task has not yet run.
     * @param taskScheduledTime the date/time at which the
     *                          {@code ManagedScheduledExecutorService.schedule}
     *                          method was invoked to schedule the task.
     * @return the date/time after which the next execution of the task should start.
     * @throws DateTimeException if a next time cannot be determined from the cron expression.
     */
    public ZonedDateTime getNextRunTime(LastExecution lastExecutionInfo, ZonedDateTime taskScheduledTime) {
        return next(lastExecutionInfo == null ? taskScheduledTime : lastExecutionInfo.getRunEnd(zone));
    }

    /**
     * Returns the timezone to use for
     * {@link java.time.ZonedDateTime ZonedDateTime} that is supplied to the
     * {@link #getNextRunTime(LastExecution, java.time.ZonedDateTime) getNextRunTime} and
     * {@link #skipRun(LastExecution, java.time.ZonedDateTime) skipRun} methods.
     *
     * @return timezone to use for operations on this trigger.
     */
    public final ZoneId getZoneId() {
        return zone;
    }

    /**
     * Configure the day-of-month cron field, overwriting any previous value for day-of-month.
     *
     * @param d one or more day numbers ranging from 1 to 31.
     * @return this instance.
     */
    public CronTrigger daysOfMonth(int... d) {
        daysOfMonth = parse("daysOfMonth", 1, 31, d);
        return this;
    }

    /**
     * Configure the day-of-month cron field, overwriting any previous value for day-of-month.
     *
     * @param d dayOfMonth cron field. For example, <code>15,L</code>.
     * @return this instance.
     */
    public CronTrigger daysOfMonth(String d) {
        daysOfMonth = parse("daysOfMonth", 1, 31, LAST, d, CronTrigger::parseDayOfMonth);
        return this;
    }

    /**
     * Configure the day-of-week cron field, overwriting any previous value for day-of-week.
     *
     * @param d one or more days of the week.
     * @return this instance.
     */
    public CronTrigger daysOfWeek(DayOfWeek... d) {
        if (d.length == 0)
            throw new IllegalArgumentException("daysOfWeek: []");
        daysOfWeek = Arrays.stream(d).map(DayOfWeek::getValue).sorted().distinct().mapToInt(Integer::intValue).toArray();
        return this;
    }

    /**
     * Configure the day-of-week cron field, overwriting any previous value for day-of-week.
     *
     * @param d dayOfWeek cron field. For example, <code>MON-FRI,SAT#L</code>.
     * @return this instance.
     */
    public CronTrigger daysOfWeek(String d) {
        daysOfWeek = parse("daysOfWeek", 1, 7, 49, d, CronTrigger::parseDayOfWeek);
        return this;
    }

    /**
     * Configure the hours cron field, overwriting any previous value for hours.
     *
     * @param h one or more hour values ranging from 0 to 23.
     * @return this instance.
     */
    public CronTrigger hours(int... h) {
        hours = parse("hours", 0, 23, h);
        return this;
    }

    /**
     * Configure the hours cron field, overwriting any previous value for hours.
     *
     * @param h hours cron field. For example, <code>9-17</code> for 9am to 5pm.
     * @return this instance.
     */
    public CronTrigger hours(String h) {
        hours = parse("hours", 0, 23, 23, h, Integer::parseInt);
        return this;
    }

    /**
     * Configure the minutes cron field, overwriting any previous value for minutes.
     *
     * @param m one or more minute values ranging from 0 to 59.
     * @return this instance.
     */
    public CronTrigger minutes(int... m) {
        minutes = parse("minutes", 0, 59, m);
        return this;
    }

    /**
     * Configure the minutes cron field, overwriting any previous value for minutes.
     *
     * @param m minutes cron field. For example, <code>5/10</code> for 10 minute intervals
     *        starting at 5 minutes after the hour (:05, :15, :25, :35, :45, :55).
     * @return this instance.
     */
    public CronTrigger minutes(String m) {
        minutes = parse("minutes", 0, 59, 59, m, Integer::parseInt);
        return this;
    }

    /**
     * Configure the month cron field, overwriting any previous value for month.
     *
     * @param m one or more months.
     * @return this instance.
     */
    public CronTrigger months(Month... m) {
        if (m.length == 0)
            throw new IllegalArgumentException("months: []");
        months = Arrays.stream(m).map(Month::getValue).sorted().distinct().mapToInt(Integer::intValue).toArray();
        return this;
    }

    /**
     * Configure the months cron field, overwriting any previous value for months.
     *
     * @param m months cron field. For example, <code>SEP-NOV,FEB-MAY</code>.
     * @return this instance.
     */
    public CronTrigger months(String m) {
        months = parse("months", 1, 12, 12, m, CronTrigger::parseMonth);
        return this;
    }

    /**
     * Configure the seconds cron field, overwriting any previous value for seconds.
     *
     * @param s one or more seconds values ranging from 0 to 59.
     * @return this instance.
     */
    public CronTrigger seconds(int... s) {
        seconds = parse("seconds", 0, 59, s);
        return this;
    }

    /**
     * Configure the seconds cron field, overwriting any previous value for seconds.
     *
     * @param s seconds cron field. For example, <code>30</code>.
     * @return this instance.
     */
    public CronTrigger seconds(String s) {
        seconds = parse("seconds", 0, 59, 59, s, Integer::parseInt);
        return this;
    }

    /**
     * Readable representation of the CronTrigger, which displays fields in list form
     * or with the * character for brevity.
     * <p>
     * For example,
     * <pre>CronTrigger@89abcdef seconds 0, minutes 0, hours 9, *, months 3,6,9,12, SAT#2,SAT#4</pre>
     *
     * @return readable representation of the parsed cron expression.
     */
    public String toString() {
        StringBuilder s = new StringBuilder("CronTrigger@").append(Integer.toHexString(hashCode()));
        toStringBuilder(s, "seconds", seconds, 60);
        toStringBuilder(s, "minutes", minutes, 60);
        toStringBuilder(s, "hours", hours, 24);
        toStringBuilder(s, "days", daysOfMonth, 31);
        toStringBuilder(s, "months", months, 12);
        if (daysOfWeek.length == 7 && daysOfWeek[6] == 7)
            s.append(" *");
        else
            for (int i = 0; i < daysOfWeek.length; i++) {
                int d = ((daysOfWeek[i] - 1) % 7 + 1);
                int ord = (daysOfWeek[i] - 1) / 7;
                s.append(i == 0 ? ' ' : ',').append(DayOfWeek.of(d).name().substring(0, 3));
                if (ord > 0)
                    s.append('#').append(ord == 6 ? "L" : ord);
            }
        return s.toString();
    }

    /**
     * Utility method for repeated logic in toString.
     */
    private void toStringBuilder(StringBuilder s, String label, int[] list, int max) {
        if (list.length == max)
            s.append(" *");
        else {
            s.append(' ').append(label).append(' ');
            StringBuilder l = new StringBuilder();
            for (int i = 0; i < list.length; i++)
                if (list[i] < 0)
                    l.append(list[i] == LAST ? "L" : (-list[i] + "L")).append(',');
                else
                    s.append(list[i]).append(',');
            s.append(l.toString());
        }
    }

    /**
     * Advance to the next date/time according to the cron schedule.
     *
     * @param from the date/time from which to compute the next time.
     * @return next date/time according to the cron schedule, or the original time if it matches.
     */
    protected ZonedDateTime next(ZonedDateTime from) {
        ZonedDateTime time = from.getNano() == 0 ? from : from.withNano(0).withSecond(from.getSecond() + 1);

        for (int i = 0; i < 1000 /** just in case expression never matches */ && time != null; ++i) {
            int year = time.getYear();
            int m = Arrays.binarySearch(months, time.getMonthValue());
            if (m < 0)
                time = nextMonth(-m-2, year, time);
            else {
                int dayOfMonth = time.getDayOfMonth();
                int lastDayOfMonth = time.getMonth().length(Year.isLeap(year));
                int d = Arrays.binarySearch(daysOfMonth, dayOfMonth);
                int l = Arrays.binarySearch(daysOfMonth, dayOfMonth - lastDayOfMonth - 1);
                if (d < 0 && l < 0)
                    time = nextDayOfMonth(-d-2, -l-2, m, year, time);
                else {
                    d = d < 0 ? (-d-2) : d;
                    l = l < 0 ? (-l-2) : l;
                    int dayOfWeek = time.getDayOfWeek().getValue();
                    int ordinalDayOfWeek = (((dayOfMonth - 1) / 7) + 1) * 7 + dayOfWeek;
                    int dayOfLastWeek = lastDayOfMonth - dayOfMonth >= 7 ? -1 : (6 * 7 + dayOfWeek);

                    if (Arrays.binarySearch(daysOfWeek, dayOfWeek) < 0 // (TUE)
                     && Arrays.binarySearch(daysOfWeek, ordinalDayOfWeek) < 0 // (WED#3)
                     && Arrays.binarySearch(daysOfWeek, dayOfLastWeek) < 0) // (THU#L)
                        time = nextDayOfMonth(d, l, m, year, time);
                    else {
                        int h = Arrays.binarySearch(hours, time.getHour());
                        if (h < 0)
                            time = nextHour(-h-2, d, l, dayOfMonth, m, year, time);
                        else {
                            int min = Arrays.binarySearch(minutes, time.getMinute());
                            if (min < 0)
                                time = nextMinute(-min-2, h, d, l, dayOfMonth, m, year, time);
                            else {
                                int s = Arrays.binarySearch(seconds, time.getSecond());
                                if (s < 0)
                                    time = nextSecond(-s-2, min, h, d, l, dayOfMonth, m, year, time);
                                else
                                    return time;
                            }
                        }
                    }
                }
            }
        }
        throw new DateTimeException("Unable to determine next time after " + from + " with " + this);
    }
    
    /**
     * Advance to next day of month.
     */
    private ZonedDateTime nextDayOfMonth(int d, int l, int m, int year, ZonedDateTime time) {
        int lastDayOfMonth = Month.of(months[m]).length(Year.isLeap(year));
        int dd = ++d < daysOfMonth.length ? daysOfMonth[d] : 32;
        int ld = ++l < daysOfMonth.length && daysOfMonth[l] < 0 ? (1 + lastDayOfMonth + daysOfMonth[l]) : 32;
        int dayOfMonth = Math.min(dd, ld);
        if (dayOfMonth > lastDayOfMonth)
            return nextMonth(m, year, time);

        return ZonedDateTime.of(year, months[m], dayOfMonth, hours[0], minutes[0], seconds[0], 0, time.getZone());
    }

    /**
     * Advance to next hour.
     */
    private ZonedDateTime nextHour(int h, int d, int l, int dayOfMonth, int m, int year, ZonedDateTime time) {
        ZonedDateTime dst = ZonedDateTime.of(year, months[m], dayOfMonth, hours[h], minutes[0], seconds[0], 0, time.getZone());
        ZonedDateTime std = dst.plusHours(1);
        if (dst.getHour() == std.getHour() && time.isAfter(dst) && time.isBefore(std))
            return std; // Daylight Saving Time --> Standard Time
        else if (++h < hours.length)
            return ZonedDateTime.of(year, months[m], dayOfMonth, hours[h], minutes[0], seconds[0], 0, time.getZone());
        else
            return nextDayOfMonth(d, l, m, year, time);
    }

    /**
     * Advance to next minute.
     */
    private ZonedDateTime nextMinute(int min, int h, int d, int l, int dayOfMonth, int m, int year, ZonedDateTime time) {
        if (++min < minutes.length)
            return time.withMinute(minutes[min]).withSecond(seconds[0]);
        else
            return nextHour(h, d, l, dayOfMonth, m, year, time);
    }

    /**
     * Advance to next month.
     */
    private ZonedDateTime nextMonth(int m, int year, ZonedDateTime time) {
        int dayOfMonth, lastDayOfMonth, cycles = 0;
        do {
            if (++m >= months.length) {
                m = 0;
                year++;
            }
            int d = 0;
            for (int i = 0; i < daysOfMonth.length && daysOfMonth[i] < 0; d = ++i) ;
            lastDayOfMonth = Month.of(months[m]).length(Year.isLeap(year));
            int dd = d < daysOfMonth.length && daysOfMonth[d] > 0 ? daysOfMonth[d] : 32;
            int ld = daysOfMonth[0] < 0 ? (1 + lastDayOfMonth + daysOfMonth[0]) : 32;
            dayOfMonth = Math.min(dd, ld);
        } while ((dayOfMonth < 1 || dayOfMonth > lastDayOfMonth) && (++cycles < 1000));

        return cycles < 1000
                ? ZonedDateTime.of(year, months[m], dayOfMonth, hours[0], minutes[0], seconds[0], 0, zone)
                : null; // expression never matched, for example 0 0 30 FEB *
    }

    /**
     * Advance to next second.
     */
    private ZonedDateTime nextSecond(int s, int min, int h, int d, int l, int dayOfMonth, int m, int year, ZonedDateTime time) {
        if (++s < seconds.length)
            return time.withSecond(seconds[s]);
        else
            return nextMinute(min, h, d, l, dayOfMonth, m, year, time);
    }

    private static final void add(SortedSet<Integer> vals, int start, int end, int increment) {
        for (int val = start; val <= end; val += increment)
            vals.add(val);
    }

    /**
     * Validate that the supplied list values are within the allowed range for the cron field type.
     *
     * @param type cron field type, such as months or hours.
     * @param min  minimum allowed value
     * @param max  maximum allowed value
     * @param list supplied list of values
     * @return sorted list of values with any duplicates removed.
     */
    private int[] parse(String type, int min, int max, int[] list) {
        if (list.length == 0)
            throw new IllegalArgumentException(type + ": []");
        SortedSet<Integer> vals = new TreeSet<Integer>();
        for (int i = 0; i < list.length; i++) {
            if (list[i] < min || list[i] > max)
                throw new IllegalArgumentException(type + ": " + list[i]);
            else
                vals.add(list[i]);
        }
        return vals.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Validate that the supplied list values are within the allowed range for the cron field type.
     *
     * @param type   cron field type, such as months or hours.
     * @param min    minimum allowed normal value
     * @param max    maximum allowed normal value
     * @param maxExt maximum allowed special value (L or SUN#L), or max if no special values allowed for this field.
     * @param field  the field's cron expression
     * @param parser parser function, such as Integer::parseInt or CronTrigger::parseMonth
     * @return sorted list of values with any duplicates removed.
     */
    private int[] parse(String name, int min, int max, int maxExt, String field, Function<String, Integer> parser) {
        if (field == null || field.length() == 0)
            throw new IllegalArgumentException(name + ": []");

        SortedSet<Integer> vals = new TreeSet<Integer>();
        for (String f : field.split(","))
            try {
                if ("*".equals(f) || "?".equals(f)) // all values
                    add(vals, min, max, 1);
                else {
                    int slash = f.indexOf('/', 1);
                    if (slash > 0 && slash < f.length() - 1) { // increment
                        int val1 = slash == 1 && f.charAt(0) == '*' ? min : parser.apply(f.substring(0, slash));
                        int increment = parser.apply(f.substring(slash + 1));
                        if (val1 < min || val1 > max || increment < 1 || maxExt > max /* dayOfWeek */)
                            throw new IllegalArgumentException(name + ": " + f);
                        add(vals, val1, max, increment);
                    } else {
                        int dash = f.indexOf('-', 1);
                        if (dash > 0 && dash < f.length() - 1) { // range
                            int val1 = parser.apply(f.substring(0, dash));
                            String end = f.substring(dash + 1);
                            int val2 = "L".equals(end) ? max : parser.apply(end);
                            if (val1 < min || val1 > max || val2 < min || val2 > max)
                                throw new IllegalArgumentException(name + ": " + f);
                            if (val2 >= val1) {
                                add(vals, val1, val2, 1);
                            } else { // wrap around (eg. OCT-MAY)
                                add(vals, val1, max, 1);
                                add(vals, min, val2, 1);
                            }
                        } else { // single value
                            int val = parser.apply(f);
                            if ((val < min || val > maxExt) && maxExt != LAST)
                                throw new IllegalArgumentException(name + ": " + f);
                            vals.add(val);
                        }
                    }
                }
            } catch (NumberFormatException x) {
                throw new IllegalArgumentException(name + ": " + f, x);
            }
        return vals.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Convert dayOfMonth value to 1-31, or negative for days from the end of the month
     * For example, L is the last day (-1) and 2L is the second to last day (-2).
     */
    private static int parseDayOfMonth(String day) throws IllegalArgumentException {
        try {
            if (day.charAt(day.length() - 1) == 'L') {
                int d = day.length() == 1 ? LAST : -Integer.parseInt(day.substring(0, day.length() - 1));
                if (d > -1 || d < -31)
                    throw new IllegalArgumentException("dayOfMonth: " + day);
                return d;
            } else {
                int d = Integer.parseInt(day);
                if (d < 1 || d > 31)
                    throw new IllegalArgumentException("dayOfMonth: " + day);
                return d;
            }
        } catch (NumberFormatException x) {
            throw new IllegalArgumentException("dayOfMonth: " + day, x);
        }
    }

    /**
     * Convert dayOfWeek value to 1-49 where first 7 are standard week days,
     * next 35 are ordinal 1st-5th of each day, and final 7 are ordinal last for each day.
     */
    private static int parseDayOfWeek(String day) throws IllegalArgumentException {
        int ordinal = 0;
        int n = day.indexOf('#'); // ordinal day of week within month (TUE#2 for second Tuesday)
        try {
            if (n > 0) {
                ordinal = day.charAt(n + 1) == 'L' ? 6 : Integer.parseInt(day.substring(n + 1));
                if (ordinal < 1 || ordinal > 6)
                    throw new IllegalArgumentException("dayOfWeek: " + day);
                day = day.substring(0, n);
            }
            if (day.length() < 3) {
                int d = Integer.parseInt(day);
                return d == 0 ? 7 : d;
            }
        } catch (NumberFormatException x) {
            throw new IllegalArgumentException("dayOfWeek: " + day, x);
        }
        Integer d = DAYS_OF_WEEK.get(day = day.toUpperCase());
        if (d == null)
            d = DayOfWeek.valueOf(day).getValue();
        return 7 * ordinal + d;
    }

    /**
     * Convert month value to 1-12.
     */
    private static int parseMonth(String month) throws IllegalArgumentException {
        if (month.length() < 3)
            try {
                return Integer.parseInt(month);
            } catch (NumberFormatException x) {
                throw new IllegalArgumentException("month: " + month, x);
            }
        Integer m = MONTHS.get(month = month.toUpperCase());
        return m == null ? Month.valueOf(month).getValue() : m;
    }
}
