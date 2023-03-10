package com.binus.pekalongancityguide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    RecyclerView foodRV;
    RecyclerView.Adapter foodRVAdapter;
    RecyclerView.LayoutManager foodRVLayoutManager;
    ArrayList<Food> foodData;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ImageSlider imageSlider = view.findViewById(R.id.spotlight);
        ArrayList<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.desti1, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.desti2, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.desti3, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.desti4, ScaleTypes.FIT));
        imageSlider.setImageList(slideModels, ScaleTypes.FIT);

        foodRV = view.findViewById(R.id.food_rv);
        foodRVLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        foodRV.setLayoutManager(foodRVLayoutManager);

        foodData = new ArrayList<>();
        for (int i = 0; i < FoodItem.foodimage.length; i++) {
            foodData.add(new Food(
                    FoodItem.foodimage[i],
                    FoodItem.foodname[i],
                    FoodItem.foodname2[i]
            ));
        }
        foodRVAdapter = new FoodAdapter(foodData);
        foodRV.setAdapter(foodRVAdapter);
        return view;
    }
}