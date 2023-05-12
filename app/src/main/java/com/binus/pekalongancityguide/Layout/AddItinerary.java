package com.binus.pekalongancityguide.Layout;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.Adapter.IterAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Destination;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.DialogChooseDateBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class AddItinerary extends Fragment implements IterAdapter.OnItemLongClickListener {
    private String categoryId, category, startDate, endDate;
    public IterAdapter iterAdapter;
    private RecyclerView iterRV;
    private Button addIter, addDate;
    private RelativeLayout selectLayout;
    private TextView selectTv;
    private ImageButton selectCancel, startBtn, endBtn;
    ;
    private EditText startEt, endEt;
    private Calendar calendar;
    private int startDay, startMonth, startYear, endDay, endMonth, endYear, counter;
    public ArrayList<Destination> destinationArrayList, selectedItems;
    private View view;
    private ItineraryPager itineraryPager;


    public AddItinerary() {
    }

    public static AddItinerary newInstance(String categoryId, String category, String uid) {
        AddItinerary fragment = new AddItinerary();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddItinerary getInstance() {
        return new AddItinerary();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_itinerary, container, false);
        init();
        showPickDateDialog();
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
        addIter.setOnClickListener(v -> {
            Log.d(TAG, "start: " + startDate + " end : " + endDate);
        });
        selectCancel.setOnClickListener(v -> iterAdapter.exitSelectMode());
        return view;
    }

    private void init() {
        iterRV = view.findViewById(R.id.recycler_view);
        addIter = view.findViewById(R.id.add_iter_btn);
        selectTv = view.findViewById(R.id.select_tv);
        selectLayout = view.findViewById(R.id.select_layout);
        selectCancel = view.findViewById(R.id.select_cancel);
    }

    private void showPickDateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        DialogChooseDateBinding chooseDateBinding = DialogChooseDateBinding.inflate(getLayoutInflater());
        builder.setView(chooseDateBinding.getRoot());
        startEt = chooseDateBinding.startDateEt;
        endEt = chooseDateBinding.endDateEt;
        startBtn = chooseDateBinding.startPickerBtn;
        endBtn = chooseDateBinding.endPickerBtn;
        addDate = chooseDateBinding.addDateBtn;

        calendar = Calendar.getInstance();

        startBtn.setOnClickListener(v -> showStartCalendar());

        startEt.setOnClickListener(v -> showStartCalendar());

        endBtn.setOnClickListener(v -> showEndCalendar());

        endEt.setOnClickListener(v -> showEndCalendar());
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();
        addDate.setOnClickListener(v -> dialog.dismiss());
    }

    private void showStartCalendar() {
        startYear = calendar.get(Calendar.YEAR);
        startMonth = calendar.get(Calendar.MONTH);
        startDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getContext(), (dateView, year, month, dayOfMonth) -> {
            startYear = year;
            startMonth = month;
            startDay = dayOfMonth;
            SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
            startDate = format.format(new Date(startYear - 1900, startMonth, startDay));
            startEt.setText(startDate);
        }, startYear, startMonth, startDay);

        dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        if (endDate != null) {
            SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
            try {
                Date endDateObj = format.parse(endDate);
                dialog.getDatePicker().setMaxDate(endDateObj.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        dialog.getWindow().setBackgroundDrawableResource(R.color.palette_4);
        dialog.show();
    }

    private void showEndCalendar() {
        endYear = calendar.get(Calendar.YEAR);
        endMonth = calendar.get(Calendar.MONTH);
        endDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getContext(), (dateView, year, month, dayOfMonth) -> {
            endYear = year;
            endMonth = month;
            endDay = dayOfMonth;
            SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
            endDate = format.format(new Date(endYear - 1900, endMonth, endDay));
            endEt.setText(endDate);
        }, endYear, endMonth, endDay);

        Calendar minDateCalendar = Calendar.getInstance();
        if (startDate != null) {
            SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
            try {
                Date startDateObj = format.parse(startDate);
                minDateCalendar.setTime(startDateObj);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            minDateCalendar.setTime(calendar.getTime());
        }
        if (minDateCalendar.after(calendar)) {
            dialog.getDatePicker().setMinDate(minDateCalendar.getTimeInMillis());
        } else {
            dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        }

        dialog.getWindow().setBackgroundDrawableResource(R.color.palette_4);
        dialog.show();
    }

    private void loadDestinations() {
        destinationArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                destinationArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Destination destination = dataSnapshot.getValue(Destination.class);
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
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
        reference.keepSynced(true);
        reference.orderByChild("categoryId").equalTo(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        destinationArrayList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Destination destination = dataSnapshot.getValue(Destination.class);
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
            if (selectedItems.size() < 1) {
                selectLayout.setVisibility(View.GONE);
                addIter.setVisibility(View.INVISIBLE);
            } else if (selectedItems.size() == 1) {
                counter = selectedItems.size();
                selectTv.setText(counter + " item selected");
                addIter.setText("Add to itinerary");
                addIter.setVisibility(View.VISIBLE);
                selectLayout.setVisibility(View.VISIBLE);
            } else {
                counter = selectedItems.size();
                selectTv.setText(counter + " items selected");
                addIter.setText("Add to itinerary");
                addIter.setVisibility(View.VISIBLE);
                selectLayout.setVisibility(View.VISIBLE);
            }
        } else {
            addIter.setVisibility(View.INVISIBLE);
            selectLayout.setVisibility(View.GONE);
        }
    }

    public void initIterAdapter() {
        iterAdapter = new IterAdapter(getContext(), destinationArrayList, this, this, itineraryPager); // Pass the reference to the fragment
        iterRV.setAdapter(iterAdapter);
    }

    @Override
    public void onItemLongClick(Destination destination) {
        checkSelect();
    }

}