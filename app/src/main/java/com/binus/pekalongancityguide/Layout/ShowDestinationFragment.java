package com.binus.pekalongancityguide.Layout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.binus.pekalongancityguide.Adapter.DestinationAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Destination;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.FragmentShowDestinationBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShowDestinationFragment extends Fragment {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");
    private static final int PERMISSION_REQUEST_LOCATION = 500;
    private String categoryId;
    private String category;
    private ArrayList<Destination> destinationArrayList;
    private DestinationAdapter destinationAdapter;
    private static final String TAG = "DESTI_USER_TAG";
    private FragmentShowDestinationBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;
    private LocationListener locationListener;

    public ShowDestinationFragment() {

    }

    public static ShowDestinationFragment newInstance(String categoryId, String category, String uid) {
        ShowDestinationFragment fragment = new ShowDestinationFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
        }
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentShowDestinationBinding.inflate(LayoutInflater.from(getContext()), container, false);
        if (category.equals("All")) {
            loadDestinations();
        } else {
            loadCategoriedDestination();
        }
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Do something with the new location
                Log.d("Location", "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        } else {
            startLocationUpdates();
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        binding.searchDesti.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    destinationAdapter.getFilter().filter(s);
                }catch (Exception e){
                    Log.d(TAG,"onTextChanged :"+e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadDestinations() {
        destinationArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                destinationArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Destination destination = dataSnapshot.getValue(Destination.class);
                    destinationArrayList.add(destination);
                }
                updateDistances();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error: " + error.getMessage());
            }
        });
    }

    private void loadCategoriedDestination(){
        destinationArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
        reference.keepSynced(true);
        reference.orderByChild("categoryId").equalTo(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        destinationArrayList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Destination destination = dataSnapshot.getValue(Destination.class);
                            destinationArrayList.add(destination);
                        }
                        updateDistances();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void updateDistances() {
        for (Destination destination : destinationArrayList) {
            getDestinationDistance(destination);
        }
        if (destinationAdapter == null) {
            destinationAdapter = new DestinationAdapter(getContext(), destinationArrayList);
            binding.destiRv.setAdapter(destinationAdapter);
        } else {
            destinationAdapter.notifyDataSetChanged();
        }
    }

    private void getDestinationDistance(Destination destination) {
        database.getReference("Destination").child(destination.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double placeLat = Double.parseDouble(snapshot.child("latitude").getValue().toString());
                Log.d(TAG, "Latitude: " + placeLat);
                double placeLng = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                Log.d(TAG, "Longitude: " + placeLng);
                if (getContext() != null && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_LOCATION);
                } else {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                        if (location != null) {
                            double currentLat = location.getLatitude();
                            double currentLng = location.getLongitude();
                            // Calculate distance  current location and itinerary location
                            float distance = calculateDistance(currentLat, currentLng, placeLat, placeLng);
                            destination.setDistance(distance);
                            Log.d(TAG, "clng: " + currentLng + " clat: " + currentLat + " dist: " + distance);

                            // notify the adapter that the data has changed
                            destinationAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error: " + error.getMessage());
            }
        });
    }

    private float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location location1 = new Location("");
        location1.setLatitude(lat1);
        location1.setLongitude(lon1);
        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(lon2);
        Location.distanceBetween(location1.getLatitude(), location1.getLongitude(),
                location2.getLatitude(), location2.getLongitude(), results);
        return results[0] / 1000;
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        // Check if GPS is enabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS is not enabled, show a dialog to ask the user to enable it
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.gpsnotEnabled);
            builder.setMessage(R.string.enable_gps_confirm);
            builder.setPositiveButton(R.string.yes_txt, (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            });
            builder.setNegativeButton(R.string.no_txt, null);
            builder.show();
        } else {
            // GPS is enabled, start requesting location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
        }
    }
}