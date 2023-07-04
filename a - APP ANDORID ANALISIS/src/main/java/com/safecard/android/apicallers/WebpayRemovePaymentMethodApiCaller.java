package com.safecard.android.apicallers;

import android.content.Context;
import android.content.ContextWrapper;

import com.android.volley.Request;
import com.safecard.android.Config;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alonso on 21/06/17.
 */

public final class WebpayRemovePaymentMethodApiCaller extends ContextWrapper {

    String url;

    public WebpayRemovePaymentMethodApiCaller(Context context, String id) {
        super(context);
        url = Config.ApiUrl + "payment/" + Utils.getMobile(context) + "/method/" + id;
    }

    public void doCall(final ApiCallback callback){
        final RequestVolley rqv = new RequestVolley(url, Request.Method.DELETE, new JSONObject(), getApplicationContext());
        rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getString("result").equals("ACK")) {
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