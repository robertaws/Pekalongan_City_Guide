package com.binus.pekalongancityguide.Misc;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.R;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
public class ImageFullscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_fullscreen);

        PhotoView photoView = findViewById(R.id.photo_view);
        String imageUri = getIntent().getStringExtra("fullImg");
        Glide.with(this).load(imageUri).into(photoView);
        photoView.setMaximumScale(5.0f);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

