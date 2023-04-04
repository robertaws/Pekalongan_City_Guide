package com.binus.pekalongancityguide.Layout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.Adapter.BookmarkAdapter;
import com.binus.pekalongancityguide.Adapter.OpeningHoursAdapter;
import com.binus.pekalongancityguide.Adapter.ReviewAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Destination;
import com.binus.pekalongancityguide.ItemTemplate.OpeningHours;
import com.binus.pekalongancityguide.ItemTemplate.Review;
import com.binus.pekalongancityguide.Misc.ImageFullscreen;
import com.binus.pekalongancityguide.Misc.MyApplication;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityDestinationDetailsBinding;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DestinationDetails extends AppCompatActivity {
    String imageUrl;
    private ActivityDestinationDetailsBinding binding;
    private OpeningHoursAdapter openingHoursAdapter;
    String destiId;
    boolean inFavorite = false;
    FirebaseAuth firebaseAuth;
    private static final String TAG = "REVIEW_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDestinationDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        destiId = intent.getStringExtra("destiId");
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null){
            checkFavorite();
        }
        loadDetails();
        binding.backDesti.setOnClickListener(v -> onBackPressed());
        binding.destiImage.setOnClickListener(v -> {
            Intent intent1 = new Intent(this,ImageFullscreen.class);
            intent1.putExtra("fullImg", imageUrl);
            startActivity(intent1);
        });

        binding.saveItem.setOnClickListener(v -> {
            if(firebaseAuth.getCurrentUser() == null){
                Toast.makeText(DestinationDetails.this, "You are not logged in!", Toast.LENGTH_SHORT).show();
            }else{
                if(inFavorite){
                    MyApplication.removeFavorite(DestinationDetails.this,destiId);
                }else{
                    MyApplication.addtoFavorite(DestinationDetails.this,destiId);
                }
            }
        });
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
                        String phone = ""+snapshot.child("phoneNumber").getValue();
                        double latitude = Double.parseDouble(snapshot.child("latitude").getValue().toString());
                        double longitude = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                        binding.destiName.setText(title);
                        binding.destiDesc.setText(description);
                        binding.destiAddress.setText(address);
                        binding.destiPhone.setText("Phone Number: "+phone);
                        imageUrl = url;
                        List<Review> reviews = new ArrayList<>();
                        for (DataSnapshot reviewSnapshot : snapshot.child("reviews").getChildren()) {
                            String authorName = reviewSnapshot.child("authorName").getValue(String.class);
                            int rating = reviewSnapshot.child("rating").getValue(int.class);
                            String text = reviewSnapshot.child("text").getValue(String.class);
                            reviews.add(new Review(authorName, rating, text));
                        }
                        ReviewAdapter reviewAdapter = new ReviewAdapter(reviews);
                        binding.reviewRv.setAdapter(reviewAdapter);
                        binding.reviewRv.setAdapter(new ReviewAdapter(reviews));

                        String filePath = getIntent().getStringExtra("imageFilePath");
                        if (filePath != null){
                            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                            binding.destiImage.setBackground(drawable);
                        }
                        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        fragment.getMapAsync(googleMap ->{
                            LatLng coordinate = new LatLng(latitude, longitude);
                            MarkerOptions marker = new MarkerOptions();
                            marker = marker.position(coordinate);
                            marker = marker.title(title);
                            googleMap.addMarker(marker);
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
                            googleMap.moveCamera(cameraUpdate);
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Destination")
                .child("openingHours");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<OpeningHours> openingHoursList = new ArrayList<>();
                for (DataSnapshot openingHoursSnapshot : snapshot.getChildren()) {
                    OpeningHours openingHours = openingHoursSnapshot.getValue(OpeningHours.class);
                    openingHoursList.add(openingHours);
                }
                openingHoursAdapter = new OpeningHoursAdapter(openingHoursList);
                binding.openingRv.setAdapter(openingHoursAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    private void checkFavorite(){
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorites").child(destiId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        inFavorite = snapshot.exists();
                        if(inFavorite){
                            binding.saveItem.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.bookmark,0,0);
                            binding.saveItem.setText("Remove from Bookmark");
                        }else{
                            binding.saveItem.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.remove_bookmark,0,0);
                            binding.saveItem.setText("Bookmark Place");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}