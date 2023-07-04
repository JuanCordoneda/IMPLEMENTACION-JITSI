package com.safecard.android.model;

import android.content.Context;
import android.util.Log;

import com.safecard.android.apicallers.ApiCallback;
import com.safecard.android.apicallers.WebpayAuthorizeApiCaller;
import com.safecard.android.apicallers.WebpayPaymentMethodsApiCaller;
import com.safecard.android.apicallers.WebpayRemovePaymentMethodApiCaller;
import com.safecard.android.model.modelutils.GenericModelCallback;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alonso on 07/12/17.
 */

public class PaymentMethodModel {
    private static final String TAG = "PaymentMethodModel";

    public static void payAuthorize(final Context context, final int amount) {
        int defaultCardId = getDefaultPaymentMethodId(context);
        if (defaultCardId < 0){
            Log.i(TAG, "error payAuthorize con defaultCardId: " + defaultCardId);
            return;
        }
        /*new WebpayAuthorizeApiCaller(context, defaultCardId, amount).doCall(new ApiCallback() {
            @Override
            public void callSuccess(JSONObject response) {
                JSONObject json = Utils.getDefaultJSONObject("authorizeResponse", context);
                if(json.has("transactionId") && json.has("authorizationCode")) {
                    try {
                        String authorizationCode = json.getString("authorizationCode");
                        String transactionId = json.getString("transactionId");

                        Log.i(TAG, "Autorización Webpay correcta" +
                                        " (Pago:$"+ amount +
                                        " transactionId:"+transactionId +
                                        " authorizationCode:"+authorizationCode + ")");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void callError(String errorType, String msg) {
                if (!msg.equals("")){
                    Log.i(TAG, "pay: " +  msg);
                }
            }
        });*/
    }

    public static void udpateList(final Context context, final GenericModelCallback cb){
        new WebpayPaymentMethodsApiCaller(context).doCall(new ApiCallback() {
            @Override
            public void callSuccess(JSONObject response) {
                cb.callSuccess();
            }

            @Override
            public void callError(String errorType, String msg) {
                cb.callError(errorType, msg);
                if (!msg.equals("")){
                    Log.i(TAG, "udpateList: " + msg);
                }
            }
        });
    }

    public static void udpateList(final Context context){
        udpateList(context, new GenericModelCallback());
    }

    public static int getDefaultPaymentMethodId(final Context context){
        int id =  Utils.getDefaultInt("default_payment_method_id", context);
        if (id != -1 && !exist(context,id)){
            id = -1;
            setDefaultPaymentMethodId(context,id);
        }
        if (id == -1 && arePaymentMethodEnrolled(context)){
            try {
                id = getAllJSONArray(context).getJSONObject(0).getInt("id");
                setDefaultPaymentMethodId(context,id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return id;
    }

    public static void setDefaultPaymentMethodId(final Context context, int id){
        Utils.setDefaultInt("default_payment_method_id", id, context);
    }

    public static boolean arePaymentMethodEnrolled(final Context context){
        JSONArray array = Utils.getDefaultJSONArray("payment_methods", context);
        return array.length() > 0;
    }

    public static JSONArray getAllJSONArray(final Context context){
        return Utils.getDefaultJSONArray("payment_methods", context);
    }

    public static  boolean exist(final Context context, int id){
        JSONArray array = Utils.getDefaultJSONArray("payment_methods", context);
        try {
            for (int i = 0; i < array.length(); i++) {
                if(array.getJSONObject(i).getInt("id") == id){
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<JSONObject> getAllList(final Context context){
        JSONArray array = Utils.getDefaultJSONArray("payment_methods", context);
        List<JSONObject> list = new ArrayList<>();
        try {
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void remove(final Context context, String id, final GenericModelCallback cb){
        new WebpayRemovePaymentMethodApiCaller(context, id).doCall(new ApiCallback() {
            @Override
            public void callSuccess(JSONObject response) {
                udpateList(context);
                cb.callSuccess(true);
            }

            @Override
            public void callError(String errorType, String msg) {
                if (!msg.equals("")){
                    Log.i(TAG, "remove: " + msg);
                }
                cb.callError(errorType, msg);
            }
        });
    }
}



