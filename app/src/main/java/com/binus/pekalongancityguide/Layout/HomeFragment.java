package com.binus.pekalongancityguide.Layout;

import static com.binus.pekalongancityguide.BuildConfig.NEWS_API_KEY;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.Adapter.FoodAdapter;
import com.binus.pekalongancityguide.Adapter.NewsAdapter;
import com.binus.pekalongancityguide.ItemList.FoodItem;
import com.binus.pekalongancityguide.ItemTemplate.Food;
import com.binus.pekalongancityguide.Misc.ImageFullscreen;
import com.binus.pekalongancityguide.R;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.Article;
import com.kwabenaberko.newsapilib.models.request.EverythingRequest;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class HomeFragment extends Fragment {
    RecyclerView foodRV, newsRV;
    RecyclerView.Adapter foodRVAdapter;
    RecyclerView.LayoutManager foodRVLayoutManager, newsRVLayoutManager;
    ArrayList<Food> foodData;
    TextView cityDesc;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        NewsApiClient newsApiClient = new NewsApiClient(NEWS_API_KEY);
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

        newsRV = view.findViewById(R.id.newsRV);
        newsRVLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        newsRV.setLayoutManager(newsRVLayoutManager);

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

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -28);
        Date dateBefore30Days = cal.getTime();
        String dateString = dateFormat.format(dateBefore30Days);

        EverythingRequest everythingRequest = new EverythingRequest.Builder()
                .q("jawa tengah")
                .language("id")
                .from(dateString)
                .to(String.valueOf(new Date()))
                .sortBy("publishedAt")
                .build();
        newsApiClient.getEverything(everythingRequest, new NewsApiClient.ArticlesResponseCallback() {
            @Override
            public void onSuccess(ArticleResponse response) {
                List<Article> articles = response.getArticles();
                NewsAdapter newsAdapter = new NewsAdapter(articles);
                newsRV.setAdapter(newsAdapter);
            }
            @Override
            public void onFailure(Throwable throwable) {
                Log.e("NewsAPI", "Error fetching news articles: " + throwable.getMessage());
            }
        });
        return view;
    }


}

