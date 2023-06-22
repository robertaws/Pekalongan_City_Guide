package com.binus.pekalongancityguide.Layout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.Model.User;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityRegisterBinding;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import static com.binus.pekalongancityguide.Misc.Constants.FIREBASE_DATABASE_URL;

public class Register extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    ImageButton back;
    EditText user, email, pass, cpass;
    TextInputLayout til, ctil, etil, util;
    Button register;
    ProgressDialog progressDialog;
    User userModel;
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
        EMAIL_FORMAT_ERROR = getString(R.string.wrongFormatEmail);
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
        setHelper();
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

    private void init() {
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

    private void setHelper() {
        user.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && s.length() < 3 || s.length() > 12) {
                    util.setHelperText(getString(R.string.user_helper));
                } else {
                    util.setHelperText(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && !Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    etil.setHelperText(getString(R.string.email_helper));
                } else {
                    etil.setHelperText(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Password = binding.regisPass.getText().toString().trim();
                boolean isValidPassword = s.length() >= 8 && containsNumber(Password) && containsSymbol(Password);
                if (s.length() > 0 && !isValidPassword) {
                    til.setHelperText(getString(R.string.pass_helper));
                } else {
                    til.setHelperText(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        cpass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Password = binding.regisPass.getText().toString().trim();
                String confirmPassword = s.toString().trim();
                boolean isValidPassword = confirmPassword.equals(Password);

                if (confirmPassword.length() > 0 && !isValidPassword) {
                    ctil.setHelperText(PASSWORD_MATCH_ERROR);
                } else {
                    ctil.setHelperText(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void validate() {
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
            til.setPasswordVisibilityToggleEnabled(false);
            pass.setError(PASSWORD_EMPTY_ERROR);
            allFieldsValid = false;
        } else if (Password.length() < 8) {
            til.setPasswordVisibilityToggleEnabled(false);
            pass.setError(PASSWORD_LENGTH_ERROR);
            allFieldsValid = false;
        } else if (!containsNumber(Password)) {
            til.setPasswordVisibilityToggleEnabled(false);
            pass.setError(PASSWORD_NUMBER_ERROR);
            allFieldsValid = false;
        } else if (!containsSymbol(Password)) {
            til.setPasswordVisibilityToggleEnabled(false);
            pass.setError(PASSWORD_SYMBOL_ERROR);
            allFieldsValid = false;
        } else {
            til.setPasswordVisibilityToggleEnabled(true);
            pass.setError(null);
        }

        if (TextUtils.isEmpty(Cfmpass)) {
            ctil.setPasswordVisibilityToggleEnabled(false);
            cpass.setError(PASSWORD_EMPTY_ERROR);
            allFieldsValid = false;
        } else if (!Cfmpass.equals(Password)) {
            ctil.setPasswordVisibilityToggleEnabled(false);
            cpass.setError(PASSWORD_MATCH_ERROR);
            allFieldsValid = false;
        } else {
            ctil.setPasswordVisibilityToggleEnabled(true);
            cpass.setError(null);
        }

        if (allFieldsValid) {
            userModel = new User(Username, Email, Password);
            createUser(userModel);
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

    private void createUser(User userModel) {
        progressDialog.setMessage(getString(R.string.create_account));
        progressDialog.show();

        firebaseAuth.fetchSignInMethodsForEmail(Email)
                .addOnSuccessListener(signInMethodsResult -> {
                    boolean isNewUser = signInMethodsResult.getSignInMethods().isEmpty();
                    if (isNewUser) {
                        firebaseAuth.createUserWithEmailAndPassword(Email, Password)
                                .addOnSuccessListener(authResult -> {
                                    progressDialog.dismiss();
                                    addUser(userModel);
                                    Toast.makeText(this, R.string.suces_register, Toast.LENGTH_SHORT).show();
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

    private void addUser(User userModel) {
        progressDialog.setMessage(getString(R.string.saving_user_info));
        long timestamp = System.currentTimeMillis();
        String uid = firebaseAuth.getUid();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("Email", userModel.getEmail());
        hashMap.put("Username", userModel.getUsername());
        hashMap.put("profileImage", "");
        hashMap.put("userType", "user");
        hashMap.put("timestamp", timestamp);
        FirebaseDatabase database = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL);
        DatabaseReference databaseReference = database.getReference("Users");
        databaseReference.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(Register.this, R.string.acount_created, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Register.this, Login.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(Register.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
