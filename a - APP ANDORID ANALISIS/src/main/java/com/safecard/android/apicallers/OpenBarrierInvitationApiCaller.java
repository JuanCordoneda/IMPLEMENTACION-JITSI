package com.safecard.android.apicallers;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.safecard.android.Config;
import com.safecard.android.Consts;
import com.safecard.android.model.Models;
import com.safecard.android.model.dataobjects.PropertyData;
import com.safecard.android.utils.LocationProvider;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;
import com.safecard.android.utils.WifiHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;


/**
 * Created by Alonso on 21/06/17.
 */

public final class OpenBarrierInvitationApiCaller extends ContextWrapper {
    String TAG = "OpenBarrierInvitationApiCaller";

    String url;

    public OpenBarrierInvitationApiCaller(Context context) {
        super(context);
    }

    public void doCall(int invitationId,
                       String gateType,
                       String gateCode,
                       String gateName,
                       final ApiCallback callback){
        Context context = getApplicationContext();
        PropertyData property = Models.getAccessModel(getApplicationContext()).getInvitationProperty(invitationId, context);

        if(property.getHash() == null) {
            Log.e(TAG, "!ac.exist");
            return;
        }

        String mobile = Utils.getMobile(context);
        long timestamp = System.currentTimeMillis() / 1000 + Utils.getDefaultInt("diff", context);
        double lng = 0, lat = 0;
        if(LocationProvider.getInstance().getMCurrentLocation() != null) {
            lng = LocationProvider.getInstance().getMCurrentLocation().getLongitude();
            lat = LocationProvider.getInstance().getMCurrentLocation().getLatitude();
        }

        String nonceBase64 =  Base64.encodeToString(property.getNonce(), Base64.DEFAULT);
        String hashBase64 =  Base64.encodeToString(property.getHash(), Base64.DEFAULT);

        Log.d("AnalysisData", "hashBase64:" + hashBase64);
        Log.d("AnalysisData", "nonceBase64:" + nonceBase64);

        try {
            JSONObject body = new JSONObject();
            body.put("rc_type", Consts.RC_TYPE_GUEST);
            body.put("gate_code", gateCode);
            body.put("gate_type", gateType);
            body.put("lat", ""+lat);
            body.put("long", ""+lng);
            body.put("nonce", nonceBase64);
            body.put("hash", hashBase64);
            body.put("timestamp", timestamp);
            body.put("organizer_id", property.getOrganizerId());
            body.put("invitation_id", property.getInvitationId());

            final JSONObject mixpanelData = new JSONObject();
            mixpanelData.put("local_wifi_used", false);
            url = Config.ApiUrl + "open/" + mobile;
            if (WifiHelper.isLocal()) {
                url = Config.ApiLocalUrl + "open/" + mobile;
                mixpanelData.put("local_wifi_used", true);
            }

            final RequestVolley rqv = new RequestVolley(url, Request.Method.POST, body, getApplicationContext());
            rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        if (response.getString("result").equals("ACK")) {
                            Utils.mixpanel.track("OPENING_BY_RC_BUTTON", mixpanelData);
                            callback.callSuccess(response);
                            return;
                        }else {
                            String msg = response.getString("msg");
                            if(!msg.contains("Cod 104:") &&
                                    !msg.contains("Cod 105:") &&
                                    !msg.contains("Cod 204:") &&
                                    !msg.contains("Cod 205:") &&
                                    !msg.contains("Cod 208:")){
                            }

                            callback.callError("NACK", msg);
                            return;

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callback.callError("JSONException", "");
                }

                @Override
                public void onError(String errorType, String msg) {
                    callback.callError(errorType, msg);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}