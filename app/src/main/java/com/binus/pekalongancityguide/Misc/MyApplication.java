package com.binus.pekalongancityguide.Misc;

import static com.binus.pekalongancityguide.Layout.AddDestination.TAG;
import static com.binus.pekalongancityguide.Misc.Constants.MAX_BYTES_IMAGE;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.binus.pekalongancityguide.Adapter.AdminDestinationAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.Locale;

public class MyApplication extends Application {
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
                        Log.d(TAG, "onFailure: fail detele data due to" + e.getMessage());
                        dialog.dismiss();
                    }
                });

    }
}