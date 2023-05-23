package com.binus.pekalongancityguide.Layout;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.FragmentEmptyBookmark2Binding;
import com.binus.pekalongancityguide.databinding.FragmentItineraryListBinding;

public class EmptyBookmark extends Fragment {
    private FragmentEmptyBookmark2Binding binding;

    public EmptyBookmark() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEmptyBookmark2Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}