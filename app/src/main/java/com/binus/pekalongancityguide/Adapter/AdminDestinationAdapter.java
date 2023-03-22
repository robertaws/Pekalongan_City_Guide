package com.binus.pekalongancityguide.Adapter;

import static com.binus.pekalongancityguide.Misc.Constants.MAX_BYTES_IMAGE;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.binus.pekalongancityguide.EditDestination;
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
        String title = destinationAdmin.getTitle();
        String description = destinationAdmin.getDescription();
        holder.title.setText(title);
        holder.description.setText(description);
        loadCategory(destinationAdmin,holder);
        loadImage(destinationAdmin,holder);
        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsDialog(destinationAdmin,holder);
            }
        });
    }

    private void showOptionsDialog(DestinationAdmin destinationAdmin, HolderAdminDestination holder) {
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
                        }else{
                            deleteDesti(destinationAdmin,holder);
                        }
                    }
                })
                .show();

    }

    private void deleteDesti(DestinationAdmin destinationAdmin, HolderAdminDestination holder) {
        String destiId = destinationAdmin.getId();
        String destiUrl = destinationAdmin.getUrl();
        String destiTitle = destinationAdmin.getTitle();
        Log.d(TAG,"delete desti : Deleting..");
        dialog.setMessage("Deleting "+destiTitle+". . .");
        dialog.show();
        Log.d(TAG,"delete desti : Deleting from storage");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(destiUrl);
        reference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Log.d(TAG,"onSuccess : Succesfully deleted data");
                        DatabaseReference reference1 = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
                        reference1.child(destiId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG,"onSuccess: data deleted from db");
                                        dialog.dismiss();
                                        Toast.makeText(context, "Destination Deleted Succesfully !", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG,"onFAilure: error deleting data because of"+e.getMessage());
                                        dialog.dismiss();
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"onFailure: fail detele data due to"+e.getMessage());
                        dialog.dismiss();
                    }
                });

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
        ImageButton options;
        public HolderAdminDestination(@NonNull View itemView) {
            super(itemView);
            layoutImage = binding.adminlayoutImage;
            title = binding.adminlocTitle;
            description = binding.adminlocDesc;
            category = binding.adminlocCat;
            options = binding.optionBtn;
        }
    }
}
