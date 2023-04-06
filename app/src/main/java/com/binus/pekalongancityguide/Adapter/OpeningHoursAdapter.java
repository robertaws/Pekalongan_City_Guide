package com.binus.pekalongancityguide.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.OpeningHours;
import com.binus.pekalongancityguide.R;

import java.util.List;

public class OpeningHoursAdapter extends RecyclerView.Adapter<OpeningHoursAdapter.OpeningHoursViewHolder> {
    private final List<OpeningHours> hours;

    public OpeningHoursAdapter(List<OpeningHours> data) {
        this.hours = data;
    }

    @NonNull
    @Override
    public OpeningHoursViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_opening_hours, parent, false);
        return new OpeningHoursViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OpeningHoursViewHolder holder, int position) {
        OpeningHours openingHours = hours.get(position);
        String openingHoursText = openingHours.getOpeningHours();
        holder.textView.setText(openingHoursText);
    }

    @Override
    public int getItemCount() {
        return hours.size();
    }

    public static class OpeningHoursViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public OpeningHoursViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.opening_hours);
        }
    }
}
