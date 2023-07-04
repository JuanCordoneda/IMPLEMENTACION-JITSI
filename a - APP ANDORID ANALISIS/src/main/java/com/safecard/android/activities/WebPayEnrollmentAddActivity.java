package com.safecard.android.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.safecard.android.R;
import com.safecard.android.apicallers.ApiCallback;
import com.safecard.android.apicallers.WebpayEnrollApiCaller;
import com.safecard.android.utils.Utils;

import org.json.JSONObject;

/**
 * Created by Alonso on 19-10-17.
 */
public class WebPayEnrollmentAddActivity extends AppCompatActivity {
    private static final String TAG = "WebPayEnrollmentAddAct";
    private TextView title;
    private Button btnAction;
    private Toolbar toolbar;
    ProgressDialog pDialog;
    Boolean clicked = false;
    Boolean callError = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webpay_enrollment_add);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_shape);

        title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText(" ");

        btnAction = (Button) findViewById(R.id.btnAction);

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDialog.show();
                clicked = true;
                if(callError){
                    enrollApiCall();
                }else{
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
    public void onResume(){
        super.onResume();

        clicked = false;
        callError = false;

        enrollApiCall();

        pDialog = new ProgressDialog(WebPayEnrollmentAddActivity.this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage(getString(R.string.activity_webpay_enrollment_add_wait_a_moment));
        pDialog.setCancelable(true);
    }





    public void enrollApiCall() {
        new WebpayEnrollApiCaller(getApplicationContext()).doCall(new ApiCallback() {
            @Override
            public void callSuccess(JSONObject response) {

            }

            @Override
            public void callError(String errorType, String msg) {
                callError = true;
                if (!msg.equals("")){
                    Utils.showToast(getApplicationContext(), msg);
                }

                if(pDialog != null && pDialog.isShowing()) {
                    pDialog.cancel();
                }
            }
        });
    }

}
