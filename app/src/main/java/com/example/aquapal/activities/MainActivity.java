package com.example.aquapal.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;

import com.example.aquapal.R;
import com.example.aquapal.adapters.SectionsPageAdapter;
import com.example.aquapal.fragments.ChartFragment;
import com.example.aquapal.fragments.UsageFragment;
import com.example.aquapal.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;

    public static FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        viewPager.setAdapter(adapter);
    }
}