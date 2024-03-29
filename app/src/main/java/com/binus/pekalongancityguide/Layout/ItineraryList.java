package com.binus.pekalongancityguide.Layout;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.FragmentItineraryListBinding;
import com.google.android.material.tabs.TabLayout;
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

import static android.content.ContentValues.TAG;
import static com.binus.pekalongancityguide.Misc.Constants.FIREBASE_DATABASE_URL;

public class ItineraryList extends Fragment {
    private FragmentItineraryListBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    ItineraryPagerAdapter vpAdapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<Fragment> fragments;

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
        init();
        database = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL);
        updateItineraryView();
        return binding.getRoot();
    }

    public void init() {
        viewPager = binding.viewPager;
        tabLayout = binding.itineraryTab;
    }

    public void setAdapter(ItineraryPagerAdapter adapter) {
        this.vpAdapter = adapter;
    }

    public void onDataChanged() {
        if (viewPager != null && viewPager.getAdapter() != null) {
            viewPager.getAdapter().notifyDataSetChanged();
        }
    }

    public void updateItineraryView() {
        DatabaseReference userRef = database.getReference("Users").child(firebaseAuth.getUid());
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
                    final DateFormat dateFormat = new SimpleDateFormat("dd MMMM", Locale.getDefault());

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

                fragments = createFragmentsList(dates);
                vpAdapter = new ItineraryPagerAdapter(getContext(), getChildFragmentManager(), fragments, dates);
                viewPager.setAdapter(vpAdapter);
                viewPager.setOffscreenPageLimit(10);

                if (!fragments.isEmpty()) {
                    tabLayout.setupWithViewPager(viewPager);
                    tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(getContext(), R.color.white));
                } else {
                    tabLayout.setVisibility(View.GONE);
                }

                vpAdapter.notifyDataSetChanged();

                if (fragments.isEmpty() && dates.isEmpty()) {
                    EmptyItinerary emptyFragment = new EmptyItinerary();
                    fragments.add(emptyFragment);
                    vpAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public class ItineraryPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> fragments;
        private final List<String> dates;
        private String selectedDate;

        public ItineraryPagerAdapter(Context context, FragmentManager fm, List<Fragment> fragments, List<String> dates) {
            super(fm);
            this.fragments = fragments;
            this.dates = dates;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        public long getItemId(int position) {
            return fragments.get(position).hashCode();
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
            if (position >= 0 && position < dates.size()) {
                return dates.get(position);
            } else {
                return "";
            }
        }

        public void setSelectedDate(String date) {
            this.selectedDate = date;
            notifyDataSetChanged();
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
            try {
                super.restoreState(state, loader);
            } catch (IllegalStateException e) {
                updateItineraryView();
            }
        }
    }

    private List<Fragment> createFragmentsList(List<String> dates) {
        List<Fragment> fragments = new ArrayList<>();
        Collections.sort(dates, new Comparator<String>() {
            final DateFormat dateFormat = new SimpleDateFormat("dd MMMM", Locale.getDefault());
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
            Toast.makeText(getContext(), "Error parsing date: " + dateStr, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getContext(), "Error parsing date: " + dateStr, Toast.LENGTH_SHORT).show();
            return "";
        }
    }
}