package com.binus.pekalongancityguide.Misc;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

public class ImageFullscreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PhotoView photoView = new PhotoView(this);
        setContentView(photoView);
        String imageUri = getIntent().getStringExtra("fullImg");
        byte[] byteArray = getIntent().getByteArrayExtra("fullImg");
        if (byteArray != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            photoView.setImageBitmap(bitmap);
        }

        Glide.with(this)
                .load(imageUri)
                .into(photoView);
        photoView.setMaximumScale(5.0f);
    }
}