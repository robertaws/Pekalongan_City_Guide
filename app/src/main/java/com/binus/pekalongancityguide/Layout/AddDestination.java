package com.binus.pekalongancityguide.Layout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.binus.pekalongancityguide.ItemTemplate.Categories;
import com.binus.pekalongancityguide.databinding.ActivityAddDestinationBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class AddDestination extends AppCompatActivity {
    private ActivityAddDestinationBinding binding;
    private FirebaseAuth firebaseAuth;
    private Uri imageUri = null;
    private static final int PICK_IMAGE_REQUEST = 1;

    private ProgressDialog progressDialog;
    ArrayList<Categories> categoriesArrayList;
    public static final String TAG = "ADD_IMAGE_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddDestinationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        loadCategory();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backtoAdmin.setOnClickListener(v -> onBackPressed());
        binding.addPicture.setOnClickListener(v -> addPhoto());
        binding.categoryPick.setOnClickListener(v -> showCategoryDialog());
        binding.addBtn.setOnClickListener(v -> validateData());
    }
    private String title="",desc="",category="";
    private void validateData() {
        Log.d(TAG,"validate data : validating data ");
        title = binding.titleEt.getText().toString().trim();
        desc = binding.descEt.getText().toString().trim();
        category = binding.categoryPick.getText().toString().trim();
        if(TextUtils.isEmpty(title)){
            binding.titleEt.setError("Enter destination title!");
        }else if(TextUtils.isEmpty((desc))){
            binding.descEt.setError("Enter destination description!");
        }else if(TextUtils.isEmpty(category)){
            binding.categoryPick.setError("Pick a category!");
        }else if(imageUri==null){
            Toast.makeText(this, "Pick an image!", Toast.LENGTH_SHORT).show();
        }else{
            uploadtoStorage();
        }
    }

    private void uploadtoStorage() {
        Log.d(TAG,"uploadtoStorage : uploading to storage");
        progressDialog.setMessage("Uploading image");
        progressDialog.show();
        long timestamp = System.currentTimeMillis();
        String filePathandName = "Destination/" + timestamp;
        StorageReference storageReference = FirebaseStorage.getInstance("gs://pekalongan-city-guide-5bf2e.appspot.com").getReference(filePathandName);
        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG,"on success : Image uploaded to Storage");
                        Log.d(TAG,"on success : getting image url");
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedImageUrl = ""+uriTask.getResult();
                        uploadtoDB(uploadedImageUrl,timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG,"on Failure : Image upload failed due to "+e.getMessage());
                        Toast.makeText(AddDestination.this, "Image upload failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
    private void uploadtoDB(String uploadedImageUrl, long timestamp){
        Log.d(TAG,"uploadtoDB : uploading image to firebase DB");
        progressDialog.setMessage("Uploading image info");
        String uid = firebaseAuth.getUid();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("title",""+title);
        hashMap.put("description",""+desc);
        hashMap.put("category",""+category);
        hashMap.put("url",""+uploadedImageUrl);
        hashMap.put("timestamp",timestamp);
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
        reference.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG,"on success : Image succesfully uploaded to db");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG,"on Failure : failed to upload to db due to"+e.getMessage());
                        Toast.makeText(AddDestination.this, "failed to upload to db due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCategory(){
        Log.d(TAG,"load Category : load Category ");
        categoriesArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriesArrayList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Categories categories = dataSnapshot.getValue(Categories.class);
                    categoriesArrayList.add(categories);
                    Log.d(TAG,"on Data Changed: "+categories.getCategory());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void showCategoryDialog(){
        Log.d(TAG,"Category dialog : showing dialog ");
        String [] categoryArray = new String[categoriesArrayList.size()];
        for(int i=0;i<categoriesArrayList.size();i++){
            categoryArray[i] = categoriesArrayList.get(i).getCategory();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoryArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String category = categoryArray[which];
                        binding.categoryPick.setText(category);
                        Log.d(TAG,"on Click : Selected Category :"+category);
                    }
                })
                .show();

    }
    private void addPhoto(){
        Log.d(TAG,"imageIntent : Start pick destination image");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            Log.d(TAG,"onActivityResult : Image picked");
            imageUri = data.getData();
            Log.d(TAG,"onActivityResult : URI : "+imageUri);
        }
        else{
            Log.d(TAG,"onActivityResult : Cancelled pick image");
            Toast.makeText(this, "Cancelled pick image", Toast.LENGTH_SHORT).show();
        }
    }
}