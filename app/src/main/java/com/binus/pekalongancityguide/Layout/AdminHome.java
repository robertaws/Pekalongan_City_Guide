package com.binus.pekalongancityguide.Layout;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.Adapter.CategoryAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Categories;
import com.binus.pekalongancityguide.databinding.ActivityAdminHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.binus.pekalongancityguide.Misc.Constants.FIREBASE_DATABASE_URL;

public class AdminHome extends AppCompatActivity {
    private ActivityAdminHomeBinding binding;
    private FirebaseAuth firebaseAuth;
    private ArrayList<Categories> categoriesArrayList;
    private CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        showCategory();
        binding.searchCat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    categoryAdapter.getFilter().filter(s);
                }catch(Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.logoutAdmin.setOnClickListener(v -> {
            firebaseAuth.signOut();
            checkUser();
        });
        binding.addCategory.setOnClickListener(v -> startActivity(new Intent(AdminHome.this, AddCategory.class)));
        binding.addLocFab.setOnClickListener(v -> startActivity(new Intent(AdminHome.this, AddDestination.class)));
    }

    private void showCategory() {
        categoriesArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Categories");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriesArrayList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Categories categories = dataSnapshot.getValue(Categories.class);
                    categoriesArrayList.add(categories);
                }
                categoryAdapter = new CategoryAdapter(AdminHome.this,categoriesArrayList);
                binding.catRv.setAdapter(categoryAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUser() {
        FirebaseUser firebaseUser =firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            startActivity(new Intent(AdminHome.this,MainActivity.class));
        }else{
            String email = firebaseUser.getEmail();
            binding.adminInfo.setText(email);
        }
    }
}