package com.binus.pekalongancityguide.Layout;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityAddCategoryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import static com.binus.pekalongancityguide.Misc.Constants.FIREBASE_DATABASE_URL;

public class AddCategory extends AppCompatActivity {
    private ActivityAddCategoryBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setTitle("Please Wait");
        dialog.setCanceledOnTouchOutside(false);
        binding.backAdmin.setOnClickListener(v -> onBackPressed());
        binding.submitBtn.setOnClickListener(v -> {
            validateData();
        });
    }
    private String category = "";
    private void validateData() {
        category = binding.categoryEt.getText().toString().trim();
        if(TextUtils.isEmpty(category)){
            Toast.makeText(this, R.string.enterCat, Toast.LENGTH_SHORT).show();
        }else{
            DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Categories");
            Query query = reference.orderByChild("category");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean categoryExists = false;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String existingCategory = dataSnapshot.child("category").getValue(String.class);
                        if (existingCategory.equalsIgnoreCase(category)) {
                            categoryExists = true;
                            break;
                        }
                    }
                    if (categoryExists) {
                        Toast.makeText(AddCategory.this, category + getString(R.string.hasAdded), Toast.LENGTH_SHORT).show();
                    } else {
                        addCategoryFirebase();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


    private void addCategoryFirebase() {
        dialog.setMessage(getString(R.string.addingCat));
        dialog.show();
        long timestamp = System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("category",""+category);
        hashMap.put("timestamp",timestamp);
        hashMap.put("uid",""+firebaseAuth.getUid());

        DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Categories");
        reference.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    dialog.dismiss();
                    onBackPressed();
                    Toast.makeText(AddCategory.this,R.string.catSuccess, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(AddCategory.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}