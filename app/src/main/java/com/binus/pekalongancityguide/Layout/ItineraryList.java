package com.binus.pekalongancityguide.Layout;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.binus.pekalongancityguide.Adapter.ItineraryAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Itinerary;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityItineraryListBinding;
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

public class ItineraryList extends AppCompatActivity {
    public ActivityItineraryListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItineraryListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backtoprofile.setOnClickListener(v -> {
            onBackPressed();
        });
        ItineraryPagerAdapter vpAdapter = new ItineraryPagerAdapter(this, getSupportFragmentManager());
        binding.viewPager.setAdapter(vpAdapter);
        binding.itineraryTab.setupWithViewPager(binding.viewPager);
        binding.itineraryTab.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.white));
    }

    public class ItineraryPagerAdapter extends FragmentPagerAdapter {

        private final String[] tabTitles = new String[] {"Day 1"};
        private final Context context;

        public ItineraryPagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ItineraryFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }
}
