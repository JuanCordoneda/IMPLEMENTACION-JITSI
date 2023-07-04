package com.safecard.android.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.safecard.android.BuildConfig;
import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.model.Models;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdvancedConfigurationActivity extends AppCompatActivity {
    private static final String TAG = "AdvancedConfigActivity";

    public static int PICK_SECTOR_FOR_DEFAULT = 1;
    public static int PICK_PROPERTY_FOR_PLATES = 2;
    public static int PICK_PROPERTY_FOR_DEFAULT= 3;
    public static int PICK_PROPERTY_FOR_GEOLOCATION = 4;
    public static int PICK_PROPERTY_FOR_DEFAULTS_ACCESS_TYPE = 5;
    public static int PICK_PROPERTY_FOR_SECTOR_DEFAULTS = 6;

    AlertDialog.Builder builder;

    ArrayList<InvitationCustomizationActivity.Property> properties;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_configuration);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_shape);

        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.activity_advanced_configuration_title));

        LinearLayout set_default_access = (LinearLayout) findViewById(R.id.set_default_access);
        LinearLayout btn_access_type = (LinearLayout) findViewById(R.id.btn_access_type);
        LinearLayout btnSector = (LinearLayout) findViewById(R.id.btn_sector);
        LinearLayout btnGeoHouse = (LinearLayout) findViewById(R.id.btn_geo_house);
        LinearLayout btn_plate = (LinearLayout) findViewById(R.id.btn_plate);

        builder = new AlertDialog.Builder(this);

        properties = InvitationCustomizationActivity.getUserPropertiesWithAllSectors(getApplicationContext());

        set_default_access.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasNoPropertiesCheckAndMsg()){
                    return;
                }

                int propertyId = Utils.getDefaultInt(Consts.ACCESS_SELECTED_ID, getApplicationContext());
                ArrayList<Integer> tickedPropertyIds = new ArrayList<>();
                tickedPropertyIds.add(propertyId);

                Intent intent = new Intent(getApplicationContext(), PickPropertiesActivity.class);
                intent.putExtra(PickPropertiesActivity.ONE_ITEM_IMMEDIATE_RESPONSE, false);
                intent.putExtra(PickPropertiesActivity.PROPERTIES, properties);
                intent.putExtra(PickPropertiesActivity.TICKED_PROPERTY_IDS, tickedPropertyIds);
                startActivityForResult(intent, PICK_PROPERTY_FOR_DEFAULT);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        btnSector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasNoPropertiesCheckAndMsg()){
                    return;
                }

                Intent intent = new Intent(getApplicationContext(), PickPropertiesActivity.class);
                intent.putExtra(PickPropertiesActivity.ONE_ITEM_IMMEDIATE_RESPONSE, true);
                intent.putExtra(PickPropertiesActivity.PROPERTIES, properties);
                startActivityForResult(intent, PICK_PROPERTY_FOR_SECTOR_DEFAULTS);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        btn_access_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasNoPropertiesCheckAndMsg()){
                    return;
                }

                Intent intent = new Intent(getApplicationContext(), PickPropertiesActivity.class);
                intent.putExtra(PickPropertiesActivity.ONE_ITEM_IMMEDIATE_RESPONSE, true);
                intent.putExtra(PickPropertiesActivity.PROPERTIES, properties);
                startActivityForResult(intent, PICK_PROPERTY_FOR_DEFAULTS_ACCESS_TYPE);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });

        if(!BuildConfig.SERVICE_USED.equals("GOOGLE")){
            btnGeoHouse.setVisibility(View.GONE);
        }

        btnGeoHouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasNoPropertiesCheckAndMsg()){
                    return;
                }

                if(!BuildConfig.SERVICE_USED.equals("GOOGLE")){
                    String msg = getString(R.string.activity_advanced_configuration_no_google);
                    Utils.showToast(getApplicationContext(), msg);
                    return;
                }

                ArrayList<Integer> alreadyGeolocatedPropertyIds = getAlreadyGeolocatedPropertyIds();

                PickPropertiesActivity.Warning noPermissionWarning =  new PickPropertiesActivity.Warning();
                noPermissionWarning.setMessage(getString(R.string.activity_advanced_configuration_no_privileges));
                noPermissionWarning.setPropertyIds(getNoPermissionPropertyIds());
                noPermissionWarning.setCanPropertyBeSelected(false);

                PickPropertiesActivity.Warning condoNoGeolocatedWarning =  new PickPropertiesActivity.Warning();
                condoNoGeolocatedWarning.setMessage(getString(R.string.activity_advanced_configuration_condo_no_geolocated));
                condoNoGeolocatedWarning.setPropertyIds(getCondoNoGeolocatedPropertyIds());
                condoNoGeolocatedWarning.setCanPropertyBeSelected(false);

                PickPropertiesActivity.Warning alreadyGeolocatedWarning =  new PickPropertiesActivity.Warning();
                alreadyGeolocatedWarning.setMessage(getString(R.string.activity_advanced_configuration_already_geolocated));
                alreadyGeolocatedWarning.setPropertyIds(alreadyGeolocatedPropertyIds);
                alreadyGeolocatedWarning.setCanPropertyBeSelected(true);

                ArrayList<PickPropertiesActivity.Warning> warnings = new ArrayList<>();
                warnings.add(noPermissionWarning);
                warnings.add(condoNoGeolocatedWarning);
                warnings.add(alreadyGeolocatedWarning);

                ArrayList<Integer> iconWarningPropertyIds = new ArrayList<>();
                iconWarningPropertyIds.addAll(noPermissionWarning.getPropertyIds());
                iconWarningPropertyIds.addAll(condoNoGeolocatedWarning.getPropertyIds());

                ArrayList<Integer> arrowedPropertyIds = PickPropertiesActivity.getIdList(properties);
                arrowedPropertyIds.removeAll(iconWarningPropertyIds);

                Intent intent = new Intent(getApplicationContext(), PickPropertiesActivity.class);
                intent.putExtra(PickPropertiesActivity.ONE_ITEM_IMMEDIATE_RESPONSE, true);
                intent.putExtra(PickPropertiesActivity.PROPERTIES, properties);
                intent.putExtra(PickPropertiesActivity.WARNINGS, warnings);
                intent.putExtra(PickPropertiesActivity.ICON_WARNING_PROPERTY_IDS, iconWarningPropertyIds);
                intent.putExtra(PickPropertiesActivity.ARROWED_PROPERTY_IDS, arrowedPropertyIds);
                intent.putExtra(PickPropertiesActivity.GEOLOCATED_PROPERTY_IDS, alreadyGeolocatedPropertyIds);
                //Log.i(TAG, "properties.count:" +  properties.size());
                startActivityForResult(intent, PICK_PROPERTY_FOR_GEOLOCATION);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        btn_plate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasNoPropertiesCheckAndMsg()){
                    return;
                }

                ArrayList<InvitationCustomizationActivity.Property> noPlateServiceAvailableProperties
                        = InvitationCustomizationActivity.filterNoPlateServiceAvailableForResidents(properties);

                ArrayList<InvitationCustomizationActivity.Property> noActiveProperties
                        = InvitationCustomizationActivity.filterNoActive(properties);

                ArrayList<InvitationCustomizationActivity.Property> activeAndPlateServiceAvailableProperties
                        = InvitationCustomizationActivity.filterActiveAndPlateServiceAvailableForResidents(properties);

                PickPropertiesActivity.Warning noPlateServiceAvailableWarning =  new PickPropertiesActivity.Warning();
                noPlateServiceAvailableWarning.setMessage(getString(R.string.activity_advanced_configuration_plate_service_not_available));
                noPlateServiceAvailableWarning.setPropertyIds(PickPropertiesActivity.getIdList(noPlateServiceAvailableProperties));
                noPlateServiceAvailableWarning.setCanPropertyBeSelected(false);


                PickPropertiesActivity.Warning noActiveWarning =  new PickPropertiesActivity.Warning();
                noActiveWarning.setMessage(getString(R.string.activity_advanced_configuration_property_not_active));
                noActiveWarning.setPropertyIds(PickPropertiesActivity.getIdList(noActiveProperties));
                noActiveWarning.setCanPropertyBeSelected(false);

                ArrayList<PickPropertiesActivity.Warning> warnings = new ArrayList<>();
                warnings.add(noPlateServiceAvailableWarning);
                warnings.add(noActiveWarning);

                ArrayList<Integer> iconWarningPropertyIds = new ArrayList<>();
                iconWarningPropertyIds.addAll(noPlateServiceAvailableWarning.getPropertyIds());
                iconWarningPropertyIds.addAll(noActiveWarning.getPropertyIds());

                Intent intent = new Intent(getApplicationContext(), PickPropertiesActivity.class);
                intent.putExtra(PickPropertiesActivity.ONE_ITEM_IMMEDIATE_RESPONSE, true);
                intent.putExtra(PickPropertiesActivity.PROPERTIES, properties);
                intent.putExtra(PickPropertiesActivity.WARNINGS, warnings);
                intent.putExtra(PickPropertiesActivity.ICON_WARNING_PROPERTY_IDS, iconWarningPropertyIds);
                intent.putExtra(PickPropertiesActivity.ARROWED_PROPERTY_IDS, PickPropertiesActivity.getIdList(activeAndPlateServiceAvailableProperties));
                //Log.i(TAG, "properties.count:" +  properties.size());
                startActivityForResult(intent, PICK_PROPERTY_FOR_PLATES);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });
    }

    private boolean hasNoPropertiesCheckAndMsg() {
        if(properties.size() == 0){
            Utils.showToast(getApplicationContext(), getString(R.string.activity_advanced_configuration_you_dont_have_properties));
            return true;
        }
        return false;
    }

    private ArrayList<Integer> getNoPermissionPropertyIds() {
        ArrayList<Integer> result = new ArrayList<>();

        try {
            JSONObject userJson = Utils.getDefaultJSONObject("user", getApplicationContext());
            JSONArray propertiesJson = userJson.getJSONArray("properties");
            for (int i = 0; i < propertiesJson.length(); i++) {
                JSONObject propertyJson = propertiesJson.getJSONObject(i);
                if (propertyJson.getInt("owner") == 0 && propertyJson.getInt("admin") == 0) {
                    result.add(propertyJson.getInt("house_id"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private ArrayList<Integer> getCondoNoGeolocatedPropertyIds() {
        ArrayList<Integer> result = new ArrayList<>();

        try {
            JSONObject userJson = Utils.getDefaultJSONObject("user", getApplicationContext());
            JSONArray propertiesJson = userJson.getJSONArray("properties");
            for (int i = 0; i < propertiesJson.length(); i++) {
                JSONObject propertyJson = propertiesJson.getJSONObject(i);
                if (propertyJson.getString("lat").equals("") || propertyJson.getString("lng").equals("")) {
                    result.add(propertyJson.getInt("house_id"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private ArrayList<Integer> getAlreadyGeolocatedPropertyIds() {
        ArrayList<Integer> result = new ArrayList<>();

        try {
            JSONObject userJson = Utils.getDefaultJSONObject("user", getApplicationContext());
            JSONArray propertiesJson = userJson.getJSONArray("properties");
            for (int i = 0; i < propertiesJson.length(); i++) {
                JSONObject propertyJson = propertiesJson.getJSONObject(i);
                if(!propertyJson.getString("house_lat").equals("_") && !propertyJson.getString("house_lng").equals("_")){
                    result.add(propertyJson.getInt("house_id"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.i(TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);

        if(data == null){
            return;
        }

        if (requestCode == PICK_PROPERTY_FOR_DEFAULT) {
            if (resultCode == AdvancedConfigurationActivity.RESULT_OK) {
                int propertyId = data.getIntExtra(PickPropertiesActivity.SELECTED_PROPERTY_ID, -1);
                if (propertyId <= 0) {
                    return;
                }
                Utils.setDefaultInt(Consts.ACCESS_SELECTED_ID, propertyId, getApplicationContext());
                Models.getAccessModel(getApplicationContext()).loadDefaultAccessFromPersisted(getApplicationContext());
                setPropertyDefaultCorrectOrEmpty();
                updatePropertyDefaultView();
            }
        } else if (requestCode == PICK_PROPERTY_FOR_SECTOR_DEFAULTS) {
            if (resultCode == AdvancedConfigurationActivity.RESULT_OK) {
                int propertyId = data.getIntExtra(PickPropertiesActivity.SELECTED_PROPERTY_ID, -1);
                if (propertyId <= 0) {
                    return;
                }

                ArrayList<Integer> selectedSectorIds =  new ArrayList<>();
                try {
                    JSONObject defaultSectors = Utils.getDefaultJSONObject("defaultSector", getApplicationContext());
                    int defaultSectorId = defaultSectors.getInt(String.valueOf(propertyId));
                    selectedSectorIds.add(defaultSectorId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                InvitationCustomizationActivity.Property property = InvitationCustomizationActivity.getProperty(properties, propertyId);

                Intent intent = new Intent(getApplicationContext(), PickSectorsActivity.class);
                intent.putExtra(PickSectorsActivity.ARG_CARDINALITY, PickSectorsActivity.CARDINALITY_ONE);
                intent.putExtra(PickSectorsActivity.PROPERTY_ID, property.getId());
                intent.putExtra(PickSectorsActivity.SELECTED_SECTORS_ID, selectedSectorIds);
                intent.putExtra(PickSectorsActivity.TITLE, property.getName() + " - " + property.getCondoName());
                intent.putExtra(PickSectorsActivity.ARG_SECTORS, (ArrayList) property.getSectors());
                startActivityForResult(intent, PICK_SECTOR_FOR_DEFAULT);

            }
        }else if (requestCode == PICK_PROPERTY_FOR_DEFAULTS_ACCESS_TYPE) {
            if (resultCode == AdvancedConfigurationActivity.RESULT_OK) {
                int propertyId = data.getIntExtra(PickPropertiesActivity.SELECTED_PROPERTY_ID, -1);
                if (propertyId <= 0) {
                    return;
                }

                Intent intent = new Intent(getApplicationContext(), DefaultsAccessTypeActivity.class);
                intent.putExtra(DefaultsAccessTypeActivity.PROPERTY_ID, propertyId);
                startActivity(intent);
            }
        } else if (requestCode == PICK_PROPERTY_FOR_GEOLOCATION) {
            if (resultCode == AdvancedConfigurationActivity.RESULT_OK) {
                int propertyId = data.getIntExtra(PickPropertiesActivity.SELECTED_PROPERTY_ID, -1);
                if (propertyId <= 0) {
                    return;
                }

                Intent intent = new Intent(getApplicationContext(), GeoHousesActivity.class);
                intent.putExtra(GeoHousesActivity.PROPERTY_ID, propertyId);
                startActivity(intent);
            }
        } else if (requestCode == PICK_PROPERTY_FOR_PLATES) {
            if (resultCode == AdvancedConfigurationActivity.RESULT_OK) {
                int propertyId = data.getIntExtra(PickPropertiesActivity.SELECTED_PROPERTY_ID, -1);
                if (propertyId <= 0) {
                    return;
                }

                InvitationCustomizationActivity.Property property = InvitationCustomizationActivity.getProperty(properties, propertyId);

                Intent intent = new Intent(getApplicationContext(), ListPlateActivity.class);
                intent.putExtra(ListPlateActivity.PROPERTY_ID, property.getId());
                intent.putExtra(ListPlateActivity.PROPERTY_NAME, property.getName());
                intent.putExtra(ListPlateActivity.PROPERTY_ACTIVE, property.isActive());
                intent.putExtra(ListPlateActivity.CONDO_NAME, property.getCondoName());
                startActivity(intent);
            }
        } else if (requestCode == PICK_SECTOR_FOR_DEFAULT) {
            if (resultCode == RESULT_OK) {
                int propertyId = data.getIntExtra(PickSectorsActivity.PROPERTY_ID, -1);
                ArrayList<Integer> SectorIds = data.getExtras().getIntegerArrayList(PickSectorsActivity.SELECTED_SECTORS_ID);
                if(SectorIds == null || SectorIds.size() != 1 || propertyId < 0){
                    return;
                }
                int sectorIdSelected = SectorIds.get(0);
                try {
                    JSONObject defaultSectors = Utils.getDefaultJSONObject("defaultSector", getApplicationContext());
                    defaultSectors.put(String.valueOf(propertyId), sectorIdSelected);
                    Utils.setDefaultJSONObject("defaultSector", defaultSectors, getApplicationContext());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setPropertyDefaultCorrectOrEmpty();
        updatePropertyDefaultView();
    }
    public void setPropertyDefaultCorrectOrEmpty() { // Empty is -1
        int propertyId = Utils.getDefaultInt(Consts.ACCESS_SELECTED_ID, getApplicationContext());
        if(propertyId > 0) {
            InvitationCustomizationActivity.Property property = InvitationCustomizationActivity.getProperty(properties, propertyId);
            if(property.getId() > 0) {
                return;
            }else{
                Utils.setDefaultInt(Consts.ACCESS_SELECTED_ID, -1, getApplicationContext());
            }
        }

        if (properties.size() > 0) {
            Utils.setDefaultInt(Consts.ACCESS_SELECTED_ID, properties.get(0).getId(), getApplicationContext());
        }
    }

    public void updatePropertyDefaultView(){
        int propertyId = Utils.getDefaultInt(Consts.ACCESS_SELECTED_ID, getApplicationContext());
        TextView default_access_text = (TextView) findViewById(R.id.default_access_text);
        if(propertyId > 0) {
            InvitationCustomizationActivity.Property property = InvitationCustomizationActivity.getProperty(properties, propertyId);
            String label = String.format("%s - %s", property.getName(), property.getCondoName());
            default_access_text.setText(label);
        }else{
            default_access_text.setText(R.string.activity_advanced_configuration_no_properties);
        }
    }
}
