package com.binus.pekalongancityguide.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Itinerary;
import com.binus.pekalongancityguide.R;

import java.util.List;

public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.ViewHolder> {
    private List<Itinerary> itineraryList;

    public ItineraryAdapter(List<Itinerary> itineraryList) {
        this.itineraryList = itineraryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_itinerary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Itinerary itinerary = itineraryList.get(position);
        holder.dateTextView.setText(itinerary.getDate());
        holder.starttimeTextView.setText(itinerary.getStartTime());
        holder.endtimeTextView.setText(itinerary.getEndTime());
        holder.placeTextView.setText(itinerary.getPlaceName());
    }

    @Override
    public int getItemCount() {
        return itineraryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView,starttimeTextView,endtimeTextView,placeTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            starttimeTextView = itemView.findViewById(R.id.startTimeTextView);
            endtimeTextView = itemView.findViewById(R.id.startTimeTextView);
            placeTextView = itemView.findViewById(R.id.placeNameTextView);
        }
    }
}


