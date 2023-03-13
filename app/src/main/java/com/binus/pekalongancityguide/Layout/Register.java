package com.binus.pekalongancityguide.Layout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    ImageButton back;
    EditText user,email,pass,cpass;
    TextInputLayout til,ctil;
    Button register;
    ProgressDialog progressDialog;
    private ActivityRegisterBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        init();
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
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
    }

    String Username = binding.regisUser.getText().toString().trim();
    String Email = binding.regisEmail.getText().toString().trim();
    String Password = binding.regisPass.getText().toString().trim();
    String Cfmpass = binding.regisCpass.getText().toString().trim();
    void init(){
        back = findViewById(R.id.backtoLogin);
        user = findViewById(R.id.regis_user);
        email = findViewById(R.id.regis_email);
        pass = findViewById(R.id.regis_pass);
        til = findViewById(R.id.regispass_til);
        register = findViewById(R.id.regis_btn);
        cpass = findViewById(R.id.regis_cpass);
        ctil = findViewById(R.id.regiscpass_til);
        firebaseAuth = FirebaseAuth.getInstance();
    }
    void validate(){
        if(Username.isEmpty() || Email.isEmpty() || Password.isEmpty()){
            Toast.makeText(this, "All field must not be empty!", Toast.LENGTH_SHORT).show();
        }else if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
            Toast.makeText(this, "Invalid email address!", Toast.LENGTH_SHORT).show();
        }else if(!Password.equals(Cfmpass)){
            Toast.makeText(this, "Password doesn't match!", Toast.LENGTH_SHORT).show();
        }else{
            createUser();
        }
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