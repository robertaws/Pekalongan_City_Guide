package com.binus.pekalongancityguide.Layout;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.binus.pekalongancityguide.AddCategory;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityAdminHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminHome extends AppCompatActivity {
    private ActivityAdminHomeBinding binding;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        binding.logoutAdmin.setOnClickListener(v -> {
            firebaseAuth.signOut();
            checkUser();
        });
        binding.addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminHome.this, AddCategory.class));
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