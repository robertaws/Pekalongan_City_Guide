package com.binus.pekalongancityguide.Layout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class HomeFragment extends Fragment {
    RecyclerView foodRV, recyclerView;
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

        recyclerView = view.findViewById(R.id.news_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }
}