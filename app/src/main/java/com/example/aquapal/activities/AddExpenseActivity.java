package com.example.aquapal.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.example.aquapal.R;
import com.example.aquapal.utils.Constants;
import com.example.aquapal.waterDb.AppExecutors;
import com.example.aquapal.waterDb.UsageViewModel;
import com.example.aquapal.waterDb.WaterDatabaseHelper;
import com.example.aquapal.waterDb.WaterUsage;
import com.example.aquapal.widget.WaterUsageWidgetProvider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AddExpenseActivity extends AppCompatActivity {

    private Button saveUsageBtn;

    TextInputEditText quantityTextInputEditText;
    TextInputEditText descriptionTextInputEditText;
    TextInputLayout descriptionTextInputLayout;
    TextView dateTextView;
    LinearLayout dateLinearLayout;
    Spinner categorySpinner;
    ArrayList<String> categories;
    Calendar myCalendar;

    String description;
    Date dateOfExpense;

    private DatePickerDialog datePickerDialog;
    private static WaterDatabaseHelper waterDatabaseHelper;
    float quantity;
    String categoryOfUsage;

    //Variable to keep track from where it came to this activity
    String intentFrom;

    UsageViewModel usageViewModel;

    int transactionid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        saveUsageBtn = findViewById(R.id.saveUsageBtn);

        quantityTextInputEditText = findViewById(R.id.quantityTextInputEditText);
        descriptionTextInputEditText = findViewById(R.id.descriptionTextInputEditText);
        descriptionTextInputLayout = findViewById(R.id.descriptionTextInputLayout);
        dateTextView = findViewById(R.id.dateTextView);
        dateLinearLayout = findViewById(R.id.dateLinerLayout);
        categorySpinner = findViewById(R.id.categorySpinner);

        waterDatabaseHelper = WaterDatabaseHelper.getDb(getApplicationContext());

        saveUsageBtn.setOnClickListener(view -> {

            float parsedQuantity = 0f;
            boolean quantityValid = !quantityTextInputEditText.getText().toString().isEmpty();
            if (quantityValid) {
                parsedQuantity = Float.parseFloat(quantityTextInputEditText.getText().toString());
                quantityValid = parsedQuantity > 0;
            }

            if (!quantityValid || descriptionTextInputEditText.getText().toString().isEmpty()) {

                if (quantityTextInputEditText.getText().toString().isEmpty())
                    quantityTextInputEditText.setError("This field cannot be empty");
                else if (!quantityValid)
                    quantityTextInputEditText.setError("Quantity must be greater than 0");
                if (descriptionTextInputEditText.getText().toString().isEmpty())
                    descriptionTextInputEditText.setError("Please write some description");

            } else {
                quantity = parsedQuantity;
                description = descriptionTextInputEditText.getText().toString();
                dateOfExpense = myCalendar.getTime();

                categoryOfUsage = categories.get(categorySpinner.getSelectedItemPosition());

                final WaterUsage mWaterUsage = new WaterUsage(
                        quantity,
                        categoryOfUsage,
                        description,
                        dateOfExpense
                );

                View currentFocus = getCurrentFocus();
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (currentFocus != null && inputManager != null) {
                    inputManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

                if(intentFrom.equals(Constants.addUsageString)){
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            waterDatabaseHelper.waterDao().insertUsage(mWaterUsage);
                            WaterUsageWidgetProvider.updateAllWidgets(getApplicationContext());
                        }
                    });

                    Snackbar.make(saveUsageBtn,"Added",Snackbar.LENGTH_LONG).show();
                }else{
                    mWaterUsage.setId(transactionid);
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            waterDatabaseHelper.waterDao().updateUsage(mWaterUsage);
                            WaterUsageWidgetProvider.updateAllWidgets(getApplicationContext());
                        }
                    });

                    Snackbar.make(saveUsageBtn,"Updated",Snackbar.LENGTH_LONG).show();
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1000);
            }
        });

        //usageViewModel = ViewModelProviders.of(this).get(UsageViewModel.class);

        categories = new ArrayList<>();

        myCalendar = Calendar.getInstance();
        setDateToTextView();

        //First task here is to determine from where this activity is launched from the 4 possibilities

        Intent intent = getIntent();

        intentFrom = intent.getStringExtra("from");

        if(intentFrom.equals(Constants.addUsageString)){
            categoryOfUsage = Constants.usageCategory;
            setTitle("Add Water Usage");
            categories.add("Drinking");
            categories.add("Cooking");
            categories.add("Cleaning/Washing");
            categories.add("Plants");
            categories.add("Bathing");
            categories.add("Others");
            categorySpinner.setAdapter(new ArrayAdapter<>(AddExpenseActivity.this,
                    android.R.layout.simple_list_item_1, categories));
        }else{
            categoryOfUsage = Constants.expenseCategory;
            setTitle("Edit Water Usage");
            quantityTextInputEditText.setText(String.valueOf(intent.getFloatExtra("quantity", 0)));
            quantityTextInputEditText.setSelection(quantityTextInputEditText.getText().length());
            descriptionTextInputEditText.setText(intent.getStringExtra("description"));
            descriptionTextInputEditText.setSelection(descriptionTextInputEditText.getText().length());
            dateTextView.setText(intent.getStringExtra("date"));
            transactionid = intent.getIntExtra("id",-1);

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            try {
                Date date = sdf.parse(intent.getStringExtra("date"));
                myCalendar.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            categories.add("Drinking");
            categories.add("Cooking");
            categories.add("Cleaning/Washing");
            categories.add("Plants");
            categories.add("Bathing");
            categories.add("Others");
            categorySpinner.setAdapter(new ArrayAdapter<>(AddExpenseActivity.this, android.R.layout.simple_list_item_1, categories));
            categorySpinner.setSelection(categories.indexOf(intent.getStringExtra("category")));
        }

        dateLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
    }

    public void showDatePicker() {

        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                setDateToTextView();
            }
        };

        DatePickerDialog datePickerDialog=new DatePickerDialog(AddExpenseActivity.this,dateSetListener,
                myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    public void setDateToTextView() {
        Date date = myCalendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String dateToBeSet = sdf.format(date);
        dateTextView.setText(dateToBeSet);
    }
}