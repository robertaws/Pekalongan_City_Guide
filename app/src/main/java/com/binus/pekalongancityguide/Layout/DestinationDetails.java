package com.binus.pekalongancityguide.Layout;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.R;

public class DestinationDetails extends AppCompatActivity {
    ImageView dImage;
    TextView dName,dDesc,dAddress;
    ImageButton backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_details);
        init();
        backButton.setOnClickListener(v -> onBackPressed());
        getData();
    }
    void init(){
        dImage = findViewById(R.id.ddimg);
        dName = findViewById(R.id.ddname);
        dDesc = findViewById(R.id.dddesc);
        dAddress = findViewById(R.id.ddadd);
        backButton = findViewById(R.id.backDestination);
    }
    void getData(){
        dImage.setBackgroundResource(this.getIntent().getIntExtra("gambar", R.drawable.desti1));
        dName.setText(this.getIntent().getStringExtra("nama"));
        dDesc.setText("\t\t\t\t\t" + this.getIntent().getStringExtra("detil"));
        dAddress.setText(this.getIntent().getStringExtra("alamat"));
    }
}