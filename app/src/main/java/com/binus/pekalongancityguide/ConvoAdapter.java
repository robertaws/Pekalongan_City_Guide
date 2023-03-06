package com.binus.pekalongancityguide;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/*public class ConvoAdapter extends RecyclerView.Adapter<ConvoAdapter.ViewHolder> {
    private List<Conversations> item;

    public ConvoAdapter(List<Conversations> items) {
        item = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_convo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversations item1 = item.get(position);
        holder.titleTextView.setText("Title : "+item1.getTitle());
        holder.bodyTextView.setText("Body : "+item1.getBody());
    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView bodyTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.item_title);
            bodyTextView = itemView.findViewById(R.id.item_body);
        }
    }
}*/
