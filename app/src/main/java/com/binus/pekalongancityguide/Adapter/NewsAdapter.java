package com.binus.pekalongancityguide.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.R;
import com.bumptech.glide.Glide;
import com.kwabenaberko.newsapilib.models.Article;

import java.util.List;


public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private List<Article> mArticles;

    public NewsAdapter(List<Article> articles) {
        this.mArticles = articles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_news, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = mArticles.get(position);

        // Set article title
        holder.titleTextView.setText(article.getTitle());

        // Set article description
        holder.descriptionTextView.setText(article.getDescription());

        // Set article author
        holder.authorTextView.setText(article.getAuthor());

        // Set article source name
        holder.sourceTextView.setText(article.getSource().getName());

        // Set article published date
        holder.dateTextView.setText(article.getPublishedAt());

        // Set article image
        if (article.getUrlToImage() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(article.getUrlToImage())
                    .into(holder.imageView);
        }

        Log.d("NewsAPI", "Title: " + article.getTitle());
        Log.d("NewsAPI", "Description: " + article.getDescription());
        Log.d("NewsAPI", "Image URL: " + article.getUrlToImage());
    }

    @Override
    public int getItemCount() {
        return mArticles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView descriptionTextView;
        private TextView authorTextView;
        private TextView sourceTextView;
        private TextView dateTextView;
        private ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.news_title);
            descriptionTextView = itemView.findViewById(R.id.news_body);
            authorTextView = itemView.findViewById(R.id.news_author);
            sourceTextView = itemView.findViewById(R.id.news_source);
            dateTextView = itemView.findViewById(R.id.news_date);
            imageView = itemView.findViewById(R.id.news_iv);
        }
    }
}
