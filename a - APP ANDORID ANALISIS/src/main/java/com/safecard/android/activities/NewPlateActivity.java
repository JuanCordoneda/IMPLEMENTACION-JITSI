package com.safecard.android.activities;

import android.app.ProgressDialog;
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



public class NewPlateActivity extends AppCompatActivity {

    public static String HOUSE_ID = "HOUSE_ID";

    private EditText mPlateNumber;
    private TextInputLayout mInputPlate;
    private ProgressDialog pDialog;

    String plateNumber;

    int houseId;

    RequestVolley rqv;

    String regex_auto_1 = "^([A-Z]{4}\\d{2})$";
    String regex_auto_2 = "^([A-Z]{2}\\d{4})$";
    String regex_moto_1 = "^([A-Z]{3}\\d{2})$";
    String regex_moto_2 = "^([A-Z]{2}\\d{3})$";
    String regex_patente = "^([A-Z]{3}\\d{3})$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_plate);
        Bundle extras = getIntent().getExtras();
        houseId = getIntent().getIntExtra(HOUSE_ID, -1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_shape);
        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText(R.string.new_plate_title);

        pDialog = new ProgressDialog(NewPlateActivity.this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setCancelable(false);

        mInputPlate = (TextInputLayout) findViewById(R.id.input_plate);
        mPlateNumber = (EditText) findViewById(R.id.plate_number);
        Button add_plate = (Button) findViewById(R.id.add_plate);

        add_plate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                plateNumber = mPlateNumber.getText().toString();

                if(plateNumber.equals("")){
                    mInputPlate.setError(getString(R.string.new_plate_validation_enter_a_plate));
                    mInputPlate.setErrorEnabled(true);
                } else if(plateNumber.length() != 6 && plateNumber.length() != 5) {
                    mInputPlate.setErrorEnabled(true);
                    mInputPlate.setError(getString(R.string.new_plate_validation_plate_not_valid));
                } else {

                    if(plateNumber.length() == 6){
                        if(!plateNumber.matches(regex_auto_1) && !plateNumber.matches(regex_auto_2) && !plateNumber.matches(regex_patente)){
                            mInputPlate.setErrorEnabled(true);
                            mInputPlate.setError(getString(R.string.new_plate_validation_plate_not_valid2));
                        } else {
                            mInputPlate.setError(null);
                            mInputPlate.setErrorEnabled(false);
                        }
                    } else if(plateNumber.length() == 5){
                        if(!plateNumber.matches(regex_moto_1) && !plateNumber.matches(regex_moto_2)){
                            mInputPlate.setErrorEnabled(true);
                            mInputPlate.setError(getString(R.string.new_plate_validation_plate_not_valid3));
                        } else {
                            mInputPlate.setError(null);
                            mInputPlate.setErrorEnabled(false);
                        }
                    } else {
                        mInputPlate.setError(null);
                        mInputPlate.setErrorEnabled(false);
                    }
                }

                if(!mInputPlate.isErrorEnabled()){
                    addPlate();
                }
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void addPlate(){
        String url = Config.ApiUrl + "add_plate/" + Utils.getMobile(getApplicationContext()) + "/" + houseId + "/" + plateNumber;
        rqv = new RequestVolley(url, getApplicationContext());

        pDialog.setMessage(getString(R.string.new_plate_registering_plate_wait_a_moment));
        pDialog.show();

        rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                if (pDialog != null && pDialog.isShowing()) {
                    Utils.safeDismissDialog(NewPlateActivity.this, pDialog);
                }
                try {
                    if(response.getString("result").equals("ACK")){
                        Utils.showToast(getApplicationContext(), getString(R.string.new_plate_plate_added_successfully));
                        finish();
                    } else {
                        mInputPlate.setErrorEnabled(true);
                        mInputPlate.setError(response.getString("msg"));
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
                    Utils.safeDismissDialog(NewPlateActivity.this, pDialog);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // java.lang.IllegalArgumentException: View not attached to window manager
        Utils.safeDismissDialog(NewPlateActivity.this, pDialog);
        pDialog = null;
    }


}
