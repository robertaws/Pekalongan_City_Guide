package com.binus.pekalongancityguide.Layout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.binus.pekalongancityguide.Adapter.IterListAdapter;
import com.binus.pekalongancityguide.Adapter.ItineraryAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Itinerary;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.FragmentItineraryDetailsBinding;
import com.binus.pekalongancityguide.databinding.FragmentItineraryListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ItineraryList extends Fragment {
    private FragmentItineraryListBinding binding;
    private String TAG = "ITERFRAG_TAG";
    private FirebaseAuth firebaseAuth;
    private IterListAdapter iterListAdapter;
    public ItineraryList() {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentItineraryListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    private void loadItinerary() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference userRef = database.getReference("Users").child(firebaseAuth.getUid());
        userRef.keepSynced(true);

    }
}