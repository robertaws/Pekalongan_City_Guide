package com.binus.pekalongancityguide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DestinationFragment extends Fragment {
    RecyclerView destiRV;
    RecyclerView.Adapter destiRVAdapter;
    RecyclerView.LayoutManager destiRVLayoutManager;
    ArrayList<Destination> destiData;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_destination, container, false);
        destiRV = view.findViewById(R.id.desti_rv);
        destiRVLayoutManager = new LinearLayoutManager(getContext());
        destiRV.setLayoutManager(destiRVLayoutManager);

        destiData = new ArrayList<>();
        for (int i = 0; i < DestinationItem.destiimage.length; i++) {
            destiData.add(new Destination(
                    DestinationItem.destiimage[i],
                    DestinationItem.destiname[i],
                    DestinationItem.destiname2[i]
            ));
        }
        destiRVAdapter = new DestinationAdapter(destiData);
        destiRV.setAdapter(destiRVAdapter);
        return view;
    }
}