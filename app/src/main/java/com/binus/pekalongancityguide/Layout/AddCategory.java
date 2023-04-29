package com.binus.pekalongancityguide.Layout;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.databinding.ActivityAddCategoryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AddCategory extends AppCompatActivity {
    private ActivityAddCategoryBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog dialog;
    private DatabaseReference categoriesRef;
    private ValueEventListener categoriesListener;

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

        categoriesRef = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Categories");
        categoriesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot){
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String existingCategory = dataSnapshot.child("category").getValue(String.class).toLowerCase();
                    if (category.toLowerCase().equals(existingCategory)) {
                        Toast.makeText(AddCategory.this, "Category already exists!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }
                }
                addCategoryFirebase();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
                Toast.makeText(AddCategory.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }
    private String category = "";
    private void validateData() {
        category = binding.categoryEt.getText().toString().trim();
        if(TextUtils.isEmpty(category)){
            Toast.makeText(this, "Please enter a category!", Toast.LENGTH_SHORT).show();
        }else{
            dialog.setMessage("Checking category...");
            dialog.show();
            categoriesRef.addListenerForSingleValueEvent(categoriesListener);
        }
    }

    private void addCategoryFirebase() {
        dialog.setMessage("Adding category...");
        dialog.show();
        long timestamp = System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("category",""+category);
        hashMap.put("timestamp",timestamp);
        hashMap.put("uid",""+firebaseAuth.getUid());

        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");
        reference.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    dialog.dismiss();
                    onBackPressed();
                    Toast.makeText(AddCategory.this, "Category Added Successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(AddCategory.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        categoriesRef.removeEventListener(categoriesListener);
    }
}
