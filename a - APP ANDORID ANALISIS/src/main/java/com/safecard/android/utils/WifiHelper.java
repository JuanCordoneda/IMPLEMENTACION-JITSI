package com.safecard.android.utils;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.safecard.android.Consts;
import com.safecard.android.Config;

import java.util.List;

public class WifiHelper {
    private static String TAG = "WifiReceiver";

    private static boolean isShowManualWifiConnection = false;
    private static String ssid = "";
    private static Handler handler;
    private static int millis = 60000;
    public static boolean isLocal() {
        return ssid.contains(Config.SSID);
    }


    public static void startChecking(final Context context) {
        if(handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }

        if(context != null) {
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    check(context);
                    handler.postDelayed(this, millis);
                }
            }, millis);
        }
    }

    public static void check(final Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && wifiManager.getConnectionInfo().getSSID() != null) {
            ssid = Utils.trimSsid(wifiManager.getConnectionInfo().getSSID());
        }
    }

    public static void setSpeed(int m, Context context) {
        millis = m;
        startChecking(context);
    }

    public static void connect(final Activity activity){

        if(activity == null){
            return;
        }

        final WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        activity.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, " ----- Wifi  onReceive SUPPLICANT_STATE_CHANGED_ACTION----- ");
                Log.i(TAG, intent.getAction() + " state:" + intent.getExtras().get(WifiManager.EXTRA_NEW_STATE));
                if(intent.getExtras().get(WifiManager.EXTRA_NEW_STATE).equals(SupplicantState.COMPLETED)){
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo.getSSID() != null) {
                        String ssid = Utils.trimSsid(wifiInfo.getSSID());
                        if (ssid.contains(Config.SSID)) {
                            isShowManualWifiConnection = false;
                        }
                    }
                }
            }
        }, intentFilter);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            activity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    Consts.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            return;
        }

        wifiManager.setWifiEnabled(true);
        isShowManualWifiConnection = true;
        Handler h = new Handler();
        h.postDelayed(new Runnable(){
            public void run(){
                if(isShowManualWifiConnection){
                    activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    isShowManualWifiConnection = false;
                }
            }
        }, 5000);

        wifiManager.startScan();
        String fullNetworkSSID = "";
        String security = "NONE";
        List<ScanResult> scanned = wifiManager.getScanResults();

        for (ScanResult i : scanned) {
            if (i.SSID != null && i.SSID.contains(Config.SSID)) {
                fullNetworkSSID = i.SSID;
                if (i.capabilities.contains("WEP")) {
                    security = "WEP";
                } else if (i.capabilities.contains("PSK")) {
                    security = "PSK";
                } else if (i.capabilities.contains("EAP")) {
                    security = "EAP";
                }
                Log.i("WifiReceiver", "fullNetworkSSID: " +fullNetworkSSID + " security: " + security);
                break;
            }
            Log.i("WifiReceiver", "ScanResult: " +i.SSID);
        }

        Log.i("WifiReceiver", "conf.SSID: "+ fullNetworkSSID);
        if (!fullNetworkSSID.equals("")) {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + fullNetworkSSID + "\"";

            if (security.equals("NONE")) {
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            } else if (security.equals("PSK") || security.equals("EAP")) {
                conf.preSharedKey = "\"" + Config.wifiPassword + "\"";
            }

            wifiManager.addNetwork(conf);

            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals("\"" + fullNetworkSSID + "\"")) {

                    Log.i("WifiReceiver", "reconnect: " + i.SSID);
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(i.networkId, true);
                    wifiManager.reassociate();
                    check(activity);
                    break;
                }
            }
        }
    }
}