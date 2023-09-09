package com.example.aquapal.waterDb;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

@Database(entities = WaterUsage.class, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class WaterDatabaseHelper extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "waterDb";
    private static WaterDatabaseHelper instance;

    public static synchronized WaterDatabaseHelper getDb(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            WaterDatabaseHelper.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public abstract WaterDao waterDao();
}
