package com.safecard.android.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SettingsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
	private static final String TAG = "SettingsActivity";

	private TextView name;
	private TextView mobile;

    private SwitchCompat notification_permission_switch, blocapp_switch;

	private int total_houses = 0;
	JSONArray properties_array;
	private String pin;

	AlertDialog.Builder alert_dialog;

	@Override
    public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

		alert_dialog = new AlertDialog.Builder(SettingsActivity.this);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setNavigationIcon(R.drawable.ic_shape);

		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
				finish();
			}
		});

		TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
		title.setText(R.string.activity_settings_title);

		//mMenuProfile = (ImageView) findViewById(R.id.menu_profile);
		//mMenuProfile.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_menu_perfil_on));

        name = (TextView) findViewById(R.id.name);
        mobile = (TextView) findViewById(R.id.mobile);

		LinearLayout advanced_conf = (LinearLayout) findViewById(R.id.advance_configuration);
		LinearLayout permissions_conf = (LinearLayout) findViewById(R.id.permissions_configurations);

		LinearLayout soPermissions = (LinearLayout) findViewById(R.id.so_perm);

		soPermissions.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				Uri uri = Uri.fromParts("package", SettingsActivity.this.getPackageName(), null);
				intent.setData(uri);
				SettingsActivity.this.startActivity(intent);
			}
		});

		LinearLayout user_perm = (LinearLayout) findViewById(R.id.user_perm);

		notification_permission_switch = (SwitchCompat) findViewById(R.id.notification_permission_switch);
		LinearLayout notification_permission = (LinearLayout) findViewById(R.id.notification_permission);

		LinearLayout btnNotifications = (LinearLayout) findViewById(R.id.btnNotifications);
		LinearLayout btnEditProfile = (LinearLayout) findViewById(R.id.btnEditProfile);
		LinearLayout btnChangePin = (LinearLayout) findViewById(R.id.btnChangePin);

		LinearLayout btnAbout = (LinearLayout) findViewById(R.id.btnAbout);
		LinearLayout btnCreditCard = (LinearLayout) findViewById(R.id.btnCreditCard);
		LinearLayout btnResidentes = (LinearLayout) findViewById(R.id.LayoutResidentes);


		try{

			String user = Utils.getDefaults("user", getApplicationContext());
			JSONObject user_json = new JSONObject(user);
			properties_array = new JSONArray(user_json.getString("properties"));
			total_houses = new JSONArray(user_json.getString("properties")).length();
			int total_students = new JSONArray(user_json.getString("students")).length();

			int total_general = total_houses + total_students;

		} catch (JSONException e){
			e.printStackTrace();
		}

		advanced_conf.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent advance_intent = new Intent(getApplicationContext(), AdvancedConfigurationActivity.class);
				startActivity(advance_intent);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});


		if(Utils.getDefaults("notification_perm", getApplicationContext()) == null){
			notification_permission_switch.setChecked(true);
		} else {
			notification_permission_switch.setChecked(Boolean.valueOf(Utils.getDefaults("notification_perm", getApplicationContext())));
		}

		notification_permission.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				notification_permission_switch.toggle();
			}
		});

		notification_permission_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Utils.setDefaults("notification_perm", String.valueOf(isChecked), getApplicationContext());
			}
		});

		permissions_conf.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				Uri uri = Uri.fromParts("package", getPackageName(), null);
				intent.setData(uri);
				startActivity(intent);
			}
		});

		btnNotifications.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent notifications_intent = new Intent(getApplicationContext(), NotificationActivity.class);
				startActivity(notifications_intent);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Intent edit_profile_intent = new Intent(getApplicationContext(), UpdateAccountActivity.class);
				startActivity(edit_profile_intent);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});

        btnChangePin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
				Intent about = new Intent(getApplicationContext(), UpdatePinActivity.class);
				startActivity(about);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

		btnCreditCard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), WebPayEnrollmentUpdateActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});

		btnAbout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent about = new Intent(getApplicationContext(), AboutActivity.class);
				startActivity(about);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});

		btnResidentes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(total_houses == 1){
					try {
						JSONObject aux = properties_array.getJSONObject(0);
						if(aux.getBoolean("active") && (aux.getInt("owner") == 1 || aux.getInt("admin") == 1)){
							Intent residents_intent = new Intent(getApplicationContext(), ResidentsActivity.class);
							residents_intent.putExtra("access_selected", properties_array.getJSONObject(0).toString());
							residents_intent.putExtra("fromActivity", Consts.ProfileActivity);
							startActivity(residents_intent);
						} else {
							Utils.showToast(getApplicationContext(), getString(R.string.no_privileges));
						}

					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else if(total_houses > 1){
					Intent houses_residents_intent = new Intent(getApplicationContext(), HousesResidentsActivity.class);
					startActivity(houses_residents_intent);
				} else {
					Utils.showToast(getApplicationContext(), getString(R.string.activity_settings_you_have_no_properties));
				}

				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});

		blocapp_switch = (SwitchCompat) findViewById(R.id.blocapp_switch);

		blocapp_switch.setOnCheckedChangeListener(null);
		blocapp_switch.setChecked(LoginActivity.isAskForPinModeActive(this));
		blocapp_switch.setOnCheckedChangeListener(this);

		LinearLayout blocapp = (LinearLayout) findViewById(R.id.blocapp);
		blocapp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				blocapp_switch.toggle();
			}
		});
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@Override
	protected void onResume(){
		super.onResume();

		try {
			String login = Utils.getDefaults("login", getApplicationContext());
			JSONObject user_js = new JSONObject(login);
			name.setText(user_js.getString("name")+" "+user_js.getString("lastName"));
			mobile.setText(user_js.getString("mobile"));
			pin = user_js.getString("password");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

    @Override
    public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.blocapp_switch:

				LayoutInflater inflater = SettingsActivity.this.getLayoutInflater();
				View mView = inflater.inflate(R.layout.check_pin, null);
				final EditText pin_text = (EditText)mView.findViewById(R.id.pin_text);
				alert_dialog.setView(mView);

				alert_dialog.setPositiveButton(R.string.activity_settings_dialog_ok_button, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String pin_md5 = Utils.md5(pin_text.getText().toString(), true);
						checkPin(pin_md5);
					}
				});

				alert_dialog.setNegativeButton(R.string.activity_settings_dialog_cancel_button, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						cancelCheck();
					}
				});

				alert_dialog.setTitle(Html.fromHtml(getString(R.string.activity_settings_dialog_block_using_pin)));
				//alert_dialog.setMessage("Ingresa pin actual");
				alert_dialog.setCancelable(false);

				AlertDialog alert;
				alert = alert_dialog.create();
				alert.show();
                break;
        }
    }

	public void checkPin(String pin_md5){
		blocapp_switch.setOnCheckedChangeListener(null);
		if(pin.equals(pin_md5)){
			Utils.setDefaults("locked", blocapp_switch.isChecked() ? "1" : "0",SettingsActivity.this);
		} else {
			blocapp_switch.setChecked(!blocapp_switch.isChecked());
			Utils.showToast(getApplicationContext(), getString(R.string.activity_settings_pin_not_correct_cant_do_action));
		}
		blocapp_switch.setOnCheckedChangeListener(this);
	}

	public void cancelCheck(){
		blocapp_switch.setOnCheckedChangeListener(null);
		blocapp_switch.setChecked(!blocapp_switch.isChecked());
		blocapp_switch.setOnCheckedChangeListener(this);
	}

	@Override
	public void onPause(){
		super.onPause();
	}

}