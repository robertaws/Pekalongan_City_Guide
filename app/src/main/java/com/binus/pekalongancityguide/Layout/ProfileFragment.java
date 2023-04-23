package com.binus.pekalongancityguide.Layout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.binus.pekalongancityguide.Misc.ImageFullscreen;
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

import java.util.Locale;
import java.util.Objects;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private Locale currentLocale = Locale.getDefault();
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "PROFILE_TAG";
    private String mProfileImgUrl;
    private SharedPreferences prefs;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        firebaseAuth = FirebaseAuth.getInstance();
        getInfo();
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String selectedLanguage = prefs.getString("language", "");
        if(selectedLanguage.equals("in")){
            binding.langText.setText(R.string.indo_opt);
        } else {
            binding.langText.setText(R.string.english_opt);
        }

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

        binding.editLang.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.select_language)
                    .setItems(new CharSequence[]{getString(R.string.english_opt), getString(R.string.indo_opt)}, (dialog, which) -> {
                        Locale newLocale;
                        if (which == 0) {
                            newLocale = new Locale("en","US");
                        } else {
                            newLocale = new Locale("id", "ID");
                        }
                        Locale currentLocale = getResources().getConfiguration().locale;
                        if (!currentLocale.equals(newLocale)) {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                            preferences.edit().putString("language", newLocale.getLanguage()).apply();
                            Configuration config = new Configuration(getResources().getConfiguration());
                            config.setLocale(newLocale);
                            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
                            Log.d("Language", "Language configuration set to " + newLocale.getDisplayLanguage());
                            getActivity().recreate();
                            Log.d("Language", "Activity recreated");
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        binding.showItineraryBtn.setOnClickListener(v -> {
            Intent showIniterary = new Intent(getActivity(), ItineraryList.class);
            startActivity(showIniterary);
        });
        return view;
    }
    private void logoutConfirm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.logout_text);
        builder.setMessage(R.string.logout_confirm);
        builder.setPositiveButton(R.string.yes_txt, (dialog, which) -> {
            firebaseAuth.signOut();
            checkUser();
        });
        builder.setNegativeButton(R.string.no_txt, (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.white));
            negativeButton.setTextColor(getResources().getColor(R.color.white));
        });
        dialog.show();
    }
    private void checkUser(){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            startActivity(new Intent(getActivity(),MainActivity.class));
            Toast.makeText(getContext(),R.string.notLogin, Toast.LENGTH_SHORT).show();
        }
    }

    private void getInfo(){
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");
        Log.e(TAG,"Loading User Info..."+firebaseAuth.getUid());
        if (firebaseAuth.getUid() != null) { // add null check here
            DatabaseReference r = database.getReference("Users");
            r.keepSynced(true);
            r.child(Objects.requireNonNull(firebaseAuth.getUid()))
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String email = ""+snapshot.child("Email").getValue();
                            String name = ""+snapshot.child("Username").getValue();
                            String profile_img = "" + snapshot.child("profileImage").getValue();
                            String timestamp = "" + snapshot.child("timestamp").getValue();
                            String formatDate = MyApplication.formatProfileDate(Long.parseLong(timestamp));
                            binding.profileEmail.setText(email);
                            binding.profileUser.setText(name);
                            binding.profileJoined.setText(formatDate);
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
}