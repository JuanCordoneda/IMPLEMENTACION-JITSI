package com.safecard.android.model;

import android.content.Context;
import android.util.Log;

import com.safecard.android.Consts;
import com.safecard.android.model.dao.PropertyDao;
import com.safecard.android.model.dao.StudentDao;
import com.safecard.android.model.dataobjects.AccessData;
import com.safecard.android.model.dataobjects.PropertyData;
import com.safecard.android.model.dataobjects.SectorData;
import com.safecard.android.model.dataobjects.StudentData;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AccessModel {
    private String TAG = "AccessModel";

    private PropertyDao propertyDao;
    private StudentDao studentDao;
    private AccessData accessSelected;

    public AccessModel(Context context){
        propertyDao = new PropertyDao(context);
        studentDao = new StudentDao(context);
        if(propertyDao.getProperties().size() == 0
                && studentDao.getStudents().size() == 0){
            propertyDao.setDummyProperty();
        }
        loadDefaultAccessFromPersisted(context);
    }

    public AccessData getAccessSelected() {
        return accessSelected;
    }

    public void setStudentSelectedById(int studentId) {
        this.accessSelected = studentDao.getStudent(studentId);
    }

    public void setPropertySelectedById(int propertyId) {
        this.accessSelected = propertyDao.getProperty(propertyId);
    }

    public boolean hasAccesses()
    {
        return hasProperties() || hasStudents();
    }

    public boolean hasProperties() {
        return propertyDao.propertiesSize() > 0 &&
                propertyDao.getProperties().get(0).getHouseId() != -1;
    }

    public boolean hasStudents() {
        return studentDao.studentsSize() > 0;
    }

    public PropertyDao getPropertyDAO() {
        return propertyDao;
    }

    public StudentDao getStudentDao() {
        return studentDao;
    }

    public static SectorData getAllowedSectorById(PropertyData property, int defaultSectorId) {
        int index = getAllowedSectorIndexById(property, defaultSectorId);
        if(index >= 0) {
            return property.getSectors().get(index);
        }
        return null;
    }

    public static int getAllowedSectorIndexById(PropertyData property, int defaultSectorId) {
        List<SectorData> sectors = property.getAllowedSectors();
        for(int i = 0; i < sectors.size(); i++){
            SectorData sector = sectors.get(i);
            if(sector.getSectorId() == defaultSectorId){
                return i;
            }
        }
        return -1;
    }

    public void loadFromPersisted(Context context) {
        propertyDao.loadFromPersisted(context);
        studentDao.loadFromPersisted(context);
        if(accessSelected instanceof PropertyData) {
            PropertyData p = (PropertyData) accessSelected;
            if (propertyDao.existProperty(p.getHouseId())) {
                accessSelected = propertyDao.getProperty(p.getHouseId());
                return;
            }
        }

        if(accessSelected instanceof StudentData) {
            StudentData s = (StudentData) accessSelected;
            if (studentDao.existStudent(s.getStudentId())) {
                accessSelected = studentDao.getStudent(s.getStudentId());
                return;
            }
        }

        if(propertyDao.propertiesSize() > 0) {
            accessSelected = propertyDao.getProperties().get(0);
            return;
        }

        if(studentDao.studentsSize() > 0) {
            accessSelected = studentDao.getStudents().get(0);
            return;
        }
    }

    public void loadDefaultAccessFromPersisted(Context context){
        int propertyId = Utils.getDefaultInt(Consts.ACCESS_SELECTED_ID, context);
        if(propertyDao.existProperty(propertyId)){
            accessSelected = propertyDao.getProperty(propertyId);
        } else if(propertyDao.getProperties().size() > 0 && propertyDao.getProperties().get(0).getCondoId() != -1){
            accessSelected = propertyDao.getProperties().get(0);
        }else if(studentDao.getStudents().size() > 0) {
            accessSelected = studentDao.getStudents().get(0);
        }else if(propertyDao.getProperties().size() > 0){
            //caso propiedad dummy
            accessSelected = propertyDao.getProperties().get(0);
        }
    }

    public PropertyData getInvitationProperty(int invitationId, Context context) {
        PropertyData propertyData = new PropertyData();
        try {
            JSONObject invitations = Utils.getDefaultJSONObject("invitations", context);
            JSONArray inbox = invitations.getJSONArray("recibidas");
            JSONObject invitationJson = null;
            for(int i = 0; i < inbox.length(); i++){
                if(invitationId == inbox.getJSONObject(i).getInt("id")){
                    invitationJson = inbox.getJSONObject(i);
                    break;
                }
            }
            if(invitationJson == null) {
                return propertyData;
            }
            propertyData.setInvitationId(invitationJson.getInt("id"));
            propertyData.setOrganizerName(invitationJson.getString("organizer_name"));
            propertyData.setOrganizerMobile(invitationJson.getString("organizer_mobile"));
            propertyData.setStartDate(invitationJson.getString("start_date"));
            propertyData.setEndDate(invitationJson.getString("end_date"));
            propertyData.setDays(invitationJson.getString("days"));

            if(invitationJson.has("hash")){
                propertyData.setCondoPublicKey(Utils.hexToBytes(invitationJson.getString("public_condo_enc")));
                propertyData.setOrganizerId(invitationJson.getInt("organizer_id"));
                propertyData.setHash(Utils.hexToBytes(invitationJson.getString("hash")));
                propertyData.setNonce(Utils.hexToBytes(invitationJson.getString("nonce")));
             }

            propertyData.setHouseId(invitationJson.getInt("house_id"));
            propertyData.setHouseName(invitationJson.getString("house_name"));
            propertyData.setCondoId(invitationJson.getInt("condo_id"));
            propertyData.setCondoName(invitationJson.getString("condo_name"));
            propertyData.setCondoAddress(invitationJson.getString("condo_address"));
            propertyData.setActive(invitationJson.getInt("active") == 1);
            propertyData.setOwner(false);
            propertyData.setAdmin(false);
            propertyData.setInvitations(false);

            if(invitationJson.getString("house_lat").equals("_") || invitationJson.getString("house_lat").equals("")){
                invitationJson.put("house_lat", 0);
            }
            if(invitationJson.getString("house_lng").equals("_") || invitationJson.getString("house_lng").equals("")){
                invitationJson.put("house_lng", 0);
            }
            if(invitationJson.getString("lat").equals("_") || invitationJson.getString("lat").equals("")){
                invitationJson.put("lat", 0);
            }
            if(invitationJson.getString("lng").equals("_") || invitationJson.getString("lng").equals("")){
                invitationJson.put("lng", 0);
            }

            propertyData.setHouseLat(invitationJson.getDouble("house_lat"));
            propertyData.setHouseLng(invitationJson.getDouble("house_lng"));
            propertyData.setLat(invitationJson.getDouble("lat"));
            propertyData.setLng(invitationJson.getDouble("lng"));

            propertyData.setAllowedSectorsIds(invitationJson.getString("allowed_sectors").replace(",", "~"));
            List<String> allowedSectorsIds = new ArrayList<>(
                    Arrays.asList(invitationJson.getString("allowed_sectors").split(",")));
            JSONArray sectorsJSONArray = invitationJson.getJSONArray("sectors");
            List<SectorData> sectors = new ArrayList<>();
            List<SectorData> allowedSectors = new ArrayList<>();
            for(int i = 0; i < sectorsJSONArray.length(); i++){
                JSONObject sectorJson = sectorsJSONArray.getJSONObject(i);
                SectorData sectorData = getPropertyDAO().getSectorDataFromJson(sectorJson);
                if(invitationJson.getInt("guest_remote_control") == 0){
                    sectorData.setUseRc(false);
                }
                sectorData.setDefaultControlType(Consts.CONTROLTYPE_QR);
                if(sectorData.isUseRc() && sectorData.getGates().size() > 0) {
                    sectorData.setDefaultControlType(Consts.CONTROLTYPE_RC);
                }
                sectorData.setRootId(propertyData.getInvitationId());
                sectors.add(sectorData);

                if(allowedSectorsIds.contains(""+sectorData.getSectorId())){
                    allowedSectors.add(sectorData);
                }
            }

            if(allowedSectors.size() == 0){
                SectorData sectorData = new SectorData();
                sectorData.setSectorId(0);
                sectorData.setSectorName("");
                sectorData.setUseQr(true);
                sectorData.setUseRc(false);
                sectorData.setUseInv(false);
                sectorData.setRootId(propertyData.getInvitationId());
                sectorData.setDefaultControlType(Consts.CONTROLTYPE_QR);
                allowedSectors.add(sectorData);
            }

            propertyData.setSectorDefault(allowedSectors.get(0));
            propertyData.setSectors(sectors);
            propertyData.setAllowedSectors(allowedSectors);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return propertyData;
    }

    public PropertyData getInvitationStudent(int invitationId, Context context) {
        PropertyData propertyData = new PropertyData();
        try {
            JSONObject invitations = Utils.getDefaultJSONObject("invitations", context);
            JSONArray inbox = invitations.getJSONArray("recibidas");
            JSONObject invitationJson = null;
            for(int i = 0; i < inbox.length(); i++){
                if(invitationId == inbox.getJSONObject(i).getInt("id")){
                    invitationJson = inbox.getJSONObject(i);
                    break;
                }
            }
            if(invitationJson == null) {
                return propertyData;
            }

            propertyData.setInvitationId(invitationJson.getInt("id"));
            propertyData.setOrganizerName(invitationJson.getString("organizer_name"));
            propertyData.setOrganizerMobile(invitationJson.getString("organizer_mobile"));
            propertyData.setStartDate(invitationJson.getString("start_date"));
            propertyData.setEndDate(invitationJson.getString("end_date"));
            propertyData.setDays(invitationJson.getString("days"));
            propertyData.setStdSchoolId(invitationJson.getInt("stdschool_id"));

            propertyData.setHouseId(invitationJson.getInt("house_id"));
            propertyData.setHouseName(invitationJson.getString("house_name"));
            propertyData.setCondoId(invitationJson.getInt("condo_id"));
            propertyData.setCondoName(invitationJson.getString("condo_name"));
            propertyData.setCondoAddress(invitationJson.getString("condo_address"));
            propertyData.setActive(invitationJson.getInt("active") == 1);
            propertyData.setOwner(false);
            propertyData.setAdmin(false);
            propertyData.setInvitations(false);

            if(invitationJson.getString("house_lat").equals("_") || invitationJson.getString("house_lat").equals("")){
                invitationJson.put("house_lat", 0);
            }
            if(invitationJson.getString("house_lng").equals("_") || invitationJson.getString("house_lng").equals("")){
                invitationJson.put("house_lng", 0);
            }
            if(invitationJson.getString("lat").equals("_") || invitationJson.getString("lat").equals("")){
                invitationJson.put("lat", 0);
            }
            if(invitationJson.getString("lng").equals("_") || invitationJson.getString("lng").equals("")){
                invitationJson.put("lng", 0);
            }

            propertyData.setHouseLat(invitationJson.getDouble("house_lat"));
            propertyData.setHouseLng(invitationJson.getDouble("house_lng"));
            propertyData.setLat(invitationJson.getDouble("lat"));
            propertyData.setLng(invitationJson.getDouble("lng"));
            SectorData sectorData = new SectorData();
            sectorData.setUseQr(true);
            sectorData.setUseRc(false);
            sectorData.setDefaultControlType(Consts.CONTROLTYPE_QR);
            sectorData.setRootId(propertyData.getInvitationId());

            propertyData.getSectors().add(sectorData);
            propertyData.getAllowedSectors().add(sectorData);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return propertyData;
    }
}