package com.safecard.android.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.safecard.android.BuildConfig;
import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.adapters.RCQRParkingAdapter;
import com.safecard.android.apicallers.ApiCallback;
import com.safecard.android.apicallers.InitApiCaller;
import com.safecard.android.model.PaymentMethodModel;
import com.safecard.android.model.modelutils.GenericModelCallback;
import com.safecard.android.utils.LocationProvider;
import com.safecard.android.utils.Utils;
import com.safecard.android.utils.WifiHelper;

import org.json.JSONObject;


public class ParkingActivity extends BaseActivity {
    private static final String TAG = "ParkingActivity";
    public static final String SHOW_BILLINGS = "SHOW_BILLINGS";

    private TextView toolbarTitle;
    private Toolbar toolbar;
    private SwipeRefreshLayout swiperefresh;
    private LinearLayout blockLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parking);
        setChildActivityCode(Consts.ParkingActivity);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swiperefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swiperefresh.setEnabled(false);

        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);

        Button button = (Button) findViewById(R.id.btnAction);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent(getApplicationContext(), WebPayEnrollmentWebViewActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        blockLayout = (LinearLayout) findViewById(R.id.block_layout);
        blockLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent(getApplicationContext(), BillingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        deviceAwake(true);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                deviceAwake(false);
            }
        }, 80000);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.getDefaultBoolean(Consts.SHOW_UPDATE, getApplicationContext())) {
            updateApp();
        }
        Utils.appBlockChecks(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null && !extras.isEmpty()) {
            boolean show = extras.getBoolean(ParkingActivity.SHOW_BILLINGS, false);
            getIntent().removeExtra(ParkingActivity.SHOW_BILLINGS);
            if(show){
                showBillings();
            }
        }

        refreshParkingView();

        WifiHelper.setSpeed(5000, getApplicationContext());
        LocationProvider.getInstance().connect();

        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.msg_new_in_parking);
        relativeLayout.setVisibility(View.GONE);
        if (!PaymentMethodModel.arePaymentMethodEnrolled(getApplicationContext())) {
            relativeLayout.setVisibility(View.VISIBLE);
            swiperefresh.setRefreshing(true);
            PaymentMethodModel.udpateList(getApplicationContext(),new GenericModelCallback(){
                @Override
                public void callSuccess(Object... objects) {
                    swiperefresh.setRefreshing(false);
                    if (PaymentMethodModel.arePaymentMethodEnrolled(getApplicationContext())) {
                        relativeLayout.setVisibility(View.GONE);
                        refreshParkingView();
                    }
                }

                @Override
                public void callError(String errorType, String msg) {
                    swiperefresh.setRefreshing(false);
                }
            });
        }

        blockLayout.setVisibility(View.GONE);
        if(!Utils.getDefaultBoolean("active_parking", getApplicationContext())) {
            blockLayout.setVisibility(View.VISIBLE);
            new InitApiCaller(getApplicationContext()).doCall(new ApiCallback() {
                @Override
                public void callSuccess(JSONObject json) {
                    if(Utils.getDefaultBoolean("active_parking", getApplicationContext())) {
                        blockLayout.setVisibility(View.GONE);
                    }
                }

                @Override
                public void callError(String errorType, String msg) {}
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        WifiHelper.setSpeed(60000, getApplicationContext());
        LocationProvider.getInstance().disconnect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        //LocationProvider.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    public void refreshParkingView() {

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabLayout tabSectors = (TabLayout) findViewById(R.id.tabs);

        toolbarTitle.setText(R.string.parking_title);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        tabSectors.setVisibility(View.GONE);
        Utils.setMaxBrightness(false, this);

        viewPager.setAdapter(new RCQRParkingAdapter(fragmentManager));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_parking, menu);
        if(!BuildConfig.SERVICE_USED.equals("GOOGLE")){
            menu.findItem(R.id.action_map).setVisible(false);
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
                showBillings();
                return true;
            case R.id.action_map:
                if(!BuildConfig.SERVICE_USED.equals("GOOGLE")){
                    String msg = getString(R.string.activity_parking_no_google);
                    Utils.showToast(getApplicationContext(), msg);
                    return true;
                }

                if (!LocationProvider.getInstance().isOn()) {
                    Utils.showToast(getApplicationContext(), getString(R.string.parking_you_must_activate_gps));
                    return true;
                }

                if (!LocationProvider.getInstance().hasPermission()) {
                    Utils.showToast(getApplicationContext(), getString(R.string.parking_you_must_grant_gps_access_permission));
                    return true;
                }

                String uri = "https://www.google.com/maps/dir/?api=1&destination=-33.404018,-70.544605" +
                        "&travelmode=driving&dir_action=navigate";

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");

                /*if (intent.getComponent() != null) {
                    Log.d(TAG, "intent.getComponent() is not null: " + intent.getComponent().toString());
                }else{
                    Log.d(TAG, "intent.getComponent() is null");
                }
                if (intent.getAction() != null) {
                    Log.d(TAG, "intent.getAction() is not null: " + intent.getAction());
                }else{
                    Log.d(TAG, "intent.getAction() is null");
                }
                if (intent.resolveType(getApplicationContext().getContentResolver()) != null) {
                    Log.d(TAG, "intent.resolveType() is not null: " + intent.resolveType(getApplicationContext().getContentResolver()));
                }else{
                    Log.d(TAG, "intent.resolveType() is null");
                }

                if (intent.getCategories() != null) {
                    Log.d(TAG, "intent.getCategories() is not null, size(): " + intent.getCategories().size());
                }else{
                    Log.d(TAG, "intent.getCategories() is null");
                }

                if (intent.getPackage() != null) {
                    Log.d(TAG, "intent.getPackage() is not null: " + intent.getPackage());
                }else{
                    Log.d(TAG, "intent.getPackage() is null");
                }*/

                PackageManager packageManager = getApplicationContext().getPackageManager();
                if (intent.resolveActivity(packageManager) == null) {
                    Utils.showToast(getApplicationContext(), getString(R.string.parking_not_available));
                    return true;
                }

                if (!Utils.isAppInstalledAndActive("com.google.android.apps.maps", getApplicationContext())) {
                    Utils.showToast(getApplicationContext(), getString(R.string.parking_not_available2));
                    return true;
                }

                Intent intent2 = new Intent(getApplicationContext(), ParkingMapActivity.class);
                startActivity(intent2);
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

    private void showBillings() {
        startActivity(new Intent(getApplicationContext(), BillingActivity.class));
    }



    public void updateApp() {
        final WindowManager manager = (WindowManager) getBaseContext().getSystemService(Context.WINDOW_SERVICE);

        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.update_available, null);
        final PopupWindow popupWindow = new PopupWindow(
                layout,
                ViewPager.LayoutParams.WRAP_CONTENT,
                ViewPager.LayoutParams.WRAP_CONTENT);

        Point point = new Point();
        manager.getDefaultDisplay().getSize(point);
        popupWindow.setWidth(point.x * 4 / 5);

        TextView dismiss = (TextView) layout.findViewById(R.id.cancel);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        Button action = (Button) layout.findViewById(R.id.action);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                }
                finish();
            }
        });

        new Handler().postDelayed(new Runnable() {
            public void run() {
                popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
            }
        }, 150);
        Utils.setDefaultBoolean("show_update", false, getApplicationContext());
    }
}