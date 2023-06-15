package com.binus.pekalongancityguide.Layout;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.R;

public class CityHistory extends AppCompatActivity {
    ImageButton back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_history);
        back = findViewById(R.id.backHome2);
        back.setOnClickListener(v -> onBackPressed());
    }
}