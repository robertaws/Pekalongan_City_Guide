package com.binus.pekalongancityguide.Layout;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.Adapter.DestinationAdapter;
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

public class AddItinerary extends Fragment {
    private String categoryId;
    private String category;
    private DestinationAdapter destinationAdapter;
    private boolean isLoading = false;
    private RecyclerView iterRV;
    private int limit = 30;
    private ArrayList<Destination> destinationArrayList;
    public AddItinerary() {}

    public static AddItinerary newInstance(String categoryId, String category, String uid){
        AddItinerary fragment = new AddItinerary();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_itinerary, container, false);
        iterRV = view.findViewById(R.id.recycler_view);
        EditText iterSearch = view.findViewById(R.id.search_iter);
        iterRV.addOnScrollListener(new EndlessScrollListener());
        iterSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    destinationAdapter.getFilter().filter(s);
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

        return view;
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
                if (destinationAdapter == null) {
                    destinationAdapter = new DestinationAdapter(getContext(), destinationArrayList);
                    iterRV.setAdapter(destinationAdapter);
                } else {
                    destinationAdapter.notifyDataSetChanged();
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
                        if (destinationAdapter == null) {
                            destinationAdapter = new DestinationAdapter(getContext(), destinationArrayList);
                            iterRV.setAdapter(destinationAdapter);
                        } else {
                            destinationAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sortDestination(ArrayList<Destination> destinationArrayList){
        Collections.sort(destinationArrayList, (destination1, destination2) -> {
            String title1 = destination1.getTitle().toLowerCase();
            String title2 = destination2.getTitle().toLowerCase();
            return title1.compareTo(title2);
        });
    }

    private class EndlessScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

            if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0 && totalItemCount >= 30) {
                loadMoreItems();
            }
        }
    }

    private void loadMoreItems() {
        isLoading = true;
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
        reference.orderByKey().startAt(String.valueOf(limit)).limitToFirst(30).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Destination destination = dataSnapshot.getValue(Destination.class);
                    destinationArrayList.add(destination);
                }
                destinationAdapter.notifyDataSetChanged();
                limit += 30;
                isLoading = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error: " + error.getMessage());
            }
        });
    }
}