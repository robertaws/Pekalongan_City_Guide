package com.binus.pekalongancityguide;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.binus.pekalongancityguide.Layout.ProfileFragment;
import com.binus.pekalongancityguide.databinding.ActivityEditProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;


public class EditProfile extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int CAMERA_PERMISSION_CODE = 3;
    private static final int STORAGE_PERMISSION_CODE = 4;
    private ImageView mImageView;
    private EditText mUsernameEditText;
    private Uri mImageUri;
    private DatabaseReference mDatabaseRef;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private ActivityEditProfileBinding binding;
    private static final String TAG = "PROFILE_EDIT_TAG";
    private String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_edit_profile);

        mImageView = findViewById(R.id.edit_image);
        mUsernameEditText = findViewById(R.id.edit_name);
        Button mSaveProfileButton = findViewById(R.id.update_profile);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users");
        mStorageRef = FirebaseStorage.getInstance().getReference("user_profile_images");
        mAuth = FirebaseAuth.getInstance();

        // Initialize the progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Updating Profile...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        // Check storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }

        getInfo();

        binding.backProfile.setOnClickListener(v -> {
            onBackPressed();
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickerDialog();
            }
        });

        mSaveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                saveProfile();
            }
        });
    }

    private void validateData() {
        name = binding.editName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            showCustomToast("Enter new name");
        } else {
            updateProfile(name, null);
        }
    }

    // Update user profile with name and profile picture
    private void updateProfile(String name, Uri imageUri) {
        if (imageUri != null) {
            // Upload new profile picture to Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("user_profile_images/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");
            UploadTask uploadTask = storageRef.putFile(imageUri);

            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Get the download URL of the uploaded profile picture
                return storageRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Update user profile with new name and profile picture URL
                    String profilePictureUrl = task.getResult().toString();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .setPhotoUri(Uri.parse(profilePictureUrl))
                            .build();

                    FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    showCustomToast("User profile updated");
                                    // Dismiss the progress dialog after the upload is successful
                                    progressDialog.dismiss();
                                    // Go to the profile fragment after successful update
                                    Fragment profileFragment = new ProfileFragment();
                                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                    Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("fragment_home");
                                    if (currentFragment != null) {
                                        transaction.replace(currentFragment.getId(), profileFragment, "fragment_profile");
                                        transaction.addToBackStack(null);
                                        transaction.commit();
                                    }
                                } else {
                                    showCustomToast("Failed to update user profile");
                                }
                            });
                } else {
                    showCustomToast("Failed to upload profile picture");
                }
            });
        } else {
            // Update user profile with new name only
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();

            FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showCustomToast("User profile updated");
                            // Dismiss the progress dialog after the upload is successful
                            progressDialog.dismiss();
                            // Go to the profile fragment after successful update
                            Fragment profileFragment = new ProfileFragment();
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.container, profileFragment);
                            transaction.commit();
                        } else {
                            showCustomToast("Failed to update user profile");
                        }
                    });
        }
    }


    private void showImagePickerDialog() {
        final CharSequence[] options = {"Take Photo", "Choose From Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (options[i].equals("Take Photo")) {
                    dispatchTakePictureIntent();
                } else if (options[i].equals("Choose From Gallery")) {
                    pickImageFromGallery();
                } else if (options[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                mImageView.setImageBitmap(imageBitmap);
                mImageUri = getImageUri(getApplicationContext(), imageBitmap);
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                mImageUri = data.getData();
                mImageView.setImageURI(mImageUri);
            }
        }
    }

    private void saveProfile() {
        String username = mUsernameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            showCustomToast("Please enter a username");
            return;
        }

        final String userId = mAuth.getCurrentUser().getUid();

        if (mImageUri != null) {
            progressDialog.setTitle("Uploading image...");
            progressDialog.show();

            // Delete old image from storage
            mStorageRef.child(userId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // Upload new image to storage
                    mStorageRef.child(userId).putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get the download URL of the image
                            mStorageRef.child(userId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Update user profile with new image URL
                                    String imageUrl = uri.toString();
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("imageUrl", imageUrl);
                                    mDatabaseRef.child(userId).updateChildren(hashMap);

                                    // Update user profile with new username
                                    name = mUsernameEditText.getText().toString().trim();
                                    updateProfile(name, mImageUri);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            showCustomToast("Failed to upload image: ");
                        }
                    });
                }
            });
        } else {
            // Update user profile with new username
            name = mUsernameEditText.getText().toString().trim();
            updateProfile(name, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dismiss any open dialogs
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission was granted, start camera intent

            } else {
                // Camera permission was denied, show a message to the user
                showCustomToast("Camera permission is required to take photos");
            }
        }
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Profile Picture", null);
        return Uri.parse(path);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void getInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
        ref.child(mAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String username = "" + dataSnapshot.child("Username").getValue();
                        String email = "" + dataSnapshot.child("Email").getValue();
                        String profileImage = "" + dataSnapshot.child("profileImage").getValue();

                        binding.editName.setText(username);
                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.person).into(binding.editImage);
                        } catch (Exception e) {
                            binding.editImage.setImageResource(R.drawable.person);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void showCustomToast(String customText) {
        // Inflate custom layout
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast));

// Set custom text
        TextView text = layout.findViewById(R.id.toastText);
        text.setText(customText);

// Create and show custom Toast
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();

    }


}
