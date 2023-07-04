package com.safecard.android.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.adapters.RCQRMsgAdapter;
import com.safecard.android.adapters.RCQRPropertyAdapter;
import com.safecard.android.adapters.RCQRStudentAdapter;
import com.safecard.android.apicallers.ApiCallback;
import com.safecard.android.apicallers.InitApiCaller;
import com.safecard.android.fragments.RCQRFragment;
import com.safecard.android.model.AccessModel;
import com.safecard.android.model.Models;
import com.safecard.android.model.PaymentMethodModel;
import com.safecard.android.model.dataobjects.AccessData;
import com.safecard.android.model.dataobjects.PropertyData;
import com.safecard.android.model.dataobjects.SectorData;
import com.safecard.android.model.dataobjects.StudentData;
import com.safecard.android.utils.LocationProvider;
import com.safecard.android.utils.PhoneCallManager;
import com.safecard.android.utils.StartAppManager;
import com.safecard.android.utils.Utils;
import com.safecard.android.utils.WifiHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



public class AccessActivity extends BaseActivity {
    private static final String TAG = "AccessActivity";
    public static final String SELECT_PROPERTY = "SELECT_PROPERTY";
    public static final String SHOW_MOVEMENTS = "SHOW_MOVEMENTS";

    public static final int PICK_PROPERTY_FOR_SELECTION = 4;
    public static final int REQUEST_PICK_PHONE_NUMBER = 5;

    private TextView toolbarTitleAccess, toolbarTitleAccessCondo;
    private Toolbar toolbar;

    /*protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG,"onNewIntent");
        int propertyId = getPropertyIdFromIntentURI(intent);
        if(propertyId > 0) {
            Models.getAccessModel(getApplicationContext()).setPropertySelectedById(propertyId);
            refreshAccessView();
        }
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");

        setContentView(R.layout.access);
        setChildActivityCode(Consts.AccessActivity);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //ImageView mMenuAcess = (ImageView) findViewById(R.id.menu_access);
        //assert mMenuAcess != null;
        //mMenuAcess.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_menu_acceso_on));

        LinearLayout changeProperty = (LinearLayout) toolbar.findViewById(R.id.change_property);
        toolbarTitleAccess = (TextView) toolbar.findViewById(R.id.toolbar_title_access);
        toolbarTitleAccessCondo = (TextView) toolbar.findViewById(R.id.toolbar_title_access_condo);


        new InitApiCaller(getApplicationContext()).doCall(new ApiCallback() {
            @Override
            public void callSuccess(JSONObject json) {
            }

            @Override
            public void callError(String errorType, String msg) {
            }
        });

        changeProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<InvitationCustomizationActivity.Property> properties
                        = InvitationCustomizationActivity.getUserProperties(getApplicationContext());

                ArrayList<Integer> tickedPropertyIds = new ArrayList<>();
                final AccessData accessSelected = Models.getAccessModel(getApplicationContext()).getAccessSelected();
                if (accessSelected instanceof PropertyData) {
                    PropertyData propertySelected = (PropertyData) accessSelected;
                    int propertyId = propertySelected.getHouseId();
                    if(propertyId != -1) {
                        tickedPropertyIds.add(propertyId);
                    }
                }

                Intent intent = new Intent(getApplicationContext(), PickPropertiesActivity.class);
                intent.putExtra(PickPropertiesActivity.ONE_ITEM_IMMEDIATE_RESPONSE, false);
                intent.putExtra(PickPropertiesActivity.PROPERTIES, properties);
                intent.putExtra(PickPropertiesActivity.TICKED_PROPERTY_IDS, tickedPropertyIds);
                startActivityForResult(intent, PICK_PROPERTY_FOR_SELECTION);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });


        deviceAwake(true);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                deviceAwake(false);
            }
        }, 80000);
        PaymentMethodModel.udpateList(getApplicationContext());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.getDefaultBoolean(Consts.SHOW_UPDATE, getApplicationContext())) {
            Utils.updateApp(AccessActivity.this);
        }
        Utils.appBlockChecks(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null && !extras.isEmpty()) {
            int selectedPropertyId = extras.getInt(AccessActivity.SELECT_PROPERTY, -1);
            boolean showMovements = extras.getBoolean(AccessActivity.SHOW_MOVEMENTS, false);
            Log.i(TAG, "selectedPropertyId: " + selectedPropertyId);
            getIntent().removeExtra(AccessActivity.SELECT_PROPERTY);
            getIntent().removeExtra(AccessActivity.SHOW_MOVEMENTS);
            if(selectedPropertyId > 0) {
                if (Models.getAccessModel(getApplicationContext())
                        .getPropertyDAO().existProperty(selectedPropertyId)) {
                    Models.getAccessModel(getApplicationContext())
                            .setPropertySelectedById(selectedPropertyId);
                }else{
                    Models.getAccessModel(getApplicationContext())
                            .loadDefaultAccessFromPersisted(getApplicationContext());
                }
                if(showMovements){
                    actionLogs();
                }
            }
        }
        refreshAccessView();

        WifiHelper.setSpeed(5000, getApplicationContext());
        LocationProvider.getInstance().connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PROPERTY_FOR_SELECTION) {
            if (resultCode == AdvancedConfigurationActivity.RESULT_OK) {
                int propertyId = data.getIntExtra(PickPropertiesActivity.SELECTED_PROPERTY_ID, -1);
                if (propertyId <= 0) {
                    return;
                }
                Models.getAccessModel(getApplicationContext()).setPropertySelectedById(propertyId);
                invalidateOptionsMenu();
            }
        }

        if (requestCode == REQUEST_PICK_PHONE_NUMBER) {
            if (resultCode == AdvancedConfigurationActivity.RESULT_OK) {
                String mobile = data.getStringExtra(PickPhoneNumbersActivity.RESPONSE_PHONE_NUMBER);
                if (mobile == null) {
                    return;
                }
                PhoneCallManager.call(mobile, AccessActivity.this);
            }
        }
    }

    public void onPause() {
        super.onPause();
        WifiHelper.setSpeed(60000, getApplicationContext());
        LocationProvider.getInstance().disconnect();
    }

    public void refreshAccessView() {
        Models.getAccessModel(getApplicationContext()).loadFromPersisted(getApplicationContext());
        final AccessData accessSelected = Models.getAccessModel(getApplicationContext()).getAccessSelected();

        if (accessSelected instanceof PropertyData) {
            PropertyData propertySelected = (PropertyData) accessSelected;
            refreshPropertyView(propertySelected);
        } else if (accessSelected instanceof StudentData) {
            StudentData studentSelected = (StudentData) accessSelected;
            refreshStudentView(studentSelected);
        }
    }

    public void refreshPropertyView(final PropertyData propertySelected) {
        SectorData sectorDefault = propertySelected.getSectorDefault();

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(1);
        TabLayout tabSectors = (TabLayout) findViewById(R.id.tabs);

        toolbarTitleAccess.setText(propertySelected.getHouseName());
        toolbarTitleAccessCondo.setText(propertySelected.getCondoName());

        //Log.d("refreshPropertyView ", "getPropertySelected:" + propertySelected.toString());
        //Log.d("refreshPropertyView ", "sectorDefault:" + sectorDefault.toString());
        final FragmentManager fragmentManager = getSupportFragmentManager();
        tabSectors.setVisibility(View.GONE);
        Utils.setMaxBrightness(false, this);
        if (!propertySelected.isActive()) {
            viewPager.setAdapter(new RCQRMsgAdapter(fragmentManager,
                    Consts.MSG_TYPE_NO_ACTIVE, propertySelected.getReason()));
        } else if (propertySelected.getHouseId() == -1) {
            viewPager.setAdapter(new RCQRMsgAdapter(fragmentManager,
                    Consts.MSG_TYPE_NO_PROPERTIES, propertySelected.getReason()));
        } else {
            viewPager.setAdapter(new RCQRPropertyAdapter(fragmentManager, propertySelected, Consts.ACCESSTYPE_OWNER_PROPERTY));
            tabSectors.setupWithViewPager(viewPager);
            toolbar.setVisibility(View.VISIBLE);

            tabSectors.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    //setBrightness(false);
                    int index = tab.getPosition();
                    List<Fragment> fragments = fragmentManager.getFragments();
                    if (fragments != null) {
                        int sectorId = propertySelected.getAllowedSectors().get(index).getSectorId();
                        for (int i = 0; i < fragments.size(); i++) {
                            Fragment fragment = fragments.get(i);
                            if (fragment instanceof RCQRFragment){
                                RCQRFragment f = (RCQRFragment) fragment;
                                if(f.getSectorId() == sectorId) {
                                    setBrightnessByControlType(f.getSelectedControlType());
                                }
                            }
                        }
                    }
                    viewPager.setCurrentItem(index);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });

            tabSectors.setVisibility(
                    propertySelected.getAllowedSectors().size() > 1 ? View.VISIBLE : View.GONE);
            int tabIndex = AccessModel.getAllowedSectorIndexById(
                    propertySelected,
                    sectorDefault.getSectorId());
            setBrightnessByControlType(sectorDefault.getDefaultControlType());
            viewPager.setCurrentItem(tabIndex);
        }
    }

    public void refreshStudentView(final StudentData studentSelected) {

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabLayout tabSectors = (TabLayout) findViewById(R.id.tabs);

        toolbarTitleAccess.setText(studentSelected.getFullName());
        toolbarTitleAccessCondo.setText(studentSelected.getCondoName());

        //Log.d("refreshStudentView ", "studentSelected:" + studentSelected.toString());
        final FragmentManager fragmentManager = getSupportFragmentManager();
        tabSectors.setVisibility(View.GONE);
        Utils.setMaxBrightness(false, this);

        viewPager.setAdapter(new RCQRStudentAdapter(fragmentManager, studentSelected, Consts.ACCESSTYPE_OWNER_STUDENT));
        tabSectors.setupWithViewPager(viewPager);
        toolbar.setVisibility(View.VISIBLE);
        tabSectors.setVisibility(View.GONE);
        setBrightnessByControlType(Consts.CONTROLTYPE_QR);
    }

    public void setBrightnessByControlType(String type) {
        if (type != null && type.equals(Consts.CONTROLTYPE_QR)) {
            Utils.setMaxBrightness(true, this);
        } else {
            Utils.setMaxBrightness(false, this);
        }
    }

    public void setBrightness(boolean status) {
        Utils.setMaxBrightness(status, this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            return super.onTouchEvent(event);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_access, menu);
        final AccessData accessSelected = Models.getAccessModel(getApplicationContext()).getAccessSelected();
        if (accessSelected instanceof PropertyData){
            PropertyData propertySelected = (PropertyData) accessSelected;
            boolean hasPhones = Utils.isJSONArrayAndHasElements(propertySelected.getCondoPhones());
            if(!hasPhones) {
                menu.findItem(R.id.action_call_phone).setVisible(false);
            }
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logs:
                actionLogs();
                return true;
            case R.id.action_call_phone:
                actionCallPhone();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deviceAwake(Boolean awake) {
        if (awake.equals(true)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void actionLogs() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AccessActivity.this);
        builder.setTitle(getString(R.string.activity_access_log_msg_title));

        if (!Models.getAccessModel(getApplicationContext()).hasAccesses()) {
            builder.setMessage(getString(R.string.activity_access_log_msg_no_properties))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.activity_access_log_msg_ok_button), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            final AccessData accessSelected = Models.getAccessModel(getApplicationContext()).getAccessSelected();

            if (accessSelected instanceof PropertyData) {
                PropertyData propertySelected = (PropertyData) accessSelected;
                if (!propertySelected.isActive()) {
                    builder.setMessage(propertySelected.getReason())
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.activity_access_log_msg_ok_button2), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), LogActivity.class);
                    intent.putExtra(LogActivity.RECINTO_LABEL, propertySelected.getHouseName() + " - " + propertySelected.getCondoName());
                    intent.putExtra(LogActivity.HOUSE_STUDENT_ID, propertySelected.getHouseId());
                    intent.putExtra(LogActivity.RECINTO_ID, propertySelected.getCondoId());
                    startActivity(intent);
                }
            } else if (accessSelected instanceof StudentData) {
                StudentData studentSelected = (StudentData) accessSelected;
                Intent intent = new Intent(getApplicationContext(), LogActivity.class);
                intent.putExtra(LogActivity.RECINTO_LABEL, studentSelected.getFullName());
                intent.putExtra(LogActivity.HOUSE_STUDENT_ID, studentSelected.getStudentId());
                intent.putExtra(LogActivity.RECINTO_ID, studentSelected.getSchoolId());
                startActivity(intent);
            }
        }
    }

    private void actionCallPhone() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AccessActivity.this);
        builder.setTitle(getString(R.string.activity_access_call_phone_permission_title));

        if (!PhoneCallManager.hasPermission(AccessActivity.this)) {
            builder.setMessage(getString(R.string.activity_access_call_phone_permission_msg))
                    .setCancelable(true)
                    .setPositiveButton(getString(R.string.activity_access_call_phone_permission_msg_ok_button),
                            new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            PhoneCallManager.requestPermission(
                                    AccessActivity.this, Consts.PERMISSION_REQUEST_CALL_PHONE_NUMBER);
                        }
                    })
                    .setNegativeButton(getString(R.string.activity_access_call_phone_permission_msg_cancel_button),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {}
                            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            final AccessData accessSelected = Models.getAccessModel(getApplicationContext()).getAccessSelected();
            if (accessSelected instanceof PropertyData){
                PropertyData propertySelected = (PropertyData) accessSelected;
                boolean hasPhones = Utils.isJSONArrayAndHasElements(propertySelected.getCondoPhones());
                if(hasPhones) {
                    Intent intent = new Intent(getApplicationContext(), PickPhoneNumbersActivity.class);
                    intent.putExtra(PickPhoneNumbersActivity.ONE_ITEM_IMMEDIATE_RESPONSE, true);
                    intent.putExtra(PickPhoneNumbersActivity.PHONE_NUMBERS_JSON, propertySelected.getCondoPhones());
                    startActivityForResult(intent, REQUEST_PICK_PHONE_NUMBER);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }


        }
    }
}