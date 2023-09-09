package com.example.aquapal.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aquapal.R;

import java.util.ArrayList;
import java.util.Random;

public class SplashScreen extends AppCompatActivity {

    private TextView quoteTextView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        quoteTextView = findViewById(R.id.quoteTextView);

        ArrayList<String> waterQuotes = new ArrayList<>();
        waterQuotes.add("Water is the driving force of all nature. - Leonardo da Vinci");
        waterQuotes.add("Save water, it doesn’t grow on trees.");
        waterQuotes.add("Save water, shower with a friend.");
        waterQuotes.add("Be a water-saving superhero, not a water-wasting villain!");
        waterQuotes.add("Water you waiting for? Save it!");
        waterQuotes.add("H2O: Handle to Optimize (your) usage!");
        waterQuotes.add("Use water responsibly – the mermaids are watching you!");
        waterQuotes.add("Don't make your toilet work overtime, it's not getting paid!");
        waterQuotes.add("Conserve water, or you'll have to survive on dehydrated pizza. Yikes!");
        waterQuotes.add("Water you waiting for? Start saving, it's 'liquid' gold!");
        waterQuotes.add("Don't be a water 'hog,' be a water 'dog' and fetch those savings!");
        waterQuotes.add("Water is our heritage, let's pass it on to future generations.");
        waterQuotes.add("Keep calm and save water, or you'll turn into a 'desert-t'!");
        waterQuotes.add("Save water like it's your job, because Earth is your office.");
        waterQuotes.add("Water you up to? Save some, buddy!");
        waterQuotes.add("Wasting water is like a bad joke - it's not funny!");
        waterQuotes.add("Use water wisely, or you'll end up 'current'-ly regretting it.");
        waterQuotes.add("Conserve water: because it's the 'pour-fect' thing to do!");
        waterQuotes.add("When in drought, save it out!");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int order = sharedPreferences.getInt("quote", 0);

        Random random = new Random();
        String randomQuote = waterQuotes.get(order);
        Log.d("Rishank", randomQuote);
        quoteTextView.setText(randomQuote);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Create an intent to start the MainActivity
                sharedPreferences.edit().putInt("quote", (order+1)%waterQuotes.size()).apply();
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish(); // Finish the current activity to prevent going back to it.
            }
        }, 3000); // Delay for 3 seconds (adjust as needed)
    }
}