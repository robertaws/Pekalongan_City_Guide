package com.binus.pekalongancityguide.Layout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.FragmentProfileBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    Button logout;
    FragmentProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "PROFILE_TAG";
    private static final int PICK_IMAGE_REQUEST = 1;
    ImageView profileImageView;
    StorageReference storageRef;
    DatabaseReference imageRef;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        logout = view.findViewById(R.id.logout_btn);

        // Retrieve a reference to the Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Retrieve a reference to the Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference imageRef = database.getReference("image");

        // Retrieve the current profile image from Firebase Storage and display it in the ImageView
        ImageView profileImageView = view.findViewById(R.id.profileImg);
        storageRef.child("images/profile.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImageView);
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        getInfo();

        // Set OnClickListener for the profile image
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open an image selection activity or dialog
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
            }
        });

        binding.editName.setOnClickListener(v -> {
            //
        });

        binding.logoutBtn.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        });

        return view;
    }

    private void handleImageSelectionResult(Intent data) {
        if (data == null || data.getData() == null) {
            // No image was selected
            return;
        }

        // Get the selected image URI
        Uri imageUri = data.getData();

        // Display the selected image in the ImageView
        Picasso.get().load(imageUri).into(profileImageView);
    }

    private void getInfo() {
        Log.e(TAG, "Loading User Info..." + firebaseAuth.getUid());
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference r = database.getReference("Users");
        r.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = "" + snapshot.child("Email").getValue();
                        String name = ""+snapshot.child("Username").getValue();
                        String profile_img = ""+snapshot.child("profileImage").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String userId = ""+snapshot.child("uid").getValue();
                        String formatDate = MyApplication.formatTimeStamp(Long.parseLong(timestamp));
                        String type = ""+snapshot.child("userType").getValue();

                        binding.profileEmail.setText(email);
                        binding.profileUser.setText(name);
                        binding.profileJoined.setText(formatDate);
                        binding.profileType.setText(type);
                        Glide.with(ProfileFragment.this)
                                .load(profile_img)
                                .placeholder(R.drawable.person)
                                .into(binding.profileImg);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    // Handle the result of the image selection activity or dialog
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the selected image URI
            Uri imageUri = data.getData();

            // Upload the new image to Firebase Storage
            StorageReference imageStorageRef = storageRef.child("images/profile.jpg");
            UploadTask uploadTask = imageStorageRef.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return imageStorageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        // Update the image URL in the Firebase Realtime Database
                        Uri downloadUri = task.getResult();
                        imageRef.setValue(downloadUri.toString());

                        // Display the new profile image in the ImageView
                        Picasso.get().load(downloadUri).into(profileImageView);
                    } else {
                        // Handle errors
                    }
                }
            });
        }
    }


}