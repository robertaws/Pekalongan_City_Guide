package com.binus.pekalongancityguide.Layout;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.R;
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
            binding.currPass.setError(getString(R.string.enter_cur_pass));
            allFieldsValid = false;
        } else if (currPass.length() < 8) {
            binding.currentpassTil.setPasswordVisibilityToggleEnabled(false);
            binding.currPass.setError(getString(R.string.pass8chara));
            allFieldsValid = false;
        } else if (!containsNumber(currPass)) {
            binding.currentpassTil.setPasswordVisibilityToggleEnabled(false);
            binding.currPass.setError(getString(R.string.pass1num));
            allFieldsValid = false;
        } else if (!containsSymbol(currPass)) {
            binding.currentpassTil.setPasswordVisibilityToggleEnabled(false);
            binding.currPass.setError(getString(R.string.pass1Symbol));
            allFieldsValid = false;
        } else {
            binding.currentpassTil.setPasswordVisibilityToggleEnabled(true);
            binding.currPass.setError(null);
        }
        if (TextUtils.isEmpty(editPass)) {
            binding.editpassTil.setPasswordVisibilityToggleEnabled(false);
            binding.editPass.setError(getString(R.string.enternewPass));
            allFieldsValid = false;
        } else if (editPass.equals(currPass)) {
            binding.editpassTil.setPasswordVisibilityToggleEnabled(false);
            binding.editPass.setError(getString(R.string.passcantSame));
            allFieldsValid = false;
        } else if (editPass.length() < 8) {
            binding.editpassTil.setPasswordVisibilityToggleEnabled(false);
            binding.editPass.setError(getString(R.string.pass8chara));
            allFieldsValid = false;
        } else if (!containsNumber(editPass)) {
            binding.editpassTil.setPasswordVisibilityToggleEnabled(false);
            binding.editPass.setError(getString(R.string.pass1num));
            allFieldsValid = false;
        } else if (!containsSymbol(editPass)) {
            binding.editpassTil.setPasswordVisibilityToggleEnabled(false);
            binding.editPass.setError(getString(R.string.pass1Symbol));
            allFieldsValid = false;
        } else {
            binding.editpassTil.setPasswordVisibilityToggleEnabled(true);
            binding.editPass.setError(null);
        }

        if (TextUtils.isEmpty(cfmeditPass)) {
            binding.editcfmpassTil.setPasswordVisibilityToggleEnabled(false);
            binding.cfmeditPass.setError(getString(R.string.cfmpassEnter));
            allFieldsValid = false;
        } else if (!cfmeditPass.equals(editPass)) {
            binding.editcfmpassTil.setPasswordVisibilityToggleEnabled(false);
            binding.cfmeditPass.setError(getString(R.string.passnotMatch));
            allFieldsValid = false;
        } else {
            binding.editcfmpassTil.setPasswordVisibilityToggleEnabled(true);
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
                                            Toast.makeText(this,R.string.pass_updated_txt, Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(this,R.string.failed_update_pass, Toast.LENGTH_SHORT).show();
                                            Log.e("ChangePassword", "Password update failed: " + task1.getException().getMessage());
                                        }
                                    });
                        } else {
                            Toast.makeText(this, R.string.failed_authentic, Toast.LENGTH_SHORT).show();
                            Log.e("ChangePassword", "Re-authentication failed: " + task.getException().getMessage());
                        }
                    });
        }
    }


}