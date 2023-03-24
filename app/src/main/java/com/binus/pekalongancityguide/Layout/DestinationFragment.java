package com.binus.pekalongancityguide.Layout;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.binus.pekalongancityguide.Adapter.DestinationAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Categories;
import com.binus.pekalongancityguide.ItemTemplate.Destination;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.FragmentDestinationBinding;
import com.denzcoskun.imageslider.adapters.ViewPagerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class DestinationFragment extends Fragment {
    public ArrayList<Categories> categoriesArrayList;
    public ViewPagerAdapter viewPagerAdapter;
    private FragmentDestinationBinding binding;
    private static final String TAG = "DESTI_USER_TAG";
    public DestinationFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDestinationBinding.inflate(LayoutInflater.from(getContext()),container,false);
        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);
        return binding.getRoot();
    }
    private void setupViewPagerAdapter(ViewPager viewPager){
        viewPagerAdapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,getContext());
        categoriesArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriesArrayList.clear();
                Categories allCategories = new Categories("01","All","",1);
                categoriesArrayList.add(allCategories);
                viewPagerAdapter.addFragment(ShowDestinationFragment.newInstance(
                        ""+allCategories.getId(),
                        ""+allCategories.getCategory(),
                        ""+allCategories.getUid()
                ),allCategories.getCategory());
                viewPagerAdapter.notifyDataSetChanged();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Categories categories = dataSnapshot.getValue(Categories.class);
                    categoriesArrayList.add(categories);
                    viewPagerAdapter.addFragment(ShowDestinationFragment.newInstance(
                            ""+categories.getId(),
                            ""+categories.getCategory(),
                            ""+categories.getUid()),categories.getCategory());
                    viewPagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        viewPager.setAdapter(viewPagerAdapter);
    }
    public class ViewPagerAdapter extends FragmentPagerAdapter{
        private ArrayList <ShowDestinationFragment> fragmentList = new ArrayList<>();;
        private ArrayList <String> fragmentTitleList = new ArrayList<>();
        private Context context;
        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
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