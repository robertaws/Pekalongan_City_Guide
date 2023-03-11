package com.binus.pekalongancityguide.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Article;
import com.binus.pekalongancityguide.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private List<Article> articles;

    public NewsAdapter(List<Article> articles) {
        this.articles = articles;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.title.setText(article.getTitle());
        holder.description.setText(article.getDescription());

        Glide.with(holder.itemView)
                .load(article.getImageUrl())
                .into(holder.image);
    }
    @Override
    public int getItemCount() {
        return articles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView description;
        public ImageView image;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.news_title);
            description = view.findViewById(R.id.news_description);
            image = view.findViewById(R.id.news_image);
        }
    }
}
