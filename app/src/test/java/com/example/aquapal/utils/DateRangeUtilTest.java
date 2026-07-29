package com.example.aquapal.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class DateRangeUtilTest {

    @Test
    public void endOfDay_setsTimeToLastMillisecondOfDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.JULY, 29, 14, 30, 0);
        Date midAfternoon = cal.getTime();

        long endMillis = DateRangeUtil.endOfDay(midAfternoon);

        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(endMillis);

        assertEquals(23, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, result.get(Calendar.MINUTE));
        assertEquals(59, result.get(Calendar.SECOND));
        assertEquals(999, result.get(Calendar.MILLISECOND));
        assertEquals(29, result.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void startOfDay_setsTimeToMidnight() {
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.JULY, 29, 14, 30, 0);
        Date midAfternoon = cal.getTime();

        long startMillis = DateRangeUtil.startOfDay(midAfternoon);

        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(startMillis);

        assertEquals(0, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, result.get(Calendar.MINUTE));
        assertEquals(0, result.get(Calendar.SECOND));
        assertEquals(0, result.get(Calendar.MILLISECOND));
        assertEquals(29, result.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void endOfDay_afterMidnightEntry_isIncludedInSameDayRange() {
        // Regression test for the bug where "today" entries recorded with a
        // time-of-day later than 00:00:00 were excluded from week/month totals
        // because the range end was computed at midnight instead of end-of-day.
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.JULY, 29, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = DateRangeUtil.startOfDay(cal.getTime());
        long endOfDay = DateRangeUtil.endOfDay(cal.getTime());

        Calendar entryTime = Calendar.getInstance();
        entryTime.set(2026, Calendar.JULY, 29, 23, 45, 0);
        long entryMillis = entryTime.getTimeInMillis();

        assertTrue(entryMillis >= startOfDay && entryMillis <= endOfDay);
    }

    @Test
    public void startOfToday_and_endOfToday_bracketCurrentTime() {
        long now = System.currentTimeMillis();
        assertTrue(DateRangeUtil.startOfToday() <= now);
        assertTrue(DateRangeUtil.endOfToday() >= now);
    }
}
