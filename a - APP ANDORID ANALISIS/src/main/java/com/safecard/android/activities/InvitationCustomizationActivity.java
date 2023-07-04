package com.safecard.android.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.utils.Invitation;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InvitationCustomizationActivity extends AppCompatActivity {
    private static final String TAG = "InvitationCustomAct";

    public static final String NAMES = "NAMES";
    public static final String TITLE = "TITLE";
    public static final String INVITATION = "INVITATION";
    public static final String PROPERTIES = "PROPERTIES";
    public static final String ORGANIZER_MOBILE = "ORGANIZER_MOBILE";
    public static final String SUMMARY_TYPE = "SUMMARY_TYPE";

    public static final String TIME_MODE_ONE_DAY = "TIME_MODE_ONE_DAY";
    public static final String TIME_MODE_MANY_DAYS = "TIME_MODE_MANY_DAYS";

    Invitation invitation;
    ArrayList<String> guestNames;

    private EditText plateNumber;
    private TextInputLayout mInputPlate;
    SwitchCompat customTimeSelectorSwitch, customDaysOfWeekSelectionSwitch, plateSwitch;

    LinearLayout days_label, label_times, repeat_days_layout, starttime_container, endtime_container, startdate_container, enddate_container;
    LinearLayout sectors_lay, plate_help_lay, plate_lay;

    int[] dayOfWeekIds;
    String[] dayOfWeekNames;

    private TextView[] dayOfWeekButtons;

    private TextView label_start_date, label_end_date, label_start_time, label_end_time, property_name, condo_name, label_sectors;

    View time_separator, date_separator, days_separator, sectors_separator, plate_separator;

    public static int PICK_PROPERTIES = 0;
    public static int PICK_SECTORS = 1;

    private boolean endDateIsUnclicked = true;
    private boolean endTimeIsUnclicked = true;

    private List<Property> properties;
    private String organizerMobile;
    private String summaryType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_customization);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_shape);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        TextInputEditText invitationSubject = findViewById(R.id.invitation_subject);

        invitationSubject.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                invitation.setName(s.toString());
            }
        });

        property_name = (TextView) findViewById(R.id.property_name);
        condo_name = (TextView) findViewById(R.id.condo_name);

        label_start_date = (TextView) findViewById(R.id.label_start_date);
        label_end_date = (TextView) findViewById(R.id.label_end_date);
        label_start_time = (TextView) findViewById(R.id.label_start_time);
        label_end_time = (TextView) findViewById(R.id.label_end_time);
        label_sectors = (TextView) findViewById(R.id.label_sectors);

        dayOfWeekNames = new String[]{
                getString(R.string.invitation_customization_day_sunday),
                getString(R.string.invitation_customization_day_monday),
                getString(R.string.invitation_customization_day_tuesday),
                getString(R.string.invitation_customization_day_wednesday),
                getString(R.string.invitation_customization_day_thursday),
                getString(R.string.invitation_customization_day_friday),
                getString(R.string.invitation_customization_day_saturday)
        };
        
        final Intent intent = getIntent();

        organizerMobile = intent.getStringExtra(ORGANIZER_MOBILE);
        guestNames = intent.getStringArrayListExtra(NAMES);

        summaryType = intent.getStringExtra(SUMMARY_TYPE);
        if(summaryType == null) {
            summaryType = Consts.SUMMARY_TYPE_TO;
        }

        properties = new ArrayList<>();
        Serializable props = intent.getSerializableExtra(PROPERTIES);
        if(props instanceof ArrayList){
            properties = (ArrayList) props;
        }

        Serializable inv = intent.getSerializableExtra(INVITATION);
        if(inv instanceof Invitation){
            invitation = (Invitation) inv;
        }

        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText("[Title]");
        String titleText = intent.getStringExtra(TITLE);
        if(titleText != null){
            title.setText(titleText);
        }

        mInputPlate = (TextInputLayout) findViewById(R.id.input_plate);
        plateNumber = (EditText) findViewById(R.id.plateNumber);

        //plateNumber.addTextChangedListener(new GenericTextWatcher(plateNumber));
        plateNumber.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String plate = s.toString();
                        if(plate.equals("")){
                            mInputPlate.setError(getString(R.string.invitation_customization_required_field));
                            mInputPlate.setErrorEnabled(true);
                        } else if (isPlateValid(plate)){
                            mInputPlate.setError(null);
                            mInputPlate.setErrorEnabled(false);
                        } else {
                            mInputPlate.setError(getString(R.string.invitation_customization_plate_format_not_valid));
                            mInputPlate.setErrorEnabled(true);
                        }
                        invitation.setPlateNumber(plate);
                        updateSummary();
                    }
                });

        customTimeSelectorSwitch = findViewById(R.id.all_day);
        customDaysOfWeekSelectionSwitch = findViewById (R.id.repeat_days);
        plateSwitch = findViewById(R.id.plate_switch);

        dayOfWeekIds = new int[]{
                R.id.sunday, R.id.monday, R.id.tuesday, R.id.wednesday,
                R.id.thursday, R.id.friday, R.id.saturday
        };

        dayOfWeekButtons = new TextView[7];
        for(int i = 0; i < 7; i++){
            dayOfWeekButtons[i] = findViewById(dayOfWeekIds[i]);
            dayOfWeekButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean[] daysOfWeekInPeriod = invitation.getDaysOfWeekInPeriod();
                    int index = getDayOfWeekIndexFromId(v.getId());

                    if(!daysOfWeekInPeriod[index]){
                        AlertDialog.Builder builder = new AlertDialog.Builder(InvitationCustomizationActivity.this);
                        builder.setTitle(R.string.invitation_customization_dialog_title);
                        builder.setMessage(String.format(getString(R.string.invitation_customization_dialog_day_is_not_in_date_range), dayOfWeekNames[index]))
                                .setCancelable(false)
                                .setPositiveButton(R.string.invitation_customization_dialog_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                        return;
                    }

                    invitation.toggleDayOfWeek(index);

                    updateDayOfWeekButtonsDisplay();
                    updateSummary();
                }
            });
        }

        days_label = (LinearLayout) findViewById(R.id.days_label);
        //input_time = (LinearLayout) findViewById(R.id.input_time);
        label_times = (LinearLayout) findViewById(R.id.label_times);

        starttime_container = (LinearLayout) findViewById(R.id.starttime_container);
        startdate_container = (LinearLayout) findViewById(R.id.startdate_container);

        endtime_container = (LinearLayout) findViewById(R.id.endtime_container);
        enddate_container = (LinearLayout) findViewById(R.id.enddate_container);

        plate_lay = (LinearLayout) findViewById(R.id.plate_lay);
        sectors_lay = (LinearLayout) findViewById(R.id.sectors_lay);
        plate_help_lay = (LinearLayout) findViewById(R.id.plate_help_lay);

        sectors_separator = findViewById(R.id.sectors_separator);

        time_separator = findViewById(R.id.time_separator);
        date_separator = findViewById(R.id.date_separator);
        days_separator = findViewById(R.id.days_separator);
        plate_separator = findViewById(R.id.plate_separator);
        
        repeat_days_layout = (LinearLayout) findViewById(R.id.repeat_days_layout);

        // Display the current date in the TextViews
        updateDateDisplay(label_start_date, invitation.getStartDateTimeCalendar());
        updateDateDisplay(label_end_date,  invitation.getEndDateTimeCalendar());
        updateTimeDisplay(label_start_time,  invitation.getStartDateTimeCalendar());
        updateTimeDisplay(label_end_time, invitation.getEndDateTimeCalendar());

        customTimeSelectorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                invitation.setCustomTime(!isChecked);
                updateCustomTimeSelectorView();
                updateSummary();
            }
        });

        customDaysOfWeekSelectionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                invitation.setCustomDaysOfWeek(!isChecked);
                updateDayOfWeekButtonsDisplay();
                updateCustomDaysOfWeekSelectionView();
                updateSummary();
            }
        });

        plateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Property property = getProperty(properties, invitation.getPropertyId());

                if(property.isPlateServiceAvailableForInvitations() && guestNames.size() == 1) {
                    invitation.setPlateNumberUsed(isChecked);
                }else{
                    invitation.setPlateNumberUsed(false);
                    plateSwitch.setChecked(false);
                }

                updatePlateSectionView();
                updateSummary();
            }
        });

        property_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PickPropertiesActivity.class);
                intent.putExtra(PickPropertiesActivity.PROPERTIES, (ArrayList) properties);
                ArrayList<Integer> tickedList = new ArrayList<>();
                tickedList.add(invitation.getPropertyId());
                intent.putExtra(PickPropertiesActivity.TICKED_PROPERTY_IDS, tickedList);
                startActivityForResult(intent, PICK_PROPERTIES);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        label_sectors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PickSectorsActivity.class);
                //Log.i(TAG, "getSelectedPropertySectors.size: "+ getSelectedPropertySectors().size());
                intent.putExtra(PickSectorsActivity.TITLE, getString(R.string.invitation_customization_pick_sectors_title));
                intent.putExtra(PickSectorsActivity.ARG_CARDINALITY, PickSectorsActivity.CARDINALITY_MANY);
                intent.putExtra(PickSectorsActivity.ARG_SECTORS, (ArrayList) getSelectedPropertySectors());
                intent.putExtra(PickSectorsActivity.SELECTED_SECTORS_ID, (ArrayList) invitation.getIdsSelectedSectors());
                startActivityForResult(intent, PICK_SECTORS);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        Button buttonContinue = findViewById(R.id.btnCrearInvitacion);
        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isInvitationValid())
                    return;

                Intent intent = new Intent();
                intent.putExtra(INVITATION, invitation);
                intent.putExtra(ORGANIZER_MOBILE, organizerMobile);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        final DatePickerDialog.OnDateSetListener onDateSetListenerStart = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                invitation.setStartDateCalendar(year, monthOfYear, dayOfMonth);
                updateDateDisplay(label_start_date, invitation.getStartDateTimeCalendar());

                if (onlyYearMonthDayComparator(invitation.getStartDateTimeCalendar(), invitation.getEndDateTimeCalendar()) > 0
                        || endDateIsUnclicked) {
                    Calendar start = invitation.getStartDateTimeCalendar();
                    invitation.setEndDateCalendar(start.get(Calendar.YEAR),
                            start.get(Calendar.MONTH),
                            start.get(Calendar.DAY_OF_MONTH));
                    updateDateDisplay(label_end_date, invitation.getEndDateTimeCalendar());
                }

                updateDayOfWeekButtonsDisplay();
                updateCustomDaysOfWeekSelectionSwitch();
                updateCustomDaysOfWeekSelectionView();
                updateSummary();
            }
        };

        final DatePickerDialog.OnDateSetListener onDateSetListenerEnd = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                invitation.setEndDateCalendar(year, monthOfYear, dayOfMonth);
                updateDateDisplay(label_end_date, invitation.getEndDateTimeCalendar());

                updateDayOfWeekButtonsDisplay();
                updateCustomDaysOfWeekSelectionSwitch();
                updateCustomDaysOfWeekSelectionView();
                updateSummary();
            }
        };

        final TimePickerDialog.OnTimeSetListener onTimeSetListenerStart = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                invitation.setStartTimeCalendar(hourOfDay, minute);
                updateTimeDisplay(label_start_time, invitation.getStartDateTimeCalendar());

                Calendar start = invitation.getStartDateTimeCalendar();
                Calendar end = invitation.getEndDateTimeCalendar();

                boolean startTimeIsAfterEndTime = onlyHourMinuteComparator(start, end) > 0;

                if(start.get(Calendar.HOUR_OF_DAY) < 22 && (startTimeIsAfterEndTime || endTimeIsUnclicked)){
                    invitation.setEndTimeCalendar(start.get(Calendar.HOUR_OF_DAY) + 2, start.get(Calendar.MINUTE));
                    updateTimeDisplay(label_end_time, invitation.getEndDateTimeCalendar());
                }

                updateSummary();
            }
        };

        final TimePickerDialog.OnTimeSetListener onTimeSetListenerEnd = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                invitation.setEndTimeCalendar(hourOfDay, minute);
                updateTimeDisplay(label_end_time, invitation.getEndDateTimeCalendar());

                updateSummary();
            }
        };

        /*Dates input*/
        startdate_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar start = invitation.getStartDateTimeCalendar();
                int y =  start.get(Calendar.YEAR);
                int m =  start.get(Calendar.MONTH);
                int d =  start.get(Calendar.DAY_OF_MONTH);
                new DatePickerDialog(InvitationCustomizationActivity.this, onDateSetListenerStart, y, m, d).show();
            }
        });

        enddate_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endDateIsUnclicked = false;
                Calendar end = invitation.getEndDateTimeCalendar();
                int y =  end.get(Calendar.YEAR);
                int m =  end.get(Calendar.MONTH);
                int d =  end.get(Calendar.DAY_OF_MONTH);
                new DatePickerDialog(InvitationCustomizationActivity.this, onDateSetListenerEnd, y, m, d).show();
            }
        });

        starttime_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar start = invitation.getStartDateTimeCalendar();
                int h =  start.get(Calendar.HOUR_OF_DAY);
                int m =  start.get(Calendar.MINUTE);
                new TimePickerDialog(InvitationCustomizationActivity.this, onTimeSetListenerStart, h, m, true).show();
            }
        });

        endtime_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTimeIsUnclicked = false;
                Calendar end = invitation.getEndDateTimeCalendar();
                int h =  end.get(Calendar.HOUR_OF_DAY);
                int m =  end.get(Calendar.MINUTE);
                new TimePickerDialog(InvitationCustomizationActivity.this, onTimeSetListenerEnd, h, m, true).show();
            }
        });


        label_times.setVisibility(View.VISIBLE);
        sectors_lay.setVisibility(View.VISIBLE);

        time_separator.setVisibility(View.VISIBLE);
        date_separator.setVisibility(View.VISIBLE);
        sectors_separator.setVisibility(View.VISIBLE);
        plate_separator.setVisibility(View.VISIBLE);

        plate_help_lay.setVisibility(View.VISIBLE);
        plate_lay.setVisibility(View.VISIBLE);

        customTimeSelectorSwitch.setChecked(!invitation.isCustomDaysOfWeek());
        customDaysOfWeekSelectionSwitch.setChecked(!invitation.isCustomDaysOfWeek());
        updateCustomDaysOfWeekSelectionSwitch();

        updatePropertyView();
        updateSectorsSectionView();
        updateDayOfWeekButtonsDisplay();
        updateCustomDaysOfWeekSelectionView();
        updateCustomTimeSelectorView();


        plateNumber.setText(invitation.getPlateNumber());
        updatePlateSwitchAndPlateNumberUsed();
        updatePlateSectionView();

        updateSummary();
    }

    private void updatePlateSwitchAndPlateNumberUsed() {
        Property property = getProperty(properties, invitation.getPropertyId());

        if(property.isPlateServiceAvailableForInvitations() && guestNames.size() == 1) {
            plateSwitch.setEnabled(true);
            plateSwitch.setChecked(invitation.isPlateNumberUsed());
        }else{
            plateSwitch.setEnabled(false);
            plateSwitch.setChecked(false);
            invitation.setPlateNumberUsed(false);
        }
    }

    private void updatePlateSectionView() {
        plateNumber.setVisibility(plateSwitch.isChecked() ? View.VISIBLE : View.GONE);
        mInputPlate.setVisibility(plateSwitch.isChecked() ? View.VISIBLE : View.GONE);
        plate_lay.setVisibility(View.VISIBLE);
        plate_help_lay.setVisibility(View.VISIBLE);
        plate_separator.setVisibility(View.VISIBLE);
        if(guestNames.size() != 1){
            plate_lay.setVisibility(View.GONE);
            plate_help_lay.setVisibility(View.GONE);
            plate_separator.setVisibility(View.GONE);
        }
    }

    private void updatePropertyView() {
        Property property = getProperty(properties, invitation.getPropertyId());
        property_name.setText(property.getName());
        condo_name.setText(property.getCondoName());
    }

    private void updateSectorsSectionView() {
        if(invitation.getIdsSelectedSectors().size() < 1){
            label_sectors.setText(R.string.invitation_customization_no_sector_selected);
        } else if (areAllSectorsSelected()) {
            label_sectors.setText(R.string.invitation_customization_all_sector_selected);
        } else {
            label_sectors.setText(getStringSelectedSectorNameList());
        }
    }

    private List<PickSectorsActivity.Sector> getSelectedSectors() {
        Property property = getProperty(properties, invitation.getPropertyId());
        List<Integer> idsSelected = invitation.getIdsSelectedSectors();
        List<PickSectorsActivity.Sector> response = new ArrayList<>();
        for(PickSectorsActivity.Sector s: property.getSectors()){
            if(idsSelected.contains(s.getId())){
                response.add(s);
            }
        }
        return response;
    }


    private String getStringSelectedSectorNameList() {
        StringBuilder result =  new StringBuilder();
        String separator = "";
        List<PickSectorsActivity.Sector> selectedSectors = getSelectedSectors();
        for(PickSectorsActivity.Sector s: selectedSectors){
            result.append(separator);
            result.append(s.getName());
            separator = ", ";
        }
        return result.toString();
    }

    private void updateCustomDaysOfWeekSelectionView() {
        days_label.setVisibility(invitation.isCustomDaysOfWeek() ? View.VISIBLE : View.GONE);
    }

    private void updateCustomTimeSelectorView() {
        starttime_container.setVisibility(invitation.isCustomTime() ? View.VISIBLE : View.GONE);
        endtime_container.setVisibility(invitation.isCustomTime() ? View.VISIBLE : View.GONE);
    }

    private int getDayOfWeekIndexFromId(int id){
        int i = 0;
        while(dayOfWeekIds[i] != id && i < 7){
            i++;
        }
        return i < 7 ? i : -1;
    }

    private void updateDayOfWeekButtonsDisplay() {
        boolean[] daysOfWeekInPeriod = invitation.getDaysOfWeekInPeriod();
        boolean[] daysOfWeekSelected = invitation.getDaysOfWeekSelected();
        for(int i = 0; i < dayOfWeekButtons.length; i++){
            if(!daysOfWeekInPeriod[i]){
                dayOfWeekButtons[i].setBackgroundResource(R.drawable.border_inactive);
            } else if(daysOfWeekSelected[i]){
                dayOfWeekButtons[i].setBackgroundResource(R.drawable.border_active);
            } else {
                dayOfWeekButtons[i].setBackgroundResource(R.drawable.border);
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PROPERTIES) {
            if (resultCode == RESULT_OK) {
                int propertyId = data.getExtras().getInt(PickPropertiesActivity.SELECTED_PROPERTY_ID);
                invitation.setPropertyId(propertyId);
                selectAllSectors(invitation, properties);
                updatePropertyView();
                updateSectorsSectionView();

                updatePlateSwitchAndPlateNumberUsed();
                updatePlateSectionView();

                updateSummary();
            }
        } else if (requestCode == PICK_SECTORS) {
            if (resultCode == RESULT_OK) {
                ArrayList<Integer> SectorIds = data.getExtras().getIntegerArrayList(PickSectorsActivity.SELECTED_SECTORS_ID);
                invitation.setIdsSelectedSectors(SectorIds);
                updateSectorsSectionView();
                updateSummary();
            }
        }
    }

    private boolean isInvitationValid() {

        boolean thereIsIssue = false;
        String message = "";

        boolean noDaysOfWeekValidsError = false;
        if(getTimeMode().equals(TIME_MODE_MANY_DAYS) && invitation.isCustomDaysOfWeek()) {
            if (invitation.getDaysOfWeekValids().size() == 0) {
                noDaysOfWeekValidsError = true;
            }
        }

        Calendar start = invitation.getStartDateTimeCalendar();
        Calendar end = invitation.getEndDateTimeCalendar();

        Property selectedProperty = getProperty(properties, invitation.getPropertyId());

        if (onlyYearMonthDayComparator(Calendar.getInstance(), start) > 0){
            thereIsIssue = true;
            message = getString(R.string.invitation_customization_validator_date_from_must_be_greater_or_equal_than_current_date);
        } else if (onlyYearMonthDayComparator(start, end) > 0){
            thereIsIssue = true;
            message = getString(R.string.invitation_customization_validator_date_to_must_be_greater_or_equal_than_date_from);
        } else if (invitation.isCustomTime() && onlyHourMinuteComparator(start, end) >= 0) {
            thereIsIssue = true;
            message = getString(R.string.invitation_customization_validator_time_to_must_be_greater_than_time_from);
        } else if (noDaysOfWeekValidsError) {
            thereIsIssue = true;
            message = getString(R.string.invitation_customization_validator_select_valid_days_of_week);
        } else if (selectedProperty.isPlateServiceAvailableForInvitations() && invitation.isPlateNumberUsed()
                && invitation.getPlateNumber().length() == 0){
            mInputPlate.setErrorEnabled(true);
            mInputPlate.setError(getString(R.string.invitation_customization_validator_plate_required));
            thereIsIssue = true;
            message = getString(R.string.invitation_customization_validator_must_add_plate_or_disable_switch);
        } else if(selectedProperty.isPlateServiceAvailableForInvitations()  && invitation.isPlateNumberUsed()
                && !isPlateValid(invitation.getPlateNumber())) {
            mInputPlate.setErrorEnabled(true);
            mInputPlate.setError(getString(R.string.invitation_customization_validator_plate_format_not_valid));
            thereIsIssue = true;
            message = getString(R.string.invitation_customization_validator_plate_format_not_valid2);
        }

        if(!thereIsIssue){
            return true;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(InvitationCustomizationActivity.this);
        builder.setTitle(R.string.invitation_customization_validator_dialog_title);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.invitation_customization_validator_dialog_button_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

        return false;
    }


    private void updateCustomDaysOfWeekSelectionSwitch() {
        if(getTimeMode().equals(TIME_MODE_ONE_DAY)){
            customDaysOfWeekSelectionSwitch.setClickable(false);
            customDaysOfWeekSelectionSwitch.setChecked(true);
            invitation.setCustomDaysOfWeek(false);
            customDaysOfWeekSelectionSwitch.setAlpha(0.3f);
            days_label.setVisibility(View.GONE);
        } else {
            customDaysOfWeekSelectionSwitch.setClickable(true);
            customDaysOfWeekSelectionSwitch.setAlpha(1.0f);
            days_label.setVisibility(View.VISIBLE);
        }
    }

    private String getTimeMode() {
        return getTimeMode(invitation);
    }

    public static String getTimeMode(Invitation inv) {
        String timeMode = TIME_MODE_ONE_DAY;
        if (inv.daysInPeriodCount() > 2) {
            timeMode = TIME_MODE_MANY_DAYS;
        }
        return timeMode;
    }

    private void updateSummary(){
        String summary = Utils.getSummary(
                InvitationCustomizationActivity.this,
                summaryType,
                invitation,
                guestNames,
                true,
                areAllSectorsSelected(),
                getSelectedCondoName(),
                getSelectedPropertyName(),
                getStringSelectedSectorNameList());

        TextView summaryTextView = findViewById(R.id.summary);
        summaryTextView.setText(summary);
    }

    private String getSelectedCondoName() {
        Property property = getProperty(properties, invitation.getPropertyId());
        return property.getCondoName();
    }

    private String getSelectedPropertyName() {
        Property property = getProperty(properties, invitation.getPropertyId());
        return property.getName();
    }

    private List<PickSectorsActivity.Sector> getSelectedPropertySectors() {
        Property property = getProperty(properties, invitation.getPropertyId());
        return property.getSectors();
    }

    private boolean areAllSectorsSelected() {
        Property property = getProperty(properties, invitation.getPropertyId());
        if (property != null) {
            return property.getSectors().size() == invitation.getIdsSelectedSectors().size();
        }
        return false;
    }

    private static boolean isPlateValid(String plate){
        String regexCar1 = "^([A-Z]{4}\\d{2})$";
        String regexCar2 = "^([A-Z]{2}\\d{4})$";
        String regexMotorcycle1 = "^([A-Z]{3}\\d{2})$";
        String regexMotorcycle2 = "^([A-Z]{2}\\d{3})$";
        String regexPlate = "^([A-Z]{3}\\d{3})$";

        return (plate.length() == 6 || plate.length() == 5) &&
                (plate.matches(regexCar1) ||
                        plate.matches(regexCar2) ||
                        plate.matches(regexMotorcycle1) ||
                        plate.matches(regexMotorcycle2) ||
                        plate.matches(regexPlate));
    }

    private static void updateDateDisplay(TextView label, Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        label.setText(dateFormat.format(calendar.getTime()));
    }

    private static void updateTimeDisplay(TextView label, Calendar calendar) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        label.setText(String.format("%s hrs.", timeFormat.format(calendar.getTime())));
    }

    // si el resultado es positivo c1 esta despues de c2
    private static int onlyYearMonthDayComparator(Calendar c1, Calendar c2) {
        if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR))
            return c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);
        if (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH))
            return c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);
        return c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH);
    }

    // si el resultado es positivo c1 esta despues de c2
    private static int onlyHourMinuteComparator(Calendar c1, Calendar c2) {
        if (c1.get(Calendar.HOUR_OF_DAY) != c2.get(Calendar.HOUR_OF_DAY))
            return c1.get(Calendar.HOUR_OF_DAY) - c2.get(Calendar.HOUR_OF_DAY);
        return c1.get(Calendar.MINUTE) - c2.get(Calendar.MINUTE);
    }

    public static void selectAllSectors(Invitation invitation, List<Property> properties) {
        Property property = getProperty(properties, invitation.getPropertyId());
        List<Integer> ids = new ArrayList<>();
        if (property != null) {
            for(PickSectorsActivity.Sector s : property.getSectors()){
                ids.add(s.getId());
            }
        }
        invitation.setIdsSelectedSectors(ids);
    }

    public static Property getProperty(List<Property> properties, int propertyId) {
        for(Property p : properties){
            if(p.getId() == propertyId){
                return p;
            }
        }
        return new Property();
    }

    public static class Property implements Serializable{
        private int id;
        private String name;
        private int condoId;
        private String condoName;
        private boolean plateServiceAvailableForInvitations;
        private boolean plateServiceAvailableForResidents;
        private boolean hasBarriers;
        private boolean availableForInvitations;
        private boolean isActive;

        List<PickSectorsActivity.Sector> sectors = new ArrayList<>();

        Property(){
            id = -1;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            isActive = active;
        }

        public boolean isAvailableForInvitations() {
            return availableForInvitations;
        }

        public void setAvailableForInvitations(boolean availableForInvitations) {
            this.availableForInvitations = availableForInvitations;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public boolean isPlateServiceAvailableForInvitations() {
            return plateServiceAvailableForInvitations;
        }

        public void setPlateServiceAvailableForInvitations(boolean plateServiceAvailableForInvitations) {
            this.plateServiceAvailableForInvitations = plateServiceAvailableForInvitations;
        }

        public boolean isHasBarriers() {
            return hasBarriers;
        }

        public void setHasBarriers(boolean hasBarriers) {
            this.hasBarriers = hasBarriers;
        }

        public List<PickSectorsActivity.Sector> getSectors() {
            return sectors;
        }

        public void setSectors(List<PickSectorsActivity.Sector> sectors) {
            this.sectors = sectors;
        }

        public void setPlateServiceAvailableForResidents(boolean plateServiceAvailableForResidents) {
            this.plateServiceAvailableForResidents = plateServiceAvailableForResidents;
        }

        public boolean isPlateServiceAvailableForResidents() {
            return plateServiceAvailableForResidents;
        }
    }

    public static ArrayList<Property> getPropertiesListFromJson(JSONArray propertiesJson, boolean forceReturnAllSectors){
        //Log.i(TAG, "propertiesJson:" + propertiesJson);
        ArrayList<Property> result = new ArrayList<>();
        try {
            for(int i = 0; i < propertiesJson.length(); i++){
                JSONObject propertyJson = propertiesJson.getJSONObject(i);
                Log.i(TAG, "propertyJson:" + propertyJson);
                Property property = new Property();
                property.setId(propertyJson.getInt("house_id"));
                property.setName(propertyJson.getString("house_name"));
                property.setCondoId(propertyJson.getInt("condo_id"));
                property.setCondoName(propertyJson.getString("condo_name"));
                property.setAvailableForInvitations(
                        !propertyJson.has("invitations")
                                || propertyJson.getInt("invitations") == 1
                );
                property.setPlateServiceAvailableForResidents(propertyJson.getInt("open_with_plate") == 1);
                property.setPlateServiceAvailableForInvitations(propertyJson.getInt("invitations_with_plates") == 1);
                property.setActive( !propertyJson.has("active")
                        || propertyJson.getBoolean("active"));

                JSONArray sectorsJson = propertyJson.getJSONArray("barriers");
                ArrayList<PickSectorsActivity.Sector> sectors = new ArrayList<>();
                for(int j = 0; j < sectorsJson.length(); j++){
                    JSONObject sectorJson = sectorsJson.getJSONObject(j);
                    if(forceReturnAllSectors || !sectorJson.has("use_inv") || sectorJson.getInt("use_inv") == 1) {
                        PickSectorsActivity.Sector sector = new PickSectorsActivity.Sector();
                        sector.setId(sectorJson.getInt("sector_id"));
                        sector.setName(sectorJson.getString("sector_name"));
                        sectors.add(sector);
                    }
                }

                property.setSectors(sectors);
                result.add(property);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ArrayList<InvitationCustomizationActivity.Property> getUserProperties(Context context){
        ArrayList<InvitationCustomizationActivity.Property> result = new ArrayList<>();
        try {
            JSONObject userJson = new JSONObject(Utils.getDefaults("user", context));
            JSONArray propertiesJson = new JSONArray(userJson.getString("properties"));
            result = InvitationCustomizationActivity.getPropertiesListFromJson(propertiesJson, false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ArrayList<InvitationCustomizationActivity.Property> getUserPropertiesWithAllSectors(Context context){
        ArrayList<InvitationCustomizationActivity.Property> result = new ArrayList<>();
        try {
            JSONObject userJson = new JSONObject(Utils.getDefaults("user", context));
            JSONArray propertiesJson = new JSONArray(userJson.getString("properties"));
            result = InvitationCustomizationActivity.getPropertiesListFromJson(propertiesJson, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ArrayList<Property> filterActiveAndAvailableForInvitations(ArrayList<Property> properties) {
        ArrayList<Property> result =  new ArrayList<>();
        for(Property property : properties){
            if(property.isActive() && property.isAvailableForInvitations()) {
                result.add(property);
            }
        }
        return result;
    }

    public static ArrayList<Property> filterNoActive(ArrayList<Property> properties) {
        ArrayList<Property> result =  new ArrayList<>();
        for(Property property : properties){
            if(!property.isActive()) {
                result.add(property);
            }
        }
        return result;
    }

    public static ArrayList<Property> filterNoPlateServiceAvailableForResidents(ArrayList<Property> properties) {
        ArrayList<Property> result =  new ArrayList<>();
        for(Property property : properties){
            if(!property.isPlateServiceAvailableForResidents()) {
                result.add(property);
            }
        }
        return result;
    }

    public static ArrayList<Property> filterActiveAndPlateServiceAvailableForResidents(ArrayList<Property> properties) {
        ArrayList<Property> result =  new ArrayList<>();
        for(Property property : properties){
            if(property.isActive() && property.isPlateServiceAvailableForResidents()) {
                result.add(property);
            }
        }
        return result;
    }

    public static boolean containsProperty(ArrayList<Property> properties, int propertyId) {
        for(Property property : properties){
            if(property.getId() == propertyId) {
                return true;
            }
        }
        return false;
    }
}
