package com.binus.pekalongancityguide.Layout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.Adapter.BookmarkAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Destination;
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

import java.util.ArrayList;

public class DestinationDetails extends AppCompatActivity {
    private ActivityDestinationDetailsBinding binding;
    String destiId;
    boolean inFavorite = false;
    FirebaseAuth firebaseAuth;

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
                        double latitude = Double.parseDouble(snapshot.child("latitude").getValue().toString());
                        double longitude = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                        binding.destiName.setText(title);
                        binding.destiDesc.setText(description);
                        binding.destiAddress.setText(address);
                        String filePath = getIntent().getStringExtra("imageFilePath");
                        if (filePath != null) {
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