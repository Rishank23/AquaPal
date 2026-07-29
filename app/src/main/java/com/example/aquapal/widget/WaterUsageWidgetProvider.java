package com.example.aquapal.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.example.aquapal.R;
import com.example.aquapal.activities.SplashScreen;
import com.example.aquapal.utils.Constants;
import com.example.aquapal.utils.DateRangeUtil;
import com.example.aquapal.utils.GoalCalculator;
import com.example.aquapal.waterDb.AppExecutors;
import com.example.aquapal.waterDb.WaterDatabaseHelper;

public class WaterUsageWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId);
        }
    }

    public static void updateAllWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName component = new ComponentName(context, WaterUsageWidgetProvider.class);
        int[] widgetIds = manager.getAppWidgetIds(component);
        for (int widgetId : widgetIds) {
            updateWidget(context, manager, widgetId);
        }
    }

    private static void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        final long startOfDay = DateRangeUtil.startOfToday();
        final long endOfDay = DateRangeUtil.endOfToday();

        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        final float goal = prefs.getFloat(Constants.KEY_DAILY_GOAL, Constants.DEFAULT_DAILY_GOAL);
        final WaterDatabaseHelper db = WaterDatabaseHelper.getDb(context.getApplicationContext());

        AppExecutors.getInstance().diskIO().execute(() -> {
            float todayTotal = db.waterDao().getQuantityByCustomDates(startOfDay, endOfDay);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_water_usage);
            String usageText = String.format("%.0f / %.0f L", todayTotal, goal);
            views.setTextViewText(R.id.widgetUsageText, usageText);
            views.setProgressBar(R.id.widgetProgressBar, 100, GoalCalculator.percentOf(todayTotal, goal), false);
            views.setContentDescription(R.id.widgetRoot, "AquaPal, today's water usage " + usageText + ", tap to open app");

            Intent openApp = new Intent(context, SplashScreen.class);
            openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openApp,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent);

            appWidgetManager.updateAppWidget(widgetId, views);
        });
    }
}
