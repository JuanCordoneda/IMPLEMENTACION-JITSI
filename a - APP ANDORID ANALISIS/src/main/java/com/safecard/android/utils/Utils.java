package com.safecard.android.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.safecard.android.BuildConfig;
import com.safecard.android.Config;
import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.activities.InvitationCustomizationActivity;
import com.safecard.android.activities.ObsoleteVersionActivity;
import com.safecard.android.activities.OtherDeviceActivity;
import com.safecard.android.activities.SmsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Utils {

    private static final String PROPERTY_REG_ID = "registration_id";
    // Clave que permite recuperar de las preferencias compartidas de la
    // aplicacion el dentificador de la version de la aplicacion
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


    // Prevents instantiation.
    private Utils() {}

    private final static String strmd5 = "DYhG93b0qyJfIxfs2guVoUubWwvniR2G0FgaC9mp";

    public static MixpanelAPI mixpanel;

    public static Map<String, String> getConnectionDetails(Context context) {
        Map<String, String> networkDetails = new HashMap<String, String>();
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiNetwork != null && wifiNetwork.isConnected()) {

                networkDetails.put("Type", wifiNetwork.getTypeName());
                networkDetails.put("Sub type", wifiNetwork.getSubtypeName());
                networkDetails.put("State", wifiNetwork.getState().name());
            }

            NetworkInfo mobileNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (mobileNetwork != null && mobileNetwork.isConnected()) {
                networkDetails.put("Type", mobileNetwork.getTypeName());
                networkDetails.put("Sub type", mobileNetwork.getSubtypeName());
                networkDetails.put("State", mobileNetwork.getState().name());
                if (mobileNetwork.isRoaming()) {
                    networkDetails.put("Roming", "YES");
                } else {
                    networkDetails.put("Roming", "NO");
                }
            }
        } catch (Exception e) {
            networkDetails.put("Status", e.getMessage());
        }
        return networkDetails;
    }

    public static Boolean isOnline() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal==0);
            return reachable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String md5(String input) {
        return md5(input, false);
    }

    public static String md5(String input, Boolean server_pass) {

        String result = input;
        if(input != null) {

            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            if(server_pass.equals(true)){
                input = strmd5+input;
            }

            md.update(input.getBytes());
            BigInteger hash = new BigInteger(1, md.digest());
            result = hash.toString(16);
            while(result.length() < 32) { //40 for SHA-1
                result = "0" + result;
            }
        }
        return result;
    }

    /**
     * Uses static final constants to detect if the device's platform version is Honeycomb or
     * later.
     */
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * Uses static final constants to detect if the device's platform version is Honeycomb MR1 or
     * later.
     */
    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    /**
     * Uses static final constants to detect if the device's platform version is ICS or
     * later.
     */
    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static void setDefaults(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void clearAllPersistedData(Context context) {
        SharedPreferences prefs = getPreferenciasCompartidas(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        editor = prefs.edit();
        editor.clear();
        editor.commit();
    }

    public static String getDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }

    public static void setDefaultString(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void setDefaultBoolean(String key, boolean value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void setDefaultInt(String key, int value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void setDefaultJSONArray(String key, JSONArray value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        if(value == null){
            editor.putString(key, "");
        }else{
            editor.putString(key, value.toString());
        }
        editor.commit();
    }

    public static void setDefaultJSONObject(String key, JSONObject value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        if(value == null){
            editor.putString(key, "");
        }else{
            editor.putString(key, value.toString());
        }
        editor.commit();
    }

    public static String getDefaultString(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, "");
    }

    public static boolean getDefaultBoolean(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(key, false);
    }

    public static int getDefaultInt(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key, -1);
    }

    public static JSONArray getDefaultJSONArray(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String aux = preferences.getString(key, "");
        if(!aux.equals("")){
            try {
                return new JSONArray(aux);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JSONArray();
    }

    public static JSONObject getDefaultJSONObject(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String aux = preferences.getString(key, "");
        if(!aux.equals("")){
            try {
                return new JSONObject(aux);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }

    public static void setDefaultHashmap(String key, Map value, Context context) {
        setDefaultJSONObject(key, new JSONObject(value), context);
    }

    public static Map<String,String> getDefaultHashmapStringToString(String key, Context context) {
        JSONObject json = getDefaultJSONObject(key, context);
        Iterator<String> keysItr = json.keys();
        Map<String,String> map = new HashMap<>();
        try {
            while(keysItr.hasNext()) {
                String k = keysItr.next();
                String val = (String) json.get(k);
                map.put(k, val);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static void updateDefaultHashmap(String key, String keyInHash, String valInHash, Context context){
        Map<String,String> map = getDefaultHashmapStringToString(key, context);
        map.put(keyInHash, valInHash);
        setDefaultHashmap(key, map, context);
    }
    

    /**
     * Recupera la version aplicacion que identifica a cada una de las
     * actualizaciones de la misma.
     *
     * @return La version del codigo de la aplicacion
     */
    private static int getAppVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getVersionApp(Context context){
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "0";
    }

    public static String getMobile(Context context) {
        try {
            String str = Utils.getDefaultString("login", context);
            if (str.equals("")){
                return "";
            }
            JSONObject json = new JSONObject(str);
            return json.getString("mobile");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getUserId(Context context) {
        try {
            String str = Utils.getDefaultString("user", context);
            if (str.equals("")){
                return "";
            }
            JSONObject json = new JSONObject(str);
            if(json.has("id")) {
                return json.getString("id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getUserFullName(Context context) {
        try {
            String str = Utils.getDefaultString("login", context);
            if (str.equals("")){
                return "";
            }
            JSONObject json = new JSONObject(str);
            return json.getString("name") + " " + json.getString("lastName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getAndroidVersion(){
        return Build.VERSION.RELEASE;
    }


    public static void setPersistedDiff(int apiDate, Context context) {
        int mobile_date = (int)(System.currentTimeMillis() / 1000L);
        int diff = apiDate - mobile_date;
        Utils.setDefaultInt("diff", diff, context);
    }

    public static String getPersistedAndroidVersion(Context context) {
        final SharedPreferences prefs = getPreferenciasCompartidas(context);
        return prefs.getString("android_version", "");
    }

    public static void setPersistedAndroidVersion(Context context, String value) {
        SharedPreferences prefs = getPreferenciasCompartidas(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("android_version", value);
        editor.commit();
    }

    public static String getPersistedVersionApp(Context context) {
        final SharedPreferences prefs = getPreferenciasCompartidas(context);
        return prefs.getString("app_version", "");
    }

    public static void setPersistedVersionApp(Context context, String value) {
        SharedPreferences prefs = getPreferenciasCompartidas(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("app_version", value);
        editor.commit();
    }

    private static SharedPreferences getPreferenciasCompartidas(Context context) {
        return context.getSharedPreferences(SmsActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    public static void setPersistedFullDevice(Context context, String value) {
        SharedPreferences prefs = getPreferenciasCompartidas(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("full_device", value);
        editor.commit();
    }

    public static String getPersistedFullDevice(Context context) {
        final SharedPreferences prefs = getPreferenciasCompartidas(context);
        return prefs.getString("full_device", "");
    }

    public static String getDeviceId(Context context) {
        final SharedPreferences prefs = getPreferenciasCompartidas(context);
        return prefs.getString("device_id", "");
    }

    public static void setDeviceId(Context context, String value) {
        SharedPreferences prefs = getPreferenciasCompartidas(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("device_id", value);
        editor.commit();
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static boolean appBlockChecks(Activity activity){
        if (activity == null){
            return false;
        }
        Context context = activity.getApplicationContext();
        //Utils.setDefaultBoolean("otherDevice", false, context);
        Log.i("Utils","appBlockChecks");
        boolean otherDevice = Utils.getDefaultBoolean("otherDevice", context);
        boolean obsoleteVersion = Utils.getDefaultBoolean("obsoleteVersion", context);
        if(otherDevice) {
            //Log.i("Utils","appBlockChecks otherDevice true");
            Intent i = new Intent(context, OtherDeviceActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            activity.finish();
            return true;
        } else if(obsoleteVersion) {
            //Log.i("Utils","appBlockChecks obsoleteVersion true");
            Intent i = new Intent(context, ObsoleteVersionActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            activity.finish();
            return true;
        }
        return false;
    }

    public static String getQRPass(String device, String mobile){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            //String SHA256_text = "Sryktszx4l" + mobile.substring(1,6);
            String SHA256_text = device + mobile.substring(1,6);
            md.update(SHA256_text.getBytes("UTF-8"));
            return String.format("%064x", new java.math.BigInteger(1, md.digest())).substring(4, 20);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String trimSsid(String wifiSsid) {
        int head = wifiSsid.startsWith("\"") ? 1 : 0;
        int tail = wifiSsid.endsWith("\"") ? 1 : 0;
        return wifiSsid.substring(head, wifiSsid.length() - tail);
    }

    public final static boolean isEmailValid(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isAppInstalledAndActive(String uri, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            boolean isEnabled = pm.getApplicationInfo(uri, 0).enabled;
            Log.i("isEnabled", "return isEnabled installed 1 isEnabled " + isEnabled);
            //return true;
            return isEnabled;
        } catch (Exception e) {
        }
        Log.i("isEnabled", "installed 0");
        return false;
    }

    public static boolean isAppInstalled(String packageName, Context context) {
        PackageManager pm = context.getPackageManager();
        boolean isAppInstalled = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            isAppInstalled = true;
        } catch (PackageManager.NameNotFoundException ignored) {}
        return isAppInstalled;
    }

    public static void setMaxBrightness(Boolean bool, Activity activity) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams layoutpars = window.getAttributes();
        /*int b = Settings.System.getInt(
                activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                0
        );
        Log.d("setMaxBrightness","Settings.System.getInt:"+b);*/

        if (bool){
            layoutpars.screenBrightness = 1.0f;
        }else{
            layoutpars.screenBrightness = -1.0f;
        }
        Log.d("setMaxBrightness", "layoutpars.screenBrightness:" + layoutpars.screenBrightness);
        window.setAttributes(layoutpars);
    }

    public static String stripeAccent(String text){

        JSONObject chars = new JSONObject();

        try{

            chars.put("a", new String[]{"á","à","ä","â","å","ã","æ","Ã ","Ã¡","Ã¢","Ã£","Ã¤","Ã¥","Ã¦"});
            chars.put("A", new String[]{"Ý","À","Â","Ä","Ã","Å","Æ","A€","Ã€","Ã\u0081","Ã‚","Ãƒ","Ã„","Ã…","Ã†"});

            chars.put("e", new String[]{"é","è","ë","ê","ę","Ã¨","Ã©","Ãª","Ã«"});
            chars.put("E", new String[]{"É","È","Ê","Ë","Ãˆ","Ã‰","ÃŠ","Ã‹"});

            chars.put("i", new String[]{"í","ì","ï","î","į","Ã¬","Ã­","Ã®","Ã¯"});
            chars.put("I", new String[]{"Í","Ï","Ï","Ý","Ì","Ý","Î","ÃŒ","Ã\u008D","ÃŽ","Ã\u008F"});

            chars.put("o", new String[]{"õ","œ","ø","ó","ø","ò","ö","ô","Ã°","Ã²","Ã³","Ã´","Ãµ","Ã¶","Ã¸"});
            chars.put("O", new String[]{"Õ","Œ","Ø","Ó","Ò","Ö","Ô","Ã’","Ã“","Ã”","Ã•","Ã—","Ã˜"});

            chars.put("u", new String[]{"ú","ù","ü","û","ū","Ã¹","Ãº","Ã»","Ã¼"});
            chars.put("U", new String[]{"Ú","Ù","Û","Ü","Ã™","Ãš","Ã›","Ãœ"});

            chars.put("n", new String[]{"ñ","Ã±"});
            chars.put("N", new String[]{"Ñ","Ã‘"});

            chars.put("c", new String[]{"ç","Ã§"});
            chars.put("C", new String[]{"Ç","Ã‡"});

            chars.put("y", new String[]{"ý","ÿ","Ã½","Ã¿"});
            chars.put("Y", new String[]{"Ý","Ÿ","Ã\u009D"});

            chars.put("s", new String[]{"š","š"});
            chars.put("z", new String[]{"ž"});
            chars.put("Z", new String[]{"Ž"});

        } catch(JSONException e){
            e.printStackTrace();
        }

        String[] spaces = new String[]{"°","®","Â¿", "¿", "º", "“", "”","?"};

        Iterator<?> keys = chars.keys();

        while( keys.hasNext() ) {
            String key = (String)keys.next();

            String[] elements = new String[0];
            try {
                elements = (String[]) chars.get(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for (String s: elements) {
                text = text.replaceAll(s, key);
            }
        }

        for(int i = 0; i < spaces.length; i++){
            text = text.replace(spaces[i], "");
        }

        text = text.replaceAll("([^A-Za-z0-9 ,:.;/+-_~])", "");

        return text;
    }

    public static String getEd25519Keys(Context context) {
        final SharedPreferences prefs = getPreferenciasCompartidas(context);
        return prefs.getString("ed25519_keys","");
    }

    public static void setEd25519Keys(Context context, String value) {
        SharedPreferences prefs = getPreferenciasCompartidas(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ed25519_keys", value);
        editor.commit();
    }

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String bytesToHex(byte[] raw) {
        if ( raw == null ) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(Character.forDigit((b & 0xF0) >> 4, 16))
                    .append(Character.forDigit((b & 0x0F), 16));
        }
        return hex.toString();
    }

    public static void setParkingPublicKeySign(Context context, String value) {
        SharedPreferences prefs = getPreferenciasCompartidas(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("public_parking_sign", value);
        editor.commit();
    }

    public static byte[] getParkingPublicKeySign(Context context) {
        final SharedPreferences prefs = getPreferenciasCompartidas(context);
        return hexToBytes(prefs.getString("public_parking_sign", ""));
    }

    public static boolean isParkingPublicKeySign(Context context) {
        final SharedPreferences prefs = getPreferenciasCompartidas(context);
        return !prefs.getString("public_parking_sign", "").equals("");
    }

    public static void setParkingPublicKeyEnc(Context context, String value) {
        SharedPreferences prefs = getPreferenciasCompartidas(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("public_parking_enc", value);
        editor.commit();
    }

    public static byte[] getParkingPublicKeyEnc(Context context) {
        final SharedPreferences prefs = getPreferenciasCompartidas(context);
        return hexToBytes(prefs.getString("public_parking_enc", ""));
    }

    public static boolean isParkingPublicKeyEnc(Context context) {
        final SharedPreferences prefs = getPreferenciasCompartidas(context);
        return !prefs.getString("public_parking_enc", "").equals("");
    }

    public static void webViewPost(WebView webView, String html) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(false);
        webSettings.setLoadWithOverviewMode(false);
        webSettings.setUseWideViewPort(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(false);
        //Log.i("userAgent", webSettings.getUserAgentString());
        webSettings.setDefaultTextEncodingName("utf-8");
        webView.loadData(html, "text/html", "UTF-8");
        Log.d("WebPayEnWebViewAct", "webView.loadData html:"+ html);
    }

    public static boolean existUserData(Context context) {
        String str = Utils.getDefaultString("login", context);
        return !str.equals("");
    }

    /*
     * Returns:
     * 0 if version1 is equal to version2,
     * 1 if version1 is greater than version2,
     * -1 if version1 is less than version2.
     */
    public static int compareAppVersion(String version1, String version2) {
        // esto es para quitar la g o la h al final del numero de
        // version, o cualquier otro caracter
        version1 = version1.replaceAll("[^\\d.]", "");
        version2 = version2.replaceAll("[^\\d.]", "");

        version1 = version1.equals("")? "0.0.0.0.0":version1;
        version2 = version2.equals("")? "0.0.0.0.0":version2;

        String[] version1Parts = version1.split("\\.");
        String[] version2Parts = version2.split("\\.");
        int result = 0;
        int len = Math.min(version1Parts.length, version2Parts.length);
        for(int i=0; i<len && result==0; i++){
            result = Integer.parseInt(version1Parts[i]) - Integer.parseInt(version2Parts[i]);
        }
        return result != 0 ? result:version1.compareTo(version2);
    }

    public static void showCustomToast(Activity activity, String text) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) activity.findViewById(R.id.custom_toast_container));

        TextView textView = (TextView) layout.findViewById(R.id.text);
        textView.setText(text);

        Toast toast = new Toast(activity.getApplicationContext());
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, -50);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }



    public static void showToast(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        //View toastView = toast.getView();
        //TextView toastMessage = toastView.findViewById(android.R.id.message);
        //toastMessage.setTextSize(20);
        toast.show();
    }

    public static void safeDismissDialog(Activity activity, ProgressDialog pDialog) {
        if(activity == null || pDialog == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed()) { // or call isFinishing() if min sdk version < 17
                return;
            }
        }else{
            if (activity.isFinishing()) {
                return;
            }
        }

        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    static public Map<String, String> countryCodesToRegexMap;

    public static boolean isPhoneFormatValid(String phone, Map<String, String> codesToRegex){
        if(phone == null || phone.length() < 2){
            return false;
        }

        phone = phone
                .replace(" ", "")
                .replace("+", "");

        for (int i = 4; i > 0; i--) {

            if(phone.length() < i){
                continue;
            }

            String tentativeCode = phone.substring(0, i);
            String regex = codesToRegex.get(tentativeCode);
            if (regex != null && phone.substring(i).matches(regex)) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, String>  generateCodesToRegexMap(JSONArray countries){
        Map<String, String> response = new HashMap<>();
        for (int i = 0; i < countries.length(); i++) {
            try {
                JSONObject country = countries.getJSONObject(i);
                if (country.has("phone_validation_regex")
                        && country.has("calling_code")) {
                    String code = country.getString("calling_code");
                    String regex = country.getString("phone_validation_regex");
                    if(!code.equals("") && !regex.equals("")) {
                        response.put(code, regex);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return response;
    }

    public static String encodeForURL(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String stringListToStringCommaSeparated(List<String> list) {
        StringBuilder result =  new StringBuilder();
        String separator = "";
        for(String string: list){
            result.append(separator);
            result.append(string);
            separator = ",";
        }
        return result.toString();
    }

    public static String getMobileLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public static JSONObject loadJSONFromAsset(Activity activity, String filename) {
        JSONObject jsonObject = null;
        try {
            InputStream is = activity.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String str = new String(buffer, "UTF-8");
            jsonObject = new JSONObject(str);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    public static void updateApp(final Activity activity) {
        final WindowManager manager = (WindowManager) activity.getBaseContext().getSystemService(Context.WINDOW_SERVICE);

        LayoutInflater layoutInflater = (LayoutInflater) activity.getBaseContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.update_available, null);
        final PopupWindow popupWindow = new PopupWindow(
                layout,
                ViewPager.LayoutParams.WRAP_CONTENT,
                ViewPager.LayoutParams.WRAP_CONTENT);

        Point point = new Point();
        manager.getDefaultDisplay().getSize(point);
        popupWindow.setWidth(point.x * 4 / 5);

        TextView dismiss = (TextView) layout.findViewById(R.id.cancel);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        Button action = (Button) layout.findViewById(R.id.action);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity.getPackageName())));
                } catch (android.content.ActivityNotFoundException anfe) {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + activity.getPackageName())));
                }
                activity.finish();
            }
        });

        new Handler().postDelayed(new Runnable() {
            public void run() {
                popupWindow.showAtLocation(activity.findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
            }
        }, 150);
        Utils.setDefaultBoolean("show_update", false, activity.getApplicationContext());
    }

    public static JSONObject getUserPropertyJsonById(Context context, int propertyId) {
        try {
            JSONObject userJson = Utils.getDefaultJSONObject("user", context);
            JSONArray propertiesJson = userJson.getJSONArray("properties");
            for (int i = 0; i < propertiesJson.length(); i++) {
                JSONObject propertyJson = propertiesJson.getJSONObject(i);
                if (propertyJson.getInt("house_id") == propertyId) {
                    return propertyJson;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSummary(
             Context activity,
             String summaryType,
             Invitation invitation,
             ArrayList<String> guestNamesOrOrganizerName,
             boolean showSectors,
             boolean areAllSectorsSelected,
             String selectedCondoName,
             String selectedPropertyName,
             String selectedSectorNameList){

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

        boolean isTimeModeManyDays =
                InvitationCustomizationActivity.getTimeMode(invitation)
                        .equals(InvitationCustomizationActivity.TIME_MODE_MANY_DAYS);

        String guestsOrOrganizerString = guestNamesOrOrganizerName.get(0);
        if(guestNamesOrOrganizerName.size() > 1){
            guestsOrOrganizerString = String.format(activity.getString(R.string.summary_guest_name_and_n_more),
                    guestNamesOrOrganizerName.get(0),
                    Integer.toString(guestNamesOrOrganizerName.size() - 1));
        }

        String whoAndWhereStringScheme = "";
        if(summaryType.equals(Consts.SUMMARY_TYPE_TO)) {
            whoAndWhereStringScheme = activity.getString(R.string.summary_who_and_where_case_to);
        } if(summaryType.equals(Consts.SUMMARY_TYPE_FROM)) {
             whoAndWhereStringScheme = activity.getString(R.string.summary_who_and_where_case_from);
        }

        String placeName = String.format("%s - %s" ,selectedPropertyName, selectedCondoName);
        String whoAndWhereSentence = String.format(whoAndWhereStringScheme, guestsOrOrganizerString, placeName);

        String startDateStr = dateFormat.format(invitation.getStartDateTimeCalendar().getTime());
        String endDateStr = dateFormat.format(invitation.getEndDateTimeCalendar().getTime());
        String startTimeStr = timeFormat.format(invitation.getStartDateTimeCalendar().getTime());
        String endTimeStr =  timeFormat.format(invitation.getEndDateTimeCalendar().getTime());

        String daysString = String.format(activity.getString(R.string.summary_dates), startDateStr, endDateStr);
        String hoursString = activity.getString(R.string.summary_times_all_day);
        if(invitation.isCustomTime()){
            hoursString = String.format(activity.getString(R.string.summary_times), startTimeStr, endTimeStr);
        }
        String daysHoursSentence = daysString + ". " + hoursString;

        int daysDuration = invitation.daysInPeriodCount();
        if(daysDuration == 1){// 1 dia
            daysString = String.format(activity.getString(R.string.summary_date_one_day), startDateStr);
            daysHoursSentence = daysString + ". " + hoursString;
        } else if(daysDuration == 2){// 2 dias
            if(invitation.isCustomTime()){
                 daysHoursSentence = String.format(activity.getString(R.string.summary_dates_times_two_days),
                         startDateStr, startTimeStr, endDateStr, endTimeStr);
            }
        }

        String daysOfWeekSentence = activity.getString(R.string.summary_days_of_week_all);
        if(daysDuration == 1 || daysDuration == 2){// 1 dia
            daysOfWeekSentence = "";
        }else{
            if(isTimeModeManyDays && invitation.isCustomDaysOfWeek()){
                List<Integer> daysOfWeekValids = invitation.getDaysOfWeekValids();
                if(daysOfWeekValids.size() < 7) {
                    daysOfWeekSentence = String.format(activity.getString(R.string.summary_days_of_week),
                            invitation.getDaysOfWeekValidsAsShortNamesString(activity));
                }
            }
        }

        String sectorsSentence = "";
        if(showSectors){
            sectorsSentence= activity.getString(R.string.summary_sectors_all);
            if(!areAllSectorsSelected){
                sectorsSentence = String.format(activity.getString(R.string.summary_sectors),
                        selectedSectorNameList);
            }
        }

        String plateSentence = "";
        if (invitation.isPlateNumberUsed() && !invitation.getPlateNumber().equals("")){
            plateSentence = String.format(activity.getString(R.string.summary_plates), invitation.getPlateNumber());
        }

        String[] arr =  new String [] {
                whoAndWhereSentence,
                daysHoursSentence,
                daysOfWeekSentence,
                sectorsSentence,
                plateSentence
        };

        StringBuilder result = new StringBuilder();
        for(int i = 0; i < arr.length; i++){
            if(arr[i].length() > 0) {
                result.append(arr[i]);
                if(!".".equals(arr[i].substring(arr[i].length() - 1))) {
                    result.append(".");
                }
                result.append(" ");
            }
        }

        return result.toString().trim();
    }

    public static boolean isJSONArrayAndHasElements(String str) {
        if(str == null || str.equals("") || str.equals("[]")){
            return false;
        }
        try {
            JSONArray condoPhonesJson = new JSONArray(str);
            if(condoPhonesJson.length() > 0){
                return true;
            }
        } catch (JSONException e) {
            Log.i("isJSONArrayAndHasElem", "JSONException");
            e.printStackTrace();
        }
        return false;
    }

    public static String getVersionInfo(Context context) {
        StringBuilder versionInfo = new StringBuilder();
        if(!BuildConfig.environment.equals("")){
            versionInfo.append(BuildConfig.environment + " - ");
        }
        if(Config.DEBUG){
            versionInfo.append("Debuggable - ");
        }
        versionInfo.append(Utils.getVersionApp(context));

        return versionInfo.toString();

    }

}