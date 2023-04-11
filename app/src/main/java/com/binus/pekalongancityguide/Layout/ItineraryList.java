package com.binus.pekalongancityguide.Layout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.binus.pekalongancityguide.Adapter.ItineraryAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Itinerary;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityItineraryListBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.binus.pekalongancityguide.BuildConfig.MAPS_API_KEY;

public class ItineraryList extends AppCompatActivity {

    LocationRequest locationRequest = LocationRequest
            .create()
            .setInterval(10000) // Update every 10 seconds
            .setFastestInterval(1000) // Get updates as fast as possible
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    private static final String TAG = "ITER_TAG";
    private static final int PERMISSION_REQUEST_LOCATION = 500;
    private final List<Itinerary> itineraryList = new ArrayList<>();
    public ActivityItineraryListBinding binding;
    ItineraryAdapter adapter;
    String destiId;
    PlacesClient placesClient;
    private FirebaseAuth firebaseAuth;
    private FusedLocationProviderClient fusedLocationClient;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItineraryListBinding.inflate(getLayoutInflater());
        Intent intent = getIntent();
        destiId = intent.getStringExtra("destinationId");
        Log.d(TAG, "Destination id: " + destiId);
        Places.initialize(getApplicationContext(), MAPS_API_KEY);
        placesClient = Places.createClient(this);
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        adapter = new ItineraryAdapter(itineraryList);
        binding.backtoprofile.setOnClickListener(v -> {
            onBackPressed();
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "onLocationResult called");

                Location currentLocation = locationResult.getLastLocation();
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Do something with the new location
                }
            }
        };

        loadItinerary();
        binding.itineraryRv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        } else {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop receiving location updates
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void loadItinerary() {
        DatabaseReference userRef = database.getReference("Users").child(firebaseAuth.getUid());
        Query itineraryQuery = userRef.child("itinerary");
        itineraryQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Itinerary> itineraryList = new ArrayList<>();
                for (DataSnapshot itinerarySnapshot : dataSnapshot.getChildren()) {
                    String date = itinerarySnapshot.child("date").getValue(String.class);
                    Log.d(TAG, "Date: " + date);
                    String startTime = itinerarySnapshot.child("startTime").getValue(String.class);
                    Log.d(TAG, "Start Time: " + startTime);
                    String endTime = itinerarySnapshot.child("endTime").getValue(String.class);
                    Log.d(TAG, "End Time: " + endTime);
                    String placeId = itinerarySnapshot.child("placeId").getValue(String.class);
                    Log.d(TAG, "Place ID: " + placeId);

                    List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);
                    if (placeId != null && !placeId.isEmpty()) {
                        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();
                        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                            Place place = response.getPlace();
                            String placeName = place.getName();
                            itineraryList.add(new Itinerary(date, endTime, placeName, startTime));
                            sortItineraryList(itineraryList);
                            // Set the sorted itineraryList to the adapter
                            ItineraryAdapter adapter = new ItineraryAdapter(itineraryList);
                            binding.itineraryRv.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }).addOnFailureListener((e) -> {
                            Log.e(TAG, "Error fetching place details: " + e.getMessage());
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching itinerary data: " + databaseError.getMessage());
            }
        });
    }

    private void sortItineraryList(List<Itinerary> itineraryList) {
        Collections.sort(itineraryList, (itinerary1, itinerary2) -> {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy hh:mm a", Locale.ENGLISH);
            try {
                Date date1 = sdf.parse(itinerary1.getDate() + " " + itinerary1.getStartTime());
                Date date2 = sdf.parse(itinerary2.getDate() + " " + itinerary2.getStartTime());
                int dateComparison = date1.compareTo(date2);
                if (dateComparison != 0) {
                    return dateComparison;
                } else {
                    return itinerary1.getStartTime().compareTo(itinerary2.getStartTime());
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date: " + e.getMessage());
                return 0;
            }
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location currentLocation = locationResult.getLastLocation();

            DatabaseReference userRef = database.getReference("Destination").child(destiId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Double destLatitude = dataSnapshot.child("latitude").getValue(Double.class);
                    Log.d(TAG, "Destination Latitude: " + destLatitude);
                    Double destLongitude = dataSnapshot.child("longitude").getValue(Double.class);
                    Log.d(TAG, "Destination Longitude: " + destLongitude);

                    // Get the distance and duration from current location to destination location
                    LatLng destination = new LatLng(destLatitude, destLongitude);
                    float[] results = new float[1];
                    Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                            destination.latitude, destination.longitude, results);
                    float distanceInMeters = results[0];
                    float distanceInKm = distanceInMeters / 1000;
                    float timeInMinutes = distanceInMeters / currentLocation.getSpeed() / 60;

                    // Update the TextView with the distance and duration values
                    TextView distanceTextView = findViewById(R.id.distanceTextView);
                    distanceTextView.setText(String.format("%.2f km", distanceInKm));

                    TextView durationTextView = findViewById(R.id.durationTextView);
                    durationTextView.setText(String.format("%.1f minutes", timeInMinutes));
                    Log.d(TAG, "Current Latitude: " + currentLocation.getLatitude());
                    Log.d(TAG, "Current Longitude: " + currentLocation.getLongitude());
                    Log.d(TAG, "Destination Latitude: " + destination.latitude);
                    Log.d(TAG, "Destination Longitude: " + destination.longitude);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle cancelled event
                }
            });
        }
    };

}
