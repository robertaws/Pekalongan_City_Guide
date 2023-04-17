package com.binus.pekalongancityguide.Layout;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.binus.pekalongancityguide.Layout.ItineraryFragment;
import com.binus.pekalongancityguide.R;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ItineraryList extends Fragment {
    private FragmentItineraryListBinding binding;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");
    private FirebaseAuth firebaseAuth;


    public ItineraryList() {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference userRef = database.getReference("Users").child(Objects.requireNonNull(firebaseAuth.getUid()));
        Query itineraryQuery = userRef.child("itinerary");
        itineraryQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> dates = new ArrayList<>();
                for (DataSnapshot itinerarySnapshot : snapshot.getChildren()) {
                    String date = itinerarySnapshot.child("date").getValue(String.class);
                    if (date != null && !date.isEmpty()) {
                        Log.d(TAG, "date: " + date);
                        date = convertToIso8601(date);
                        dates.add(date);
                    }
                }

                List<Fragment> fragments = createFragmentsList(dates);

                ItineraryPagerAdapter vpAdapter = new ItineraryPagerAdapter(getContext(), getActivity().getSupportFragmentManager(), fragments, dates);
                binding.viewPager.setAdapter(vpAdapter);
                binding.itineraryTab.setupWithViewPager(binding.viewPager);
                binding.itineraryTab.setSelectedTabIndicatorColor(ContextCompat.getColor(getContext(), R.color.white));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentItineraryListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        binding.backtoprofile.setOnClickListener(v -> {
            getActivity().onBackPressed();
        });
        return view;

    }

    public class ItineraryPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments;
        private final List<String> dates;

        public ItineraryPagerAdapter(Context context, FragmentManager fm, List<Fragment> fragments, List<String> dates) {
            super(fm);
            this.fragments = fragments;
            this.dates = dates;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return dates.get(position);
        }
    }

    public static class EmptyFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_empty, container, false);
        }
    }

    private List<Fragment> createEmptyFragments(List<String> emptyDates) {
        List<Fragment> fragments = new ArrayList<>();
        for (String date : emptyDates) {
            fragments.add(new com.binus.pekalongancityguide.Layout.ItineraryList.EmptyFragment());
        }
        return fragments;
    }

    private List<Fragment> createFragmentsList(List<String> dates) {
        List<Fragment> fragments = new ArrayList<>();
        List<String> emptyDates = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date prevDate = null;
        int dayCount = 1; // initialize the day count to 1
        for (String date : dates) {
            try {
                Date currentDate = dateFormat.parse(date);
                if (prevDate != null) {
                    long diffInMs = currentDate.getTime() - prevDate.getTime();
                    long diffInDays = TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);
                    if (diffInDays > 1) {
                        for (int i = 1; i < diffInDays; i++) {
                            String emptyDate = dateFormat.format(new Date(prevDate.getTime() + i * TimeUnit.DAYS.toMillis(1)));
                            emptyDates.add(emptyDate);
                        }
                    }
                }
                ItineraryFragment fragment = new ItineraryFragment();
                Bundle args = new Bundle();
                args.putInt("dayIndex", dayCount); // pass the index of the date in the list
                fragment.setArguments(args);
                fragments.add(fragment);
                prevDate = currentDate;
                Log.d(TAG, "current date: " + currentDate);
                dayCount++; // increment the day count
            } catch (ParseException e) {
                Log.e("ItineraryList", "Error parsing date: " + date, e);
                Toast.makeText(getContext(), "Error parsing date: " + date, Toast.LENGTH_SHORT).show();
            }
        }
        fragments.addAll(createEmptyFragments(emptyDates));
        dates.addAll(emptyDates);
        return fragments;
    }

    private String convertToIso8601(String dateStr) {
        try {
            DateFormat originalDateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
            Date date = originalDateFormat.parse(dateStr);
            DateFormat targetDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return targetDateFormat.format(date);
        } catch (ParseException e) {
            Log.e("ItineraryList", "Error parsing date: " + dateStr, e);
            Toast.makeText(getContext(), "Error parsing date: " + dateStr, Toast.LENGTH_SHORT).show();
            return "";
        }
    }
}