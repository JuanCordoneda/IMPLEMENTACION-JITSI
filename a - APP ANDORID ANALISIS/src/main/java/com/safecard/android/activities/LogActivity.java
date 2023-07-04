package com.safecard.android.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.safecard.android.Config;
import com.safecard.android.R;
import com.safecard.android.adapters.LogsAdapter;
import com.safecard.android.utils.DividerItemDecoration;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;


/**
 * Created by efajardo on 05-11-15.
 */
public class LogActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LogActivity";

    private static Integer page = 0;
    private String[] months;

    public static String RECINTO_LABEL = "RECINTO_LABEL";
    public static String RECINTO_ID = "RECINTO_ID";
    public static String HOUSE_STUDENT_ID = "HOUSE_STUDENT_ID";
    public static String HOUSE_STUDENT_NAME = "HOUSE_STUDENT_NAME";

    private LogsAdapter mAdapter;

    private TextView title;
    private Toolbar toolbar;
    private static String COMMUNITY_NAME = "COMMUNITY_NAME";
    TextView label_recinto;

    private int house_student_id, recinto_id;

    SwipeRefreshLayout swiperefresh;
    RecyclerView listLogs;
    JSONArray logs_json_array;


    Button btnFind;
    EditText txtDateStart, txtDateFinish ,txtTimeStart, txtTimeFinish, txtFilter;
    private int mYearFinish, mMonthFinish, mDayFinish, mHourFinish, mMinuteFinish;
    private int mYearStart, mMonthStart, mDayStart, mHourStart, mMinuteStart;

    String sa_community_name, sa_community_id, sa_house_student;

    TextView resultTitle;
    private LinearLayout advancedSearchLayout;
    private TextView txtadvancedSearch;

    boolean defaultSearch = true;

    TimePickerDialog timePickerDialog;
    DatePickerDialog datePickerDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logs);

        months  = new String[]{
                getString(R.string.log_month_jan),
                getString(R.string.log_month_feb),
                getString(R.string.log_month_mar),
                getString(R.string.log_month_apr),
                getString(R.string.log_month_may),
                getString(R.string.log_month_jun),
                getString(R.string.log_month_jul),
                getString(R.string.log_month_aug),
                getString(R.string.log_month_sep),
                getString(R.string.log_month_oct),
                getString(R.string.log_month_nov),
                getString(R.string.log_month_dec)};

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_shape);

        title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        label_recinto = (TextView) findViewById(R.id.label_recinto);

        resultTitle = (TextView) findViewById(R.id.result_title);

        swiperefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        Bundle extras = getIntent().getExtras();
        title.setText(R.string.log_title);
        label_recinto.setText(extras.getString(RECINTO_LABEL));

        house_student_id = extras.getInt(HOUSE_STUDENT_ID);
        recinto_id = extras.getInt(RECINTO_ID);

        sa_community_id = recinto_id +"";
        sa_community_name = extras.getString(COMMUNITY_NAME);
        sa_house_student = extras.getString(HOUSE_STUDENT_NAME);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mAdapter = new LogsAdapter(getApplicationContext());

        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (advancedSearchLayout.getVisibility() == View.GONE) {
                    setDefaultSearch();
                    refreshUIDates();
                }
                mAdapter.removeAll();
                page = 0;
                callApiLogs(10000, 10000);
            }
        });


        listLogs = (RecyclerView) findViewById(R.id.listLogs);
        listLogs.setLayoutManager(new LinearLayoutManager(this));

        advancedSearchLayout = (LinearLayout) findViewById(R.id.advanced_search_layout);
        txtDateStart = (EditText) findViewById(R.id.in_date_start);
        txtDateFinish = (EditText) findViewById(R.id.in_date_finish);
        txtTimeStart = (EditText) findViewById(R.id.in_time_start);
        txtTimeFinish = (EditText) findViewById(R.id.in_time_finish);
        txtadvancedSearch = (TextView) findViewById(R.id.advanced_search_text);
        txtFilter = (EditText) findViewById(R.id.filter);

        btnFind = (Button) findViewById(R.id.btn_find);

        advancedSearchLayout.setVisibility(View.GONE);
        txtDateStart.setOnClickListener(this);
        txtDateFinish.setOnClickListener(this);
        txtTimeStart.setOnClickListener(this);
        txtTimeFinish.setOnClickListener(this);
        txtadvancedSearch.setOnClickListener(this);
        btnFind.setOnClickListener(this);

        setDefaultSearch();
        mAdapter.setRecyclerView(listLogs);
        mAdapter.setOnLoadMoreListener(new LogsAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                Handler handler = new Handler();
                //add progress item
                Log.i(TAG, "onLoadMore");

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //remove progress item
                        Log.i(TAG, "onLoadMore postDelayed");
                        page++;
                        callApiLogs(10000, 10000);
                        defaultSearch = false;
                    }
                }, 1);
            }
        });

        listLogs.setHasFixedSize(true);
        listLogs.setLayoutManager(new LinearLayoutManager(this));
        listLogs.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        listLogs.setAdapter(mAdapter);

        page = 0;
        mAdapter.removeAll();
        callApiLogs(30000, 10000);

        refreshUIDates();

        txtFilter.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    Utils.hideKeyboard(LogActivity.this);
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onPause(){
        super.onPause();
    }



    @Override
    public void onResume(){
        super.onResume();
    }

    public void callApiLogs(int volleyTimeout, int appTimeout) {

        if(validateDatesOrder()) {
            Utils.showToast(getApplicationContext(), getString(R.string.log_validation_from_date_has_to_be_smaller_than_to_date));
            if (swiperefresh.isRefreshing()) {
                swiperefresh.setRefreshing(false);
            }
            return;
        }

        final Map<String, String> networkDetails = Utils.getConnectionDetails(getApplicationContext());
        if (networkDetails.isEmpty()) {
            try {
                Utils.showToast(getApplicationContext(), getString(R.string.internet_connection));
                String logs = Utils.getDefaults("logs", getApplicationContext());
                if (logs != null) {
                    JSONArray logs_json_array = new JSONArray(logs);
                    populateLogs(logs_json_array);
                }
                if (swiperefresh.isRefreshing()) {
                    swiperefresh.setRefreshing(false);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {

            String url = Config.ApiUrl + "logs/" +
                    Utils.getMobile(getApplicationContext()) + "/" +
                    recinto_id + "/" + house_student_id + "/" + page;

            url += "?from=" + getStartDatetime() + "&to=" + getFinishDatetime();
            url += "&query=" + txtFilter.getText();
            url = url.replaceAll(" ", "%20");
            Log.i(TAG, "url: " + url);

            swiperefresh.setRefreshing(true);
            RequestVolley rqv = new RequestVolley(url, getApplicationContext());
            rqv.requestApi(new RequestVolley.VolleyJsonCallback() {

                @Override
                public void onSuccess(JSONObject response) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (swiperefresh.isRefreshing()) {
                                swiperefresh.setRefreshing(false);
                            }
                        }
                    });

                    try {
                        if (response.getString("result").equals("ACK")) {
                            if (defaultSearch) {
                                Log.i(TAG, "persisting");
                                Utils.setDefaults("logs", response.getString("logs"), getApplicationContext());
                            }
                            logs_json_array = new JSONArray(response.getString("logs"));
                            populateLogs(logs_json_array);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onError(final String errorType, final String msg) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (swiperefresh.isRefreshing()) {
                                swiperefresh.setRefreshing(false);
                            }
                        }
                    });
                    runOnUiThread(new Runnable() {
                        public void run() {
                        Boolean loadedPersisted = false;
                        if (defaultSearch) {
                            String logs = Utils.getDefaults("logs", getApplicationContext());
                            if (logs != null && logs.length() > 0 ) {
                                Log.i(TAG, "load persisted");
                                try {
                                    logs_json_array = new JSONArray(logs);
                                    populateLogs(logs_json_array);
                                    loadedPersisted = true;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (!loadedPersisted || !errorType.equals("TimeoutError")) {
                            Log.i(TAG, "There are not persisted or errorType is not TimeoutError");
                            if (!msg.equals("")) {
                                Utils.showToast(getApplicationContext(), msg);
                            }
                        }
                        }
                    });
                }
            });
        }
    }

    public void populateLogs(final JSONArray logs_json_array) {
        Log.i(TAG, "populateLogs");
        for(int i = 0; i< logs_json_array.length(); i++){
            try {
                mAdapter.add(logs_json_array.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mAdapter.setLoaded();

    }

    protected void setDefaultSearch() {
        Calendar c = Calendar.getInstance();
        mYearFinish = c.get(Calendar.YEAR);
        mMonthFinish = c.get(Calendar.MONTH) + 1;
        mDayFinish = c.get(Calendar.DAY_OF_MONTH);
        mHourFinish = 23;
        mMinuteFinish = 59;

        c.add(Calendar.DATE, -7);

        mYearStart = c.get(Calendar.YEAR);
        mMonthStart = c.get(Calendar.MONTH) + 1;
        mDayStart = c.get(Calendar.DAY_OF_MONTH);
        mHourStart = 0;
        mMinuteStart = 1;

        defaultSearch = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onClick(final View v) {

        Utils.hideKeyboard(this);

        int id = v.getId();
        //Log.i(TAG, "id " + id);

        if (v == txtDateStart || v == txtDateFinish) {

            final Calendar c = Calendar.getInstance();

            DatePickerDialog.OnDateSetListener mOnDateSetListener = null;

            if (v == txtDateStart) {
                mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        mDayStart = day;
                        mMonthStart = month + 1;
                        mYearStart = year;
                        defaultSearch = false;
                        refreshUIDates();
                    }
                };
                datePickerDialog = new DatePickerDialog(
                        this, mOnDateSetListener, mYearStart, mMonthStart - 1, mDayStart);
            } else if(v == txtDateFinish) {
                mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        mDayFinish = day;
                        mMonthFinish = month + 1;
                        mYearFinish = year;
                        defaultSearch = false;
                        refreshUIDates();
                    }
                };
                datePickerDialog = new DatePickerDialog(
                        this, mOnDateSetListener, mYearFinish, mMonthFinish - 1, mDayFinish);
            }
            datePickerDialog.show();
        }

        if (v == txtTimeStart || v == txtTimeFinish) {

            final Calendar c = Calendar.getInstance();
            TimePickerDialog.OnTimeSetListener mOnTimeSetListener = null;

            if (v == txtTimeStart) {
                mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mHourStart = hourOfDay;
                        mMinuteStart = minute;
                        defaultSearch = false;
                        refreshUIDates();
                    }
                };

                timePickerDialog = new TimePickerDialog(
                        this, mOnTimeSetListener, mHourStart, mMinuteStart, true);

            } else if (v == txtTimeFinish) {
                mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mHourFinish = hourOfDay;
                        mMinuteFinish = minute;
                        defaultSearch = false;
                        refreshUIDates();
                    }
                };

                timePickerDialog = new TimePickerDialog(
                        this, mOnTimeSetListener, mHourFinish, mMinuteFinish, true);
            }

            timePickerDialog.show();
        }


        if (v == btnFind) {
            page = 0;
            mAdapter.removeAll();
            callApiLogs(10000, 10000);
        }

        if (v == txtadvancedSearch) {
            if (advancedSearchLayout.getVisibility() == View.GONE) {
                advancedSearchLayout.setVisibility(View.VISIBLE);
                txtadvancedSearch.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_visibility_white_24dp, 0);
            } else {
                advancedSearchLayout.setVisibility(View.GONE);
                txtadvancedSearch.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_visibility_off_white_24dp, 0);
            }
        }
    }

    protected void refreshUIDates() {
        txtDateFinish.setText(
                String.format("%02d %s. %04d", mDayFinish, months[mMonthFinish - 1], mYearFinish));
        txtTimeFinish.setText(
                String.format("%02d:%02d hrs.", mHourFinish, mMinuteFinish));

        txtDateStart.setText(
                String.format("%02d %s. %04d", mDayStart, months[mMonthStart - 1], mYearStart));

        txtTimeStart.setText(
                String.format("%02d:%02d hrs.", mHourStart, mMinuteStart));
    }

    protected String getStartDatetime() {
        return String.format("%04d-%02d-%02d %02d:%02d:00", mYearStart, mMonthStart, mDayStart,
                mHourStart, mMinuteStart);
    }

    protected String getFinishDatetime() {
        return String.format("%04d-%02d-%02d %02d:%02d:00", mYearFinish, mMonthFinish, mDayFinish,
                mHourFinish, mMinuteFinish);
    }

    protected boolean validateDatesOrder() {
        String d1 = String.format("%04d-%02d-%02d %02d:%02d:00", mYearStart, mMonthStart, mDayStart,
                mHourStart, mMinuteStart);
        String d2 = String.format("%04d-%02d-%02d %02d:%02d:00", mYearFinish, mMonthFinish, mDayFinish,
                mHourFinish, mMinuteFinish);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date1 = sdf.parse(d1);
            Date date2 = sdf.parse(d2);
            return date1.after(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
}