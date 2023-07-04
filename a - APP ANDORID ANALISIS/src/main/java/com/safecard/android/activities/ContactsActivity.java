package com.safecard.android.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.safecard.android.Config;
import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.apicallers.ApiCallback;
import com.safecard.android.apicallers.CountriesApiCaller;
import com.safecard.android.fragments.PickContactsFragment;
import com.safecard.android.utils.Contact;
import com.safecard.android.utils.Invitation;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ContactsActivity extends BaseActivity implements PickContactsFragment.OnContactsInteractionListener  {
    private static final String TAG = "ContactsActivity";

    public static String SHARED_PREFERENCES_KEY_PLATES_MAP = "SHARED_PREFERENCES_KEY_PLATES_MAP";

    private static final int GET_CUSTOMIZED_INVITATION = 100;
    Button choiceDoneButton;
    ArrayList<Contact> selectedContacts;

    ProgressDialog pDialog;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.safeDismissDialog(this, pDialog);
        pDialog = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts);
        setChildActivityCode(Consts.ContactsActivity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(R.string.contacts_title);

        pDialog = new ProgressDialog(this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("");
        pDialog.setCancelable(true);

        choiceDoneButton = findViewById(R.id.choice_done_button);
        choiceDoneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if(!isInvitationPermitted()){
                    return;
                }

                int count = 0;
                if(selectedContacts != null) {
                    count = selectedContacts.size();
                }

                if(count == 0) {
                    Utils.showToast(getApplicationContext(), getString(R.string.contacts_pick_one_contact_at_least));
                    return;
                }

                ArrayList<InvitationCustomizationActivity.Property> properties =
                        InvitationCustomizationActivity.getUserProperties(getApplicationContext());
                properties = InvitationCustomizationActivity.filterActiveAndAvailableForInvitations(properties);

                Invitation invitation = new Invitation();
                int defaultPropertyId = getDefaultPropertyId();
                if(properties.size() > 0 &&
                        (defaultPropertyId < 0 || !InvitationCustomizationActivity.containsProperty(properties, defaultPropertyId))){
                    defaultPropertyId = properties.get(0).getId();
                }

                invitation.setPropertyId(defaultPropertyId);
                InvitationCustomizationActivity.selectAllSectors(invitation, properties);

                if(count == 1){
                    Map<String, String> platesMap = Utils.getDefaultHashmapStringToString(ContactsActivity.SHARED_PREFERENCES_KEY_PLATES_MAP, getApplicationContext());
                    String plate = platesMap.get(selectedContacts.get(0).getMobile());
                    if(plate != null && !plate.equals("")) {
                        invitation.setPlateNumberUsed(false);
                        invitation.setPlateNumber(plate);
                    }
                }

                Intent intent = new Intent(getApplicationContext(), InvitationCustomizationActivity.class);
                intent.putExtra(InvitationCustomizationActivity.TITLE, getString(R.string.contacts_invitation_customization_title));
                intent.putExtra(InvitationCustomizationActivity.NAMES, (ArrayList) getNameListFromContacts(selectedContacts));
                intent.putExtra(InvitationCustomizationActivity.PROPERTIES, properties);
                intent.putExtra(InvitationCustomizationActivity.INVITATION, invitation);
                startActivityForResult(intent, GET_CUSTOMIZED_INVITATION);

            }
        });


        FragmentManager mFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        //fragTransaction.remove(fragment);
        fragmentTransaction.replace(R.id.contacts_list_container,  new PickContactsFragment()).commit();
        //fragmentTransaction.executePendingTransactions();
    }

    private boolean isInvitationPermitted() {

        boolean thereIsIssue = false;
        String message = "";

        try {
            JSONObject userJson = new JSONObject(Utils.getDefaults("user", getApplicationContext()));
            JSONArray properties = new JSONArray(userJson.getString("properties"));
            if (properties.length() < 0) {
                thereIsIssue = true;
                message = getString(R.string.contacts_you_have_no_property);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<InvitationCustomizationActivity.Property> properties =
                InvitationCustomizationActivity.getUserProperties(getApplicationContext());
        properties = InvitationCustomizationActivity.filterActiveAndAvailableForInvitations(properties);

        if (!thereIsIssue && properties.size() < 1){
            thereIsIssue = true;
            message = getString(R.string.contacts_you_have_no_available_property);
        }

        if (!thereIsIssue){
            return true;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.contacts_invitation_dialog_title);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.contacts_invitation_dialog_ok_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

        return false;
    }

    @Override
    public void onSelectedContactsChanged(ArrayList<Contact> selectedContacts) {
        this.selectedContacts = selectedContacts;
        String text = String.format(Locale.getDefault(),
                getString(R.string.contacts_done_button), this.selectedContacts.size());
        choiceDoneButton.setText(text);
    }

    private int getDefaultPropertyId(){
        return Utils.getDefaultInt(Consts.ACCESS_SELECTED_ID, getApplicationContext());
    }

    public List<String> getNameListFromContacts(List<Contact> contacts) {
        List<String> names = new ArrayList<>();
        for (Contact c : contacts) {
            names.add(c.getName() + " (" + c.getMobile() + ")");
        }
        return names;
    }

    public List<String> getMobileListFromContacts(List<Contact> contacts) {
        List<String> names = new ArrayList<>();
        for (Contact c : contacts) {
            names.add(c.getMobile());
        }
        return names;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Utils.getDefaultBoolean(Consts.SHOW_UPDATE, getApplicationContext())) {
            Utils.updateApp(ContactsActivity.this);
        }
        Utils.appBlockChecks(this);

        // Llamada para actualizar lista de paises
        new CountriesApiCaller(getApplicationContext()).doCall(new ApiCallback() {
            @Override
            public void callSuccess(JSONObject response) {
            }

            @Override
            public void callError(String errorType, String msg) {
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //TODO AGREGAR la visibilidad al boton invitar, mejorar la peticion de el permiso
    }

    /*public void onContactSelected(ArrayList<Contact> contacts){
        contact_selected = contacts;
    }*/

   /* @Override
    public boolean onSearchRequested() {
        // Don't allow another search if this activity instance is already showing
        // search results. Only used pre-HC.
        return !isSearchResultView && super.onSearchRequested();
    }*/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_CUSTOMIZED_INVITATION) {
            if (resultCode == RESULT_OK) {
                Serializable inv = data.getSerializableExtra(InvitationCustomizationActivity.INVITATION);
                if(inv instanceof Invitation){
                    sendInvitation((Invitation) inv);
                }
            }
        }
    }

    private void sendInvitation(Invitation invitation){

        final List<String> guestMobiles = getMobileListFromContacts(selectedContacts);
        final List<String> guestNames = getNameListFromContacts(selectedContacts);

        ArrayList<InvitationCustomizationActivity.Property> properties
                = InvitationCustomizationActivity.getUserProperties(getApplicationContext());
        InvitationCustomizationActivity.Property property
                = InvitationCustomizationActivity.getProperty(properties, invitation.getPropertyId());

        String startPattern = "yyyy-MM-dd'T'00:00:00";
        String endPattern = "yyyy-MM-dd'T'23:59:59";
        if(invitation.isCustomTime()){
            startPattern = "yyyy-MM-dd'T'HH:mm:00";
            endPattern = "yyyy-MM-dd'T'HH:mm:59";
        }
        String startDate = new SimpleDateFormat(startPattern)
                .format(invitation.getStartDateTimeCalendar().getTime());
        String endDate = new SimpleDateFormat(endPattern)
                .format(invitation.getEndDateTimeCalendar().getTime());

        String subject = "_";
        if (!invitation.getName().equals("")) {
            subject = Utils.encodeForURL(invitation.getName());
        }

        String repeat = "_";
        String days = "_";
        if (invitation.isCustomDaysOfWeek()) {
            repeat = "1";
            days = invitation.getDaysOfWeekValidsAsString();
        }

        String sectors = "_";
        if(invitation.getIdsSelectedSectors().size() > 0){
            sectors = invitation.getIdsSelectedSectorsAsString();
        }

        String plate = "_";
        if(invitation.isPlateNumberUsed()){
            plate = invitation.getPlateNumber();
        }

        //sent_invitations/:mobile_organizer/:guestMobiles/:guestNames/:condo_id/:house_id/:relation_id/:subject/:days/:repeat/:start_date/:end_date
        //sent_invitations/:mobile_organizer/:guestMobiles/:guestNames/:condo_id/:house_id/:start_date/:end_date/:relation_id/:subject?/:repeat?/:days?/:allowed_sectors?

        String url = Config.ApiUrl + "send_invitations/" +
                Utils.getMobile(getApplicationContext()) + "/" +
                Utils.stringListToStringCommaSeparated(guestMobiles) + "/" +
                Utils.encodeForURL(Utils.stringListToStringCommaSeparated(guestNames)) + "/" +
                property.getCondoId() + "/" +
                property.getId() + "/" +
                startDate + "/" +
                endDate + "/" +
                "3" + "/" +
                subject + "/" +
                repeat + "/" +
                days + "/" +
                sectors + "/" +
                plate;

        pDialog.setMessage(getString(R.string.contacts_dialog_sending_invitation));
        pDialog.show();

        final boolean wasPlateUsed = !plate.equals("_");
        final String finalPlate = plate;


        RequestVolley rqv = new RequestVolley(url, getApplicationContext());
        rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
            @Override
            public void onSuccess(final JSONObject response) {
                if (pDialog != null && pDialog.isShowing()) {
                    Utils.safeDismissDialog(ContactsActivity.this, pDialog);
                }
                try {
                    if (response.getString("result").equals("ACK")) {
                        pDialog.setMessage(getString(R.string.contacts_wait_a_moment));
                        pDialog.show();
                        JSONObject props = new JSONObject();

                        props.put("plate_used", wasPlateUsed);
                        Utils.mixpanel.track("INVITATION_SENT", props);

                        final String event_id = response.getString("event_id");

                        JSONArray invitations = new JSONArray(response.getString("invitations"));
                        boolean registered = true;
                        for(int i=0; i < invitations.length(); i++){
                            JSONObject obj = invitations.getJSONObject(i);
                           if(obj.getInt("registered") == 0){
                               registered = false;
                               break;
                           }
                        }
                        final boolean registeredFinal = registered;

                        String url = Config.ApiUrl + "get_event_url/" + Utils.getMobile(getApplicationContext()) + "/"+event_id;

                        RequestVolley rqv2 = new RequestVolley(url, getApplicationContext());
                        rqv2.requestApi(new RequestVolley.VolleyJsonCallback() {
                            @Override
                            public void onSuccess(JSONObject response) {
                                if (pDialog != null && pDialog.isShowing()) {
                                    Utils.safeDismissDialog(ContactsActivity.this, pDialog);
                                }
                                try {
                                    if(response.getString("result").equals("ACK")){
                                        Intent share_invitation = new Intent(getApplicationContext(), ShareInvitationActivity.class);
                                        share_invitation.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                        if(response.has("url") &&
                                                !("".equals(response.getString("url")))){
                                            share_invitation.putExtra("URLWSP", response.getString("url"));
                                        } else {
                                            share_invitation.putExtra("URLWSP", response.getString("long_url"));
                                        }

                                        share_invitation.putExtra("REGISTERED", registeredFinal);
                                        share_invitation.putExtra("RESEND", false);
                                        share_invitation.putExtra("EVENT_ID", event_id);
                                        startActivity(share_invitation);

                                        if(wasPlateUsed && guestMobiles.size() == 1){
                                            Utils.updateDefaultHashmap(
                                                    ContactsActivity.SHARED_PREFERENCES_KEY_PLATES_MAP,
                                                    guestMobiles.get(0),
                                                    finalPlate,
                                                    getApplicationContext());
                                        }

                                    } else {
                                        Utils.showToast(getApplicationContext(), response.getString("msg"));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(String errorType, String msg) {
                                if (pDialog != null && pDialog.isShowing()) {
                                    Utils.safeDismissDialog(ContactsActivity.this, pDialog);
                                }
                            }
                        });
                    } else {
                        Utils.showToast(getApplicationContext(), response.getString("msg"));
                    }
                } catch (JSONException e) {
                    if (pDialog != null && pDialog.isShowing()) {
                        Utils.safeDismissDialog(ContactsActivity.this, pDialog);
                    }
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorType, String msg) {
                if(!msg.equals("")){
                    Utils.showToast(getApplicationContext(), msg);
                }

                if (pDialog != null && pDialog.isShowing()) {
                    Utils.safeDismissDialog(ContactsActivity.this, pDialog);
                }
            }
        });

    }

}

