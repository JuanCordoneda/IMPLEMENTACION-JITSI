package com.safecard.android.apicallers;

import org.json.JSONObject;

/**
 * Created by alonso on 21-06-17.
 */

public interface ApiCallback {
    void callSuccess(JSONObject response);
    void callError(String errorType, String msg);
}
