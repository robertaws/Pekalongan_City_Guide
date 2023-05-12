package com.binus.pekalongancityguide.Layout;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.binus.pekalongancityguide.Adapter.IterAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Categories;
import com.binus.pekalongancityguide.ItemTemplate.Destination;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.DialogChooseDateBinding;
import com.binus.pekalongancityguide.databinding.FragmentItineraryPagerBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ItineraryPager extends DialogFragment {
    public ArrayList<Categories> categoriesArrayList;
    public ViewPagerAdapter viewPagerAdapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FragmentItineraryPagerBinding binding;
    private EditText startEt,endEt;
    private ImageButton startBtn,endBtn;
    private Button addDate;
    private int startDay,startMonth,startYear,startDay1,startMonth1,startYear1,
            endDay,endMonth,endYear,endDay1,endMonth1,endYear1;
    public ItineraryPager() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentItineraryPagerBinding.inflate(LayoutInflater.from(getContext()), container, false);
        init();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        DialogChooseDateBinding chooseDateBinding = DialogChooseDateBinding.inflate(getLayoutInflater());
        builder.setView(chooseDateBinding.getRoot());
        startEt = chooseDateBinding.startDateEt;
        endEt = chooseDateBinding.endDateEt;
        startBtn = chooseDateBinding.startPickerBtn;
        endBtn = chooseDateBinding.endPickerBtn;
        addDate = chooseDateBinding.addDateBtn;
        startBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            startYear = calendar.get(Calendar.YEAR);
            startMonth = calendar.get(Calendar.MONTH);
            startDay = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog;
            dialog = new DatePickerDialog(getContext(), (dateView, year, month, dayOfMonth) -> {
                startYear = year;
                startMonth = month;
                startDay = dayOfMonth;
                SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
                String dateString = format.format(new Date(startYear - 1900, startMonth, startDay));
                startEt.setText(dateString);
            }, startYear, startMonth, startDay);
            dialog.getWindow().setBackgroundDrawableResource(R.color.palette_4);
            dialog.show();
        });
        startEt.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            startYear1 = calendar.get(Calendar.YEAR);
            startMonth1 = calendar.get(Calendar.MONTH);
            startDay1 = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog;
            dialog = new DatePickerDialog(getContext(), (dateView, year, month, dayOfMonth) -> {
                startYear1 = year;
                startMonth1 = month;
                startDay1 = dayOfMonth;
                SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
                String dateString = format.format(new Date(startYear1 - 1900, startMonth1, startDay1));
                startEt.setText(dateString);
            }, startYear1, startMonth1, startDay1);
            dialog.getWindow().setBackgroundDrawableResource(R.color.palette_4);
            dialog.show();
        });
        endBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            endYear = calendar.get(Calendar.YEAR);
            endMonth = calendar.get(Calendar.MONTH);
            endDay = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog;
            dialog = new DatePickerDialog(getContext(), (dateView, year, month, dayOfMonth) -> {
                endYear = year;
                endMonth = month;
                endDay = dayOfMonth;
                SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
                String dateString = format.format(new Date(endYear - 1900, endMonth, endDay));
                endEt.setText(dateString);
            }, endYear, endMonth, endDay);
            dialog.getWindow().setBackgroundDrawableResource(R.color.palette_4);
            dialog.show();
        });
        endEt.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            endYear1 = calendar.get(Calendar.YEAR);
            endMonth1 = calendar.get(Calendar.MONTH);
            endDay1 = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog;
            dialog = new DatePickerDialog(getContext(), (dateView, year, month, dayOfMonth) -> {
                endYear1 = year;
                endMonth1 = month;
                endDay1 = dayOfMonth;
                SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
                String dateString = format.format(new Date(endYear1 - 1900, endMonth1, endDay1));
                endEt.setText(dateString);
            }, endYear1, endMonth1, endDay1);
            dialog.getWindow().setBackgroundDrawableResource(R.color.palette_4);
            dialog.show();
        });
        addDate.setOnClickListener(v ->{

        });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();
        setupViewPagerAdapter(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        return binding.getRoot();
    }

    private void init() {
        tabLayout = binding.iterTabLayout;
        viewPager = binding.iterViewPager;
    }

    private void setupViewPagerAdapter(ViewPager viewPager) {
        viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), getContext());
        categoriesArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriesArrayList.clear();
                Categories allCategories = new Categories("01","All","",1);
                categoriesArrayList.add(allCategories);
                viewPagerAdapter.addFragment(AddItinerary.newInstance(
                        ""+allCategories.getId(),
                        ""+allCategories.getCategory(),
                        ""+allCategories.getUid()
                ),allCategories.getCategory());
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Categories categories = dataSnapshot.getValue(Categories.class);
                    categoriesArrayList.add(categories);
                    viewPagerAdapter.addFragment(AddItinerary.newInstance(
                            ""+categories.getId(),
                            ""+categories.getCategory(),
                            ""+categories.getUid()),categories.getCategory());
                }
                viewPagerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(10);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private final ArrayList<AddItinerary> fragmentList = new ArrayList<>();
        private final ArrayList<String> fragmentTitleList = new ArrayList<>();
        private final Context context;

        public ViewPagerAdapter(@NonNull FragmentManager fm, Context context) {
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

        private void addFragment(AddItinerary fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

}