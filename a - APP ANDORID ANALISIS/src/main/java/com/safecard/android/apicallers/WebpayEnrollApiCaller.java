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

public final class WebpayEnrollApiCaller extends ContextWrapper {

    String url;

    public WebpayEnrollApiCaller(Context context) {
        super(context);
        url = Config.ApiUrl + "payment/" + Utils.getMobile(context) + "/webpay_init_inscription";
    }

    public void doCall(final ApiCallback callback){
        final RequestVolley rqv = new RequestVolley(url, getApplicationContext());
        rqv.requestApi(new RequestVolley.VolleyStringCallback() {
            @Override
            public void onSuccess(String response) {
                if (response.length() > 0){
                    Utils.setDefaultString("html_init_inscription", response, getApplicationContext());
                    callback.callSuccess(new JSONObject());
                    return;
                }
                System.out.println("RequestWebpayApi response: " + response);
                callback.callError("Error", "Error");
            }

            @Override
            public void onError(String errorType, String msg) {
                callback.callError(errorType, msg);
            }
        });
    }
}