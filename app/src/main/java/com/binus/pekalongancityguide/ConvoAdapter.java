package com.binus.pekalongancityguide;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ConvoAdapter extends RecyclerView.Adapter<ConvoAdapter.ViewHolder> {
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
        holder.nameTV.setText(item1.getName());
        holder.aksaraTV.setText("Aksara : " + item1.getAksara());
        holder.latinTV.setText("Jawa : " + item1.getLatin());
        holder.englishTV.setText("English : " + item1.getEnglish());
        holder.indoTV.setText("Indonesian : " + item1.getIndo());
    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTV, aksaraTV, latinTV, englishTV, indoTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.convo_name);
            aksaraTV = itemView.findViewById(R.id.convo_aksara);
            latinTV = itemView.findViewById(R.id.convo_latin);
            englishTV = itemView.findViewById(R.id.convo_english);
            indoTV = itemView.findViewById(R.id.convo_indo);
        }
    }
}
