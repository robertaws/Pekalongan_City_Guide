package com.binus.pekalongancityguide.Layout;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.binus.pekalongancityguide.Misc.ToastUtils;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityDestinationDetailsBinding;
import com.binus.pekalongancityguide.databinding.DialogAddCommentBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.binus.pekalongancityguide.Misc.Constants.FIREBASE_DATABASE_URL;

public class DestinationDetails extends AppCompatActivity {
    private String categoryId, date, startDate, openingHours, startTime, endTime, subtitle;
    private int startHour, startMinute, endHour, endMinute, startYear, startMonth, startDay;
    String imageUrl;
    private final List<String> openHours = new ArrayList<>();
    private final List<String> closeHours = new ArrayList<>();
    private ActivityDestinationDetailsBinding binding;
    String destiId;
    private LinearLayout containerLayout;
    private SimpleDateFormat format;
    private EditText startEt, endEt, dateEt;
    private ImageButton startBtn, endBtn, dateBtn;
    private Calendar calendar;
    private AlertDialog dialog;
    boolean inFavorite = false;
    FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private ArrayList<Comments> commentsArrayList;
    private CommentAdapter commentAdapter;
    private static final String TAG = "REVIEW_TAG";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDestinationDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        calendar = Calendar.getInstance();
        format = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
        destiId = intent.getStringExtra("destiId");
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);
        database = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            checkFavorite();
        }
        if (firebaseAuth.getCurrentUser() == null) {
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
                    MyApplication.removeFavorite(DestinationDetails.this, destiId,firebaseAuth.getUid());
                } else {
                    MyApplication.addtoFavorite(DestinationDetails.this, destiId,firebaseAuth.getUid());
                }
            }
        });
        binding.addItenary.setOnClickListener(v ->{
            if (firebaseAuth.getCurrentUser() == null) {
                Toast.makeText(DestinationDetails.this,R.string.notLogin, Toast.LENGTH_SHORT).show();
            }else{
                showAddItineraryDialog();
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
                DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Destination");
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

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_input_details, null);
        builder.setView(view);

        containerLayout = view.findViewById(R.id.container_layout);
        RelativeLayout timePickerLayout = view.findViewById(R.id.time_picker_container);
        Button addBtn = view.findViewById(R.id.add_iter_button);
        TextView titleText = view.findViewById(R.id.dialog_title);
        TextView subtitleText = view.findViewById(R.id.dialog_subtitle);

        containerLayout.setVisibility(View.GONE);
        timePickerLayout.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams pickerLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        pickerLayoutParams.addRule(RelativeLayout.BELOW, subtitleText.getId());
        timePickerLayout.setLayoutParams(pickerLayoutParams);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.BELOW, timePickerLayout.getId());
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        addBtn.setLayoutParams(layoutParams);

        dateEt = view.findViewById(R.id.date_et);
        startEt = view.findViewById(R.id.starttime_et);
        endEt = view.findViewById(R.id.endtime_et);
        dateBtn = view.findViewById(R.id.datepicker_btn);
        startBtn = view.findViewById(R.id.startpicker_btn);
        endBtn = view.findViewById(R.id.endpicker_btn);

        startBtn.setEnabled(false);
        startEt.setEnabled(false);
        endBtn.setEnabled(false);
        endEt.setEnabled(false);

        dateBtn.setOnClickListener(v -> showCalendar());
        dateEt.setOnClickListener(v -> showCalendar());
        startBtn.setOnClickListener(v -> showStartTimer());
        startEt.setOnClickListener(v -> showStartTimer());
        endBtn.setOnClickListener(v -> showEndTimer());
        endEt.setOnClickListener(v -> showEndTimer());

        if (categoryId.equals("1680077442322")) {
            subtitle = getString(R.string.thirty2Hour);
        } else if (categoryId.equals("1680077753090") || categoryId.equals("1681812366209")) {
            subtitle = getString(R.string.thirty_oneHour);
        } else if (categoryId.equals("1680166171816") || categoryId.equals("1681807033132")) {
            subtitle = getString(R.string.depend_onNeeds);
        } else if (categoryId.equals("1680167384847") || categoryId.equals("1681811119599")) {
            subtitle = getString(R.string.one_two_hour);
        } else if (categoryId.equals("1682060935295") || categoryId.equals("1680077486439")) {
            subtitle = getString(R.string.based_on);
        } else if (categoryId.equals("1682061580514")) {
            subtitle = getString(R.string.forty5_minute);
        } else {
            subtitle = getString(R.string.not_found);
        }

        addBtn.setText(getString(R.string.add_to_itinerary_btn));
        titleText.setText(R.string.pickTheTime);
        subtitleText.setText(getString(R.string.rekomen_time) + subtitle);

        dialog = builder.create();
        dialog.setOnDismissListener(dialog1 -> {
            openHours.clear();
            closeHours.clear();
        });
        addBtn.setOnClickListener(v -> validateData(dateEt, startEt, endEt));

        dialog.show();
    }

    private void getOpeningHours(int dayOfWeek) {
        DatabaseReference openingHoursRef = database.getReference("Destination");
        openingHoursRef.child(destiId).child("openingHours").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                openHours.clear();
                closeHours.clear();
                int newDoW = dayOfWeek - 2;
                if (newDoW == -1) {
                    newDoW = 6;
                }
                if (dataSnapshot.exists()) {
                    openingHours = dataSnapshot.child(String.valueOf(newDoW)).getValue(String.class);
                    Log.d(TAG, "CHECK DAY: " + (newDoW));
                    Log.d(TAG, "OPENING HOURS: " + openingHours);

                    if (openingHours != null) {
                        String[] parts = openingHours.split(": ");
                        if (parts.length == 2) {
                            String timeRange = parts[1];

                            if (timeRange.equals("Open 24 hours")) {
                                startTime = "12:00 AM";
                                endTime = "11:59 PM";
                                openHours.add(startTime);
                                closeHours.add(endTime);
                            } else if (timeRange.equals("Closed")) {
                                startTime = "";
                                endTime = "";
                                openingHours = "Closed";
                                ToastUtils.showToast(DestinationDetails.this, getString(R.string.justnotAvail), Toast.LENGTH_SHORT);
                                openHours.add(startTime);
                                closeHours.add(endTime);
                            } else {
                                String[] timeSlots = timeRange.split(", ");

                                for (String slot : timeSlots) {
                                    String[] times = slot.split(" – ");

                                    if (times.length == 2) {
                                        String startTimeSlot = times[0];
                                        String endTimeSlot = times[1];

                                        openHours.add(startTimeSlot);
                                        closeHours.add(endTimeSlot);
                                    } else {
                                        ToastUtils.showToast(DestinationDetails.this, getString(R.string.invalidTimeSlot) + slot, Toast.LENGTH_SHORT);
                                    }
                                }
                            }
                        } else {
                            ToastUtils.showToast(DestinationDetails.this, getString(R.string.invalid_opening_format), Toast.LENGTH_SHORT);
                        }
                    } else {
                        startTime = "";
                        endTime = "";
                        openingHours = "Closed";
                        ToastUtils.showToast(DestinationDetails.this, getString(R.string.justnotAvail), Toast.LENGTH_SHORT);
                        openHours.add(startTime);
                        closeHours.add(endTime);
                    }
                    Log.d(TAG, "Open hour: " + openHours);
                    Log.d(TAG, "Close hour: " + closeHours);
                } else {
                    startTime = "12:00 AM";
                    endTime = "11:59 PM";
                    openingHours = "Not Found";
                    ToastUtils.showToast(DestinationDetails.this, getString(R.string.data_not_found), Toast.LENGTH_SHORT);
                    new Handler().postDelayed(() -> {
                        ToastUtils.showToast(DestinationDetails.this, getString(R.string.allowAnyTime), Toast.LENGTH_SHORT);
                    }, 2000);
                    openHours.add(startTime);
                    closeHours.add(endTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void showCalendar() {
        dateEt.setText("");
        startYear = calendar.get(Calendar.YEAR);
        startMonth = calendar.get(Calendar.MONTH);
        startDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(DestinationDetails.this, (dateView, year, month, dayOfMonth) -> {
            startYear = year;
            startMonth = month;
            startDay = dayOfMonth;
            calendar.set(startYear, startMonth, startDay);

            SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
            startDate = format.format(calendar.getTime());
            Log.d(TAG, "showCalendar: " + startDate);
            dateEt.setText(startDate);
            startBtn.setEnabled(true);
            startEt.setEnabled(true);

            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            getOpeningHours(dayOfWeek);
        }, startYear, startMonth, startDay);

        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        dialog.getWindow().setBackgroundDrawableResource(R.color.palette_4);
        dialog.show();
    }

    private void showStartTimer() {
        startEt.setText("");
        if (openHours.isEmpty()) {
            ToastUtils.showToast(DestinationDetails.this, getString(R.string.data_not_found), Toast.LENGTH_SHORT);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(DestinationDetails.this);
        View customView = inflater.inflate(R.layout.dialog_custom_title, null);
        TextView dialogRealTitle = customView.findViewById(R.id.dialog_title);
        TextView dialogTitle = customView.findViewById(R.id.dialog_subtitle);
        ViewGroup timePickerContainer = customView.findViewById(R.id.time_picker_container);
        dialogRealTitle.setText(R.string.select_start_time_iter);

        String dialogTitleText = String.format(Locale.getDefault(), "Opening Hour: %s", openingHours);
        dialogTitle.setText(dialogTitleText);

        AlertDialog.Builder builder = new AlertDialog.Builder(DestinationDetails.this)
                .setView(customView)
                .setNegativeButton(R.string.cancel_opt, null);

        TimePicker timePicker = new TimePicker(new ContextThemeWrapper(DestinationDetails.this, R.style.TimePickerStyle));
        timePicker.setIs24HourView(false);

        timePickerContainer.addView(timePicker);

        builder.setPositiveButton("OK", (dialogInterface, which) -> {
            int selectedHour = timePicker.getCurrentHour();
            int selectedMinute = timePicker.getCurrentMinute();

            boolean withinOpeningHours = false;

            for (int i = 0; i < openHours.size(); i++) {
                String openingTime = openHours.get(i);
                String closingTime = closeHours.get(i);

                if (openingTime == null || closingTime == null) {
                    ToastUtils.showToast(DestinationDetails.this, getString(R.string.data_not_found), Toast.LENGTH_SHORT);
                    return;
                }

                int openingHour = convertTo24HourFormat(openingTime);
                int openingMinute = Integer.parseInt(openingTime.split(":")[1].split(" ")[0]);

                int closingHour = convertTo24HourFormat(closingTime);
                int closingMinute = Integer.parseInt(closingTime.split(":")[1].split(" ")[0]);

                if (closingHour < openingHour || (closingHour == openingHour && closingMinute < openingMinute)) {
                    if (selectedHour > openingHour || (selectedHour == openingHour && selectedMinute >= openingMinute)) {
                        withinOpeningHours = true;
                        break;
                    }
                    if (selectedHour < closingHour || (selectedHour == closingHour && selectedMinute <= closingMinute)) {
                        withinOpeningHours = true;
                        break;
                    }
                } else {
                    if (selectedHour > openingHour || (selectedHour == openingHour && selectedMinute >= openingMinute)) {
                        if (selectedHour < closingHour || (selectedHour == closingHour && selectedMinute <= closingMinute)) {
                            withinOpeningHours = true;
                            break;
                        }
                    }
                }
            }

            if (withinOpeningHours) {
                startHour = selectedHour;
                startMinute = selectedMinute;

                if (startHour < 12) {
                    startEt.setText(String.format(Locale.getDefault(), "%d:%02d AM", startHour, startMinute));
                } else if (startHour == 12) {
                    startEt.setText(String.format(Locale.getDefault(), "12:%02d PM", startMinute));
                } else {
                    startEt.setText(String.format(Locale.getDefault(), "%d:%02d PM", startHour - 12, startMinute));
                }

                endEt.setEnabled(true);
                endBtn.setEnabled(true);
            } else {
                ToastUtils.showToast(DestinationDetails.this, getString(R.string.outside_business), Toast.LENGTH_SHORT);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showEndTimer() {
        endEt.setText("");
        if (openHours.isEmpty()) {
            ToastUtils.showToast(DestinationDetails.this, getString(R.string.data_not_found), Toast.LENGTH_SHORT);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(DestinationDetails.this);
        View customView = inflater.inflate(R.layout.dialog_custom_title, null);
        TextView dialogRealTitle = customView.findViewById(R.id.dialog_title);
        TextView dialogTitle = customView.findViewById(R.id.dialog_subtitle);
        ViewGroup timePickerContainer = customView.findViewById(R.id.time_picker_container);
        dialogRealTitle.setText(R.string.select_end_time_iter);

        String dialogTitleText = String.format(Locale.getDefault(), "Opening Hour: %s", openingHours);
        dialogTitle.setText(dialogTitleText);

        AlertDialog.Builder builder = new AlertDialog.Builder(DestinationDetails.this)
                .setView(customView)
                .setNegativeButton(R.string.cancel_opt, null);

        TimePicker timePicker = new TimePicker(new ContextThemeWrapper(DestinationDetails.this, R.style.TimePickerStyle));
        timePicker.setIs24HourView(false);

        timePickerContainer.addView(timePicker);

        builder.setPositiveButton("OK", (dialogInterface, which) -> {
            int selectedHour = timePicker.getCurrentHour();
            int selectedMinute = timePicker.getCurrentMinute();

            boolean withinOpeningHours = false;
            String errorMessage = null;

            for (int i = 0; i < openHours.size(); i++) {
                String openingTime = openHours.get(i);
                String closingTime = closeHours.get(i);

                if (openingTime == null || closingTime == null) {
                    errorMessage = getString(R.string.data_not_found);
                    break;
                }

                int closingHour = convertTo24HourFormat(closingTime);
                int closingMinute = Integer.parseInt(closingTime.split(":")[1].split(" ")[0]);

                if ((closingHour < startHour || (closingHour == startHour && closingMinute < startMinute))
                        || (selectedHour > closingHour || (selectedHour == closingHour && selectedMinute > closingMinute))
                        || (selectedHour < startHour || (selectedHour == startHour && selectedMinute < startMinute))) {
                    withinOpeningHours = false;
                    if (selectedHour < startHour || (selectedHour == startHour && selectedMinute < startMinute)) {
                        errorMessage = getString(R.string.end_time_earlier);
                    } else {
                        errorMessage = getString(R.string.outside_business);
                    }
                    break;
                } else {
                    withinOpeningHours = true;
                }
            }

            if (errorMessage != null) {
                ToastUtils.showToast(DestinationDetails.this, errorMessage, Toast.LENGTH_SHORT);
            } else {
                if (withinOpeningHours) {
                    endHour = selectedHour;
                    endMinute = selectedMinute;

                    if (endHour < 12) {
                        endEt.setText(String.format(Locale.getDefault(), "%d:%02d AM", endHour, endMinute));
                    } else if (endHour == 12) {
                        endEt.setText(String.format(Locale.getDefault(), "12:%02d PM", endMinute));
                    } else {
                        endEt.setText(String.format(Locale.getDefault(), "%d:%02d PM", endHour - 12, endMinute));
                    }
                } else {
                    ToastUtils.showToast(DestinationDetails.this, getString(R.string.outside_business), Toast.LENGTH_SHORT);
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private int convertTo24HourFormat(String time) {
        if (time == null) {
            return 0;
        }

        String[] parts = time.split(":");
        if (parts.length < 2) {
            return 0;
        }

        int hour = Integer.parseInt(parts[0]);

        if (parts[1].contains(" ")) {
            String amPm = parts[1].split(" ")[1];
            if (amPm.equalsIgnoreCase("PM") && hour != 12) {
                hour += 12;
            } else if (amPm.equalsIgnoreCase("AM") && hour == 12) {
                hour = 0;
            }
        }

        return hour;
    }

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
            onBackPressed();
        }

    }

    private void uploadToDB(String date, String startTime, String endTime) {
        String uid = firebaseAuth.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL)
                .getReference("Destination");
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
                DatabaseReference itineraryRef = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL)
                        .getReference("Users")
                        .child(uid)
                        .child("itinerary");

                String itineraryId = itineraryRef.push().getKey();
                hashMap.put("itineraryId", itineraryId);

                itineraryRef.child(itineraryId).setValue(hashMap)
                        .addOnSuccessListener(aVoid -> {
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(getApplicationContext(), "Itinerary uploaded successfully", Toast.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(e -> {
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(DestinationDetails.this, "Data upload failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "on Failure: " + e.getMessage());
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Destination");
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
                        categoryId = "" + snapshot.child("categoryId").getValue();
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
        DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Users");
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