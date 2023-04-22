package com.binus.pekalongancityguide.Layout;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.Adapter.CommentAdapter;
import com.binus.pekalongancityguide.Adapter.OpeningHoursAdapter;
import com.binus.pekalongancityguide.Adapter.ReviewAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Comments;
import com.binus.pekalongancityguide.ItemTemplate.OpeningHours;
import com.binus.pekalongancityguide.ItemTemplate.Review;
import com.binus.pekalongancityguide.Misc.ImageFullscreen;
import com.binus.pekalongancityguide.Misc.MyApplication;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityDestinationDetailsBinding;
import com.binus.pekalongancityguide.databinding.DialogAddCommentBinding;
import com.binus.pekalongancityguide.databinding.DialogAddToItineraryBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private ArrayList<Comments> commentsArrayList;
    private CommentAdapter commentAdapter;
    private static final String TAG = "REVIEW_TAG";
    private ProgressDialog progressDialog;
    int startHour,startMinute,startHour1,startMinute1
            ,endHour,endMinute,endHour1,endMinute1
            ,dayDate,monthDate,yearDate,dayDate1,monthDate1,yearDate1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDestinationDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        destiId = intent.getStringExtra("destiId");
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            checkFavorite();
        }
        if(firebaseAuth.getCurrentUser()==null) {
            binding.addCommentBtn.setVisibility(View.INVISIBLE);
        }
        loadDetails();
        loadComments();
        binding.backDesti.setOnClickListener(v -> onBackPressed());
        binding.destiImage.setOnClickListener(v -> {
            Intent intent1 = new Intent(this, ImageFullscreen.class);
            intent1.putExtra("fullImg", imageUrl);
            startActivity(intent1);
        });
            binding.addCommentBtn.setOnClickListener(v ->{
                showAddCommentDialog();
        });
        binding.saveItem.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() == null) {
                Toast.makeText(DestinationDetails.this,R.string.notLogin, Toast.LENGTH_SHORT).show();
            } else {
                if (inFavorite) {
                    MyApplication.removeFavorite(DestinationDetails.this, destiId);
                } else {
                    MyApplication.addtoFavorite(DestinationDetails.this, destiId);
                }
            }
        });
        binding.addItenary.setOnClickListener(v ->{
            showAddItineraryDialog();
        });
    }

    private void loadComments(){
        commentsArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
        reference.child(destiId).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentsArrayList.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Comments comments = dataSnapshot.getValue(Comments.class);
                            commentsArrayList.add(comments);
                        }
                        commentAdapter = new CommentAdapter(DestinationDetails.this,commentsArrayList);
                        binding.commentRv.setAdapter(commentAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
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
                DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
                reference.child(destiId).child("Comments").child(timestamp)
                        .setValue(hashMap)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(DestinationDetails.this,R.string.success_add_comment, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(DestinationDetails.this, getString(R.string.failed_add_comment)+e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            dialog.dismiss();
                        });
            }
        });
    }
    private void showAddItineraryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogAddToItineraryBinding addToItineraryBinding = DialogAddToItineraryBinding.inflate(getLayoutInflater());
        builder.setView(addToItineraryBinding.getRoot());
        EditText dateEt,startEt,endEt;
        ImageButton dateBtn,startBtn,endBtn;
        Button addItinerary;
        dateEt = addToItineraryBinding.dateEt;
        startEt = addToItineraryBinding.starttimeEt;
        endEt = addToItineraryBinding.endtimeEt;
        dateBtn = addToItineraryBinding.datepickerBtn;
        startBtn = addToItineraryBinding.startpickerBtn;
        endBtn = addToItineraryBinding.endpickerBtn;
        addItinerary = addToItineraryBinding.additineraryBtn;

        startBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            startHour = calendar.get(Calendar.HOUR_OF_DAY);
            startMinute = calendar.get(Calendar.MINUTE);
            MaterialTimePicker.Builder mybuilder = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(startHour)
                    .setMinute(startMinute)
                    .setTitleText("Select start time")
                    .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK);
            MaterialTimePicker dialog = mybuilder.build();
            dialog.addOnPositiveButtonClickListener(timeview -> {
                startHour = dialog.getHour();
                startMinute = dialog.getMinute();
                if (startHour < 12) {
                    startEt.setText(String.format(Locale.getDefault(), "%d:%02d am", startHour, startMinute));
                } else if (startHour == 12) {
                    startEt.setText(String.format(Locale.getDefault(), "12:%02d pm", startMinute));
                } else {
                    startEt.setText(String.format(Locale.getDefault(), "%d:%02d pm", startHour - 12, startMinute));
                }
            });
            dialog.show(getSupportFragmentManager(), "startTimePicker");
        });
        startEt.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            startHour1 = calendar.get(Calendar.HOUR_OF_DAY);
            startMinute1 = calendar.get(Calendar.MINUTE);
            MaterialTimePicker.Builder mybuilder = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(startHour1)
                    .setMinute(startMinute1)
                    .setTitleText("Select start time")
                    .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK);
            MaterialTimePicker dialog = mybuilder.build();
            dialog.addOnPositiveButtonClickListener(timeview -> {
                startHour1 = dialog.getHour();
                startMinute1 = dialog.getMinute();
                if (startHour1 < 12) {
                    startEt.setText(String.format(Locale.getDefault(), "%d:%02d am", startHour1, startMinute1));
                } else if (startHour1 == 12) {
                    startEt.setText(String.format(Locale.getDefault(), "12:%02d pm", startMinute1));
                } else {
                    startEt.setText(String.format(Locale.getDefault(), "%d:%02d pm", startHour1 - 12, startMinute1));
                }
            });
            dialog.show(getSupportFragmentManager(), "startTimePicker");
        });
        endBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            endHour = calendar.get(Calendar.HOUR_OF_DAY);
            endMinute = calendar.get(Calendar.MINUTE);
            MaterialTimePicker.Builder mybuilder = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(endHour)
                    .setMinute(endMinute)
                    .setTitleText("Select start time")
                    .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK);
            MaterialTimePicker dialog = mybuilder.build();
            dialog.addOnPositiveButtonClickListener(timeview -> {
                endHour = dialog.getHour();
                endMinute = dialog.getMinute();
                if (endHour < 12) {
                    startEt.setText(String.format(Locale.getDefault(), "%d:%02d am", endHour, endMinute));
                } else if (endHour == 12) {
                    startEt.setText(String.format(Locale.getDefault(), "12:%02d pm", endMinute));
                } else {
                    startEt.setText(String.format(Locale.getDefault(), "%d:%02d pm", endHour - 12, endMinute));
                }
            });
            dialog.show(getSupportFragmentManager(), "startTimePicker");
        });
        endEt.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            endHour1 = calendar.get(Calendar.HOUR_OF_DAY);
            endMinute1 = calendar.get(Calendar.MINUTE);
            MaterialTimePicker.Builder mybuilder = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(endHour1)
                    .setMinute(endMinute1)
                    .setTitleText("Select start time")
                    .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK);
            MaterialTimePicker dialog = mybuilder.build();
            dialog.addOnPositiveButtonClickListener(timeview -> {
                endHour1 = dialog.getHour();
                endMinute1 = dialog.getMinute();
                if (endHour1 < 12) {
                    startEt.setText(String.format(Locale.getDefault(), "%d:%02d am", endHour1, endMinute1));
                } else if (endHour1 == 12) {
                    startEt.setText(String.format(Locale.getDefault(), "12:%02d pm", endMinute1));
                } else {
                    startEt.setText(String.format(Locale.getDefault(), "%d:%02d pm", endHour1 - 12, endMinute1));
                }
            });
            dialog.show(getSupportFragmentManager(), "startTimePicker");
        });
        dateBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            yearDate = calendar.get(Calendar.YEAR);
            monthDate = calendar.get(Calendar.MONTH);
            dayDate = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog;
            dialog = new DatePickerDialog(this, (dateView, year, month, dayOfMonth) -> {
                yearDate = year;
                monthDate = month;
                dayDate = dayOfMonth;
                SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
                String dateString = format.format(new Date(yearDate - 1900, monthDate, dayDate));
                dateEt.setText(dateString);
            }, yearDate, monthDate, dayDate);
            dialog.getWindow().setBackgroundDrawableResource(R.color.palette_4);
            dialog.show();
        });
        dateEt.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            yearDate1 = calendar.get(Calendar.YEAR);
            monthDate1 = calendar.get(Calendar.MONTH);
            dayDate1 = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog;
            dialog = new DatePickerDialog(this, (dateView, year, month, dayOfMonth) -> {
                yearDate1 = year;
                monthDate1 = month;
                dayDate1 = dayOfMonth;
                SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
                String dateString = format.format(new Date(yearDate1 - 1900, monthDate1, dayDate1));
                dateEt.setText(dateString);
            }, yearDate1, monthDate1, dayDate1);
            dialog.getWindow().setBackgroundDrawableResource(R.color.palette_4);
            dialog.show();
        });
        addItinerary.setOnClickListener(v ->{
            validateData(dateEt, startEt, endEt);
        });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();
    }

    private String date = "", startTime = "", endTime = "";

    private void validateData(EditText dateEt, EditText startTimeEt, EditText endTimeEt) {
        date = dateEt.getText().toString().trim();
        startTime = startTimeEt.getText().toString().trim();
        endTime = endTimeEt.getText().toString().trim();
        boolean allFieldsFilled = true;

        if (TextUtils.isEmpty(date)) {
            dateEt.setError(getString(R.string.choose_date));
            allFieldsFilled = false;
        } else {
            dateEt.setError(null);
        }

        if (TextUtils.isEmpty(startTime)) {
            startTimeEt.setError(getString(R.string.choose_start));
            allFieldsFilled = false;
        } else {
            startTimeEt.setError(null);
        }

        if (TextUtils.isEmpty(endTime)) {
            endTimeEt.setError(getString(R.string.choose_end));
            allFieldsFilled = false;
        } else {
            endTimeEt.setError(null);
        }

        if (allFieldsFilled) {
            uploadToDB(date, startTime, endTime);
            Toast.makeText(this, R.string.added_to_iter, Toast.LENGTH_SHORT).show();
        }

    }

    private void uploadToDB(String date, String startTime, String endTime) {
        String uid = firebaseAuth.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
        reference.child(destiId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String placeID = "" + snapshot.child("placeId").getValue();
                Log.d(TAG, "placeID: " + placeID);
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("startTime", startTime);
                hashMap.put("endTime", endTime);
                hashMap.put("date", date);
                hashMap.put("destiId", destiId);
                DatabaseReference itineraryRef = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
                itineraryRef.child(uid).child("itinerary").push().setValue(hashMap)
                        .addOnSuccessListener(aVoid -> {
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(getApplicationContext(), "Itinerary uploaded successfully", Toast.LENGTH_LONG).show();
                        }).addOnFailureListener(e -> {
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }
                            Log.d(TAG, "on Failure : " + e.getMessage());
                    Toast.makeText(DestinationDetails.this, "Data upload failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
        reference.keepSynced(true);
        reference.child(destiId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot){
                        String title = "" + snapshot.child("title").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String address = "" + snapshot.child("address").getValue();
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

                        Glide.with(DestinationDetails.this)
                                .load(url)
                                .centerCrop()
                                .error(R.drawable.logo)
                                .into(binding.destiImage);

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
                            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(DestinationDetails.this, R.raw.map_style));
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
                            binding.saveItem.setText(R.string.unbookmark_text);
                        }else{
                            binding.saveItem.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.remove_bookmark,0,0);
                            binding.saveItem.setText(R.string.bookmark_text);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}