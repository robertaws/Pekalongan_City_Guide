package com.binus.pekalongancityguide.Layout;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.binus.pekalongancityguide.Adapter.IterAdapter;
import com.binus.pekalongancityguide.ItemTemplate.Destination;
import com.binus.pekalongancityguide.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static com.binus.pekalongancityguide.BuildConfig.MAPS_API_KEY;

public class AddItinerary extends Fragment implements IterAdapter.OnItemLongClickListener {
    private String categoryId, category, startDate, endDate, uid;
    private double latitude, longitude;
    private Destination destination;
    public IterAdapter iterAdapter;
    private RecyclerView iterRV;
    private Button addIter;
    private RelativeLayout selectLayout;
    private LinearLayout containerLayout;
    private TextView selectTv;
    private ImageButton selectCancel;
    public ArrayList<Destination> destinationArrayList, selectedItems;
    private View view;
    private ItineraryPager itineraryPager;


    public AddItinerary() {
    }

    public static AddItinerary newInstance(String categoryId, String categoryName, String categoryUid, String startDate, String endDate, Double latitude, Double longitude) {
        AddItinerary fragment = new AddItinerary();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", categoryName);
        args.putString("uid", categoryUid);
        args.putString("startDate", startDate);
        args.putString("endDate", endDate);
        args.putDouble("latitude", latitude);
        args.putDouble("longitude", longitude);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddItinerary getInstance() {
        return new AddItinerary();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
            startDate = getArguments().getString("startDate");
            endDate = getArguments().getString("endDate");
            latitude = getArguments().getDouble("latitude");
            longitude = getArguments().getDouble("longitude");

            Log.d(TAG, "newInstance: categoryId=" + categoryId + ", startDate=" + startDate + ", endDate=" + endDate + "\n latitude, longitude" + latitude + longitude);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_itinerary, container, false);
        init();
        checkSelect();
        EditText iterSearch = view.findViewById(R.id.search_iter);
        iterSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    iterAdapter.getFilter().filter(s);
                }catch (Exception e){
                    Log.d(TAG,"onTextChanged :"+e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        if (category.equals("All")) {
            loadDestinations();
        } else {
            loadCategoriedDestination();
        }
        addIter.setOnClickListener(v -> showInputDialog());
        selectCancel.setOnClickListener(v -> iterAdapter.exitSelectMode());
        return view;
    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Set the custom layout for the dialog
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_input_details, null);
        builder.setView(view);

        // Get references to the views in the layout
        containerLayout = view.findViewById(R.id.container_layout);
        Button addBtn = view.findViewById(R.id.add_iter_button);

        // Get the selected items from the adapter
        ArrayList<Destination> selectedItems = iterAdapter.getSelectedItems();

        // Sort the selected items by distance
        Collections.sort(selectedItems, (d1, d2) -> {
            double distance1 = calculateDistance(latitude, longitude, Double.parseDouble(d1.getLatitude()), Double.parseDouble(d1.getLongitude()));
            double distance2 = calculateDistance(latitude, longitude, Double.parseDouble(d2.getLatitude()), Double.parseDouble(d2.getLongitude()));
            return Double.compare(distance1, distance2);
        });

        int maxItems = Math.min(selectedItems.size(), 3);
        for (int i = 0; i < maxItems; i++) {
            destination = selectedItems.get(i);
            View itemView = createItemView(destination);
            containerLayout.addView(itemView);
        }

        AlertDialog dialog = builder.create();
        addBtn.setOnClickListener(v -> {
            // Iterate through the container layout and retrieve the input data for each item
            for (int i = 0; i < containerLayout.getChildCount(); i++) {
                // Check if the RadioButton is selected
                if (selectedItems != null) {
                    // Handle the selected item here
                    // For example, you can access the other views and retrieve their data
                    Destination destination = selectedItems.get(i);
                    handleSelectedItem(destination);

                    // Do something with the selected item's data
                }
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private View createItemView(Destination destination) {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_input_details, null);

        CardView cardView = itemView.findViewById(R.id.add_item_cardview);
        LinearLayout layoutBG = itemView.findViewById(R.id.layout_bg);
        TextView placeText = itemView.findViewById(R.id.placeNameTextView);
        TextView distanceTv = itemView.findViewById(R.id.distanceTextView);
        TextView durationTv = itemView.findViewById(R.id.durationTextView);

        double destinationLatitude = Double.parseDouble(destination.getLatitude());
        double destinationLongitude = Double.parseDouble(destination.getLongitude());
        placeText.setText(destination.getTitle());
        float distance = calculateDistance(latitude, longitude, destinationLatitude, destinationLongitude);
        String distanceString = (distance < 1) ? ((int) (distance * 1000)) + " m" : String.format(Locale.getDefault(), "%.2f km", distance);
        distanceTv.setText(distanceString);

        calculateDuration(latitude, longitude, destinationLatitude, destinationLongitude, (durationText, durationTextView) -> durationTextView.setText(durationText), durationTv);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 10, 0, 10);
        cardView.setLayoutParams(layoutParams);

        // Load the image for the destination
        loadImage(destination, layoutBG);

        cardView.setOnClickListener(v -> {
            // Reset the tint and border for all items
            for (int i = 0; i < containerLayout.getChildCount(); i++) {
                View childView = containerLayout.getChildAt(i);
                unselectItem(childView);
            }

            // Select the clicked item
            selectItem(itemView);
        });

        return itemView;
    }

    private void selectItem(View itemView) {
        // Apply the tint and border to the selected item
        CardView cardView = itemView.findViewById(R.id.add_item_cardview);
        LinearLayout layoutBG = itemView.findViewById(R.id.layout_bg);
        cardView.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.selected_item_background));
        layoutBG.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.grayishTint));
    }

    private void unselectItem(View itemView) {
        // Remove the tint and border from the unselected item
        CardView cardView = itemView.findViewById(R.id.add_item_cardview);
        LinearLayout layoutBG = itemView.findViewById(R.id.layout_bg);
        cardView.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.unselected_item_background));
        layoutBG.setBackgroundTintList(null);
    }

    private void handleSelectedItem(Destination destination) {
        // Retrieve the data and perform necessary actions
        String selectedPlace = destination.getTitle();
        String distance = calculateDistanceString(destination);
        String duration = calculateDurationString(destination);

        // Do something with the selected item's data
    }

    private String calculateDistanceString(Destination destination) {
        double destinationLatitude = Double.parseDouble(destination.getLatitude());
        double destinationLongitude = Double.parseDouble(destination.getLongitude());
        float distance = calculateDistance(latitude, longitude, destinationLatitude, destinationLongitude);
        return (distance < 1) ? ((int) (distance * 1000)) + " m" : String.format(Locale.getDefault(), "%.2f km", distance);
    }

    private String calculateDurationString(Destination destination) {
        double destinationLatitude = Double.parseDouble(destination.getLatitude());
        double destinationLongitude = Double.parseDouble(destination.getLongitude());
        String[] durationText = new String[1];
        calculateDuration(latitude, longitude, destinationLatitude, destinationLongitude, (text, textView) -> {
            durationText[0] = text;
        }, null); // Pass null for the textView parameter
        return durationText[0];
    }

    private void loadImage(Destination destination, LinearLayout layoutBG) {
        String imageUrl = destination.getUrl();
        Glide.with(layoutBG.getContext())
                .asBitmap()
                .load(imageUrl)
                .placeholder(R.drawable.logo)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // Set the image bitmap as the background of layoutBG
                        BitmapDrawable drawable = new BitmapDrawable(layoutBG.getResources(), resource);
                        layoutBG.setBackground(drawable);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        Log.d(TAG, "on Failure: failed to get file from URL");
                    }
                });
    }

    private void init() {
        iterRV = view.findViewById(R.id.recycler_view);
        addIter = view.findViewById(R.id.add_iter_btn);
        selectTv = view.findViewById(R.id.select_tv);
        selectLayout = view.findViewById(R.id.select_layout);
        selectCancel = view.findViewById(R.id.select_cancel);
    }

    private void loadDestinations() {
        destinationArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                destinationArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Destination destination = dataSnapshot.getValue(Destination.class);
                    destinationArrayList.add(destination);
                    sortDestination(destinationArrayList);
                }
                if (iterAdapter == null) {
                    initIterAdapter();
                } else {
                    iterAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error: " + error.getMessage());
            }
        });
    }

    private void loadCategoriedDestination(){
        destinationArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
        reference.keepSynced(true);
        reference.orderByChild("categoryId").equalTo(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        destinationArrayList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Destination destination = dataSnapshot.getValue(Destination.class);
                            destinationArrayList.add(destination);
                            sortDestination(destinationArrayList);
                        }
                        if (iterAdapter == null) {
                            initIterAdapter();
                        } else {
                            iterAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sortDestination(ArrayList<Destination> destinationArrayList) {
        Collections.sort(destinationArrayList, (destination1, destination2) -> {
            String title1 = destination1.getTitle().toLowerCase();
            String title2 = destination2.getTitle().toLowerCase();
            return title1.compareTo(title2);
        });
    }

    public void checkSelect() {
        if (iterAdapter != null) {
            selectedItems = iterAdapter.getSelectedItems();
            int counter;
            if (selectedItems.size() < 1) {
                selectLayout.setVisibility(View.GONE);
                addIter.setVisibility(View.INVISIBLE);
            } else if (selectedItems.size() == 1) {
                counter = selectedItems.size();
                selectTv.setText(counter + " item selected");
                addIter.setText("Add to itinerary");
                addIter.setVisibility(View.VISIBLE);
                selectLayout.setVisibility(View.VISIBLE);
            } else {
                counter = selectedItems.size();
                selectTv.setText(counter + " items selected");
                addIter.setText("Add to itinerary");
                addIter.setVisibility(View.VISIBLE);
                selectLayout.setVisibility(View.VISIBLE);
            }
        } else {
            addIter.setVisibility(View.INVISIBLE);
            selectLayout.setVisibility(View.GONE);
        }
    }

    public void initIterAdapter() {
        iterAdapter = new IterAdapter(getContext(), destinationArrayList, this, this, itineraryPager); // Pass the reference to the fragment
        iterRV.setAdapter(iterAdapter);
    }

    @Override
    public void onItemLongClick(Destination destination) {
        checkSelect();
    }

    private float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location location1 = new Location("");
        location1.setLatitude(lat1);
        location1.setLongitude(lon1);
        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(lon2);
        Location.distanceBetween(location1.getLatitude(), location1.getLongitude(),
                location2.getLatitude(), location2.getLongitude(), results);
        return results[0] / 1000;
    }

    private void calculateDuration(double lat1, double lon1, double lat2, double lon2, AddItinerary.DurationCallback callback, TextView durationTv) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + lat1 + "," + lon1 + "&destination=" + lat2 + "," + lon2 + "&key=" + MAPS_API_KEY;

        if (isAdded() && getContext() != null) {
            RequestQueue queue = Volley.newRequestQueue(getContext().getApplicationContext());
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
                try {
                    JSONArray routes = response.getJSONArray("routes");
                    if (routes.length() > 0) {
                        JSONObject route = routes.getJSONObject(0);
//                        Log.d(TAG, "route: " + route);
                        JSONArray legs = route.getJSONArray("legs");
//                        Log.d(TAG, "legs: " + legs);
                        JSONObject leg = legs.getJSONObject(0);
//                        Log.d(TAG, "leg: " + leg);
                        JSONObject duration = leg.getJSONObject("duration");
                        String durationText = duration.getString("text");
                        Log.d(TAG, "Duration: " + durationText);
                        callback.onDurationReceived(durationText, durationTv);
                    } else {
                        Log.e(TAG, "No routes found");
                        callback.onDurationReceived("No routes found", durationTv);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
                Log.e(TAG, "Error calculating travel duration: " + error.getMessage());
                callback.onDurationReceived("Error calculating travel duration", durationTv);
            });
            queue.add(request);
        }
    }

    public interface DurationCallback {
        void onDurationReceived(String durationText, TextView durationTv);
    }

}