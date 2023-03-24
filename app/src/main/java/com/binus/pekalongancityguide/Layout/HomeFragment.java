package com.binus.pekalongancityguide.Layout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.Adapter.FoodAdapter;
import com.binus.pekalongancityguide.ItemList.FoodItem;
import com.binus.pekalongancityguide.ItemTemplate.Food;
import com.binus.pekalongancityguide.R;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    RecyclerView foodRV, recyclerView;

    RecyclerView.Adapter foodRVAdapter;
    RecyclerView.LayoutManager foodRVLayoutManager;
    ArrayList<Food> foodData;
    TextView cityDesc;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ImageSlider imageSlider = view.findViewById(R.id.spotlight);
        ArrayList<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.slide1, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.slide2, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.slide3, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.slide4, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.slide5, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.slide6, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.slide7, ScaleTypes.FIT));
        imageSlider.setImageList(slideModels, ScaleTypes.FIT);
        cityDesc = view.findViewById(R.id.city_desc);
        cityDesc.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CityHistory.class));
        });
        foodRV = view.findViewById(R.id.food_rv);
        foodRVLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        foodRV.setLayoutManager(foodRVLayoutManager);

        foodData = new ArrayList<>();
        for (int i = 0; i < FoodItem.foodimage.length; i++) {
            foodData.add(new Food(
                    FoodItem.foodimage[i],
                    FoodItem.foodimage1[i],
                    FoodItem.foodimage2[i],
                    FoodItem.foodimage3[i],
                    FoodItem.foodname[i],
                    FoodItem.foodname2[i],
                    FoodItem.fooddesc[i]
            ));
        }
        foodRVAdapter = new FoodAdapter(foodData);
        foodRV.setAdapter(foodRVAdapter);

        recyclerView = view.findViewById(R.id.newsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }
}