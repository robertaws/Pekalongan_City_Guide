package com.binus.pekalongancityguide.Layout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.Adapter.CategoryAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Categories;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityAdminHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import static com.binus.pekalongancityguide.Misc.Constants.FIREBASE_DATABASE_URL;

public class AdminHome extends AppCompatActivity {
    private EditText categoryField;
    private String category = "";
    private ActivityAdminHomeBinding binding;
    private FirebaseAuth firebaseAuth;
    private ArrayList<Categories> categoriesArrayList;
    private CategoryAdapter categoryAdapter;
    private ProgressDialog dialog;
    private AlertDialog aDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        showCategory();
        dialog = new ProgressDialog(this);
        dialog.setTitle("Please Wait");
        dialog.setCanceledOnTouchOutside(false);
        binding.searchCat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    categoryAdapter.getFilter().filter(s);
                }catch(Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.logoutAdmin.setOnClickListener(v -> {
            firebaseAuth.signOut();
            checkUser();
        });
        binding.addCategory.setOnClickListener(v -> showCategoryDialog());
        binding.addLocFab.setOnClickListener(v -> startActivity(new Intent(AdminHome.this, AddDestination.class)));
    }

    private void showCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_comment, null);
        builder.setView(view);

        Button submitBtn = view.findViewById(R.id.addcomment_btn);
        TextView titleText = view.findViewById(R.id.dialog_title);
        categoryField = view.findViewById(R.id.comment_et);

        titleText.setText(R.string.addCategory);
        categoryField.setHint(R.string.cat);

        Drawable drawableLeft = getResources().getDrawable(R.drawable.category);
        categoryField.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);

        submitBtn.setOnClickListener(v -> validateCategory());
        aDialog = builder.create();
        aDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        aDialog.show();
    }

    private void validateCategory() {
        category = categoryField.getText().toString().trim();
        if (TextUtils.isEmpty(category)) {
            Toast.makeText(this, R.string.enterCat, Toast.LENGTH_SHORT).show();
        } else {
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
                        Toast.makeText(AdminHome.this, category + getString(R.string.hasAdded), Toast.LENGTH_SHORT).show();
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
        hashMap.put("id", "" + timestamp);
        hashMap.put("category", "" + category);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", "" + firebaseAuth.getUid());

        DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Categories");
        reference.child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    dialog.dismiss();
                    aDialog.dismiss();
                    Toast.makeText(this, R.string.catSuccess, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    aDialog.dismiss();
                    Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showCategory() {
        categoriesArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Categories");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriesArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Categories categories = dataSnapshot.getValue(Categories.class);
                    categoriesArrayList.add(categories);
                }
                categoryAdapter = new CategoryAdapter(AdminHome.this,categoriesArrayList);
                binding.catRv.setAdapter(categoryAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUser() {
        FirebaseUser firebaseUser =firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            startActivity(new Intent(AdminHome.this,MainActivity.class));
        }else{
            String email = firebaseUser.getEmail();
            binding.adminInfo.setText(email);
        }
    }
}