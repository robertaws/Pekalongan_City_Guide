package com.binus.pekalongancityguide.Layout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.binus.pekalongancityguide.BuildConfig.MAPS_API_KEY;

public class AddDestination extends AppCompatActivity {
    public static final String TAG = "ADD_IMAGE_TAG";
    private static final int PICK_IMAGE_REQUEST = 1;
    PlacesClient placesClient;
    ArrayList<String> categoriesTitleArrayList, categoryIdArrayList;
    private List<String> addedPlaces = new ArrayList<>();

    private ActivityAddDestinationBinding binding;
    private FirebaseAuth firebaseAuth;
    private Uri imageUri;
    private ProgressDialog progressDialog;
    private String title = "";
    private String desc = "";
    private String selectedCategoryId, selectedCategoryTitle;

    private void loadAddedPlaces() {
        SharedPreferences prefs = getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        Set<String> set = prefs.getStringSet("added_places", null);
        if (set != null) {
            addedPlaces = new ArrayList<>(set);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadAddedPlaces();
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

    private void validateData() {
        Log.d(TAG, "validate data : validating data ");
        title = binding.titleEt.getText().toString().trim();
        desc = binding.descEt.getText().toString().trim();
        FindAutocompletePredictionsRequest request;
        if (TextUtils.isEmpty(title)) {
            binding.titleEt.setError("Enter destination title!");
        } else if (TextUtils.isEmpty(selectedCategoryTitle)) {
            binding.categoryPick.setError("Pick a category!");
        } else {
            placesClient = Places.createClient(this);
            List<Place.Field> placeFields = Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG,
                    Place.Field.RATING,
                    Place.Field.OPENING_HOURS,
                    Place.Field.PHONE_NUMBER,
                    Place.Field.PHOTO_METADATAS,
                    Place.Field.TYPES);
            AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
            request = FindAutocompletePredictionsRequest.builder()
                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                    .setSessionToken(token)
                    .setQuery(title)
                    .build();
            Task<FindAutocompletePredictionsResponse> task = placesClient.findAutocompletePredictions(request);
            task.addOnSuccessListener(response -> {
                if (!response.getAutocompletePredictions().isEmpty()) {
                    String placeId = response.getAutocompletePredictions().get(0).getPlaceId();
                    if (addedPlaces.contains(placeId)) {
                        Toast.makeText(this, "This place has already been added", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        addedPlaces.add(placeId);
                        updateAddedPlaces();
                        String url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeId + "&key=" + MAPS_API_KEY;
                        FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                        placesClient.fetchPlace(placeRequest).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Place place = task1.getResult().getPlace();
                                String address = place.getAddress();
                                String phoneNumber = place.getPhoneNumber();
                                OpeningHours openingHours = place.getOpeningHours();
                                double latitude = place.getLatLng().latitude;
                                double longitude = place.getLatLng().longitude;
                                double rating = place.getRating() != null ? place.getRating() : 0;
                                Log.d(TAG, "Address: " + address);
                                Log.d(TAG, "Latitude: " + latitude);
                                Log.d(TAG, "Longitude: " + longitude);
                                Log.d(TAG, "Rating: " + rating);
                                Log.d(TAG, "Phone number: " + phoneNumber);
                                if (TextUtils.isEmpty(desc)) {
                                    StringBuilder sb = new StringBuilder();
                                    for (Place.Type type : place.getTypes()) {
                                        String typeName = type.name().replace("_", " ");
                                        if (!typeName.equals("POINT OF INTEREST")) {
                                            sb.append(typeName.toLowerCase().substring(0, 1).toUpperCase()).append(typeName.toLowerCase().substring(1));
                                            sb.append(", ");
                                        }
                                    }
                                    desc = sb.toString().trim();
                                    if (desc.endsWith(",")) {
                                        desc = desc.substring(0, desc.length() - 1);
                                    }
                                    Log.d(TAG, "Description: " + desc);
                                }
                                new GetReviewsTask() {
                                    @Override
                                    protected void onPostExecute(JSONArray reviews) {
                                        if (reviews != null) {
                                            Log.d(TAG, "Reviews: " + reviews);
                                            if (openingHours != null) {
                                                uploadtoStorage(placeId, address, latitude, longitude, rating, reviews, phoneNumber, openingHours.getWeekdayText(), place);
                                            } else {
                                                uploadtoStorage(placeId, address, latitude, longitude, rating, reviews, phoneNumber, null, place);
                                            }
                                        } else {
                                            Toast.makeText(AddDestination.this, "Error getting reviews", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }.execute(url);
                            } else {
                                Toast.makeText(AddDestination.this, "Error getting location details: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
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
    private void uploadtoStorage(String placeId, String address, double lat, double lng, double rating, JSONArray reviews, String phoneNumber, List<String> weekday, Place place) {
        Log.d(TAG, "uploadtoStorage : uploading to storage");
        progressDialog.setMessage("Uploading image");
        progressDialog.show();
        long timestamp = System.currentTimeMillis();
        String filePathandName = "Destination/" + timestamp;
        if (imageUri == null){
            List<PhotoMetadata> photoMetadataList = place.getPhotoMetadatas();
            if (photoMetadataList != null && !photoMetadataList.isEmpty()) {
                // If photo metadata is available, fetch the photo and set it as the image
                PhotoMetadata photoMetadata = photoMetadataList.get(0);
                FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                        .setMaxWidth(800)
                        .setMaxHeight(500)
                        .build();
                placesClient.fetchPhoto(photoRequest).addOnSuccessListener(fetchPhotoResponse -> {
                    Bitmap bitmap = fetchPhotoResponse.getBitmap();
                    if (bitmap != null) {
                        try {
                            imageUri = bitmapToUri(this, bitmap);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        Log.d(TAG, "image uri: " + imageUri);
                        Glide.with(AddDestination.this)
                                .load(bitmap)
                                .centerCrop()
                                .into(binding.addPicture);
                        StorageReference storageReference = FirebaseStorage.getInstance("gs://pekalongan-city-guide-5bf2e.appspot.com").getReference(filePathandName);
                        storageReference.putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot -> {
                                    Log.d(TAG, "on success : Image uploaded to Storage");
                                    Log.d(TAG, "on success : getting image url");
                                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                    while (!uriTask.isSuccessful()) ;
                                    String uploadedImageUrl = "" + uriTask.getResult();
                                    uploadtoDB(uploadedImageUrl, timestamp, placeId, address, lat, lng, rating, reviews, phoneNumber, weekday);
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Log.d(TAG, "on Failure : Image upload failed due to " + e.getMessage());
                                    Toast.makeText(AddDestination.this, "Image upload failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }).addOnFailureListener(exception -> {
                    Log.e(TAG, "Failed to fetch image, asking user to add an image from the gallery");
                    new AlertDialog.Builder(AddDestination.this)
                            .setTitle("Failed to fetch image")
                            .setMessage("Do you want to add an image from the gallery?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                addPhoto();
                            })
                            .setNegativeButton("No", (dialog, which) -> {
                                this.imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.logo);
                                Glide.with(AddDestination.this)
                                        .load(this.imageUri)
                                        .centerCrop()
                                        .into(binding.addPicture);
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    progressDialog.dismiss();
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        int statusCode = apiException.getStatusCode();
                        Log.e(TAG, "Place photo not found: " + exception.getMessage());
                    }
                });
            } else {
                Log.e(TAG, "Failed to fetch image, asking user to add an image from the gallery");
                new AlertDialog.Builder(AddDestination.this)
                        .setTitle("Failed to fetch image")
                        .setMessage("Do you want to add an image from the gallery?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            addPhoto();
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            this.imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.logo);
                            Glide.with(AddDestination.this)
                                    .load(this.imageUri)
                                    .centerCrop()
                                    .into(binding.addPicture);
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                progressDialog.dismiss();
            }
        } else {
            StorageReference storageReference = FirebaseStorage.getInstance("gs://pekalongan-city-guide-5bf2e.appspot.com").getReference(filePathandName);
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(TAG, "on success : Image uploaded to Storage");
                        Log.d(TAG, "on success : getting image url");
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String uploadedImageUrl = "" + uriTask.getResult();
                        progressDialog.dismiss();
                        uploadtoDB(uploadedImageUrl, timestamp, placeId, address, lat, lng, rating, reviews, phoneNumber, weekday);
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Log.d(TAG, "on Failure : Image upload failed due to " + e.getMessage());
                        Toast.makeText(AddDestination.this, "Image upload failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void uploadtoDB(String uploadedImageUrl, long timestamp, String placeId, String address, double desLat, double desLong, double rating, JSONArray reviews, String phoneNumber, List<String> weekday) {
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
        hashMap.put("longitude", "" + desLong);
        hashMap.put("rating", "" + rating);
        hashMap.put("categoryId", "" + selectedCategoryId);
        hashMap.put("url", "" + uploadedImageUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("placeId", placeId);
        hashMap.put("phoneNumber", phoneNumber);

        ArrayList<HashMap<String, Object>> reviewsList = new ArrayList<>();
        for (int i = 0; i < reviews.length(); i++) {
            try {
                JSONObject review = reviews.getJSONObject(i);
                HashMap<String, Object> reviewMap = new HashMap<>();
                reviewMap.put("authorName", review.getString("author_name"));
                reviewMap.put("rating", review.getInt("rating"));
                reviewMap.put("text", review.getString("text"));
                reviewsList.add(reviewMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        hashMap.put("reviews", reviewsList);
        hashMap.put("openingHours", weekday);

        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
        reference.child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Image uploaded successfully", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.d(TAG, "on Failure : " + e.getMessage());
                    Toast.makeText(AddDestination.this, "Data upload failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "uploadtoDB : Place ID successfully added to database");
                        getPlaceDetails(placeId);
                        onBackPressed();
                    }
                });
    }

    private void loadCategory() {
        Log.d(TAG, "load Category : load Category ");
        categoriesTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Categories");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriesTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String categoryId = "" + dataSnapshot.child("id").getValue();
                    String categoryTitle = "" + dataSnapshot.child("category").getValue();
                    categoriesTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showCategoryDialog() {
        Log.d(TAG, "Category dialog : showing dialog ");
        String[] categoryArray = new String[categoriesTitleArrayList.size()];
        for (int i = 0; i < categoriesTitleArrayList.size(); i++) {
            categoryArray[i] = categoriesTitleArrayList.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoryArray, (dialog, which) -> {
                    selectedCategoryTitle = categoriesTitleArrayList.get(which);
                    selectedCategoryId = categoryIdArrayList.get(which);
                    binding.categoryPick.setText(selectedCategoryTitle);
                    Log.d(TAG, "on Click : Selected Category :" + selectedCategoryId + " " + selectedCategoryTitle);
                })
                .show();

    }

    private void addPhoto() {
        Log.d(TAG, "imageIntent : Start pick destination image");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
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

    public Uri bitmapToUri(Context context, Bitmap bitmap) throws IOException {

        File tempFile = File.createTempFile("tempImage", ".png", context.getCacheDir());

        FileOutputStream fos = new FileOutputStream(tempFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 25, fos);
        fos.flush();
        fos.close();

        return Uri.fromFile(tempFile);
    }

    private static abstract class GetReviewsTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            String url = params[0];
            JSONObject json = null;
            try {
                URL urlObj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod("GET");

                InputStream stream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                json = new JSONObject(response.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return json;
        }
        @Override
        protected void onPostExecute(JSONObject json) {
            if (json != null) {
                try {
                    JSONObject result = json.getJSONObject("result");
                    JSONArray reviews = result.optJSONArray("reviews");
                    if (reviews == null) {
                        reviews = new JSONArray();
                    }
                    for (int i = 0; i < reviews.length(); i++) {
                        JSONObject review = reviews.getJSONObject(i);
                        String authorName = review.getString("author_name");
                        int reviewRating = review.getInt("rating");
                        String text = review.getString("text");

                        Log.d("Review #" + i, "Author Name: " + authorName);
                        Log.d("Review #" + i, "Rating: " + reviewRating);
                        Log.d("Review #" + i, "Text: " + text);
                    }
                    onPostExecute(reviews);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error with JSON parsing: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "Error getting reviews from server");
                JSONArray emptyReviews = new JSONArray();
                onPostExecute(emptyReviews);
            }
        }

        protected abstract void onPostExecute(JSONArray reviews);
    }

    private void saveAddedPlaces() {
        SharedPreferences prefs = getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> set = new HashSet<>(addedPlaces);
        editor.putStringSet("added_places", set);
        editor.apply();
    }

    private void updateAddedPlaces() {
        saveAddedPlaces();
    }

}
