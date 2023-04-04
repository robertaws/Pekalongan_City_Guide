package com.binus.pekalongancityguide.Adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Food;
import com.binus.pekalongancityguide.Layout.FoodDetails;
import com.binus.pekalongancityguide.R;

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
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Food item1 = item.get(position);
        holder.layoutBg.setBackgroundResource(item1.getFoodImage());
        holder.foodnameTV.setText(item1.getFoodName());
        holder.foodnameTV2.setText(item1.getFoodName2());
        holder.fooddescTV.setText(item1.getFoodDesc());
        holder.foodIV1.setBackgroundResource(item1.getFoodImage1());
        holder.foodIV2.setBackgroundResource(item1.getFoodImage2());
        holder.foodIV3.setBackgroundResource(item1.getFoodImage3());
        holder.layoutBg.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), FoodDetails.class);
            intent.putExtra("imeg", item.get(position).getFoodImage());
            intent.putExtra("imeg1", item.get(position).getFoodImage1());
            intent.putExtra("imeg2", item.get(position).getFoodImage2());
            intent.putExtra("imeg3", item.get(position).getFoodImage3());
            intent.putExtra("name", item.get(position).getFoodName());
            intent.putExtra("desc", item.get(position).getFoodDesc());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView foodnameTV, foodnameTV2,fooddescTV;
        RelativeLayout layoutBg;
        ImageView foodIV1,foodIV2,foodIV3;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fooddescTV = itemView.findViewById(R.id.food_desc);
            foodnameTV = itemView.findViewById(R.id.food_name);
            foodnameTV2 = itemView.findViewById(R.id.food_name2);
            layoutBg = itemView.findViewById(R.id.layoutfoodImage);
            foodIV1 = itemView.findViewById(R.id.foodimage_1);
            foodIV2 = itemView.findViewById(R.id.foodimage_2);
            foodIV3 = itemView.findViewById(R.id.foodimage_3);
        }
    }
}
