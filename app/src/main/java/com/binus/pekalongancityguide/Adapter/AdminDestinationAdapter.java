package com.binus.pekalongancityguide.Adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.Layout.DestinationDetailAdmin;
import com.binus.pekalongancityguide.Layout.EditDestination;
import com.binus.pekalongancityguide.ItemTemplate.DestinationAdmin;
import com.binus.pekalongancityguide.Misc.FilterDestiAdmin;
import com.binus.pekalongancityguide.Misc.MyApplication;
import com.binus.pekalongancityguide.databinding.ListDestiAdminBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static com.binus.pekalongancityguide.Misc.Constants.MAX_BYTES_IMAGE;

public class AdminDestinationAdapter extends RecyclerView.Adapter<AdminDestinationAdapter.HolderAdminDestination> implements Filterable {
    private Context context;
    public ArrayList<DestinationAdmin> destinationAdminArrayList,filterList;
    public static final String TAG = "DESTINATION_ADAPTER_TAG";
    private FilterDestiAdmin filterDestiAdmin;
    private ListDestiAdminBinding binding;
    private ProgressDialog dialog;
    public AdminDestinationAdapter(Context context, ArrayList<DestinationAdmin> destinationAdminArrayList) {
        this.context = context;
        this.destinationAdminArrayList = destinationAdminArrayList;
        this.filterList = destinationAdminArrayList;
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

        DestinationAdmin destinationAdmin = destinationAdminArrayList.get(position);
        String destiId = destinationAdmin.getId();
        String categoryId = destinationAdmin.getCategoryId();
        String imageUrl = destinationAdmin.getUrl();
        String title = destinationAdmin.getTitle();
        String description = destinationAdmin.getDescription();
        holder.title.setText(title);
        holder.description.setText(description);
        holder.rating.setText("4.5");
        loadImage(destinationAdmin, holder);
        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsDialog(destinationAdmin, holder);
            }
        });
        holder.itemView.setOnClickListener(v -> {
            Drawable drawable = holder.layoutImage.getBackground();
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            byte[] byteArray = stream.toByteArray();

            Intent intent = new Intent(context, DestinationDetailAdmin.class);
            intent.putExtra("destiId",destiId);
            intent.putExtra("image", byteArray);
            context.startActivity(intent);
        });
    }

    private void showOptionsDialog(DestinationAdmin destinationAdmin, HolderAdminDestination holder){
        String destiId = destinationAdmin.getId();
        String destiUrl = destinationAdmin.getUrl();
        String destiTitle = destinationAdmin.getTitle();
        String[] options = {"Edit","Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                    }
                })
                .show();
    }



//    private void loadCategory(DestinationAdmin destinationAdmin, HolderAdminDestination holder) {
//        String categoryId = destinationAdmin.getCategoryId();
//        DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
//        reference.child(categoryId)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        String categorytext = ""+snapshot.child("category").getValue();
//                        holder.category.setText(categorytext);
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//    }

    public void loadImage(DestinationAdmin destinationAdmin, HolderAdminDestination holder){
        String imageUrl = destinationAdmin.getUrl();
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        reference.getBytes(MAX_BYTES_IMAGE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "on Success: " + destinationAdmin.getTitle() + "successfully got the file");
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        BitmapDrawable drawable = new BitmapDrawable(holder.itemView.getResources(), bitmap);
                        drawable.setGravity(Gravity.FILL);
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

    public class HolderAdminDestination extends RecyclerView.ViewHolder {
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
        }
    }
}
