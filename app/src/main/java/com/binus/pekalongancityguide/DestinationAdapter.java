package com.binus.pekalongancityguide;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.ViewHolder> {
    private List<Destination> item;

    public DestinationAdapter(List<Destination> items) {
        item = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_destination, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Destination item1 = item.get(position);
        holder.layoutBg.setBackgroundResource(item1.getDestiImage());
        holder.destinameTV.setText(item1.getDestiName());
    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView destinameTV;
        RelativeLayout layoutBg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            destinameTV = itemView.findViewById(R.id.loc_name);
            layoutBg = itemView.findViewById(R.id.layoutImage);
        }
    }
}
