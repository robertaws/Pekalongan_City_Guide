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
import android.widget.Toast;

import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    Button login,noLogin;
    EditText email,pass;
    TextInputLayout til;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        init();

        binding.noLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Home.class));
            }
        });
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });
    }

    String Email, Password;
    void init(){
        login = findViewById(R.id.login_btn);
        noLogin = findViewById(R.id.no_login);
        email = findViewById(R.id.login_email);
        pass = findViewById(R.id.login_pass);
        til = findViewById(R.id.loginpass_til);
        firebaseAuth = FirebaseAuth.getInstance();
    }
    void validate(){
        Email = binding.loginEmail.getText().toString().trim();
        Password = binding.loginPass.getText().toString().trim();
        if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
            Toast.makeText(this, "Invalid Email Address!", Toast.LENGTH_SHORT).show();
        }else if(Email.isEmpty() || Password.isEmpty()){
            Toast.makeText(this, "All field must not be empty!", Toast.LENGTH_SHORT).show();
        }else{
            tryLogin();
        }
    }

    private void tryLogin() {
        progressDialog.setMessage("Logging in..");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(Email,Password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        progressDialog.setMessage("Checking user..");
        FirebaseUser firebaseUser =firebaseAuth.getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        String userType =""+snapshot.child("userType").getValue();
                        if(userType.equals("user")){
                            startActivity(new Intent(MainActivity.this,Home.class));
                            finish();
                        }else if(userType.equals("admin")){
                            startActivity(new Intent(MainActivity.this,Home.class));
                            finish();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
    }
}