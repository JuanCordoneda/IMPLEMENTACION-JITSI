package com.safecard.android.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.safecard.android.R;
import com.safecard.android.utils.CallUtils;
import com.safecard.android.utils.Utils;

// la clase IncomingCallActivity se utiliza para mostrar una llamada entrante en la aplicación y proporciona botones para aceptar o rechazar la llamada. Dependiendo de la acción del usuario, se inicia una llamada utilizando la clase CallUtils o se finaliza la actividad.
public class IncomingCallActivity extends AppCompatActivity {

    // Se definen constantes públicas para los nombres de los parámetros que se 
    // pasarán a la actividad a través del Intent.
    public static final String PARAM_CONFERENCE_SAFECARD_ID = "PARAM_CONFERENCE_SAFECARD_ID";
    public static final String PARAM_CONFERENCE_DESCRIPTION = "PARAM_CONFERENCE_DESCRIPTION";
    public static final String PARAM_ROOM_ID = "PARAM_ROOM_ID";
    public static final String PARAM_ROOM_NAME = "PARAM_ROOM_NAME";
    public static final String PARAM_ROOM_KEY = "PARAM_ROOM_KEY";
    public static final String PARAM_ROOM_SERVER_URL = "PARAM_ROOM_SERVER_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Se anula el método onCreate() de la clase base y se establece el diseño de la actividad utilizando el archivo de diseño XML activity_incoming_call.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        // Se obtiene el Bundle de extras del Intent que inició esta actividad. Si el Bundle es nulo, se lanza una excepción RuntimeException.
        Bundle bundle = getIntent().getExtras();
        if(bundle == null){
            throw new RuntimeException("There is no bundle");
        }

        // Se obtienen los valores de los parámetros pasados a la actividad a través del Bundle.
        final int conferenceSafecardId = bundle.getInt(PARAM_CONFERENCE_SAFECARD_ID);
        final String conferenceDescription = bundle.getString(PARAM_CONFERENCE_DESCRIPTION);
        final String roomId = bundle.getString(PARAM_ROOM_ID);
        final String roomName = bundle.getString(PARAM_ROOM_NAME);
        final String roomKey = bundle.getString(PARAM_ROOM_KEY);
        final String roomServerURL = bundle.getString(PARAM_ROOM_SERVER_URL);

        // Se encuentra la vista TextView en el diseño de la actividad mediante su ID y se establece el texto de la descripción de la conferencia.
        TextView descriptionTextView = findViewById(R.id.description);
        descriptionTextView.setText(conferenceDescription);

        // Se encuentra el botón de aceptar llamada en el diseño de la actividad mediante su ID y 
        // se configura un OnClickListener para manejar el evento de clic. Cuando se hace clic en el botón, 
        // se obtiene el nombre de usuario utilizando la utilidad Utils, se llama al método launchCall() 
        // de la clase CallUtils para iniciar la llamada y se finaliza la actividad actual.
        Button acceptButton = findViewById(R.id.accept_button);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userName = Utils.getUserFullName(getApplicationContext());
                CallUtils.launchCall(
                        userName,
                        roomServerURL,
                        roomId,
                        roomName,
                        roomKey,
                        IncomingCallActivity.this);

                IncomingCallActivity.this.finish();
            }
        });

        // Se encuentra el botón de rechazar llamada en el diseño de la actividad mediante su ID 
        // y se configura un OnClickListener para manejar el evento de clic. Cuando se hace clic en el botón, 
        // se finaliza la actividad actual.
        Button rejectButton = findViewById(R.id.reject_button);
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IncomingCallActivity.this.finish();
            }
        });
    }
}
