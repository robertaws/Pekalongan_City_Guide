package com.binus.pekalongancityguide.Layout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Splash extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        firebaseAuth = FirebaseAuth.getInstance();
        new Handler().postDelayed(() -> checkUser(), 3000);
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null){
            startActivity (new Intent(Splash.this, MainActivity.class));
            finish();
        }else {
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");
            DatabaseReference databaseReference = database.getReference("Users");
            databaseReference.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String userType = "" + snapshot.child("userType").getValue();
                            if (userType.equals("user")) {
                                startActivity(new Intent(Splash.this, Home.class));
                                finish();
                            } else if (userType.equals("admin")) {
                                startActivity(new Intent(Splash.this, AdminHome.class));
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }
}