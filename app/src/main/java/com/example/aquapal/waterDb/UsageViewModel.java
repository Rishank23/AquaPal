package com.example.aquapal.waterDb;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class UsageViewModel extends AndroidViewModel {

    public final LiveData<List<WaterUsage>> waterUsage;
    private WaterDatabaseHelper waterDatabaseHelper;


    public UsageViewModel(@NonNull Application application) {
        super(application);

        waterDatabaseHelper = WaterDatabaseHelper.getDb(this.getApplication());

        waterUsage = waterDatabaseHelper.waterDao().getAllWaterUsage();

    }


    public LiveData<List<WaterUsage>> getWaterUsage() {
        return waterUsage;
    }

//    The code below can be ignored

    public void updateTransaction(WaterUsage waterUsage){
        new updateUsageDetails(waterDatabaseHelper).execute(waterUsage);
    }

   private static class updateUsageDetails extends AsyncTask<WaterUsage,Void,Void>{

        private WaterDatabaseHelper mdb;

        public updateUsageDetails(WaterDatabaseHelper waterDatabaseHelper){
            mdb = waterDatabaseHelper;
        }
       @Override
       protected Void doInBackground(WaterUsage... waterUsages) {
            mdb.waterDao().updateUsage(waterUsages[0]);
           return null;
       }
   }
}