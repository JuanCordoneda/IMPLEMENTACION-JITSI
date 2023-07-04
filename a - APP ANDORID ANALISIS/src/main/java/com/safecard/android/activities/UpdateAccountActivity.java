package com.safecard.android.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.safecard.android.Config;
import com.safecard.android.R;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by efajardo on 13-10-15.
 */
public class UpdateAccountActivity extends AppCompatActivity {
    private EditText edit_name, edit_last_name, edit_email;
    private TextInputLayout input_name, input_last_name, input_email;
    ProgressDialog pDialog;
    RequestVolley rqv;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_shape);

        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText(R.string.activity_edit_profile_title);

        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_last_name = (EditText) findViewById(R.id.edit_last_name);
        edit_email = (EditText) findViewById(R.id.edit_email);

        input_name = (TextInputLayout) findViewById(R.id.input_name);
        input_last_name = (TextInputLayout) findViewById(R.id.input_last_name);
        input_email = (TextInputLayout) findViewById(R.id.input_email);

        TextView user_name = (TextView) findViewById(R.id.user_name);

        pDialog = new ProgressDialog(UpdateAccountActivity.this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage(getString(R.string.activity_edit_profile_dialog_updating));
        pDialog.setCancelable(false);

        try{
            String login = Utils.getDefaults("login", getApplicationContext());
            JSONObject login_json = new JSONObject(login);
            user_name.setText(login_json.getString("name") + " " + login_json.getString("lastName"));

            edit_name.setText(login_json.getString("name"));
            edit_last_name.setText(login_json.getString("lastName"));
            edit_email.setText(login_json.getString("email")== null? "":login_json.getString("email"));
        } catch(JSONException e){
            e.printStackTrace();
        }

        Button btnEditAction = (Button) findViewById(R.id.btnEditAction);

        btnEditAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String name = edit_name.getText().toString().replace(",","");
                final String last_name = edit_last_name.getText().toString().replace(",","");
                final String email = edit_email.getText().toString().trim();

                if(name.equals("") || name.length() < 3){
                    input_name.setError(getString(R.string.activity_edit_profile_real_name_required));
                    input_name.setErrorEnabled(true);
                } else {
                    input_name.setError(null);
                    input_name.setErrorEnabled(false);
                }

                if(last_name.equals("") || last_name.length() < 3){
                    input_last_name.setError(getString(R.string.activity_edit_profile_real_lastname_required));
                    input_last_name.setErrorEnabled(true);
                } else {
                    input_last_name.setError(null);
                    input_last_name.setErrorEnabled(false);
                }

                if(email.equals("") || !Utils.isEmailValid(email)){
                    input_email.setError(getString(R.string.activity_edit_profile_valid_email_required));
                    input_email.setErrorEnabled(true);
                } else {
                    input_email.setError(null);
                    input_email.setErrorEnabled(false);
                }

                if (!input_name.isErrorEnabled() && !input_last_name.isErrorEnabled() && !input_email.isErrorEnabled()) {

                    String url = null;
                    try {
                        url = Config.ApiUrl + "actualizar/" + Utils.getMobile(getApplicationContext()) + "/" +
                                URLEncoder.encode(name, "UTF-8").replaceAll("\\+", "%20") + "/" +
                                URLEncoder.encode(last_name, "UTF-8").replaceAll("\\+", "%20") + "/" +
                                URLEncoder.encode(email.replaceAll("\\+", "%20"), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    pDialog.show();
                    rqv = new RequestVolley(url, getApplicationContext());
                    rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            if (pDialog != null && pDialog.isShowing()) {
                                Utils.safeDismissDialog(UpdateAccountActivity.this, pDialog);
                            }
                            try {
                                if (response.getString("result").equals("ACK")) {

                                    String login = Utils.getDefaults("login", getApplicationContext());
                                    JSONObject login_json = new JSONObject(login);
                                    login_json.put("name", name);
                                    login_json.put("lastName", last_name);
                                    login_json.put("email", email);
                                    Utils.setDefaults("login", login_json.toString(), getApplicationContext());

                                    Utils.showToast(getApplicationContext(), getString(R.string.activity_edit_profile_updated_succesfully));
                                    Intent profile = new Intent(getApplicationContext(), SettingsActivity.class);
                                    startActivityForResult(profile, 500);
                                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                                } else {
                                    if(!response.getString("msg").equals("")){
                                        Utils.showToast(getApplicationContext(), response.getString("msg"));
                                    } else {
                                        Utils.showToast(getApplicationContext(), getString(R.string.activity_edit_profile_error_while_updating_try_again));
                                    }
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
                                Utils.safeDismissDialog(UpdateAccountActivity.this, pDialog);
                            }
                        }
                    });
                }
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Utils.safeDismissDialog(UpdateAccountActivity.this, pDialog);
        pDialog = null;
    }
}
