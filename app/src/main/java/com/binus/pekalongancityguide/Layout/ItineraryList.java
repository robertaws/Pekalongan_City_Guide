package com.binus.pekalongancityguide.Layout;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityItineraryListBinding;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.content.ContentValues.TAG;

public class ItineraryList extends AppCompatActivity {
    public ActivityItineraryListBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        binding = ActivityItineraryListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backtoprofile.setOnClickListener(v -> {
            onBackPressed();
        });
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference userRef = database.getReference("Users").child(Objects.requireNonNull(firebaseAuth.getUid()));
        userRef.keepSynced(true);
        Query itineraryQuery = userRef.child("itinerary");
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

                List<Fragment> fragments = createFragmentsList(dates);

                ItineraryPagerAdapter vpAdapter = new ItineraryPagerAdapter(ItineraryList.this, getSupportFragmentManager(), fragments, dates);
                binding.viewPager.setAdapter(vpAdapter);
                binding.viewPager.setOffscreenPageLimit(10);
                binding.itineraryTab.setupWithViewPager(binding.viewPager);
                binding.itineraryTab.setSelectedTabIndicatorColor(ContextCompat.getColor(ItineraryList.this, R.color.white));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public class ItineraryPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments;
        private final List<String> dates;
        private String selectedDate;
        public ItineraryPagerAdapter(Context context, FragmentManager fm, List<Fragment> fragments, List<String> dates) {
            super(fm);
            this.fragments = fragments;
            this.dates = dates;
        }
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = fragments.get(position);
            return fragment;
        }
        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return dates.get(position);
        }

        public void setSelectedDate(String date) {
            this.selectedDate = date;
            notifyDataSetChanged();
        }
    }

    private List<Fragment> createFragmentsList(List<String> dates) {
        List<Fragment> fragments = new ArrayList<>();
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

        for (String date : dates) {
            ItineraryFragment fragment = new ItineraryFragment();
            Bundle args = new Bundle();
            args.putString("date", date);
            args.putString("selectedDate", convertToNormalDate(date));
            Log.d(TAG, "passed date: " + args);
            fragment.setArguments(args);
            fragments.add(fragment);
        }
        return fragments;
    }

    private String convertToNormalDate(String dateStr) {
        try {
            DateFormat originalDateFormat = new SimpleDateFormat("dd MMMM", Locale.getDefault());
            Date date = originalDateFormat.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            int currentYear = calendar.get(Calendar.YEAR);
            calendar.setTime(date);
            calendar.set(Calendar.YEAR, currentYear);
            Date dateWithYear = calendar.getTime();
            DateFormat targetDateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
            return targetDateFormat.format(dateWithYear);
        } catch (ParseException e) {
            Log.e("ItineraryList", "Error parsing date: " + dateStr, e);
            Toast.makeText(ItineraryList.this, "Error parsing date: " + dateStr, Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    private String convertWithoutDay(String dateStr) {
        try {
            DateFormat originalDateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
            Date date = originalDateFormat.parse(dateStr);
            DateFormat targetDateFormat = new SimpleDateFormat("dd MMMM", Locale.getDefault());
            return targetDateFormat.format(date);
        } catch (ParseException e) {
            Log.e("ItineraryList", "Error parsing date: " + dateStr, e);
            Toast.makeText(ItineraryList.this, "Error parsing date: " + dateStr, Toast.LENGTH_SHORT).show();
            return "";
        }
    }
}