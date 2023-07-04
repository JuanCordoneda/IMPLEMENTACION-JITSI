package com.safecard.android.model.dao;

import android.content.Context;
import android.util.Log;

import com.safecard.android.Consts;
import com.safecard.android.model.AccessModel;
import com.safecard.android.model.dataobjects.PropertyData;
import com.safecard.android.model.dataobjects.SectorData;
import com.safecard.android.model.dataobjects.GateData;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PropertyDao {

    private List<PropertyData> properties;
    private String TAG = "PropertyDao";

    public PropertyDao(Context context){
        properties = new ArrayList<>();
        loadFromPersisted(context);
    }

    public void setDummyProperty() {
        PropertyData p = new PropertyData();
        p.setHouseName("Propiedad");
        p.setCondoName("Condominio");
        p.setHouseId(-1);
        p.setActive(true);
        p.getSectors().add(new SectorData());
        p.getAllowedSectors().add(new SectorData());
        properties.add(p);
    }

    public List<PropertyData> getProperties(){
        return properties;
    }

    public int propertiesSize() {
        return properties.size();
    }

    public boolean existProperty(int propertyId) {
        return getProperty(propertyId) != null;
    }

    public PropertyData getProperty(int propertyId) {
        PropertyData propertyData = null;
        for(int i = 0; i < properties.size(); i++){
            PropertyData aux = properties.get(i);
            if(aux.getHouseId() == propertyId){
                propertyData = aux;
                break;
            }
        }
        return propertyData;
    }

    public void loadFromPersisted(Context context){
        loadPropertiesFromPersisted(context);
        loadDefaultSectorsFromPersisted(context);
        loadDefaultControlTypeFromPersisted(context);
    }

    private void loadPropertiesFromPersisted(Context context){
        try {
            List<PropertyData>  props = new ArrayList<>();
            JSONObject initData = Utils.getDefaultJSONObject("init_data", context);
            Log.d(TAG, "loadPropertiesFromPersisted initData:" + initData.toString());
            if(initData.has("properties")) {
                JSONArray propertiesJSONArray = new JSONArray(initData.getString("properties"));
                for (int i = 0; i < propertiesJSONArray.length(); i++) {
                    JSONObject propertyJson = propertiesJSONArray.getJSONObject(i);
                    props.add(getPropertyDataFromJson(propertyJson));
                }
            }
            if(props.size() > 0){
                this.properties = props;
            }
            Log.d(TAG, "loadPropertiesFromPersisted properties count:" + props.size());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadDefaultSectorsFromPersisted(Context context){
        try {
            JSONObject defaultSectorsJSON = Utils.getDefaultJSONObject("defaultSector", context);
            for(int i = 0; i < properties.size(); i++){
                PropertyData p = properties.get(i);
                if(defaultSectorsJSON.has(""+p.getHouseId())){
                    int defaultSectorId = defaultSectorsJSON.getInt(""+p.getHouseId());
                    p.setSectorDefault(AccessModel.getAllowedSectorById(p, defaultSectorId));
                }else if(p.getAllowedSectors().size() > 0){
                    p.setSectorDefault(p.getAllowedSectors().get(0));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadDefaultControlTypeFromPersisted(Context context){
        try {
            JSONObject json = Utils.getDefaultJSONObject("defaultSectorControl", context);
            for(int i = 0; i < properties.size(); i++){
                PropertyData property = properties.get(i);
                List<SectorData> sectors = property.getAllowedSectors();
                for(int j = 0; j < sectors.size(); j++) {
                    SectorData sector = sectors.get(j);
                    sector.setDefaultControlType(Consts.CONTROLTYPE_QR);
                    if(sector.isUseRc() && sector.getGates().size() > 0) {
                        sector.setDefaultControlType(Consts.CONTROLTYPE_RC);
                        String key = property.getHouseId() + "-" + sector.getSectorId();
                        if(sector.isUseQr() && json.has(key) && json.getString(key).equals("QR")) {
                            sector.setDefaultControlType(Consts.CONTROLTYPE_QR);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private PropertyData getPropertyDataFromJson(JSONObject json) {
        PropertyData propertyData = new PropertyData();
        try {
            propertyData.setHouseId(json.getInt("house_id"));
            propertyData.setHouseName(json.getString("house_name"));
            propertyData.setCondoId(json.getInt("condo_id"));
            propertyData.setCondoName(json.getString("condo_name"));
            propertyData.setCondoAddress(json.getString("condo_address"));
            propertyData.setCondoPublicKey(Utils.hexToBytes(json.getString("public_condo_enc")));
            propertyData.setNotAllowGuests(json.getInt("not_allow_guests") == 1);
            propertyData.setNotAllowGuestsMsg(json.getString("not_allow_guests_msg"));
            propertyData.setOpenWithPlate(json.getInt("open_with_plate") == 1);
            propertyData.setInvitationsWithPlates(json.getInt("invitations_with_plates") == 1);
            propertyData.setResidents(json.getInt("residents"));
            propertyData.setActive(json.getBoolean("active"));
            propertyData.setReason(json.getString("reason"));
            propertyData.setOwner(json.getInt("owner") == 1);
            propertyData.setAdmin(json.getInt("admin") == 1);
            propertyData.setInvitations(json.getInt("invitations") == 1);
            if(json.has("condo_phones")) {
                Log.i(TAG, json.getString("condo_phones"));
                propertyData.setCondoPhones(json.getString("condo_phones"));
            }

            if(json.getString("house_lat").equals("_") || json.getString("house_lat").equals("") ){
                json.put("house_lat", 0);
            }
            if(json.getString("house_lng").equals("_") || json.getString("house_lng").equals("") ){
                json.put("house_lng", 0);
            }
            if(json.getString("lat").equals("_") || json.getString("lat").equals("") ){
                json.put("lat", 0);
            }
            if(json.getString("lng").equals("_") || json.getString("lng").equals("") ){
                json.put("lng", 0);
            }

            propertyData.setHouseLat(json.getDouble("house_lat"));
            propertyData.setHouseLng(json.getDouble("house_lng"));
            propertyData.setLat(json.getDouble("lat"));
            propertyData.setLng(json.getDouble("lng"));

            propertyData.setAllowedSectorsIds(json.getString("allowed_sectors").replace(",", "~"));
            List<String> allowedSectorsIds = new ArrayList<>(
                    Arrays.asList(json.getString("allowed_sectors").split(",")));
            JSONArray sectorsJSONArray = json.getJSONArray("barriers");
            List<SectorData> sectors = new ArrayList<>();
            List<SectorData> allowedSectors = new ArrayList<>();
            for(int i = 0; i < sectorsJSONArray.length(); i++){
                JSONObject sectorJson = sectorsJSONArray.getJSONObject(i);
                SectorData sectorData = getSectorDataFromJson(sectorJson);
                sectorData.setRootId(propertyData.getHouseId());
                sectors.add(sectorData);
                if(json.getString("allowed_sectors").equals("_") ||
                        allowedSectorsIds.contains(""+sectorData.getSectorId())){
                    allowedSectors.add(sectorData);
                }
            }
            propertyData.setSectors(sectors);

            if(allowedSectors.size() == 0){
                SectorData sectorData = new SectorData();
                sectorData.setSectorId(0);
                sectorData.setSectorName("");
                sectorData.setUseQr(true);
                sectorData.setUseRc(false);
                sectorData.setUseInv(false);
                sectorData.setRootId(propertyData.getHouseId());
                allowedSectors.add(sectorData);
            }
            propertyData.setAllowedSectors(allowedSectors);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return propertyData;
    }

    public SectorData getSectorDataFromJson(JSONObject json) {
        SectorData sectorData = new SectorData();
        try {
            sectorData.setSectorId(json.getInt("sector_id"));
            sectorData.setSectorName(json.getString("sector_name"));
            sectorData.setUseQr(json.getInt("use_qr") == 1);
            sectorData.setUseRc(json.getInt("use_rc") == 1);
            if(json.has("use_inv")) {
                sectorData.setUseInv(json.getInt("use_inv") == 1);
            }
            JSONArray gatesJSONArray = json.getJSONArray("gates");
            ArrayList<GateData> gates = new ArrayList<>();
            for(int i = 0; i < gatesJSONArray.length(); i++){
                JSONObject gateJson = gatesJSONArray.getJSONObject(i);
                gates.add(getGateDataFromJson(gateJson));
            }
            sectorData.setGates(gates);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sectorData;
    }

    private GateData getGateDataFromJson(JSONObject json) {
        GateData gateData = new GateData();
        try {
            gateData.setGateId(json.getInt("id"));
            gateData.setGateName(json.getString("gate_name"));
            gateData.setGateCode(json.getString("gate_code").replace(" ", ""));
            gateData.setGateColor(json.getString("gate_color").replace(" ", ""));
            gateData.setGateType(json.getInt("gate_type"));
            gateData.setOwnersGate(json.getInt("owners_gate") == 1);
            gateData.setActive(json.getInt("active") == 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return gateData;
    }

    @Override
    public String toString() {
        return "PropertyDao{" +
                "properties=" + properties.toString() +
                '}';
    }
}