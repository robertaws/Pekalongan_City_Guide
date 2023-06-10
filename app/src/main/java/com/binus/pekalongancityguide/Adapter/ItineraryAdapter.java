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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Itinerary;
import com.binus.pekalongancityguide.Layout.ItineraryList;
import com.binus.pekalongancityguide.Misc.AlphaTransformation;
import com.binus.pekalongancityguide.Misc.MyApplication;
import com.binus.pekalongancityguide.Misc.ToastUtils;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.ItineraryViewHolder> {
    private static final int MAPS_PERMIT = 1;
    private final Context context;
    private final List<Itinerary> itineraryList;
    private final List<String> openHours = new ArrayList<>();
    private final List<String> closeHours = new ArrayList<>();
    private final FragmentManager fragmentManager;
    private FirebaseDatabase database;
    private EditText startEt, endEt, dateEt;
    private ImageButton dateBtn, startBtn, endBtn;
    private Calendar calendar;
    private AlertDialog dialog;
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
    private void updateItinerary(String date, String startTime, String endTime) {
        String uid = FirebaseAuth.getInstance().getUid();
        HashMap<String,Object> hashMap =new HashMap<>();
        hashMap.put("date",""+date);
        hashMap.put("startTime",""+startTime);
        hashMap.put("endTime",""+endTime);
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
        reference.child(uid).child("itinerary").addListenerForSingleValueEvent(new ValueEventListener() {
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
                                    ItineraryList itineraryList1 = new ItineraryList();
                                    Toast.makeText(context, R.string.iterUpdateSuccess, Toast.LENGTH_LONG).show();
                                    AppCompatActivity appCompatActivity = (AppCompatActivity) context;
                                    FragmentManager fragmentManager = appCompatActivity.getSupportFragmentManager();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.replace(R.id.container,itineraryList1);
                                    fragmentTransaction.commit();
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(context,context.getString(R.string.iterFailUpdate) + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
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
                    if (which == 0) {
                        showEditDialog();
                    } else {
                        MyApplication.deleteIter(
                                context, "" + DESTIID, this, holder.getAdapterPosition()
                        );
                    }
                })
                .show();
    }

    private void showEditDialog() {
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
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();
        dialog.setOnDismissListener(dialog1 -> {
            openHours.clear();
            closeHours.clear();
        });
        addItinerary.setOnClickListener(v -> {
            validateData(dateEt, startEt, endEt);
        });
    }

    private void showCalendar() {
        dateEt.setText("");
        startYear = calendar.get(Calendar.YEAR);
        startMonth = calendar.get(Calendar.MONTH);
        startDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(context, (dateView, year, month, dayOfMonth) -> {
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

    private void getOpeningHours(int dayOfWeek) {
        DatabaseReference openingHoursRef = database.getReference("Destination");
        openingHoursRef.child(destiID).child("openingHours").addListenerForSingleValueEvent(new ValueEventListener() {
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
                                Toast.makeText(context,context.getString(R.string.justnotAvail), Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(context,R.string.invalidTimeSlot + slot, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(context, R.string.invalid_opening_format, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        startTime = "";
                        endTime = "";
                        openingHours = "Closed";
                        Toast.makeText(context,context.getString(R.string.justnotAvail), Toast.LENGTH_SHORT).show();
                        openHours.add(startTime);
                        closeHours.add(endTime);
                    }
                    Log.d(TAG, "Open hour: " + openHours);
                    Log.d(TAG, "Close hour: " + closeHours);
                } else {
                    startTime = "12:00 AM";
                    endTime = "11:59 PM";
                    openingHours = "Not Found";
                    Toast.makeText(context,context.getString(R.string.data_not_found), Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(() -> {
                        Toast.makeText(context,context.getString(R.string.allowAnyTime), Toast.LENGTH_SHORT).show();
                        openHours.add(startTime);
                        closeHours.add(endTime);
                    }, 2000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void showStartTimer() {
        startEt.setText("");
        if (openHours.isEmpty()) {
            Toast.makeText(context,context.getString(R.string.data_not_found), Toast.LENGTH_SHORT).show();
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        View customView = inflater.inflate(R.layout.dialog_custom_title, null);
        TextView dialogRealTitle = customView.findViewById(R.id.dialog_title);
        TextView dialogTitle = customView.findViewById(R.id.dialog_subtitle);
        ViewGroup timePickerContainer = customView.findViewById(R.id.time_picker_container);
        dialogRealTitle.setText(R.string.select_start_time_iter);

        String dialogTitleText = String.format(Locale.getDefault(), "Opening Hour: %s", openingHours);
        dialogTitle.setText(dialogTitleText);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(customView)
                .setNegativeButton(R.string.cancel_opt, null);

        TimePicker timePicker = new TimePicker(new ContextThemeWrapper(context, R.style.TimePickerStyle));
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
                    Toast.makeText(context,context.getString(R.string.data_not_found), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context,context.getString(R.string.outside_business), Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showEndTimer() {
        endEt.setText("");
        if (openHours.isEmpty()) {
            Toast.makeText(context,context.getString(R.string.data_not_found), Toast.LENGTH_SHORT).show();
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        View customView = inflater.inflate(R.layout.dialog_custom_title, null);
        TextView dialogRealTitle = customView.findViewById(R.id.dialog_title);
        TextView dialogTitle = customView.findViewById(R.id.dialog_subtitle);
        ViewGroup timePickerContainer = customView.findViewById(R.id.time_picker_container);
        dialogRealTitle.setText(R.string.select_end_time_iter);

        String dialogTitleText = String.format(Locale.getDefault(), "Opening Hour: %s", openingHours);
        dialogTitle.setText(dialogTitleText);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(customView)
                .setNegativeButton(R.string.cancel_opt, null);

        TimePicker timePicker = new TimePicker(new ContextThemeWrapper(context, R.style.TimePickerStyle));
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
                    errorMessage = context.getString(R.string.data_not_found);
                    break;
                }

                int closingHour = convertTo24HourFormat(closingTime);
                int closingMinute = Integer.parseInt(closingTime.split(":")[1].split(" ")[0]);

                if ((closingHour < startHour || (closingHour == startHour && closingMinute < startMinute))
                        || (selectedHour > closingHour || (selectedHour == closingHour && selectedMinute > closingMinute))
                        || (selectedHour < startHour || (selectedHour == startHour && selectedMinute < startMinute))) {
                    withinOpeningHours = false;
                    if (selectedHour < startHour || (selectedHour == startHour && selectedMinute < startMinute)) {
                        errorMessage = context.getString(R.string.end_time_earlier);
                    } else {
                        errorMessage = context.getString(R.string.outside_business);
                    }
                    break;
                } else {
                    withinOpeningHours = true;
                }
            }

            if (errorMessage != null) {
                ToastUtils.showToast(this.context, errorMessage, Toast.LENGTH_SHORT);
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
                    ToastUtils.showToast(this.context, context.getString(R.string.outside_business), Toast.LENGTH_SHORT);
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
            dialog.dismiss();
            updateItinerary(date, startTime, endTime);
            Toast.makeText(context, R.string.itinerary_updated, Toast.LENGTH_SHORT).show();
        }
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
