package com.safecard.android.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.safecard.android.Config;
import com.safecard.android.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.net.ssl.SSLContext;

public class RequestVolley extends ContextWrapper {
    public static final String TAG = "RequestVolley";

    private String signature;
    public String url;
    public int method;
    public JSONObject body;

    public String token;
    public int retries = 0;
    //public RequestQueue mRequestQueue;

    public static final String REQUEST_TAG = "REQUEST_TAG";

    public static final String CALLBACK_TYPE_STRING = "CALLBACK_TYPE_STRING";
    public static final String CALLBACK_TYPE_JSON = "CALLBACK_TYPE_JSON";

    private boolean useCustomCert = false;

    public RequestVolley(String url, int method, JSONObject body, Context context) {
        super(context);
        newRequestVolley(url, method, body);
    }

    public RequestVolley(String url, Context context) {
        super(context);
        newRequestVolley(url, Request.Method.GET, new JSONObject());
    }

    String methodToStr(int method) {
        switch(method){
            case -1: return "DEPRECATED_GET_OR_POST";
            case 0: return "GET";
            case 1: return "POST";
            case 2: return "PUT";
            case 3: return "DELETE";
            case 4: return "HEAD";
            case 5: return "OPTIONS";
            case 6: return "TRACE";
            case 7: return "PATCH";
            default: return "UNKNOWN";
        }
    }

    private void newRequestVolley(String url, int method, JSONObject body) {
        this.token = new DeviceIdManager(getApplicationContext()).getMd5DeviceId();
        this.method = method;
        this.body = body;

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timeInUrl = (url.indexOf("?") > 0 ? "&" : "?") + "time=" + sdfDate.format(new Date());
        this.url = url + timeInUrl;
        Log.i(TAG, methodToStr(method) +" url: " + this.url);

        AsymmetricCryptoUtils ac = new AsymmetricCryptoUtils(getApplicationContext());
        int startSubstringIndex = (this.url.startsWith(Config.ApiUrl) ? Config.ApiUrl.length() : Config.ApiLocalUrl.length()) - 1;
        String urlSign = this.url.substring(startSubstringIndex, this.url.length());
        this.signature = "";
        if (ac.exist()) {
            String toSign = urlSign;
            if(!this.body.toString().equals("{}")){
                toSign += this.body.toString();
            }
            byte[] s = new AsymmetricCryptoUtils(getApplicationContext()).sign(toSign.getBytes());
            this.signature = Utils.bytesToHex(s);
        }
    }

    public void requestApi(final VolleyJsonCallback callback) {
        final long startTime = System.currentTimeMillis();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                this.method,
                this.url,
                this.body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.d(TAG, "VolleyResponse seconds: " + (System.currentTimeMillis() - startTime) / 1000);
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.d(TAG, "onErrorResponse seconds: " + (System.currentTimeMillis() - startTime) / 1000);

                String msg = "", errorType = "_";
                try {
                    if (error != null && error.networkResponse != null) {
                        Log.d(TAG, "VolleyError http code: " + error.networkResponse.statusCode);
                        errorType = "Error Code:" + error.networkResponse.statusCode;
                    }

                    if(checkForRecallWithTLSFix(error, callback, CALLBACK_TYPE_JSON)) return;
                    if(checkForRecallWithCustomCert(error, callback, CALLBACK_TYPE_JSON)) return;

                    if (error instanceof NoConnectionError) {
                        Log.d(TAG, "VolleyError NoConnectionError: " + error.getMessage());
                        errorType = "NoConnectionError";
                        msg = getString(R.string.request_volley_error_connection_not_active);
                    } else if (error instanceof TimeoutError) {
                        errorType = "TimeoutError";
                        msg = getString(R.string.request_volley_error_timeout);
                    } else if (error != null && error.networkResponse != null
                            && error.networkResponse.statusCode == 401) {
                        JSONObject data = new JSONObject(new String(error.networkResponse.data));
                        String code = "";
                        if (data.has("code")) {
                            code = data.getString("code");
                        }

                        Log.d(TAG, "Error bodyCode: " + code);
                        if(Utils.getDefaultBoolean("obsoleteVersion", getApplicationContext())) {
                            Utils.setDefaultBoolean("obsoleteVersion", false, getApplicationContext());
                        }
                        if (code.equals("115")) {
                            //show other_device
                            errorType = "115";
                            msg = "";
                            Utils.setDefaultBoolean("otherDevice", true, getApplicationContext());
                        } else if (code.equals("117")) {
                            //show app_obsolete
                            errorType = "117";
                            msg = "";
                            Utils.setDefaultBoolean("obsoleteVersion", true, getApplicationContext());
                        }
                    } else {
                        if (error != null && error.networkResponse != null) {
                            JSONObject data = new JSONObject(new String(error.networkResponse.data));
                            if (data.has("msg")) {
                                msg = data.getString("msg");
                            }
                        }
                    }
                    callback.onError(errorType, msg);
                    return;

                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                callback.onError("JSONException", getString(R.string.request_volley_error_try_later));
            }
        }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("signature", RequestVolley.this.signature);
                headers.put("key", Config.apiKey);
                headers.put("token", RequestVolley.this.token);
                headers.put("Accept-Language", Locale.getDefault().getLanguage());
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                    JSONObject jsonResponse = new JSONObject(jsonString);
                    JSONObject headers = new JSONObject(response.headers);
                    if (headers.has("signature")) {
                        //Log.d(TAG, "signature:" + headers.getString("signature"));
                        AsymmetricCryptoUtils ac = new AsymmetricCryptoUtils(getApplicationContext());
                        if (ac.existApiKeys() &&
                                !ac.isApiSignOk(response.data, Utils.hexToBytes(headers.getString("signature")))) {
                            Log.d(TAG, "isApiSignOk: false");
                            return Response.error(new VolleyError("WrongSignature"));
                        } else {
                            Log.d(TAG, "isApiSignOk: true");
                        }
                    } else {
                        Log.d(TAG, "no signature in response");
                    }
                    return Response.success(jsonResponse,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }
            }
        };

        jsonRequest.setTag(REQUEST_TAG);
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 0f));

        if(useCustomCert) {
            SSLContext sslContext = MySSLCert.getSSLContext(getApplicationContext());
            HurlStack hurlStack = new HurlStack(null, sslContext.getSocketFactory());
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext(), hurlStack);
            queue.add(jsonRequest);
        }else {
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(jsonRequest);
        }
    }

    public void requestApi(final VolleyStringCallback callback) {
        final long startTime = System.currentTimeMillis();
        StringRequest stringRequest = new StringRequest(
                this.method,
                this.url,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.d(TAG, "onResponse seconds: " + (System.currentTimeMillis() - startTime) / 1000);
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.d(TAG, "onErrorResponse seconds: " + (System.currentTimeMillis() - startTime) / 1000);

                String msg = "", errorType = "_";
                try {

                    if (error != null && error.networkResponse != null) {
                        Log.d(TAG, "VolleyError http code: " + error.networkResponse.statusCode);
                        errorType = "Error Code:" + error.networkResponse.statusCode;
                    }

                    if(checkForRecallWithTLSFix(error, callback, CALLBACK_TYPE_STRING)) return;
                    if(checkForRecallWithCustomCert(error, callback, CALLBACK_TYPE_STRING)) return;

                    if (error instanceof NoConnectionError) {
                        Log.d(TAG, "VolleyError NoConnectionError: " + error.getMessage());

                        errorType = "NoConnectionError";
                        msg = getString(R.string.request_volley_error_connection_not_active2);
                    } else if (error instanceof TimeoutError) {
                        errorType = "TimeoutError";
                        msg = getString(R.string.request_volley_error_timeout2);
                    } else if (error != null && error.networkResponse != null
                            && error.networkResponse.statusCode == 401) {
                        JSONObject data = new JSONObject(new String(error.networkResponse.data));
                        String code = "";
                        if (data.has("code")) {
                            code = data.getString("code");
                        }

                        Log.d(TAG, "Error bodyCode: " + code);
                        if(Utils.getDefaultBoolean("obsoleteVersion", getApplicationContext())) {
                            Utils.setDefaultBoolean("obsoleteVersion", false, getApplicationContext());
                        }
                        if (code.equals("115")) {
                            //show other_device
                            errorType = "115";
                            msg = "";
                            Utils.setDefaultBoolean("otherDevice", true, getApplicationContext());
                        } else if (code.equals("117")) {
                            //show app_obsolete
                            errorType = "117";
                            msg = "";
                            Utils.setDefaultBoolean("obsoleteVersion", true, getApplicationContext());
                        }
                    } else {
                        if (error != null && error.networkResponse != null) {
                            JSONObject data = new JSONObject(new String(error.networkResponse.data));
                            if (data.has("msg")) {
                                msg = data.getString("msg");
                            }
                        }
                    }
                    callback.onError(errorType, msg);
                    return;

                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
                callback.onError("JSONException", getString(R.string.request_volley_error_try_later2));
            }
        }){
            @Override
            public Map<String, String> getHeaders(){
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "text/html; charset=utf-8");
                headers.put("signature", RequestVolley.this.signature);
                headers.put("key", Config.apiKey);
                headers.put("token", RequestVolley.this.token);
                return headers;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    String bodyString = new String(response.data);
                    JSONObject headers = new JSONObject(response.headers);
                    if (headers.has("signature")) {
                        Log.d(TAG, "signature:" + headers.getString("signature"));
                        AsymmetricCryptoUtils ac = new AsymmetricCryptoUtils(getApplicationContext());
                        if (ac.existApiKeys() &&
                                !ac.isApiSignOk(response.data, Utils.hexToBytes(headers.getString("signature")))) {
                            Log.d(TAG, "isApiSignOk: false");
                            return Response.error(new VolleyError("WrongSignature"));
                        } else {
                            Log.d(TAG, "isApiSignOk: true");
                        }
                    } else {
                        Log.d(TAG, "no signature in response");
                    }
                    return Response.success(bodyString,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }
            }
        };

        stringRequest.setTag(REQUEST_TAG);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 0f));

        if(useCustomCert) {
            SSLContext sslContext = MySSLCert.getSSLContext(getApplicationContext());
            HurlStack hurlStack = new HurlStack(null, sslContext.getSocketFactory());
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext(), hurlStack);
            queue.add(stringRequest);
        }else {
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(stringRequest);
        }
    }

    private boolean checkForRecallWithTLSFix(VolleyError error, Object callback, String callbackType) {
        if (error instanceof NoConnectionError
                && error.getMessage().contains("SSLException")
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "retry: " + retries + " msg: " + error.getMessage());

            if (retries == 0) {
                ServiceUtils.fixTlsWithGoogle(this);
                retries++;
                if (callbackType.equals(CALLBACK_TYPE_STRING)){
                    requestApi((VolleyStringCallback) callback);
                } else if(callbackType.equals(CALLBACK_TYPE_JSON)) {
                    requestApi((VolleyJsonCallback) callback);
                } else {
                    Log.d(TAG, "callbackType invalid");
                }
                Log.d(TAG, "checkForRecallWithTLSFix returns true");
                //true means stop old response, new one are going to respond
                return true;
            } else if (retries == 1) {
                if(this.url.startsWith("https://rest.safecard.cl")) {
                    this.url = "https://rest-mirror.safecard.cl" + this.url.substring("https://rest.safecard.cl".length());
                    Log.d(TAG, "new url: " + this.url);
                    retries++;
                    if (callbackType.equals(CALLBACK_TYPE_STRING)){
                        requestApi((VolleyStringCallback) callback);
                    } else if(callbackType.equals(CALLBACK_TYPE_JSON)) {
                        requestApi((VolleyJsonCallback) callback);
                    } else {
                        Log.d(TAG, "callbackType invalid");
                    }
                    Log.d(TAG, "checkForRecallWithTLSFix returns true");
                    //true means stop old response, new one are going to respond
                    return true;
                }
            }
        }
        Log.d(TAG, "checkForRecallWithTLSFix returns false");
        //false means continue with old response, new one was not generated
        return false;
    }

    private boolean checkForRecallWithCustomCert(VolleyError error, Object callback, String callbackType) {
        if (Build.VERSION.SDK_INT <= 21 ||
                (error instanceof NoConnectionError &&
                        (error.getMessage().contains("CertPathValidatorException")
                                || error.getMessage().contains("SSLHandshakeException")))) {
            Log.d(TAG, "useCustomCert: " + useCustomCert + " msg: " + error.getMessage());

            if (!useCustomCert) {
                useCustomCert = true;
                if (callbackType.equals(CALLBACK_TYPE_STRING)){
                    requestApi((VolleyStringCallback) callback);
                } else if(callbackType.equals(CALLBACK_TYPE_JSON)) {
                    requestApi((VolleyJsonCallback) callback);
                } else {
                    Log.d(TAG, "callbackType invalid");
                }
                Log.d(TAG, "checkForRecallWithCustomCert returns true");
                //true means stop old response, new one are going to respond
                return true;
            }
        }
        Log.d(TAG, "checkForRecallWithCustomCert returns false");
        //false means continue with old response, new one was not generated
        return false;
    }

    private void installGooglePlayServices() {
        String LINK_TO_GOOGLE_PLAY_SERVICES = "play.google.com/store/apps/details?id=com.google.android.gms";
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://" + LINK_TO_GOOGLE_PLAY_SERVICES));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException anfe) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + LINK_TO_GOOGLE_PLAY_SERVICES));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    public interface VolleyJsonCallback {
        void onSuccess(JSONObject response);

        void onError(String errorType, String msg);
    }

    public interface VolleyStringCallback {
        void onSuccess(String response);

        void onError(String errorType, String msg);
    }
}
