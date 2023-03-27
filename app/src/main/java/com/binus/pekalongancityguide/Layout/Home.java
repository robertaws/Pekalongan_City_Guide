package com.binus.pekalongancityguide.Layout;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.R;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.firebase.auth.FirebaseAuth;

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
    MeowBottomNavigation bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        bottomNavigationView = findViewById(R.id.bottom_navi);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
        bottomNavigationView.add(new MeowBottomNavigation.Model(home, R.drawable.ic_home));
        bottomNavigationView.add(new MeowBottomNavigation.Model(desti, R.drawable.destination));
        bottomNavigationView.add(new MeowBottomNavigation.Model(bm, R.drawable.remove_bookmark));
        bottomNavigationView.add(new MeowBottomNavigation.Model(convo, R.drawable.chat));
        bottomNavigationView.add(new MeowBottomNavigation.Model(pr, R.drawable.profile));

        bottomNavigationView.setOnShowListener(new MeowBottomNavigation.ShowListener() {
            @Override
            public void onShowItem(MeowBottomNavigation.Model item) {
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

        bottomNavigationView.setOnClickMenuListener(new MeowBottomNavigation.ClickListener() {
            @Override
            public void onClickItem(MeowBottomNavigation.Model item) {
            }
        });

        bottomNavigationView.setOnReselectListener(new MeowBottomNavigation.ReselectListener() {
            @Override
            public void onReselectItem(MeowBottomNavigation.Model item) {
            }
        });
        bottomNavigationView.show(home, true);
    }
}