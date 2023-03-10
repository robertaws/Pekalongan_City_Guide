package com.binus.pekalongancityguide;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Destination item1 = item.get(position);
        holder.layoutBg.setBackgroundResource(item1.getDestiImage());
        holder.destinameTV.setText(item1.getDestiName());
        holder.destinameTV2.setText(item1.getDestiName2());
        holder.destiDescTV.setText(item1.getDestiDesc());
        holder.destiAddressTV.setText(item1.getDestiAddress());
        holder.layoutBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),DestinationDetails.class);
                intent.putExtra("gambar",item.get(position).getDestiImage());
                intent.putExtra("nama",item.get(position).getDestiName());
                intent.putExtra("detil",item.get(position).getDestiDesc());
                intent.putExtra("alamat",item.get(position).getDestiAddress());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView destinameTV, destinameTV2, destiDescTV, destiAddressTV;
        RelativeLayout layoutBg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            destinameTV = itemView.findViewById(R.id.loc_name);
            destinameTV2 = itemView.findViewById(R.id.loc_name2);
            layoutBg = itemView.findViewById(R.id.layoutImage);
            destiDescTV = itemView.findViewById(R.id.loc_desc);
            destiAddressTV = itemView.findViewById(R.id.loc_address);
        }
    }
}
