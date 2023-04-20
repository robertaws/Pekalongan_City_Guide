package com.binus.pekalongancityguide.Layout;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.binus.pekalongancityguide.Misc.MyApplication;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityEditProfileBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class EditProfile extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private ActivityEditProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "PROFILE_EDIT_TAG";
    private Uri imguri = null;
    private String name = "";
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        getInfo();

        binding.backProfile.setOnClickListener(v -> {
            onBackPressed();
        });

        binding.editImage.setOnClickListener(v -> {
            showImage();
        });
        binding.updateProfile.setOnClickListener(v -> {
            // Hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
            validatedata();
        });

        View activityRootView = findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
            if (heightDiff > dpToPx(200)) {
                findViewById(R.id.editProfile).setOnTouchListener((v, event) -> {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    return false;
                });
            }
        });

    }

    private void validatedata() {
        name = binding.editName.getText().toString().trim();
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, R.string.enter_name_edit, Toast.LENGTH_SHORT).show();
        }else{
            if(imguri==null){
                updateProfile("");
            }else{
                uploadImage();
            }
        }
    }

    private void updateProfile(String imageUri) {
        Log.d(TAG, "Update profile: updating user profile");
        progressDialog.setMessage("updating user profile...");
        progressDialog.show();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("Username",""+name);
        if(imguri != null){
            hashMap.put("profileImage",""+imageUri);
        }
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
        reference.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "on Success: Profile updated");
                    progressDialog.dismiss();
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "on Success: Failed to update db due to"+e.getMessage());
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to update db due to" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void uploadImage() {
        Log.d(TAG,"UploadImage: Uploading profile image..");
        progressDialog.setMessage("Updating profile image");
        progressDialog.show();

        String filePathAndName = "ProfileImages/"+firebaseAuth.getUid();
        StorageReference storageReference = FirebaseStorage.getInstance("gs://pekalongan-city-guide-5bf2e.appspot.com/").getReference(filePathAndName);
        storageReference.putFile(imguri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "on Success: Profile image uploaded");
                    Log.d(TAG, "on Success: Getting url of the uploaded image");
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    String uploadedImageUrl = "" + uriTask.getResult();
                    Log.d(TAG, "on Success: Uploaded image url:" + uploadedImageUrl);
                    updateProfile(uploadedImageUrl);
                    onBackPressed();
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "on Failure: Failed to upload image due to" + e.getMessage());
                    progressDialog.dismiss();
                    Toast.makeText(this, "on Failure: Failed to upload image due to" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
        PopupMenu popupMenu = new PopupMenu(this, binding.editImage);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Gallery");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(item -> {
            int which = item.getItemId();
            if (which == 0) {
                pickImageCamera();
            } else if (which == 1) {
                pickImageGallery();
            }
            return false;
        });
    }

    private void pickImageCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"new Pick");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image Description");
        imguri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imguri);
        cameraResultActivityLauncher.launch(intent);
    }
    private void pickImageGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryResultActivityLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> cameraResultActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: " + imguri);
                        Intent data = result.getData();
                        binding.editImage.setImageURI(imguri);
                        Glide.with(EditProfile.this)
                                .load(imguri)
                                .placeholder(R.drawable.person)
                                .centerCrop()
                                .into(binding.editImage);
                    } else {
                        Toast.makeText(EditProfile.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
    private final ActivityResultLauncher<Intent> galleryResultActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: " + imguri);
                        Intent data = result.getData();
                        imguri = data.getData();
                        Log.d(TAG, "onActivityResult: Picked from Gallery" + imguri);
                        binding.editImage.setImageURI(imguri);
                        Glide.with(EditProfile.this)
                                .load(imguri)
                                .placeholder(R.drawable.person)
                                .centerCrop()
                                .into(binding.editImage);
                    }else{
                        Toast.makeText(EditProfile.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
    private void getInfo(){
        Log.e(TAG,"Loading User Info..."+firebaseAuth.getUid());
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference r = database.getReference("Users");
        r.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = ""+snapshot.child("Email").getValue();
                        String name = ""+snapshot.child("Username").getValue();
                        String profile_img = ""+snapshot.child("profileImage").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String userId = ""+snapshot.child("uid").getValue();
                        String formatDate = MyApplication.formatTimeStamp(Long.parseLong(timestamp));
                        String type = ""+snapshot.child("userType").getValue();

                        binding.editName.setText(name);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

}