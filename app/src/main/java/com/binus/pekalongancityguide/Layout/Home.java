package com.binus.pekalongancityguide.Layout;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.binus.pekalongancityguide.R;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.firebase.auth.FirebaseAuth;

public class Home extends AppCompatActivity{
    private static final int home = 1;
    HomeFragment homeFragment = new HomeFragment();
    DestinationPager destinationPager = new DestinationPager();
    ConversationFragment conversationFragment = new ConversationFragment();
    BookmarkFragment bookmarkFragment = new BookmarkFragment();
    ProfileFragment profileFragment = new ProfileFragment();
    ItineraryList itineraryList = new ItineraryList();
    private static final int desti = 2;
    private static final int iter = 3;
    private static final int bm = 4;
    private static final int convo = 5;
    private static final int pr = 6;
    FirebaseAuth firebaseAuth;
    MeowBottomNavigation bottomNavigationView;
    private boolean doubleTap = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        firebaseAuth = FirebaseAuth.getInstance();
        bottomNavigationView = findViewById(R.id.bottom_navi);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
        bottomNavigationView.add(new MeowBottomNavigation.Model(home, R.drawable.ic_home));
        bottomNavigationView.add(new MeowBottomNavigation.Model(desti, R.drawable.destination));
        bottomNavigationView.add(new MeowBottomNavigation.Model(iter, R.drawable.route));
        bottomNavigationView.add(new MeowBottomNavigation.Model(bm, R.drawable.remove_bookmark));
        bottomNavigationView.add(new MeowBottomNavigation.Model(convo, R.drawable.chat));
        bottomNavigationView.add(new MeowBottomNavigation.Model(pr, R.drawable.profile));

        bottomNavigationView.setOnShowListener(item -> {
            switch (item.getId()) {
                case home:
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commitAllowingStateLoss();
                    break;

                case desti:
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, destinationPager).commitAllowingStateLoss();
                    break;
                case iter:
                    if (firebaseAuth.getCurrentUser() == null) {
                        Toast.makeText(Home.this, R.string.notLogin, Toast.LENGTH_SHORT).show();
                    } else {
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, itineraryList).commitAllowingStateLoss();
                    }
                    break;
                case bm:
                    if (firebaseAuth.getCurrentUser() == null) {
                        Toast.makeText(Home.this, R.string.notLogin, Toast.LENGTH_SHORT).show();
                    } else {
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, bookmarkFragment).commitAllowingStateLoss();
                    }
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
            switch (item.getId()) {
                case iter:
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, itineraryList).commitAllowingStateLoss();
                    break;
            }
        });
        bottomNavigationView.show(home, true);
    }
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int backStackEntryCount = fragmentManager.getBackStackEntryCount();
        if (backStackEntryCount > 0) {
            fragmentManager.popBackStack();
        } else {
            if (doubleTap) {
                super.onBackPressed();
                return;
            }
            this.doubleTap = true;
            Toast.makeText(this, R.string.press_back, Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> doubleTap = false, 2000);
        }
    }
}