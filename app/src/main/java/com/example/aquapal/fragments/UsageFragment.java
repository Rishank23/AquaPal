package com.example.aquapal.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aquapal.R;
import com.example.aquapal.adapters.CustomAdapter;
import com.example.aquapal.waterDb.AppExecutors;
import com.example.aquapal.waterDb.UsageViewModel;
import com.example.aquapal.waterDb.WaterDatabaseHelper;
import com.example.aquapal.waterDb.WaterUsage;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class UsageFragment extends Fragment {

    private RecyclerView rv;
    private List<WaterUsage> waterUsages;
    private CustomAdapter customAdapter;
    public UsageViewModel usageViewModel;
    private WaterDatabaseHelper mAppDb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view=inflater.inflate(R.layout.fragment_expense,container,false);
        rv = view.findViewById(R.id.transactionRecyclerView);
        rv.setHasFixedSize(true);
        waterUsages = new ArrayList<>();
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAppDb = WaterDatabaseHelper.getDb(getContext());

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();

                        List<WaterUsage> transactionEntries = customAdapter.getWaterUsages();
                        mAppDb.waterDao().removeUsage(transactionEntries.get(position));

                    }
                });

                Snackbar.make(view,"Deleted",Snackbar.LENGTH_LONG).show();
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
                        for(int i=0; i<waterUsages.size(); ++i){
                            String description = waterUsages.get(i).getDescription();
                            float quantity = waterUsages.get(i).getQuantity();
                        }

                        customAdapter = new CustomAdapter(getActivity(), waterUsages);
                        rv.setAdapter(customAdapter);
                    }
                });
    }
}
