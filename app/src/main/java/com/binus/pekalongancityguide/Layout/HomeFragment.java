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
import com.binus.pekalongancityguide.Adapter.NewsAdapter;
import com.binus.pekalongancityguide.ItemList.FoodItem;
import com.binus.pekalongancityguide.ItemList.NewsItem;
import com.binus.pekalongancityguide.ItemTemplate.Food;
import com.binus.pekalongancityguide.Misc.NewsApiResponse;
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

    private NewsAdapter adapter;
    private List<NewsItem> newsItems = new ArrayList<>();


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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final NewsAdapter adapter = new NewsAdapter(new ArrayList<>());

        RecyclerView recyclerView = view.findViewById(R.id.news_list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Make the API request here and update the adapter with the retrieved news items.
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.newscatcherapi.com/v2/search?q=pekalongan&lang=id&sort_by=relevancy&page=1&page_size=10")
                .get()
                .addHeader("x-api-key", "a91a7EddioEC84xTmy3PBxE8WvG_UhVKsE6a-2eQp1M")
                .build();

        NewsAdapter finalAdapter = adapter;
        NewsAdapter finalAdapter1 = adapter;
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String responseString = response.body().string();
                    Gson gson = new Gson();
                    NewsApiResponse newsApiResponse = gson.fromJson(responseString, NewsApiResponse.class);
                    List<NewsItem> newNewsItems = newsApiResponse.getNewsItems();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setNewsItems(newNewsItems);
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API Request", "Error occurred during API request", e);
            }
        });

    }
}