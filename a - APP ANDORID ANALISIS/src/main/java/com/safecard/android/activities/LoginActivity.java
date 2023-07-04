package com.safecard.android.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;


public class LoginActivity extends AppCompatActivity implements OnClickListener {
    private String TAG = "LoginActivity";
    public static final String EXTRA_DATA = "EXTRA_DATA";

    private EditText pin;
    private Button button1, button2, button3, button4, button5, button6, button7, button8, button9, button0;
    private TextView circle1, circle2, circle3, circle4, circle11, circle22, circle33, circle44;
    private ImageView deletePin;
    int go_to = 0;
    JSONObject extraData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Bundle extras = getIntent().getExtras();
        if (extras != null && !extras.isEmpty()) {
            go_to = extras.getInt("GOTO", 0);
            extraData = new JSONObject();
            String extraDataString = extras.getString(LoginActivity.EXTRA_DATA, "");
            if(!extraDataString.equals("")) {
                try {
                    extraData =  new JSONObject(extraDataString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        //Log.i(TAG, "go_to:" + go_to);
        //Log.i(TAG, "extraData:" + extraData.toString());




        Executor executor = Executors.newSingleThreadExecutor();

        FragmentActivity activity = this;

        final BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    // user clicked negative button
                } else {
                    //TODO: Called when an unrecoverable error has been encountered and the operation is complete.
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                //TODO: Called when a biometric is recognized.
                redirections();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                //TODO: Called when a biometric is valid but not recognized.
            }
        });

        final BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.login_biometric_title))
                //.setSubtitle("Set the subtitle to display.")
                .setDescription(getString(R.string.login_biometric_description))
                .setNegativeButtonText(getString(R.string.login_biometric_cancel_button))
                .build();

        findViewById(R.id.biometric_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                biometricPrompt.authenticate(promptInfo);
            }
        });

        button0 = (Button) findViewById(R.id.button0);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);
        button6 = (Button) findViewById(R.id.button6);
        button7 = (Button) findViewById(R.id.button7);
        button8 = (Button) findViewById(R.id.button8);
        button9 = (Button) findViewById(R.id.button9);

        button0.setOnClickListener(this);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button5.setOnClickListener(this);
        button6.setOnClickListener(this);
        button7.setOnClickListener(this);
        button8.setOnClickListener(this);
        button9.setOnClickListener(this);

        pin = (EditText) findViewById(R.id.pin);

        deletePin = (ImageView) findViewById(R.id.deletePin);

        circle1 = (TextView) findViewById(R.id.circle1);
        circle2 = (TextView) findViewById(R.id.circle2);
        circle3 = (TextView) findViewById(R.id.circle3);
        circle4 = (TextView) findViewById(R.id.circle4);

        circle11 = (TextView) findViewById(R.id.circle11);
        circle22 = (TextView) findViewById(R.id.circle22);
        circle33 = (TextView) findViewById(R.id.circle33);
        circle44 = (TextView) findViewById(R.id.circle44);

        deletePin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (pin.getText().length() > 0) {
                    pin.setText(pin.getText().toString().substring(0, pin.getText().length() - 1));
                }

                circle1.setVisibility(View.VISIBLE);
                circle11.setVisibility(View.GONE);
                circle2.setVisibility(View.VISIBLE);
                circle22.setVisibility(View.GONE);
                circle3.setVisibility(View.VISIBLE);
                circle33.setVisibility(View.GONE);
                circle4.setVisibility(View.VISIBLE);
                circle44.setVisibility(View.GONE);

                switch (pin.length()) {
                    case 1:
                        circle1.setVisibility(View.GONE);
                        circle11.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        circle1.setVisibility(View.GONE);
                        circle2.setVisibility(View.GONE);
                        circle11.setVisibility(View.VISIBLE);
                        circle22.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        circle1.setVisibility(View.GONE);
                        circle2.setVisibility(View.GONE);
                        circle3.setVisibility(View.GONE);
                        circle11.setVisibility(View.VISIBLE);
                        circle22.setVisibility(View.VISIBLE);
                        circle33.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isAskForPinModeActive(this)) {
            redirections();
        }
    }


    @Override
    public void onClick(View v) {
        v.playSoundEffect(SoundEffectConstants.CLICK);

        String number = pin.getText().toString();

        switch (v.getId()) {
            case R.id.button1:
                pin.setText(number + "1");
                break;
            case R.id.button2:
                pin.setText(number + "2");
                break;
            case R.id.button3:
                pin.setText(number + "3");
                break;
            case R.id.button4:
                pin.setText(number + "4");
                break;
            case R.id.button5:
                pin.setText(number + "5");
                break;
            case R.id.button6:
                pin.setText(number + "6");
                break;
            case R.id.button7:
                pin.setText(number + "7");
                break;
            case R.id.button8:
                pin.setText(number + "8");
                break;
            case R.id.button9:
                pin.setText(number + "9");
                break;
            case R.id.button0:
                pin.setText(number + "0");
                break;
        }

        switch (pin.length()) {
            case 1:
                circle1.setVisibility(View.GONE);
                circle11.setVisibility(View.VISIBLE);
                break;
            case 2:
                circle2.setVisibility(View.GONE);
                circle22.setVisibility(View.VISIBLE);
                break;
            case 3:
                circle3.setVisibility(View.GONE);
                circle33.setVisibility(View.VISIBLE);
                break;
            case 4:
                circle4.setVisibility(View.GONE);
                circle44.setVisibility(View.VISIBLE);

                //Log.i(TAG,"Pin " + pin.getText().toString());
                final String pin_md5 = Utils.md5(pin.getText().toString(), true);

                String login = Utils.getDefaults("login", getApplicationContext());
                String password = "";
                try {
                    JSONObject login_json = new JSONObject(login);
                    password = login_json.getString("password");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (password.equals(pin_md5)) {
                    pin.setText("");
                    //Utils.setVolatile("signed_in", "true", this);
                    redirections();

                } else {
                    pin.setText("");
                    Utils.showToast(getApplicationContext(), getString(R.string.login_invalid_password));
                }
                circle1.setVisibility(View.VISIBLE);
                circle11.setVisibility(View.GONE);
                circle2.setVisibility(View.VISIBLE);
                circle22.setVisibility(View.GONE);
                circle3.setVisibility(View.VISIBLE);
                circle33.setVisibility(View.GONE);
                circle4.setVisibility(View.VISIBLE);
                circle44.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        // exit skipping "onBackPressed()"
        return;
    }

    public void redirections() {
        if (go_to == Consts.AccessOrInvitationActivity) {
            go_to = getAccessOrInvitationActivity();
        }
        Intent intent;
        if (Utils.getDefaults("finish_tour", getApplicationContext()) == null ||
                Utils.getDefaults("finish_tour", getApplicationContext()).equals("0")) {
            Utils.setDefaults("countries", null, getApplicationContext());
            intent = new Intent(getApplicationContext(), TourActivity.class);
        } else if (go_to == Consts.NotificationActivity) {
            //Log.i(TAG, "go_to NotificationActivity");
            intent = new Intent(getApplicationContext(), NotificationActivity.class);
        } else if (go_to == Consts.AccessActivity) {
            intent = new Intent(getApplicationContext(), AccessActivity.class);
            try {
                if(extraData.has("house_id") && extraData.getInt("house_id") > 0) {
                    intent.putExtra(AccessActivity.SELECT_PROPERTY, extraData.getInt("house_id"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }  else if (go_to == Consts.ParkingActivityBillings) {
            intent = new Intent(getApplicationContext(), ParkingActivity.class);
            intent.putExtra(ParkingActivity.SHOW_BILLINGS, true);
        } else if (go_to == Consts.InvitationActivity) {
            intent = new Intent(getApplicationContext(), InvitationActivity.class);
        } else if (go_to == Consts.AccessActivityMovements) {
            intent = new Intent(getApplicationContext(), AccessActivity.class);
            if(!extraData.equals("")) {
                try {
                    if(extraData.has("house_id") && extraData.getInt("house_id") > 0) {
                        intent.putExtra(AccessActivity.SELECT_PROPERTY, extraData.getInt("house_id"));
                        intent.putExtra(AccessActivity.SHOW_MOVEMENTS, true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            intent = new Intent(getApplicationContext(), InvitationActivity.class);
        }
        startActivity(intent);
        finish();
    }

    public int getAccessOrInvitationActivity() {
        int activity = Consts.AccessActivity;
        try {
            JSONObject user_json = new JSONObject(Utils.getDefaults("user", getApplicationContext()));
            JSONArray properties = new JSONArray();
            JSONArray students = new JSONArray();
            if(user_json.has("properties") && !user_json.getString("properties").equals("")) {
                properties = new JSONArray(user_json.getString("properties"));
            }
            if(user_json.has("students") && !user_json.getString("students").equals("")){
                students = new JSONArray(user_json.getString("students"));
            }
            if (properties.length() == 0 && students.length() == 0) {
                activity = Consts.InvitationActivity;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return activity;
    }

    public static boolean isAskForPinModeActive(Context ctx) {
        String locked = Utils.getDefaults("locked", ctx);
        return locked != null && locked.equals("1");
    }

}