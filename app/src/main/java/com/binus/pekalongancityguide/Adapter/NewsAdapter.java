package com.binus.pekalongancityguide.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.lifecycle.ViewModelProviderGetKt;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.R;
import com.bumptech.glide.Glide;
import com.kwabenaberko.newsapilib.models.Article;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


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

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = mArticles.get(position);
        holder.titleTv.setText(article.getTitle());
        holder.descTV.setText(article.getDescription());
        holder.authorTV.setText(article.getAuthor());
        holder.sourceTV.setText(article.getSource().getName());
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        String formattedDate = "";
        try {
            Date date = inputDateFormat.parse(article.getPublishedAt());
            formattedDate = outputDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.dateTV.setText(formattedDate);
        if (article.getUrlToImage() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(article.getUrlToImage())
                    .into(holder.newsImage);
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
        private TextView titleTv, descTV, authorTV, sourceTV, dateTV;
        private ImageView newsImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.news_title);
            descTV = itemView.findViewById(R.id.news_body);
            authorTV = itemView.findViewById(R.id.news_author);
            sourceTV = itemView.findViewById(R.id.news_source);
            dateTV = itemView.findViewById(R.id.news_date);
            newsImage = itemView.findViewById(R.id.news_iv);
        }
    }
}
