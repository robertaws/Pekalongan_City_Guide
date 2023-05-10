package com.binus.pekalongancityguide.Layout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.binus.pekalongancityguide.Adapter.IterListAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Iter;
import com.binus.pekalongancityguide.databinding.FragmentItineraryListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ItineraryList extends Fragment {
    private FragmentItineraryListBinding binding;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef;
    private FirebaseDatabase database;
    private Query itineraryQuery;
    private String TAG = "ITERFRAG_TAG";

    public ItineraryList() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentItineraryListBinding.inflate(inflater, container, false);
        database = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");
        userRef = database.getReference("Users").child(Objects.requireNonNull(firebaseAuth.getUid()));
        userRef.keepSynced(true);
        itineraryQuery = userRef.child("itineraries");
        loadItinerary();
        sortItinerary();
        return binding.getRoot();
    }

    private void loadItinerary() {
        itineraryQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.iterEmptyTv.setVisibility(View.GONE);
                    List<Iter> itineraryList = new ArrayList<>();
                    for (DataSnapshot itinerarySnapshot : snapshot.getChildren()) {
                        String itineraryName = itinerarySnapshot.getKey();
                        Log.d(TAG, "iter name: " + itineraryName);
                        userRef.child("itineraries").child(itineraryName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String date = snapshot.child("date").getValue().toString();
                                String destiId = snapshot.child("destiId").getValue().toString();
                                database.getReference("Destination").child(destiId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String url = snapshot.child("url").getValue().toString();
                                        itineraryList.add(new Iter(itineraryName, date, destiId, url));
                                        IterListAdapter adapter = new IterListAdapter(getContext(), itineraryList);
                                        binding.iterListRv.setAdapter(adapter);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                } else {
                    binding.iterEmptyTv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sortItinerary() {

        itineraryQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashSet<String> uniqueDates = new HashSet<>();
                for (DataSnapshot itinerarySnapshot : snapshot.getChildren()) {
                    String date = itinerarySnapshot.child("date").getValue(String.class);
                    if (date != null && !date.isEmpty()) {
                        Log.d(TAG, "date: " + date);
                        date = convertWithoutDay(date);
                        uniqueDates.add(date);
                    }
                }

                List<String> dates = new ArrayList<>(uniqueDates);
                Collections.sort(dates, new Comparator<String>() {
                    DateFormat dateFormat = new SimpleDateFormat("dd MMMM", Locale.getDefault());

                    @Override
                    public int compare(String date1, String date2) {
                        try {
                            Date dateObj1 = dateFormat.parse(date1);
                            Date dateObj2 = dateFormat.parse(date2);
                            return dateObj1.compareTo(dateObj2);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private String convertWithoutDay(String dateStr) {
        try {
            DateFormat originalDateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
            Date date = originalDateFormat.parse(dateStr);
            DateFormat targetDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            return targetDateFormat.format(date);
        } catch (ParseException e) {
            Log.e("ItineraryList", "Error parsing date: " + dateStr, e);
            Toast.makeText(getContext(), "Error parsing date: " + dateStr, Toast.LENGTH_SHORT).show();
            return "";
        }
    }
}