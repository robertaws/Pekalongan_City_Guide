package com.binus.pekalongancityguide.Misc;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    private static boolean showToast = true;

    public static void setToastEnabled(boolean enabled) {
        showToast = enabled;
    }

    public static void showToast(Context context, String message, int duration) {
        if (showToast) {
            Toast.makeText(context, message, duration).show();
        }
    }

}
