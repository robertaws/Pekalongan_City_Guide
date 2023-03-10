package com.binus.pekalongancityguide.Layout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.Adapter.FoodAdapter;
import com.binus.pekalongancityguide.Adapter.NewsAdapter;
import com.binus.pekalongancityguide.ItemList.FoodItem;
import com.binus.pekalongancityguide.ItemTemplate.Article;
import com.binus.pekalongancityguide.ItemTemplate.Food;
import com.binus.pekalongancityguide.Misc.NewsApi;
import com.binus.pekalongancityguide.Misc.NewsResponse;
import com.binus.pekalongancityguide.R;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class HomeFragment extends Fragment {
    private static final String BASE_URL = "https://newsapi.org/";
    private static final String API_KEY = "49cac8db4dfe4313bad00a25c272f6e6";
    NewsAdapter adapter;
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

        fetchNews();
        return view;
    }
    private void fetchNews(){
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        NewsApi api = retrofit.create(NewsApi.class);
        Call<NewsResponse> call = api.getNews("pekalongan", API_KEY);
        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful()) {
                    List<Article> articles = response.body().getArticles();
                    adapter = new NewsAdapter(articles);
                    recyclerView.setAdapter(adapter);
                } else {
                    Log.e("NewsAPI", "Request failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                Log.e("NewsAPI", "Request failed: " + t.getMessage());
            }
        });
    }
}