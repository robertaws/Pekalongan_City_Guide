package com.binus.pekalongancityguide.Layout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    Button login, noLogin;
    EditText email, pass;
    TextInputLayout til,etil;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
    TextView register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseApp.initializeApp(this);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseDatabase.getInstance().getReference().keepSynced(true);

        DatabaseReference myRef = database.getReference("path/to/data");

        init();

        binding.noLogin.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Home.class)));
        binding.loginBtn.setOnClickListener(v -> {
            validate();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        });

        binding.mainRegis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "mainRegis is not null: " + (binding.mainRegis != null));
                Intent regisIntent = new Intent(MainActivity.this, Register.class);
                startActivity(regisIntent);
            }
        });

        til.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // The user has clicked on the text input layout
                til.setPasswordVisibilityToggleEnabled(true);
            } else {
                // The user has left the text input layout
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
        register = findViewById(R.id.main_regis);
        etil = findViewById(R.id.loginemail_til);
    }
    void validate() {
        Email = binding.loginEmail.getText().toString().trim();
        Password = binding.loginPass.getText().toString().trim();
        if (Email.isEmpty()) {
            email.setError("All field must not be empty!");
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            email.setError("Invalid Email Address!");
        }
        if (Password.isEmpty()) {
            pass.setError("All field must not be empty!");
            til.setPasswordVisibilityToggleEnabled(false);
        } else {
            tryLogin();
        }
    }

    private void tryLogin() {
        progressDialog.setMessage("Logging in..");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(Email,Password)
                .addOnSuccessListener(authResult -> checkUser())
                .addOnFailureListener(e -> {
                    Snackbar.make(binding.getRoot(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                        }
                    }, 2000);
                });
    }

    private void checkUser() {
        progressDialog.setMessage("Checking user..");
        FirebaseUser firebaseUser =firebaseAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference databaseReference = database.getReference("Users");
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