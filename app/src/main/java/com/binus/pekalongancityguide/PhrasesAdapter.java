package com.binus.pekalongancityguide;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PhrasesAdapter extends RecyclerView.Adapter<PhrasesAdapter.ViewHolder> {
    private List<Phrases> item;

    public PhrasesAdapter(List<Phrases> items) {
        item = items;
    }

    @NonNull
    @Override
    public PhrasesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_phrases, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhrasesAdapter.ViewHolder holder, int position) {
        Phrases item1 = item.get(position);
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
        TextView aksaraTV, latinTV, englishTV, indoTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            aksaraTV = itemView.findViewById(R.id.phrase_aksara);
            latinTV = itemView.findViewById(R.id.phrase_latin);
            englishTV = itemView.findViewById(R.id.phrase_english);
            indoTV = itemView.findViewById(R.id.phrase_indo);
        }
    }

}
