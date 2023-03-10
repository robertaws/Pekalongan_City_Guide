package com.binus.pekalongancityguide;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {
    private List<Food> item;

    public FoodAdapter(List<Food> items) {
        item = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_culinary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Food item1 = item.get(position);
        holder.layoutBg.setBackgroundResource(item1.getFoodImage());
        holder.foodnameTV.setText(item1.getFoodName());
        holder.foodnameTV2.setText(item1.getFoodName2());
    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView foodnameTV, foodnameTV2;
        RelativeLayout layoutBg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            foodnameTV = itemView.findViewById(R.id.food_name);
            foodnameTV2 = itemView.findViewById(R.id.food_name2);
            layoutBg = itemView.findViewById(R.id.layoutfoodImage);
        }
    }
}
