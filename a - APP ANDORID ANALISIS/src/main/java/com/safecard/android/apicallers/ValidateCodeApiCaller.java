package com.safecard.android.apicallers;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import com.safecard.android.Config;
import com.safecard.android.utils.AsymmetricCryptoUtils;
import com.safecard.android.utils.DeviceIdManager;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alonso on 21/06/17.
 */

public final class ValidateCodeApiCaller extends ContextWrapper {
    private static final String TAG = "ValidateCodeApiCaller";
    String url;
    String code;
    String device, publicKeyEncrypt, publicKeySign;
    int retry = 0;
    public ValidateCodeApiCaller(Context context, String code) {
        super(context);
        this.code = code;
    }

    public void doCall(final ApiCallback callback){

        DeviceIdManager deviceIdManager = new DeviceIdManager(getApplicationContext());
        deviceIdManager.create();
        device = deviceIdManager.getDeviceId();

        final AsymmetricCryptoUtils asymmetricCryptoUtils = new AsymmetricCryptoUtils(getApplicationContext());
        asymmetricCryptoUtils.create();
        publicKeyEncrypt = asymmetricCryptoUtils.getTentativePublicKeyEncrypt();
        publicKeySign = asymmetricCryptoUtils.getTentativePublicKeySign();

        url = Config.ApiUrl + "validate_code/" +
                Utils.getDefaults("mobile_verificado", getApplicationContext()) +
                "/" + code +
                "/" + deviceIdManager.getDeviceId() +
                "/AND/" +
                Utils.getVersionApp(getApplicationContext()) +
                "/" + Utils.getAndroidVersion() +
                "/" + publicKeyEncrypt +
                "/" + publicKeySign;

        final RequestVolley rqv = new RequestVolley(url, getApplicationContext());
        final String finalDevice = device;
        final String finalPublicKeySign = publicKeySign;
        final String finalpublicKeyEncrypt = publicKeyEncrypt;

        rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getString("result").equals("ACK")) {

                        if(!finalDevice.equals("_")) {
                            String apiDevice = response.getString("device");
                            if(apiDevice != null && finalDevice.equals(apiDevice)) {
                                Utils.setDeviceId(getApplicationContext(), apiDevice);
                            }else {
                                //Log.e(TAG,"apiDevice!=device. FinalDevice:" + finalDevice + " response:" + response.toString());
                                retry(callback, "apiDevice!=device", "Validate code: Api device distinto a local device.");
                                return;
                            }
                        }

                        if(response.has("public_api_sign") && response.has("public_api_enc") ) {
                            String publicApiSign = response.getString("public_api_sign");
                            String publicApiEnc = response.getString("public_api_enc");
                            asymmetricCryptoUtils.persistApi(publicApiEnc, publicApiSign);
                        }

                        if(!finalPublicKeySign.equals("_") && !finalpublicKeyEncrypt.equals("_") ) {
                            String fromApiSign = response.getString("public_phone_sign");
                            String fromApiEncrypt = response.getString("public_phone_enc");
                            if(fromApiSign != null && finalPublicKeySign.equals(fromApiSign) &&
                                    fromApiEncrypt != null && finalpublicKeyEncrypt.equals(fromApiEncrypt)) {
                                asymmetricCryptoUtils.persist();
                            }else {
                                /*Log.e(TAG,"finalPublicKeySign!=fromApiSign"
                                            + " o finalpublicKeyEncrypt!=fromApiEncrypt. "
                                            + "finalPublicKeySign:" + finalPublicKeySign
                                            + " finalpublicKeyEncrypt:" + finalpublicKeyEncrypt
                                            + " response:" + response.toString());*/

                                retry(callback, "apiAppPublicKey!=publicKey", "Validate code: api publicKeySign distinto a local publicKeySign o api publicKeyEncrypt distinto a local publicKeyEncrypt.");
                                return;
                            }
                        }

                        if(response.has("public_parking_sign")) {
                            String key = response.getString("public_parking_sign");
                            Utils.setParkingPublicKeySign(getApplicationContext(), key);
                        }

                        if(response.has("public_parking_enc")) {
                            String key = response.getString("public_parking_enc");
                            Utils.setParkingPublicKeyEnc(getApplicationContext(), key);
                        }

                        Object userObj = response.get("user");
                        if( userObj instanceof JSONObject){
                            JSONObject userJson = response.getJSONObject("user");
                            userJson.put("mobile", Utils.getDefaults("mobile_verificado", getApplicationContext()));
                            Utils.setDefaults("login", userJson.toString(), getApplicationContext());
                        }
                        callback.callSuccess(response);
                        return;
                    } else {
                        retry(callback, "NACK", response.getString("msg"));
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    retry(callback, "JSONException", "");
                }
            }

            @Override
            public void onError(String errorType, String msg) {
                retry(callback, errorType, msg);
            }
        });
    }

    public void retry(final ApiCallback callback, String errorType, String msg) {
        if (retry >= 0) {
            Log.i(TAG, "retry end. callError");
            callback.callError(errorType, msg);
        } else {
            retry++;
            Log.i(TAG, "retry " + retry);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    doCall(callback);
                }
            }).start();
        }
    }
}