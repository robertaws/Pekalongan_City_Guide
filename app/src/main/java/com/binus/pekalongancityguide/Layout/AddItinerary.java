package com.binus.pekalongancityguide.Layout;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import static android.content.ContentValues.TAG;

public class AddItinerary extends Fragment implements IterAdapter.OnItemLongClickListener {
    private String categoryId, category, startDate, endDate, uid;
    public IterAdapter iterAdapter;
    private RecyclerView iterRV;
    private Button addIter;
    private RelativeLayout selectLayout;
    private TextView selectTv;
    private ImageButton selectCancel;
    public ArrayList<Destination> destinationArrayList, selectedItems;
    private View view;
    private ItineraryPager itineraryPager;


    public AddItinerary() {
    }

    public static AddItinerary newInstance(String categoryId, String categoryName, String categoryUid, String startDate, String endDate) {
        AddItinerary fragment = new AddItinerary();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", categoryName);
        args.putString("uid", categoryUid);
        args.putString("startDate", startDate);
        args.putString("endDate", endDate);
        Log.d(TAG, "newInstance: categoryId=" + categoryId + ", categoryName=" + categoryName + ", categoryUid=" + categoryUid + ", startDate=" + startDate + ", endDate=" + endDate);
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
            uid = getArguments().getString("uid");
            startDate = getArguments().getString("startDate");
            endDate = getArguments().getString("endDate");
            Log.d(TAG, "onCreate: categoryId=" + categoryId + ", category=" + category + ", uid=" + uid + ", startDate=" + startDate + ", endDate=" + endDate);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_itinerary, container, false);
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
            int counter;
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