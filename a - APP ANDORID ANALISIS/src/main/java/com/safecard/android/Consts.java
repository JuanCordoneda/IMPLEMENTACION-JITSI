package com.safecard.android;

/**
 * Created by efajardo on 12/05/16.
 */
public interface Consts {
    int ProfileActivity = 1001;
    int AccessActivity = 1002;
    int AdvancedConfigurationActivity = 1004;
    int ConfirmResidentActivity = 1005;
    int NotificationActivity = 1006;
    int InvitationActivity = 1007;
    int AccessOrInvitationActivity = 1008;
    int VerifyActivity = 1009;
    int ChangePinActivity = 1010;
    int RegisterActivity = 1011;
    int TourActivity = 1012;
    int SmsActivity = 1013;
    int ListPlateActivity = 1014;
    int AccessDefaultSectorActivity = 1015;
    int GeoHousesActivity = 1016;
    int ParkingActivity = 1017;
    int ContactsActivity = 1018;
    int AccessActivityMovements = 1019;
    int ParkingActivityBillings = 1020;
    
    //ACCESS TYPE
    String ACCESSTYPE_OWNER_PROPERTY = "ACCESSTYPE_OWNER_PROPERTY";
    String ACCESSTYPE_INVITATION_PROPERTY = "ACCESSTYPE_INVITATION_PROPERTY";
    String ACCESSTYPE_OWNER_STUDENT = "ACCESSTYPE_OWNER_STUDENT";
    String ACCESSTYPE_INVITATION_STUDENT = "ACCESSTYPE_INVITATION_STUDENT";
    String ACCESSTYPE_PARKING = "ACCESSTYPE_PARKING";

    //CONTROL TYPE
    String CONTROLTYPE_QR = "CONTROLTYPE_QR";
    String CONTROLTYPE_RC = "CONTROLTYPE_RC";
    String CONTROLTYPE_NONE = "CONTROLTYPE_NONE";

    int RESULT_OK = 0;
    int RESULT_NOK = 1;
    int REQUEST_PICK_PROPERTIES = 1;

    int PERMISSION_REQUEST_LOCATION = 0;
    int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 100;
    int PERMISSION_REQUEST_CALL_PHONE_NUMBER = 200;

    String GPS_NO_PERMISSION = "GPS_NO_PERMISSION";
    String GPS_LOCATING = "GPS_LOCATING";
    String GPS_OK = "GPS_OK";
    String GPS_NO_ACTIVE = "GPS_NO_ACTIVE";

    String MSG_TYPE_NO_ACTIVE = "MSG_TYPE_NO_ACTIVE";
    String MSG_TYPE_NO_PROPERTIES = "MSG_TYPE_NO_PROPERTIES";
    String MSG_TYPE_EXPIRED = "MSG_TYPE_EXPIRED";
    String MSG_TYPE_NO_MSG = "MSG_TYPE_NO_MSG";

    String RC_TYPE_GUEST = "RC_TYPE_GUEST";
    String RC_TYPE_OWNER = "RC_TYPE_OWNER";

    String SECTOR_MODE_FLAG = "SECTOR_MODE_FLAG";
    String SECTOR_MODE_LIST = "SECTOR_MODE_LIST";

    String USER_TYPE_OWNER = "USER_TYPE_OWNER";
    String USER_TYPE_RESIDENT_ADMIN = "USER_TYPE_RESIDENT_ADMIN";
    String USER_TYPE_RESIDENT = "USER_TYPE_RESIDENT";

    String SHOW_UPDATE = "show_update";

    String SUMMARY_TYPE_TO = "SUMMARY_TYPE_TO";
    String SUMMARY_TYPE_FROM = "SUMMARY_TYPE_FROM";

    String ACCESS_SELECTED_ID = "ACCESS_SELECTED_ID";
}
