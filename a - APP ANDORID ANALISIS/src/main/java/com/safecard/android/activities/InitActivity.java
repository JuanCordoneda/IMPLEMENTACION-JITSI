package com.safecard.android.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.apicallers.ApiCallback;
import com.safecard.android.apicallers.InitApiCaller;

import org.json.JSONObject;

public class InitActivity extends AppCompatActivity {
    private static final String TAG = "InitActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        new InitApiCaller(getApplicationContext()).doCall(new ApiCallback() {
            @Override
            public void callSuccess(JSONObject response) {
                Intent intent = new Intent(InitActivity.this, LoginActivity.class);
                intent.putExtra("GOTO", Consts.AccessOrInvitationActivity);
                startActivity(intent);
                finish();
            }

            @Override
            public void callError(String errorType, String msg) {
            }
        });
    }
}
