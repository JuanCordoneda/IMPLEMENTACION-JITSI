package com.safecard.android.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class PhoneCallManager {
    private static final String TAG = "PhoneCallManager";

    public static void call(String number, Activity activity) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        activity.startActivity(intent);
    }

    public static boolean hasPermission(Activity activity) {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity activity, int responseConstant) {
        Log.i(TAG, "requestPermission");
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CALL_PHONE}, responseConstant);
    }

}
