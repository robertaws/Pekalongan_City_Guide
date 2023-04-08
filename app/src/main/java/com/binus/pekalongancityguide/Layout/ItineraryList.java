    package com.binus.pekalongancityguide.Layout;

    import android.os.Bundle;
    import android.util.Log;

    import androidx.appcompat.app.AppCompatActivity;

    import com.binus.pekalongancityguide.Adapter.ItineraryAdapter;
    import com.binus.pekalongancityguide.ItemTemplate.Itinerary;
    import com.binus.pekalongancityguide.databinding.ActivityItineraryListBinding;
    import com.google.android.libraries.places.api.Places;
    import com.google.android.libraries.places.api.model.Place;
    import com.google.android.libraries.places.api.net.FetchPlaceRequest;
    import com.google.android.libraries.places.api.net.PlacesClient;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.Query;
    import com.google.firebase.database.ValueEventListener;

    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.List;

    public class ItineraryList extends AppCompatActivity {
        private static final String TAG = "ITER_TAG";
        private final List<Itinerary> itineraryList = new ArrayList<>();
        public ActivityItineraryListBinding binding;
        ItineraryAdapter adapter;
        private FirebaseAuth firebaseAuth;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            binding = ActivityItineraryListBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            firebaseAuth = FirebaseAuth.getInstance();
            adapter = new ItineraryAdapter(itineraryList);
            binding.backtoprofile.setOnClickListener(v -> {
                onBackPressed();
            });
            loadItinerary();
            binding.itineraryRv.setAdapter(adapter);
        }

        private void loadItinerary() {
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/");
            DatabaseReference userRef = database.getReference("Users").child(firebaseAuth.getUid());
            Query itineraryQuery = userRef.child("itinerary");

            itineraryQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Itinerary> itineraryList = new ArrayList<>();
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String destiId = childSnapshot.getKey();
                        Log.d(TAG, "Destination ID: " + destiId);
                        for (DataSnapshot itinerarySnapshot : dataSnapshot.child(destiId).getChildren()) {
                            String date = itinerarySnapshot.child("date").getValue(String.class);
                            Log.d(TAG, "Date: " + date);
                            String startTime = itinerarySnapshot.child("startTime").getValue(String.class);
                            Log.d(TAG, "Start Time: " + startTime);
                            String endTime = itinerarySnapshot.child("endTime").getValue(String.class);
                            Log.d(TAG, "End Time: " + endTime);
                            String placeId = itinerarySnapshot.child("placeId").getValue(String.class);
                            Log.d(TAG, "Place ID: " + placeId);
                            if (!Places.isInitialized()) {
                                Places.initialize(getApplicationContext(), "MAPS_API_KEY");
                            }
                            PlacesClient placesClient = Places.createClient(ItineraryList.this);
                            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME);
                            if (placeId != null && !placeId.isEmpty()) {
                                FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();
                                placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                                    Place place = response.getPlace();
                                    String placeName = place.getName();
                                    itineraryList.add(new Itinerary(date, startTime, endTime, placeName));

                                    ItineraryAdapter adapter = new ItineraryAdapter(itineraryList);
                                    binding.itineraryRv.setAdapter(adapter);
                                }).addOnFailureListener((e) -> {
                                    Log.e(TAG, "Error fetching place details: " + e.getMessage());
                                });
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                }
            });
        }

    }
