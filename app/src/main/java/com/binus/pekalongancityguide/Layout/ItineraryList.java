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
import com.binus.pekalongancityguide.databinding.ActivityItineraryListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ItineraryList extends Fragment {
    public ActivityItineraryListBinding binding;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");
    private FirebaseAuth firebaseAuth;
    public ArrayList<Itinerary> itineraryArrayList;
    public ItineraryList.ViewPagerAdapter viewPagerAdapter;
    private static final String TAG = "DESTI_USER_TAG";

    public ItineraryList() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityItineraryListBinding.inflate(inflater, container, false);
        setupViewPagerAdapter(binding.viewPager);
        binding.itineraryTab.setupWithViewPager(binding.viewPager);
        firebaseAuth = FirebaseAuth.getInstance();
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
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Itinerary itinerary = dataSnapshot.getValue(Itinerary.class);
                    itineraryArrayList.add(itinerary);
                    viewPagerAdapter.addFragment(ItineraryFragment.newInstance(
                            "" + itinerary.getDate(),
                            "" + itinerary.getUid()), itinerary.getDate()); //TODO change to DAY #
                    viewPagerAdapter.notifyDataSetChanged();
                }
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