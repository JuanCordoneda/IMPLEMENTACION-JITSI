package com.safecard.android.activities;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.safecard.android.Config;
import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ShareInvitationActivity extends AppCompatActivity {

    private ImageView wsp, copy_link, send_sms, share_img;
    private Toolbar toolbar;
    private TextView title, resume_invitation, title_invitation, content_invitation;
    private Button cancel;
    private LinearLayout tic_layout;
    private AppBarLayout appBarLayout;
    String url_wsp, mobile, invitation_id, eventId;
    Boolean registered, resend;
    RequestVolley rqv;

    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_invitation);

        pDialog = new ProgressDialog(ShareInvitationActivity.this);
        pDialog.setCancelable(false);

        try{
            JSONObject login_json = new JSONObject(Utils.getDefaults("login", getApplicationContext()));
            mobile = login_json.getString("mobile");
        } catch (JSONException e){
            e.printStackTrace();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        title_invitation = (TextView) findViewById(R.id.title_invitation);
        resume_invitation = (TextView) findViewById(R.id.resume_invitation);
        content_invitation = (TextView) findViewById(R.id.content_invitation);

        share_img = (ImageView) findViewById(R.id.share_img);

        wsp = (ImageView) findViewById(R.id.wsp);
        copy_link = (ImageView) findViewById(R.id.copy_link);
        send_sms = (ImageView) findViewById(R.id.send_sms);
        cancel = (Button) findViewById(R.id.cancel);

        tic_layout = (LinearLayout) findViewById(R.id.tic_layout);

        Intent intent = getIntent();
        url_wsp = intent.getExtras().getString("URLWSP");
        registered = intent.getExtras().getBoolean("REGISTERED");
        resend = intent.getExtras().getBoolean("RESEND");
        invitation_id = intent.getExtras().getString("INVITATION_ID");
        eventId = intent.getExtras().getString("EVENT_ID");

        if(resend.equals(true)){
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_shape);
            title_invitation.setText(R.string.activity_share_invitation_title_share_again);
            resume_invitation.setText("");
            content_invitation.setText(R.string.activity_share_invitation_share_again_by_using);
            cancel.setVisibility(View.GONE);
            appBarLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.greenBlue));

            share_img.setImageResource(R.drawable.ic_resend_inv);

        } else {
            toolbar.setVisibility(View.GONE);
            content_invitation.setText(R.string.activity_share_invitation_also_share_by_using);
            cancel.setVisibility(View.VISIBLE);

            if(registered.equals(true)){
                title_invitation.setText(R.string.activity_share_invitation_title_shared_successfully);
                resume_invitation.setText(R.string.activity_share_invitation_content_safecard_user);
                tic_layout.setBackgroundColor(Color.rgb(3, 166, 120));
            } else {
                title_invitation.setText(R.string.activity_share_invitation_title_created_successfully);
                resume_invitation.setText(R.string.activity_share_invitation_content_no_safecard_user_);
                tic_layout.setBackgroundColor(Color.rgb(238, 174, 30));
            }
        }

        title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText("");

        wsp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareInvitation(url_wsp);
            }
        });

        copy_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(getApplicationContext().CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.activity_share_invitation_url_copied), url_wsp);
                clipboard.setPrimaryClip(clip);
                Utils.showToast(getApplicationContext(), getString(R.string.activity_share_invitation_link_copied));
            }
        });

        send_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSMS();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contact = new Intent(getApplicationContext(), ContactsActivity.class);
                contact.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(contact);
                finish();
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        // java.lang.IllegalArgumentException: View not attached to window manager
        Utils.safeDismissDialog(ShareInvitationActivity.this, pDialog);
        pDialog = null;
    }

    public void shareInvitation(String url){

        if (Utils.isAppInstalled("com.whatsapp", getApplicationContext())){
            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            waIntent.setPackage("com.whatsapp");
            waIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.activity_share_invitation_whatsapp_message) + url);
            startActivityForResult(Intent.createChooser(waIntent, getString(R.string.activity_share_invitation_share_with)), 1);
            Utils.mixpanel.track("INVITATION_WHATSAPP_SHARED");
        } else if (Utils.isAppInstalled("com.whatsapp.w4b", getApplicationContext())){
            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            waIntent.setPackage("com.whatsapp.w4b");
            waIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.activity_share_invitation_whatsapp_message) + url);
            startActivityForResult(Intent.createChooser(waIntent, getString(R.string.activity_share_invitation_share_with)), 1);
            Utils.mixpanel.track("INVITATION_WHATSAPP_SHARED");
        } else {
            Utils.showToast(this, getString(R.string.activity_share_invitation_error_no_whatsapp));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resend.equals(false)){

            int go_to = getAccessOrInvitationActivity();
            Intent intent;
            if (go_to == Consts.AccessActivity) {
                intent = new Intent(getApplicationContext(), AccessActivity.class);
            } else if (go_to == Consts.InvitationActivity) {
                intent = new Intent(getApplicationContext(), InvitationActivity.class);
            } else {
                intent = new Intent(getApplicationContext(), AccessActivity.class);
            }

            startActivity(intent);
            finish();
        }
    }

    public void sendSMS(){
        if(eventId != null && !eventId.equals("")){
            sendSMSEvent();
        }else if(invitation_id != null && !invitation_id.equals("")){
            sendSMSInvitation();
        }
    }

    public void sendSMSEvent(){
        String url = Config.ApiUrl + "resend_event_sms/" + mobile + "/" + eventId;

        pDialog.setMessage(getString(R.string.activity_share_invitation_sending_sms));
        pDialog.show();

        rqv = new RequestVolley(url, getApplicationContext());
        rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                if (pDialog != null && pDialog.isShowing()) {
                    Utils.safeDismissDialog(ShareInvitationActivity.this, pDialog);
                }
                try {
                    if(response.getString("result").equals("ACK")){
                        Utils.showToast(getApplicationContext(), getString(R.string.activity_share_invitation_sms_sent));
                    } else {
                        Utils.showToast(getApplicationContext(), response.getString("msg"));
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorType, String msg) {
                if (!msg.equals("")) {
                    Utils.showToast(getApplicationContext(), msg);
                }
                if (pDialog != null && pDialog.isShowing()) {
                    Utils.safeDismissDialog(ShareInvitationActivity.this, pDialog);
                }
            }
        });
    }

    public void sendSMSInvitation(){
        String url = Config.ApiUrl + "resend_invitation_sms/" + mobile + "/" + invitation_id;

        pDialog.setMessage(getString(R.string.activity_share_invitation_sending_sms2));
        pDialog.show();

        rqv = new RequestVolley(url, getApplicationContext());
        rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                if (pDialog != null && pDialog.isShowing()) {
                    Utils.safeDismissDialog(ShareInvitationActivity.this, pDialog);
                }
                try {
                    if(response.getString("result").equals("ACK")){
                        Utils.showToast(getApplicationContext(), getString(R.string.activity_share_invitation_sms_sent2));
                    } else {
                        Utils.showToast(getApplicationContext(), response.getString("msg"));
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorType, String msg) {
                if (!msg.equals("")) {
                    Utils.showToast(getApplicationContext(), msg);
                }
                if (pDialog != null && pDialog.isShowing()) {
                    Utils.safeDismissDialog(ShareInvitationActivity.this, pDialog);
                }
            }
        });
    }

    @Override
    public void onBackPressed(){
        return;
    }

    public int getAccessOrInvitationActivity() {
        int activity = Consts.AccessActivity;
        try {
            JSONObject user_json = new JSONObject(Utils.getDefaults("user", getApplicationContext()));
            JSONArray properties = new JSONArray(user_json.getString("properties"));
            JSONArray students = new JSONArray(user_json.getString("students"));
            if (properties.length() == 0 && students.length() == 0) {
                activity = Consts.InvitationActivity;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return activity;
    }
}
