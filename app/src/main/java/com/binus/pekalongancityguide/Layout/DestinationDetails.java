package com.binus.pekalongancityguide.Layout;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.Adapter.OpeningHoursAdapter;
import com.binus.pekalongancityguide.Adapter.ReviewAdapter;
import com.binus.pekalongancityguide.ItemTemplate.OpeningHours;
import com.binus.pekalongancityguide.ItemTemplate.Review;
import com.binus.pekalongancityguide.Misc.AddToItineraryDialog;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DestinationDetails extends AppCompatActivity {
    String imageUrl;
    private ActivityDestinationDetailsBinding binding;
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
        if (firebaseAuth.getCurrentUser() != null) {
            checkFavorite();
        }
        loadDetails();
        binding.backDesti.setOnClickListener(v -> onBackPressed());
        binding.destiImage.setOnClickListener(v -> {
            Intent intent1 = new Intent(this, ImageFullscreen.class);
            intent1.putExtra("fullImg", imageUrl);
            startActivity(intent1);
        });
        binding.addItenary.setOnClickListener(v -> {
            AddToItineraryDialog dialog = new AddToItineraryDialog();
            dialog.show(getSupportFragmentManager(), "add_to_itinerary_dialog");
        });
        binding.saveItem.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() == null) {
                Toast.makeText(DestinationDetails.this, "You are not logged in!", Toast.LENGTH_SHORT).show();
            } else {
                if (inFavorite) {
                    MyApplication.removeFavorite(DestinationDetails.this, destiId);
                } else {
                    MyApplication.addtoFavorite(DestinationDetails.this, destiId);
                }
            }
        });
    }

    private void showAddItineraryDialog() {
        final Dialog dialog = new Dialog(DestinationDetails.this);
        dialog.setContentView(R.layout.dialog_add_to_itinerary);
        dialog.setTitle("Add to Itinerary");

        // Get references to the views in the dialog
        final EditText dateEditText = dialog.findViewById(R.id.date_edit_text);
        final Button dateButton = dialog.findViewById(R.id.date_button);
        final EditText startTimeEditText = dialog.findViewById(R.id.start_time_edit_text);
        final Button startTimeButton = dialog.findViewById(R.id.start_time_button);
        final EditText endTimeEditText = dialog.findViewById(R.id.end_time_edit_text);
        final Button endTimeButton = dialog.findViewById(R.id.end_time_button);
        Button addToItineraryButton = dialog.findViewById(R.id.add_to_itinerary_button);

        // Set onClickListener to the date button
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the date picker
                DatePickerDialog datePickerDialog = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    datePickerDialog = new DatePickerDialog(DestinationDetails.this);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    datePickerDialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {
                        // Set the selected date to the date EditText
                        String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                        dateEditText.setText(date);
                    });
                }
                datePickerDialog.show();
            }
        });

        // Set onClickListener to the start time button
        startTimeButton.setOnClickListener(v -> {
            // Show the start time picker
            TimePickerDialog timePickerDialog = new TimePickerDialog(DestinationDetails.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    // Set the selected start time to the start time EditText
                    String startTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    startTimeEditText.setText(startTime);
                }
            }, 0, 0, true);
            timePickerDialog.show();
        });

        // Set onClickListener to the end time button
        endTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the end time picker
                TimePickerDialog timePickerDialog = new TimePickerDialog(DestinationDetails.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Set the selected end time to the end time EditText
                        String endTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        endTimeEditText.setText(endTime);
                    }
                }, 0, 0, true);
                timePickerDialog.show();
            }
        });

        // Set onClickListener to the "add to itinerary" button
        addToItineraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Add the selected date, start time, and end time to the itinerary
                // Dismiss the dialog
                dialog.dismiss();
            }
        });

        // Show the dialog
        dialog.show();
    }

    private void loadDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
        reference.child(destiId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String title = "" + snapshot.child("title").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String address = "" + snapshot.child("address").getValue();
                        String categoryId = "" + snapshot.child("categoryId").getValue();
                        String url = "" + snapshot.child("url").getValue();
                        String phone = "" + snapshot.child("phoneNumber").getValue();
                        double latitude = Double.parseDouble(snapshot.child("latitude").getValue().toString());
                        double longitude = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                        binding.destiName.setText(title);
                        binding.destiDesc.setText(description);
                        binding.destiAddress.setText(address);
                        binding.destiPhone.setText("Phone Number: " + phone);
                        imageUrl = url;
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
                        binding.openingRv.setAdapter(hoursAdapter);

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