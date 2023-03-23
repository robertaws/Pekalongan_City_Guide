package com.binus.pekalongancityguide.Layout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Rating;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ActivityAddDestinationBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.binus.pekalongancityguide.BuildConfig.MAPS_API_KEY;

public class AddDestination extends AppCompatActivity {
    private ActivityAddDestinationBinding binding;
    private FirebaseAuth firebaseAuth;
    private Uri imageUri = null;
    private static final int PICK_IMAGE_REQUEST = 1;

    private ProgressDialog progressDialog;
    ArrayList<String> categoriesTitleArrayList, categoryIdArrayList;
    public static final String TAG = "ADD_IMAGE_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Places.initialize(getApplicationContext(), MAPS_API_KEY);
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
        binding.addBtn.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
            validateData();
        });

    }

    private String title = "", desc = "", address = "";
    private Double latitude, longitude;

    private void validateData() {
        Log.d(TAG, "validate data : validating data ");
        title = binding.titleEt.getText().toString().trim();
        desc = binding.descEt.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            binding.titleEt.setError("Enter destination title!");
        } else if (TextUtils.isEmpty((desc))) {
            binding.descEt.setError("Enter destination description!");
        } else if (TextUtils.isEmpty(selectedCategoryTitle)) {
            binding.categoryPick.setError("Pick a category!");
        }else if(imageUri==null){
            Toast.makeText(this, "Pick an image!", Toast.LENGTH_SHORT).show();
        }else {
            PlacesClient placesClient = Places.createClient(this);
            List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.RATING);
            AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                    .setSessionToken(token)
                    .setQuery(title)
                    .build();
            Task<FindAutocompletePredictionsResponse> task = placesClient.findAutocompletePredictions(request);
            task.addOnSuccessListener(response -> {
                if (!response.getAutocompletePredictions().isEmpty()) {
                    String placeId = response.getAutocompletePredictions().get(0).getPlaceId();
                    Log.d(TAG, "Place ID: " + placeId);

                    FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                    placesClient.fetchPlace(placeRequest).addOnCompleteListener(new OnCompleteListener<FetchPlaceResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<FetchPlaceResponse> task) {
                            if (task.isSuccessful()) {
                                Place place = task.getResult().getPlace();
                                String address = place.getAddress();
                                double latitude = place.getLatLng().latitude;
                                double longitude = place.getLatLng().longitude;
                                double rating = place.getRating();
                                Log.d(TAG, "Address: " + address);
                                Log.d(TAG, "Latitude: " + latitude);
                                Log.d(TAG, "Longitude: " + longitude);
                                Log.d(TAG, "Rating: " + rating);

                                uploadtoStorage(placeId, address, latitude, longitude,rating);
                            } else {
                                Toast.makeText(AddDestination.this, "Error getting location details: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(this, "No location found for the given title", Toast.LENGTH_SHORT).show();
                }
            });
            task.addOnFailureListener(e -> {
                Log.e(TAG, "Error getting place ID: " + e.getMessage());
                Toast.makeText(this, "Error getting location from title: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        }
    }

    private void uploadtoStorage(String placeId, String address, double lat, double lng,double rating) {
        Log.d(TAG, "uploadtoStorage : uploading to storage");
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
                        uploadtoDB(uploadedImageUrl, timestamp, placeId, address, lat, lng,rating);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "on Failure : Image upload failed due to " + e.getMessage());
                        Toast.makeText(AddDestination.this, "Image upload failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void uploadtoDB(String uploadedImageUrl, long timestamp, String placeId, String address, double desLat, double desLong, double rating) {
        Log.d(TAG, "uploadtoDB : uploading image to firebase DB");
        progressDialog.setMessage("Uploading image info");
        String uid = firebaseAuth.getUid();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + uid);
        hashMap.put("id", "" + timestamp);
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + desc);
        hashMap.put("address", "" + address);
        hashMap.put("latitude", "" + desLat);
        hashMap.put("longitude","" + desLong);
        hashMap.put("rating","" + rating);
        hashMap.put("categoryId", "" + selectedCategoryId);
        hashMap.put("url", "" + uploadedImageUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("placeId", placeId);
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
        reference.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Image uploaded successfully", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "on Failure : " + e.getMessage());
                        Toast.makeText(AddDestination.this, "Data upload failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "uploadtoDB : Place ID successfully added to database");
                            getPlaceDetails(placeId);
                        }
                    }
                });
    }

    private void loadCategory(){
        Log.d(TAG,"load Category : load Category ");
        categoriesTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriesTitleArrayList.clear();
                categoryIdArrayList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String categoryId = ""+dataSnapshot.child("id").getValue();
                    String categoryTitle = ""+dataSnapshot.child("category").getValue();
                    categoriesTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private String selectedCategoryId,selectedCategoryTitle;
    private void showCategoryDialog(){
        Log.d(TAG,"Category dialog : showing dialog ");
        String [] categoryArray = new String[categoriesTitleArrayList.size()];
        for(int i=0;i<categoriesTitleArrayList.size();i++){
            categoryArray[i] = categoriesTitleArrayList.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoryArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCategoryTitle = categoriesTitleArrayList.get(which);
                        selectedCategoryId = categoryIdArrayList.get(which);
                        binding.categoryPick.setText(selectedCategoryTitle);
                        Log.d(TAG,"on Click : Selected Category :"+selectedCategoryId+" "+selectedCategoryTitle);
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
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Log.d(TAG, "onActivityResult : Image picked");
            imageUri = data.getData();
            Log.d(TAG, "onActivityResult : URI : " + imageUri);
            Glide.with(AddDestination.this)
                    .load(imageUri)
                    .placeholder(R.drawable.person)
                    .centerCrop()
                    .into(binding.addPicture);
        } else {
            Log.d(TAG, "onActivityResult : Cancelled pick image");
            Toast.makeText(this, "Cancelled pick image", Toast.LENGTH_SHORT).show();
        }
    }

    private void getPlaceDetails(String placeId) {
        Log.d(TAG, "getPlaceDetails : getting place details");
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();
        PlacesClient placesClient = Places.createClient(this);
        placesClient.fetchPlace(request)
                .addOnSuccessListener((response) -> {
                    Place place = response.getPlace();
                    Log.d(TAG, "Place details: " + place.getName() + ", " + place.getAddress() + ", " + place.getLatLng());
                })
                .addOnFailureListener((exception) -> {
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                });
    }

}