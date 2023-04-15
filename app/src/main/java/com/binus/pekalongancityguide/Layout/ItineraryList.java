package com.binus.pekalongancityguide.Layout;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.binus.pekalongancityguide.ItemTemplate.Itinerary;
import com.binus.pekalongancityguide.databinding.FragmentItineraryListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItineraryList extends Fragment {
    public FragmentItineraryListBinding binding;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");
    private FirebaseAuth firebaseAuth;
    public ArrayList<Itinerary> itineraryArrayList;
    public com.binus.pekalongancityguide.Layout.ItineraryList.ViewPagerAdapter viewPagerAdapter;
    private static final String TAG = "DESTI_USER_TAG";

    public ItineraryList() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentItineraryListBinding.inflate(inflater, container, false);
        setupViewPagerAdapter(binding.viewPager);
        binding.itineraryTab.setupWithViewPager(binding.viewPager);
        return binding.getRoot();
    }

    private void setupViewPagerAdapter(ViewPager viewPager) {
        viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), getContext());
        itineraryArrayList = new ArrayList<>();
        DatabaseReference reference = database.getReference().child(firebaseAuth.getUid());
        Query itineraryQuery = reference.child("itinerary");
        itineraryQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itineraryArrayList.clear();
                viewPagerAdapter.notifyDataSetChanged();

                // Step 1: Create a HashSet to store all the unique dates in the database
                Set<String> uniqueDates = new HashSet<>();

                // Step 2: Create a HashMap to map each date to its corresponding Itinerary objects
                Map<String, List<Itinerary>> itinerariesByDate = new HashMap<>();

                // Step 3: Loop through all the Itinerary objects in the database and add them to the HashMap
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Itinerary itinerary = dataSnapshot.getValue(Itinerary.class);
                    itineraryArrayList.add(itinerary);
                    String date = itinerary.getDate();
                    if (!itinerariesByDate.containsKey(date)) {
                        itinerariesByDate.put(date, new ArrayList<>());
                    }
                    itinerariesByDate.get(date).add(itinerary);
                    uniqueDates.add(date);
                }

                // Step 4: Loop through all the unique dates in the HashSet
                int dayCounter = 1;
                for (String date : uniqueDates) {
                    List<Itinerary> itinerariesOnThisDay = itinerariesByDate.get(date);

                    // Step 5: If the HashMap contains an Itinerary object for the current date, add it to the ViewPagerAdapter
                    if (itinerariesOnThisDay != null && !itinerariesOnThisDay.isEmpty()) {
                        viewPagerAdapter.addFragment(ItineraryFragment.newInstance(
                                "" + date,
                                "" + itinerariesOnThisDay.get(0).getUid()
                        ), "Day " + dayCounter);
                    }

                    // Step 6: If the HashMap does not contain an Itinerary object for the current date, add a blank Fragment to the ViewPagerAdapter
                    else {
                        viewPagerAdapter.addFragment(ItineraryFragment.newInstance(
                                "No itinerary on this day",
                                ""), "Day " + dayCounter);
                    }

                    dayCounter++;
                }

                viewPagerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(1);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private final ArrayList<ItineraryFragment> fragmentList = new ArrayList<>();
        private final ArrayList<String> fragmentTitleList = new ArrayList<>();
        private final Context context;

        public ViewPagerAdapter(FragmentManager fm, Context context) {
            super(fm, BEHAVIOR_SET_USER_VISIBLE_HINT);
            this.context = context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        private void addFragment(ItineraryFragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }
}