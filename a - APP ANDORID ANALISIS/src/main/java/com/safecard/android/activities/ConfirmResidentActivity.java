package com.safecard.android.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.safecard.android.Config;
import com.safecard.android.R;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ConfirmResidentActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private String TAG = "ConfirmResidentActivity";
    private TextView property, title;
    JSONObject property_json, login_json, resident;
    JSONArray sectors_property;
    private Button add_resident;

    String resident_name, resident_mobile = "", url_add_resident, url_block_unblock_resident, mobile, house_id, access_selected, sectors_selected_ids = "", house_name, resident_active;
    int not_allow_guests;
    Boolean can_block = true;
    Boolean new_resident = true;
    ArrayList<String> selectedItems = new ArrayList<String>();

    private Boolean is_admin = false, is_permanent = false, can_view_logs = false, can_send_invitations = false, heritance = false, is_admin_user = false;
    int total_sectors;
    private SwitchCompat admin_switch, permanent_switch, view_logs, send_invitation;
    private ProgressDialog pDialog;
    private Toolbar toolbar;
    private AlertDialog.Builder alertDialog;

    private LinearLayout sectors_select_lay;
    private LinearLayout contact_select_lay;
    private LinearLayout start_access_lay, end_access_lay;
    private LinearLayout resident_data_new, resident_data_edit, block_unblock_resident;
    private LinearLayout can_invite_lay, permanent_switch_lay, view_logs_lay, resident_admin_lay;

    private TextView sectors_selected_tv;
    private TextView new_resident_name_tv, new_resident_mobile_tv, edit_resident_name_tv, edit_resident_mobile_tv, resident_admin_info, edit_resident_status_tv;
    private TextView block_unblock_tv;
    private TextView help_can_invite, help_time_sector;
    private ImageView block_unblock_img;
    private TextView label_start_date, label_end_date;
    private View separator_resident_admin, separator_sector;

    private EditText start_date, end_date;
    int mYearStart, mMonthStart, mDayStart, mYearEnd, mMonthEnd, mDayEnd;
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final String DATE_FORMAT_LABEL = "dd MMM yyyy";

    public static int PICK_SECTOR = 0;
    public static int PICK_CONTACT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_resident);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_shape);

        title = (TextView) toolbar.findViewById(R.id.toolbar_title);

        add_resident = (Button) findViewById(R.id.add_resident);

        new_resident_name_tv = (TextView) findViewById(R.id.new_resident_name);
        new_resident_mobile_tv = (TextView) findViewById(R.id.new_resident_mobile);

        edit_resident_name_tv = (TextView) findViewById(R.id.edit_resident_name);
        edit_resident_mobile_tv = (TextView) findViewById(R.id.edit_resident_mobile);
        edit_resident_status_tv = (TextView) findViewById(R.id.edit_resident_status);

        sectors_select_lay = (LinearLayout) findViewById(R.id.sectors_select_lay);
        contact_select_lay = (LinearLayout) findViewById(R.id.contact_select_lay);
        start_access_lay = (LinearLayout) findViewById(R.id.start_access_lay);
        end_access_lay = (LinearLayout) findViewById(R.id.end_access_lay);

        resident_data_new = (LinearLayout) findViewById(R.id.resident_data_new);
        resident_data_edit = (LinearLayout) findViewById(R.id.resident_data_edit);
        block_unblock_resident = (LinearLayout) findViewById(R.id.block_unblock_resident);

        block_unblock_tv = (TextView) findViewById(R.id.block_unblock_tv);
        block_unblock_img = (ImageView) findViewById(R.id.block_unblock_img);

        resident_admin_lay = (LinearLayout) findViewById(R.id.resident_admin_lay);
        resident_admin_info = (TextView) findViewById(R.id.resident_admin_info);
        help_time_sector = (TextView) findViewById(R.id.help_time_sector);
        separator_resident_admin = findViewById(R.id.separator_resident_admin);
        separator_sector = findViewById(R.id.separator_sector);

        sectors_selected_tv = (TextView) findViewById(R.id.sectors_selected);

        can_invite_lay = (LinearLayout) findViewById(R.id.can_invite_lay);
        permanent_switch_lay = (LinearLayout) findViewById(R.id.permanent_switch_lay);
        view_logs_lay = (LinearLayout) findViewById(R.id.view_logs_lay);
        help_can_invite = (TextView) findViewById(R.id.help_can_invite);

        alertDialog = new AlertDialog.Builder(ConfirmResidentActivity.this);

        Bundle extras = getIntent().getExtras();
        access_selected = extras.getString("access_selected");

        pDialog = new ProgressDialog(ConfirmResidentActivity.this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setCancelable(false);

        property = (TextView) findViewById(R.id.property);

        admin_switch = (SwitchCompat) findViewById(R.id.admin_switch);
        admin_switch.setOnCheckedChangeListener(this);

        permanent_switch = (SwitchCompat) findViewById(R.id.permanent_switch);
        permanent_switch.setOnCheckedChangeListener(this);

        view_logs = (SwitchCompat) findViewById(R.id.view_logs);
        view_logs.setOnCheckedChangeListener(this);

        send_invitation = (SwitchCompat) findViewById(R.id.send_invitation);
        send_invitation.setOnCheckedChangeListener(this);

        label_start_date = (TextView) findViewById(R.id.label_start_date);
        label_end_date = (TextView) findViewById(R.id.label_end_date);
        start_date = (EditText) findViewById(R.id.start_date);
        end_date = (EditText) findViewById(R.id.end_date);

        try {
            login_json = new JSONObject(Utils.getDefaults("login", getApplicationContext()));
            mobile = login_json.getString("mobile");
            property_json = new JSONObject(access_selected);

            house_id = property_json.getString("house_id");
            house_name = property_json.getString("house_name");
            not_allow_guests = property_json.getInt("not_allow_guests");
            property.setText(property_json.getString("house_name"));

            sectors_property = new JSONArray(property_json.getString("barriers"));
            total_sectors = sectors_property.length();

            heritance = extras.getBoolean("heritance");
            is_admin_user = extras.getBoolean("is_admin");
            new_resident = extras.getBoolean("new_resident");

            resident_admin_lay.setVisibility(heritance && !is_admin_user? View.VISIBLE : View.GONE);
            resident_admin_info.setVisibility(heritance && !is_admin_user? View.VISIBLE : View.GONE);
            separator_resident_admin.setVisibility(heritance && !is_admin_user? View.VISIBLE : View.GONE);

            can_invite_lay.setVisibility(not_allow_guests == 1 ? View.GONE : View.VISIBLE);
            help_can_invite.setVisibility(not_allow_guests == 1 ? View.GONE : View.VISIBLE);

            if(sectors_property.length() <= 1){
                separator_sector.setVisibility(View.GONE);
                sectors_select_lay.setVisibility(View.GONE);
                help_time_sector.setText(R.string.confirm_resident_explanation_time_sector);
            }

            if (new_resident.equals(true)){
                title.setText(R.string.confirm_resident_title_new_resident);

                selectAllSectors();

                /* Set Current date */

                Calendar calStart = Calendar.getInstance();
                Calendar calEnd = Calendar.getInstance();
                calEnd.add(Calendar.MONTH, 1);

                mYearStart = calStart.get(Calendar.YEAR);
                mMonthStart =  calStart.get(Calendar.MONTH);
                mDayStart = calStart.get(Calendar.DAY_OF_MONTH);

                mYearEnd = calEnd.get(Calendar.YEAR);
                mMonthEnd =  calEnd.get(Calendar.MONTH);
                mDayEnd = calEnd.get(Calendar.DAY_OF_MONTH);

                updateDateDisplay(start_date, label_start_date, mYearStart, mMonthStart, mDayStart);
                updateDateDisplay(end_date, label_end_date, mYearEnd, mMonthEnd, mDayEnd);

                send_invitation.setChecked(not_allow_guests == 1 ? false : true);
                permanent_switch.setChecked(true);

            } else {
                resident_data_edit.setVisibility(View.VISIBLE);
                contact_select_lay.setVisibility(View.GONE);

                resident = new JSONObject(extras.getString("resident"));
                resident_name = resident.getString("name");
                resident_mobile = resident.getString("mobile");
                resident_active = resident.getString("active");

                edit_resident_name_tv.setText(resident_name);
                edit_resident_mobile_tv.setText(resident_mobile);

                if(resident_active.equals("1")){
                    can_block = true;
                    block_unblock_tv.setText(R.string.confirm_resident_block);
                    //edit_resident_name_tv.setPaintFlags(edit_resident_name_tv.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                    //edit_resident_mobile_tv.setPaintFlags(edit_resident_mobile_tv.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                    block_unblock_img.setColorFilter(Color.parseColor("#FFFFFF"));
                    edit_resident_status_tv.setVisibility(View.GONE);
                } else {
                    can_block = false;
                    block_unblock_tv.setText(R.string.confirm_resident_enable);
                    //edit_resident_name_tv.setPaintFlags(edit_resident_name_tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    //edit_resident_mobile_tv.setPaintFlags(edit_resident_mobile_tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    block_unblock_img.setColorFilter(Color.parseColor("#F0524A"));
                    edit_resident_status_tv.setText(R.string.confirm_resident_resident_blocked);
                    edit_resident_status_tv.setTextColor(Color.parseColor("#F0524A"));
                }

                title.setText(R.string.confirm_resident_edit_resident);
                add_resident.setText(R.string.confirm_resident_save);

                if(resident.getString("in_effect_since").equals("") || resident.getString("in_effect_until").equals("")){
                    permanent_switch.setChecked(true);

                    Calendar calStart = Calendar.getInstance();
                    Calendar calEnd = Calendar.getInstance();
                    calEnd.add(Calendar.MONTH, 1);

                    mYearStart = calStart.get(Calendar.YEAR);
                    mMonthStart =  calStart.get(Calendar.MONTH);
                    mDayStart = calStart.get(Calendar.DAY_OF_MONTH);

                    mYearEnd = calEnd.get(Calendar.YEAR);
                    mMonthEnd =  calEnd.get(Calendar.MONTH);
                    mDayEnd = calEnd.get(Calendar.DAY_OF_MONTH);

                    updateDateDisplay(start_date, label_start_date, mYearStart, mMonthStart, mDayStart);
                    updateDateDisplay(end_date, label_end_date, mYearEnd, mMonthEnd, mDayEnd);

                } else {
                    SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date since = dt.parse(resident.getString("in_effect_since"));
                    Date until = dt.parse(resident.getString("in_effect_until"));

                    Calendar calStart = Calendar.getInstance();
                    calStart.setTime(since);
                    Calendar calEnd = Calendar.getInstance();
                    calEnd.setTime(until);

                    mYearStart = calStart.get(Calendar.YEAR);
                    mMonthStart =  calStart.get(Calendar.MONTH);
                    mDayStart = calStart.get(Calendar.DAY_OF_MONTH);

                    mYearEnd = calEnd.get(Calendar.YEAR);
                    mMonthEnd =  calEnd.get(Calendar.MONTH);
                    mDayEnd = calEnd.get(Calendar.DAY_OF_MONTH);

                    updateDateDisplay(start_date, label_start_date, mYearStart, mMonthStart, mDayStart);
                    updateDateDisplay(end_date, label_end_date, mYearEnd, mMonthEnd, mDayEnd);

                }

                if(resident.getString("all_logs").equals("1")){
                    view_logs.setChecked(true);
                }

                if(resident.getString("invitations").equals("1")){
                    send_invitation.setChecked(true);
                }

                if(resident.getInt("admin") == 1 || resident.getInt("owner") == 1){
                    admin_switch.setChecked(true);
                }

                int sectors_resident = 0;
                String sectors_selected_names = "";
                sectors_selected_ids = "";
                JSONArray residentSectors = resident.getJSONArray("sectors");

                for(int j = 0; j < sectors_property.length(); j++){
                    int propertySectorId = sectors_property.getJSONObject(j).getInt("sector_id");

                    boolean hasSector = false;
                    for(int k = 0; k < residentSectors.length(); k++){
                        if (residentSectors.getInt(k) == propertySectorId){
                            hasSector = true;
                            break;
                        }
                    }

                    if (hasSector){
                        selectedItems.add(j, "true");
                        sectors_selected_ids += sectors_property.getJSONObject(j).getString("sector_id") + ",";
                        sectors_selected_names += sectors_property.getJSONObject(j).getString("sector_name") + ", ";
                        sectors_resident++;
                    } else{
                        selectedItems.add(j, "false");
                    }
                }

                if(!sectors_selected_ids.equals("")){
                    sectors_selected_ids = sectors_selected_ids.substring(0, sectors_selected_ids.length() - 1);
                    sectors_selected_names = sectors_selected_names.substring(0, sectors_selected_names.length() - 2);

                    if(sectors_resident == sectors_property.length()){
                        sectors_selected_tv.setText(R.string.confirm_resident_all);
                    } else {
                        sectors_selected_tv.setText(sectors_selected_names);
                    }
                } else {
                    sectors_selected_tv.setText(R.string.confirm_resident_noone);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        add_resident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addResident();
            }
        });

        block_unblock_resident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStatusResident(can_block);
            }
        });

        sectors_select_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sectors_intent = new Intent(getApplicationContext(), SectorsActivity.class);
                try {
                    if(is_admin){
                        Utils.showToast(getApplicationContext(), getString(R.string.confirm_resident_resident_admin_has_access_to_all_sectors));
                    } else {
                        sectors_intent.putExtra("SECTORS", property_json.getString("barriers"));
                        sectors_intent.putExtra("SELECTED_ITEMS", selectedItems);
                        startActivityForResult(sectors_intent, PICK_SECTOR);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        contact_select_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contact_intent = new Intent(getApplicationContext(), ContactResidentActivity.class);
                startActivityForResult(contact_intent, PICK_CONTACT);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        resident_data_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contact_intent = new Intent(getApplicationContext(), ContactResidentActivity.class);
                startActivityForResult(contact_intent, PICK_CONTACT);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        final DatePickerDialog.OnDateSetListener onDateSetListenerStart = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                mYearStart = year;
                mMonthStart = monthOfYear;
                mDayStart = dayOfMonth;
                updateDateDisplay(start_date, label_start_date, mYearStart, mMonthStart, mDayStart);
            }
        };

        final DatePickerDialog.OnDateSetListener onDateSetListenerEnd = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                mYearEnd = year;
                mMonthEnd = monthOfYear;
                mDayEnd = dayOfMonth;
                updateDateDisplay(end_date, label_end_date, mYearEnd, mMonthEnd, mDayEnd);
            }
        };

        /*Dates input*/
        start_access_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ConfirmResidentActivity.this, onDateSetListenerStart,
                        mYearStart, mMonthStart, mDayStart).show();
            }
        });

        end_access_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ConfirmResidentActivity.this, onDateSetListenerEnd,
                        mYearEnd, mMonthEnd, mDayEnd).show();
            }
        });

        /*Switches toogle*/
        view_logs_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_logs.toggle();
            }
        });

        resident_admin_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                admin_switch.toggle();
            }
        });

        permanent_switch_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permanent_switch.toggle();
            }
        });

        can_invite_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_invitation.toggle();
            }
        });
    }

    public void addResident(){
        try {
            alertDialog.setTitle(R.string.confirm_resident_title_resident);

            Date in_effect_since_aux, in_effect_until_aux;
            Date today = new Date();

            in_effect_since_aux = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(start_date.getText().toString() + " 00:00:00");
            in_effect_until_aux = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(end_date.getText().toString() + " 23:59:59");

            String admin = is_admin ? "1" : "0";
            String logs = can_view_logs ? "1" : "0";
            String send_invitations = not_allow_guests == 1 ? "0" : can_send_invitations ? "1" : "0";

            if(sectors_selected_ids.equals("")){
                sectors_selected_ids = "_";
            }

            SimpleDateFormat formatwanted = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String in_effect_since = formatwanted.format(in_effect_since_aux);
            String in_effect_until = formatwanted.format(in_effect_until_aux);

            if(resident_mobile.equals("")){
                alertDialog.setMessage(R.string.confirm_resident_select_a_contact)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                alertDialog.show();
            } else if (!is_permanent && (in_effect_until_aux.before(today) || in_effect_until_aux.equals(today))) {
                alertDialog.setMessage(R.string.confirm_resident_to_date_has_to_be_bigger_than_current)
                        .setCancelable(false)
                        .setPositiveButton(R.string.confirm_resident_ok1, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                alertDialog.show();
            } else if (!is_permanent && (in_effect_since_aux.after(in_effect_until_aux) || in_effect_since.equals(in_effect_until_aux))) {
                alertDialog.setMessage(R.string.confirm_resident_from_date_has_to_be_smaller_than_to_date)
                        .setCancelable(false)
                        .setPositiveButton(R.string.confirm_resident_ok2, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                alertDialog.show();
            } else {
                pDialog.setMessage(new_resident.equals(true) ? getString(R.string.confirm_resident_adding_resident) : getString(R.string.confirm_resident_updating_resident));
                pDialog.show();
                if(!is_permanent){
                    url_add_resident = Config.ApiUrl + "add_resident/" + mobile + "/" + resident_mobile + "/" + house_id + "/" + admin + "/" + URLEncoder.encode(in_effect_since, "UTF-8").replaceAll("\\+", "%20") + "/" + URLEncoder.encode(in_effect_until, "UTF-8").replaceAll("\\+", "%20") + "/" + sectors_selected_ids + "/" + logs + "/" + send_invitations;
                } else {
                    url_add_resident = Config.ApiUrl + "add_resident/" + mobile + "/" + resident_mobile + "/" + house_id + "/" + admin + "/_/_/" + sectors_selected_ids + "/" + logs + "/" + send_invitations;
                }

                Log.i(TAG,"URL: " + url_add_resident);

                RequestVolley rqv = new RequestVolley(url_add_resident, getApplicationContext());
                rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        Utils.safeDismissDialog(ConfirmResidentActivity.this, pDialog);
                        try {
                            if(response.getString("result").equals("ACK")){
                                Utils.showToast(getApplicationContext(),new_resident.equals(true) ? getString(R.string.confirm_resident_resident_successfully_created) : getString(R.string.confirm_resident_resident_successfully_updated));
                            } else {
                                Utils.showToast(getApplicationContext(),response.getString("msg"));
                            }
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(String errorType, String msg) {
                        if (!msg.equals("")) {
                            Utils.showToast(getApplicationContext(), msg);
                        }
                        Utils.safeDismissDialog(ConfirmResidentActivity.this, pDialog);
                    }
                });
            }

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void selectAllSectors(){
        try {
            sectors_selected_ids = "";
            for(int i = 0; i < sectors_property.length(); i++){
                //Por default: todos seleccionados
                selectedItems.add(i, "true");
                sectors_selected_ids += sectors_property.getJSONObject(i).getString("sector_id") + ",";
            }

            if(!sectors_selected_ids.equals("")){
                sectors_selected_ids = sectors_selected_ids.substring(0, sectors_selected_ids.length() - 1);
                sectors_selected_tv.setText(R.string.confirm_resident_sectors_all);
            } else {
                sectors_selected_tv.setText(R.string.confirm_resident_no_sectors);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void changeStatusResident(final Boolean block){

        alertDialog.setTitle(R.string.confirm_resident_popup_title_confirm);
        String positive_btn_text;

        if(block){
            String question = String.format( getString(R.string.confirm_resident_popup_question_block), resident_name);
            String questionComplement = getString(R.string.confirm_resident_popup_question_block_complement);

            alertDialog.setMessage(Html.fromHtml(String.format("<b>%s</b><br><br>%s", question, questionComplement)));
            url_block_unblock_resident = Config.ApiUrl + "resident_block/" + mobile + "/" + resident_mobile + "/" + house_id;
            positive_btn_text = getString(R.string.confirm_resident_popup_btn_block);
        } else {
            String question = String.format( getString(R.string.confirm_resident_popup_question_enable), resident_name);
            String questionComplement = getString(R.string.confirm_resident_popup_question_enable_complement);

            alertDialog.setMessage(Html.fromHtml(String.format("<b>%s</b><br><br>%s", question, questionComplement)));
            url_block_unblock_resident = Config.ApiUrl + "resident_unblock/" + mobile + "/" + resident_mobile + "/" + house_id;
            positive_btn_text = getString(R.string.confirm_resident_popup_btn_enable);
        }

        alertDialog.setPositiveButton(positive_btn_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                pDialog.setMessage(block ? getString(R.string.confirm_resident_blocking_resident) :  getString(R.string.confirm_resident_enabling_resident));
                pDialog.show();

                //Log.i(TAG,"URL REMOVE RESIDENT: " + url_block_unblock_resident);

                RequestVolley rqv = new RequestVolley(url_block_unblock_resident, getApplicationContext());
                rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        if (pDialog != null && pDialog.isShowing()) {
                            Utils.safeDismissDialog(ConfirmResidentActivity.this, pDialog);
                        }
                        try {
                            if(response.getString("result").equals("ACK")){

                                if(block.equals(true)){
                                    can_block = false;

                                    block_unblock_tv.setText(R.string.confirm_resident_enable2);
                                    block_unblock_img.setColorFilter(Color.parseColor("#F0524A"));

                                    edit_resident_status_tv.setVisibility(View.VISIBLE);
                                    edit_resident_status_tv.setText(R.string.confirm_resident_resident_blocked2);
                                    edit_resident_status_tv.setTextColor(Color.parseColor("#F0524A"));

                                    Utils.showToast(getApplicationContext(), getString(R.string.confirm_resident_resident_blocked3));
                                } else {
                                    can_block = true;

                                    block_unblock_tv.setText(R.string.confirm_resident_block2);
                                    block_unblock_img.setColorFilter(Color.parseColor("#FFFFFF"));

                                    edit_resident_status_tv.setVisibility(View.GONE);

                                    Utils.showToast(getApplicationContext(), getString(R.string.confirm_resident_resident_enabled));
                                }

                            } else {
                                Utils.showToast(getApplicationContext(), response.getString("msg"));
                            }

                            /*Intent access_intent = new Intent(getApplicationContext(), AccessActivity.class);
                            access_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(access_intent);
                            finish();*/

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(String errorType, String msg) {
                        if (pDialog != null && pDialog.isShowing()) {
                            Utils.safeDismissDialog(ConfirmResidentActivity.this, pDialog);
                        }
                    }

                });
            }
        });

        alertDialog.setNegativeButton(R.string.confirm_resident_btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.admin_switch:
                is_admin = isChecked;
                if(isChecked){
                    selectAllSectors();
                }
                break;
            case R.id.permanent_switch:
                start_access_lay.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                end_access_lay.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                is_permanent = isChecked;
                break;
            case R.id.view_logs:
                can_view_logs = isChecked;
                break;
            case R.id.send_invitation:
                can_send_invitations = isChecked;
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_SECTOR) {
            if (resultCode == ConfirmResidentActivity.RESULT_OK) {
                try {
                    selectedItems.clear();
                    selectedItems = data.getExtras().getStringArrayList("SELECTED_ITEMS");

                    String txt_sectors_selected = "";
                    sectors_selected_ids = "";
                    int total_selected = 0;
                    for (int i = 0; i < selectedItems.size(); i++) {
                        if (selectedItems.get(i).equals("true")) {
                            total_selected++;
                            txt_sectors_selected += sectors_property.getJSONObject(i).getString("sector_name") + ", ";
                            sectors_selected_ids += sectors_property.getJSONObject(i).getString("sector_id") + ",";
                        }
                    }

                    if (total_selected == total_sectors) {
                        sectors_selected_tv.setText(R.string.confirm_resident_sectors_all2);
                    } else {
                        sectors_selected_tv.setText(txt_sectors_selected.substring(0, txt_sectors_selected.length() - 2));
                    }
                    sectors_selected_ids = sectors_selected_ids.substring(0, sectors_selected_ids.length() - 1);

                    if (new_resident.equals(false)) {
                        Utils.showToast(getApplicationContext(), getString(R.string.confirm_resident_press_save_explanation));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == PICK_CONTACT) {

            if (resultCode == ContactsActivity.RESULT_OK) {
                resident_data_new.setVisibility(View.VISIBLE);
                contact_select_lay.setVisibility(View.GONE);
                new_resident_name_tv.setText(data.getStringExtra("resident_name"));
                new_resident_mobile_tv.setText(data.getStringExtra("resident_mobile"));
                resident_mobile = data.getStringExtra("resident_mobile");
            }
        }
    }

    /** Updates the date in the TextView */
    private void updateDateDisplay(EditText input, TextView label, int y, int m, int d) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(y, m, d);

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_LABEL);
        label.setText(dateFormat.format(calendar.getTime()));

        dateFormat = new SimpleDateFormat(DATE_FORMAT);
        input.setText(dateFormat.format(calendar.getTime()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // java.lang.IllegalArgumentException: View not attached to window manager
        Utils.safeDismissDialog(ConfirmResidentActivity.this, pDialog);
    }
}
