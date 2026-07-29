package com.example.aquapal.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.aquapal.R;
import com.example.aquapal.adapters.SectionsPageAdapter;
import com.example.aquapal.fragments.ChartFragment;
import com.example.aquapal.fragments.TrendsFragment;
import com.example.aquapal.fragments.UsageFragment;
import com.example.aquapal.reminders.ReminderScheduler;
import com.example.aquapal.utils.Constants;
import com.example.aquapal.utils.CsvExporter;
import com.example.aquapal.utils.CsvImporter;
import com.example.aquapal.waterDb.AppExecutors;
import com.example.aquapal.waterDb.WaterDatabaseHelper;
import com.example.aquapal.waterDb.WaterUsage;
import com.example.aquapal.widget.WaterUsageWidgetProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;

    public static FloatingActionButton fab;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    enableReminder();
                } else {
                    Toast.makeText(this, "Notification permission is needed for reminders", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> importCsvLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    importCsv(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager=findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout=findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), AddExpenseActivity.class);
            intent.putExtra("from", Constants.addUsageString);
            startActivity(intent);
        });
    }

    private void setupViewPager(ViewPager viewPager){
        SectionsPageAdapter adapter=new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new UsageFragment(),"Usage");
        adapter.addFragment(new ChartFragment(),"Chart");
        adapter.addFragment(new TrendsFragment(),"Trends");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        MenuItem reminderItem = menu.findItem(R.id.action_reminder);
        reminderItem.setChecked(prefs.getBoolean(Constants.KEY_REMINDER_ENABLED, false));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_export) {
            exportCsv();
            return true;
        } else if (item.getItemId() == R.id.action_import) {
            importCsvLauncher.launch("text/*");
            return true;
        } else if (item.getItemId() == R.id.action_reminder) {
            toggleReminder(item);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleReminder(MenuItem item) {
        boolean newState = !item.isChecked();
        item.setChecked(newState);

        if (newState) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                enableReminder();
            }
        } else {
            disableReminder();
        }
    }

    private void enableReminder() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(Constants.KEY_REMINDER_ENABLED, true).apply();
        ReminderScheduler.schedule(this);
        Toast.makeText(this, "Daily reminder enabled for 8:00 PM", Toast.LENGTH_SHORT).show();
    }

    private void disableReminder() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(Constants.KEY_REMINDER_ENABLED, false).apply();
        ReminderScheduler.cancel(this);
        Toast.makeText(this, "Daily reminder disabled", Toast.LENGTH_SHORT).show();
    }

    private void importCsv(Uri uri) {
        final WaterDatabaseHelper db = WaterDatabaseHelper.getDb(getApplicationContext());
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                CsvImporter.Result result = CsvImporter.parse(getContentResolver(), uri);
                if (!result.imported.isEmpty()) {
                    db.waterDao().insertAll(result.imported);
                    WaterUsageWidgetProvider.updateAllWidgets(getApplicationContext());
                }

                AppExecutors.getInstance().mainThread().execute(() -> {
                    String message = "Imported " + result.imported.size() + " entries";
                    if (result.skipped > 0) {
                        message += ", skipped " + result.skipped + " invalid rows";
                    }
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                });
            } catch (IOException e) {
                AppExecutors.getInstance().mainThread().execute(() ->
                        Toast.makeText(MainActivity.this, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void exportCsv() {
        final WaterDatabaseHelper db = WaterDatabaseHelper.getDb(getApplicationContext());
        AppExecutors.getInstance().diskIO().execute(() -> {
            final List<WaterUsage> entries = db.waterDao().getAllWaterUsageOnce();
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (entries.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No data to export yet", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    startActivity(CsvExporter.buildShareIntent(MainActivity.this, entries));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
