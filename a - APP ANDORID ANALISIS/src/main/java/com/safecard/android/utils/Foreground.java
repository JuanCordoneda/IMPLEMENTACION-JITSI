package com.safecard.android.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.safecard.android.Consts;
import com.safecard.android.Config;
import com.safecard.android.activities.LoginActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Usage:
 *
 * 1. Get the Foreground Singleton, passing a Context or Application object unless you
 * are sure that the Singleton has definitely already been initialised elsewhere.
 *
 * 2.a) Perform a direct, synchronous check: Foreground.isForeground() / .isBackground()
 *
 * or
 *
 * 2.b) Register to be notified (useful in Service or other non-UI components):
 *
 *   Foreground.Listener myListener = new Foreground.Listener(){
 *       public void onBecameForeground(){
 *           // ... whatever you want to do
 *       }
 *       public void onBecameBackground(){
 *           // ... whatever you want to do
 *       }
 *   }
 *
 *   public void onCreate(){
 *      super.onCreate();
 *      Foreground.get(this).addListener(listener);
 *   }
 *
 *   public void onDestroy(){
 *      super.onCreate();
 *      Foreground.get(this).removeListener(listener);
 *   }
 */
public class Foreground implements Application.ActivityLifecycleCallbacks {
    public static final String TAG = "Foreground";

    public static final String PREVENT_LOGIN_SCREEN = "PREVENT_LOGIN_SCREEN";

    public static final long CHECK_DELAY = 500;
    public Date startDate, endDate;

    public interface Listener {

        public void onBecameForeground();

        public void onBecameBackground();

    }

    private static Foreground instance;

    public static boolean foreground = false, paused = true;
    private Handler handler = new Handler();
    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    private Runnable check;

    /**
     * Its not strictly necessary to use this method - _usually_ invoking
     * get with a Context gives us a path to retrieve the Application and
     * initialise, but sometimes (e.g. in test harness) the ApplicationContext
     * is != the Application, and the docs make no guarantees.
     *
     * @param application
     * @return an initialised Foreground instance
     */
    public static Foreground init(Application application){
        if (instance == null) {
            instance = new Foreground();
            application.registerActivityLifecycleCallbacks(instance);
        }
        return instance;
    }

    public static Foreground get(Application application){
        if (instance == null) {
            init(application);
        }
        return instance;
    }

    public static Foreground get(Context ctx){
        if (instance == null) {
            Context appCtx = ctx.getApplicationContext();
            if (appCtx instanceof Application) {
                init((Application)appCtx);
            }
            throw new IllegalStateException(
                    "Foreground is not initialised and " +
                            "cannot obtain the Application object");
        }
        return instance;
    }

    public static Foreground get(){
        if (instance == null) {
            throw new IllegalStateException(
                    "Foreground is not initialised - invoke " +
                            "at least once with parameterised init/get");
        }
        return instance;
    }

    public boolean isForeground(){
        return foreground;
    }

    public boolean isBackground(){
        return !foreground;
    }

    public void addListener(Listener listener){
        listeners.add(listener);
    }

    public void removeListener(Listener listener){
        listeners.remove(listener);
    }

    public String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    @Override
    public void onActivityResumed(final Activity activity) {
        Context ctx = activity.getApplicationContext();
        paused = false;
        boolean wasBackground = !foreground;
        foreground = true;

        if (check != null)
            handler.removeCallbacks(check);

        if (wasBackground) {
            Log.i(TAG, "went foreground");

            if(Utils.getDefaults("startDateForeground", activity.getApplicationContext()) != null){

                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                try {
                    endDate = sdfDate.parse(getCurrentTimeStamp());
                    startDate = sdfDate.parse(Utils.getDefaults("startDateForeground", activity.getApplicationContext()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                long diff = endDate.getTime() - startDate.getTime();
                long diffSeconds = diff / 1000;
                Log.i(TAG, diffSeconds + " sec");

                Bundle extras = activity.getIntent().getExtras();
                int go_to = 0;

                boolean preventLogin = false;
                if(extras!= null && !extras.isEmpty()){
                    go_to = extras.getInt("GOTO");
                    preventLogin = extras.getBoolean(Foreground.PREVENT_LOGIN_SCREEN, false);
                }

                if(go_to == 0 && !preventLogin) {
                    String user = Utils.getDefaults("user", ctx);
                    if ((diffSeconds > Config.time_restart || LoginActivity.isAskForPinModeActive(ctx)) && user != null) {
                        if (diffSeconds > Config.time_restart){
                            Utils.mixpanel.track("APP_STARTED");
                        }
                        Log.i(TAG, "LoginActivity (AccessOrInvitationActivity)");
                        Intent intent = new Intent(ctx, LoginActivity.class);
                        intent.putExtra("GOTO", Consts.AccessOrInvitationActivity);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                }

                //else if (go_to > 0){
                  //  Intent intent = new Intent(ctx, LoginActivity.class);
                  //  intent.putExtra("GOTO", go_to);
                  //  activity.startActivity(intent);
                  //}
            }

            for (Listener l : listeners) {
                try {
                    l.onBecameForeground();
                } catch (Exception exc) {
                    Log.e(TAG, "Listener threw exception!", exc);
                }
            }
        } else {
            Log.i(TAG, "still foreground");
        }
    }

    @Override
    public void onActivityPaused(final Activity activity) {
        paused = true;

        if (check != null)
            handler.removeCallbacks(check);

        handler.postDelayed(check = new Runnable(){
            @Override
            public void run() {
                if (foreground && paused) {
                    foreground = false;
                    Log.i(TAG, "went background");

                    Utils.setDefaults("startDateForeground", getCurrentTimeStamp(), activity.getApplicationContext());
                    Log.i(TAG, getCurrentTimeStamp() + " hora paused");

                    for (Listener l : listeners) {
                        try {
                            l.onBecameBackground();
                        } catch (Exception exc) {
                            Log.e(TAG, "Listener threw exception!", exc);
                        }
                    }
                } else {
                    Log.i(TAG, "still foreground");
                }
            }
        }, CHECK_DELAY);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}
}
