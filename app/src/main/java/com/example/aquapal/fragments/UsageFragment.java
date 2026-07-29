package com.example.aquapal.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aquapal.R;
import com.example.aquapal.adapters.CustomAdapter;
import com.example.aquapal.utils.Constants;
import com.example.aquapal.utils.DateRangeUtil;
import com.example.aquapal.utils.GoalCalculator;
import com.example.aquapal.waterDb.AppExecutors;
import com.example.aquapal.waterDb.UsageViewModel;
import com.example.aquapal.waterDb.WaterDatabaseHelper;
import com.example.aquapal.waterDb.WaterUsage;
import com.example.aquapal.widget.WaterUsageWidgetProvider;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class UsageFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private RecyclerView rv;
    private List<WaterUsage> waterUsages;
    private CustomAdapter customAdapter;
    public UsageViewModel usageViewModel;
    private WaterDatabaseHelper mAppDb;

    private View emptyStateLayout;
    private View goalCard;
    private TextView goalProgressTextView;
    private ProgressBar goalProgressBar;
    private TextView emptyStateTitleTextView;
    private TextView emptyStateSubtitleTextView;
    private Spinner categoryFilterSpinner;
    private String selectedCategoryFilter = null;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view=inflater.inflate(R.layout.fragment_expense,container,false);
        rv = view.findViewById(R.id.transactionRecyclerView);
        rv.setHasFixedSize(true);
        waterUsages = new ArrayList<>();
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        goalCard = view.findViewById(R.id.goalCard);
        goalProgressTextView = view.findViewById(R.id.goalProgressTextView);
        goalProgressBar = view.findViewById(R.id.goalProgressBar);
        emptyStateTitleTextView = view.findViewById(R.id.emptyStateTitleTextView);
        emptyStateSubtitleTextView = view.findViewById(R.id.emptyStateSubtitleTextView);
        categoryFilterSpinner = view.findViewById(R.id.categoryFilterSpinner);

        mAppDb = WaterDatabaseHelper.getDb(getContext());
        prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);

        goalCard.setOnClickListener(v -> showSetGoalDialog());

        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.category_filter_array, android.R.layout.simple_spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilterSpinner.setAdapter(filterAdapter);
        categoryFilterSpinner.setOnItemSelectedListener(this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                final WaterUsage removedUsage = customAdapter.getWaterUsages().get(position);

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mAppDb.waterDao().removeUsage(removedUsage);
                        WaterUsageWidgetProvider.updateAllWidgets(view.getContext().getApplicationContext());
                    }
                });

                Snackbar.make(view, "Deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                mAppDb.waterDao().insertUsage(removedUsage);
                                WaterUsageWidgetProvider.updateAllWidgets(view.getContext().getApplicationContext());
                            }
                        }))
                        .show();
            }
        }).attachToRecyclerView(rv);

        setupViewModel();

        return view;
    }

    public void setupViewModel(){

        usageViewModel = ViewModelProviders.of(this).get(UsageViewModel.class);
        usageViewModel.getWaterUsage()
                .observe(getViewLifecycleOwner(), new Observer<List<WaterUsage>>() {
                    @Override
                    public void onChanged(List<WaterUsage> waterUsagesFromDb) {
                        waterUsages = waterUsagesFromDb;
                        applyCategoryFilter();
                        refreshTodayGoalProgress();
                    }
                });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedCategoryFilter = position == 0 ? null : parent.getItemAtPosition(position).toString();
        applyCategoryFilter();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void applyCategoryFilter() {
        List<WaterUsage> filtered;
        if (selectedCategoryFilter == null) {
            filtered = waterUsages;
        } else {
            filtered = new ArrayList<>();
            for (WaterUsage usage : waterUsages) {
                if (selectedCategoryFilter.equals(usage.getCategory())) {
                    filtered.add(usage);
                }
            }
        }

        boolean isEmpty = filtered.isEmpty();
        emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rv.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty && selectedCategoryFilter != null) {
            emptyStateTitleTextView.setText("No entries for " + selectedCategoryFilter);
            emptyStateSubtitleTextView.setText("Try a different category filter");
        } else {
            emptyStateTitleTextView.setText("No water usage logged yet");
            emptyStateSubtitleTextView.setText("Tap the + button to add your first entry");
        }

        customAdapter = new CustomAdapter(getActivity(), filtered);
        rv.setAdapter(customAdapter);
    }

    private void refreshTodayGoalProgress() {
        final long startOfDay = DateRangeUtil.startOfToday();
        final long endOfDay = DateRangeUtil.endOfToday();

        final float goal = prefs.getFloat(Constants.KEY_DAILY_GOAL, Constants.DEFAULT_DAILY_GOAL);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final float todayTotal = mAppDb.waterDao().getQuantityByCustomDates(startOfDay, endOfDay);

                AppExecutors.getInstance().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (getContext() == null) return;

                        goalProgressTextView.setText(String.format("%.0f / %.0f L", todayTotal, goal));
                        goalProgressBar.setProgress(GoalCalculator.percentOf(todayTotal, goal));

                        int colorRes = GoalCalculator.isOverBudget(todayTotal, goal) ? R.color.goalOverBudget : R.color.goalOnTrack;
                        goalProgressBar.setProgressTintList(
                                android.content.res.ColorStateList.valueOf(ContextCompat.getColor(getContext(), colorRes)));
                    }
                });
            }
        });
    }

    private void showSetGoalDialog() {
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(prefs.getFloat(Constants.KEY_DAILY_GOAL, Constants.DEFAULT_DAILY_GOAL)));
        input.setSelection(input.getText().length());

        int padding = (int) (getResources().getDisplayMetrics().density * 20);
        input.setPadding(padding, padding / 2, padding, padding / 2);

        new AlertDialog.Builder(getContext())
                .setTitle("Set daily water goal (Litres)")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        float newGoal = Float.parseFloat(input.getText().toString());
                        if (newGoal > 0) {
                            prefs.edit().putFloat(Constants.KEY_DAILY_GOAL, newGoal).apply();
                            refreshTodayGoalProgress();
                        }
                    } catch (NumberFormatException ignored) {
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
