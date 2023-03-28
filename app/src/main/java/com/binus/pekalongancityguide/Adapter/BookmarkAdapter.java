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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.Destination;
import com.binus.pekalongancityguide.Layout.DestinationDetails;
import com.binus.pekalongancityguide.Misc.FilterBookmark;
import com.binus.pekalongancityguide.Misc.MyApplication;
import com.binus.pekalongancityguide.databinding.ListFavoriteBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static com.binus.pekalongancityguide.Misc.Constants.MAX_BYTES_IMAGE;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.HolderBookmark> implements Filterable{
    private static final String TAG = "BOOKMARK_ADAPTER_TAG";
    private Context context;
    public ArrayList<Destination> destiArray,filterListBookmark;
    private ListFavoriteBinding binding;
    private FilterBookmark filterBookmark;

    public BookmarkAdapter(Context context, ArrayList<Destination> destiArray) {
        this.context = context;
        this.destiArray = destiArray;
        this.filterListBookmark = destiArray;
    }

    @NonNull
    @Override
    public HolderBookmark onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        binding = ListFavoriteBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderBookmark(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBookmark holder, int position){
        Destination destination = destiArray.get(position);
        loadDestination(destination,holder);
        holder.itemView.setOnClickListener(v -> {
            Drawable drawable = holder.layoutImage.getBackground();
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 25, stream);
            byte[] byteArray = stream.toByteArray();

            String filePath = context.getFilesDir().getPath() + "/image.png";
            FileOutputStream fos = null;
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
            intent.putExtra("destiId",destination.getId());
            intent.putExtra("imageFilePath", filePath);
            context.startActivity(intent);
        });
        holder.unBookmark.setOnClickListener(v -> {
            MyApplication.removeFavorite(context,destination.getId());
        });
    }

    private void loadDestination(Destination destination, HolderBookmark holder) {
        String destiId = destination.getId();
        Log.d(TAG,"loadDesti : Destination details of  desti ID : "+destiId);
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
        reference.child(destiId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String title = "" + snapshot.child("title").getValue();
                            String description = "" + snapshot.child("description").getValue();
                            String address = "" + snapshot.child("address").getValue();
                            String categoryId = "" + snapshot.child("categoryId").getValue();
                            String url = "" + snapshot.child("url").getValue();
                            String desRating = "" + snapshot.child("rating").getValue();
                            double latitude = Double.parseDouble(snapshot.child("latitude").getValue().toString());
                            double longitude = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                            destination.setFavorite(true);
                            destination.setTitle(title);
                            destination.setDescription(description);
                            destination.setAddress(address);
                            destination.setCategoryId(categoryId);
                            destination.setUrl(url);
                            destination.setDesLat(latitude);
                            destination.setDesLong(longitude);
                            destination.setRating(desRating);
                            StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                            reference.getBytes(MAX_BYTES_IMAGE)
                                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            Log.d(TAG, "on Success: " + destination.getTitle() + "successfully got the file");
                                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            BitmapDrawable drawable = new BitmapDrawable(holder.itemView.getResources(), bitmap);
                                            drawable.setGravity(Gravity.FILL);
                                            holder.layoutImage.setBackground(drawable);
                                            holder.progressBar.setVisibility(View.GONE);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "on Failure: failed to getting file from url due to" + e.getMessage());
                                        }
                                    });
                            holder.title.setText(title);
                            holder.rating.setText(desRating);
                        } else {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
    @Override
    public int getItemCount() {
        return destiArray.size();
    }

    @Override
    public Filter getFilter() {
        if(filterBookmark==null){
            filterBookmark = new FilterBookmark(filterListBookmark,this);

        }
        return filterBookmark;
    }

    class HolderBookmark extends RecyclerView.ViewHolder{
        RelativeLayout layoutImage;
        TextView title, rating;
        ImageButton unBookmark;
        ProgressBar progressBar;
        public HolderBookmark(@NonNull View itemView) {
            super(itemView);
            layoutImage = binding.bookmarklayoutImage;
            title = binding.bookmarkLocTitle;
            rating = binding.bookmarkLocRat;
            unBookmark = binding.unbookmarkBtn;
            progressBar = binding.progressBookmark;
        }
    }
}
