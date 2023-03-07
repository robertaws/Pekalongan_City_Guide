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
        holder.destinameTV2.setText(item1.getDestiName2());
    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView destinameTV, destinameTV2;
        RelativeLayout layoutBg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            destinameTV = itemView.findViewById(R.id.loc_name);
            destinameTV2 = itemView.findViewById(R.id.loc_name2);
            layoutBg = itemView.findViewById(R.id.layoutImage);
        }
    }
}
