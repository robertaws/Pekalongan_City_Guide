package com.binus.pekalongancityguide;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.binus.pekalongancityguide.Layout.DestinationDetails;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

public class ChooseLocation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_location);
        double currentLat = getIntent().getDoubleExtra("current_lat", 0);
        double currentLng = getIntent().getDoubleExtra("current_lng", 0);

        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.user_map);
        fragment.getMapAsync(googleMap -> {
            LatLng coordinate = new LatLng(currentLat, currentLng);
            MarkerOptions marker = new MarkerOptions();
            marker.position(coordinate);
            marker.title("Current Location");
            googleMap.addMarker(marker);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
            googleMap.moveCamera(cameraUpdate);
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(ChooseLocation.this, R.raw.map_style));
        });
    }

}