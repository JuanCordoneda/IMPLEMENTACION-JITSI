package com.safecard.android.utils;

import android.app.Activity;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;

import java.net.MalformedURLException;
import java.net.URL;

public class CallUtils {
    // Se define la clase CallUtils como pública y se declara el método launchCall() 
    // que acepta varios parámetros necesarios para iniciar una llamada. 
    // Estos parámetros incluyen el nombre a mostrar del usuario, la URL del servidor Jitsi Meet, 
    // el ID de la sala, el nombre de la sala, la clave de la sala y la actividad actual.
    public static void launchCall(
            String displayName,
            String url,
            String roomId,
            String roomName,
            String roomKey,
            Activity activity) {

        // Aquí se crea un objeto URL a partir de la URL del servidor Jitsi Meet proporcionada. 
        // Si la URL es incorrecta, se imprime el rastreo de la excepción y se lanza una excepción 
        // RuntimeException con un mensaje de error.
        URL serverURL;
        try {
            serverURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid server URL!");
        }

        // Se crea un objeto JitsiMeetUserInfo para almacenar la información del usuario, 
        // y se establece el nombre a mostrar del usuario utilizando el valor proporcionado.
        JitsiMeetUserInfo jitsiMeetUserInfo = new JitsiMeetUserInfo();
        jitsiMeetUserInfo.setDisplayName(displayName);


        // Se configuran las opciones predeterminadas para la conferencia de Jitsi Meet 
        // utilizando un objeto JitsiMeetConferenceOptions.Builder(). Se establece la URL del 
        // servidor, la información del usuario y se desactivan varias funciones mediante los 
        // flags de características (feature flags).
        JitsiMeetConferenceOptions defaultOptions
                = new JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                //.setSubject(roomName)
                // When using JaaS, set the obtained JWT here
                //.setToken(roomKey)
                .setUserInfo(jitsiMeetUserInfo)
                //.setWelcomePageEnabled(false)

                .setFeatureFlag("pip.enabled",false)
                .setFeatureFlag("calendar.enabled",false)
                .setFeatureFlag("call-integration.enabled",false)
                .setFeatureFlag("close-captions.enabled",false)
                .setFeatureFlag("chat.enabled",false)
                .setFeatureFlag("invite.enabled",false)
                .setFeatureFlag("live-streaming.enabled",false)
                //.setFeatureFlag("meeting-name.enabled",false)
                .setFeatureFlag("meeting-password.enabled",false)
                .setFeatureFlag("raise-hand.enabled",false)
                .setFeatureFlag("video-share.enabled",false)
                .build();

        // Se establecen las opciones predeterminadas para las conferencias de Jitsi Meet utilizando 
        // el método setDefaultConferenceOptions() de la clase JitsiMeet
        JitsiMeet.setDefaultConferenceOptions(defaultOptions);

        JitsiMeetConferenceOptions options
                = new JitsiMeetConferenceOptions.Builder()
                .setSubject(roomName)
                .setRoom(roomId)
                .build();

        // Se crean las opciones específicas para la conferencia actual, estableciendo el nombre de la 
        // sala y el ID de la sala.
        JitsiMeetActivity.launch(activity, options);
    }
}
