package com.safecard.android.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.activities.IncomingCallActivity;
import com.safecard.android.activities.LoginActivity;
import com.safecard.android.activities.SplashScreenActivity;
import com.safecard.android.utils.Foreground;
import com.safecard.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NotificationUtils2 {
    private static final String TAG = "NotificationUtils2";
    public static final String UPDATE_INVITATION_LIST_ACTION = "update_invitation_list_action";

    public static final String CATEGORY_MOVEMENT = "CATEGORY_MOVEMENT";
    public static final String CATEGORY_CHARGE = "CATEGORY_CHARGE";
    public static final String CATEGORY_INVITATION = "CATEGORY_INVITATION";
    public static final String CATEGORY_NEW_RESIDENT = "CATEGORY_NEW_RESIDENT";
    public static final String CATEGORY_NO_CATEGORY = "CATEGORY_NO_CATEGORY";

    public static final String NOTIFICATION_TYPE_PUSH = "NOTIFICATION_TYPE_PUSH";
    public static final String NOTIFICATION_TYPE_ALERT = "NOTIFICATION_TYPE_ALERT";
    public static final String NOTIFICATION_TYPE_TOAST = "NOTIFICATION_TYPE_TOAST";

    public static final String MESSAGE_TYPE_NOTIFICATION = "MESSAGE_TYPE_NOTIFICATION";
    public static final String MESSAGE_TYPE_CALL = "MESSAGE_TYPE_CALL";
    public static final String MESSAGE_TYPE_AUTHORIZATION_REQUEST = "MESSAGE_TYPE_AUTHORIZATION_REQUEST";

    public static final String CHANNEL_ID_DEFAULT = "my_channel_id_01";
    public static final String CHANNEL_ID_CALLS = "safecard_calls_channel_id";

    public static void processMessage(Map<String, String> originalData, Context context) {
        Map<String, String> dataWithDefaults = getDataWithDefaults(originalData);
        String messageType = getMessageType(dataWithDefaults);
        processMessageByMessageType(messageType, dataWithDefaults, context);

        sendUpdateInvitationListBroadcast(context);
    }

    private static Map<String, String> getDataWithDefaults(Map<String, String> notificationData) {
        Map<String, String> result = new HashMap<>();
        result.put("message", "-");
        result.put("title", "-");
        result.put("subtitle", "");
        result.put("tickerText", "");
        result.put("type", "PUSH");
        result.put("element_type", "");
        result.put("extra_data", "");

        for (String item: result.keySet()) {
            if(notificationData.containsKey(item)){
                result.put(item, notificationData.get(item));
            }
        }

        return result;
    }

    private static String getMessageType(Map<String, String> data) {
        String type = "NOTIFICATION";
        if(data.containsKey("element_type") && data.get("element_type") != null){
            type = data.get("element_type");
        }
        switch (type){
            case "CALL":
                return MESSAGE_TYPE_CALL;
            case "AUTHORIZATION_REQUEST":
                return MESSAGE_TYPE_AUTHORIZATION_REQUEST;
            default:
                return MESSAGE_TYPE_NOTIFICATION;
        }
    }

    private static void sendUpdateInvitationListBroadcast(Context context) {
        Intent in = new Intent();
        in.setAction(UPDATE_INVITATION_LIST_ACTION);
        LocalBroadcastManager.getInstance(context).sendBroadcast(in);
    }

    private static void processMessageByMessageType(
            String messageType, Map<String, String> data, Context context) {

        switch (messageType){
            case MESSAGE_TYPE_CALL:
                processIncomingCall(data, context);
                return;
            case MESSAGE_TYPE_AUTHORIZATION_REQUEST:
                //TODO
                return;
            case MESSAGE_TYPE_NOTIFICATION:
                processNotification(data, context);
                return;
        }
    }

    public static void processIncomingCall(Map<String, String> data, Context context) {
        showIncomingCallPush(data, context);
    }

    public static void showIncomingCallPush(Map<String, String> data, Context context) {
        try {
            String extraData = data.get("extra_data");
            if(extraData == null){
                return;
            }

            JSONObject json = new JSONObject(extraData);
            int conferenceSafecardId = -1; //json.getInt("conference_safecard_id");
            String conferenceDescription = json.getString("conference_description");

            JSONObject room = json.getJSONObject("room");
            String roomUrl = room.getString("server_url");
            String roomId = room.getString("id");
            String roomName = room.getString("name");
            String roomKey = room.getString("key");

            String message = data.get("message");
            String title = data.get("title");

            showIncomingCallPush(conferenceSafecardId, conferenceDescription,
                    roomUrl, roomId, roomName, roomKey, message, title, context);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // La función showIncomingCallPush() se utiliza para mostrar una notificación de llamada 
    // entrante en la aplicación
    // Se define una función estática llamada showIncomingCallPush() que toma varios parámetros, 
    // incluyendo el ID y la descripción de la conferencia, la URL de la sala, el ID, el nombre y la 
    // clave de la sala, el mensaje y el título de la notificación, y el contexto de la aplicación.
    public static void showIncomingCallPush(
            int conferenceSafecardId, String conferenceDescription,
            String roomUrl, String roomId, String roomName, String roomKey,
            String message, String title, Context context) {

        // Se crea un nuevo Intent llamado onTapIntent que apunta a la clase SplashScreenActivity. 
        // Se crea un Bundle y se agregan varios extras al Bundle utilizando las constantes definidas 
        // en la clase IncomingCallActivity para indicar la información de la llamada entrante. 
        // Luego, el Bundle se agrega al Intent.
        Intent onTapIntent = new Intent(context, SplashScreenActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(IncomingCallActivity.PARAM_CONFERENCE_SAFECARD_ID, conferenceSafecardId);
        bundle.putString(IncomingCallActivity.PARAM_CONFERENCE_DESCRIPTION, conferenceDescription);
        bundle.putString(IncomingCallActivity.PARAM_ROOM_ID, roomId);
        bundle.putString(IncomingCallActivity.PARAM_ROOM_SERVER_URL, roomUrl);
        bundle.putString(IncomingCallActivity.PARAM_ROOM_NAME, roomName);
        bundle.putString(IncomingCallActivity.PARAM_ROOM_KEY, roomKey);
        onTapIntent.putExtras(bundle);

        // Se establecen las banderas del Intent para indicar que la actividad se debe iniciar en una nueva 
        // tarea y se debe borrar cualquier actividad anterior en la tarea actual.
        onTapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Se crea un PendingIntent llamado resultPendingIntent utilizando el Intent onTapIntent. 
        // El PendingIntent se utilizará más adelante para abrir la actividad SplashScreenActivity 
        // cuando se toque la notificación.
        PendingIntent resultPendingIntent
                = PendingIntent.getActivity(
                context, 0, onTapIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Se define una variable shortNotificationSoundUri que actualmente está establecida en null. 
        // En este caso, no se está especificando un sonido personalizado para la notificación. 
        // A continuación, se llama a la función showPush() para mostrar la notificación utilizando 
        // los parámetros proporcionados, incluyendo el mensaje, el título, el canal de notificación, 
        // el sonido de la notificación, el PendingIntent y el contexto.
        Uri shortNotificationSoundUri = null;
        // se muestra una notificación utilizando la función showPush().
        showPush(message, title, "",
                CHANNEL_ID_CALLS, shortNotificationSoundUri, resultPendingIntent, context);
    }

    public static void processNotification(Map<String, String> data, Context context) {
        String notificationType = getNotificationType(data);
        processNotificationByNotificationType(notificationType, data, context);
    }

    private static String getNotificationType(Map<String, String> data) {
        String type = "PUSH";
        if(data.containsKey("type") && data.get("type") != null){
            type = data.get("type");
        }
        switch (type){
            case "ALERT":
                return NOTIFICATION_TYPE_ALERT;
            case "TOAST":
                return NOTIFICATION_TYPE_TOAST;
            default:
                return NOTIFICATION_TYPE_PUSH;
        }
    }

    private static void processNotificationByNotificationType(
            String notificationType, Map<String, String> data, Context context) {

        switch (notificationType){
            case NOTIFICATION_TYPE_PUSH:
                processPush(data, context);
                return;
            case NOTIFICATION_TYPE_TOAST:
                processToast(data, context);
                return;
            case NOTIFICATION_TYPE_ALERT:
                processPush(data, context);
                processToast(data, context);
                return;
        }
    }

    private static void processPush(Map<String, String> data, Context context) {
        if (!isPushInternalPermission(context)) {
            return;
        }
        showPush(data, context);
    }

    private static boolean isPushInternalPermission(Context context) {
        String InternalNotificationPermissionString
                = Utils.getDefaults("notification_perm", context);

        return InternalNotificationPermissionString == null
                || Boolean.parseBoolean(InternalNotificationPermissionString);
    }

    private static void processToast(Map<String, String> data, Context context) {
        if(!Foreground.foreground) {
            return;
        }
        showToast(data, context);
    }

    private static void showToast(Map<String, String> data, Context context) {
        showToast(data.get("message"), context);
    }

    private static void showToast(String message, Context context) {
        try {
            Utils.showToast(context, message);
        }catch (Exception e){
            Log.d(TAG, e.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Log.d(TAG, sw.toString());
        }
    }

    private static void showPush(Map<String, String> data, Context context) {
        Intent onTapIntent = new Intent(context, LoginActivity.class);
        onTapIntent.putExtra("EXTRA_DATA", data.get("extra_data"));
        onTapIntent.putExtra("GOTO", getGotoIdForPush(data.get("element_type")));

        PendingIntent resultPendingIntent
                = PendingIntent.getActivity(
                context, 0, onTapIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        showPush(data.get("message"), data.get("title"), data.get("tickerText"),
                CHANNEL_ID_DEFAULT, null, resultPendingIntent, context);
    }

    private static void showPush(String message, String title, String tickerText,
                                 String channelId, Uri soundUri, PendingIntent resultPendingIntent, Context context) {
        Log.d(TAG, "showPush");

        createNotificationChannels(context);

        if(soundUri == null){
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId);

        notificationBuilder
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(com.safecard.android.R.drawable.ic_push)
                .setColor(ContextCompat.getColor(context, com.safecard.android.R.color.colorAccent))
                .setTicker(tickerText)
                .setContentTitle(title)
                .setContentText(message)
                .setContentInfo(message)
                .setSound(soundUri)
                .setContentIntent(resultPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        }

        Random random = new Random();
        int rand = random.nextInt(9999 - 1000) + 1000;
        notificationManager.notify(rand, notificationBuilder.build());
    }

    private static void createNotificationChannels(Context context) {
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            //channel calls
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID_CALLS,
                            context.getString(R.string.service_notifications_channel_calls),
                            NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(
                    context.getString(R.string.service_notifications_channel_calls_description));
            notificationManager.createNotificationChannel(channel);

            //channel default
            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID_DEFAULT,
                            context.getString(R.string.service_notifications_channel_name),
                            NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription(
                    context.getString(R.string.service_notifications_channel_description));
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private static int getGotoIdForPush(String elementType) {
        String category = getCategoryFromElementType(elementType);
        switch (category){
            case CATEGORY_CHARGE: return Consts.ParkingActivityBillings;
            case CATEGORY_INVITATION: return Consts.InvitationActivity;
            case CATEGORY_NEW_RESIDENT: return Consts.AccessActivity;
            case CATEGORY_MOVEMENT: return Consts.AccessActivityMovements;
            default: return Consts.NotificationActivity;
        }
    }

    private static String getCategoryFromElementType(String elementType) {
        switch(elementType){
            case "INV": //Acceder al listado de invitaciones
                return CATEGORY_INVITATION;
            case "ADR": //Notificacion al ser agregrado a una propiedad.
                return CATEGORY_NEW_RESIDENT;
            case "BILL": //Notificacion de pago
                return CATEGORY_CHARGE;
            case "IN": //Notificacion de movimiento de entrada
            case "OUT": //Notificacion de movimiento de salida
            case "EXPIRED": //Notificacion de no retiro de invitado
                return CATEGORY_MOVEMENT;
            case "NCK1": //Notificaciones de error del LS
            case "NCK2": //Notificacion de stock de propiedades con patentes
            default:
                return CATEGORY_NO_CATEGORY;
        }
    }
}