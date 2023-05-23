package com.binus.pekalongancityguide.Misc;

import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.binus.pekalongancityguide.Adapter.ItineraryAdapter;
import com.binus.pekalongancityguide.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import static com.binus.pekalongancityguide.Misc.Constants.FIREBASE_DATABASE_URL;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    public static final String formatTimeStamp(long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);
        String date = DateFormat.format("dd/MM/yyyy",calendar).toString();
        return date;
    }
    public static final String formatProfileDate(long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);
        String date = DateFormat.format("dd/MMMM/yyyy",calendar).toString();
        return date;
    }
    public static void deleteIter(Context context, String destiId, ItineraryAdapter adapter, int position){
        String uid = FirebaseAuth.getInstance().getUid();
        String TAG = "DELETE_ITER_TAG";
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.confirm_delete);
        builder.setMessage(R.string.delete_item);
        builder.setPositiveButton(R.string.yes_myapp, (dialogInterface, i) -> {
            ProgressDialog dialog = new ProgressDialog(context);
            dialog.setTitle(R.string.wait);
            dialog.setMessage(context.getString(R.string.deleting));
            dialog.show();
            DatabaseReference itineraryRef = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL)
                    .getReference("Users")
                    .child(uid)
                    .child("itinerary");

            Query query = itineraryRef.orderByChild("destiId").equalTo(destiId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        snapshot.getRef().removeValue();
                    }
                    adapter.notifyItemRemoved(position);
                    dialog.dismiss();
                    Toast.makeText(context,R.string.delete_iter, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    dialog.dismiss();
                    Toast.makeText(context,context.getString(R.string.fail_deteltIter) + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                }
            });
        });
        builder.setNegativeButton(R.string.noMyapp, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            Toast.makeText(context,R.string.iter_notdeleted, Toast.LENGTH_SHORT).show();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public static void deleteDesti(Context context, String destiId, String destiUrl, String destiTitle) {
        String TAG = "DELETE_DESTI_TAG";
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete " + destiTitle + "?");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            ProgressDialog dialog = new ProgressDialog(context);
            dialog.setTitle("Please Wait");
            dialog.setMessage("Deleting " + destiTitle + ". . .");
            dialog.show();
            Log.d(TAG,"delete desti : Deleting from storage");
            StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(destiUrl);
            reference.delete()
                    .addOnSuccessListener(unused -> {
                        Log.d(TAG, "onSuccess : Succesfully deleted data");
                        DatabaseReference reference1 = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Destination");
                        reference1.child(destiId)
                                .removeValue()
                                .addOnSuccessListener(unused1 -> {
                                    Log.d(TAG, "onSuccess: data deleted from db");
                                    dialog.dismiss();
                                    Toast.makeText(context, "Destination Deleted Succesfully !", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.d(TAG, "onFAilure: error deleting data because of" + e.getMessage());
                                    dialog.dismiss();
                                    Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "onFailure: fail detele data due to" + e.getMessage());
                        dialog.dismiss();
                    });
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            Toast.makeText(context, "Destination Not Deleted", Toast.LENGTH_SHORT).show();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void addtoFavorite(Context context, String destiId){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            Toast.makeText(context,R.string.notLogin, Toast.LENGTH_SHORT).show();
        }else{
            long timestamp = System.currentTimeMillis();
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("destiId",destiId);
            hashMap.put("timestamp",timestamp);
            DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Favorites").child(destiId)
                    .setValue(hashMap)
                    .addOnSuccessListener(unused -> Toast.makeText(context,R.string.added_bookmark, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to Added favorites due to" + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
    public static void removeFavorite(Context context, String destiId){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null){
            Toast.makeText(context,R.string.notLogin, Toast.LENGTH_SHORT).show();
        }else{
            DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Favorites").child(destiId)
                    .removeValue()
                    .addOnSuccessListener(unused -> Toast.makeText(context,R.string.removed_bookmark, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to remove favorites due to" + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
