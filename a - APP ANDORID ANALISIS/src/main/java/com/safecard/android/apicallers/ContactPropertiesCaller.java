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

public final class ContactPropertiesCaller extends ContextWrapper {

    String url;

    public ContactPropertiesCaller(Context context, String contactMobile) {
        super(context);
        url = Config.ApiUrl + "properties/" +
                Utils.getMobile(getApplicationContext()) + "/" +
                contactMobile;
    }

    public void doCall(final ApiCallback callback){

        final RequestVolley rqv = new RequestVolley(url, getApplicationContext());
        rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getString("result").equals("ACK")) {
                        callback.callSuccess(response);
                        return;
                    }else {
                        callback.callError("NACK", response.getString("msg"));
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.callError("JSONException", "");
                }
            }

            @Override
            public void onError(String errorType, String msg) {
                callback.callError(errorType, msg);
            }
        });
    }
}
