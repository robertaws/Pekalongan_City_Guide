package com.binus.pekalongancityguide.Layout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.binus.pekalongancityguide.Adapter.DestinationAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Destination;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.DialogChangeLocBinding;
import com.binus.pekalongancityguide.databinding.DialogSortDestiBinding;
import com.binus.pekalongancityguide.databinding.FragmentShowDestinationBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.binus.pekalongancityguide.BuildConfig.MAPS_API_KEY;

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
    private Geocoder geocoder;
    private AutocompleteSupportFragment autocompleteFragment;
    private SupportMapFragment fragment;
    private LatLng coordinate;
    private String addressString;
    private double currentLat, currentLng;
    private float distance;
    private boolean isChangeLocDialogShowing = false;
    private static SharedPreferences prefs;
    DestinationPager destinationPager;

    public ShowDestinationFragment() {
    }

    public static ShowDestinationFragment newInstance(String id, String category, String uid) {
        ShowDestinationFragment fragment = new ShowDestinationFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString("category", category);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        geocoder = new Geocoder(getContext(), Locale.getDefault());
        if(getArguments()!=null){
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
        }
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        prefs = getActivity().getSharedPreferences("coordinate", Context.MODE_PRIVATE);
        destinationPager = new DestinationPager();
        String lastLatitude = prefs.getString("lastLatitude", "0");
        String lastLongitude = prefs.getString("lastLongitude", "0");
        if (!lastLatitude.equals("0") && !lastLongitude.equals("0")) {
            double latitude = Double.parseDouble(lastLatitude);
            double longitude = Double.parseDouble(lastLongitude);
            coordinate = new LatLng(latitude, longitude);
        }
//        Log.d(TAG, "ON START COORDINATES: " + coordinate);
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
        binding.searchDesti.addTextChangedListener(new TextWatcher(){
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
        binding.sortButton.setOnClickListener(v ->{
            showSortDialog();
        });
        binding.locLayout.setOnClickListener(v -> {
            if (getContext() != null && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_LOCATION);
            } else {
                showChangeLocDialog();
            }
        });
        DestinationAdapter adapter = new DestinationAdapter(getContext(), destinationArrayList, getParentFragmentManager());
        adapter.setOnDataChangedListener(() -> {
            // Notify the parent fragment that the data has changed
            if (getParentFragment() instanceof DestinationPager) {
                ((DestinationPager) getParentFragment()).onDataChanged();
            }
        });

        return binding.getRoot();
    }

    private void showChangeLocDialog() {
        if (isChangeLocDialogShowing) return;
        isChangeLocDialogShowing = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        DialogChangeLocBinding locBinding = DialogChangeLocBinding.inflate(getLayoutInflater());
        builder.setView(locBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialog1 -> {
            requireActivity().runOnUiThread(() -> {
                getChildFragmentManager().beginTransaction().remove(fragment).commit();
                getChildFragmentManager().beginTransaction().remove(autocompleteFragment).commit();
            });
            isChangeLocDialogShowing = false;
        });
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();
        if (!Places.isInitialized()) {
            Places.initialize(getActivity().getApplicationContext(), MAPS_API_KEY);
        }
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    List<Address> addresses = geocoder.getFromLocation(currentLat, currentLng, 1);
                    if (addresses.size() > 0) {
                        return addresses.get(0).getAddressLine(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error: Geocoder service not available";
                }
                return null;
            }

            @Override
            protected void onPostExecute(String address) {
                // update the location text view in the UI thread
                if (address != null) {
                    addressString = address;
                    locBinding.locTv.setText(addressString);
                    autocompleteFragment.setText(addressString);
                } else {
                    locBinding.locTv.setText("Address not found");
                }
            }
        }.execute();
        fragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.user_map);
        fragment.getMapAsync(googleMap -> {
            coordinate = new LatLng(currentLat, currentLng);
            MarkerOptions marker = new MarkerOptions();
            marker.position(coordinate);
            marker.title("Current Location");
            googleMap.addMarker(marker);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
            googleMap.moveCamera(cameraUpdate);
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));
        });
        PlacesClient placesClient = Places.createClient(getContext());
        autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));
        autocompleteFragment.setCountries("ID");
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                coordinate = place.getLatLng();
                fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.user_map);
                fragment.getMapAsync(googleMap -> {
                    googleMap.clear();
                    MarkerOptions marker = new MarkerOptions();
                    marker.position(coordinate);
                    marker.title(place.getName());
                    googleMap.addMarker(marker);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
                    googleMap.moveCamera(cameraUpdate);
                });
                locBinding.locTv.setText(place.getAddress());
                addressString = place.getAddress();
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.e(TAG, "An error occurred: " + status);
            }
        });
        locBinding.useCurLoc.setOnClickListener(v -> {
            if (getContext() != null && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_LOCATION);
            } else {
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLat = location.getLatitude();
                        currentLng = location.getLongitude();
                        coordinate = new LatLng(currentLat, currentLng);
                        new AsyncTask<Void, Void, String>() {
                            @Override
                            protected String doInBackground(Void... voids) {
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(currentLat, currentLng, 1);
                                    if (addresses.size() > 0) {
                                        return addresses.get(0).getAddressLine(0);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return "Error: Geocoder service not available";
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(String address) {
                                if (address != null) {
//                                    Log.d("ADDRESS IN DIALOG", address);
                                    addressString = address;
                                    locBinding.locTv.setText(addressString);
                                    autocompleteFragment.setText(addressString);
                                    fragment.getMapAsync(googleMap -> {
                                        coordinate = new LatLng(currentLat, currentLng);
                                        MarkerOptions marker = new MarkerOptions();
                                        marker.position(coordinate);
                                        marker.title("Current Location");
                                        googleMap.addMarker(marker);
                                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
                                        googleMap.moveCamera(cameraUpdate);
                                        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));
                                    });
                                }
                            }
                        }.execute();
                    }
                });
            }
        });
        locBinding.setLocBtn.setOnClickListener(v -> {
            binding.changeLoc.setText(addressString);
            dialog.dismiss();
            updateDistances();
            if (coordinate != null) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("lastLatitude", String.valueOf(coordinate.latitude));
                editor.putString("lastLongitude", String.valueOf(coordinate.longitude));
                editor.apply();
            }
//            Log.d(TAG, "COORDINATES: " + coordinate);
        });
    }

    public void showSortDialog(){
        DialogSortDestiBinding binding1 = DialogSortDestiBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        CheckBox ratingCheck = binding1.ratingSort;
        CheckBox distanceCheck = binding1.distanceSort;
        builder.setView(binding1.getRoot());
        builder.setPositiveButton(R.string.sort_txt, (dialog, which) -> {
            int start = 0;
            if (ratingCheck.isChecked() && distanceCheck.isChecked()) {
                Collections.sort(destinationArrayList, (destination1, destination2) -> {
                    Double rating1 = Double.parseDouble(destination1.getRating());
                    Double rating2 = Double.parseDouble(destination2.getRating());
                    Float distance1 = destination1.getDistance();
                    Float distance2 = destination2.getDistance();
                    int distanceCompare = distance1.compareTo(distance2);
                    if (distanceCompare != 0) {
                        return distanceCompare;
                    }
                    return Double.compare(rating1, rating2);
                });
            } else if (ratingCheck.isChecked()) {
                Collections.sort(destinationArrayList, (destination1, destination2) -> {
                    Double rating1 = Double.parseDouble(destination1.getRating());
                    Double rating2 = Double.parseDouble(destination2.getRating());
                    return Double.compare(rating2, rating1);
                });
            } else if (distanceCheck.isChecked()) {
                Collections.sort(destinationArrayList, (destination1, destination2) -> {
                    Float distance1 = destination1.getDistance();
                    Float distance2 = destination2.getDistance();
                    return distance1.compareTo(distance2);
                });
            }
            String searchText = binding.searchDesti.getText().toString().trim();
            destinationAdapter.getFilter().filter(searchText);
            int itemCount = destinationArrayList.size() - start;
            destinationAdapter.notifyItemRangeChanged(start, itemCount);
        });

        builder.setNegativeButton(R.string.cancel_txt, (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();
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
            destinationAdapter = new DestinationAdapter(getContext(), destinationArrayList, getParentFragmentManager());
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
                double placeLng = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                if (coordinate != null) {
                    currentLat = coordinate.latitude;
                    currentLng = coordinate.longitude;
                    distance = calculateDistance(currentLat, currentLng, placeLat, placeLng);
                    destination.setDistance(distance);
                    destinationAdapter.notifyDataSetChanged();

                    new AsyncTask<Void, Void, String>() {
                        @Override
                        protected String doInBackground(Void... voids) {
                            try {
                                List<Address> addresses = geocoder.getFromLocation(currentLat, currentLng, 1);
                                if (addresses.size() > 0) {
                                    return addresses.get(0).getAddressLine(0);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                return "Error: Geocoder service not available";
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(String address) {
                            if (address != null) {
//                                Log.d("ADDRESS", address);
                                binding.changeLoc.setText(address);
                            }
                        }
                    }.execute();
                } else {
                    if (getContext() != null && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_LOCATION);
                    } else {
                        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                            if (location != null) {
                                currentLat = location.getLatitude();
                                currentLng = location.getLongitude();
                                distance = calculateDistance(currentLat, currentLng, placeLat, placeLng);
                                destination.setDistance(distance);
                                sortDestination(destinationArrayList);
                                destinationAdapter.notifyDataSetChanged();

                                new AsyncTask<Void, Void, String>() {
                                    @Override
                                    protected String doInBackground(Void... voids) {
                                        try {
                                            List<Address> addresses = geocoder.getFromLocation(currentLat, currentLng, 1);
                                            if (addresses.size() > 0) {
                                                return addresses.get(0).getAddressLine(0);
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            return "Error: Geocoder service not available";
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(String address) {
                                        if (address != null) {
//                                            Log.d("ADDRESS", address);
                                            binding.changeLoc.setText(address);
                                        }
                                    }
                                }.execute();
                            }
                        });
                    }
                }
//                Log.d(TAG, "distance: " + destination.getDistance());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    };
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

    private void sortDestination(ArrayList<Destination> destinationArrayList){
        Collections.sort(destinationArrayList, (destination1, destination2) -> {
            String title1 = destination1.getTitle().toLowerCase();
            String title2 = destination2.getTitle().toLowerCase();
            return title1.compareTo(title2);
        });
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
        }
    }

}