package com.safecard.android;

import androidx.multidex.MultiDexApplication;
import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.safecard.android.utils.Foreground;
import com.safecard.android.utils.LocationProvider;
import com.safecard.android.utils.ServiceUtils;
import com.safecard.android.utils.Utils;


public class MainApp extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        Foreground.init(this);

        LocationProvider.loadInstance(getApplicationContext());
        Utils.mixpanel = MixpanelAPI.getInstance(getApplicationContext(), Config.mixpanelToken);

        ServiceUtils.loadCrashReporter(getApplicationContext());

        if(Config.DEBUG) {
            Log.i("MainApp","DEBUGGING ON");
        }else{
            Log.i("MainApp","DEBUGGING OFF");
        }
    }
}