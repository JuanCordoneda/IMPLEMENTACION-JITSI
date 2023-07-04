package com.safecard.android.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.provider.Settings;
import android.util.Log;

import com.safecard.android.Config;

/**
 * Created by agaete on 30/05/17.
 */

public final class DeviceIdManager extends ContextWrapper {

    private String TAG = "DeviceIdManager";
    private String deviceId = "";

    public DeviceIdManager(Context base) {
        super(base);
        deviceId = Utils.getDeviceId(getApplicationContext());
        //Log.i(TAG, "DeviceIdManager deviceId: " +  deviceId);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getMd5DeviceId() {
        //Log.i(TAG, "getMd5DeviceId (" + deviceId + ") : " + Utils.md5(deviceId));
        return Utils.md5(deviceId);
    }

    public void create() {
        String android_id = Settings.Secure.getString(
                getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        this.deviceId = Utils.md5(System.currentTimeMillis() + android_id);
    }

    //no se use usa porque se persiste el device que devuelve el api
    public void persist() {
        Utils.setDeviceId(getApplicationContext(), deviceId);
    }

    public boolean exist() {
        return !deviceId.equals("");
    }

}