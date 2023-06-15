package com.binus.pekalongancityguide.Layout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.binus.pekalongancityguide.databinding.FragmentEmptyBookmark2Binding;

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