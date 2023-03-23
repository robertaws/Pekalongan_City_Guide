package com.binus.pekalongancityguide.Layout;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

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
        checkUser();
        bottomNavigationView = findViewById(R.id.bottom_navi);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
        bottomNavigationView.add(new chetanBottomNavigation.Model(home, R.drawable.ic_home));
        bottomNavigationView.add(new chetanBottomNavigation.Model(desti, R.drawable.destination));
        bottomNavigationView.add(new chetanBottomNavigation.Model(bm, R.drawable.bookmark));
        bottomNavigationView.add(new chetanBottomNavigation.Model(convo, R.drawable.chat));
        bottomNavigationView.add(new chetanBottomNavigation.Model(pr, R.drawable.profile));

        bottomNavigationView.setOnShowListener(new chetanBottomNavigation.ShowListener() {
            @Override
            public void onShowItem(chetanBottomNavigation.Model item) {
                String name;
                switch (item.getId()) {
                    case home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commit();
                        break;

                    case desti:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, destinationFragment).commit();
                        break;

                    case bm:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, bookmarkFragment).commit();
                        break;

                    case convo:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, conversationFragment).commit();
                        break;

                    case pr:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, profileFragment).commit();
                        break;

                    default:

                        break;
                }
            }
        });

        bottomNavigationView.setOnClickMenuListener(new chetanBottomNavigation.ClickListener() {
            @Override
            public void onClickItem(chetanBottomNavigation.Model item) {
//                Toast.makeText(Home.this, "item clicked", Toast.LENGTH_SHORT).show();
            }
        });

        bottomNavigationView.setOnReselectListener(new chetanBottomNavigation.ReselectListener() {
            @Override
            public void onReselectItem(chetanBottomNavigation.Model item) {
//                Toast.makeText(Home.this, "item reselected", Toast.LENGTH_SHORT).show();
            }
        });

        bottomNavigationView.show(home, true);
    }

    private void checkUser(){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            startActivity(new Intent(Home.this,MainActivity.class));
            finish();
        }
    }
}