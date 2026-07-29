package com.example.aquapal.fragments;

import static com.example.aquapal.activities.MainActivity.fab;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.aquapal.R;
import com.example.aquapal.utils.Constants;
import com.example.aquapal.utils.DateRangeUtil;
import com.example.aquapal.utils.UsageList;
import com.example.aquapal.waterDb.AppExecutors;
import com.example.aquapal.waterDb.WaterDatabaseHelper;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChartFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private WaterDatabaseHelper mAppDb;
    PieChart pieChart;
    Spinner spinner;

    private TextView usageTv;
    private TextView dateTv;
    private TextView costTv;
    private SharedPreferences prefs;
    private float lastTotalLitres = 0f;

    private float balanceAmount;
    private float drinkingUsage, cookingUsage, cleaningUsage, plantsUsage, bathingUsage, othersUsage;

    long firstDate;

    ArrayList<UsageList> usageList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_balance,container,false);

        pieChart= view.findViewById(R.id.balancePieChart);
        spinner = view.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        mAppDb = WaterDatabaseHelper.getDb(getContext());

        usageTv = view.findViewById(R.id.totalUsageTextView);

        dateTv = view.findViewById(R.id.dateTextView);
        costTv = view.findViewById(R.id.costTextView);
        prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        costTv.setOnClickListener(v -> showSetCostDialog());
        usageList = new ArrayList<>();
        getAllBalanceAmount();
        setupPieChart();
        return view;
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.date_array,
                android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.i("fragment", String.valueOf(isVisibleToUser));
        if (isVisibleToUser){
            setupSpinner();
            fab.setVisibility(View.GONE);
        } else{
            fab.setVisibility(View.VISIBLE);
        }
    }

    private void setupPieChart() {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if(spinner.getSelectedItemPosition()==0)
                    getAllPieValues();
                else if(spinner.getSelectedItemPosition()==1) {
                    try {
                        getWeekPieValues();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                else if(spinner.getSelectedItemPosition()==2){
                    try {
                        getMonthPieValues();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                usageList.clear();

             if(drinkingUsage !=0 )
                 usageList.add(new UsageList("Drinking", drinkingUsage));
             if(cookingUsage !=0 )
                 usageList.add(new UsageList("Cooking", cookingUsage));
             if(cleaningUsage !=0 )
                 usageList.add(new UsageList("Cleaning/Washing", cleaningUsage));
             if(plantsUsage !=0 )
                 usageList.add(new UsageList("Plants", plantsUsage));
             if(bathingUsage !=0 )
                 usageList.add(new UsageList("Bathing", bathingUsage));
             if(othersUsage !=0 )
                 usageList.add(new UsageList("Others", othersUsage));

                // Update the chart on the main thread only after the disk read above
                // has actually finished, otherwise the pie chart can render stale/empty data.
                AppExecutors.getInstance().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {

                        List<PieEntry> pieEntries = new ArrayList<>();
                        List<Integer> pieColors = new ArrayList<>();
                        for(int i = 0 ; i <usageList.size(); i++){
                            pieEntries.add(new PieEntry(usageList.get(i).getQuantity(), usageList.get(i).getCategory()));
                            pieColors.add(ContextCompat.getColor(getContext(), Constants.colorForCategory(usageList.get(i).getCategory())));
                        }
                        pieChart.setVisibility(View.VISIBLE);
                        PieDataSet dataSet = new PieDataSet(pieEntries,null);
                        dataSet.setColors(pieColors);
                        PieData pieData = new PieData(dataSet);

                        pieData.setValueTextSize(16);
                        pieData.setValueTextColor(Color.WHITE);
                        pieData.setValueFormatter(new PercentFormatter());
                        pieChart.setUsePercentValues(true);
                        pieChart.setData(pieData);
                        pieChart.animateY(1000);
                        pieChart.invalidate();

                        pieChart.getDescription().setText("");
                        Legend l=pieChart.getLegend();
                        l.setPosition(Legend.LegendPosition.LEFT_OF_CHART);
                        l.setTextColor(ContextCompat.getColor(getContext(), R.color.chartLabelColor));
                    }
                });
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        if(adapterView.getSelectedItemPosition()==0){
            getAllBalanceAmount();
            setupPieChart();
        }

        else if (adapterView.getSelectedItemPosition() == 1){
            //This week
            try {
                getWeekBalanceAmount();
                setupPieChart();
                Log.e("Rishank", "working fine");
            }
            catch (ParseException e) {
                Log.e("Rishank", e.getMessage());
                e.printStackTrace();
            }
        }
        else if(adapterView.getSelectedItemPosition()==2){
            //This month
            try {
                getMonthBalanceAmount();
                setupPieChart();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    private void getAllPieValues(){
        drinkingUsage = mAppDb.waterDao().getSumUsageByCategory("Drinking");
        cookingUsage = mAppDb.waterDao().getSumUsageByCategory("Cooking");
        cleaningUsage = mAppDb.waterDao().getSumUsageByCategory("Cleaning/Washing");
        plantsUsage = mAppDb.waterDao().getSumUsageByCategory("Plants");
        bathingUsage = mAppDb.waterDao().getSumUsageByCategory("Bathing");
        othersUsage = mAppDb.waterDao().getSumUsageByCategory("Others");
    }

    private void getWeekPieValues() throws ParseException {
        Calendar calendar;
        calendar=Calendar.getInstance();

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String startDate = "", endDate = "";
        // Set the calendar to sunday of the current week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        startDate = df.format(calendar.getTime());
        Date sDate = df.parse(startDate);
        final long sdate = sDate.getTime();

        calendar.add(Calendar.DATE, 6);
        endDate = df.format(calendar.getTime());
        final long edate = DateRangeUtil.endOfDay(df.parse(endDate));

        drinkingUsage = mAppDb.waterDao().getSumQuantityByCategoryCustomDate("Drinking", sdate, edate);
        cookingUsage = mAppDb.waterDao().getSumQuantityByCategoryCustomDate("Cooking", sdate, edate);
        cleaningUsage = mAppDb.waterDao().getSumQuantityByCategoryCustomDate("Cleaning/Washing", sdate, edate);
        plantsUsage = mAppDb.waterDao().getSumQuantityByCategoryCustomDate("Plants", sdate, edate);
        bathingUsage = mAppDb.waterDao().getSumQuantityByCategoryCustomDate("Bathing",sdate,edate);
        othersUsage = mAppDb.waterDao().getSumQuantityByCategoryCustomDate("Others",sdate,edate);
    }

    private void getMonthPieValues() throws ParseException{

        Calendar calendar;
        calendar=Calendar.getInstance();

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String startDate = "", endDate = "";

        calendar.set(Calendar.DAY_OF_MONTH,1);
        startDate = df.format(calendar.getTime());
        Date sDate=df.parse(startDate);
        final long sdate=sDate.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        endDate = df.format(calendar.getTime());
        final long edate = DateRangeUtil.endOfDay(df.parse(endDate));

        drinkingUsage = mAppDb.waterDao().getSumQuantityByCategoryCustomDate("Drinking", sdate, edate);
        cookingUsage = mAppDb.waterDao().getSumQuantityByCategoryCustomDate("Cooking", sdate, edate);
        cleaningUsage = mAppDb.waterDao().getSumQuantityByCategoryCustomDate("Cleaning/Washing", sdate, edate);
        plantsUsage = mAppDb.waterDao().getSumQuantityByCategoryCustomDate("Plants", sdate, edate);
        bathingUsage = mAppDb.waterDao().getSumQuantityByCategoryCustomDate("Bathing",sdate,edate);
        othersUsage = mAppDb.waterDao().getSumQuantityByCategoryCustomDate("Others",sdate,edate);
    }

    private void getAllBalanceAmount(){

        //get date of first transaction and today's date, then compute the total,
        //all on the disk IO thread so the UI update below always sees fresh values.
       AppExecutors.getInstance().diskIO().execute(new Runnable() {
           @Override
           public void run() {
               firstDate = mAppDb.waterDao().getFirstDate();
               balanceAmount = mAppDb.waterDao().getSumAllTimeWaterUsage();

               AppExecutors.getInstance().mainThread().execute(new Runnable() {
                   @Override
                   public void run() {
                       SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                       String first = df.format(new Date(firstDate));
                       Date today = Calendar.getInstance().getTime();
                       String todaysDate = df.format(today);
                       dateTv.setText(first + " - " + todaysDate);
                       usageTv.setText(balanceAmount + " Litres");
                       updateCostDisplay(balanceAmount);
                   }
               });
           }
       });
    }

    private void getWeekBalanceAmount() throws ParseException {
        Calendar calendar;
        calendar=Calendar.getInstance();

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String startDate = "", endDate = "";
        // Set the calendar to sunday of the current week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        startDate = df.format(calendar.getTime());
        Date sDate=df.parse(startDate);
        final long sdate=sDate.getTime();

        calendar.add(Calendar.DATE, 6);
        endDate = df.format(calendar.getTime());
        final long edate = DateRangeUtil.endOfDay(df.parse(endDate));

        String dateString = startDate + " - " + endDate;
        dateTv.setText(dateString);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                balanceAmount = mAppDb.waterDao().getQuantityByCustomDates(sdate, edate);
                AppExecutors.getInstance().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        usageTv.setText(balanceAmount+" Litres");
                        updateCostDisplay(balanceAmount);
                    }
                });
            }
        });
    }

    private void getMonthBalanceAmount() throws ParseException {
        Calendar calendar;
        calendar=Calendar.getInstance();

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String startDate = "", endDate = "";

        calendar.set(Calendar.DAY_OF_MONTH,1);
        startDate = df.format(calendar.getTime());
        Date sDate=df.parse(startDate);
        final long sdate=sDate.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        endDate = df.format(calendar.getTime());
        final long edate = DateRangeUtil.endOfDay(df.parse(endDate));

        String dateString = startDate + " - " + endDate;
        dateTv.setText(dateString);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                balanceAmount = mAppDb.waterDao().getQuantityByCustomDates(sdate, edate);
                AppExecutors.getInstance().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        usageTv.setText(balanceAmount+" Litres");
                        updateCostDisplay(balanceAmount);
                    }
                });
            }
        });
    }

    private void updateCostDisplay(float totalLitres) {
        if (costTv == null) return;
        lastTotalLitres = totalLitres;
        float rate = prefs.getFloat(Constants.KEY_COST_PER_LITRE, 0f);
        if (rate > 0) {
            costTv.setText(String.format(Locale.getDefault(), "Est. Cost: %.2f (tap to edit rate)", totalLitres * rate));
        } else {
            costTv.setText("Tap to set water cost rate");
        }
    }

    private void showSetCostDialog() {
        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(prefs.getFloat(Constants.KEY_COST_PER_LITRE, 0f)));
        input.setSelection(input.getText().length());

        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Cost per litre")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        float rate = Float.parseFloat(input.getText().toString());
                        if (rate >= 0) {
                            prefs.edit().putFloat(Constants.KEY_COST_PER_LITRE, rate).apply();
                            updateCostDisplay(lastTotalLitres);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
