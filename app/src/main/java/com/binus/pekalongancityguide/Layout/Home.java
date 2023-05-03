package com.binus.pekalongancityguide.Layout;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.R;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;

public class Home extends AppCompatActivity {
    private static final int home = 1;
    HomeFragment homeFragment = new HomeFragment();
    DestinationFragment destinationFragment = new DestinationFragment();
    ConversationFragment conversationFragment = new ConversationFragment();
    BookmarkFragment bookmarkFragment = new BookmarkFragment();
    ProfileFragment profileFragment = new ProfileFragment();
    AddItinerary addItinerary = new AddItinerary();
    private static final int desti = 2;
    private static final int iter = 3;
    private static final int bm = 4;
    private static final int convo = 5;
    private static final int pr = 6;
    MeowBottomNavigation bottomNavigationView;
    private boolean doubleTap = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        bottomNavigationView = findViewById(R.id.bottom_navi);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
        bottomNavigationView.add(new MeowBottomNavigation.Model(home, R.drawable.ic_home));
        bottomNavigationView.add(new MeowBottomNavigation.Model(desti, R.drawable.destination));
        bottomNavigationView.add(new MeowBottomNavigation.Model(iter, R.drawable.route));
        bottomNavigationView.add(new MeowBottomNavigation.Model(bm, R.drawable.remove_bookmark));
        bottomNavigationView.add(new MeowBottomNavigation.Model(convo, R.drawable.chat));
        bottomNavigationView.add(new MeowBottomNavigation.Model(pr, R.drawable.profile));

        bottomNavigationView.setOnShowListener(item -> {
            String name;
            switch (item.getId()) {
                case home:
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commitAllowingStateLoss();
                    break;

                case desti:
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, destinationFragment).commitAllowingStateLoss();
                    break;
                case iter:
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, addItinerary).commitAllowingStateLoss();
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
        });

        bottomNavigationView.setOnClickMenuListener(item -> {
        });

        bottomNavigationView.setOnReselectListener(item -> {
        });
        bottomNavigationView.show(home, true);
    }
    @Override
    public void onBackPressed() {
        if (doubleTap) {
            super.onBackPressed();
            return;
        }
        this.doubleTap = true;
        Toast.makeText(this,R.string.press_back, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleTap = false;
            }
        }, 2000);
    }
}