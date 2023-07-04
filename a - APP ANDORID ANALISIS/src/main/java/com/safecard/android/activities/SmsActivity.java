package com.safecard.android.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.safecard.android.Config;
import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.apicallers.ApiCallback;
import com.safecard.android.apicallers.CountriesApiCaller;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SmsActivity extends AppCompatActivity {

	String login;

	// Identificador de la instancia del servicio de GCM al cual accedemos
	//private static final String SENDER_ID = "666654194362";
	// Clase que da acceso a la api de GCM
	//private GoogleCloudMessaging gcm;
	// Identificador de registro

	private Button btnConfirmar;
	private EditText codeCountry;
	private EditText mMobile;
	private TextInputLayout mInputMobile;
	private Toolbar toolbar;
	JSONArray countries_array;
	private Spinner spinner_sel_country;
	private ProgressDialog pDialog;

	private TextView title;

	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);

		pDialog = new ProgressDialog(SmsActivity.this);
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog.setMessage(getString(R.string.activity_sms_dialog_loading_wait_a_moment));
		pDialog.setCancelable(false);

		setContentView(R.layout.sms);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		title = (TextView) toolbar.findViewById(R.id.toolbar_title);

		mMobile = (EditText) findViewById(R.id.mobile);
		mInputMobile = (TextInputLayout) findViewById(R.id.input_mobile);

		mMobile.addTextChangedListener(new SmsActivity.GenericTextWatcher(mMobile));

		btnConfirmar = (Button) findViewById(R.id.btnConfirmar);
		codeCountry = (EditText) findViewById(R.id.codeCountry);
		spinner_sel_country = (Spinner) findViewById(R.id.sel_countries);

		title.setText(R.string.activity_sms_title);

	}

	@Override
	protected void onPause(){
		super.onPause();
		Utils.setDefaults("GOTO_INSTALL", String.valueOf(Consts.SmsActivity), getApplicationContext());
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();

		Utils.safeDismissDialog(SmsActivity.this, pDialog);
		pDialog = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		verifiedPhoneNumber();
	}
	
	@Override
	public void onBackPressed() {
	}

	public void processCountries() {
		try{
			JSONObject countries = Utils.getDefaultJSONObject("countries", getApplicationContext());

			countries_array = new JSONArray();
			if (countries.has("countries")){
				countries_array = countries.getJSONArray("countries");
			}

			if (countries_array.length() == 0){
				JSONObject country = new JSONObject();
				country.put("iso2","CL");
				country.put("calling_code","56");
				country.put("short_name","Chile");
				country.put("phone_validation_regex","^9\\d{8}$");
				countries_array.put(country);
			}

			TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String CountryIso = tMgr.getSimCountryIso().toUpperCase();
			if (CountryIso.equals("")) {
				CountryIso = "CL";
			}

			int pos = -1;
			String calling_code = "56";
			List<String> countriesList = new ArrayList<String>();
			for (int i = 0; i < countries_array.length(); i++) {
				JSONObject country = countries_array.getJSONObject(i);
				if (CountryIso.equals(country.getString("iso2"))) {
					pos = i;
					calling_code = country.getString("calling_code");
				}
				countriesList.add(country.getString("short_name"));
			}
			ArrayAdapter dataAdapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_item, countriesList);
			dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
			spinner_sel_country.setAdapter(dataAdapter);
			if (pos >= 0) {
				spinner_sel_country.setSelection(pos);
			}
			codeCountry.setText("+" + calling_code);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void verifiedPhoneNumber(){
		pDialog.show();
		// Llamada para actualizar lista de paises
		new CountriesApiCaller(getApplicationContext()).doCall(new ApiCallback() {
			@Override
			public void callSuccess(JSONObject response) {
				if(pDialog != null && pDialog.isShowing()){
					Utils.safeDismissDialog(SmsActivity.this, pDialog);
				}
				processCountries();
				btnConfirmar.setEnabled(true);
			}

			@Override
			public void callError(String errorType, String msg) {
				if (!msg.equals("")) {
					Utils.showToast(getApplicationContext(), msg);
				}
				if(pDialog != null && pDialog.isShowing()){
					Utils.safeDismissDialog(SmsActivity.this, pDialog);
				}
				processCountries();
				btnConfirmar.setEnabled(true);
			}
		});

		spinner_sel_country.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
				try {
					JSONObject countries = Utils.getDefaultJSONObject("countries", getApplicationContext());
					countries_array = countries.getJSONArray("countries");
					codeCountry.setText("+" + countries_array.getJSONObject(position).getString("calling_code"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			public void onNothingSelected(AdapterView<?> adapterView) {
				return;
			}
		});
        		
		btnConfirmar.setOnClickListener(new View.OnClickListener() {   
			public void onClick(View view) {
				Map<String, String> networkDetails = Utils.getConnectionDetails(getApplicationContext());
				if (networkDetails.isEmpty()) {
					Utils.showToast(getApplicationContext(), getString(R.string.internet_connection));
				} else {
					sendSMS(view);
				}
			} 
		});
	}
	
	public void sendSMS(View view){
		String code = codeCountry.getText().toString();
		String cel = mMobile.getText().toString();

		int position = spinner_sel_country.getSelectedItemPosition();
		String regex = "";
		try {
			regex = countries_array.getJSONObject(position).getString("phone_validation_regex");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if(code.isEmpty() || cel.isEmpty()){
			mInputMobile.setError(getString(R.string.activity_sms_validation_enter_your_phone_number));
			mInputMobile.setErrorEnabled(true);
		} else if(!regex.equals("") && !cel.matches(regex)){
			mInputMobile.setError(getString(R.string.activity_sms_validation_invalid_phone_number));
			mInputMobile.setErrorEnabled(true);
		}

		if(!mInputMobile.isErrorEnabled()) {
			mInputMobile.setErrorEnabled(false);
			final String validatedMobile = code + cel;
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SmsActivity.this);
			dialogBuilder
					.setTitle(getString(R.string.activity_sms_dialog_title))
					.setMessage(String.format(getString(R.string.activity_sms_dialog_message_check_phone_number), validatedMobile))
					.setNegativeButton(R.string.activity_sms_dialog_cancel_button, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					})
					.setPositiveButton(R.string.activity_sms_dialog_ok_button, new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int id){
						dialog.cancel();

						String url = Config.ApiUrl + "generate_code/" + validatedMobile + "/sms";

						pDialog.setMessage(getString(R.string.activity_sms_sending_sms));
						pDialog.show();

						RequestVolley rqv = new RequestVolley(url, getApplicationContext());

						rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
							@Override
							public void onSuccess(JSONObject response) {
								try {
									if(pDialog != null && pDialog.isShowing()){
										Utils.safeDismissDialog(SmsActivity.this, pDialog);
									}
									if (response.getString("result").equals("ACK")) {
										Utils.setDefaults("confirm_pin", "false", getApplicationContext());

										Utils.setDefaults("mobile_verificado", validatedMobile, getApplicationContext());
										Intent verify = new Intent(SmsActivity.this, VerifyActivity.class);
										startActivity(verify);
										finish();
									} else {
										AlertDialog.Builder builder = new AlertDialog.Builder(SmsActivity.this);
										builder.setTitle(R.string.activity_sms_verify_phone_number);
										String msg;
										if(!response.getString("msg").equals("")){
											msg = response.getString("msg");
										} else {
											msg = getString(R.string.activity_sms_it_seems_like_number_not_exist);
										}

										builder.setMessage(msg)
												.setCancelable(false)
												.setPositiveButton(R.string.activity_sms_dialog_ok_button2, new DialogInterface.OnClickListener() {
													public void onClick(DialogInterface dialog, int id) {
													}
												});

										AlertDialog alert = builder.create();
										alert.show();
									}

								} catch(JSONException e){
									e.printStackTrace();
								}
							}

							@Override
							public void onError(String errorType, String msg) {
								if (!msg.equals("")){
									Utils.showToast(getApplicationContext(), msg);
								}
								Utils.safeDismissDialog(SmsActivity.this, pDialog);
							}
						});
					}
				});
			dialogBuilder.create().show();
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
				case R.id.mobile:
					if(text.equals("")){
						mInputMobile.setError(getString(R.string.activity_sms_validation_required_field));
						mInputMobile.setErrorEnabled(true);
					} else {
						mInputMobile.setError(null);
						mInputMobile.setErrorEnabled(false);
					}
					break;
			}
		}
	}
}