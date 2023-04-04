package com.binus.pekalongancityguide.Adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.Layout.NewsDetail;
import com.binus.pekalongancityguide.databinding.ListNewsBinding;
import com.bumptech.glide.Glide;
import com.kwabenaberko.newsapilib.models.Article;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private List<Article> mArticles;
    private static ListNewsBinding binding;

    public NewsAdapter(List<Article> articles) {
        this.mArticles = articles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ListNewsBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = mArticles.get(position);
        holder.titleTV.setText(article.getTitle());
        holder.sourceTV.setText(article.getSource().getName());
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -28);
        String publishedDateString = article.getPublishedAt();
        try {
            Date publishedDate = inputDateFormat.parse(publishedDateString);
            String formattedDate = outputDateFormat.format(publishedDate);
            holder.dateTV.setText(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (article.getUrlToImage() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(article.getUrlToImage())
                    .into(holder.newsIV);
        }
        Log.d("NewsAPI", "Title: " + article.getTitle());
        Log.d("NewsAPI", "Description: " + article.getDescription());
        Log.d("NewsAPI", "Image URL: " + article.getUrlToImage());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), NewsDetail.class);
            intent.putExtra("title", article.getTitle());
            intent.putExtra("desc", article.getDescription());
            intent.putExtra("imageUrl", article.getUrlToImage());
            intent.putExtra("source", article.getSource().getName());
            intent.putExtra("newsUrl", article.getUrl());
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID"));
            Date publishedDate = null;
            try {
                publishedDate = input.parse(article.getPublishedAt());
            }catch (ParseException e) {
                e.printStackTrace();
            }
            String formattedDate = output.format(publishedDate);
            intent.putExtra("date", formattedDate);
            intent.putExtra("author",article.getAuthor());
            v.getContext().startActivity(intent);
        });
    }
    @Override
    public int getItemCount() {
        return mArticles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTV,dateTV,sourceTV;
        private ImageView newsIV;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTV = binding.newsTitle;
            dateTV = binding.newsDate;
            sourceTV = binding.newsSource;
            newsIV = binding.newsIv;
        }
    }
}
