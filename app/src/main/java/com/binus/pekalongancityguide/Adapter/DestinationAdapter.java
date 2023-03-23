package com.binus.pekalongancityguide.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Categories;
import com.binus.pekalongancityguide.databinding.ListDestinationBinding;

import java.util.ArrayList;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.HolderDestination>{
    private Context context;
    private ArrayList<Categories> categoriesArrayList;
    private ListDestinationBinding binding;
    private static final String TAG = "ADAPTER_USER_TAG";


    public DestinationAdapter(Context context, ArrayList<Categories> categoriesArrayList) {
        this.context = context;
        this.categoriesArrayList = categoriesArrayList;
    }

    @NonNull
    @Override
    public HolderDestination onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull HolderDestination holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class HolderDestination extends RecyclerView.ViewHolder{

        public HolderDestination(@NonNull View itemView) {
            super(itemView);
        }
    }
}
