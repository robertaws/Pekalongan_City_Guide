package com.binus.pekalongancityguide.Layout;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.FragmentEmptyBookmark2Binding;
import com.binus.pekalongancityguide.databinding.FragmentEmptyItinerary2Binding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class EmptyItinerary extends Fragment {
    private FragmentEmptyItinerary2Binding binding;
    private static final int PERMISSION_REQUEST_LOCATION = 500;
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLat,currentLng;
    public EmptyItinerary() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEmptyItinerary2Binding.inflate(inflater, container, false);
        getCurLoc();
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
    private void getCurLoc() {
        if (getContext() != null && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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