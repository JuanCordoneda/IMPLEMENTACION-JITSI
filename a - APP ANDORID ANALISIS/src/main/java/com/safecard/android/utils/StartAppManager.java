package com.safecard.android.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.util.Log;

import com.safecard.android.BuildConfig;
import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.activities.RegisterActivity;
import com.safecard.android.activities.RegisterPinActivity;
import com.safecard.android.activities.SmsActivity;
import com.safecard.android.activities.TermsActivity;
import com.safecard.android.activities.TourActivity;
import com.safecard.android.activities.VerifyActivity;
import com.safecard.android.apicallers.ActualizarDeviceApiCaller;
import com.safecard.android.apicallers.ApiCallback;
import com.safecard.android.apicallers.InitApiCaller;
import com.safecard.android.model.Models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

public class StartAppManager {
    private static final String TAG = "StartAppManager";

    public interface StartAppManagerCallback {
        void onNoIssueFinish();
    }

    public StartAppManager(){ }

    public void process(final Activity activity, final StartAppManagerCallback callback){

        if(BuildConfig.SERVICE_USED.equals("HUAWEI")
                && !ServiceUtils.isHmsAvailable(activity)){
            showDialogNoHMSFound(activity);
            return;
        }

        informMixpanelAppStarted(activity);

        if (Utils.getDefaults("user", activity.getApplicationContext()) == null) {
            install(activity);
            return;
        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                actualizarDeviceApiCall(activity, callback);
            }
        }).start();
    }


    protected void actualizarDeviceApiCall(final Activity activity, final StartAppManagerCallback callback) {
        ActualizarDeviceApiCaller actualizarDeviceApiCaller
                = new ActualizarDeviceApiCaller(activity.getApplicationContext());

        actualizarDeviceApiCaller.doCall(new ApiCallback() {
            @Override
            public void callSuccess(JSONObject response) {
                JSONObject initData = Utils.getDefaultJSONObject("init_data", activity.getApplicationContext());
                Log.d(TAG, "initData:"+ initData);

                if(!initData.toString().equals("{}")) {
                    next(activity, callback);
                    return;
                }

                Log.d(TAG, "SplashScreenActivity InitApiCaller");
                new InitApiCaller(activity.getApplicationContext()).doCall(new ApiCallback() {
                    @Override
                    public void callSuccess(JSONObject response) {
                        Models.forceReloadModel(activity.getApplicationContext());
                        Log.d(TAG, "callSuccess ActualizarDeviceReady");
                        next(activity, callback);
                    }

                    @Override
                    public void callError(String errorType, String msg) {
                        Log.d(TAG, "callError ActualizarDeviceReady");
                        next(activity, callback);
                    }
                });
            }

            @Override
            public void callError(String errorType, String msg) {
                Log.d(TAG, "actualizarDeviceApiCall callError");
            }
        });

    }

    protected void next(final Activity activity, final StartAppManagerCallback callback) {

        if(areTermsUpdated(activity)){
            activity.startActivity(new Intent(activity, TermsActivity.class));
            activity.finish();
            return;
        }

        if(Utils.appBlockChecks(activity)){
            return;
        }

        callback.onNoIssueFinish();
    }

    protected void informMixpanelAppStarted(final Activity activity) {
        try {
            JSONObject json = Utils.getDefaultJSONObject("login", activity.getApplicationContext());
            if(json.has("mobile")){
                String userName = json.getString("name") + " " + json.getString("lastName");
                String email = json.getString("email");
                String mobile = json.getString("mobile");

                /*Registro de usuario en mixpanel*/
                Utils.mixpanel.identify(mobile);
                Utils.mixpanel.getPeople().identify(mobile);
                JSONObject prop_person = new JSONObject();
                prop_person.put("mobile", mobile);
                prop_person.put("name", userName);
                prop_person.put("email", email);
                Utils.mixpanel.getPeople().set(prop_person);
                Utils.mixpanel.track("APP_STARTED");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void showDialogNoHMSFound(final Activity activity){
        String title = activity.getString(R.string.activity_splash_screen_hms_not_found_title);
        String message = activity.getString(R.string.activity_splash_screen_hms_not_found_message);
        AlertDialog.Builder infoDialog = new AlertDialog.Builder(activity)
                .setPositiveButton(R.string.activity_splash_screen_hms_not_found_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        activity.finish();
                    }
                })
                .setTitle(Html.fromHtml(title))
                .setMessage(Html.fromHtml(message))
                .setCancelable(false);
        infoDialog.create().show();
    }

    protected void install(final Activity activity){

        int last_activity = Utils.getDefaults("GOTO_INSTALL",
                activity.getApplicationContext()) == null ?
                Consts.TourActivity : Integer.parseInt(
                Utils.getDefaults("GOTO_INSTALL", activity.getApplicationContext()));

        switch(last_activity){
            case Consts.SmsActivity:
                activity.startActivity(new Intent(activity, SmsActivity.class));
                break;
            case Consts.ChangePinActivity:
                activity.startActivity(new Intent(activity, RegisterPinActivity.class));
                break;
            case Consts.VerifyActivity:
                activity.startActivity(new Intent(activity, VerifyActivity.class));
                break;
            case Consts.RegisterActivity:
                activity.startActivity(new Intent(activity, RegisterActivity.class));
                break;
            case Consts.TourActivity:
            default:
                activity.startActivity(new Intent(activity, TourActivity.class));
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

        activity.finish();
    }

    protected boolean areTermsUpdated(final Activity activity) {
        int lastTermsVersion = Utils.getDefaultInt("last_terms_version", activity.getApplicationContext());
        int lastTermsVersionAccepted = Utils.getDefaultInt("last_terms_version_accepted", activity.getApplicationContext());
        Log.i(TAG, "Splash lastTermsVersion:" + lastTermsVersion + " lastTermsVersionAccepted:" + lastTermsVersionAccepted);
        return lastTermsVersionAccepted < 1 || lastTermsVersion > lastTermsVersionAccepted;
    }
}