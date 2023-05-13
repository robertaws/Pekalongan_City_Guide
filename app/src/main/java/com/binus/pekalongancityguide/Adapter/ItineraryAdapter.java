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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Itinerary;
import com.binus.pekalongancityguide.Layout.ItineraryList;
import com.binus.pekalongancityguide.Misc.AlphaTransformation;
import com.binus.pekalongancityguide.Misc.MyApplication;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.DialogAddToItineraryBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.ItineraryViewHolder>{
    private static final int MAPS_PERMIT= 1;
    private final Context context;
    private final List<Itinerary> itineraryList;
    private final FragmentManager fragmentManager;
    String destiID;

    private int startHour,startMinute,startHour1,startMinute1
            ,endHour,endMinute,endHour1,endMinute1
            ,dayDate,monthDate,yearDate,dayDate1,monthDate1,yearDate1;

    public ItineraryAdapter(Context context, List<Itinerary> itineraryList, FragmentManager fragmentManager) {
        this.context = context;
        this.itineraryList = itineraryList;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public ItineraryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_itinerary, parent, false);
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
        DialogAddToItineraryBinding addToItineraryBinding = DialogAddToItineraryBinding.inflate(inflater);
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
            dialog.show(fragmentManager, "startTimePicker");
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
            dialog.show(fragmentManager, "startTimePicker");
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
                    endEt.setText(String.format(Locale.getDefault(), "%d:%02d am", endHour, endMinute));
                } else if (endHour == 12) {
                    endEt.setText(String.format(Locale.getDefault(), "12:%02d pm", endMinute));
                } else {
                    endEt.setText(String.format(Locale.getDefault(), "%d:%02d pm", endHour - 12, endMinute));
                }
            });
            dialog.show(fragmentManager, "startTimePicker");
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
                    endEt.setText(String.format(Locale.getDefault(), "%d:%02d am", endHour1, endMinute1));
                } else if (endHour1 == 12) {
                    endEt.setText(String.format(Locale.getDefault(), "12:%02d pm", endMinute1));
                } else {
                    endEt.setText(String.format(Locale.getDefault(), "%d:%02d pm", endHour1 - 12, endMinute1));
                }
            });
            dialog.show(fragmentManager, "startTimePicker");
        });
        dateBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            yearDate = calendar.get(Calendar.YEAR);
            monthDate = calendar.get(Calendar.MONTH);
            dayDate = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog;
            dialog = new DatePickerDialog(context, (dateView, year, month, dayOfMonth) -> {
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
            dialog = new DatePickerDialog(context, (dateView, year, month, dayOfMonth) -> {
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
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();
        addItinerary.setOnClickListener(v -> {
            validateData(dateEt, startEt, endEt);
            dialog.dismiss();
        });
    }

    private String date = "", startTime = "", endTime = "";

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
                                    if (fragmentManager != null) {
                                        ItineraryList itineraryList = (ItineraryList) fragmentManager.findFragmentByTag("itineraryList");
                                        if (itineraryList != null) {
                                            itineraryList.updateItineraryView();
                                        }
                                    }

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
