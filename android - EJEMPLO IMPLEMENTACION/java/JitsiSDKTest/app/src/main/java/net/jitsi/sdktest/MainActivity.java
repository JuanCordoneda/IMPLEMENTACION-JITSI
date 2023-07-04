package net.jitsi.sdktest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.BroadcastIntentHelper;
import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;

import timber.log.Timber;
// El código proporcionado es una implementación de una actividad de Android en la que se utiliza el S
// DK de Jitsi Meet para realizar videoconferencias.
public class MainActivity extends AppCompatActivity {

    // Se define un receptor de difusión (BroadcastReceiver) llamado broadcastReceiver que se 
    // utiliza para recibir eventos de difusión enviados por el SDK de Jitsi Meet.
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onBroadcastReceived(intent);
        }
    };

    // En el método onCreate(), se establece el contenido de la actividad y se inicializan las
    //  opciones por defecto para las conferencias de Jitsi Meet. Luego, se llama al método 
    //  registerForBroadcastMessages() para registrar el receptor de difusión.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa las opciones por defecto para las conferencias de Jitsi Meet.
        URL serverURL;
        try {
            // Cuando se utiliza JaaS, reemplaza "https://meet.jit.si" con la URL correcta del servidor.
            serverURL = new URL("https://meet.jit.si");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("¡URL de servidor no válida!");
        }
        JitsiMeetConferenceOptions defaultOptions
                = new JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                // Cuando se utiliza JaaS, establece el JWT obtenido aquí
                //.setToken("MiJWT")
                // Se pueden establecer diferentes indicadores de funciones
                // .setFeatureFlag("toolbox.enabled", false)
                // .setFeatureFlag("filmstrip.enabled", false)
                .setFeatureFlag("welcomepage.enabled", false)
                .build();
        JitsiMeet.setDefaultConferenceOptions(defaultOptions);

        registerForBroadcastMessages();
    }

    // En el método onDestroy(), se anula el registro del receptor de difusión (broadcastReceiver) 
    // para evitar pérdidas de memoria.
    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);

        super.onDestroy();
    }

    // El método onButtonClick() se llama cuando se hace clic en un botón en la interfaz de usuario. 
    // Recupera el texto ingresado en un campo de edición (EditText), crea un objeto de opciones para 
    // unirse a la conferencia y lanza la actividad JitsiMeetActivity con las opciones especificadas.
    public void onButtonClick(View v) {
        EditText editText = findViewById(R.id.conferenceName);
        String text = editText.getText().toString();

        if (text.length() > 0) {
            // Crea el objeto de opciones para unirse a la conferencia. El SDK fusionará
            // las opciones por defecto establecidas anteriormente y estas opciones al unirse.
            JitsiMeetConferenceOptions options
                    = new JitsiMeetConferenceOptions.Builder()
                    .setRoom(text)
                    // Configuraciones de audio y video
                    //.setAudioMuted(true)
                    //.setVideoMuted(true)
                    .build();
            // Inicia la nueva actividad con las opciones dadas. El método launch() se encarga
            // de crear el Intent requerido y pasar las opciones.
            JitsiMeetActivity.launch(this, options);
        }
    }

    // El método registerForBroadcastMessages() se utiliza para registrar el receptor de difusión 
    // (broadcastReceiver) para recibir eventos de difusión específicos enviados por el SDK de Jitsi Meet. 
    // Se crea un IntentFilter y se agregan todas las acciones posibles de los eventos de difusión. 
    // Luego, se utiliza LocalBroadcastManager para registrar el receptor de difusión con el IntentFilter.
    private void registerForBroadcastMessages() {
        IntentFilter intentFilter = new IntentFilter();

        /* Esto se registra para cada evento posible enviado desde JitsiMeetSDK
           Si solo se necesitan algunos de los eventos, el bucle for se puede reemplazar
           por declaraciones individuales:
           Ejemplo:  intentFilter.addAction(BroadcastEvent.Type.AUDIO_MUTED_CHANGED.getAction());
                     intentFilter.addAction(BroadcastEvent.Type.CONFERENCE_TERMINATED.getAction());
                     ... otros eventos
         */
        for (BroadcastEvent.Type type : BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.getAction());
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    // El método onBroadcastReceived() se llama cuando se recibe un evento de difusión. 
    // Se crea un objeto BroadcastEvent a partir del Intent recibido y se utiliza para determinar 
    // el tipo de evento. Dependiendo del tipo de evento, se ejecuta una acción específica. 
    // En el ejemplo, se muestra un registro utilizando la biblioteca Timber.
    private void onBroadcastReceived(Intent intent) {
        if (intent != null) {
            BroadcastEvent event = new BroadcastEvent(intent);

            switch (event.getType()) {
                case CONFERENCE_JOINED:
                    Timber.i("Conferencia unida con URL%s", event.getData().get("url"));
                    break;
                case PARTICIPANT_JOINED:
                    Timber.i("Participante unido%s", event.getData().get("name"));
                    break;
            }
        }
    }

    // El método hangUp() se utiliza para enviar una acción de finalización de llamada al SDK de Jitsi Meet.
    //  Se crea un Intent utilizando BroadcastIntentHelper y se envía una difusión utilizando 
    //  LocalBroadcastManager.
    private void hangUp() {
        Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hangupBroadcastIntent);
    }
}
