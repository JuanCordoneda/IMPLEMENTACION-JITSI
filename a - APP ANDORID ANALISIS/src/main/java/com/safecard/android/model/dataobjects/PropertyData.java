package com.safecard.android.model.dataobjects;

import android.util.Log;

import com.safecard.android.Config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PropertyData implements Serializable, AccessData {

    //API data
    private int houseId;
    private String houseName;
    private Double houseLat;
    private Double houseLng;
    private int condoId;
    private String condoName;
    private String condoAddress;
    private Double lat; //del condo
    private Double lng; //del condo
    private boolean notAllowGuests;
    private String notAllowGuestsMsg;
    private boolean openWithPlate;
    private boolean invitationsWithPlates;
    private int residents;
    private boolean active;
    private String reason;
    private boolean owner;
    private boolean admin;
    private boolean invitations;
    private byte[] condoPublicKey;

    private String condoPhones;

    private List<SectorData> allowedSectors;
    private List<SectorData> sectors;

    //LOCAL data
    private SectorData sectorDefault;

    //INVITATION
    private int invitationId;
    private String startDate;
    private String endDate;
    private int stdSchoolId;
    private String organizerName;
    private String organizerMobile;
    private String days;

    private byte[] invitationKey ;
    private int organizerId;
    private byte[] salt;
    private int signLength;
    private byte[] sign;
    private byte[] startDateBin;
    private byte[] endDateBin;
    private byte[] daysBin;
    private byte[] allowedSectorsBin;
    private int allowedSectorsCount;
    private String allowedSectorsIds;
    private byte[] hash;
    private byte[] nonce;

    public PropertyData() {
        houseId = -1;
        houseName = "Sin propiedad";
        allowedSectors = new ArrayList<>();
        sectors = new ArrayList<>();
        sectorDefault =  new SectorData();
        sectorDefault.setSectorName("Sin sector por defecto.");
        sectorDefault.setSectorId(-1);
        stdSchoolId = -1;
        condoPhones = "[]";
    }

    public String getCondoPhones() {
        return condoPhones;
    }

    public void setCondoPhones(String condoPhones) {
        this.condoPhones = condoPhones;
    }

    public int getHouseId() {
        return houseId;
    }

    public void setHouseId(int houseId) {
        this.houseId = houseId;
    }

    public String getHouseName() {
        return houseName;
    }

    public void setHouseName(String houseName) {
        this.houseName = houseName;
    }

    public Double getHouseLat() {
        return houseLat;
    }

    public void setHouseLat(Double houseLat) {
        this.houseLat = houseLat;
    }

    public Double getHouseLng() {
        return houseLng;
    }

    public void setHouseLng(Double houseLng) {
        this.houseLng = houseLng;
    }

    public int getCondoId() {
        return condoId;
    }

    public void setCondoId(int condoId) {
        this.condoId = condoId;
    }

    public String getCondoName() {
        return condoName;
    }

    public void setCondoName(String condoName) {
        this.condoName = condoName;
    }

    public String getCondoAddress() {
        return condoAddress;
    }

    public void setCondoAddress(String condoAddress) {
        this.condoAddress = condoAddress;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public boolean isNotAllowGuests() {
        return notAllowGuests;
    }

    public void setNotAllowGuests(boolean notAllowGuests) {
        this.notAllowGuests = notAllowGuests;
    }

    public String getNotAllowGuestsMsg() {
        return notAllowGuestsMsg;
    }

    public void setNotAllowGuestsMsg(String notAllowGuestsMsg) {
        this.notAllowGuestsMsg = notAllowGuestsMsg;
    }

    public boolean isOpenWithPlate() {
        return openWithPlate;
    }

    public void setOpenWithPlate(boolean openWithPlate) {
        this.openWithPlate = openWithPlate;
    }

    public boolean isInvitationsWithPlates() {
        return invitationsWithPlates;
    }

    public void setInvitationsWithPlates(boolean invitationsWithPlates) {
        this.invitationsWithPlates = invitationsWithPlates;
    }

    public int getResidents() {
        return residents;
    }

    public void setResidents(int residents) {
        this.residents = residents;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isInvitations() {
        return invitations;
    }

    public void setInvitations(boolean invitations) {
        this.invitations = invitations;
    }

    public List<SectorData> getAllowedSectors() {
        return allowedSectors;
    }

    public void setAllowedSectors(List<SectorData> allowedSectors) {
        this.allowedSectors = allowedSectors;
    }

    public List<SectorData> getSectors() {
        return sectors;
    }

    public void setSectors(List<SectorData> sectors) {
        this.sectors = sectors;
    }

    public SectorData getSectorDefault() {
        return sectorDefault;
    }

    public void setSectorDefault(SectorData sectorDefault) {
        this.sectorDefault = sectorDefault;
    }

    @Override
    public String toString() {
        String sectorDefault = "NULL";
        if(sectorDefault != null){
            sectorDefault = sectorDefault.toString(); // TODO fix this
        }
        return "PropertyData{" +
                "houseId=" + houseId +
                ", houseName='" + houseName + '\'' +
                ", houseLat=" + houseLat +
                ", houseLng=" + houseLng +
                ", condoId=" + condoId +
                ", condoName='" + condoName + '\'' +
                ", condoAddress='" + condoAddress + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", notAllowGuests=" + notAllowGuests +
                ", notAllowGuestsMsg='" + notAllowGuestsMsg + '\'' +
                ", openWithPlate=" + openWithPlate +
                ", invitationsWithPlates=" + invitationsWithPlates +
                ", residents=" + residents +
                ", active=" + active +
                ", reason='" + reason + '\'' +
                ", owner=" + owner +
                ", admin=" + admin +
                ", invitations=" + invitations +
                ", allowedSectors=" + allowedSectors.toString() +
                ", sectors=" + sectors.toString() +
                ", sectorDefault=" + sectorDefault +
                '}';
    }

    public int getInvitationId() {
        return this.invitationId;
    }

    public void setInvitationId(int invitationId) {
        this.invitationId = invitationId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getStdSchoolId() {
        return stdSchoolId;
    }

    public void setStdSchoolId(int stdSchoolId) {
        this.stdSchoolId = stdSchoolId;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getOrganizerMobile() {
        return organizerMobile;
    }

    public void setOrganizerMobile(String organizerMobile) {
        this.organizerMobile = organizerMobile;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public byte[] getInvitationKey() {
        return invitationKey;
    }

    public void setInvitationKey(byte[] invitationKey) {
        this.invitationKey = invitationKey;
    }

    public int getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(int organizerId) {
        this.organizerId = organizerId;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public int getSignLength() {
        return signLength;
    }

    public void setSignLength(int signLength) {
        this.signLength = signLength;
    }

    public byte[] getSign() {
        return sign;
    }

    public void setSign(byte[] sign) {
        this.sign = sign;
    }

    public byte[] getStartDateBin() {
        return startDateBin;
    }

    public void setStartDateBin(byte[] startDateBin) {
        Log.d("PropertyData", "startDateBin" + startDateBin.length);
        this.startDateBin = startDateBin;
    }

    public byte[] getEndDateBin() {
        return endDateBin;
    }

    public void setEndDateBin(byte[] endDateBin) {
        Log.d("PropertyData", "endDateBin" + endDateBin.length);
        this.endDateBin = endDateBin;
    }

    public byte[] getDaysBin() {
        return daysBin;
    }

    public void setDaysBin(byte[] daysBin) {
        Log.d("PropertyData", "daysBin" + daysBin.length);
        this.daysBin = daysBin;
    }

    public byte[] getAllowedSectorsBin() {
        return allowedSectorsBin;
    }

    public void setAllowedSectorsBin(byte[] allowedSectorsBin) {
        Log.d("PropertyData", "allowedSectorsBin" + allowedSectorsBin.length);
        this.allowedSectorsBin = allowedSectorsBin;
    }

    public int getAllowedSectorsCount() {
        return allowedSectorsCount;
    }

    public void setAllowedSectorsCount(int allowedSectorsCount) {
        this.allowedSectorsCount = allowedSectorsCount;
    }

    public String getAllowedSectorsIds() {
        return allowedSectorsIds;
    }

    public void setAllowedSectorsIds(String allowedSectorsIds) {
        this.allowedSectorsIds = allowedSectorsIds;
    }

    public byte[] getCondoPublicKey() {
        return condoPublicKey;
    }

    public void setCondoPublicKey(byte[] condoPublicKey) {
        this.condoPublicKey = condoPublicKey;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }
}