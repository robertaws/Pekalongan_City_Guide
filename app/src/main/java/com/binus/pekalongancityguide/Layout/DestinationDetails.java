package com.binus.pekalongancityguide.Layout;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        fragment.getMapAsync(googleMap ->{
            double latitude = getIntent().getDoubleExtra("lat", 0);
            double longitude = getIntent().getDoubleExtra("long", 0);
            LatLng coordinate = new LatLng(latitude, longitude);
            MarkerOptions marker = new MarkerOptions();
            marker = marker.position(coordinate);
            marker = marker.title(getIntent().getStringExtra("judul"));
            googleMap.addMarker(marker);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
            googleMap.moveCamera(cameraUpdate);
        });
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