package com.binus.pekalongancityguide.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Itinerary;
import com.binus.pekalongancityguide.Layout.DestinationDetails;
import com.binus.pekalongancityguide.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static com.binus.pekalongancityguide.Misc.Constants.MAX_BYTES_IMAGE;

public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.ItineraryViewHolder> {
    private final Context context;
    private List<Itinerary> itineraryList;

    public ItineraryAdapter(Context context, List<Itinerary> itineraryList) {
        this.context = context;
        this.itineraryList = itineraryList;
    }

    @Override
    public ItineraryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_itinerary, parent, false);
        return new ItineraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItineraryViewHolder holder, int position) {
        Itinerary itinerary = itineraryList.get(position);
        String destiId = itinerary.getDestiId();
        loadImage(itinerary, holder);
        holder.dateTextView.setText(itinerary.getDate());
        holder.startTimeTextView.setText(itinerary.getStartTime());
        holder.endTimeTextView.setText(itinerary.getEndTime());
        holder.placeNameTextView.setText(itinerary.getPlaceName());
        float distance = itinerary.getDistance();
        String distanceString;
        if (distance < 1) {
            int distanceInMeters = (int) (distance * 1000);
            distanceString = distanceInMeters + " m";
        } else {
            distanceString = String.format(Locale.getDefault(), "%.2f km", distance);
        }
        holder.distanceTextView.setText(distanceString);
        holder.durationTextView.setText(itinerary.getDurationText());
        holder.itemView.setOnClickListener(v -> {
            Drawable drawable = holder.itineraryBg.getBackground();
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 25, stream);
            byte[] byteArray = stream.toByteArray();

            String filePath = context.getFilesDir().getPath() + "/image.png";
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(filePath);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            try {
                fos.write(byteArray);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Intent intent = new Intent(context, DestinationDetails.class);
            intent.putExtra("destiId", destiId);
            intent.putExtra("imageFilePath", filePath);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itineraryList.size();
    }

    private void loadImage(Itinerary itinerary, ItineraryAdapter.ItineraryViewHolder holder) {
        String imageUrl = itinerary.getUrl();
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        reference.getBytes(MAX_BYTES_IMAGE)
                .addOnSuccessListener(bytes -> {
                    Log.d(TAG, "on Success: " + itinerary.getPlaceName() + "successfully got the file");
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    BitmapDrawable drawable = new BitmapDrawable(holder.itemView.getResources(), bitmap);
                    drawable.setGravity(Gravity.FILL);
                    holder.itineraryBg.setBackground(drawable);
                })
                .addOnFailureListener(e -> Log.d(TAG, "on Failure: failed to getting file from url due to" + e.getMessage()));
    }

    public static class ItineraryViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout itineraryBg;
        public TextView dateTextView, startTimeTextView, endTimeTextView, placeNameTextView, distanceTextView, durationTextView;

        public ItineraryViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            endTimeTextView = itemView.findViewById(R.id.endTimeTextView);
            placeNameTextView = itemView.findViewById(R.id.placeNameTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            itineraryBg = itemView.findViewById(R.id.itinerary_bg);
        }
    }
}
