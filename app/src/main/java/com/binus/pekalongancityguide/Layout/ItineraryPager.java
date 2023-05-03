package com.binus.pekalongancityguide.Layout;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.binus.pekalongancityguide.ItemTemplate.Categories;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.FragmentDestinationBinding;
import com.binus.pekalongancityguide.databinding.FragmentItineraryPagerBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ItineraryPager extends Fragment {
    public ArrayList<Categories> categoriesArrayList;
    public ViewPagerAdapter viewPagerAdapter;
    private FragmentItineraryPagerBinding binding;
    public ItineraryPager() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        binding = FragmentItineraryPagerBinding.inflate(LayoutInflater.from(getContext()), container, false);
        setupViewPagerAdapter(binding.iterViewPager);
        binding.iterTabLayout.setupWithViewPager(binding.iterViewPager);
        return binding.getRoot();
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
        private void addFragment(AddItinerary fragment, String title){
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }
        public CharSequence getPageTitle(int position){
            return fragmentTitleList.get(position);
        }
    }

}