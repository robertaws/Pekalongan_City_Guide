package com.binus.pekalongancityguide.Layout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

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

import java.util.Locale;

public class Splash extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String language = preferences.getString("language", "");
        if (!TextUtils.isEmpty(language)) {
            Locale locale = new Locale(language);
            Configuration config = new Configuration(getResources().getConfiguration());
            config.setLocale(locale);
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }
        firebaseAuth = FirebaseAuth.getInstance();
        new Handler().postDelayed(() -> checkUser(), 3000);
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(Splash.this, MainActivity.class));
            finish();
        } else {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
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
                                Toast.makeText(Splash.this,R.string.error_connect_database,Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Splash.this, MainActivity.class));
                                finish();
                            }
                        });
            } else {
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Splash.this, Home.class));
                finish();
            }
        }
    }
}