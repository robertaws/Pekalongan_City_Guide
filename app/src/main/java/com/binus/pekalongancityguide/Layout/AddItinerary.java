package com.binus.pekalongancityguide.Layout;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.binus.pekalongancityguide.Adapter.IterAdapter;
import com.binus.pekalongancityguide.Misc.ToastUtils;
import com.binus.pekalongancityguide.Model.Destination;
import com.binus.pekalongancityguide.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static com.binus.pekalongancityguide.BuildConfig.MAPS_API_KEY;
import static com.binus.pekalongancityguide.Misc.Constants.FIREBASE_DATABASE_URL;

public class AddItinerary extends Fragment implements IterAdapter.OnItemLongClickListener {
    private String categoryId, category, startDate, endDate, openingHours, startTime, endTime, selectedItemName, title, subtitle, placeDate, selectedItemId, placeCategory;
    private int startHour, startMinute, endHour, endMinute, selectedItemsInitialSize, startYear, startMonth, startDay, dialogCount, selectedItemIndex;
    private double latitude, longitude;
    private long startDateMillis, endDateMillis;
    private boolean cardViewSelected = false;
    public IterAdapter iterAdapter;
    private RecyclerView iterRV;
    private Button addIter;
    private CardView cardView;
    private SimpleDateFormat format;
    private EditText startEt, endEt, dateEt;
    private ImageButton startBtn, endBtn, dateBtn;
    private RelativeLayout selectLayout;
    private LinearLayout containerLayout;
    private TextView selectTv;
    private ImageButton selectCancel;
    public ArrayList<Destination> destinationArrayList, selectedItems;
    private final List<String> openHours = new ArrayList<>();
    private final List<String> closeHours = new ArrayList<>();
    private View view;
    private Calendar calendar;
    private AlertDialog dialog;
    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private ItineraryPager itineraryPager;


    public AddItinerary() {
    }

    public static AddItinerary newInstance(String categoryId, String categoryName, String categoryUid, String startDate, String endDate, Double latitude, Double longitude) {
        AddItinerary fragment = new AddItinerary();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", categoryName);
        args.putString("uid", categoryUid);
        args.putString("startDate", startDate);
        args.putString("endDate", endDate);
        args.putDouble("latitude", latitude);
        args.putDouble("longitude", longitude);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddItinerary getInstance() {
        return new AddItinerary();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL);
        ToastUtils.setToastEnabled(true);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            startDate = getArguments().getString("startDate");
            endDate = getArguments().getString("endDate");
            latitude = getArguments().getDouble("latitude");
            longitude = getArguments().getDouble("longitude");

            Log.d(TAG, "newInstance: categoryId=" + categoryId + ", startDate=" + startDate + ", endDate=" + endDate + "\n latitude, longitude" + latitude + longitude);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_itinerary, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        init();
        checkSelect();
        EditText iterSearch = view.findViewById(R.id.search_iter);
        iterSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    iterAdapter.getFilter().filter(s);
                }catch (Exception e){
                    Log.d(TAG,"onTextChanged :"+e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        if (category.equals("All")) {
            loadDestinations();
        } else {
            loadCategoriedDestination();
        }
        addIter.setOnClickListener(v -> showInputDialog());
        selectCancel.setOnClickListener(v -> iterAdapter.exitSelectMode());
        return view;
    }

    private void showInputDialog() {
        ToastUtils.setToastEnabled(true);
        selectedItems = iterAdapter.getSelectedItems();
        selectedItemsInitialSize = selectedItems.size();
        dialogCount++;
        if (dialogCount == 1) {
            title = getString(R.string.pick_start);
            subtitle = getString(R.string.below_are);
        } else {
            title = getString(R.string.pick_next);
            subtitle = getString(R.string.place_near_to) + selectedItemName;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_input_details, null);
        builder.setView(view);

        containerLayout = view.findViewById(R.id.container_layout);
        RelativeLayout timePickerLayout = view.findViewById(R.id.time_picker_container);

        containerLayout.setVisibility(View.VISIBLE);
        timePickerLayout.setVisibility(View.GONE);

        Button addBtn = view.findViewById(R.id.add_iter_button);
        TextView titleText = view.findViewById(R.id.dialog_title);
        TextView subtitleText = view.findViewById(R.id.dialog_subtitle);

        RelativeLayout.LayoutParams containerLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        containerLayoutParams.addRule(RelativeLayout.BELOW, subtitleText.getId());
        containerLayout.setLayoutParams(containerLayoutParams);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.BELOW, containerLayout.getId());
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        addBtn.setLayoutParams(layoutParams);

        addBtn.setText(R.string.next_txt);
        titleText.setText(title);
        subtitleText.setText(subtitle);

        Collections.sort(selectedItems, (d1, d2) -> {
            double distance1 = calculateDistance(latitude, longitude, Double.parseDouble(d1.getLatitude()), Double.parseDouble(d1.getLongitude()));
            double distance2 = calculateDistance(latitude, longitude, Double.parseDouble(d2.getLatitude()), Double.parseDouble(d2.getLongitude()));
            return Double.compare(distance1, distance2);
        });

        int maxItems = Math.min(selectedItems.size(), 3);
        for (int i = 0; i < maxItems; i++) {
            Destination destination = selectedItems.get(i);
            View itemView = createItemView(destination);
            containerLayout.addView(itemView);
        }

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialog1 -> cardViewSelected = false);
        addBtn.setOnClickListener(v -> {
            if (cardViewSelected) {
                dialog.dismiss();
                showTimePickerDialog();
            } else {
                ToastUtils.showToast(getContext(), getString(R.string.pick_aPlace), Toast.LENGTH_SHORT);
            }
        });

        dialog.show();
    }

    private View createItemView(Destination destination) {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_input_details, null);

        CardView cardView = itemView.findViewById(R.id.add_item_cardview);
        LinearLayout layoutBG = itemView.findViewById(R.id.layout_bg);
        TextView placeText = itemView.findViewById(R.id.placeNameTextView);
        TextView distanceTv = itemView.findViewById(R.id.distanceTextView);
        TextView durationTv = itemView.findViewById(R.id.durationTextView);

        double destinationLatitude = Double.parseDouble(destination.getLatitude());
        double destinationLongitude = Double.parseDouble(destination.getLongitude());
        placeText.setText(destination.getTitle());
        float distance = calculateDistance(latitude, longitude, destinationLatitude, destinationLongitude);
        String distanceString = (distance < 1) ? ((int) (distance * 1000)) + " m" : String.format(Locale.getDefault(), "%.2f km", distance);
        distanceTv.setText(distanceString);

        calculateDuration(latitude, longitude, destinationLatitude, destinationLongitude, (durationText, durationTextView) -> durationTextView.setText(durationText), durationTv);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 10, 0, 10);
        cardView.setLayoutParams(layoutParams);

        loadImage(destination, layoutBG);

        cardView.setOnClickListener(v -> {
            for (int i = 0; i < containerLayout.getChildCount(); i++) {
                View childView = containerLayout.getChildAt(i);
                unselectItem(childView);
            }
            selectedItemIndex = containerLayout.indexOfChild(itemView);
            selectedItemId = selectItem(itemView, destination.getId());
            latitude = Double.parseDouble(destination.getLatitude());
            longitude = Double.parseDouble(destination.getLongitude());
            placeCategory = destination.getCategoryId();
        });

        return itemView;
    }

    private String selectItem(View itemView, String id) {
        cardView = itemView.findViewById(R.id.add_item_cardview);
        LinearLayout layoutBG = itemView.findViewById(R.id.layout_bg);
        TextView placeText = itemView.findViewById(R.id.placeNameTextView);
        selectedItemName = placeText.getText().toString();

        cardViewSelected = true;
        cardView.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.selected_item_background));
        layoutBG.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.grayishTint));
        return id;
    }

    private void unselectItem(View itemView) {
        cardView = itemView.findViewById(R.id.add_item_cardview);
        LinearLayout layoutBG = itemView.findViewById(R.id.layout_bg);

        cardViewSelected = false;
        cardView.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.unselected_item_background));
        layoutBG.setBackgroundTintList(null);
    }

    private void handleSelectedItem(ArrayList<Destination> selectedItems) {
        if (selectedItems.size() > 0) {
            showInputDialog();
        } else {
            iterAdapter.exitSelectMode();
        }
    }

    private void showTimePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_input_details, null);
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

        if (startDate.equals(endDate)) {
            dateEt.setText(startDate);
            dateEt.setEnabled(false);
            dateBtn.setEnabled(false);
            startBtn.setEnabled(true);
            startEt.setEnabled(true);
            getDateOfWeek(selectedItems.get(selectedItemIndex));
        } else {
            startBtn.setEnabled(false);
            startEt.setEnabled(false);
            endBtn.setEnabled(false);
            endEt.setEnabled(false);
        }

        dateBtn.setOnClickListener(v -> showCalendar());
        dateEt.setOnClickListener(v -> showCalendar());
        startBtn.setOnClickListener(v -> showStartTimer());
        startEt.setOnClickListener(v -> showStartTimer());
        endBtn.setOnClickListener(v -> showEndTimer());
        endEt.setOnClickListener(v -> showEndTimer());

        if (placeCategory.equals("1680077442322")) {
            subtitle = getString(R.string.thirty2Hour);
        } else if (placeCategory.equals("1680077753090") || placeCategory.equals("1681812366209")) {
            subtitle = getString(R.string.thirty_oneHour);
        } else if (placeCategory.equals("1680166171816") || placeCategory.equals("1681807033132")) {
            subtitle = getString(R.string.depend_onNeeds);
        } else if (placeCategory.equals("1680167384847") || placeCategory.equals("1681811119599")) {
            subtitle = getString(R.string.one_two_hour);
        } else if (placeCategory.equals("1682060935295") || placeCategory.equals("1680077486439")) {
            subtitle = getString(R.string.based_on);
        } else if (placeCategory.equals("1682061580514")) {
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

    private void getDateOfWeek(Destination destination) {
        try {
            Date startDateObj = format.parse(startDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDateObj);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            getOpeningHours(dayOfWeek, destination);
            openHours.clear();
            closeHours.clear();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void getOpeningHours(int dayOfWeek, Destination destination) {
        DatabaseReference openingHoursRef = database.getReference("Destination");
        selectedItemId = destination.getId();
        Log.d(TAG, "SELECTED ITEM ID: " + selectedItemId);
        openingHoursRef.child(selectedItemId).child("openingHours").addListenerForSingleValueEvent(new ValueEventListener() {
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
                                ToastUtils.showToast(getContext(), getString(R.string.justnotAvail), Toast.LENGTH_SHORT);
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
                                        ToastUtils.showToast(getContext(), getString(R.string.invalidTimeSlot) + slot, Toast.LENGTH_SHORT);
                                    }
                                }
                            }
                        } else {
                            ToastUtils.showToast(getContext(), getString(R.string.invalid_opening_format), Toast.LENGTH_SHORT);
                        }
                    } else {
                        startTime = "";
                        endTime = "";
                        openingHours = "Closed";
                        ToastUtils.showToast(getContext(),getString(R.string.justnotAvail), Toast.LENGTH_SHORT);
                        openHours.add(startTime);
                        closeHours.add(endTime);
                    }
                    Log.d(TAG, "Open hour: " + openHours);
                    Log.d(TAG, "Close hour: " + closeHours);
                } else {
                    startTime = "12:00 AM";
                    endTime = "11:59 PM";
                    openingHours = "Not Found";
                    ToastUtils.showToast(getContext(), getString(R.string.data_not_found), Toast.LENGTH_SHORT);
                    new Handler().postDelayed(() -> {
                        ToastUtils.showToast(getContext(), getString(R.string.allowAnyTime), Toast.LENGTH_SHORT);
                    }, 2000);
                    openHours.add(startTime);
                    closeHours.add(endTime);
                }
                Log.d(TAG, destination.getTitle() + " OPENING HOURS: " + openingHours);
                destination.setOpen(openingHours == null || !openingHours.equals("Closed"));
                Log.d(TAG, "PLACE NAME: " + destination.getTitle() + " DESTI ID: " + destination.getId() + " IS OPEN: " + destination.isOpen() + " OPENING HOURS: " + openingHours);
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

        DatePickerDialog dialog = new DatePickerDialog(getContext(), (dateView, year, month, dayOfMonth) -> {
            startYear = year;
            startMonth = month;
            startDay = dayOfMonth;
            calendar.set(startYear, startMonth, startDay);

            placeDate = format.format(calendar.getTime());
            Log.d(TAG, "showCalendar: " + placeDate);

            dateEt.setText(placeDate);
            startBtn.setEnabled(true);
            startEt.setEnabled(true);

            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            getOpeningHours(dayOfWeek, selectedItems.get(selectedItemIndex));
        }, startYear, startMonth, startDay);

        try {
            Date startDateDate = format.parse(startDate);
            startDateMillis = startDateDate.getTime();
            Date endDateDate = format.parse(endDate);
            endDateMillis = endDateDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        dialog.getDatePicker().setMinDate(startDateMillis);
        dialog.getDatePicker().setMaxDate(endDateMillis);

        dialog.getWindow().setBackgroundDrawableResource(R.color.palette_4);
        dialog.show();
    }

    private void showStartTimer() {
        startEt.setText("");
        if (openHours.isEmpty()) {
            ToastUtils.showToast(getContext(),getString(R.string.data_not_found), Toast.LENGTH_SHORT);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.dialog_custom_title, null);
        TextView dialogRealTitle = customView.findViewById(R.id.dialog_title);
        TextView dialogTitle = customView.findViewById(R.id.dialog_subtitle);
        ViewGroup timePickerContainer = customView.findViewById(R.id.time_picker_container);
        dialogRealTitle.setText(R.string.select_start_time_iter);

        String dialogTitleText = String.format(Locale.getDefault(), "Opening Hour: %s", openingHours);
        dialogTitle.setText(dialogTitleText);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(customView)
                .setNegativeButton(R.string.cancel_opt, null);

        TimePicker timePicker = new TimePicker(new ContextThemeWrapper(getContext(), R.style.TimePickerStyle));
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
                    ToastUtils.showToast(getContext(), getString(R.string.data_not_found), Toast.LENGTH_SHORT);
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
                ToastUtils.showToast(getContext(), getString(R.string.outside_business), Toast.LENGTH_SHORT);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showEndTimer() {
        endEt.setText("");
        if (openHours.isEmpty()) {
            ToastUtils.showToast(getContext(),getString(R.string.data_not_found), Toast.LENGTH_SHORT);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.dialog_custom_title, null);
        TextView dialogRealTitle = customView.findViewById(R.id.dialog_title);
        TextView dialogTitle = customView.findViewById(R.id.dialog_subtitle);
        ViewGroup timePickerContainer = customView.findViewById(R.id.time_picker_container);
        dialogRealTitle.setText(R.string.select_end_time_iter);

        String dialogTitleText = String.format(Locale.getDefault(), "Opening Hour: %s", openingHours);
        dialogTitle.setText(dialogTitleText);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(customView)
                .setNegativeButton(R.string.cancel_opt, null);

        TimePicker timePicker = new TimePicker(new ContextThemeWrapper(getContext(), R.style.TimePickerStyle));
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
                        errorMessage = "End time can't be earlier than the start time.";
                    } else {
                        errorMessage = getString(R.string.outside_business);
                    }
                    break;
                } else {
                    withinOpeningHours = true;
                }
            }

            if (errorMessage != null) {
                ToastUtils.showToast(getContext(), errorMessage, Toast.LENGTH_SHORT);
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
                    ToastUtils.showToast(getContext(), getString(R.string.outside_business), Toast.LENGTH_SHORT);
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

    private void loadImage(Destination destination, LinearLayout layoutBG) {
        String imageUrl = destination.getUrl();
        Glide.with(layoutBG.getContext())
                .asBitmap()
                .load(imageUrl)
                .placeholder(R.drawable.logo)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        BitmapDrawable drawable = new BitmapDrawable(layoutBG.getResources(), resource);
                        layoutBG.setBackground(drawable);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        Log.d(TAG, "on Failure: failed to get file from URL");
                    }
                });
    }

    private void init() {
        calendar = Calendar.getInstance();
        iterRV = view.findViewById(R.id.recycler_view);
        addIter = view.findViewById(R.id.add_iter_btn);
        selectTv = view.findViewById(R.id.select_tv);
        selectLayout = view.findViewById(R.id.select_layout);
        selectCancel = view.findViewById(R.id.select_cancel);
        format = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
        dialogCount = 0;
    }

    private void loadDestinations() {
        destinationArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Destination");
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                destinationArrayList.clear();
                openHours.clear();
                closeHours.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Destination destination = dataSnapshot.getValue(Destination.class);
                    destination.setOpen(true);
                    if (startDate.equals(endDate)) {
                        getDateOfWeek(destination);
                        ToastUtils.setToastEnabled(false);
                    }
                    destinationArrayList.add(destination);
                    sortDestination(destinationArrayList);
                }
                if (iterAdapter == null) {
                    initIterAdapter();
                } else {
                    iterAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error: " + error.getMessage());
            }
        });
    }

    private void loadCategoriedDestination(){
        destinationArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Destination");
        reference.keepSynced(true);
        reference.orderByChild("categoryId").equalTo(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        destinationArrayList.clear();
                        openHours.clear();
                        closeHours.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Destination destination = dataSnapshot.getValue(Destination.class);
                            if (startDate.equals(endDate)) {
                                getDateOfWeek(destination);
                                ToastUtils.setToastEnabled(false);
                            }
                            destinationArrayList.add(destination);
                            sortDestination(destinationArrayList);
                            openHours.clear();
                            closeHours.clear();
                        }
                        if (iterAdapter == null) {
                            initIterAdapter();
                        } else {
                            iterAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sortDestination(ArrayList<Destination> destinationArrayList) {
        Collections.sort(destinationArrayList, (destination1, destination2) -> {
            String title1 = destination1.getTitle().toLowerCase();
            String title2 = destination2.getTitle().toLowerCase();
            return title1.compareTo(title2);
        });
    }

    public void checkSelect() {
        if (iterAdapter != null) {
            selectedItems = iterAdapter.getSelectedItems();
            int counter;
            if (selectedItems.size() < 1) {
                selectLayout.setVisibility(View.GONE);
                addIter.setVisibility(View.INVISIBLE);
            } else if (selectedItems.size() == 1) {
                counter = selectedItems.size();
                selectTv.setText(counter + getString(R.string.item_selected));
                addIter.setText(getString(R.string.add_to_itinerary_btn));
                addIter.setVisibility(View.VISIBLE);
                selectLayout.setVisibility(View.VISIBLE);
            } else {
                counter = selectedItems.size();
                selectTv.setText(counter + getString(R.string.items_selected));
                addIter.setText(getString(R.string.add_to_itinerary_btn));
                addIter.setVisibility(View.VISIBLE);
                selectLayout.setVisibility(View.VISIBLE);
            }
        } else {
            addIter.setVisibility(View.INVISIBLE);
            selectLayout.setVisibility(View.GONE);
        }
    }

    private void validateData(EditText dateEt, EditText startTimeEt, EditText endTimeEt) {
        placeDate = dateEt.getText().toString().trim();
        startTime = startTimeEt.getText().toString().trim();
        endTime = endTimeEt.getText().toString().trim();
        boolean allFieldsFilled = true;

        if (TextUtils.isEmpty(placeDate)) {
            dateEt.setError(getContext().getString(R.string.choose_date));
            allFieldsFilled = false;
        } else {
            dateEt.setError(null);
        }

        if (TextUtils.isEmpty(startTime)) {
            startTimeEt.setError(getContext().getString(R.string.choose_start));
            allFieldsFilled = false;
        } else {
            startTimeEt.setError(null);
        }

        if (TextUtils.isEmpty(endTime)) {
            endTimeEt.setError(getContext().getString(R.string.choose_end));
            allFieldsFilled = false;
        } else {
            endTimeEt.setError(null);
        }

        if (allFieldsFilled) {
            dialog.dismiss();
            uploadToDB(placeDate, startTime, endTime);
            ToastUtils.showToast(getContext(), getString(R.string.itinerary_updated), Toast.LENGTH_SHORT);
        }
    }

    private void uploadToDB(String date, String startTime, String endTime) {
        String uid = firebaseAuth.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL)
                .getReference("Destination");
        reference.child(selectedItemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String placeID = "" + snapshot.child("placeId").getValue();
                Log.d(TAG, "placeID: " + placeID);
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("startTime", startTime);
                hashMap.put("endTime", endTime);
                hashMap.put("date", date);
                hashMap.put("destiId", selectedItemId);
                DatabaseReference itineraryRef = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL)
                        .getReference("Users")
                        .child(uid)
                        .child("itinerary");

                String itineraryId = itineraryRef.push().getKey();

                hashMap.put("itineraryId", itineraryId);

                itineraryRef.child(itineraryId).setValue(hashMap)
                        .addOnSuccessListener(aVoid -> {
                            selectedItems.remove(selectedItemIndex);
                            handleSelectedItem(selectedItems);
                            ToastUtils.showToast(getContext(), getString(R.string.iterUploadSuccess), Toast.LENGTH_LONG);
                        })
                        .addOnFailureListener(e -> {
                            ToastUtils.showToast(getContext(), getString(R.string.failIterUpload) + e.getMessage(), Toast.LENGTH_SHORT);
                            Log.d(TAG, "on Failure: " + e.getMessage());
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void initIterAdapter() {
        iterAdapter = new IterAdapter(getContext(), destinationArrayList, this, this, itineraryPager);
        iterRV.setAdapter(iterAdapter);
    }

    @Override
    public void onItemLongClick(Destination destination) {
    }

    private float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location location1 = new Location("");
        location1.setLatitude(lat1);
        location1.setLongitude(lon1);
        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(lon2);
        Location.distanceBetween(location1.getLatitude(), location1.getLongitude(),
                location2.getLatitude(), location2.getLongitude(), results);
        return results[0] / 1000;
    }

    private void calculateDuration(double lat1, double lon1, double lat2, double lon2, AddItinerary.DurationCallback callback, TextView durationTv) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + lat1 + "," + lon1 + "&destination=" + lat2 + "," + lon2 + "&key=" + MAPS_API_KEY;

        if (isAdded() && getContext() != null) {
            RequestQueue queue = Volley.newRequestQueue(getContext().getApplicationContext());
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
                try {
                    JSONArray routes = response.getJSONArray("routes");
                    if (routes.length() > 0) {
                        JSONObject route = routes.getJSONObject(0);
                        JSONArray legs = route.getJSONArray("legs");
                        JSONObject leg = legs.getJSONObject(0);
                        JSONObject duration = leg.getJSONObject("duration");
                        String durationText = duration.getString("text");
                        Log.d(TAG, "Duration: " + durationText);
                        callback.onDurationReceived(durationText, durationTv);
                    } else {
                        Log.e(TAG, "No routes found");
                        callback.onDurationReceived("No routes found", durationTv);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
                Log.e(TAG, "Error calculating travel duration: " + error.getMessage());
                callback.onDurationReceived("Error calculating travel duration", durationTv);
            });
            queue.add(request);
        }
    }

    public interface DurationCallback {
        void onDurationReceived(String durationText, TextView durationTv);
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}