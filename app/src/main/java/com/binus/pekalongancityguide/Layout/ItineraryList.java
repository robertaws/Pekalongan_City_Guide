    package com.binus.pekalongancityguide.Layout;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import android.graphics.Color;
    import android.os.Bundle;
    import android.util.Log;

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
    import com.google.firebase.database.ValueEventListener;
    import com.lriccardo.timelineview.TimelineDecorator;

    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.List;

    public class ItineraryList extends AppCompatActivity {
        public ActivityItineraryListBinding binding;
        private FirebaseAuth firebaseAuth;
        private static final String TAG = "ITER_TAG";
        ItineraryAdapter adapter;
        private List<Itinerary> itineraryList = new ArrayList<>();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            binding = ActivityItineraryListBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            firebaseAuth = FirebaseAuth.getInstance();
            adapter = new ItineraryAdapter(itineraryList);
            binding.backtoprofile.setOnClickListener(v ->{
                onBackPressed();
            });
            loadItinerary();
            binding.itineraryRv.setAdapter(adapter);
        }

        private void loadItinerary(){
            DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("Users");
            reference.child(firebaseAuth.getUid())
                    .child("itinerary")
                    .child("destiId").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String date = "" + dataSnapshot.child("date").getValue();
                                String startTime = "" + dataSnapshot.child("startTime").getValue();
                                String endTime = "" + dataSnapshot.child("endTime").getValue();
                                String placeId = "" + dataSnapshot.child("placeId").getValue();
                                if (!Places.isInitialized()) {
                                    Places.initialize(getApplicationContext(), "MAPS_API_KEY");
                                }
                                PlacesClient placesClient = Places.createClient(ItineraryList.this);
                                List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME);
                                FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();
                                placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                                    Place place = response.getPlace();
                                    String placeName = place.getName();
                                    Itinerary itinerary = new Itinerary(date, startTime, endTime, placeName);
                                    itineraryList.add(itinerary);
                                    adapter.notifyDataSetChanged();
                                }).addOnFailureListener((e) -> {
                                    Log.e(TAG,""+ e.getMessage());
                                });
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }
