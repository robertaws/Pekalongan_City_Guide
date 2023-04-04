package com.binus.pekalongancityguide.Layout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.Adapter.ReviewAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Review;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityDestinationDetailAdminBinding;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DestinationDetailAdmin extends AppCompatActivity {
    private ActivityDestinationDetailAdminBinding binding;
    String destiId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDestinationDetailAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        destiId = intent.getStringExtra("destiId");

        loadDetails();
        binding.backDestinAdmin.setOnClickListener(v -> onBackPressed());

    }
    private void loadDetails(){
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
        reference.child(destiId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String title = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String address = ""+snapshot.child("address").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String url = ""+snapshot.child("url").getValue();
                        double latitude = Double.parseDouble(snapshot.child("latitude").getValue().toString());
                        double longitude = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                        binding.destiAdminName.setText(title);
                        binding.destiAdminDesc.setText(description);
                        binding.destiAdminAddress.setText(address);

                        List<Review> reviews = new ArrayList<>();
                        for (DataSnapshot reviewSnapshot : snapshot.child("reviews").getChildren()) {
                            String authorName = reviewSnapshot.child("authorName").getValue(String.class);
                            int rating = reviewSnapshot.child("rating").getValue(int.class);
                            String text = reviewSnapshot.child("text").getValue(String.class);
                            reviews.add(new Review(authorName, rating, text));
                        }
                        ReviewAdapter reviewAdapter = new ReviewAdapter(reviews);
                        binding.adminreviewRv.setAdapter(reviewAdapter);

                        binding.adminreviewRv.setAdapter(new ReviewAdapter(reviews));
                        String filePath = getIntent().getStringExtra("imageFilePath");
                        if (filePath != null) {
                            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                            binding.destiAdminImage.setBackground(drawable);
                        }
                        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.admin_map);
                        fragment.getMapAsync(googleMap -> {
                            LatLng coordinate = new LatLng(latitude, longitude);
                            MarkerOptions marker = new MarkerOptions();
                            marker.position(coordinate);
                            marker.title(title);
                            googleMap.addMarker(marker);
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
                            googleMap.moveCamera(cameraUpdate);
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}