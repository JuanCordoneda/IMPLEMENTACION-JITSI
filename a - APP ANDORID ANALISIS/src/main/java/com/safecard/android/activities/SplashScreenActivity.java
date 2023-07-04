package com.safecard.android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.model.Models;
import com.safecard.android.utils.StartAppManager;

import org.json.JSONException;
import org.json.JSONObject;
// es una actividad de inicio que muestra una pantalla de presentación al iniciar la aplicación. 
// Verifica si se pasaron ciertos extras en el Intent, inicia una actividad específica en función de 
// esos extras o pasa al siguiente paso para iniciar la actividad de inicio de sesión.

// Se define la clase SplashScreenActivity como pública y se extiende de la clase base AppCompatActivity, 
// que proporciona funcionalidades adicionales para actividades en versiones anteriores de Android. 
// Además, se define una constante de cadena llamada TAG para su uso en el registro de mensajes.
public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = "SplashScreenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Se anula el método onCreate() de la clase base y se establece el contenido 
        // de la actividad utilizando el diseño XML correspondiente.
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // Se verifica si la actividad actual es la raíz de la tarea (la primera actividad en la pila). 
        // Si no es así, se finaliza la actividad y se retorna.
        if (!isTaskRoot()) {
            Log.i(TAG, "onCreate finish");
            finish();
            return;
        }

        // Se obtiene el Bundle de extras del Intent que inició esta actividad. 
        // Si el Bundle no es nulo y contiene la clave correspondiente al ID de la tarjeta de conferencia 
        // (PARAM_CONFERENCE_SAFECARD_ID), se crea un nuevo Intent para iniciar la actividad IncomingCallActivity
        //  y se inicia esa actividad con los extras del Bundle. Luego, se finaliza la actividad actual.
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            if (bundle.containsKey(IncomingCallActivity.PARAM_CONFERENCE_SAFECARD_ID)){
                Intent intent = new Intent(this, IncomingCallActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
                return;
            }
        }

        // Si no se cumple la condición anterior, se crea una instancia de la clase StartAppManager y 
        // se llama al método process() pasando la referencia de la actividad actual y un objeto 
        // StartAppManagerCallback. Dentro del método onNoIssueFinish() del StartAppManagerCallback, 
        // se llama al método next()
        new StartAppManager().process(this, new StartAppManager.StartAppManagerCallback() {
            @Override
            public void onNoIssueFinish() {
                next();
            }
        });
    }

    // El método next() se ejecuta cuando no se cumplen las condiciones anteriores. 
    // Dentro de este método, se crea un nuevo Intent para iniciar la actividad LoginActivity. 
    // Se verifica si existen extras en el Intent actual y si el valor del extra "LOGIN_GOTO" 
    // es igual a Consts.AccessActivity. En ese caso, se agregan extras adicionales al Intent para 
    // indicar la opción de acceso y el ID de la propiedad seleccionada. Luego, se inicia la actividad 
    // LoginActivity con el Intent, se establecen animaciones de transición y se finaliza la actividad actual.
    protected void next() {
        Log.i(TAG, "next");

        Intent intent = new Intent(this, LoginActivity.class);

        Bundle extras = getIntent().getExtras();
        if (extras != null && !extras.isEmpty()
                && extras.getInt("LOGIN_GOTO", -1) == Consts.AccessActivity) {
            intent.putExtra("GOTO", Consts.AccessActivity);
            int selectedPropertyId = extras.getInt(AccessActivity.SELECT_PROPERTY, 0);
            getIntent().removeExtra(AccessActivity.SELECT_PROPERTY);
            Log.i(TAG, "SELECT_PROPERTY: " + selectedPropertyId);
            try {
                JSONObject json =  new JSONObject();
                json.put("house_id", selectedPropertyId);
                intent.putExtra(LoginActivity.EXTRA_DATA, json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            intent.putExtra("GOTO", Consts.AccessOrInvitationActivity);
        }
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
    }
    
    // Se anula el método onNewIntent() de la clase base. Este método se llama cuando un Intent 
    // nuevo se entrega a la actividad mientras está en primer plano. En este caso, simplemente se 
    // registra un mensaje de registro indicando que se ha llamado al método.
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG,"onNewIntent");
    }
}