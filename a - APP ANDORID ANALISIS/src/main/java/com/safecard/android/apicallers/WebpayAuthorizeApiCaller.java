package com.safecard.android.apicallers;

import android.content.Context;
import android.content.ContextWrapper;

import com.safecard.android.Config;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alonso on 21/06/17.
 */

public final class WebpayAuthorizeApiCaller extends ContextWrapper {

    String url;

    public WebpayAuthorizeApiCaller(Context context, int defaultPaymentMethodId, int amount) {
        super(context);
        url = Config.ApiUrl + "pay/" + Utils.getMobile(context) + "/" + defaultPaymentMethodId + "/" + amount;
    }

    public void doCall(final ApiCallback callback){
        final RequestVolley rqv = new RequestVolley(url, getApplicationContext());
        rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getString("result").equals("ACK")) {
                        JSONObject json = new JSONObject();
                        json.put("authorizationCode", response.getString("authorizationCode"));
                        json.put("transactionId", response.getString("transactionId"));
                        Utils.setDefaultJSONObject("authorizeResponse", json, getApplicationContext());
                        callback.callSuccess(response);
                        return;
                    }
                    System.out.println("RequestWebpayApi response: " + response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.callError("Error", "Error");
            }

            @Override
            public void onError(String errorType, String msg) {
                callback.callError(errorType, msg);
            }
        });
    }
}