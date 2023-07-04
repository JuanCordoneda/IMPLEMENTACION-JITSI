package com.safecard.android.apicallers;

import android.content.Context;
import android.content.ContextWrapper;

import com.safecard.android.Config;
import com.safecard.android.model.Models;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alonso on 21/06/17.
 */

public final class InitApiCaller extends ContextWrapper {
    String TAG = "InitApiCaller";
    String url;

    public InitApiCaller(Context context) {
        super(context);
        url = Config.ApiUrl + "init/" + Utils.getMobile(context);
    }

    public void doCall(final ApiCallback callback){
        final RequestVolley rqv = new RequestVolley(url, getApplicationContext());
        rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getString("result").equals("ACK")) {
                        if(response.has("api_date")){
                            Utils.setPersistedDiff(response.getInt("api_date"), getApplicationContext());
                        }

                        String login = Utils.getDefaults("login", getApplicationContext());
                        JSONObject loginJson = new JSONObject(login);

                        loginJson.put("name", response.getString("name"));
                        loginJson.put("lastName", response.getString("lastName"));
                        loginJson.put("email", response.getString("email"));
                        //loginJson.put("user_id", response.getString("id"));

                        Utils.setDefaults("user", response.toString(), getApplicationContext());
                        Utils.setDefaultJSONObject("init_data", response, getApplicationContext());
                        Utils.setDefaults("login", loginJson.toString(), getApplicationContext());

                        String oldVersion = Utils.getDefaultString("last_version_and", getApplicationContext());
                        Utils.setDefaultString("last_version_and",response.getString("last_version_and"), getApplicationContext());
                        String newVersion = Utils.getDefaultString("last_version_and", getApplicationContext());
                        if(Utils.compareAppVersion(Utils.getVersionApp(getApplicationContext()), newVersion) < 0
                                && Utils.compareAppVersion(oldVersion, newVersion) < 0){
                            Utils.setDefaultBoolean("show_update", true, getApplicationContext());
                        }

                        int lastTermsVersionAccepted = Utils.getDefaultInt("last_terms_version_accepted", getApplicationContext());
                        if(response.getInt("last_terms_version") > lastTermsVersionAccepted){
                            new TermsApiCaller(getApplicationContext()).doCall(new ApiCallback() {
                                @Override
                                public void callSuccess(JSONObject response) {}

                                @Override
                                public void callError(String errorType, String msg) {}
                            });
                        }
                        Models.getAccessModel(getApplicationContext()).loadFromPersisted(getApplicationContext());

                        //response.put("active_parking", 0);
                        if(response.has("active_parking")){
                            Utils.setDefaultBoolean("active_parking",
                                    response.getInt("active_parking") == 1,
                                    getApplicationContext());
                        }

                        callback.callSuccess(response);
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
    }
}