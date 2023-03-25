package com.binus.pekalongancityguide.Layout;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.binus.pekalongancityguide.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sagarkoli.chetanbottomnavigation.chetanBottomNavigation;

public class Home extends AppCompatActivity {
    private static final int home = 1;
    HomeFragment homeFragment = new HomeFragment();
    DestinationFragment destinationFragment = new DestinationFragment();
    ConversationFragment conversationFragment = new ConversationFragment();
    BookmarkFragment bookmarkFragment = new BookmarkFragment();
    ProfileFragment profileFragment = new ProfileFragment();
    private static final int desti = 2;
    private static final int bm = 3;
    private static final int convo = 4;
    private static final int pr = 5;
    private FirebaseAuth firebaseAuth;
    chetanBottomNavigation bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        bottomNavigationView = findViewById(R.id.bottom_navi);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
        bottomNavigationView.add(new chetanBottomNavigation.Model(home, R.drawable.ic_home));
        bottomNavigationView.add(new chetanBottomNavigation.Model(desti, R.drawable.destination));
        bottomNavigationView.add(new chetanBottomNavigation.Model(bm, R.drawable.remove_bookmark));
        bottomNavigationView.add(new chetanBottomNavigation.Model(convo, R.drawable.chat));
        bottomNavigationView.add(new chetanBottomNavigation.Model(pr, R.drawable.profile));

        bottomNavigationView.setOnShowListener(new chetanBottomNavigation.ShowListener() {
            @Override
            public void onShowItem(chetanBottomNavigation.Model item) {
                String name;
                switch (item.getId()) {
                    case home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commitAllowingStateLoss();
                        break;

                    case desti:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, destinationFragment).commitAllowingStateLoss();
                        break;

                    case bm:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, bookmarkFragment).commitAllowingStateLoss();
                        break;

                    case convo:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, conversationFragment).commitAllowingStateLoss();
                        break;

                    case pr:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, profileFragment).commitAllowingStateLoss();
                        break;
                }
            }
        });

        bottomNavigationView.setOnClickMenuListener(new chetanBottomNavigation.ClickListener() {
            @Override
            public void onClickItem(chetanBottomNavigation.Model item) {
            }
        });

        bottomNavigationView.setOnReselectListener(new chetanBottomNavigation.ReselectListener() {
            @Override
            public void onReselectItem(chetanBottomNavigation.Model item) {
            }
        });
        bottomNavigationView.show(home, true);
    }
}