package com.example.aquapal.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aquapal.R;
import com.example.aquapal.activities.AddExpenseActivity;
import com.example.aquapal.utils.Constants;
import com.example.aquapal.waterDb.WaterDatabaseHelper;
import com.example.aquapal.waterDb.WaterUsage;

import java.text.SimpleDateFormat;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    Context context;
    private List<WaterUsage> waterUsages;
    private WaterDatabaseHelper appDatabase;

    public CustomAdapter(Context context, List<WaterUsage> waterUsages){
        this.context = context;
        this.waterUsages = waterUsages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.categoryTextViewrv.setText(waterUsages.get(position).getCategory());

        holder.quantityTextViewrv.setText(String.valueOf(waterUsages.get(position).getQuantity()));
        holder.quantityTextViewrv.setTextColor(Color.parseColor("#ff5722"));

        SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy");
        String dateToBeSet=sdf.format(waterUsages.get(position).getDate());
        holder.dateTextViewrv.setText(dateToBeSet);
        holder.descriptionTextViewrv.setText(waterUsages.get(position).getDescription());
    }

    @Override
    public int getItemCount() {
        if (waterUsages == null || waterUsages.size() == 0){
            return 0;
        } else {
            return waterUsages.size();
        }
    }

    public List<WaterUsage> getWaterUsages(){
        return waterUsages;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView categoryTextViewrv;
        TextView quantityTextViewrv;
        TextView descriptionTextViewrv;
        TextView dateTextViewrv;

        public ViewHolder(View itemView) {
            super(itemView);

            categoryTextViewrv = itemView.findViewById(R.id.categoryTextViewrv);
            quantityTextViewrv = itemView.findViewById(R.id.quantityTextViewrv);
            descriptionTextViewrv = itemView.findViewById(R.id.descriptionTextViewrv);
            dateTextViewrv = itemView.findViewById(R.id.dateTextViewrv);

            appDatabase = WaterDatabaseHelper.getDb(context);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(context, AddExpenseActivity.class);

                    SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy");
                    String date = sdf.format(waterUsages.get(getAdapterPosition()).getDate());

                    intent.putExtra("from", Constants.editUsageString);
                    intent.putExtra("quantity", waterUsages.get(getAdapterPosition()).getQuantity());
                    intent.putExtra("description", waterUsages.get(getAdapterPosition()).getDescription());
                    intent.putExtra("date", date);
                    intent.putExtra("category", waterUsages.get(getAdapterPosition()).getCategory());
                    intent.putExtra("id", waterUsages.get(getAdapterPosition()).getId());
                    context.startActivity(intent);
                }
            });
        }
    }
}
