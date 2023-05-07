package com.binus.pekalongancityguide.Layout;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static com.binus.pekalongancityguide.BuildConfig.MAPS_API_KEY;

public class ChooseLocation extends AppCompatActivity {
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final String TAG = "ChooseLocation";
    private AutocompleteSupportFragment autocompleteFragment;
    private PlacesClient placesClient;
    private ImageButton backButton;
    private TextView locEt,curLoc;
    private LatLng coordinate;
    private String addressString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ShowDestinationFragment showDestinationFragment = new ShowDestinationFragment();

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

        backButton.setOnClickListener(v ->{
            onBackPressed();

        });
        placesClient = Places.createClient(this);
        autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,Place.Field.LAT_LNG));
        curLoc.setOnClickListener(v -> {
            locEt.setText(addressString);
            autocompleteFragment.setText(addressString);
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
            public void onPlaceSelected(@NonNull Place place){
                LatLng latLng = place.getLatLng();
                Bundle bundle = new Bundle();
                bundle.putString("address", place.getAddress());
                showDestinationFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container_showDesti, showDestinationFragment)
                        .commit();
                SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.user_map);
                fragment.getMapAsync(googleMap -> {
                    googleMap.clear();
                    MarkerOptions marker = new MarkerOptions();
                    marker.position(latLng);
                    marker.title(place.getName());
                    googleMap.addMarker(marker);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                    googleMap.moveCamera(cameraUpdate);
                });
                locEt.setText(place.getAddress());
            }
            @Override
            public void onError(@NonNull Status status) {
                Log.e(TAG, "An error occurred: " + status);
            }
        });


    }
    private void init() {
        backButton = findViewById(R.id.backtoHome);
        locEt = findViewById(R.id.autocompleteTv);
        curLoc = findViewById(R.id.use_cur_loc);
    }
}
