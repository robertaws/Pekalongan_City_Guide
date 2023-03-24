package com.binus.pekalongancityguide.Layout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.Adapter.ConvoAdapter;
import com.binus.pekalongancityguide.Adapter.PhrasesAdapter;
import com.binus.pekalongancityguide.ItemList.ConvoItem;
import com.binus.pekalongancityguide.ItemList.PhrasesItem;
import com.binus.pekalongancityguide.ItemTemplate.Conversations;
import com.binus.pekalongancityguide.ItemTemplate.Phrases;
import com.binus.pekalongancityguide.R;

import java.util.ArrayList;

public class ConversationFragment extends Fragment {
    RecyclerView convoRV, phrasesRV;
    RecyclerView.Adapter convoRVAdapter, phrasesRVAdapter;
    RecyclerView.LayoutManager convoRVLayoutManager, phrasesRVLayoutManager;
    ArrayList<Conversations> convoData;
    ArrayList<Phrases> phrasesData;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);

        convoRV = view.findViewById(R.id.convo_rv);
        phrasesRV = view.findViewById(R.id.phrases_rv);

        convoRVLayoutManager = new LinearLayoutManager(getContext());
        convoRV.setLayoutManager(convoRVLayoutManager);
        convoRV.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

        phrasesRVLayoutManager = new LinearLayoutManager(getContext());
        phrasesRV.setLayoutManager(phrasesRVLayoutManager);
        phrasesRV.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

        convoData = new ArrayList<>();
        for (int i = 0; i < ConvoItem.name.length; i++) {
            convoData.add(new Conversations(
                    ConvoItem.name[i],
                    ConvoItem.aksara[i],
                    ConvoItem.latinText[i],
                    ConvoItem.engText[i],
                    ConvoItem.indoText[i]
            ));
        }

        convoRVAdapter = new ConvoAdapter(convoData);
        convoRV.setAdapter(convoRVAdapter);

        phrasesData = new ArrayList<>();
        for (int i = 0; i < PhrasesItem.aksara.length; i++) {
            phrasesData.add(new Phrases(
                    PhrasesItem.aksara[i],
                    PhrasesItem.latinText[i],
                    PhrasesItem.engText[i],
                    PhrasesItem.indoText[i]
            ));
        }

        phrasesRVAdapter = new PhrasesAdapter(phrasesData);
        phrasesRV.setAdapter(phrasesRVAdapter);

        return view;
    }
}