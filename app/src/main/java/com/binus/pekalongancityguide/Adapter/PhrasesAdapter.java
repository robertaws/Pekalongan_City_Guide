package com.binus.pekalongancityguide.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Phrases;
import com.binus.pekalongancityguide.R;

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
        holder.aksaraTV.setText(item1.getAksara());
        holder.latinTV.setText(item1.getLatin());
        holder.englishTV.setText(item1.getEnglish());
        holder.indoTV.setText(item1.getIndo());
    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView aksaraTV, latinTV, englishTV, indoTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            aksaraTV = itemView.findViewById(R.id.phrase_aksara_value);
            latinTV = itemView.findViewById(R.id.phrase_latin_value);
            englishTV = itemView.findViewById(R.id.phrase_english_value);
            indoTV = itemView.findViewById(R.id.phrase_indo_value);
        }
    }

}
