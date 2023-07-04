package com.safecard.android.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.safecard.android.Config;
import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.apicallers.ApiCallback;
import com.safecard.android.apicallers.ValidateCodeApiCaller;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class VerifyActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView title;

    private TextView label_mobile, wrong_mobile, countdown_sms, countdown_call;
    private TextInputLayout text_input_layout_code;

    private Button btnVerificar;
    private EditText codigo_sms;
    private ProgressDialog pDialog;

    RequestVolley rqv;
    String mobile;

    ImageView retry_sms_img, callme_img;
    LinearLayout container_sms, container_callme;
    CountDownTimer countdown;

    //SmsListener smsListener;

    boolean alreadySmsReceived = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.verify);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*IntentFilter smsIntentFilter = new IntentFilter();
        smsIntentFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        smsListener = new SmsListener();
        smsListener.setOnSmsReceivedListener(this);
        registerReceiver(smsListener, smsIntentFilter);
        */

        title = (TextView) findViewById(R.id.toolbar_title);
        title.setText(R.string.activity_verify_title);

        pDialog = new ProgressDialog(VerifyActivity.this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage(getString(R.string.activity_verify_dialog_loading_wait_a_moment));
        pDialog.setCancelable(false);

        btnVerificar = (Button) findViewById(R.id.btnVerificar);

        text_input_layout_code = (TextInputLayout) findViewById(R.id.text_input_layout_code);
        codigo_sms = (EditText) findViewById(R.id.codigo_sms);
        codigo_sms.requestFocus();

        container_sms = (LinearLayout) findViewById(R.id.container_sms);
        container_callme = (LinearLayout) findViewById(R.id.container_callme);

        countdown_sms = (TextView) findViewById(R.id.countdown_sms);
        countdown_call = (TextView) findViewById(R.id.countdown_call);

        retry_sms_img = (ImageView) findViewById(R.id.retry_sms_img);
        callme_img = (ImageView) findViewById(R.id.callme_img);
        wrong_mobile = (TextView) findViewById(R.id.wrong_mobile);
        label_mobile = (TextView) findViewById(R.id.label_mobile);
        label_mobile.setText(label_mobile.getText().toString().replace("[MOBILE]", Utils.getDefaults("mobile_verificado", getApplicationContext())));

        codigo_sms.addTextChangedListener(new VerifyActivity.GenericTextWatcher(codigo_sms));

        countdown = new CountDownTimer(90000, 1000){
            public void onTick(long milisUntilFinished){

                int seconds = (int) (milisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                countdown_sms.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
                countdown_call.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));

                retry_sms_img.setColorFilter(getResources().getColor(R.color.strongray), PorterDuff.Mode.valueOf("SRC_IN"));
                callme_img.setColorFilter(getResources().getColor(R.color.strongray), PorterDuff.Mode.valueOf("SRC_IN"));

                container_sms.setClickable(false);
                container_callme.setClickable(false);

            }
            public void onFinish(){
                countdown_sms.setText("");
                countdown_call.setText("");
                retry_sms_img.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.valueOf("SRC_IN"));
                callme_img.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.valueOf("SRC_IN"));

                container_sms.setClickable(true);
                container_callme.setClickable(true);
            }
        };

        countdown.start();

        btnVerificar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String code = codigo_sms.getText().toString();
                verifyCode(code);
            }
        });

        wrong_mobile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent sms_intent = new Intent(VerifyActivity.this, SmsActivity.class);
                startActivity(sms_intent);
                finish();
            }
        });

        container_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = Config.ApiUrl + "generate_code/" + Utils.getDefaults("mobile_verificado", getApplicationContext()) +"/sms";

                pDialog.setMessage(getString(R.string.activity_verify_dialog_requesting_code));
                pDialog.show();

                rqv = new RequestVolley(url,getApplicationContext());

                rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {

                        if(pDialog != null && pDialog.isShowing()){
                            Utils.safeDismissDialog(VerifyActivity.this, pDialog);
                        }

                        try {
                            if(response.getString("result").equals("ACK")){
                                countdown.start();
                                Utils.showToast(getApplicationContext(), getString(R.string.activity_verify_dialog_sms_sent_wait_a_moment));
                            } else {
                                Utils.showToast(getApplicationContext(), getString(R.string.activity_verify_error_try_again));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(String errorType, String msg) {
                        if (!msg.equals("")){
                            Utils.showToast(getApplicationContext(), msg);
                        }

                        if(pDialog != null && pDialog.isShowing()){
                            Utils.safeDismissDialog(VerifyActivity.this, pDialog);
                        }
                    }
                });
            }
        });

        container_callme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = Config.ApiUrl + "generate_code/" + Utils.getDefaults("mobile_verificado", getApplicationContext()) +"/call";

                pDialog.setMessage(getString(R.string.activity_verify_dialog_requesting_call));
                pDialog.show();

                rqv = new RequestVolley(url,getApplicationContext());

                rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {

                        if(pDialog != null && pDialog.isShowing()){
                            Utils.safeDismissDialog(VerifyActivity.this, pDialog);
                        }
                        try {
                            if(response.getString("result").equals("ACK")){
                                countdown.start();
                                Utils.showToast(getApplicationContext(), getString(R.string.activity_verify_youll_get_a_call_in_a_moment));
                            } else {
                                Utils.showToast(getApplicationContext(), getString(R.string.activity_verify_error_try_again2));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(String errorType, String msg) {
                        if (!msg.equals("")){
                            Utils.showToast(getApplicationContext(), msg);
                        }

                        if(pDialog != null && pDialog.isShowing()){
                            Utils.safeDismissDialog(VerifyActivity.this, pDialog);
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        Utils.setDefaults("GOTO_INSTALL", String.valueOf(Consts.VerifyActivity), getApplicationContext());
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        //unregisterReceiver(smsListener);
        if(pDialog != null && pDialog.isShowing()){
            Utils.safeDismissDialog(VerifyActivity.this, pDialog);
        }
        pDialog = null;
    }

    @Override
    public void onBackPressed() {
    }

    /*@Override
    public void onSmsReceived(String message) {
        if (!alreadySmsReceived) {
            String code = message.replaceAll("\\D", "");
            verifyCode(code);
            alreadySmsReceived = true;
        }
    }*/

    public synchronized void verifyCode(String code){
        text_input_layout_code.setErrorEnabled(false);
        if (code.isEmpty()) {
            text_input_layout_code.setError(getString(R.string.activity_verify_required_field));
            text_input_layout_code.setErrorEnabled(true);
        }

        if(!text_input_layout_code.isErrorEnabled()){
            pDialog.setMessage(getString(R.string.activity_verify_checking_code));
            pDialog.show();
            new ValidateCodeApiCaller(getApplicationContext(), code)
                .doCall(new ApiCallback() {
                    @Override
                    public void callSuccess(JSONObject response) {
                        if(pDialog != null && pDialog.isShowing()){
                            Utils.safeDismissDialog(VerifyActivity.this, pDialog);
                        }

                        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void callError(String errorType, String msg) {
                        if (!msg.equals("")){
                            Utils.showToast(getApplicationContext(), msg);
                        }
                        if(pDialog != null && pDialog.isShowing()){
                            Utils.safeDismissDialog(VerifyActivity.this, pDialog);
                        }
                        text_input_layout_code.setError(getString(R.string.activity_verify_three_retries_msg));

                        if(errorType.equals("NACK")) {
                            text_input_layout_code.setError(msg);
                        }

                        text_input_layout_code.setErrorEnabled(true);
                    }
                });
        }
    }

    private class GenericTextWatcher implements TextWatcher {

        private View view;
        private GenericTextWatcher(View view){
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();

            switch(view.getId()){
                case R.id.codigo_sms:
                    if(text.equals("")){
                        text_input_layout_code.setError(getString(R.string.activity_verify_required_field2));
                        text_input_layout_code.setErrorEnabled(true);
                    } else if(text.length() == 6){
                        btnVerificar.performClick();
                    } else {
                        text_input_layout_code.setError(null);
                        text_input_layout_code.setErrorEnabled(false);
                    }
                    break;
            }
        }
    }
}
