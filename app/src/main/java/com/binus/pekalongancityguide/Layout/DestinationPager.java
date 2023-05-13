package com.binus.pekalongancityguide.Layout;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.binus.pekalongancityguide.ItemTemplate.Categories;
import com.binus.pekalongancityguide.databinding.FragmentDestinationBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DestinationPager extends Fragment {
    private ViewPager viewPager;
    private TabLayout tabLayout;
    public ArrayList<Categories> categoriesArrayList;
    public ViewPagerAdapter viewPagerAdapter;
    private FragmentDestinationBinding binding;
    private int selectedTabPosition = 0;
    private static final String TAG = "DESTI_USER_TAG";

    public DestinationPager() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDestinationBinding.inflate(LayoutInflater.from(getContext()), container, false);
        init();
        setupViewPagerAdapter(viewPager);
        updatePager();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // This method is called when the user starts scrolling the pager
            }

            @Override
            public void onPageSelected(int position) {
                updatePager();
                Log.d("Destination Pager", "Selected tab: " + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // This method is called when the scroll state changes (idle, dragging, settling)
            }
        });
        return binding.getRoot();
    }

    private void init() {
        viewPager = binding.viewPager;
        tabLayout = binding.tabLayout;
    }

    public void updatePager() {
        tabLayout.setupWithViewPager(viewPager);
    }

    public void onDataChanged() {
        if (viewPager != null && viewPager.getAdapter() != null) {
            viewPager.getAdapter().notifyDataSetChanged();
        }
    }

    private void setupViewPagerAdapter(ViewPager viewPager) {
        viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), getContext());
        categoriesArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriesArrayList.clear();
                Categories allCategories = new Categories("01", "All", "", 1);
                categoriesArrayList.add(allCategories);
                viewPagerAdapter.addFragment(ShowDestinationFragment.newInstance(
                        ""+allCategories.getId(),
                        ""+allCategories.getCategory(),
                        ""+allCategories.getUid()
                ),allCategories.getCategory());
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Categories categories = dataSnapshot.getValue(Categories.class);
                    categoriesArrayList.add(categories);
                    viewPagerAdapter.addFragment(ShowDestinationFragment.newInstance(
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
        private final ArrayList<ShowDestinationFragment> fragmentList = new ArrayList<>();
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
        private void addFragment(ShowDestinationFragment fragment, String title){
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }
        public CharSequence getPageTitle(int position){
            return fragmentTitleList.get(position);
        }
    }
}