package com.binus.pekalongancityguide;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.Layout.MyApplication;
import com.binus.pekalongancityguide.databinding.ActivityEditProfileBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class EditProfile extends AppCompatActivity {
    private static final int REQUEST_GALLERY = 500;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private ActivityEditProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "PROFILE_EDIT_TAG";
    private Uri imguri = null;
    private String name = "";
    private ProgressDialog progressDialog;
    ShapeableImageView imgView;

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
            validatedata();
        });
    }

    private void validatedata() {
        name = binding.editName.getText().toString().trim();
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Enter new name", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "Failed to update db due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadImage() {
        Log.d(TAG,"UploadImage: Uploading profile image..");
        progressDialog.setMessage("Updating profile image");
        progressDialog.show();

        String filePathAndName = "ProfileImages/"+firebaseAuth.getUid();
        StorageReference storageReference = FirebaseStorage.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference(filePathAndName);
        storageReference.putFile(imguri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "on Success: Profile image uploaded");
                        Log.d(TAG, "on Success: Getting url of the uploaded image");
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedImageUrl = ""+uriTask.getResult();
                        Log.d(TAG, "on Success: Uploaded image url:"+uploadedImageUrl);
                        updateProfile(uploadedImageUrl);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "on Failure: Failed to upload image due to" + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(EditProfile.this, "on Failure: Failed to upload image due to" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private ActivityResultLauncher<Void> cameraResultActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(), new ActivityResultCallback<Bitmap>() {
                @Override
                public void onActivityResult(Bitmap result) {
                    if (result != null) {
                        // set the captured image to ImageView
                        imgView.setImageBitmap(result);

                        // save the image to local storage if needed
                        // saveImageToGallery(result);

                        // convert the Bitmap image to Uri
                        imguri = getImageUri(this, result);

                        // upload the image to Firebase Storage or do other things with the Uri
                        uploadImage();
                    }
                }
            });

    private void showImage() {
        PopupMenu popupMenu = new PopupMenu(this, binding.editImage);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Gallery");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int which = item.getItemId();
                if (which == 0) {
                    // launch the camera to take a picture
                    cameraResultActivityLauncher.launch(null);
                    return true;
                } else if (which == 1) {
                    // launch the gallery to choose a picture
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, REQUEST_GALLERY);
                    return true;
                }
                return false;
            }
        });
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_GALLERY && data != null) {
                imguri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imguri);
                    imgView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private ActivityResultLauncher<Intent> galleryResultActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: " + imguri);
                        Intent data = result.getData();
                        imguri = data.getData();
                        Log.d(TAG,"onActivityResult: Picked from Gallery"+imguri);
                        binding.editImage.setImageURI(imguri);
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
                        Glide.with(EditProfile.this)
                                .load(profile_img)
                                .placeholder(R.drawable.person)
                                .into(binding.editImage);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private Uri getImageUri(ActivityResultCallback<Bitmap> context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

}