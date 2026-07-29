package com.example.aquapal.fragments;

import static com.example.aquapal.activities.MainActivity.fab;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.aquapal.R;
import com.example.aquapal.waterDb.AppExecutors;
import com.example.aquapal.waterDb.WaterDatabaseHelper;
import com.example.aquapal.waterDb.WaterUsage;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TrendsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private WaterDatabaseHelper mAppDb;
    private BarChart barChart;
    private Spinner rangeSpinner;
    private TextView emptyTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trends, container, false);

        barChart = view.findViewById(R.id.trendBarChart);
        rangeSpinner = view.findViewById(R.id.trendRangeSpinner);
        emptyTextView = view.findViewById(R.id.trendEmptyTextView);

        mAppDb = WaterDatabaseHelper.getDb(getContext());

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.trend_range_array, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rangeSpinner.setAdapter(arrayAdapter);
        rangeSpinner.setOnItemSelectedListener(this);

        loadWeeklyTrend();

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (fab == null) return;
        if (isVisibleToUser) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            loadWeeklyTrend();
        } else {
            loadMonthlyTrend();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void loadWeeklyTrend() {
        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
        final long endMillis = end.getTimeInMillis();

        Calendar start = Calendar.getInstance();
        start.add(Calendar.DATE, -6);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        final long startMillis = start.getTimeInMillis();

        final List<String> dayLabels = new ArrayList<>();
        final Map<String, Float> totals = new LinkedHashMap<>();
        SimpleDateFormat labelFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        Calendar cursor = (Calendar) start.clone();
        for (int i = 0; i < 7; i++) {
            String label = labelFormat.format(cursor.getTime());
            dayLabels.add(label);
            totals.put(dayKey(cursor), 0f);
            cursor.add(Calendar.DATE, 1);
        }

        AppExecutors.getInstance().diskIO().execute(() -> {
            List<WaterUsage> usages = mAppDb.waterDao().getUsageInRange(startMillis, endMillis);
            Calendar entryCal = Calendar.getInstance();
            for (WaterUsage usage : usages) {
                entryCal.setTime(usage.getDate());
                String key = dayKey(entryCal);
                Float current = totals.get(key);
                if (current != null) {
                    totals.put(key, current + usage.getQuantity());
                }
            }

            List<Float> values = new ArrayList<>(totals.values());
            AppExecutors.getInstance().mainThread().execute(() -> renderChart(dayLabels, values));
        });
    }

    private void loadMonthlyTrend() {
        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
        final long endMillis = end.getTimeInMillis();

        Calendar start = Calendar.getInstance();
        start.add(Calendar.MONTH, -5);
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        final long startMillis = start.getTimeInMillis();

        final List<String> monthLabels = new ArrayList<>();
        final Map<String, Float> totals = new LinkedHashMap<>();
        SimpleDateFormat labelFormat = new SimpleDateFormat("MMM", Locale.getDefault());
        Calendar cursor = (Calendar) start.clone();
        for (int i = 0; i < 6; i++) {
            String label = labelFormat.format(cursor.getTime());
            monthLabels.add(label);
            totals.put(monthKey(cursor), 0f);
            cursor.add(Calendar.MONTH, 1);
        }

        AppExecutors.getInstance().diskIO().execute(() -> {
            List<WaterUsage> usages = mAppDb.waterDao().getUsageInRange(startMillis, endMillis);
            Calendar entryCal = Calendar.getInstance();
            for (WaterUsage usage : usages) {
                entryCal.setTime(usage.getDate());
                String key = monthKey(entryCal);
                Float current = totals.get(key);
                if (current != null) {
                    totals.put(key, current + usage.getQuantity());
                }
            }

            List<Float> values = new ArrayList<>(totals.values());
            AppExecutors.getInstance().mainThread().execute(() -> renderChart(monthLabels, values));
        });
    }

    private String dayKey(Calendar cal) {
        return cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.DAY_OF_YEAR);
    }

    private String monthKey(Calendar cal) {
        return cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH);
    }

    private void renderChart(List<String> labels, List<Float> values) {
        if (getContext() == null) return;

        boolean hasData = false;
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            entries.add(new BarEntry(i, values.get(i)));
            if (values.get(i) > 0) hasData = true;
        }

        emptyTextView.setVisibility(hasData ? View.GONE : View.VISIBLE);

        BarDataSet dataSet = new BarDataSet(entries, "Litres");
        dataSet.setColor(Color.parseColor("#0288D1"));
        dataSet.setValueTextSize(11f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        int labelColor = androidx.core.content.ContextCompat.getColor(getContext(), R.color.chartLabelColor);
        dataSet.setValueTextColor(labelColor);

        barChart.setData(barData);
        barChart.getDescription().setText("");
        barChart.getLegend().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setTextColor(labelColor);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setTextColor(labelColor);

        barChart.animateY(600);
        barChart.invalidate();
    }
}
