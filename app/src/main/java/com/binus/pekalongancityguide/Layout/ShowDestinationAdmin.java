package com.binus.pekalongancityguide.Layout;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.Adapter.AdminDestinationAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Destination;
import com.binus.pekalongancityguide.databinding.ActivityShowDestinationAdminBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.binus.pekalongancityguide.Misc.Constants.FIREBASE_DATABASE_URL;

public class ShowDestinationAdmin extends AppCompatActivity {
    private ActivityShowDestinationAdminBinding binding;
    private ArrayList<Destination> destinationArrayList;
    private AdminDestinationAdapter adapter;
    private String categoryId, categoryTitle;
    private static final String TAG = "DESTI_LIST_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowDestinationAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTitle = intent.getStringExtra("categoryTitle");
        binding.subtitleTv.setText(categoryTitle);

        loadDestination();

        binding.searchDestiadmin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapter.getFilter().filter(s);
                }catch (Exception e){
                    Log.d(TAG,"onTextChanged: "+e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.backCategory.setOnClickListener(v -> onBackPressed());
    }

    private void loadDestination(){
        destinationArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Destination");
        reference.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        destinationArrayList.clear();
                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            Destination destination = dataSnapshot.getValue(Destination.class);
                            destinationArrayList.add(destination);
                            Log.d(TAG,"onDatachanged: "+ destination.getId()+""+ destination.getTitle());
                        }
                        adapter = new AdminDestinationAdapter(ShowDestinationAdmin.this, destinationArrayList);
                        binding.destiAdminrv.setAdapter(adapter);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}