package com.binus.pekalongancityguide.Layout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityRegisterBinding;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    ImageButton back;
    EditText user, email, pass, cpass;
    TextInputLayout til, ctil, etil, util;
    Button register;
    ProgressDialog progressDialog;
    private ActivityRegisterBinding binding;

    private String USERNAME_EMPTY_ERROR;
    private String USERNAME_LENGTH_ERROR;
    private String EMAIL_EMPTY_ERROR;
    private String EMAIL_FORMAT_ERROR;
    private String PASSWORD_EMPTY_ERROR;
    private String PASSWORD_LENGTH_ERROR;
    private String PASSWORD_NUMBER_ERROR;
    private String PASSWORD_SYMBOL_ERROR;
    private String PASSWORD_MATCH_ERROR;
    private String EMAIL_ALREADY_USED_ERROR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        USERNAME_EMPTY_ERROR = getString(R.string.empty_username);
        USERNAME_LENGTH_ERROR = getString(R.string.user_length);
        EMAIL_EMPTY_ERROR = getString(R.string.empty_email);
        EMAIL_FORMAT_ERROR = getString(R.string.wrong_email);
        PASSWORD_EMPTY_ERROR = getString(R.string.empty_pass);
        PASSWORD_LENGTH_ERROR = getString(R.string.pass_length);
        PASSWORD_NUMBER_ERROR = getString(R.string.pass_1num);
        PASSWORD_SYMBOL_ERROR = getString(R.string.pass1Symbol);
        PASSWORD_MATCH_ERROR = getString(R.string.passnotMatch);
        EMAIL_ALREADY_USED_ERROR = getString(R.string.used_email);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.please_wait);
        progressDialog.setCanceledOnTouchOutside(false);

        init();

        back.setOnClickListener(v -> onBackPressed());

        binding.regisBtn.setOnClickListener(v -> validate());
        til.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                til.setPasswordVisibilityToggleEnabled(true);
            } else {
            }
        });

        ctil.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ctil.setPasswordVisibilityToggleEnabled(true);
            } else {
            }
        });

    }
    String Username, Email, Password, Cfmpass;
    void init(){
        back = findViewById(R.id.backtoLogin);
        user = findViewById(R.id.regis_user);
        email = findViewById(R.id.regis_email);
        pass = findViewById(R.id.regis_pass);
        til = findViewById(R.id.regispass_til);
        register = findViewById(R.id.regis_btn);
        cpass = findViewById(R.id.regis_cpass);
        ctil = findViewById(R.id.regiscpass_til);
        etil = findViewById(R.id.regisemail_til);
        util = findViewById(R.id.regisuser_til);
        firebaseAuth = FirebaseAuth.getInstance();
        setupListeners();
    }

    void validate() {
        String USERNAME_EMPTY_ERROR = getString(R.string.empty_username);
        String USERNAME_LENGTH_ERROR = getString(R.string.user_length);
        String EMAIL_EMPTY_ERROR = getString(R.string.empty_email);
        String EMAIL_FORMAT_ERROR = getString(R.string.wrong_email);
        String PASSWORD_EMPTY_ERROR = getString(R.string.empty_pass);
        String PASSWORD_LENGTH_ERROR = getString(R.string.pass_length);
        String PASSWORD_NUMBER_ERROR = getString(R.string.pass_1num);
        String PASSWORD_SYMBOL_ERROR = getString(R.string.pass1Symbol);
        String PASSWORD_MATCH_ERROR = getString(R.string.passnotMatch);
        Username = binding.regisUser.getText().toString().trim();
        Email = binding.regisEmail.getText().toString().trim();
        Password = binding.regisPass.getText().toString().trim();
        Cfmpass = binding.regisCpass.getText().toString().trim();
        boolean allFieldsValid = true;

        if (TextUtils.isEmpty(Username)) {
            binding.regisUser.setError(USERNAME_EMPTY_ERROR);
            allFieldsValid = false;
        } else if (Username.length() < 3 || Username.length() > 12) {
            binding.regisUser.setError(USERNAME_LENGTH_ERROR);
            allFieldsValid = false;
        } else {
            binding.regisUser.setError(null);
        }

        if (TextUtils.isEmpty(Email)) {
            binding.regisEmail.setError(EMAIL_EMPTY_ERROR);
            allFieldsValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            binding.regisEmail.setError(EMAIL_FORMAT_ERROR);
            allFieldsValid = false;
        } else {
            binding.regisEmail.setError(null);
        }

        if (TextUtils.isEmpty(Password)) {
            til.setPasswordVisibilityToggleEnabled(false); // remove toggle
            pass.setError(PASSWORD_EMPTY_ERROR);
            allFieldsValid = false;
        } else if (Password.length() < 8) {
            til.setPasswordVisibilityToggleEnabled(false); // remove toggle
            pass.setError(PASSWORD_LENGTH_ERROR);
            allFieldsValid = false;
        } else if (!containsNumber(Password)) {
            til.setPasswordVisibilityToggleEnabled(false); // remove toggle
            pass.setError(PASSWORD_NUMBER_ERROR);
            allFieldsValid = false;
        } else if (!containsSymbol(Password)) {
            til.setPasswordVisibilityToggleEnabled(false); // remove toggle
            pass.setError(PASSWORD_SYMBOL_ERROR);
            allFieldsValid = false;
        } else {
            til.setPasswordVisibilityToggleEnabled(true); // show toggle
            pass.setError(null);
        }

        if (TextUtils.isEmpty(Cfmpass)) {
            ctil.setPasswordVisibilityToggleEnabled(false); // remove toggle
            cpass.setError(PASSWORD_EMPTY_ERROR);
            allFieldsValid = false;
        } else if (!Cfmpass.equals(Password)) {
            ctil.setPasswordVisibilityToggleEnabled(false); // remove toggle
            cpass.setError(PASSWORD_MATCH_ERROR);
            allFieldsValid = false;
        } else {
            ctil.setPasswordVisibilityToggleEnabled(true); // show toggle
            cpass.setError(null);
        }

        if (allFieldsValid) {
            createUser();
        }
    }

    private boolean containsNumber(String password) {
        return password.matches(".*\\d.*");
    }

    private boolean containsSymbol(String password) {
        return password.matches(".*[!@#$%^&*()].*");
    }

    private void setupListeners() {
        pass.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                validate();
            }
        });
        cpass.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                validate();
            }
        });

        til.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            til.setPasswordVisibilityToggleEnabled(hasFocus);
        });

        ctil.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            ctil.setPasswordVisibilityToggleEnabled(hasFocus);
        });

    }
    private void createUser() {
        progressDialog.setMessage(getString(R.string.create_account));
        progressDialog.show();
        firebaseAuth.fetchSignInMethodsForEmail(Email)
                .addOnSuccessListener(signInMethodsResult -> {
                    boolean isNewUser = signInMethodsResult.getSignInMethods().isEmpty();
                    if (isNewUser) {
                        firebaseAuth.createUserWithEmailAndPassword(Email, Password)
                                .addOnSuccessListener(authResult -> {
                                    progressDialog.dismiss();
                                    addUser();
                                    Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(Register.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        progressDialog.dismiss();
                        binding.regisEmail.setError(EMAIL_ALREADY_USED_ERROR);
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(Register.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void addUser() {
        progressDialog.setMessage(getString(R.string.saving_user_info));
        long timestamp = System.currentTimeMillis();
        String uid = firebaseAuth.getUid();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("Email",Email);
        hashMap.put("Username",Username);
        hashMap.put("profileImage","");
        hashMap.put("userType","user");
        hashMap.put("timestamp", timestamp);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference databaseReference = database.getReference("Users");
        databaseReference.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(Register.this, "Account created!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Register.this,MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(Register.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}