package com.binus.pekalongancityguide.Layout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    ImageButton back;
    EditText user, email, pass, cpass;
    String Username, Email, Password, Cfmpass;
    TextInputLayout til, ctil, etil, util;
    Button register;
    ProgressDialog progressDialog;
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        init();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.regisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });
        til.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // The user has clicked on the text input layout
                    til.setPasswordVisibilityToggleEnabled(true);
                } else {
                    // The user has left the text input layout
                }
            }
        });

        ctil.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // The user has clicked on the text input layout
                    ctil.setPasswordVisibilityToggleEnabled(true);
                } else {
                    // The user has left the text input layout
                }
            }
        });

    }


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

        Username = binding.regisUser.getText().toString().trim();
        Email = binding.regisEmail.getText().toString().trim();
        Password = binding.regisPass.getText().toString().trim();
        Cfmpass = binding.regisCpass.getText().toString().trim();
    }
    void validate(){
        boolean isEmpty = false;
        EditText[] textFields = new EditText[] {user, email, pass, cpass};

        for (EditText textfield : textFields) {
            if (TextUtils.isEmpty(textfield.getText().toString())) {
                isEmpty = true;
                textfield.setError("All fields must not be empty!");
                if (textfield == pass || textfield == cpass) {
                    til.setPasswordVisibilityToggleEnabled(false);
                    ctil.setPasswordVisibilityToggleEnabled(false);
                }
            }
        }if (isEmpty) {
            return;
        }else if(Username.length()<3 || Username.length()>12){
            user.setError("Username must be between 3-12 characters!");
        }else if(!checkPass(Password)){
                pass.setError("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character.");
        } else if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
            email.setError("Invalid Email Address!");
        }else if(!Password.equals(Cfmpass)){
            cpass.setError("Password doesn't match!");
        }else{
            createUser();
        }
    }
    public boolean checkPass(String Password) {
        boolean hasUppercase = !Password.equals(Password.toLowerCase());
        boolean hasLowercase = !Password.equals(Password.toUpperCase());
        boolean hasNumber = Password.matches(".*\\d.*");
        boolean hasSpecialChar = !Password.matches("[A-Za-z0-9]*");

        return (Password.length() >= 8 && hasUppercase && hasLowercase && hasNumber && hasSpecialChar);
    }

    private void createUser(){
        progressDialog.setMessage("Creating account...");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(Email,Password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        progressDialog.dismiss();
                        addUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Register.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addUser() {
        progressDialog.setMessage("Saving user info");
        long timestamp = System.currentTimeMillis();
        String uid = firebaseAuth.getUid();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("Email",Email);
        hashMap.put("Username",Username);
        hashMap.put("profileImage","");
        hashMap.put("userType","user");
        hashMap.put("timestamp", timestamp);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(Register.this, "Account created!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Register.this,Home.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Register.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}