package com.binus.pekalongancityguide.Misc;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

public class AlphaTransformation extends BitmapTransformation {

    private static final String ID = "com.example.myapp.AlphaTransformation";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    private final float opacity;

    public AlphaTransformation(float opacity) {
        this.opacity = opacity;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap source, int outWidth, int outHeight) {
        Bitmap.Config config = source.getConfig() != null ? source.getConfig() : Bitmap.Config.ARGB_8888;
        Bitmap bitmap = pool.get(outWidth, outHeight, config);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        ColorFilter filter = new PorterDuffColorFilter(Color.TRANSPARENT, PorterDuff.Mode.DST_IN);
        paint.setColorFilter(filter);
        canvas.drawBitmap(source, 0f, 0f, paint);
        paint.setColorFilter(null);
        paint.setAlpha(Math.round(255 * opacity));
        canvas.drawBitmap(source, 0f, 0f, paint);
        return bitmap;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AlphaTransformation && ((AlphaTransformation) o).opacity == opacity;
    }

    @Override
    public int hashCode() {
        return ID.hashCode() + Float.valueOf(opacity).hashCode();
    }
}

