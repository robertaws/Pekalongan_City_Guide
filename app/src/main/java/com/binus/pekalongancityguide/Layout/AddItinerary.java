package com.binus.pekalongancityguide.Layout;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.binus.pekalongancityguide.ItemTemplate.Destination;
import com.binus.pekalongancityguide.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class AddItinerary extends Fragment {
    private ArrayList<Destination> destinationArrayList;

    public AddItinerary() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_itinerary, container, false);
        GridLayout gridLayout = view.findViewById(R.id.grid_layout);
        gridLayout.setColumnCount(3);
        loadDestinations(gridLayout);
        return view;
    }

    private void loadDestinations(GridLayout gridLayout) {
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
                }
                // Call the method to create and add CardViews to the GridLayout dynamically
                addCardViewsToGridLayout(destinationArrayList, gridLayout);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error: " + error.getMessage());
            }
        });
    }

    private void addCardViewsToGridLayout(List<Destination> destinationList, GridLayout gridLayout) {
        for (Destination destination : destinationArrayList) {

            CardView cardView = new CardView(getActivity());
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            int margin = (int) getResources().getDimension(R.dimen.card_margin);
            layoutParams.width = 0;
            layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            layoutParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            layoutParams.setMargins(margin, margin, margin, margin);
            cardView.setLayoutParams(layoutParams);

            // Create a linear layout to hold the image and text view
            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            ImageView imageView = new ImageView(getActivity());
            LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300); // Set a fixed height for the ImageView
            imageView.setLayoutParams(imageLayoutParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.get().load(destination.getUrl()).into(imageView);

            // Create a text view to hold the destination name
            TextView textView = new TextView(getActivity());
            textView.setText(destination.getTitle()); // Use the destination name to set the text of the TextView
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            // Add the image view and text view to the linear layout
            linearLayout.addView(imageView);
            linearLayout.addView(textView);

            // Add the linear layout to the card view
            cardView.addView(linearLayout);

            // Add the card view to the grid layout
            gridLayout.addView(cardView);
        }
    }

}