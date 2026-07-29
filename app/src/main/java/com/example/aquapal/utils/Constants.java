package com.example.aquapal.utils;

import com.example.aquapal.R;

public class Constants {
    public static final String expenseCategory="Expense";
    public static final String addUsageString = "addWater";
    public static final String editUsageString = "editWater";
    public static final String usageCategory = "category";

    public static final String PREFS_NAME = "aquapal_prefs";
    public static final String KEY_DAILY_GOAL = "daily_goal_litres";
    public static final float DEFAULT_DAILY_GOAL = 150f;
    public static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    public static final int REMINDER_HOUR = 20;
    public static final int REMINDER_MINUTE = 0;
    public static final String NOTIFICATION_CHANNEL_ID = "aquapal_reminders";
    public static final int REMINDER_REQUEST_CODE = 1001;
    public static final int REMINDER_NOTIFICATION_ID = 2001;
    public static final String WIDGET_UPDATE_ACTION = "com.example.aquapal.WIDGET_UPDATE";
    public static final String KEY_COST_PER_LITRE = "cost_per_litre";

    public static int colorForCategory(String category) {
        if (category == null) return R.color.categoryOthers;
        switch (category) {
            case "Drinking": return R.color.categoryDrinking;
            case "Cooking": return R.color.categoryCooking;
            case "Cleaning/Washing": return R.color.categoryCleaning;
            case "Plants": return R.color.categoryPlants;
            case "Bathing": return R.color.categoryBathing;
            default: return R.color.categoryOthers;
        }
    }
}
