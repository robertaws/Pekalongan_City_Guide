package com.binus.pekalongancityguide.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Itinerary;
import com.binus.pekalongancityguide.R;

import java.util.List;

public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.ItineraryViewHolder> {
    private final List<Itinerary> itineraryList;

    public ItineraryAdapter(List<Itinerary> itineraryList) {
        this.itineraryList = itineraryList;
    }

    @Override
    public ItineraryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_itinerary, parent, false);
        return new ItineraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItineraryViewHolder holder, int position) {
        Itinerary itinerary = itineraryList.get(position);
        holder.dateTextView.setText(itinerary.getDate());
        holder.startTimeTextView.setText(itinerary.getStartTime());
        holder.endTimeTextView.setText(itinerary.getEndTime());
        holder.placeNameTextView.setText(itinerary.getPlaceId());
    }

    @Override
    public int getItemCount() {
        return itineraryList.size();
    }

    public static class ItineraryViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView, startTimeTextView, endTimeTextView, placeNameTextView;

        public ItineraryViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            endTimeTextView = itemView.findViewById(R.id.endTimeTextView);
            placeNameTextView = itemView.findViewById(R.id.placeNameTextView);
        }
    }
}
