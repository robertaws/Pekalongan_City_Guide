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
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.binus.pekalongancityguide.Adapter.ItineraryAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Itinerary;
import com.binus.pekalongancityguide.databinding.FragmentItineraryBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.binus.pekalongancityguide.BuildConfig.MAPS_API_KEY;

public class ItineraryFragment extends Fragment {
    private FragmentItineraryBinding binding;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final String TAG = "ITER_TAG";
    private static final String apiKey = MAPS_API_KEY;
    private static final int PERMISSION_REQUEST_LOCATION = 500;
    private final List<Itinerary> itineraryList = new ArrayList<>();
    ItineraryAdapter adapter;
    PlacesClient placesClient;
    private String selectedDate;
    private FirebaseAuth firebaseAuth;
    private FusedLocationProviderClient fusedLocationClient;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");
    public ItineraryFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentItineraryBinding.inflate(LayoutInflater.from(getContext()), container, false);
        Places.initialize(getContext().getApplicationContext(), apiKey);
        placesClient = Places.createClient(getContext());
        firebaseAuth = FirebaseAuth.getInstance();
        adapter = new ItineraryAdapter(getContext(), itineraryList);
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
        Bundle args = getArguments();
        if (args != null) {
            selectedDate = args.getString("selectedDate", selectedDate);
            Log.d(TAG, "selected date: " + selectedDate);
        }
        loadItinerary(selectedDate);
        binding.itineraryRv.setAdapter(adapter);
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

    private void loadItinerary(String date) {
        DatabaseReference userRef = database.getReference("Users").child(firebaseAuth.getUid());
        Query itineraryQuery = userRef.child("itinerary").orderByChild("date").equalTo(date);
        Log.d(TAG, "itineraryQuery: " + itineraryQuery);
        itineraryQuery.addValueEventListener(new ValueEventListener() {
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
                    String destiId = itinerarySnapshot.child("destiId").getValue(String.class);
                    Log.d(TAG, "Desti ID: " + destiId);

                    database.getReference("Destination").child(destiId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            double placeLat = Double.parseDouble(snapshot.child("latitude").getValue().toString());
                            Log.d(TAG, "Latitude: " + placeLat);
                            double placeLng = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                            Log.d(TAG, "Longitude: " + placeLng);
                            String placeName = snapshot.child("title").getValue(String.class);
                            Log.d(TAG, "Place Name: " + placeName);
                            String url = "" + snapshot.child("url").getValue();

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
                                        Log.d(TAG, "Distance: " + distance);
                                        calculateDuration(currentLat, currentLng, placeLat, placeLng, durationText -> {
                                            itineraryList.add(new Itinerary(date, startTime, endTime, placeName, destiId, url, durationText, placeLat, placeLng, distance));
                                            sortItineraryList(itineraryList);
                                            binding.showRoutes.setOnClickListener(v -> {
                                                if (itineraryList.size() > 0) {
                                                    String origin = "current location";
                                                    Itinerary firstItinerary = itineraryList.get(0);
                                                    double latitude = firstItinerary.getLatitude();
                                                    double longitude = firstItinerary.getLongitude();
                                                    StringBuilder waypoints = new StringBuilder();
                                                    for (int i = 1; i < itineraryList.size(); i++) {
                                                        Itinerary itinerary = itineraryList.get(i);
                                                        waypoints.append(itinerary.getLatitude()).append(",").append(itinerary.getLongitude()).append("|");
                                                    }
                                                    waypoints.setLength(waypoints.length() - 1); // Remove the last "|"
                                                    String routeUrl = "https://www.google.com/maps/dir/?api=1&origin=" + origin + "&destination=" + latitude + "," + longitude + "&waypoints=" + waypoints + "&travelmode=driving";
                                                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(routeUrl));
                                                    mapIntent.setPackage("com.google.android.apps.maps");
                                                    if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                                        startActivity(mapIntent);
                                                    } else {
                                                        Toast.makeText(getContext(), "Google Maps is not installed on your device. Opening Google Maps website...", Toast.LENGTH_SHORT).show();
                                                        String websiteUrl = "https://www.google.com/maps/dir/?api=1&origin=" + origin + "&destination=" + latitude + "," + longitude + "&waypoints=" + waypoints + "&travelmode=driving";
                                                        Intent websiteIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));
                                                        startActivity(websiteIntent);
                                                    }
                                                } else {
                                                    Toast.makeText(getContext(), "There is no destination in the itinerary", Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                            ItineraryAdapter adapter = new ItineraryAdapter(getContext(), itineraryList);
                                            binding.itineraryRv.setAdapter(adapter);
                                        });
                                    }
                                }).addOnFailureListener(e -> {
                                    Log.e(TAG, "Error getting last known location: " + e.getMessage());
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
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
    private void calculateDuration(double lat1, double lon1, double lat2, double lon2, DurationCallback callback) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + lat1 + "," + lon1 + "&destination=" + lat2 + "," + lon2 + "&key=" + apiKey;

        if (isAdded() && getContext() != null) {
            RequestQueue queue = Volley.newRequestQueue(getContext().getApplicationContext());
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
                try {
                    JSONArray routes = response.getJSONArray("routes");
                    if (routes.length() > 0) {
                        JSONObject route = routes.getJSONObject(0);
                        JSONArray legs = route.getJSONArray("legs");
                        JSONObject leg = legs.getJSONObject(0);
                        JSONObject duration = leg.getJSONObject("duration");
                        String durationText = duration.getString("text");
                        Log.d(TAG, "Duration: " + durationText);
                        callback.onDurationReceived(durationText);
                    } else {
                        Log.e(TAG, "No routes found");
                        callback.onDurationReceived("No routes found");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
                Log.e(TAG, "Error calculating travel duration: " + error.getMessage());
                callback.onDurationReceived("Error calculating travel duration");
            });
            queue.add(request);
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        // Check if GPS is enabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS is not enabled, show a dialog to ask the user to enable it
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("GPS not enabled");
            builder.setMessage("Would you like to enable GPS?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            });
            builder.setNegativeButton("No", null);
            builder.show();
        } else {
            // GPS is enabled, start requesting location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
        }
    }
    public interface DurationCallback {
        void onDurationReceived(String durationText);
    }
}