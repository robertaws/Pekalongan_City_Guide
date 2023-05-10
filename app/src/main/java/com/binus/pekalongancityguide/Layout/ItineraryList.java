package com.binus.pekalongancityguide.Layout;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.FragmentItineraryDetailsBinding;
import com.binus.pekalongancityguide.databinding.FragmentItineraryListBinding;

public class ItineraryList extends Fragment {
    private FragmentItineraryListBinding binding;
    public ItineraryList() {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentItineraryListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}