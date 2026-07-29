package com.example.aquapal.waterDb;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class WaterDaoTest {

    private WaterDatabaseHelper db;
    private WaterDao dao;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        db = Room.inMemoryDatabaseBuilder(context, WaterDatabaseHelper.class)
                .allowMainThreadQueries()
                .build();
        dao = db.waterDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void sumAllTimeWaterUsage_onEmptyTable_returnsZeroNotCrash() {
        // Regression test: SQLite SUM() over zero rows returns NULL, which
        // previously risked a crash mapping into Room's primitive float return type.
        assertEquals(0f, dao.getSumAllTimeWaterUsage(), 0.001f);
    }

    @Test
    public void getFirstDate_onEmptyTable_returnsZero() {
        assertEquals(0L, dao.getFirstDate());
    }

    @Test
    public void insertAndSumAllTimeWaterUsage_returnsCorrectTotal() {
        dao.insertUsage(new WaterUsage(10f, "Drinking", "glass", new Date()));
        dao.insertUsage(new WaterUsage(5f, "Cooking", "rice", new Date()));

        assertEquals(15f, dao.getSumAllTimeWaterUsage(), 0.001f);
    }

    @Test
    public void getSumUsageByCategory_filtersCorrectly() {
        dao.insertUsage(new WaterUsage(10f, "Drinking", "glass", new Date()));
        dao.insertUsage(new WaterUsage(5f, "Cooking", "rice", new Date()));
        dao.insertUsage(new WaterUsage(3f, "Drinking", "bottle", new Date()));

        assertEquals(13f, dao.getSumUsageByCategory("Drinking"), 0.001f);
        assertEquals(5f, dao.getSumUsageByCategory("Cooking"), 0.001f);
        assertEquals(0f, dao.getSumUsageByCategory("Plants"), 0.001f);
    }

    @Test
    public void getQuantityByCustomDates_includesEntryAtRangeBoundaries() {
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.JULY, 29, 23, 45, 0);
        Date lateInDay = cal.getTime();

        dao.insertUsage(new WaterUsage(20f, "Drinking", "late entry", lateInDay));

        Calendar startCal = Calendar.getInstance();
        startCal.set(2026, Calendar.JULY, 29, 0, 0, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.set(2026, Calendar.JULY, 29, 23, 59, 59);
        endCal.set(Calendar.MILLISECOND, 999);

        float total = dao.getQuantityByCustomDates(startCal.getTimeInMillis(), endCal.getTimeInMillis());
        assertEquals(20f, total, 0.001f);
    }

    @Test
    public void removeUsage_deletesEntry() {
        WaterUsage usage = new WaterUsage(10f, "Drinking", "glass", new Date());
        dao.insertUsage(usage);

        List<WaterUsage> all = dao.getAllWaterUsageOnce();
        assertEquals(1, all.size());

        dao.removeUsage(all.get(0));
        assertEquals(0, dao.getAllWaterUsageOnce().size());
    }

    @Test
    public void insertAll_batchInsertsAllEntries() {
        dao.insertAll(java.util.Arrays.asList(
                new WaterUsage(1f, "Drinking", "a", new Date()),
                new WaterUsage(2f, "Cooking", "b", new Date()),
                new WaterUsage(3f, "Plants", "c", new Date())
        ));

        assertEquals(3, dao.getAllWaterUsageOnce().size());
        assertEquals(6f, dao.getSumAllTimeWaterUsage(), 0.001f);
    }
}
