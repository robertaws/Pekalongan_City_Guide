package com.binus.pekalongancityguide.Layout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.databinding.ActivityEditDestinationBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class EditDestination extends AppCompatActivity {
    private ActivityEditDestinationBinding binding;
    private String destiId;
    private ProgressDialog dialog;
    private ArrayList<String> categoryTitleArrayList,categoryIdArrayList;
    private static final String TAG = "DESTI_EDIT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditDestinationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        destiId = getIntent().getStringExtra("destiId");
        dialog = new ProgressDialog(this);
        dialog.setTitle("Please Wait");
        dialog.setCanceledOnTouchOutside(false);

        loadCategory();
        loadDestiInfo();
        binding.categoryTV.setOnClickListener(v -> categoryDialog());
        binding.backDestiAdmin.setOnClickListener(v -> onBackPressed());
        binding.updateDesti.setOnClickListener(v -> validateData());
    }


    private void loadDestiInfo(){
        Log.d(TAG,"loadDEstiInfo: loading destination info");
        DatabaseReference refDes = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
        refDes.child(destiId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        selectedCategoryId= ""+snapshot.child("categoryId").getValue();
                        String title = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        binding.editDestiname.setText(title);
                        binding.editDestidesc.setText(description);
                        Log.d(TAG,"onDataChanged: Loading Desti Category Info");
                        DatabaseReference refDesCat =  FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");
                        refDesCat.child(selectedCategoryId)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String category = ""+snapshot.child("category").getValue();
                                        binding.categoryTV.setText(category);

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String title="",description="";
    private void validateData(){
        title = binding.editDestiname.getText().toString().trim();
        description = binding.editDestidesc.getText().toString().trim();
        if(TextUtils.isEmpty(title)){
            binding.editDestiname.setError("Enter Destination Name");
        }else if(TextUtils.isEmpty(description)){
            binding.editDestidesc.setError("Enter Destination Description");
        }else if(TextUtils.isEmpty(selectedCategoryId)){
            binding.categoryTV.setError("Pick a category");
        }else{
            updateDesti();
        }
    }

    private void updateDesti(){
        Log.d(TAG,"updateDesti : Starting update destination to db");
        dialog.setMessage("updating destination");
        dialog.show();
        HashMap<String,Object> hashMap =new HashMap<>();
        hashMap.put("title",""+title);
        hashMap.put("description",""+description);
        hashMap.put("categoryId",""+selectedCategoryId);
        DatabaseReference ref = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
        ref.child(destiId)
                .updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "onSuccess: Destination updated");
                    dialog.dismiss();
                    Toast.makeText(EditDestination.this, "Destination info updated . . .", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.d(TAG, "onFailure: error update due to" + e.getMessage()));
    }


    private String selectedCategoryId="",selectedCategoryTitle="";

    private void categoryDialog(){
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for(int i=0;i<categoryTitleArrayList.size();i++){
            categoriesArray[i]=categoryTitleArrayList.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Category")
                .setItems(categoriesArray, (dialog, which) -> {
                    selectedCategoryId = categoryIdArrayList.get(which);
                    selectedCategoryTitle = categoryTitleArrayList.get(which);

                    binding.categoryTV.setText(selectedCategoryTitle);
                })
                .show();
    }
    private void loadCategory(){
        Log.d(TAG,"load category: loading categories");
        categoryIdArrayList = new ArrayList<>();
        categoryTitleArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String id = ""+dataSnapshot.child("id").getValue();
                    String category = ""+dataSnapshot.child("category").getValue();
                    categoryIdArrayList.add(id);
                    categoryTitleArrayList.add(category);
                    Log.d(TAG,"onDataChanged: ID :"+id);
                    Log.d(TAG,"onDataChanged: Category :"+category);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}