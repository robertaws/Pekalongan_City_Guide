package com.binus.pekalongancityguide.Layout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityNewsDetailsBinding;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NewsDetail extends AppCompatActivity {
    private ActivityNewsDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewsDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.backtoHome.setOnClickListener(v -> onBackPressed());
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String author = intent.getStringExtra("author");
        String desc = intent.getStringExtra("desc");
        String imageUrl = intent.getStringExtra("imageUrl");
        String source = intent.getStringExtra("source");
        String url = intent.getStringExtra("newsUrl");
        String date = intent.getStringExtra("date");

        binding.newsDetailTitle.setText(title);
        binding.newsDetailContent.setText(desc);
        binding.newsDetailAuthor.setText(author);
        binding.newsDetailDate.setText(date);
        binding.newsDetailSource.setText("Source : "+source);
        Glide.with(this)
                .load(imageUrl)
                .into(binding.newsDetailIv);
        binding.newsDetailLink.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
    }


}