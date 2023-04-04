package com.binus.pekalongancityguide.Adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.OpeningHours;
import com.binus.pekalongancityguide.R;

import java.util.ArrayList;
import java.util.List;

public class OpeningHoursAdapter extends RecyclerView.Adapter<OpeningHoursAdapter.ViewHolder> {
    private List<OpeningHours> mData;

    public OpeningHoursAdapter(List<OpeningHours> data) {
        mData = data;
    }

    public void setData(List<OpeningHours> data) {
        mData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_opening_hours, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OpeningHours openingHours = mData.get(position);
        List<String> weekday = openingHours.getWeekday();
        String openingHoursText = TextUtils.join(", ", weekday);
        holder.textView.setText(openingHoursText);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.opening_hours);
        }
    }
}



