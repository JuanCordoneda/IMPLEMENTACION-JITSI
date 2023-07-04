package com.safecard.android.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.safecard.android.R;
import com.safecard.android.apicallers.ApiCallback;
import com.safecard.android.apicallers.WebpayEnrollApiCaller;
import com.safecard.android.utils.Utils;

import org.json.JSONObject;


/**
 * Created by Alonso on 19-10-17.
 */
public class WebPayEnrollmentWebViewActivity extends AppCompatActivity {
    private static final String TAG = "WebPayEnWebViewAct";
    private Toolbar toolbar;
    ProgressDialog pDialog;
    Boolean callError = false;
    private WebView webview;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webpay_enrollment_webview);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_shape);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });


        pDialog = new ProgressDialog(WebPayEnrollmentWebViewActivity.this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage(getString(R.string.activity_webpay_enrollment_webview_wait_a_moment));
        pDialog.setCancelable(true);
        pDialog.show();

        webview = (WebView) findViewById(R.id.webview);
        //webview.setBackgroundColor(0x33333300);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(false);
        webSettings.setLoadWithOverviewMode(false);
        webSettings.setUseWideViewPort(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(false);
        //Log.i("userAgent", webSettings.getUserAgentString());
        webSettings.setDefaultTextEncodingName("utf-8");

        enrollApiCall();

    }

    public void enrollApiCall() {
        new WebpayEnrollApiCaller(getApplicationContext()).doCall(new ApiCallback() {
            @Override
            public void callSuccess(JSONObject response) {
                String html = Utils.getDefaultString("html_init_inscription", getApplicationContext());
                String htmlBase64 = Base64.encodeToString(html.getBytes(), Base64.NO_PADDING);
                Log.i(TAG,"HTML no b64:"+ html);
                if(html.length() > 0) {
                    webview.loadData(htmlBase64, "text/html", "base64");
                }
                Utils.setDefaultString("html_init_inscription", "", getApplicationContext());

                Utils.safeDismissDialog(WebPayEnrollmentWebViewActivity.this, pDialog);
            }

            @Override
            public void callError(String errorType, String msg) {
                callError = true;
                if (!msg.equals("")){
                    Utils.showToast(getApplicationContext(), msg);
                }

                Utils.safeDismissDialog(WebPayEnrollmentWebViewActivity.this, pDialog);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // java.lang.IllegalArgumentException: View not attached to window manager
        Utils.safeDismissDialog(WebPayEnrollmentWebViewActivity.this, pDialog);
        pDialog = null;
    }



}
