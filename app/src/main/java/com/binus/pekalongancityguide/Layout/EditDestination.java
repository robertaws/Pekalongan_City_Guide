package com.binus.pekalongancityguide.Layout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityEditDestinationBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;

public class EditDestination extends AppCompatActivity {
    private ActivityEditDestinationBinding binding;
    FirebaseDatabase refDes;
    FirebaseStorage fiStoRef;
    DatabaseReference ref, catRef;
    private String destiId;
    private Uri imguri = null;
    private ProgressDialog dialog;
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;
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

        refDes = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");
        fiStoRef = FirebaseStorage.getInstance("gs://pekalongan-city-guide-5bf2e.appspot.com");
        ref = refDes.getReference("Destination");
        catRef = refDes.getReference("Categories");

        loadCategory();
        loadDestiInfo();
        binding.categoryTV.setOnClickListener(v -> categoryDialog());
        binding.backDestiAdmin.setOnClickListener(v -> onBackPressed());
        binding.updateDesti.setOnClickListener(v -> validateData());
        binding.editPicture.setOnClickListener(v -> pickImage());
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        resultActivityLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> resultActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: " + imguri);
                        Intent data = result.getData();
                        imguri = data.getData();
                        Log.d(TAG, "onActivityResult: Picked from Gallery" + imguri);
                        binding.editPicture.setImageURI(imguri);
                        Glide.with(EditDestination.this)
                                .load(imguri)
                                .placeholder(R.drawable.person)
                                .centerCrop()
                                .into(binding.editPicture);
                        uploadToStorage(imguri);
                    } else {
                        Toast.makeText(EditDestination.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void uploadToStorage(Uri uri) {
        long timestamp = System.currentTimeMillis();
        String filePathandName = "Destination/" + timestamp;
        fiStoRef.getReference(filePathandName).putFile(uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            String uploadedImageUrl = "" + uriTask.getResult();
            uploadToDB(uploadedImageUrl);
        });
    }

    private void uploadToDB(String url) {
        ref.child(destiId).child("url").setValue(url);
    }

    private void loadDestiInfo() {
        Log.d(TAG, "loadDEstiInfo: loading destination info");
        ref.child(destiId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        selectedCategoryId = "" + snapshot.child("categoryId").getValue();
                        String title = "" + snapshot.child("title").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        binding.editDestiname.setText(title);
                        binding.editDestidesc.setText(description);
                        Log.d(TAG,"onDataChanged: Loading Desti Category Info");
                        catRef.child(selectedCategoryId)
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
        ref.child(destiId)
                .updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "onSuccess: Destination updated");
                    dialog.dismiss();
                    onBackPressed();
                    Toast.makeText(EditDestination.this, "Destination info updated . . .", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditDestination.this, "Destination failed to update because"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    Log.d(TAG, "onFailure: error update due to" + e.getMessage());
                });
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
        catRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String id = "" + dataSnapshot.child("id").getValue();
                    String category = "" + dataSnapshot.child("category").getValue();
                    categoryIdArrayList.add(id);
                    categoryTitleArrayList.add(category);
                    Log.d(TAG, "onDataChanged: ID :" + id);
                    Log.d(TAG,"onDataChanged: Category :"+category);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}