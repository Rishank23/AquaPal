package com.example.aquapal.utils;

public class GoalCalculator {

    public static int percentOf(float total, float goal) {
        if (goal <= 0) return 0;
        int percent = Math.round((total / goal) * 100);
        return Math.max(0, Math.min(100, percent));
    }

    public static boolean isOverBudget(float total, float goal) {
        return total > goal;
    }
}
