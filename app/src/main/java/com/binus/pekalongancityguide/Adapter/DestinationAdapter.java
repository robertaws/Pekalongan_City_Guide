package com.binus.pekalongancityguide.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Destination;
import com.binus.pekalongancityguide.Layout.DestinationDetails;
import com.binus.pekalongancityguide.Misc.FilterDestiUser;
import com.binus.pekalongancityguide.R;
import com.binus.pekalongancityguide.databinding.ListDestinationBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.HolderDestination> implements Filterable {
    private final Context context;
    public ArrayList<Destination> destinations, filterList;
    private ListDestinationBinding binding;
    private FilterDestiUser filterDestiUser;
    private static final String TAG = "ADAPTER_USER_TAG";

    public DestinationAdapter(Context context, ArrayList<Destination> destinations) {
        this.context = context;
        this.destinations = destinations;
        this.filterList = destinations;
    }

    @NonNull
    @Override
    public HolderDestination onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ListDestinationBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderDestination(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderDestination holder, int position) {
        Destination destination = destinations.get(position);
        String destiId = destination.getId();
        String title = destination.getTitle();
        String description = destination.getDescription();
        holder.title.setText(title);
        loadImage(destination, holder);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination")
                .child(destiId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String desRating = ""+dataSnapshot.child("rating").getValue();
                holder.rating.setText(desRating);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
        holder.itemView.setOnClickListener(v -> {
            if (holder.isImageLoaded) {
                Drawable drawable = holder.layoutImage.getBackground();
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
            }
        });
    }

    private void loadImage(Destination destination, HolderDestination holder) {
        String imageUrl = destination.getUrl();
        Glide.with(holder.itemView.getContext())
                .asBitmap()
                .load(imageUrl)
                .placeholder(R.drawable.logo)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Log.d(TAG, "on Success: " + destination.getTitle() + "successfully got the file");
                        holder.isImageLoaded = true;
                        BitmapDrawable drawable = new BitmapDrawable(holder.itemView.getResources(), resource);
                        drawable.setGravity(Gravity.FILL);
                        holder.layoutImage.setBackground(drawable);
                        holder.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        Log.d(TAG, "on Failure: failed to getting file from url due to");
                    }
                });
    }

    @Override
    public int getItemCount() {
        return destinations.size();
    }

    @Override
    public Filter getFilter() {
        if (filterDestiUser == null) {
            filterDestiUser = new FilterDestiUser(filterList, this);

        }
        return filterDestiUser;
    }

    class HolderDestination extends RecyclerView.ViewHolder{
        RelativeLayout layoutImage;
        TextView title,rating;
        ProgressBar progressBar;
        boolean isImageLoaded;
        public HolderDestination(@NonNull View itemView) {
            super(itemView);
            layoutImage = binding.layoutImage;
            title = binding.locTitle;
            rating = binding.locRat;
            progressBar = binding.progressBar;
            isImageLoaded = false;
        }
    }
}
