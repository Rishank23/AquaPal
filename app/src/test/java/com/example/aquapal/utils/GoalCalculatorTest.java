package com.example.aquapal.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GoalCalculatorTest {

    @Test
    public void percentOf_halfwayToGoal_returns50() {
        assertEquals(50, GoalCalculator.percentOf(75f, 150f));
    }

    @Test
    public void percentOf_overGoal_capsAt100() {
        assertEquals(100, GoalCalculator.percentOf(300f, 150f));
    }

    @Test
    public void percentOf_zeroGoal_returnsZeroInsteadOfDividingByZero() {
        assertEquals(0, GoalCalculator.percentOf(50f, 0f));
    }

    @Test
    public void percentOf_negativeGoal_returnsZero() {
        assertEquals(0, GoalCalculator.percentOf(50f, -10f));
    }

    @Test
    public void percentOf_zeroTotal_returnsZero() {
        assertEquals(0, GoalCalculator.percentOf(0f, 150f));
    }

    @Test
    public void isOverBudget_totalExceedsGoal_returnsTrue() {
        assertTrue(GoalCalculator.isOverBudget(200f, 150f));
    }

    @Test
    public void isOverBudget_totalEqualsGoal_returnsFalse() {
        assertFalse(GoalCalculator.isOverBudget(150f, 150f));
    }

    @Test
    public void isOverBudget_totalBelowGoal_returnsFalse() {
        assertFalse(GoalCalculator.isOverBudget(100f, 150f));
    }
}
