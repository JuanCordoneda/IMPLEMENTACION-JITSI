package com.safecard.android.activities;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

	private Toolbar toolbar;
    private Button btnRegister;
    private EditText inputName, inputFatherSurname, inputEmail;
	private TextView mobile_tv, title, help_tv;
	private TextInputLayout text_input_layout_name, text_input_layout_father_surname, text_input_layout_email;
	RequestVolley rqv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.register);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		title = (TextView) toolbar.findViewById(R.id.toolbar_title);
		title.setText(R.string.activity_register_title);

		help_tv = (TextView) findViewById(R.id.help_tv);

        mobile_tv = (TextView) findViewById(R.id.mobile_tv);

		text_input_layout_name = (TextInputLayout) findViewById(R.id.text_input_layout_name);
		text_input_layout_father_surname = (TextInputLayout) findViewById(R.id.text_input_layout_father_surname);
		text_input_layout_email = (TextInputLayout) findViewById(R.id.text_input_layout_email);

		text_input_layout_name.setErrorEnabled(false);
		text_input_layout_father_surname.setErrorEnabled(false);
		text_input_layout_email.setErrorEnabled(false);

		inputName = (EditText) findViewById(R.id.registerName);
        inputFatherSurname = (EditText) findViewById(R.id.registerFatherSurname);
		inputEmail = (EditText) findViewById(R.id.registerEmail);

		if(Utils.getDefaults("login", getApplicationContext()) != null){
			help_tv.setText(R.string.activity_register_check_your_data_and_confirm);

			try {
				JSONObject login_json = new JSONObject(Utils.getDefaults("login", getApplicationContext()));

				inputName.setText(login_json.getString("name"));
				inputFatherSurname.setText(login_json.getString("lastName"));
				inputEmail.setText(login_json.getString("email"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

        btnRegister = (Button) findViewById(R.id.btnRegister);

		mobile_tv.setText(Utils.getDefaults("mobile_verificado", getApplicationContext()));

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
			Registrar();
			}
		});
    }

	@Override
	protected void onPause(){
		super.onPause();
		Utils.setDefaults("GOTO_INSTALL", String.valueOf(Consts.RegisterActivity), getApplicationContext());
	}

	private void Registrar(){
		final String name = inputName.getText().toString().replace(",", "");
		final String last_name = inputFatherSurname.getText().toString().replace(",", "");
		final String email = inputEmail.getText().toString().trim();

		final String mobile = Utils.getDefaults("mobile_verificado", getApplicationContext());

		if(name.equals("") || name.length() < 3){
			text_input_layout_name.setError(getString(R.string.activity_register_validation_real_name_required));
			text_input_layout_name.setErrorEnabled(true);
		} else {
			text_input_layout_name.setError(null);
			text_input_layout_name.setErrorEnabled(false);
		}

		if(last_name.equals("") || last_name.length() < 3){
			text_input_layout_father_surname.setError(getString(R.string.activity_register_validation_real_lastname_required));
			text_input_layout_father_surname.setErrorEnabled(true);
		} else {
			text_input_layout_father_surname.setError(null);
			text_input_layout_father_surname.setErrorEnabled(false);
		}

		if(email.equals("") || !Utils.isEmailValid(email)){
			text_input_layout_email.setError(getString(R.string.activity_register_validation_valid_mail_required));
			text_input_layout_email.setErrorEnabled(true);
		} else {
			text_input_layout_email.setError(null);
			text_input_layout_email.setErrorEnabled(false);
		}

		if (!text_input_layout_name.isErrorEnabled() && !text_input_layout_father_surname.isErrorEnabled() && !text_input_layout_email.isErrorEnabled()) {

			try {

				JSONObject user_json = new JSONObject();
				user_json.put("name", name);
				user_json.put("lastName", last_name);
				user_json.put("mobile", mobile);
				user_json.put("email", email);
				Utils.setDefaults("login", user_json.toString(), getApplicationContext());

				Intent intent = new Intent(getApplicationContext(), RegisterPinActivity.class);
				startActivity(intent);
				finish();

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onBackPressed() {

	}

}
