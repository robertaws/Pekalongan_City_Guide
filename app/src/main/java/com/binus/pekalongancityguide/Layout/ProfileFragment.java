package com.binus.pekalongancityguide.Layout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.binus.pekalongancityguide.Misc.MyApplication;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.FragmentProfileBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {
    Button logout;
    private FragmentProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "PROFILE_TAG";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
         View view = binding.getRoot();
        logout = view.findViewById(R.id.logout_btn);

        firebaseAuth = FirebaseAuth.getInstance();
        getInfo();

        binding.editName.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfile.class));
        });
        binding.logoutBtn.setOnClickListener(v -> {
            firebaseAuth.signOut();
            checkUser();
        });

        return view;
    }

    private void checkUser(){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            startActivity(new Intent(getActivity(),MainActivity.class));
            getActivity().finish();
        }
    }

    private void getInfo(){
        Log.e(TAG,"Loading User Info..."+firebaseAuth.getUid());
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference r = database.getReference("Users");
        r.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = ""+snapshot.child("Email").getValue();
                        String name = ""+snapshot.child("Username").getValue();
                        String profile_img = "" + snapshot.child("profileImage").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String userId = "" + snapshot.child("uid").getValue();
                        String formatDate = MyApplication.formatTimeStamp(Long.parseLong(timestamp));
                        String type = "" + snapshot.child("userType").getValue();

                        binding.profileEmail.setText(email);
                        binding.profileUser.setText(name);
                        binding.profileJoined.setText(formatDate);
                        binding.profileType.setText(type);
                        if (isAdded()) {
                            String imageUrl = snapshot.child("profileImage").getValue(String.class);
                            if (imageUrl != null) {
                                Glide.with(ProfileFragment.this)
                                        .load(profile_img)
                                        .centerCrop()
                                        .into(binding.profileImg);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}