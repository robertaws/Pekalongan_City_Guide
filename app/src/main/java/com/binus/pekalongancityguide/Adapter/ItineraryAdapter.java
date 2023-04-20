package com.binus.pekalongancityguide.Adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Itinerary;
import com.binus.pekalongancityguide.Misc.AlphaTransformation;
import com.binus.pekalongancityguide.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;
import java.util.Locale;

public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.ItineraryViewHolder> {
    private static final int MAPS_PERMIT= 1;
    private final Context context;
    private final List<Itinerary> itineraryList;

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
        loadImage(itinerary, holder);
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
            if (holder.isImageLoaded) {
                String destinationName = itinerary.getPlaceName();
                double latitude = itinerary.getLatitude();
                double longitude = itinerary.getLongitude();
                String origin = getMyLocation();
                String url = "https://www.google.com/maps/dir/?api=1&origin=" + origin + "&destination=" + latitude + "," + longitude + "&travelmode=driving";
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                } else {
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(webIntent);
                }
            }
        });
    }

private String getMyLocation(){
    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions((Activity) context,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MAPS_PERMIT);
        return null;
    } else {
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            return latitude + "," + longitude;
        } else {
            return null;
        }
    }
}

    @Override
    public int getItemCount() {
        return itineraryList.size();
    }

    private void loadImage(Itinerary itinerary, ItineraryViewHolder holder) {
        String imageUrl = itinerary.getUrl();
        RequestOptions requestOptions = new RequestOptions()
                .transforms(new CenterCrop(), new AlphaTransformation(1f));

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .apply(requestOptions)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        holder.itineraryBg.setBackground(resource);
                        holder.isImageLoaded = true;
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    public static class ItineraryViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout itineraryBg;
        public TextView startTimeTextView;
        public TextView endTimeTextView;
        public TextView placeNameTextView;
        public TextView distanceTextView;
        public TextView durationTextView;
        public boolean isImageLoaded;

        public ItineraryViewHolder(View itemView) {
            super(itemView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            endTimeTextView = itemView.findViewById(R.id.endTimeTextView);
            placeNameTextView = itemView.findViewById(R.id.placeNameTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            itineraryBg = itemView.findViewById(R.id.itinerary_bg);
            isImageLoaded = false;
        }
    }
}
