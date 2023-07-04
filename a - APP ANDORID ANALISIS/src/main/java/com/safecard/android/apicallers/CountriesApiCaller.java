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

public final class CountriesApiCaller extends ContextWrapper {

    String url;

    public CountriesApiCaller(Context context) {
        super(context);
        url = Config.ApiUrl + "countries";
    }

    public void doCall(final ApiCallback callback){
        int countriesPersistTime = Utils.getDefaultInt("countries_persist_time", getApplicationContext());
        int mobileTimestamp = (int) (System.currentTimeMillis() / 1000L);

        //if (countriesPersistTime > 0 && countriesPersistTime + 43200 > mobileTimestamp){
        //    callback.callSuccess(new JSONObject());
        //    return;
        //}

        final RequestVolley rqv = new RequestVolley(url, getApplicationContext());
        rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getString("result").equals("ACK")) {
                        JSONObject countries = Utils.getDefaultJSONObject("countries", getApplicationContext());
                        String countriesLastModified = response.getString("countries_last_modified");
                        if (!countries.has("countries_last_modified") ||
                                !countriesLastModified.equals(countries.getString("countries_last_modified"))) {
                            Utils.setDefaultJSONObject("countries", response, getApplicationContext());
                            int mobileTimestamp = (int) (System.currentTimeMillis() / 1000L);
                            Utils.setDefaultInt("countries_persist_time", mobileTimestamp, getApplicationContext());
                        }
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
