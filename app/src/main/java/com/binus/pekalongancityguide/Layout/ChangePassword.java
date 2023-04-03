package com.binus.pekalongancityguide.Layout;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.binus.pekalongancityguide.Layout.MainActivity;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityChangePasswordBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {
    private ActivityChangePasswordBinding binding;
    private String newPass = "", cfmnewPass = "",currPass = "";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    binding.updatePass.setOnClickListener(v -> {
        validatedata();
    });
    }
    private void validatedata() {
        newPass = binding.editPass.getText().toString().trim();
        cfmnewPass = binding.cfmeditPass.getText().toString().trim();
        currPass = binding.currPass.getText().toString().trim();
        if(TextUtils.isEmpty(currPass)){
            binding.currPass.setError("Enter new password!");
        }else if(TextUtils.isEmpty(newPass)){
            binding.editPass.setError("Enter new password!");
        }else if(newPass.equals(currPass)){
            binding.editPass.setError("Your new password cannot be the same!");
        }else if (newPass.length() < 8) {
            binding.editPass.setError("Password must be more than 8 characters!");
        } else if (!newPass.matches(".*[0-9].*")) {
            binding.editPass.setError("Password must contain at least one number!");
        } else if (!newPass.matches(".*[!@#$%^&*()].*")) {
            binding.editPass.setError("Password must contain at least one symbol!");
        }else if(!cfmnewPass.equals(newPass)){
            binding.cfmeditPass.setError("Password does not match!");
        }else{
            updatePass();
        }
    }
    private void showCustomToast(String toastText) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast));

        TextView text = layout.findViewById(R.id.toastText);
        text.setText(toastText);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 50);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
    private void updatePass(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String password = currPass;
            AuthCredential credential = EmailAuthProvider.getCredential(email, password);

            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPass)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            showCustomToast("Password updated successfully");
                                            finish();
                                        } else {
                                            showCustomToast("Failed to update password");
                                            Log.e("ChangePassword", "Password update failed: " + task1.getException().getMessage());
                                        }
                                    });
                        } else {
                            showCustomToast("Failed to re-authenticate user");
                            Log.e("ChangePassword", "Re-authentication failed: " + task.getException().getMessage());
                        }
                    });
        }
    }


}