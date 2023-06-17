package com.binus.pekalongancityguide.Layout;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.FragmentEmptyItinerary2Binding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class EmptyItinerary extends Fragment {
    private FragmentEmptyItinerary2Binding binding;
    private static final int PERMISSION_REQUEST_LOCATION = 500;
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLat, currentLng;
    private LatLng coordinate;
    private static SharedPreferences prefs;

    public EmptyItinerary() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEmptyItinerary2Binding.inflate(inflater, container, false);
        prefs = getActivity().getSharedPreferences("coordinate", Context.MODE_PRIVATE);
        String lastLatitude = prefs.getString("lastLatitude", "0");
        String lastLongitude = prefs.getString("lastLongitude", "0");
        if (!lastLatitude.equals("0") && !lastLongitude.equals("0")) {
            double latitude = Double.parseDouble(lastLatitude);
            double longitude = Double.parseDouble(lastLongitude);
            coordinate = new LatLng(latitude, longitude);
        }
        initializeAddress();
        binding.addIterBtn.setOnClickListener(v -> {
            ItineraryPager itineraryPager = new ItineraryPager();
            Bundle bundle = new Bundle();
            bundle.putDouble("currentLatitude", currentLat);
            bundle.putDouble("currentLongitude", currentLng);
            itineraryPager.setArguments(bundle);
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, itineraryPager);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
        return binding.getRoot();
    }

    private void initializeAddress() {
        Context context = getContext();
        if (coordinate != null) {
            if (context != null && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_LOCATION);
            } else {
                currentLat = coordinate.latitude;
                currentLng = coordinate.longitude;
            }
        } else {
            if (context != null && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_LOCATION);
            } else {
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLat = location.getLatitude();
                        currentLng = location.getLongitude();
                    }
                });
            }
        }
    }

}