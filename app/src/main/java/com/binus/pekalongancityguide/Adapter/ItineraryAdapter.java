package com.binus.pekalongancityguide.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Itinerary;
import com.binus.pekalongancityguide.Layout.DestinationDetails;
import com.binus.pekalongancityguide.Misc.AlphaTransformation;
import com.binus.pekalongancityguide.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.ItineraryViewHolder> {
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
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
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
