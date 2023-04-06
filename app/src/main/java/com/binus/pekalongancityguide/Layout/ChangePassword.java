package com.binus.pekalongancityguide.Layout;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.databinding.ActivityChangePasswordBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {
    private ActivityChangePasswordBinding binding;
    private String editPass
            = "", cfmeditPass
            = "", currPass = "";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.updatePass.setOnClickListener(v -> {
            validate();
        });
        binding.backtoProfile.setOnClickListener(v -> {
            onBackPressed();
        });
        setupListeners();
    }

    void validate() {
        currPass = binding.currPass.getText().toString().trim();
        editPass = binding.editPass.getText().toString().trim();
        cfmeditPass = binding.cfmeditPass.getText().toString().trim();
        boolean allFieldsValid = true;

        if (TextUtils.isEmpty(currPass)) {
            binding.currentpassTil.setPasswordVisibilityToggleEnabled(false);
            binding.currPass.setError("Enter current password!");
            allFieldsValid = false;
        } else if (currPass.length() < 8) {
            binding.currentpassTil.setPasswordVisibilityToggleEnabled(false); // remove toggle
            binding.currPass.setError("Password must be more than 8 characters!");
            allFieldsValid = false;
        } else if (!containsNumber(currPass)) {
            binding.currentpassTil.setPasswordVisibilityToggleEnabled(false); // remove toggle
            binding.currPass.setError("Password must contain at least one number!");
            allFieldsValid = false;
        } else if (!containsSymbol(currPass)) {
            binding.currentpassTil.setPasswordVisibilityToggleEnabled(false); // remove toggle
            binding.currPass.setError("Password must contain at least one symbol!");
            allFieldsValid = false;
        } else {
            binding.currentpassTil.setPasswordVisibilityToggleEnabled(true);
            binding.currPass.setError(null);
        }

        if (TextUtils.isEmpty(editPass)) {
            binding.editpassTil.setPasswordVisibilityToggleEnabled(false); // remove toggle
            binding.editPass.setError("Enter new password!");
            allFieldsValid = false;
        } else if (editPass.equals(currPass)) {
            binding.editpassTil.setPasswordVisibilityToggleEnabled(false); // remove toggle
            binding.editPass.setError("Your new password cannot be the same as your current password!");
            allFieldsValid = false;
        } else if (editPass.length() < 8) {
            binding.editpassTil.setPasswordVisibilityToggleEnabled(false); // remove toggle
            binding.editPass.setError("Password must be more than 8 characters!");
            allFieldsValid = false;
        } else if (!containsNumber(editPass)) {
            binding.editpassTil.setPasswordVisibilityToggleEnabled(false); // remove toggle
            binding.editPass.setError("Password must contain at least one number!");
            allFieldsValid = false;
        } else if (!containsSymbol(editPass)) {
            binding.editpassTil.setPasswordVisibilityToggleEnabled(false); // remove toggle
            binding.editPass.setError("Password must contain at least one symbol!");
            allFieldsValid = false;
        } else {
            binding.editpassTil.setPasswordVisibilityToggleEnabled(true); // show toggle
            binding.editPass.setError(null);
        }

        if (TextUtils.isEmpty(cfmeditPass)) {
            binding.editcfmpassTil.setPasswordVisibilityToggleEnabled(false); // remove toggle
            binding.cfmeditPass.setError("Enter confirm password!");
            allFieldsValid = false;
        } else if (!cfmeditPass.equals(editPass)) {
            binding.editcfmpassTil.setPasswordVisibilityToggleEnabled(false); // remove toggle
            binding.cfmeditPass.setError("Password does not match!");
            allFieldsValid = false;
        } else {
            binding.editcfmpassTil.setPasswordVisibilityToggleEnabled(true); // show toggle
            binding.cfmeditPass.setError(null);
        }

        if (allFieldsValid) {
            updatePass();
        }
    }

    private boolean containsNumber(String password) {
        return password.matches(".*\\d.*");
    }

    private boolean containsSymbol(String password) {
        return password.matches(".*[!@#$%^&*()].*");
    }

    private void setupListeners() {
        binding.editPass.setOnFocusChangeListener((view, hasFocus) -> {
            binding.editpassTil.setPasswordVisibilityToggleEnabled(hasFocus);
        });
        binding.cfmeditPass.setOnFocusChangeListener((view, hasFocus) -> {
            binding.editcfmpassTil.setPasswordVisibilityToggleEnabled(hasFocus);
        });
    }

    private void updatePass() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String password = currPass;
            AuthCredential credential = EmailAuthProvider.getCredential(email, password);

            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.updatePassword(editPass
                                    )
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                            Log.e("ChangePassword", "Password update failed: " + task1.getException().getMessage());
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Failed to re-authenticate user", Toast.LENGTH_SHORT).show();
                            Log.e("ChangePassword", "Re-authentication failed: " + task.getException().getMessage());
                        }
                    });
        }
    }


}