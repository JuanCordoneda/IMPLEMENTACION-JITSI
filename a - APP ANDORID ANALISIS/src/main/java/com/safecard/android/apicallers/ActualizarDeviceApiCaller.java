package com.safecard.android.apicallers;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import com.safecard.android.Config;
import com.safecard.android.services.MessageToken;
import com.safecard.android.utils.AsymmetricCryptoUtils;
import com.safecard.android.utils.DeviceIdManager;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alonso on 21/06/17.
 */

public final class ActualizarDeviceApiCaller extends ContextWrapper {
    private final String TAG = "ActualizarDeviceApiC";
    private boolean doCall, waitForResponse;
    private boolean isCallbackCalled;
    String url;
    String publicKeyEncrypt, publicKeySign;
    String device;


    int retryTimes = 0;

    public ActualizarDeviceApiCaller(Context context) {
        super(context);
        device = "_";
        publicKeyEncrypt = "_";
        publicKeySign = "_";
        waitForResponse = false;
        doCall = false;
        isCallbackCalled = false;

        String lastVersion = Utils.getPersistedVersionApp(getApplicationContext());
        String actualVersion = Utils.getVersionApp(getApplicationContext());
        Log.i(TAG, "lastVersion " + lastVersion);
        if (!lastVersion.equals(actualVersion)) {
            doCall = true;
            if (Utils.compareAppVersion(lastVersion, "2.7.0") < 0 ||
                    Utils.compareAppVersion(actualVersion, "2.7.0") < 0) {
                waitForResponse = true;
            }
            Utils.setDefaultBoolean("obsoleteVersion", false, getApplicationContext());
        }
        String lastVersionAndroid = Utils.getPersistedAndroidVersion(getApplicationContext());
        String actualVersionAndroid = Utils.getAndroidVersion();
        if (!actualVersionAndroid.equals(lastVersionAndroid)) {
            Log.i(TAG, "!lastVersionAndroid");
            doCall = true;
        }

        DeviceIdManager deviceIdManager = new DeviceIdManager(getApplicationContext());
        if (!deviceIdManager.exist()) {
            Log.i(TAG, "deviceIdManager !exist");
            waitForResponse = true;
            doCall = true;
        }

        AsymmetricCryptoUtils asymmetricCryptoUtils = new AsymmetricCryptoUtils(getApplicationContext());
        if (!asymmetricCryptoUtils.exist() || !asymmetricCryptoUtils.existApiKeys()) {
            Log.i(TAG, "asymmetricCryptoUtils !exist");
            doCall = true;
        }

        if (!Utils.isParkingPublicKeySign(getApplicationContext())) {
            System.out.println("!parkingPublicKeySign");
            waitForResponse = true;
            doCall = true;
        }

        if (!Utils.isParkingPublicKeyEnc(getApplicationContext())) {
            System.out.println("!parkingPublicKeyEnc");
            waitForResponse = true;
            doCall = true;
        }

    }

    public void doCall(final ApiCallback callback) {

        DeviceIdManager deviceIdManager = new DeviceIdManager(getApplicationContext());
        if (!deviceIdManager.exist()) {
            Log.i(TAG, "deviceIdManager create");
            deviceIdManager.create();
            device = deviceIdManager.getDeviceId();
        }

        final AsymmetricCryptoUtils asymmetricCryptoUtils = new AsymmetricCryptoUtils(getApplicationContext());
        if (!asymmetricCryptoUtils.exist()) {
            Log.i(TAG, "asymmetricCryptoUtils create");
            asymmetricCryptoUtils.create();
            publicKeyEncrypt = asymmetricCryptoUtils.getTentativePublicKeyEncrypt();
            publicKeySign = asymmetricCryptoUtils.getTentativePublicKeySign();
        }

        Log.i(TAG,"waitForResponse:" + waitForResponse);
        if (!waitForResponse) {
            callSuccess(callback, new JSONObject());
        }

        MessageToken.request(new MessageToken.MessageTokenCallback() {
                    @Override
                    public void onComplete(String fullDevice, String origin) {
                        String lastFullDevice = Utils.getPersistedFullDevice(getApplicationContext());
                        String fullDeviceWithoutCompany = fullDevice;
                        if (origin.equals("HUAWEI") || origin.equals("GOOGLE")){
                            fullDevice = "[" + origin + "]" + fullDevice;
                        }
                        if (/*!"".equals(fullDeviceWithoutCompany) &&*/ !fullDevice.equals(lastFullDevice)) {
                            Log.i(TAG, "!lastFullDevice");
                            doCall = true;
                        }

                        Log.i(TAG,"doCall:" + doCall);
                        if (!doCall) {
                            callSuccess(callback, new JSONObject());
                            Log.i(TAG, "nothing changes: not do call actualizar_device");
                            return;
                        }

                        url = Config.ApiUrl + "actualizar_device/" +
                                Utils.getMobile(getApplicationContext()) +
                                "/AND/" +
                                fullDevice + "/" +
                                Utils.getVersionApp(getApplicationContext()) + "/" +
                                Utils.getAndroidVersion() + "/" +
                                device + "/" +
                                publicKeyEncrypt + "/" +
                                publicKeySign;

                        final RequestVolley rqv = new RequestVolley(url, getApplicationContext());
                        final String finalDevice = device;
                        final String finalFullDevice = fullDevice;
                        final String finalPublicKeySign = publicKeySign;
                        final String finalPublicKeyEncrypt = publicKeyEncrypt;

                        Log.i(TAG, "pre actualizar_device requestApi");
                        rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
                            @Override
                            public void onSuccess(JSONObject response) {
                                Log.i(TAG, "actualizar_device onSuccess");
                                try {
                                    if (response.getString("result").equals("ACK")) {
                                        Log.i(TAG, "actualizar_device ACK");

                                        if (!finalDevice.equals("_")) {
                                            String apiDevice = response.getString("device");
                                            if (apiDevice != null && finalDevice.equals(apiDevice)) {
                                                Utils.setDeviceId(getApplicationContext(), apiDevice);
                                            } else {
                                                retry(callback, "apiDevice!=device", response.toString());
                                                return;
                                            }
                                        }

                                        if (!finalPublicKeySign.equals("_") && !finalPublicKeyEncrypt.equals("_")) {
                                            String apiPublicKeySign = response.getString("public_phone_sign");
                                            String apiPublicKeyEncrypt = response.getString("public_phone_enc");
                                            if (apiPublicKeySign != null && finalPublicKeySign.equals(apiPublicKeySign) &&
                                                    apiPublicKeyEncrypt != null && finalPublicKeyEncrypt.equals(apiPublicKeyEncrypt)) {
                                                asymmetricCryptoUtils.persist();
                                            } else {
                                                retry(callback, "apiAppPublicKey!=publicKey", response.toString());
                                                return;
                                            }
                                        }

                                        if (response.has("public_api_sign") && response.has("public_api_enc")) {
                                            String publicApiSign = response.getString("public_api_sign");
                                            String publicApiEnc = response.getString("public_api_enc");
                                            asymmetricCryptoUtils.persistApi(publicApiEnc, publicApiSign);
                                            Log.d("HEADERS", "si public_api_sign si public_api_enc");
                                        } else {
                                            Log.d("HEADERS", "no public_api_sign no public_api_enc");
                                        }

                                        if (response.has("public_parking_sign")) {
                                            String key = response.getString("public_parking_sign");
                                            Utils.setParkingPublicKeySign(getApplicationContext(), key);
                                        }

                                        if (response.has("public_parking_enc")) {
                                            String key = response.getString("public_parking_enc");
                                            Utils.setParkingPublicKeyEnc(getApplicationContext(), key);
                                        }

                                        Utils.setPersistedFullDevice(getApplicationContext(), finalFullDevice);

                                        Utils.setPersistedVersionApp(
                                                getApplicationContext(),
                                                Utils.getVersionApp(getApplicationContext()));

                                        Utils.setPersistedAndroidVersion(
                                                getApplicationContext(),
                                                Utils.getAndroidVersion());

                                        //Se actualiza el key de persistencia que se desencripta
                                        String user = Utils.getDefaults("user", getApplicationContext());
                                        if (user != null) {
                                            JSONObject user_json = new JSONObject(user);
                                            if (user_json.has("key")) {
                                                user_json.put("key", response.getString("key"));
                                                Utils.setDefaults("user", user_json.toString(), getApplicationContext());
                                            }
                                        }
                                        Log.i(TAG, "ActualizarDeviceApiCaller onSuccess");
                                        callSuccess(callback, response);
                                        return;
                                    } else {
                                        retry(callback, "NACK", response.getString("msg"));
                                        return;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                retry(callback, "JSONException", "");
                                return;
                            }

                            @Override
                            public void onError(String errorType, String msg) {
                                retry(callback, errorType, msg);
                            }
                        });


                    }
                }, getApplicationContext());
    }

    public void retry(final ApiCallback callback, String errorType, String msg) {
        Log.i(TAG, "errorType:" + errorType + " msg:" + msg);
        if (waitForResponse) {
            if (retryTimes > 2) {
                Log.i(TAG, "retry end");
                Log.i(TAG, "callError");
                callError(callback, errorType, msg);
            } else {
                retryTimes++;
                Log.i(TAG, "retryTimes " + retryTimes);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        doCall(callback);
                    }
                }).start();
            }
        }
    }

    public void callSuccess(ApiCallback callback, JSONObject json) {
        if (!isCallbackCalled) {
            callback.callSuccess(json);
            isCallbackCalled = true;
        }
    }

    public void callError(ApiCallback callback, String errorType, String msg) {
        if (!isCallbackCalled) {
            callback.callError(errorType, msg);
            isCallbackCalled = true;
        }
    }
}