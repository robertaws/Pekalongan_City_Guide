package com.binus.pekalongancityguide.Adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Itinerary;
import com.binus.pekalongancityguide.Layout.Home;
import com.binus.pekalongancityguide.Misc.AlphaTransformation;
import com.binus.pekalongancityguide.Misc.MyApplication;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.DialogAddToItineraryBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.ItineraryViewHolder> {
    private static final int MAPS_PERMIT = 1;
    private final Context context;
    private final List<Itinerary> itineraryList;
    private final FragmentManager fragmentManager;
    private FirebaseDatabase database;
    private EditText startEt, endEt, dateEt;
    private ImageButton dateBtn, startBtn, endBtn;
    private Calendar calendar;
    private String startDate, startTime, endTime, date, openingHours;
    String destiID;
    private int startHour, startMinute, endHour, endMinute, startDay, startMonth, startYear;

    public ItineraryAdapter(Context context, List<Itinerary> itineraryList, FragmentManager fragmentManager) {
        this.context = context;
        this.itineraryList = itineraryList;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public ItineraryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_itinerary, parent, false);
        database = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");
        return new ItineraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItineraryViewHolder holder, int position) {
        Itinerary itinerary = itineraryList.get(position);
        loadImage(itinerary, holder);
        holder.startTimeTextView.setText(itinerary.getStartTime());
        holder.endTimeTextView.setText(itinerary.getEndTime());
        holder.placeNameTextView.setText(itinerary.getPlaceName());
        float distance = itinerary.getDistance();
        String distanceString;
        if (distance < 1) {
            int distanceInMeters = (int) (distance * 1000);
            distanceString = distanceInMeters + " m";

        } else {
            distanceString = String.format(Locale.getDefault(), "%.2f km", distance);
        }
        holder.distanceTextView.setText(distanceString);
        holder.durationTextView.setText(itinerary.getDurationText());
        holder.optionBtn.setOnClickListener(v -> {
            showOptionsDialog(itinerary, holder);
        });
        holder.itemView.setOnClickListener(v -> {
            if (holder.isImageLoaded) {
                double latitude = itinerary.getLatitude();
                double longitude = itinerary.getLongitude();
                String origin = getMyLocation();
                String url = "https://www.google.com/maps/dir/?api=1&origin=" + origin + "&itinerary=" + latitude + "," + longitude + "&travelmode=driving";
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                } else {
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(webIntent);
                }
            }
        });
    }

    private void showOptionsDialog(Itinerary itinerary, ItineraryViewHolder holder) {
        String DESTIID = itinerary.getDestiId();
        destiID = DESTIID;
        String[] options = {"Edit", "Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, (dialog, which) -> {
                    if(which==0){
                        showEditDialog(fragmentManager);
                    }else{
                        MyApplication.deleteIter(
                                context, ""+DESTIID
                        );
                    }
                })
                .show();
    }

    private void showEditDialog(FragmentManager fragmentManager){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        calendar = Calendar.getInstance();
        DialogAddToItineraryBinding addToItineraryBinding = DialogAddToItineraryBinding.inflate(inflater);
        builder.setView(addToItineraryBinding.getRoot());
        Button addItinerary;
        dateEt = addToItineraryBinding.dateEt;
        startEt = addToItineraryBinding.starttimeEt;
        endEt = addToItineraryBinding.endtimeEt;
        dateBtn = addToItineraryBinding.datepickerBtn;
        startBtn = addToItineraryBinding.startpickerBtn;
        endBtn = addToItineraryBinding.endpickerBtn;
        addItinerary = addToItineraryBinding.additineraryBtn;

        startBtn.setEnabled(false);
        startEt.setEnabled(false);
        endBtn.setEnabled(false);
        endEt.setEnabled(false);

        startBtn.setOnClickListener(v -> showStartTimer());
        startEt.setOnClickListener(v -> showStartTimer());
        endBtn.setOnClickListener(v -> showEndTimer());
        endEt.setOnClickListener(v -> showEndTimer());
        dateBtn.setOnClickListener(v -> showCalendar());
        dateEt.setOnClickListener(v -> showCalendar());
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();
        addItinerary.setOnClickListener(v -> {
            validateData(dateEt, startEt, endEt);
            dialog.dismiss();
        });
    }

    private void showCalendar() {
        startYear = calendar.get(Calendar.YEAR);
        startMonth = calendar.get(Calendar.MONTH);
        startDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(context, (dateView, year, month, dayOfMonth) -> {
            startYear = year;
            startMonth = month;
            startDay = dayOfMonth;
            calendar.set(startYear, startMonth, startDay); // Set the selected date to the Calendar object

            SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
            startDate = format.format(calendar.getTime());
            Log.d(TAG, "showCalendar: " + startDate);
            dateEt.setText(startDate);
            startBtn.setEnabled(true);
            startEt.setEnabled(true);

            // Get the day of the week for the selected date
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            // Retrieve the opening hours for the selected day
            getOpeningHours(dayOfWeek);
        }, startYear, startMonth, startDay);

        // Set the minimum date to today's date
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        dialog.getWindow().setBackgroundDrawableResource(R.color.palette_4);
        dialog.show();
    }

    private void getOpeningHours(int dayOfWeek) {
        DatabaseReference openingHoursRef = database.getReference("Destination");
        openingHoursRef.child(destiID).child("openingHours").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    openingHours = dataSnapshot.child(String.valueOf(dayOfWeek - 2)).getValue(String.class);
                    Log.d(TAG, "OPENING HOURS: " + openingHours);

                    if (openingHours != null) {
                        // Split the opening hours data into day and time range
                        String[] parts = openingHours.split(": ");

                        if (parts.length == 2) {
                            String day = parts[0];
                            String timeRange = parts[1];

                            if (timeRange.equals("Open 24 hours")) {
                                startTime = "12:00 AM";
                                endTime = "11:59 PM";
                            } else {
                                String[] times = timeRange.trim().split(" – ");

                                if (times.length == 2) {
                                    startTime = times[0];
                                    Log.d(TAG, "Start Time: " + startTime);
                                    endTime = times[1];
                                    Log.d(TAG, "End Time: " + endTime);

                                    // Now you have the valid start and end time values
                                    // Perform any further processing as needed
                                } else {
                                    // Handle the case when the time range is invalid or not in the expected format
                                    Toast.makeText(context, "Invalid time range", Toast.LENGTH_SHORT).show();
                                }
                            }
                            // Remove whitespace and split the time range into start and end time
                        } else {
                            // Handle the case when the opening hours data is not in the expected format
                            Toast.makeText(context, "Invalid opening hours format", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle the case when the opening hours data is null or not available for the selected day
                        startTime = "12:00 AM";
                        endTime = "11:59 PM";
                        Toast.makeText(context, "Opening hours not available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle the case when the opening hours data doesn't exist in the database
                    Toast.makeText(context, "Opening hours data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the onCancelled event if necessary
            }
        });
    }

    private void showStartTimer() {
        int openingHour = convertTo24HourFormat(startTime);
        int openingMinute = Integer.parseInt(startTime.split(":")[1].split(" ")[0]);

        int closingHour = convertTo24HourFormat(endTime);
        int closingMinute = Integer.parseInt(endTime.split(":")[1].split(" ")[0]);

        // Custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View customView = inflater.inflate(R.layout.dialog_custom_title, null);
        TextView dialogRealTitle = customView.findViewById(R.id.dialog_title);
        TextView dialogTitle = customView.findViewById(R.id.dialog_subtitle);
        ViewGroup timePickerContainer = customView.findViewById(R.id.time_picker_container);
        dialogRealTitle.setText("Select start time");

        String dialogTitleText = String.format(Locale.getDefault(), "Opening Hour: %s", openingHours);
        dialogTitle.setText(dialogTitleText);

        TimePicker timePicker = new TimePicker(new ContextThemeWrapper(context, R.style.TimePickerStyle));
        timePicker.setIs24HourView(false); // Set the desired time format

        timePickerContainer.addView(timePicker);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(customView)
                .setPositiveButton("OK", (dialogInterface, which) -> {
                    int selectedHour = timePicker.getCurrentHour(); // Retrieve the selected hour
                    int selectedMinute = timePicker.getCurrentMinute(); // Retrieve the selected minute

                    if (selectedHour > openingHour || (selectedHour == openingHour && selectedMinute >= openingMinute)) {
                        if (selectedHour < closingHour || (selectedHour == closingHour && selectedMinute <= closingMinute)) {
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
                            return; // Exit the method after setting the start time
                        }
                    }

                    // If the selected time is outside the opening and closing hour/minute range, show an error message
                    Toast.makeText(context, "Selected time is outside business hours", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showEndTimer() {
        int closingHour = convertTo24HourFormat(endTime);
        int closingMinute = Integer.parseInt(endTime.split(":")[1].split(" ")[0]);

        // Custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View customView = inflater.inflate(R.layout.dialog_custom_title, null);
        TextView dialogRealTitle = customView.findViewById(R.id.dialog_title);
        TextView dialogTitle = customView.findViewById(R.id.dialog_subtitle);
        ViewGroup timePickerContainer = customView.findViewById(R.id.time_picker_container);
        dialogRealTitle.setText("Select end time");

        String dialogTitleText = String.format(Locale.getDefault(), "Opening Hour: %s", openingHours);
        dialogTitle.setText(dialogTitleText);

        TimePicker timePicker = new TimePicker(new ContextThemeWrapper(context, R.style.TimePickerStyle));
        timePicker.setIs24HourView(false); // Set the desired time format

        timePickerContainer.addView(timePicker);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(customView)
                .setPositiveButton("OK", (dialogInterface, which) -> {
                    int selectedHour = timePicker.getCurrentHour(); // Retrieve the selected hour
                    int selectedMinute = timePicker.getCurrentMinute(); // Retrieve the selected minute

                    if (selectedHour > startHour || (selectedHour == startHour && selectedMinute >= startMinute)) {
                        if (selectedHour < closingHour || (selectedHour == closingHour && selectedMinute <= closingMinute)) {
                            endHour = selectedHour;
                            endMinute = selectedMinute;

                            if (endHour < 12) {
                                endEt.setText(String.format(Locale.getDefault(), "%d:%02d AM", endHour, endMinute));
                            } else if (endHour == 12) {
                                endEt.setText(String.format(Locale.getDefault(), "12:%02d PM", endMinute));
                            } else {
                                endEt.setText(String.format(Locale.getDefault(), "%d:%02d PM", endHour - 12, endMinute));
                            }
                            return; // Exit the method after setting the start time
                        } else {
                            Toast.makeText(context, "Selected time is outside business hours", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Selected time cannot be earlier than start time", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private int convertTo24HourFormat(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        String amPm = parts[1].split(" ")[1];

        if (amPm.equalsIgnoreCase("PM") && hour != 12) {
            hour += 12;
        } else if (amPm.equalsIgnoreCase("AM") && hour == 12) {
            hour = 0;
        }

        return hour;
    }

    private void validateData(EditText dateEt, EditText startTimeEt, EditText endTimeEt) {
        date = dateEt.getText().toString().trim();
        startTime = startTimeEt.getText().toString().trim();
        endTime = endTimeEt.getText().toString().trim();
        boolean allFieldsFilled = true;

        if (TextUtils.isEmpty(date)) {
            dateEt.setError(context.getString(R.string.choose_date));
            allFieldsFilled = false;
        } else {
            dateEt.setError(null);
        }

        if (TextUtils.isEmpty(startTime)) {
            startTimeEt.setError(context.getString(R.string.choose_start));
            allFieldsFilled = false;
        } else {
            startTimeEt.setError(null);
        }

        if (TextUtils.isEmpty(endTime)) {
            endTimeEt.setError(context.getString(R.string.choose_end));
            allFieldsFilled = false;
        } else {
            endTimeEt.setError(null);
        }

        if (allFieldsFilled) {
            updateItinerary(date, startTime, endTime);
            Toast.makeText(context, "itinerary updated", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateItinerary(String date, String startTime, String endTime) {
        String uid = FirebaseAuth.getInstance().getUid();
        HashMap<String,Object> hashMap =new HashMap<>();
        hashMap.put("date",""+date);
        hashMap.put("startTime",""+startTime);
        hashMap.put("endTime",""+endTime);
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
        reference.child(uid).child("itinerary").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itinerarySnapshot : snapshot.getChildren()) {
                    String destiId = itinerarySnapshot.child("destiId").getValue(String.class);
                    if (destiId.equals(destiID)) {
                        DatabaseReference itineraryRef = itinerarySnapshot.getRef();
                        itineraryRef.child("date").setValue(date);
                        itineraryRef.child("startTime").setValue(startTime);
                        itineraryRef.child("endTime").setValue(endTime)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Itinerary updated successfully", Toast.LENGTH_LONG).show();
                                    context.startActivity(new Intent(context, Home.class));

                                }).addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to update itinerary: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private String getMyLocation(){
    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions((Activity) context,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MAPS_PERMIT);
        return null;
    } else {
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            return latitude + "," + longitude;
        } else {
            return null;
        }
    }
    }

    public interface OnDataChangedListener {
        void onDataChanged();
    }

    private OnDataChangedListener mListener;

    public void setOnDataChangedListener(OnDataChangedListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return itineraryList.size();
    }

    private void loadImage(Itinerary itinerary, ItineraryViewHolder holder) {
        String imageUrl = itinerary.getUrl();
        RequestOptions requestOptions = new RequestOptions()
                .transforms(new CenterCrop(), new AlphaTransformation(1f));

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .apply(requestOptions)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        holder.itineraryBg.setBackground(resource);
                        holder.isImageLoaded = true;
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    public static class ItineraryViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout itineraryBg;
        public TextView startTimeTextView;
        public TextView endTimeTextView;
        public TextView placeNameTextView;
        public TextView distanceTextView;
        public TextView durationTextView;
        public ImageButton optionBtn;
        public boolean isImageLoaded;

        public ItineraryViewHolder(View itemView) {
            super(itemView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            endTimeTextView = itemView.findViewById(R.id.endTimeTextView);
            placeNameTextView = itemView.findViewById(R.id.placeNameTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            optionBtn = itemView.findViewById(R.id.iter_option);
            itineraryBg = itemView.findViewById(R.id.itinerary_bg);
            isImageLoaded = false;
        }
    }

}
