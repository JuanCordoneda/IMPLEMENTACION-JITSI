package com.safecard.android.apicallers;

import android.content.Context;
import android.content.ContextWrapper;

import com.safecard.android.Config;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alonso on 21/06/17.
 */

public final class WebpayPaymentMethodsApiCaller extends ContextWrapper {

    String url;

    public WebpayPaymentMethodsApiCaller(Context context) {
        super(context);
        url = Config.ApiUrl + "payment/" + Utils.getMobile(context) + "/methods";
    }

    public void doCall(final ApiCallback callback){
        final RequestVolley rqv = new RequestVolley(url, getApplicationContext());
        rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    System.out.println("RequestWebpayApi response: " + response);
                    if (response.getString("result").equals("ACK")) {
                        JSONArray array = response.getJSONArray("payment_methods");
                        Utils.setDefaultJSONArray("payment_methods", array, getApplicationContext());
                        callback.callSuccess(response);
                        return;
                    }
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