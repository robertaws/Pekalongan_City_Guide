package com.binus.pekalongancityguide.Adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Destination;
import com.binus.pekalongancityguide.Layout.DestinationDetailAdmin;
import com.binus.pekalongancityguide.Layout.EditDestination;
import com.binus.pekalongancityguide.Misc.FilterDestiAdmin;
import com.binus.pekalongancityguide.Misc.MyApplication;
import com.binus.pekalongancityguide.databinding.ListDestiAdminBinding;
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

import static com.binus.pekalongancityguide.Misc.Constants.FIREBASE_DATABASE_URL;

public class AdminDestinationAdapter extends RecyclerView.Adapter<AdminDestinationAdapter.HolderAdminDestination> implements Filterable {
    private final Context context;
    public ArrayList<Destination> destinationArrayList,filterList;
    public static final String TAG = "DESTINATION_ADAPTER_TAG";
    private FilterDestiAdmin filterDestiAdmin;
    private ListDestiAdminBinding binding;
    private final ProgressDialog dialog;
    public AdminDestinationAdapter(Context context, ArrayList<Destination> destinationArrayList) {
        this.context = context;
        this.destinationArrayList = destinationArrayList;
        this.filterList = destinationArrayList;
        dialog = new ProgressDialog(context);
        dialog.setTitle("Please Wait");
        dialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderAdminDestination onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ListDestiAdminBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderAdminDestination(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAdminDestination holder, int position) {

        Destination destination = destinationArrayList.get(position);
        String destiId = destination.getId();
        String title = destination.getTitle();
        String description = destination.getDescription();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Destination")
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
        holder.title.setText(title);
        holder.description.setText(description);
        loadImage(destination, holder);
        holder.options.setOnClickListener(v -> showOptionsDialog(destination, holder));
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
                Intent intent = new Intent(context, DestinationDetailAdmin.class);
                intent.putExtra("destiId", destiId);
                intent.putExtra("imageFilePath", filePath);
                context.startActivity(intent);
            }
        });
    }
    private void showOptionsDialog(Destination destination, HolderAdminDestination holder){
        String destiId = destination.getId();
        String destiUrl = destination.getUrl();
        String destiTitle = destination.getTitle();
        String[] options = {"Edit", "Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, (dialog, which) -> {
                    if(which==0){
                        Intent intent = new Intent(context, EditDestination.class);
                        intent.putExtra("destiId",destiId);
                        context.startActivity(intent);
                    }else{
                        MyApplication.deleteDesti(
                                context,
                                ""+destiId,
                                ""+destiUrl,
                                ""+destiTitle
                        );
                    }
                })
                .show();
    }

    public void loadImage(Destination destination, HolderAdminDestination holder) {
        String imageUrl = destination.getUrl();
        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Log.d(TAG, "on Success: " + destination.getTitle() + "successfully got the file");
                        BitmapDrawable drawable = new BitmapDrawable(holder.itemView.getResources(), resource);
                        drawable.setGravity(Gravity.FILL);
                        holder.layoutImage.setBackground(drawable);
                        holder.isImageLoaded = true;
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
        return destinationArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filterDestiAdmin==null){
            filterDestiAdmin = new FilterDestiAdmin(filterList,this);

        }
        return filterDestiAdmin;
    }

    public class HolderAdminDestination extends RecyclerView.ViewHolder {
        public boolean isImageLoaded;
        RelativeLayout layoutImage;
        TextView title, description, rating;
        ImageButton options;

        public HolderAdminDestination(@NonNull View itemView) {
            super(itemView);
            layoutImage = binding.adminlayoutImage;
            title = binding.adminlocTitle;
            description = binding.adminlocDesc;
            rating = binding.adminlocRat;
            options = binding.optionBtn;
            isImageLoaded = false;
        }
    }
}
