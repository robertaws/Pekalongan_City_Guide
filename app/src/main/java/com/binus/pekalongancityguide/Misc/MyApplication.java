package com.binus.pekalongancityguide.Misc;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.binus.pekalongancityguide.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static final String formatTimeStamp(long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);
        String date = DateFormat.format("dd/MM/yyyy",calendar).toString();
        return date;
    }
    public static void deleteDesti(Context context, String destiId, String destiUrl, String destiTitle){
        String TAG = "DELETE_DESTI_TAG";
        Log.d(TAG,"delete desti : Deleting..");
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTitle("Please Wait");
        dialog.setMessage("Deleting "+destiTitle+". . .");
        dialog.show();
        Log.d(TAG,"delete desti : Deleting from storage");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(destiUrl);
        reference.delete()
                .addOnSuccessListener(unused -> {

                    Log.d(TAG, "onSuccess : Succesfully deleted data");
                    DatabaseReference reference1 = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Destination");
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
            DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
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
            DatabaseReference reference = FirebaseDatabase.getInstance("https://pekalongan-city-guide-5bf2e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Favorites").child(destiId)
                    .removeValue()
                    .addOnSuccessListener(unused -> Toast.makeText(context,R.string.removed_bookmark, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to remove favorites due to" + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
