package com.binus.pekalongancityguide;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class Home extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment = new HomeFragment();
    DestinationFragment destinationFragment = new DestinationFragment();
    ConversationFragment conversationFragment = new ConversationFragment();
    BookmarkFragment bookmarkFragment = new BookmarkFragment();
    ProfileFragment profileFragment = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        bottomNavigationView = findViewById(R.id.bottom_navi);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_nav:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
                        return true;
                    case R.id.destination_nav:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, destinationFragment).commit();
                        return true;
                    case R.id.conversation_nav:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, conversationFragment).commit();
                        return true;
                    case R.id.bookmark_nav:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, bookmarkFragment).commit();
                        return true;
                    case R.id.profile_nav:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, profileFragment).commit();
                        return true;
                }
                return false;
            }
        });
    }
}