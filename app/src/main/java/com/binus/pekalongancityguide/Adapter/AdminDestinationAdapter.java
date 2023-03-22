package com.binus.pekalongancityguide.Adapter;

import static com.binus.pekalongancityguide.Misc.Constants.MAX_BYTES_IMAGE;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.ItemTemplate.DestinationAdmin;
import com.binus.pekalongancityguide.Misc.Constants;
import com.binus.pekalongancityguide.Misc.FilterDestiAdmin;
import com.binus.pekalongancityguide.databinding.ListDestiAdminBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AdminDestinationAdapter extends RecyclerView.Adapter<AdminDestinationAdapter.HolderAdminDestination> implements Filterable {
    private Context context;
    public ArrayList<DestinationAdmin> destinationAdminArrayList,filterList;
    public static final String TAG = "DESTINATION_ADAPTER_TAG";
    private FilterDestiAdmin filterDestiAdmin;
    private ListDestiAdminBinding binding;
    public AdminDestinationAdapter(Context context, ArrayList<DestinationAdmin> destinationAdminArrayList) {
        this.context = context;
        this.destinationAdminArrayList = destinationAdminArrayList;
        this.filterList = destinationAdminArrayList;
    }

    @NonNull
    @Override
    public HolderAdminDestination onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ListDestiAdminBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderAdminDestination(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAdminDestination holder, int position) {

        DestinationAdmin destinationAdmin = destinationAdminArrayList.get(position);
        String title = destinationAdmin.getTitle();
        String description = destinationAdmin.getDescription();
        holder.title.setText(title);
        holder.description.setText(description);
        loadCategory(destinationAdmin,holder);
        loadImage(destinationAdmin,holder);
    }

    private void loadCategory(DestinationAdmin destinationAdmin, HolderAdminDestination holder) {
        String categoryId = destinationAdmin.getCategoryId();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        reference.child(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String categorytext = ""+snapshot.child("category").getValue();
                        holder.category.setText(categorytext);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadImage(DestinationAdmin destinationAdmin, HolderAdminDestination holder) {
        String imageUrl = destinationAdmin.getUrl();
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        reference.getBytes(MAX_BYTES_IMAGE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG,"on Success: "+destinationAdmin.getTitle()+"successfully got the file");
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        BitmapDrawable drawable = new BitmapDrawable(holder.itemView.getResources(), bitmap);
                        holder.layoutImage.setBackground(drawable);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"on Failure: failed to getting file from url due to"+e.getMessage());
                    }
                });
    }

    @Override
    public int getItemCount() {
        return destinationAdminArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filterDestiAdmin==null){
            filterDestiAdmin = new FilterDestiAdmin(filterList,this);

        }
        return filterDestiAdmin;
    }

    class HolderAdminDestination extends RecyclerView.ViewHolder{
        RelativeLayout layoutImage;
        TextView title,description,category;
        public HolderAdminDestination(@NonNull View itemView) {
            super(itemView);
            layoutImage = binding.adminlayoutImage;
            title = binding.adminlocTitle;
            description = binding.adminlocDesc;
            category = binding.adminlocCat;
        }
    }
}
