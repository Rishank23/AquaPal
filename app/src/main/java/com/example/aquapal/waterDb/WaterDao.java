package com.example.aquapal.waterDb;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WaterDao {
    @Query("select * from WaterUsage order by date DESC")
    LiveData<List<WaterUsage>> getAllWaterUsage();

    @Query("select sum(quantity) from WaterUsage")
    float getSumAllTimeWaterUsage();

    @Query("select sum(quantity) from WaterUsage where category=:category")
    float getSumUsageByCategory(String category);

    @Query("select sum(quantity) from WaterUsage where date between :startDate and :endDate")
    float getQuantityByCustomDates(long startDate, long endDate);

    @Query("select sum(quantity) from WaterUsage where category=:category and date between :startDate and :endDate")
    float getSumQuantityByCategoryCustomDate(String category, long startDate, long endDate);

    @Query("select min(date) from waterusage")
    long getFirstDate();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUsage(WaterUsage waterUsage);

    @Delete
    void removeUsage(WaterUsage waterUsage);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateUsage(WaterUsage waterUsage);
}