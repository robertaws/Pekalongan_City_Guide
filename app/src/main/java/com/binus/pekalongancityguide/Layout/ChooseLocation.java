package com.binus.pekalongancityguide.Layout;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.binus.pekalongancityguide.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.binus.pekalongancityguide.BuildConfig.MAPS_API_KEY;

public class ChooseLocation extends AppCompatActivity {
    private static final String TAG = "ChooseLocation";
    private AutocompleteSupportFragment autocompleteFragment;
    private ImageButton backButton;
    private Button saveButton;
    private TextView locEt, curLoc;
    private String addressString;
    private LatLng coordinate;
    private String selectedAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), MAPS_API_KEY);
        }
        setContentView(R.layout.activity_choose_location);
        init();
        double currentLat = getIntent().getDoubleExtra("current_lat", 0);
        double currentLng = getIntent().getDoubleExtra("current_lng", 0);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(currentLat, currentLng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                sb.append(address.getAddressLine(i)).append(", ");
            }
            addressString = sb.toString();
            locEt.setText(addressString);
        } else {
            locEt.setText("Address not found");
        }
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.user_map);
        fragment.getMapAsync(googleMap -> {
            coordinate = new LatLng(currentLat, currentLng);
            MarkerOptions marker = new MarkerOptions();
            marker.position(coordinate);
            marker.title("Current Location");
            googleMap.addMarker(marker);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
            googleMap.moveCamera(cameraUpdate);
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(ChooseLocation.this, R.raw.map_style));
        });

        backButton.setOnClickListener(v -> {
            onBackPressed();
        });
        Places.createClient(this);
        autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,Place.Field.LAT_LNG));
        curLoc.setOnClickListener(v -> {
            locEt.setText(addressString);
            autocompleteFragment.setText(addressString);
            selectedAddress = addressString;
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
        });
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener(){
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                coordinate = place.getLatLng();
                SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.user_map);
                fragment.getMapAsync(googleMap -> {
                    googleMap.clear();
                    MarkerOptions marker = new MarkerOptions();
                    marker.position(coordinate);
                    marker.title(place.getName());
                    googleMap.addMarker(marker);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
                    googleMap.moveCamera(cameraUpdate);
                });
                locEt.setText(place.getAddress());
                selectedAddress = place.getAddress();
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.e(TAG, "An error occurred: " + status);
            }
        });
        saveButton.setOnClickListener(v -> {
            ShowDestinationFragment showDestinationFragment = new ShowDestinationFragment();
            Bundle bundle = new Bundle();
            bundle.putString("address", selectedAddress);
            bundle.putParcelable("coordinates", coordinate);
            showDestinationFragment.setArguments(bundle);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.desti_container, showDestinationFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }
    private void init() {
        backButton = findViewById(R.id.backtoHome);
        locEt = findViewById(R.id.autocompleteTv);
        curLoc = findViewById(R.id.use_cur_loc);
        saveButton = findViewById(R.id.saveLocBtn);
    }
}
