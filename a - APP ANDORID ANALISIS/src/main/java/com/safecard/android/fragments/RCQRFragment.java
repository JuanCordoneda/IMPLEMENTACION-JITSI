package com.safecard.android.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.safecard.android.BuildConfig;
import com.safecard.android.Config;
import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.apicallers.ApiCallback;
import com.safecard.android.apicallers.OpenBarrierInvitationApiCaller;
import com.safecard.android.apicallers.OpenBarrierOwnerApiCaller;
import com.safecard.android.model.dataobjects.GateData;
import com.safecard.android.model.dataobjects.SectorData;
import com.safecard.android.model.dataobjects.StudentData;
import com.safecard.android.utils.LocationProvider;
import com.safecard.android.utils.QRCodeBitmapEncoder;
import com.safecard.android.utils.QRFormatter;
import com.safecard.android.utils.Utils;
import com.safecard.android.utils.WifiHelper;

import org.json.JSONObject;
import java.util.List;


public class RCQRFragment extends Fragment {
    public static final String TAG = "RCQRFragment";
    public static final String SECTOR = "SECTOR";
    public static final String ACCESSTYPE = "ACCESSTYPE";
    private int smallerDimension;
    private boolean isOpenBarrierInvitationBlocked = false;
    private boolean isOpenBarrierOwnerBlocked = false;

    public static RCQRFragment newInstance(SectorData sector, String accessType) {
        RCQRFragment fragment = new RCQRFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(SECTOR, sector);
        Log.d(TAG, "sector" + sector.toString());
        bundle.putString(ACCESSTYPE, accessType);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static Fragment newInstance(StudentData student, String accessType) {
        RCQRFragment fragment = new RCQRFragment();
        Bundle bundle = new Bundle();
        SectorData sector = new SectorData();
        sector.setUseRc(false);
        sector.setUseQr(true);
        sector.setDefaultControlType(Consts.CONTROLTYPE_QR);
        sector.setRootId(student.getStudentId());
        bundle.putSerializable(SECTOR, sector);
        bundle.putString(ACCESSTYPE, accessType);

        fragment.setArguments(bundle);
        return fragment;
    }

    public static Fragment newInstanceParking() {
        RCQRFragment fragment = new RCQRFragment();
        Bundle bundle = new Bundle();
        SectorData sector = new SectorData();
        sector.setUseRc(false);
        sector.setUseQr(true);
        sector.setDefaultControlType(Consts.CONTROLTYPE_QR);
        sector.setRootId(-1);
        bundle.putSerializable(SECTOR, sector);
        bundle.putString(ACCESSTYPE, Consts.ACCESSTYPE_PARKING);
        fragment.setArguments(bundle);
        return fragment;
    }

    private int qrSize;
    SectorData sector;
    String defaultControlType;
    String selectedControlType;
    String accessType;

    View rootView;
    Handler gpsWifiHandler, qrHandler;
    boolean visible = false;

    ImageView changeRc;
    ImageView changeQr;
    ImageView qrCodeImageView;
    TextView debugInfo;

    ProgressDialog pDialog;

    public String getSelectedControlType(){
        return selectedControlType;
    }

    public int getSectorId(){
        return sector.getSectorId();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("selectedControlType", selectedControlType);
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        this.visible = visible;
        if(qrHandler != null) {
            updateQR();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(true);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        accessType = getArguments().getString(ACCESSTYPE);
        sector = (SectorData) getArguments().getSerializable(SECTOR);
        defaultControlType = sector.getDefaultControlType();
        selectedControlType = defaultControlType;
        if(savedInstanceState != null &&
                savedInstanceState.getString("selectedControlType") != null){
            selectedControlType = savedInstanceState.getString("selectedControlType");
        }

        Point size = new Point();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getSize(size);
        smallerDimension = size.x < size.y ? size.x : size.y;

        /*int mWidthPixels = size.x;
        int mHeightPixels = size.y;
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        Log.d(TAG,"Screen x: " + mWidthPixels);
        Log.d(TAG,"Screen x: " + dm.xdpi);
        float ppi = (mWidthPixels/mWidthPixels/dm.xdpi);
        */

        qrSize = Math.round( smallerDimension * 13 / 20);
        //if(mWidthPixels/dm.xdpi*7/10 > 1){
        //    qrSize = (int)(640);
        //}
    }

    @Override
    public void onResume() {
        super.onResume();
        if(gpsWifiHandler == null) {
            gpsWifiHandler = new Handler();
            new Runnable() {
                @Override
                public void run() {
                    updateWifiStatus();
                    updateGpsStatus();
                    if (gpsWifiHandler != null) {
                        gpsWifiHandler.postDelayed(this, 2000);
                    }
                }
            }.run();
        }

        if (selectedControlType != null && selectedControlType.equals(Consts.CONTROLTYPE_QR)) {
            Utils.setMaxBrightness(true, getActivity());
            startQr();
        } else {
            Utils.setMaxBrightness(false, getActivity());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        gpsWifiHandler.removeCallbacks(null);
        gpsWifiHandler = null;
        stopQR();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_access, container, false);


        debugInfo = rootView.findViewById(R.id.debug_info);
        changeRc = (ImageView) rootView.findViewById(R.id.change_cr);
        changeQr = (ImageView) rootView.findViewById(R.id.change_qr);
        LinearLayout controlChanger = (LinearLayout) rootView.findViewById(R.id.control_change);
        qrCodeImageView = (ImageView) rootView.findViewById(R.id.qr_code);

        qrCodeImageView.setMinimumWidth(qrSize);
        qrCodeImageView.setMinimumHeight(qrSize);

        controlChanger.setVisibility(
                sector.isUseRc() && sector.isUseQr() &&
                sector.getGates().size() > 0? View.VISIBLE : View.GONE);

        changeQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!selectedControlType.equals(Consts.CONTROLTYPE_QR)) {
                    selectedControlType = Consts.CONTROLTYPE_QR;
                    startQr();
                    animatedRefreshView();
                    Utils.setMaxBrightness(true, getActivity());
                }
            }
        });

        changeRc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!selectedControlType.equals(Consts.CONTROLTYPE_RC)) {
                    selectedControlType = Consts.CONTROLTYPE_RC;
                    animatedRefreshView();
                    Utils.setMaxBrightness(false, getActivity());
                    stopQR();
                }
            }
        });
        refreshView();
        return rootView;
    }

    public void animatedRefreshView() {
        refresh(true);
    }
    public void refreshView() {
        refresh(false);
    }

    public void refresh(Boolean animate) {
        final LinearLayout panelStatus = (LinearLayout) rootView.findViewById(R.id.panel_status);
        final View panel = rootView.findViewById(R.id.panel);
        rootView.findViewById(R.id.QRLayout).setVisibility(View.GONE);
        rootView.findViewById(R.id.RCLayout).setVisibility(View.GONE);
        rootView.findViewById(R.id.MsgLayout).setVisibility(View.GONE);
        loadRC();
        preloadQR();
        changeQr.getDrawable().clearColorFilter();
        changeRc.getDrawable().clearColorFilter();
        if (selectedControlType.equals(Consts.CONTROLTYPE_RC)) {
            rootView.findViewById(R.id.RCLayout).setVisibility(View.VISIBLE);
            //changeRc.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            //changeQr.getDrawable().setColorFilter(Color.parseColor("#5A5B5D"), PorterDuff.Mode.MULTIPLY);
            if (animate) {
                panelStatus.setVisibility(View.VISIBLE);
                panel.setY(-panelStatus.getHeight());
                panel.animate()
                        .translationY(0)
                        .setDuration(200);
            } else {
                panelStatus.setVisibility(View.VISIBLE);
            }
        } else if (selectedControlType.equals(Consts.CONTROLTYPE_QR)) {
            rootView.findViewById(R.id.QRLayout).setVisibility(View.VISIBLE);
            //changeQr.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            //changeRc.getDrawable().setColorFilter(Color.parseColor("#5A5B5D"), PorterDuff.Mode.MULTIPLY);
            if (animate) {
                panel.animate()
                        .translationY(-panelStatus.getHeight())
                        .setDuration(200);
            } else {
                panelStatus.setVisibility(View.GONE);
            }
            Utils.mixpanel.track("QR_SHOWN");
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void loadRC() {
        //Log.d(TAG,"loadRC");
        TextView wifiStatus = (TextView) rootView.findViewById(R.id.wifi_status);
        TextView gpsStatus = (TextView) rootView.findViewById(R.id.gps_status);

        wifiStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpWifi();
            }
        });

        gpsStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpGps();
            }
        });

        int[] btnremotes = {R.id.btnremote01, R.id.btnremote02,R.id.btnremote03, R.id.btnremote04};

        try {
            List<GateData> gates = sector.getGates();
            for (int i = 0; i < gates.size(); i++) {
                GateData gate = gates.get(i);
                Button btnRemote = (Button) rootView.findViewById(btnremotes[i]);
                btnRemote.setVisibility(View.VISIBLE);
                btnRemote.setText(Html.fromHtml(gate.getGateName()));
                ViewGroup.LayoutParams params = btnRemote.getLayoutParams();
                //Tamaños
                params.height = params.width = (int) (smallerDimension * 0.25);
                if (gate.isOwnersGate()){
                    params.height = params.width = (int) (smallerDimension * 0.3);
                }
                btnRemote.setLayoutParams(params);
                ShapeDrawable circle = new ShapeDrawable(new OvalShape());
                circle.getPaint().setColor(Color.parseColor(gate.getGateColor()));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    btnRemote.setBackground(circle);
                } else {
                    btnRemote.setBackgroundDrawable(circle);
                }

                btnRemote.setTag(R.id.tag_barrier_code, gate.getGateCode());
                btnRemote.setTag(R.id.tag_barrier_type, gate.getGateType());
                btnRemote.setTag(R.id.tag_barrier_name, gate.getGateName());

                LinearLayout ll = (LinearLayout) btnRemote.getParent();
                ll.setVisibility(View.VISIBLE);

                btnRemote.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                Animation animScale = AnimationUtils.loadAnimation(getContext(), R.anim.anim_scale);
                                v.startAnimation(animScale);
                                break;
                            case MotionEvent.ACTION_UP:
                                Vibrator vib = (Vibrator) getContext().getSystemService(getContext().VIBRATOR_SERVICE);
                                vib.vibrate(300);
                                String gateCode = v.getTag(R.id.tag_barrier_code).toString();
                                String gateType = v.getTag(R.id.tag_barrier_type).toString();
                                String gateName = v.getTag(R.id.tag_barrier_name).toString();

                                if(!Utils.appBlockChecks(getActivity())) {
                                    if (!WifiHelper.isLocal() &&
                                            (!LocationProvider.getInstance().isOn() || !LocationProvider.getInstance().hasPermission())) {
                                        String msg = getString(R.string.fragment_rcqr_info_gps_or_wifi_local);
                                        Utils.showToast(getActivity().getApplicationContext(), msg);
                                    } else {
                                        pDialog.setMessage(getString(R.string.fragment_rcqr_requesting_opening_wait_a_moment));
                                        pDialog.show();
                                        if (accessType.equals(Consts.ACCESSTYPE_OWNER_PROPERTY)) {
                                            openBarrierOwner(sector.getRootId(), gateCode, gateType, gateName);
                                        } else if (accessType.equals(Consts.ACCESSTYPE_INVITATION_PROPERTY)) {
                                            openBarrierInvitation(sector.getRootId(), gateCode, gateType, gateName);
                                        }
                                    }
                                }
                                break;
                        }
                        return false;
                    }
                });
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void openBarrierInvitation(int invitationId,
                                       String gateCode,
                                       String gateType,
                                       String gateName) {
        if(isOpenBarrierInvitationBlocked){
            return;
        }
        isOpenBarrierInvitationBlocked = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isOpenBarrierInvitationBlocked = false;
            }
        }, 1000);

        new OpenBarrierInvitationApiCaller(getActivity().getApplicationContext()).doCall(
                invitationId,
                gateType,
                gateCode,
                gateName,
                new ApiCallback() {
                    @Override
                    public void callSuccess(JSONObject response) {
                        if(pDialog != null && pDialog.isShowing()){
                            Utils.safeDismissDialog(getActivity(), pDialog);
                        }
                    }

                    @Override
                    public void callError(String errorType, String msg) {
                        if(pDialog != null && pDialog.isShowing()){
                            Utils.safeDismissDialog(getActivity(), pDialog);
                        }
                        if (!msg.equals("") && getActivity() != null){
                            Utils.showToast(getActivity().getApplicationContext(), msg);
                        }
                        if (getActivity() != null) {
                            Utils.appBlockChecks(getActivity());
                        }
                    }
                });

    }

    private void openBarrierOwner(int propertyId,
                                  String gateCode,
                                  String gateType,
                                  String gateName) {
        if(isOpenBarrierOwnerBlocked){
            return;
        }
        isOpenBarrierOwnerBlocked = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isOpenBarrierOwnerBlocked = false;
            }
        }, 1000);

        new OpenBarrierOwnerApiCaller(getActivity().getApplicationContext()).doCall(
                propertyId,
                gateType,
                gateCode,
                gateName,
                new ApiCallback() {
                    @Override
                    public void callSuccess(JSONObject response) {
                        if(pDialog != null && pDialog.isShowing()){
                            Utils.safeDismissDialog(getActivity(), pDialog);
                        }
                    }

                    @Override
                    public void callError(String errorType, String msg) {
                        if(pDialog != null && pDialog.isShowing()){
                            Utils.safeDismissDialog(getActivity(), pDialog);
                        }
                        if (!msg.equals("") && getActivity() != null){
                            Utils.showToast(getActivity().getApplicationContext(), msg);
                        }
                        if (getActivity() != null) {
                            Utils.appBlockChecks(getActivity());
                        }
                    }
                });
    }

    public void preloadQR() {
        TextView info = (TextView) rootView.findViewById(R.id.info);
        info.setVisibility(View.VISIBLE);
        updateQR();
    }

    public void startQr() {
        //Log.d(TAG,"loadQR");
        //TextView student_name = (TextView) rootView.findViewById(R.id.student_name);
        TextView info = (TextView) rootView.findViewById(R.id.info);
        info.setVisibility(View.VISIBLE);

        if(qrHandler == null) {
            qrHandler = new Handler();

            Runnable r = new Runnable() {
                @Override
                public void run() {

                    //Log.d(TAG,"qr running: " + sector.getSectorName());
                    int pos[] = new int[2];
                    rootView.getLocationInWindow(pos);
                    if(selectedControlType.equals(Consts.CONTROLTYPE_QR) &&
                            RCQRFragment.this.visible &&
                            pos[0] == 0 &&
                            getActivity() != null) {
                        updateQR();
                    }
                    if(qrHandler != null) {
                        qrHandler.postDelayed(this, Config.time_refresh_qr);
                    }
                }
            };
            r.run();
            //Thread t = new Thread(r);
            //t.setPriority(Thread.MIN_PRIORITY);
            //t.run();
        }
    }

    public void stopQR() {
        if(qrHandler != null) {
            qrHandler.removeCallbacks(null);
            qrHandler = null;
        }
    }

    @Override
   public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG,"Fragment onRequestPermissionsResult");
        if (requestCode == PERMISSIONS_CODE_LOCATION) {
            boolean locationCoarse = false, locationFine = false;
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                        && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    locationCoarse = true;
                }else if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                        && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    locationFine = true;
                }
            }
            if(locationCoarse && locationFine){
                Log.d(TAG,"Fragment LocationProvider.getInstance().connect");
                LocationProvider.getInstance().connect();
            }
        }
    }

    private void updateQR() {
        //Log.d(TAG,"update QR");
        Log.d("lag","load QRDATA");
        byte[] data = new byte[]{(byte)0xFF};
        if(accessType.equals(Consts.ACCESSTYPE_OWNER_PROPERTY)){
            data = QRFormatter.getOwnerPropertyQRData(sector.getRootId(), getActivity().getApplicationContext());
        }else if(accessType.equals(Consts.ACCESSTYPE_INVITATION_PROPERTY)) {
            data = QRFormatter.getInvitationPropertyQRData(sector.getRootId(), getActivity().getApplicationContext());
        }else if(accessType.equals(Consts.ACCESSTYPE_OWNER_STUDENT)) {
            data =  "STUDENT".getBytes();
        }else if(accessType.equals(Consts.ACCESSTYPE_INVITATION_STUDENT)) {
            data =  "STUDENT".getBytes();
        }else if(accessType.equals(Consts.ACCESSTYPE_PARKING)) {
            data = QRFormatter.getParkingQRData(getActivity().getApplicationContext());
        }
        BitmapDrawable draw = QRCodeBitmapEncoder.getDrawable(data);
        qrCodeImageView.setImageDrawable(draw);
    }

    private void helpWifi(){
        if (!WifiHelper.isLocal()) {
            String title = getString(R.string.fragment_rcqr_dialog_title_request_wifi_local);
            String message = getString(R.string.fragment_rcqr_dialog_message_request_wifi_local);
            AlertDialog.Builder infoDialog = new AlertDialog.Builder(getActivity())
                    .setNegativeButton(R.string.fragment_rcqr_dialog_message_cancel_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {}})
                    .setPositiveButton(R.string.fragment_rcqr_dialog_message_accept_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            WifiHelper.connect(getActivity());
                        }})
                    .setTitle(Html.fromHtml(title))
                    .setMessage(Html.fromHtml(message))
                    .setCancelable(false);
            infoDialog.create().show();
        }
    }

    private static final int PERMISSIONS_CODE_LOCATION = 10002;
    private void helpGps() {

        if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    PERMISSIONS_CODE_LOCATION);
        }else{
            LocationManager locationManager = (LocationManager) getActivity()
                    .getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

            if ( LocationProvider.getInstance().getGpsStatus().equals(Consts.GPS_NO_ACTIVE)
                    || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                String title = getString(R.string.fragment_rcqr_dialog_title_request_gps);
                String message = getString(R.string.fragment_rcqr_dialog_message_request_gps);
                AlertDialog.Builder infoDialog = new AlertDialog.Builder(getActivity())
                        .setNegativeButton(R.string.fragment_rcqr_dialog_message_cancel_button2, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .setPositiveButton(R.string.fragment_rcqr_dialog_message_accept_button2, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                LocationProvider.getInstance().activateGps(getActivity());
                            }
                        })
                        .setTitle(Html.fromHtml(title))
                        .setMessage(Html.fromHtml(message))
                        .setCancelable(false);
                infoDialog.create().show();
            }
        }
    }

    private void updateWifiStatus() {
        if(!isAdded()){
           return;
        }

        String wfiLocal = getString(R.string.fragment_rcqr_wifi_local);
        String wfiLocalOk = getString(R.string.fragment_rcqr_wifi_local_ok);
        String wfiLocalConnect = getString(R.string.fragment_rcqr_wifi_local_connect);

        TextView wifiStatus = (TextView) rootView.findViewById(R.id.wifi_status);
        if (WifiHelper.isLocal()) {
            final SpannableStringBuilder sb = new SpannableStringBuilder(String.format("%s: %s", wfiLocal, wfiLocalOk));
            ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(255, 255, 255));
            sb.setSpan(fcs, 0, wfiLocal.length() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            fcs = new ForegroundColorSpan(Color.rgb(54, 142, 58));
            sb.setSpan(fcs, wfiLocal.length() + 2, sb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            wifiStatus.setText(sb);
            wifiStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            final SpannableStringBuilder sb = new SpannableStringBuilder(String.format("%s: %s", wfiLocal, wfiLocalConnect));
            ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(255, 255, 255));
            sb.setSpan(fcs, 0, wfiLocal.length() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            fcs = new ForegroundColorSpan(Color.rgb(247, 202, 22));
            sb.setSpan(fcs, wfiLocal.length() + 2, sb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            wifiStatus.setText(sb);
            wifiStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_help, 0);
        }
    }

    private void updateGpsStatus() {
        if(!isAdded()){
            return;
        }

        String gps = getString(R.string.fragment_rcqr_gps);
        String gpsNoPermission = getString(R.string.fragment_rcqr_gps_no_permission);
        String gpsLocating = getString(R.string.fragment_rcqr_gps_locating);
        String gpsOk = getString(R.string.fragment_rcqr_gps_ok);
        String gpsActivate = getString(R.string.fragment_rcqr_gps_activate);

        if("debug".equals(BuildConfig.BUILD_TYPE)){
            debugInfo.setVisibility(View.VISIBLE);
            debugInfo.setText(LocationProvider.getInstance().getCoordinatesListString());
        }

        String status = LocationProvider.getInstance().getGpsStatus();
        TextView gpsStatus = (TextView) rootView.findViewById(R.id.gps_status);
        if (status.equals(Consts.GPS_NO_PERMISSION)) {
            final SpannableStringBuilder sb = new SpannableStringBuilder(String.format("%s: %s", gps, gpsNoPermission));
            ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(255, 255, 255));
            sb.setSpan(fcs, 0, gps.length() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            fcs = new ForegroundColorSpan(Color.rgb(240, 82, 74));
            sb.setSpan(fcs, gps.length() + 2, sb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            gpsStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_help, 0);
            gpsStatus.setText(sb);
        } else if (status.equals(Consts.GPS_LOCATING)) {
            final SpannableStringBuilder sb = new SpannableStringBuilder(String.format("%s: %s", gps, gpsLocating));
            ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(255, 255, 255));
            sb.setSpan(fcs, 0, gps.length() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            fcs = new ForegroundColorSpan(Color.rgb(247, 202, 22));
            sb.setSpan(fcs, gps.length() + 2, sb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            gpsStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            gpsStatus.setText(sb);
        } else if (status.equals(Consts.GPS_OK)) {
            final SpannableStringBuilder sb = new SpannableStringBuilder(String.format("%s: %s", gps, gpsOk));
            ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(255, 255, 255));
            sb.setSpan(fcs, 0, gps.length() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            fcs = new ForegroundColorSpan(Color.rgb(54, 142, 58));
            sb.setSpan(fcs, gps.length() + 2, sb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            gpsStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            gpsStatus.setText(sb);
        } else {
            final SpannableStringBuilder sb = new SpannableStringBuilder(String.format("%s: %s", gps, gpsActivate));
            ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(255, 255, 255));
            sb.setSpan(fcs, 0, gps.length() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            fcs = new ForegroundColorSpan(Color.rgb(240, 82, 74));
            if (WifiHelper.isLocal())
                fcs = new ForegroundColorSpan(Color.rgb(247, 202, 22));
            sb.setSpan(fcs, gps.length() + 2, sb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            gpsStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_help, 0);
            gpsStatus.setText(sb);
        }
    }
}