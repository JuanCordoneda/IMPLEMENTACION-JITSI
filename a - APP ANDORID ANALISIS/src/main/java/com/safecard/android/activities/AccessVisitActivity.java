package com.safecard.android.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.safecard.android.BuildConfig;
import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.adapters.RCQRMsgAdapter;
import com.safecard.android.adapters.RCQRPropertyAdapter;
import com.safecard.android.fragments.RCQRFragment;
import com.safecard.android.model.Models;
import com.safecard.android.model.dataobjects.AccessData;
import com.safecard.android.model.dataobjects.PropertyData;
import com.safecard.android.model.dataobjects.SectorData;
import com.safecard.android.utils.Invitation;
import com.safecard.android.utils.LocationProvider;
import com.safecard.android.utils.Utils;
import com.safecard.android.utils.WifiHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class AccessVisitActivity extends AppCompatActivity {
    private static final String TAG = "AccessVisitActivity";

    private TextView toolbarTitleAccess, toolbarTitleAccessCondo;
    private Toolbar toolbar;
    private JSONObject invObj;
    private AccessData accessSelected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_visit);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_shape);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });

        try {
            invObj = new JSONObject(getIntent().getStringExtra("invObj"));
            setSummary();

            if (invObj.has("type") && invObj.getString("type").equals("house")) {
                accessSelected = Models.getAccessModel(getApplicationContext()).getInvitationProperty(invObj.getInt("id"), getApplicationContext());
            }else if (invObj.has("type") && invObj.getString("type").equals("student")) {
                accessSelected = Models.getAccessModel(getApplicationContext()).getInvitationStudent(invObj.getInt("id"), getApplicationContext());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        toolbarTitleAccess = (TextView) toolbar.findViewById(R.id.toolbar_title_access);
        toolbarTitleAccessCondo = (TextView) toolbar.findViewById(R.id.toolbar_title_access_condo);

        deviceAwake(true);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                deviceAwake(false);
            }
        }, 80000);

        Utils.mixpanel.track("INVITATION_OPENED");
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invitation_detail, menu);
        View view = menu.findItem(R.id.action_map).getActionView();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapApp();
            }
        });
        if(!BuildConfig.SERVICE_USED.equals("GOOGLE")){
            menu.findItem(R.id.action_map).setVisible(false);
        }

        return true;
    }

    public void openMapApp() {
        if(!BuildConfig.SERVICE_USED.equals("GOOGLE")){
            String msg = getString(R.string.activity_access_visit_no_google);
            Utils.showToast(getApplicationContext(), msg);
            return;
        }

        try {
            if (invObj.getString("lat").equals("") || invObj.getString("lng").equals("") ||
                    invObj.getString("lat").equals("_") || invObj.getString("lng").equals("_")) {
                Utils.showToast(this, getString(R.string.activity_access_visit_no_geolocated_cant_be_shown_in_map));
                return;
            }

            if (Utils.isAppInstalledAndActive("com.google.android.apps.maps", getApplicationContext())) {
                String condoGPS = invObj.getDouble("lat") + "," + invObj.getDouble("lng");
                String uri = "https://www.google.com/maps/dir/?api=1" +
                        "&destination=" + condoGPS +
                        "&travelmode=driving&dir_action=navigate";

                if (!invObj.getString("house_lat").equals("") && !invObj.getString("house_lng").equals("") &&
                        !invObj.getString("house_lat").equals("_") && !invObj.getString("house_lng").equals("_")) {
                    String houseGPS = invObj.getDouble("house_lat") + "," + invObj.getDouble("house_lng");
                    uri = "https://www.google.com/maps/dir/?api=1" +
                            "&destination=" + houseGPS + "&waypoints=" + condoGPS +
                            "&travelmode=driving&dir_action=navigate";
                }

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");

                PackageManager packageManager = getApplicationContext().getPackageManager();
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent);
                    return;
                }
                Log.d(TAG,"No Intent available to handle action");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        Utils.showToast(this, getString(R.string.activity_access_visit_map_not_available));
    }


    private void setSummary() {
        String summary = "";

        try {

            Invitation invitation = new Invitation();
            invitation.setPropertyId(invObj.getInt("house_id"));
            invitation.setName(invObj.getString("subject"));

            final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

            Calendar dateStart = Calendar.getInstance();
            Calendar dateEnd = Calendar.getInstance();
            dateStart.setTime(dateFormat.parse(invObj.getString("start_date")));
            dateEnd.setTime(dateFormat.parse(invObj.getString("end_date")));

            invitation.setStartDateCalendar(dateStart.get(Calendar.YEAR), dateStart.get(Calendar.MONTH), dateStart.get(Calendar.DAY_OF_MONTH));
            invitation.setEndDateCalendar(dateEnd.get(Calendar.YEAR), dateEnd.get(Calendar.MONTH), dateEnd.get(Calendar.DAY_OF_MONTH));
            invitation.setStartTimeCalendar(dateStart.get(Calendar.HOUR_OF_DAY), dateStart.get(Calendar.MINUTE));
            invitation.setEndTimeCalendar(dateEnd.get(Calendar.HOUR_OF_DAY), dateEnd.get(Calendar.MINUTE));

            invitation.setCustomTime(true);
            if (dateStart.get(Calendar.HOUR_OF_DAY) == 0 && dateStart.get(Calendar.MINUTE) == 0
                    && dateEnd.get(Calendar.HOUR_OF_DAY) == 23 && dateEnd.get(Calendar.MINUTE) == 59) {
                invitation.setCustomTime(false);
            }

            JSONArray arrJsonPlates = invObj.getJSONArray("plates");
            invitation.setPlateNumberUsed(false);
            invitation.setPlateNumber("");
            if(arrJsonPlates.length() > 0) {
                invitation.setPlateNumberUsed(true);
                invitation.setPlateNumber(arrJsonPlates.getString(0));
            }

            boolean[] daysOfWeekSelected = new boolean[7];
            for(int i = 0; i < 7; i++){
                daysOfWeekSelected[i] = false;
            }
            String[] days = invObj.getString("days").split("~");
            for (String d: days) {
                int index = Integer.parseInt(d);
                daysOfWeekSelected[index] = true;
            }
            invitation.setCustomDaysOfWeek(days.length < 7);
            invitation.setDaysOfWeekSelected(daysOfWeekSelected);

            invitation.setIdsSelectedSectors(new ArrayList<Integer>());

            ArrayList<String> organizerName = new ArrayList<>();
            organizerName.add(invObj.getString("organizer_name"));

            summary = Utils.getSummary(
                    AccessVisitActivity.this,
                    Consts.SUMMARY_TYPE_FROM,
                    invitation,
                    organizerName,
                    false,
                    false,
                    invObj.getString("condo_name"),
                    invObj.getString("house_name"),
                    "");

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        TextView summaryTextView = findViewById(R.id.summary);
        summaryTextView.setText(summary);

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAccessView();

        WifiHelper.setSpeed(5000, getApplicationContext());
        LocationProvider.getInstance().connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        WifiHelper.setSpeed(60000, getApplicationContext());
        LocationProvider.getInstance().disconnect();
    }

    public void refreshAccessView() {
        if (accessSelected instanceof PropertyData &&
                ((PropertyData) accessSelected).getStdSchoolId() != -1) {
            PropertyData studentSelected = (PropertyData) accessSelected;
            refreshStudentView(studentSelected);
        }else if (accessSelected instanceof PropertyData) {
            PropertyData propertySelected = (PropertyData) accessSelected;
            refreshPropertyView(propertySelected);
        }
    }

    public void refreshPropertyView(final PropertyData propertySelected) {
        SectorData sectorDefault = propertySelected.getSectorDefault();

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
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
            viewPager.setAdapter(new RCQRPropertyAdapter(fragmentManager, propertySelected, Consts.ACCESSTYPE_INVITATION_PROPERTY));
            tabSectors.setupWithViewPager(viewPager);
            toolbar.setVisibility(View.VISIBLE);

            tabSectors.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    int index = tab.getPosition();
                    List<Fragment> fragments = fragmentManager.getFragments();
                    if (fragments != null) {
                        int sectorId = propertySelected.getAllowedSectors().get(index).getSectorId();
                        for (int i = 0; i < fragments.size(); i++) {
                            RCQRFragment f = (RCQRFragment) fragments.get(i);
                            if (f != null && f.getSectorId() == sectorId) {
                                setBrightness(f.getSelectedControlType());
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
            int tabIndex = Models.getAccessModel(getApplicationContext()).getAllowedSectorIndexById(
                    propertySelected,
                    sectorDefault.getSectorId());
            setBrightness(sectorDefault.getDefaultControlType());
            viewPager.setCurrentItem(tabIndex);
        }
    }

    public void refreshStudentView(final PropertyData studentSelected) {

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabLayout tabSectors = (TabLayout) findViewById(R.id.tabs);

        toolbarTitleAccess.setText(studentSelected.getHouseName());
        toolbarTitleAccessCondo.setText(studentSelected.getCondoName());

        //Log.d("refreshStudentView ", "studentSelected:" + studentSelected.toString());
        final FragmentManager fragmentManager = getSupportFragmentManager();
        tabSectors.setVisibility(View.GONE);

        viewPager.setAdapter(new RCQRPropertyAdapter(fragmentManager, studentSelected, Consts.ACCESSTYPE_INVITATION_STUDENT));
        tabSectors.setupWithViewPager(viewPager);
        toolbar.setVisibility(View.VISIBLE);

        tabSectors.setVisibility(View.GONE);
        setBrightness(Consts.CONTROLTYPE_QR);
    }

    public void setBrightness(String type) {
        if (type != null && type.equals(Consts.CONTROLTYPE_QR)) {
            Utils.setMaxBrightness(true, this);
        } else {
            Utils.setMaxBrightness(false, this);
        }
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    private void deviceAwake(Boolean awake) {
        if (awake.equals(true)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }


}