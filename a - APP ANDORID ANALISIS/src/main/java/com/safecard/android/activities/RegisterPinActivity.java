package com.safecard.android.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.safecard.android.Config;
import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.apicallers.ApiCallback;
import com.safecard.android.apicallers.InitApiCaller;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class RegisterPinActivity extends AppCompatActivity {

	private static final String TAG = "RegisterPinActivity";
	private EditText newPin, confirmPin;
	private TextInputLayout input_new_pin, input_confirm_pin;
	String mobile = "";
	private ProgressDialog pDialog;

	RequestVolley rqv;
	JSONObject user_json;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MixpanelAPI mixpanel = MixpanelAPI.getInstance(getApplicationContext(), Config.mixpanelToken);

		setContentView(R.layout.activity_register_pin);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
		title.setText(getString(R.string.activity_register_pin_title));

		//Intent intent = getIntent();
		TextView lblName = (TextView) findViewById(R.id.lblName);

		newPin = (EditText) findViewById(R.id.registerNewPin);
		confirmPin = (EditText) findViewById(R.id.registerConfirmPin);

		input_new_pin = (TextInputLayout) findViewById(R.id.input_new_pin);
		input_confirm_pin = (TextInputLayout) findViewById(R.id.input_confirm_pin);

		input_new_pin.setErrorEnabled(false);
		input_confirm_pin.setErrorEnabled(false);

		Button update_pin = (Button) findViewById(R.id.update_pin);

		pDialog = new ProgressDialog(RegisterPinActivity.this);
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog.setMessage(getString(R.string.activity_register_pin_updating_pin));
		pDialog.setCancelable(false);

		String user = Utils.getDefaults("login", RegisterPinActivity.this);

		try {
			user_json = new JSONObject(user);

			//Registro de usuario en mixpanel
			mixpanel.identify(user_json.getString("mobile"));
			mixpanel.getPeople().identify(user_json.getString("mobile"));
			JSONObject prop_person = new JSONObject();
			prop_person.put("mobile", user_json.getString("mobile"));
			prop_person.put("name", user_json.getString("name")+" "+user_json.getString("lastName"));
			prop_person.put("email", user_json.getString("email"));
			mixpanel.getPeople().set(prop_person);

			lblName.setText(String.format("%s %s", user_json.getString("name"), user_json.getString("lastName")));
			mobile = user_json.getString("mobile");

		} catch (JSONException e) {
			e.printStackTrace();
		}

		update_pin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Registrar();
			}
		});
	}

	@Override
	protected void onPause(){
		super.onPause();
		Utils.setDefaults("GOTO_INSTALL", String.valueOf(Consts.RegisterActivity), getApplicationContext());
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();

		// java.lang.IllegalArgumentException: View not attached to window manager
		if (pDialog != null && pDialog.isShowing()) {
			Utils.safeDismissDialog(RegisterPinActivity.this, pDialog);
		}
		pDialog = null;
	}
	@Override
	public void onBackPressed() {
	}

	private void Registrar(){
		try {
			final String new_pin = newPin.getText().toString();
			String confirm_pin = confirmPin.getText().toString();

			if(new_pin.equals("") || new_pin.length() != 4){
				input_new_pin.setError(getString(R.string.activity_register_pin_pin_must_have_four_of_length));
				input_new_pin.setErrorEnabled(true);
			} else {
				input_new_pin.setError(null);
				input_new_pin.setErrorEnabled(false);
			}

			if(confirm_pin.equals("") || confirm_pin.length() != 4){
				input_confirm_pin.setError(getString(R.string.activity_register_pin_pin_must_have_four_of_length2));
				input_confirm_pin.setErrorEnabled(true);
			} else if(!confirm_pin.equals(new_pin)){
				input_confirm_pin.setError(getString(R.string.activity_register_pin_pins_are_diferents));
				input_confirm_pin.setErrorEnabled(true);
			} else {
				input_confirm_pin.setError(null);
				input_confirm_pin.setErrorEnabled(false);
			}

			if(!input_new_pin.isErrorEnabled() && !input_confirm_pin.isErrorEnabled()){

				pDialog.show();
				String url_register = Config.ApiUrl + "registrar/" + mobile + "/" +
						URLEncoder.encode(user_json.getString("name"), "UTF-8").replaceAll("\\+", "%20") + "/" +
						URLEncoder.encode(user_json.getString("lastName"),"UTF-8").replaceAll("\\+", "%20") + "/" +
						new_pin + "/" + URLEncoder.encode(user_json.getString("email"), "UTF-8").replaceAll("\\+", "%20");
				rqv = new RequestVolley(url_register, getApplicationContext());
				//Log.d(TAG, "registrar requestApi");
				rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
					@Override
					public void onSuccess(JSONObject response) {
						try {
							if(response.getString("result").equals("ACK")){
								//Log.d(TAG, "registrar ACK");

								String login = Utils.getDefaults("login", getApplicationContext());
								JSONObject user_json = new JSONObject(login);
								user_json.put("password", Utils.md5(newPin.getText().toString(), true));
								Utils.setDefaults("login", user_json.toString(), getApplicationContext());
								Utils.showToast(getApplicationContext(), getString(R.string.activity_register_pin_pin_is_successfully_updated));
								new InitApiCaller(getApplicationContext()).doCall(new ApiCallback() {
									@Override
									public void callSuccess(JSONObject response) {
										if(pDialog != null && pDialog.isShowing()){
											Utils.safeDismissDialog(RegisterPinActivity.this, pDialog);
										}
										Utils.setDefaultBoolean("otherDevice", false, getApplicationContext());
										Intent intent = new Intent(getApplicationContext(), SplashScreenActivity.class);
										startActivity(intent);
										finish();
									}

									@Override
									public void callError(String errorType, String msg) {
										if (pDialog != null && pDialog.isShowing()) {
											Utils.safeDismissDialog(RegisterPinActivity.this, pDialog);
										}
										Utils.showToast(getApplicationContext(), "Init error");
									}
								});
							} else {
								if (pDialog != null && pDialog.isShowing()) {
									Utils.safeDismissDialog(RegisterPinActivity.this, pDialog);
								}
								//Log.d(TAG, "registrar NACK");
								Utils.showToast(getApplicationContext(), response.getString("msg"));
							}
						} catch (JSONException e) {
							if (pDialog != null && pDialog.isShowing()) {
								Utils.safeDismissDialog(RegisterPinActivity.this, pDialog);
							}
							Utils.showToast(getApplicationContext(), "Registration response is not json");
							//Log.d(TAG, "registrar JSONException");
							e.printStackTrace();
						}
					}

					@Override
					public void onError(String errorType, String msg) {
						if (pDialog != null && pDialog.isShowing()) {
							Utils.safeDismissDialog(RegisterPinActivity.this, pDialog);
						}
						//Log.d(TAG, "registrar onError: " + msg);
						if (!msg.equals("")) {
							Utils.showToast(getApplicationContext(), msg);
						}else{
							Utils.showToast(getApplicationContext(), "Registration error");
						}
					}
				});
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Utils.showToast(getApplicationContext(), "Encoding error");
		} catch(JSONException e){
			if(pDialog != null && pDialog.isShowing()){
				Utils.safeDismissDialog(RegisterPinActivity.this, pDialog);
			}
			e.printStackTrace();
			Utils.showToast(getApplicationContext(), "Parse json error");
		}
	}
}
