package com.binus.pekalongancityguide.Layout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.Adapter.CommentAdapter;
import com.binus.pekalongancityguide.Adapter.OpeningHoursAdapter;
import com.binus.pekalongancityguide.Adapter.ReviewAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Comments;
import com.binus.pekalongancityguide.ItemTemplate.OpeningHours;
import com.binus.pekalongancityguide.ItemTemplate.Review;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityDestinationDetailAdminBinding;
import com.binus.pekalongancityguide.databinding.DialogAddCommentBinding;
import com.bumptech.glide.Glide;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.binus.pekalongancityguide.Misc.Constants.FIREBASE_DATABASE_URL;

public class DestinationDetailAdmin extends AppCompatActivity {
    private ActivityDestinationDetailAdminBinding binding;
    String destiId;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private ArrayList<Comments> commentsArrayList;
    private CommentAdapter commentAdapter;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDestinationDetailAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);
        Intent intent = getIntent();
        destiId = intent.getStringExtra("destiId");
        loadDetails();
        loadComments();
        binding.backDestinAdmin.setOnClickListener(v -> onBackPressed());
        binding.addCommentAdminBtn.setOnClickListener(v ->{
            showAddCommentDialog();
        });
    }
    private String comment = "";
    private void showAddCommentDialog(){
        DialogAddCommentBinding commentBinding = DialogAddCommentBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(commentBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();
        commentBinding.addcommentBtn.setOnClickListener(v -> {
            comment = commentBinding.commentEt.getText().toString().trim();
            if(TextUtils.isEmpty(comment)){
                commentBinding.commentTil.setError(getString(R.string.comment_empty));
            }else{
                progressDialog.setMessage(getString(R.string.adding_comment));
                progressDialog.show();
                String timestamp = ""+System.currentTimeMillis();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("id",""+timestamp);
                hashMap.put("destiId",""+destiId);
                hashMap.put("timestamp",""+timestamp);
                hashMap.put("comment",""+comment);
                hashMap.put("uid",""+firebaseAuth.getUid());
                DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Destination");
                reference.child(destiId).child("Comments").child(timestamp)
                        .setValue(hashMap)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(DestinationDetailAdmin.this,R.string.success_add_comment, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(DestinationDetailAdmin.this, getString(R.string.failed_add_comment)+e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            dialog.dismiss();
                        });
            }
        });
    }
    private void loadComments(){
        commentsArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Destination");
        reference.child(destiId).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentsArrayList.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Comments comments = dataSnapshot.getValue(Comments.class);
                            commentsArrayList.add(comments);
                        }
                        commentAdapter = new CommentAdapter(DestinationDetailAdmin.this,commentsArrayList);
                        binding.adminCommentRv.setAdapter(commentAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadDetails(){
        DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Destination");
        reference.child(destiId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String title = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String address = ""+snapshot.child("address").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String url = ""+snapshot.child("url").getValue();
                        String phone = "" + snapshot.child("phoneNumber").getValue();
                        double latitude = Double.parseDouble(snapshot.child("latitude").getValue().toString());
                        double longitude = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                        binding.destiAdminName.setText(title);
                        binding.destiAdminDesc.setText(description);
                        binding.destiAdminAddress.setText(address);
                        binding.destiAdminPhone.setText("Phone Number: " + phone);
                        Map<String, String> openingHoursMap = new HashMap<>();
                        for (DataSnapshot hourSnapshot : snapshot.child("openingHours").getChildren()) {
                            String dayOfWeek = hourSnapshot.getKey();
                            String openingHours = hourSnapshot.getValue(String.class);
                            openingHoursMap.put(dayOfWeek, openingHours);
                        }

                        for (Map.Entry<String, String> entry : openingHoursMap.entrySet()) {
                            String dayOfWeek = entry.getKey();
                            String openingHours = entry.getValue();
                            System.out.println(dayOfWeek + ": " + openingHours);
                        }
                        List<OpeningHours> openingHoursList = new ArrayList<>();
                        for (Map.Entry<String, String> entry : openingHoursMap.entrySet()) {
                            String dayOfWeek = entry.getKey();
                            String openingHours = entry.getValue();
                            openingHoursList.add(new OpeningHours(dayOfWeek, openingHours));
                        }
                        OpeningHoursAdapter hoursAdapter = new OpeningHoursAdapter(openingHoursList);
                        binding.adminOpeningRv.setAdapter(hoursAdapter);

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
                        Glide.with(DestinationDetailAdmin.this)
                                .load(url)
                                .centerCrop()
                                .error(R.drawable.logo)
                                .into(binding.destiAdminImage);
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