package com.example.phonewearai;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class PermissionChecker {

    private static final int REQUEST_SENSOR_PERMISSION = 1;

    public static void checkPermissions(Context context, Activity activity){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BODY_SENSORS}, REQUEST_SENSOR_PERMISSION);
        }
    }
}
