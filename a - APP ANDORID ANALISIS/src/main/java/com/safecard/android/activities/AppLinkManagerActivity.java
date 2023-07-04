package com.safecard.android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.utils.Foreground;


public class AppLinkManagerActivity extends AppCompatActivity {
    private static final String TAG = "AppLinkManagerActivity";
    public static final String SELECT_PROPERTY = "SELECT_PROPERTY";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");

        if(isAppStartedFromURI()){
            Log.i(TAG,"isAppStartedFromURI");
            final int propertyId = getPropertyIdFromIntentURI(getIntent());

            Intent intent = new Intent(this, SplashScreenActivity.class);
            intent.putExtra(Foreground.PREVENT_LOGIN_SCREEN, true);
            intent.putExtra("LOGIN_GOTO", Consts.AccessActivity);
            intent.putExtra(AppLinkManagerActivity.SELECT_PROPERTY, propertyId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }

        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
        return;
    }

    protected boolean isAppStartedFromURI() {
        Uri data = getIntent().getData();
        if (data != null){
            String host = data.getHost();
            String path = data.getPath();

            if(host != null && path != null
                    && host.equals("www.safecard.cl")
                    && path.startsWith("/app")) {
                return true;
            }
        }
        return false;
    }

    protected int getPropertyIdFromIntentURI(Intent intent) {
        try {
            return Integer.parseInt(intent.getData().getQueryParameter("property_id"));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG,"onNewIntent");
    }

}