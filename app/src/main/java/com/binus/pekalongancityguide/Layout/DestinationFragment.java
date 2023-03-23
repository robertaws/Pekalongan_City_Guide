package com.binus.pekalongancityguide.Layout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.Adapter.DestinationAdapter;
import com.binus.pekalongancityguide.ItemList.DestinationItem;
import com.binus.pekalongancityguide.ItemTemplate.Categories;
import com.binus.pekalongancityguide.ItemTemplate.Destination;
import com.binus.pekalongancityguide.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class DestinationFragment extends Fragment {
    public ArrayList<Categories> categoriesArrayList;
    RecyclerView destiRV;
    RecyclerView.Adapter destiRVAdapter;
    RecyclerView.LayoutManager destiRVLayoutManager;
    ArrayList<Destination> destiData;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_destination, container, false);
        destiRV = view.findViewById(R.id.desti_rv);
        return view;
    }
}