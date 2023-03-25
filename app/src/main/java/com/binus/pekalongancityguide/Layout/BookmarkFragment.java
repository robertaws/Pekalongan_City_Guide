package com.binus.pekalongancityguide.Layout;

import static com.binus.pekalongancityguide.Layout.AddDestination.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.binus.pekalongancityguide.Adapter.BookmarkAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Destination;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.FragmentBookmarkBinding;
import com.binus.pekalongancityguide.databinding.FragmentDestinationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BookmarkFragment extends Fragment {
    private ArrayList<Destination> destinationArrayList;
    private BookmarkAdapter bookmarkAdapter;
    private FragmentBookmarkBinding binding;
    private FirebaseAuth firebaseAuth;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookmarkBinding.inflate(LayoutInflater.from(getContext()),container,false);
        firebaseAuth = FirebaseAuth.getInstance();
        loadBookmark();
        binding.searchBookmark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    bookmarkAdapter.getFilter().filter(s);
                }catch (Exception e){
                    Log.d(TAG,"onTextChanged :"+e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return binding.getRoot();
    }
    private void loadBookmark(){
        destinationArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        destinationArrayList.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            String destiId = ""+dataSnapshot.child("destiId").getValue();
                            Destination destination = new Destination();
                            destination.setId(destiId);
                            destinationArrayList.add(destination);
                        }
                            bookmarkAdapter = new BookmarkAdapter(getContext(),destinationArrayList);
                            binding.destiRV.setAdapter(bookmarkAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}