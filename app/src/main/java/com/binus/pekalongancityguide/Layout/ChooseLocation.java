package com.binus.pekalongancityguide.Layout;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static com.binus.pekalongancityguide.BuildConfig.MAPS_API_KEY;

public class ChooseLocation extends AppCompatActivity {
    ImageButton backButton;
    EditText locField;
    GoogleMap googleMap;
    private static final LatLngBounds BOUNDS_PEKALONGAN = new LatLngBounds(
            new LatLng(-6.9995, 109.6345), new LatLng(-6.8695, 109.7975));

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

        // Initialize the AutocompleteSupportFragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // Handle the selected place
                LatLng latLng = place.getLatLng();
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(place.getName());
                Marker marker = googleMap.addMarker(markerOptions);
                marker.showInfoWindow();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                googleMap.moveCamera(cameraUpdate);
                locField.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(locField.getWindowToken(), 0);
            }

            @Override
            public void onError(@NonNull Status status) {
                // Handle the error
                Log.e(TAG, "Autocomplete error: " + status.getStatusMessage());
            }
        });

        backButton.setOnClickListener(v -> {
            onBackPressed();
        });

        locField = findViewById(R.id.loc_field);
        locField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String address = locField.getText().toString().trim();
                if (!address.isEmpty()) {
                    Geocoder geocoder = new Geocoder(ChooseLocation.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocationName(address, 1, BOUNDS_PEKALONGAN.southwest.latitude,
                                BOUNDS_PEKALONGAN.southwest.longitude, BOUNDS_PEKALONGAN.northeast.latitude, BOUNDS_PEKALONGAN.northeast.longitude);
                        if (addresses.size() > 0) {
                            Address foundAddress = addresses.get(0);
                            LatLng latLng = new LatLng(foundAddress.getLatitude(), foundAddress.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(foundAddress.getAddressLine(0));
                            Marker marker = googleMap.addMarker(markerOptions);
                            marker.showInfoWindow();
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                            googleMap.moveCamera(cameraUpdate);
                            locField.clearFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(locField.getWindowToken(), 0);
                            return true;
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Geocoding failed: " + e.getMessage());
                    }
                }
                Toast.makeText(ChooseLocation.this, "Location not found", Toast.LENGTH_SHORT).show();
            }
            return false;
        });
    }

    public void init() {
        backButton = findViewById(R.id.backtoHome);
        locField = findViewById(R.id.loc_field);
    }
}