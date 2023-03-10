package com.binus.pekalongancityguide;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class DestinationDetails extends AppCompatActivity {
    ImageView dImage;
    TextView dName,dDesc,dAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_details);
        init();
        getData();
    }
    void init(){
        dImage = findViewById(R.id.ddimg);
        dName = findViewById(R.id.ddname);
        dDesc = findViewById(R.id.dddesc);
        dAddress = findViewById(R.id.ddadd);
    }
    void getData(){
        dImage.setImageResource(this.getIntent().getIntExtra("gambar",R.drawable.desti1));
        dName.setText(this.getIntent().getStringExtra("nama"));
        dDesc.setText(this.getIntent().getStringExtra("detil"));
        dAddress.setText(this.getIntent().getStringExtra("alamat"));
    }
}