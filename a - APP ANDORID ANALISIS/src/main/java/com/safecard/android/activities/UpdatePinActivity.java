package com.safecard.android.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.safecard.android.Config;
import com.safecard.android.R;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by efajardo on 13-10-15.
 */
public class UpdatePinActivity extends AppCompatActivity {
    private Button btnChangePinAction;

    private EditText newPin, confirmPin, oldPin;
    private TextInputLayout input_old_pin, input_new_pin, input_confirm_pin;
    private ProgressDialog pDialog;
    RequestVolley rqv;

    String login;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_pin);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_shape);

        oldPin = (EditText) findViewById(R.id.registerOldPin);
        newPin = (EditText) findViewById(R.id.registerNewPin);
        confirmPin = (EditText) findViewById(R.id.registerConfirmPin);

        input_old_pin = (TextInputLayout) findViewById(R.id.input_old_pin);
        input_new_pin = (TextInputLayout) findViewById(R.id.input_new_pin);
        input_confirm_pin = (TextInputLayout) findViewById(R.id.input_confirm_pin);

        input_old_pin.setErrorEnabled(false);
        input_new_pin.setErrorEnabled(false);
        input_confirm_pin.setErrorEnabled(false);

        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        TextView pass_recovery = (TextView) findViewById(R.id.pass_recovery);
        title.setText(R.string.activity_update_pin_title);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });

        pass_recovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://app.safecard.cl/users/pass_recovery"));
                startActivity(browserIntent);
            }
        });

        btnChangePinAction = (Button) findViewById(R.id.btnChangePinAction);
    }
    @Override
    public void onResume() {
        super.onResume();

        pDialog = new ProgressDialog(UpdatePinActivity.this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage(getString(R.string.activity_update_pin_updating));
        pDialog.setCancelable(false);

        btnChangePinAction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                String old_pin = oldPin.getText().toString();
                final String new_pin = newPin.getText().toString();
                String confirm_pin = confirmPin.getText().toString();

                if(old_pin.equals("")){
                    input_old_pin.setError(getString(R.string.activity_update_pin_current_pin_must_have_four_of_length));
                    input_old_pin.setErrorEnabled(true);
                } else {
                    input_old_pin.setError(null);
                    input_old_pin.setErrorEnabled(false);
                }

                if(new_pin.equals("") || new_pin.length() != 4){
                    input_new_pin.setError(getString(R.string.activity_update_pin_new_pin_must_have_four_of_length));
                    input_new_pin.setErrorEnabled(true);
                } else {
                    input_new_pin.setError(null);
                    input_new_pin.setErrorEnabled(false);
                }

                if(confirm_pin.equals("") || confirm_pin.length() != 4){
                    input_confirm_pin.setError(getString(R.string.activity_update_pin_confirmation_pin_must_have_four_of_length));
                    input_confirm_pin.setErrorEnabled(true);
                } else if(!confirm_pin.equals(new_pin)){
                    input_confirm_pin.setError(getString(R.string.activity_update_pin_pins_are_diferents));
                    input_confirm_pin.setErrorEnabled(true);
                } else {
                    input_confirm_pin.setError(null);
                    input_confirm_pin.setErrorEnabled(false);
                }

                if (!input_new_pin.isErrorEnabled() && !input_old_pin.isErrorEnabled() && !input_confirm_pin.isErrorEnabled()){

                    String url = Config.ApiUrl + "actualizar_pin/" +
                            Utils.getMobile(getApplicationContext())  + "/" +
                            old_pin + "/" + new_pin;
                    ///api/actualizar_pin/:mobile/:old_pin/:new_pin
                    pDialog.show();
                    rqv = new RequestVolley(url, getApplicationContext());
                    rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            if (pDialog != null && pDialog.isShowing()) {
                                Utils.safeDismissDialog(UpdatePinActivity.this, pDialog);
                            }
                            try {
                                if (response.getString("result").equals("ACK")) {
                                    login = Utils.getDefaults("login", getApplicationContext());
                                    JSONObject login_json = new JSONObject(login);
                                    login_json.put("password", Utils.md5(new_pin, true));
                                    Utils.setDefaults("login", login_json.toString(), getApplicationContext());

                                    Utils.showToast(getApplicationContext(), getString(R.string.activity_update_pin_pin_is_successfully_updated));
                                    Intent profile = new Intent(getApplicationContext(), SettingsActivity.class);
                                    startActivityForResult(profile, 500);
                                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                                } else {
                                    input_old_pin.setError(getString(R.string.activity_update_pin_pin_is_invalid_try_again));
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(String errorType, String msg) {
                            if (!msg.equals("")) {
                                Utils.showToast(getApplicationContext(), msg);
                            }
                            if (pDialog != null && pDialog.isShowing()) {
                                Utils.safeDismissDialog(UpdatePinActivity.this, pDialog);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Utils.safeDismissDialog(UpdatePinActivity.this, pDialog);
        pDialog = null;
    }

}
