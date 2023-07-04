package com.safecard.android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.safecard.android.BuildConfig;
import com.safecard.android.Config;
import com.safecard.android.R;
import com.safecard.android.utils.Utils;

public class AboutActivity extends AppCompatActivity{
    private static final String TAG = "AboutActivity";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_shape);

        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.activity_about_title));

        TextView version = findViewById(R.id.version);
        TextView device = findViewById(R.id.device);
        TextView environment = findViewById(R.id.environment);
        TextView debuggable = findViewById(R.id.debuggable);

        TextView website = findViewById(R.id.website);
        TextView mailto = findViewById(R.id.mailto);
        TextView backOffice = findViewById(R.id.backoffice);
        TextView terms = findViewById(R.id.terms);

        if(Config.DEBUG){
            environment.setText(BuildConfig.environment);
            environment.setVisibility(View.VISIBLE);

            debuggable.setVisibility(View.VISIBLE);
        }

        String versionName = Utils.getVersionApp(getApplicationContext());
        version.setText(String.format(getString(R.string.about_version), versionName));

        String deviceId = Utils.getDeviceId(getApplicationContext());
        device.setText(String.format(getString(R.string.about_device), deviceId.substring(deviceId.length() - 8)));


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });

        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_url_link_safecard)));
                startActivity(intent);
            }
        });

        mailto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", getString(R.string.about_email), null));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.about_email)});
                intent.putExtra(Intent.EXTRA_SUBJECT, R.string.about_email_subject);
                intent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(intent, getString(R.string.about_send_mail)));
            }
        });

        backOffice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_url_link_backoffice)));
                startActivity(intent);
            }
        });

        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(getString(R.string.about_url_link_terms), Utils.getMobileLanguage())));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
