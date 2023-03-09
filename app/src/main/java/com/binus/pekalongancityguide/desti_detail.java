package com.binus.pekalongancityguide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class desti_detail extends Fragment {
    ImageView dImage;
    TextView dName,dDesc,dAddress;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_desti_detail, container, false);
        dImage = view.findViewById(R.id.ddimg);
        dName = view.findViewById(R.id.ddname);
        dDesc = view.findViewById(R.id.dddesc);
        dAddress = view.findViewById(R.id.ddadd);
        dImage.setImageResource(getActivity().getIntent().getIntExtra("destiImage",R.drawable.desti1));
        dName.setText(getActivity().getIntent().getStringExtra("destiName"));
        dDesc.setText(getActivity().getIntent().getStringExtra("destiDesc"));
        dAddress.setText(getActivity().getIntent().getStringExtra("destiAddress"));
        return view;
    }

}