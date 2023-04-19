package com.binus.pekalongancityguide.Layout;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.binus.pekalongancityguide.Misc.ImageFullscreen;
import com.binus.pekalongancityguide.Misc.MyApplication;
import com.binus.pekalongancityguide.databinding.FragmentProfileBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;
import java.util.Objects;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private Locale currentLocale = Locale.getDefault();
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "PROFILE_TAG";
    private String mProfileImgUrl;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        firebaseAuth = FirebaseAuth.getInstance();
        getInfo();
        binding.profileImg.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ImageFullscreen.class);
            intent.putExtra("fullImg", mProfileImgUrl);
            startActivity(intent);
        });
        binding.changePass.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ChangePassword.class));
        });
        binding.editName.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfile.class));
        });
        binding.logoutBtn.setOnClickListener(v -> {
            logoutConfirm();
        });

        binding.changeLang.setOnClickListener(v -> {
            Locale currentLocale = getResources().getConfiguration().locale;

            // Set the new locale based on the current locale
            Locale newLocale = currentLocale.equals(Locale.getDefault())
                    ? new Locale("id", "ID") // Indonesian locale
                    : Locale.getDefault(); // Default locale

            // Update the app's configuration to use the new locale
            Configuration config = new Configuration(getResources().getConfiguration());
            config.setLocale(newLocale);
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());

            // Restart the activity to apply the language changes
            getActivity().recreate();
        });

        binding.showItineraryBtn.setOnClickListener(v -> {
            Intent showIniterary = new Intent(getActivity(), ItineraryList.class);
            startActivity(showIniterary);
        });
        return view;
    }

//    public void showLanguageDialog() {
//        Context context = getContext();
//        if (context != null) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(context);
//            builder.setTitle("Select Language")
//                    .setItems(new CharSequence[]{"Default", "Indonesian"}, (dialog, which) -> {
//                        if (which == 0) {
//                            setLocale("", context);
//                        } else if (which == 1) {
//                            setLocale("id", context);
//                        }
//                    });
//            AlertDialog dialog = builder.create();
//            dialog.show();
//        }
//    }


//    private void setLocale(String languageCode, Context context) {
//        Locale locale = new Locale(languageCode);
//        Resources resources = context.getResources();
//        Configuration configuration = resources.getConfiguration();
//        configuration.setLocale(locale);
//        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
//        // Refresh UI
//        Intent intent = new Intent(context, Home.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        context.startActivity(intent);
//    }


    private void logoutConfirm(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to log out?");
        builder.setPositiveButton("Yes", (dialog, which) ->{
            firebaseAuth.signOut();
            checkUser();
        } );
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
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
        r.child(Objects.requireNonNull(firebaseAuth.getUid()))
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
                        mProfileImgUrl = profile_img;
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}