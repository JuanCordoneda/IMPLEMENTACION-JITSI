package com.safecard.android;

/**
 * Created by Alonso on 24-01-17.
 */

public class Config {

    final public static boolean DEBUG = "debug".equals(BuildConfig.BUILD_TYPE);

    final public static String ApiUrl = BuildConfig.apiUrl;
    final public static String ApiLocalUrl = BuildConfig.apiLocalUrl;
    final public static String mixpanelToken = BuildConfig.mixpanelToken;
    final public static String environment = BuildConfig.environment;
    final public static String apiKey = BuildConfig.apiKey;
    final public static String SSID = BuildConfig.ssid;
    final public static String wifiPassword = BuildConfig.wifiPassword;

    public static int time_refresh_qr = 5000;// MilliSeconds to refresh QR
    public static int time_restart = 300;  // Seconds in Background to reset view to default Activity

}
