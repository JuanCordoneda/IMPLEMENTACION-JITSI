package com.safecard.android.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.safecard.android.R;
import com.safecard.android.apicallers.ApiCallback;
import com.safecard.android.apicallers.TermsApiCaller;
import com.safecard.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;


public class TermsActivity extends AppCompatActivity{

    private Button agreeBtn;
    private Toolbar toolbar;
    private EditText termsText;
    private SwipeRefreshLayout swiperefresh;
    private ScrollView scrollView;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText(R.string.activity_terms_title);

        agreeBtn = (Button) findViewById(R.id.agree_btn);
        termsText = (EditText) findViewById(R.id.terms_text);
        swiperefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        scrollView = (ScrollView) findViewById(R.id.terms_text_scroll);

        termsText.setKeyListener(null);
        termsText.setTypeface(Typeface.MONOSPACE);
        agreeBtn.setEnabled(false);
        swiperefresh.setEnabled(false);

        updateTermsFromApi();
    }

    protected void updateTermsFromApi() {
        swiperefresh.setRefreshing(true);
        new TermsApiCaller(getApplicationContext()).doCall(new ApiCallback() {
            @Override
            public void callSuccess(JSONObject response) {
                if(swiperefresh != null && swiperefresh.isRefreshing()){
                    swiperefresh.setRefreshing(false);
                }
                loadTermsFromPersisted();
            }

            @Override
            public void callError(String errorType, String msg) {
                if(swiperefresh != null && swiperefresh.isRefreshing()){
                    swiperefresh.setRefreshing(false);
                }
                loadTermsFromPersisted();
            }
        });
    }

    protected void loadTermsFromPersisted() {
        String terms = Utils.getDefaultString("terms_text", getApplicationContext());
        int lastTermsVersion = Utils.getDefaultInt("last_terms_version", getApplicationContext());

        if(terms.equals("") || lastTermsVersion < 0){
            String termsFilename = "terms_en.json";
            if("es".equals(Locale.getDefault().getLanguage())) {
                termsFilename = "terms_es.json";
            }
            JSONObject json = Utils.loadJSONFromAsset(TermsActivity.this, termsFilename);
            try {
                terms = json.getString("terms_text");
                lastTermsVersion = json.getInt("last_terms_version");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        termsText.setText(terms);

        final int finalLastTermsVersion = lastTermsVersion;
        agreeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.setDefaultInt("last_terms_version_accepted", finalLastTermsVersion, getApplicationContext());
                startActivity(new Intent(getApplicationContext(), SplashScreenActivity.class));
                finish();
            }
        });

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
            if (scrollView != null) {
                if (scrollView.canScrollVertically(-1)) {
                    agreeBtn.setEnabled(true);
                }
            }
            }
        });

        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (!scrollView.canScrollVertically(1)) {
                    agreeBtn.setEnabled(true);
                }
            }
        }, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


}
