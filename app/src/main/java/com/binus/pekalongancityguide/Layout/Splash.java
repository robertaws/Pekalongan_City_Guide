package com.binus.pekalongancityguide.Layout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.binus.pekalongancityguide.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.binus.pekalongancityguide.Misc.Constants.FIREBASE_DATABASE_URL;

public class Splash extends AppCompatActivity {
    private static final int MAPS_PERMIT = 1;
    private FirebaseAuth firebaseAuth;
    private boolean doubleTap = false;
    private LatLng coordinate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        SharedPreferences langPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String language = langPrefs.getString("language", "");
        if (!TextUtils.isEmpty(language)) {
            Locale locale = new Locale(language);
            Configuration config = new Configuration(getResources().getConfiguration());
            config.setLocale(locale);
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }
        firebaseAuth = FirebaseAuth.getInstance();
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), 1);
        } else {
            getMyLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MAPS_PERMIT) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                new Handler().postDelayed(() -> checkUser(), 3000);
            } else {
                Toast.makeText(this, R.string.somePermissions, Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(() -> checkUser(), 3000);
            }
        }
    }
    @Override
    public void onBackPressed() {
        if (doubleTap) {
            super.onBackPressed();
            return;
        }
        this.doubleTap = true;
        Toast.makeText(this, R.string.press_back, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleTap = false;
            }
        }, 2000);
    }
    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(Splash.this, MainActivity.class));
            finish();
        } else {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Users");
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

    private void getMyLocation() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MAPS_PERMIT);
        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                coordinate = new LatLng(latitude, longitude);
            } else {
                double defaultLatitude = -6.8869;
                double defaultLongitude = 109.6744;
                coordinate = new LatLng(defaultLatitude, defaultLongitude);
            }
            SharedPreferences locPrefs = this.getApplicationContext().getSharedPreferences("coordinate", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = locPrefs.edit();
            editor.clear();
            editor.putString("lastLatitude", String.valueOf(coordinate.latitude));
            editor.putString("lastLongitude", String.valueOf(coordinate.longitude));
            editor.apply();
            checkUser();
        }
    }

}