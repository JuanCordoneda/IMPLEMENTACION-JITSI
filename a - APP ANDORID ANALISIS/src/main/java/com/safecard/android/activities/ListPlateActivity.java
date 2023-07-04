package com.safecard.android.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.safecard.android.Config;
import com.safecard.android.R;
import com.safecard.android.adapters.PlatesAdapter;
import com.safecard.android.utils.ClickListener;
import com.safecard.android.utils.DividerItemDecoration;
import com.safecard.android.utils.RecyclerTouchListener;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



public class ListPlateActivity extends AppCompatActivity {

    private static final String TAG = "ListPlateActivity";

    public static String PROPERTY_ID = "PROPERTY_ID";
    public static String PROPERTY_NAME = "PROPERTY_NAME";
    public static String PROPERTY_ACTIVE = "PROPERTY_ACTIVE";
    public static String CONDO_NAME = "CONDO_NAME";

    private FloatingActionButton mAddPlate;
    private RecyclerView mPlateList;
    private ProgressDialog pDialog;

    private List<JSONObject> plates_list = new ArrayList<>();
    private PlatesAdapter plate_adapter;

    SwipeRefreshLayout swiperefresh;

    RequestVolley rqv;

    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private ActionMode actionMode;
    private Boolean refresh_list = true;

    private int propertyId;
    private boolean propertyActive;
    private LinearLayout mPlateListPlaceholder;
    private TextView mPlateListPlaceholderText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_plate);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_shape);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView titleAccess = (TextView) toolbar.findViewById(R.id.toolbar_title_access);
        TextView titleCondo = (TextView) toolbar.findViewById(R.id.toolbar_title_access_condo);
        swiperefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        pDialog = new ProgressDialog(ListPlateActivity.this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setCancelable(false);

        mPlateListPlaceholderText = findViewById(R.id.plate_list_placeholder_text);
        mPlateListPlaceholder = findViewById(R.id.plate_list_placeholder);
        mPlateList = (RecyclerView) findViewById(R.id.plate_list);
        mPlateList.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mPlateList.setLayoutManager(mLayoutManager);
        mPlateList.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mPlateList.setItemAnimator(new DefaultItemAnimator());

        mAddPlate = (FloatingActionButton) findViewById(R.id.add_plate);

        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPlates();
            }
        });


        propertyId = getIntent().getIntExtra(PROPERTY_ID, -1);
        propertyActive = getIntent().getBooleanExtra(PROPERTY_ACTIVE, false);

        String propertyName = getIntent().getStringExtra(PROPERTY_NAME);
        if(propertyName == null){
            propertyName = "";
        }

        String condoName = getIntent().getStringExtra(CONDO_NAME);
        if(condoName == null){
            condoName = "";
        }

        titleAccess.setText(propertyName);
        titleCondo.setText(condoName);

        mAddPlate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!propertyActive) {
                    Utils.showToast(getApplicationContext(), getString(R.string.no_privileges));
                    return;
                }

                Intent intent = new Intent(getApplicationContext(), NewPlateActivity.class);
                intent.putExtra(NewPlateActivity.HOUSE_ID, propertyId);
                startActivity(intent);
            }
        });

        mPlateList.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mPlateList, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (actionMode != null){
                    toggleSelection(position);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        refresh_list = actionMode == null;
    }

    public void toggleSelection(int position) {
        plate_adapter.toggleSelection(position);
        int count = plate_adapter.getSelectedItemCount();

        actionMode.setTitle(String.valueOf(count));
        actionMode.invalidate();
    }

    @Override
    public void onResume(){
        super.onResume();

        if(refresh_list){
            getPlates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // java.lang.IllegalArgumentException: View not attached to window manager
        Utils.safeDismissDialog(ListPlateActivity.this, pDialog);
        pDialog = null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_plate, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.action_remove_plate:

                if (actionMode == null) {
                    actionMode = startSupportActionMode(actionModeCallback);
                    actionMode.setTitle("0");
                    plate_adapter.displayCheckbox(true);
                    mAddPlate.setVisibility(View.INVISIBLE);
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getPlates(){
        swiperefresh.setRefreshing(true);
        mPlateListPlaceholderText.setText(R.string.list_plate_wait_while_loading_data);
        String url = Config.ApiUrl + "plates/" + Utils.getMobile(getApplicationContext()) + "/" + propertyId;
        rqv = new RequestVolley(url, getApplicationContext());
        rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                if (swiperefresh != null && swiperefresh.isRefreshing()) {
                    swiperefresh.setRefreshing(false);
                }
                //Log.i(TAG, "response:   "+response);

                try {
                    if(response.getString("result").equals("ACK")){
                        JSONArray array = new JSONArray(response.getString("plates"));
                        //Log.i(TAG, "persisting");

                        plates_list.clear();
                        for (int i = 0; i < array.length(); i++) {
                            plates_list.add(array.getJSONObject(i));
                        }
                        plate_adapter = new PlatesAdapter(plates_list);
                        mPlateList.setAdapter(plate_adapter);

                        mPlateListPlaceholder.setVisibility(View.INVISIBLE);
                        mPlateList.setVisibility(View.INVISIBLE);
                        if(array.length() == 0){
                            mPlateListPlaceholderText.setText(R.string.list_plate_you_have_no_plates);
                            mPlateListPlaceholder.setVisibility(View.VISIBLE);
                        } else {
                            mPlateList.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(final String errorType, final String msg) {
                if (swiperefresh != null && swiperefresh.isRefreshing()) {
                    swiperefresh.setRefreshing(false);
                }
                if (!errorType.equals("TimeoutError")) {
                    Log.i(TAG, "errorType is not TimeoutError");
                    if (!msg.equals("")) {
                        Utils.showToast(getApplicationContext(), msg);
                    }
                }
            }
        });
    }



    private class ActionModeCallback implements ActionMode.Callback {
        private final String TAG = "ActionModeCallback";

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate (R.menu.menu_confirm, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_confirm_remove:

                    if(plate_adapter.getSelectedItemCount() > 0){

                        removePlates(mode);

                    } else {
                        Utils.showToast(getApplicationContext(),getString(R.string.list_plate_you_must_select_one_plate_at_least));
                    }

                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            plate_adapter.clearSelection();
            plate_adapter.displayCheckbox(false);
            mAddPlate.setVisibility(View.VISIBLE);
            actionMode = null;
        }
    }

    public void removePlates(final ActionMode mode){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ListPlateActivity.this);

        String plates_ids = "";
        final List<Integer> selected_items = plate_adapter.getSelectedItems();
        try {
            for(int i = 0; i < selected_items.size(); i++){
                plates_ids += plate_adapter.getPlateArray().get(selected_items.get(i)).getString("id")+",";

                //plates_ids += plates_array.getJSONObject(selected_items.get(i)).getString("id")+",";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        alertDialog.setMessage(R.string.list_plate_are_you_sure_about_delete_these_plates);
        alertDialog.setCancelable(false);
        final String ids = plates_ids.substring(0, plates_ids.length() - 1);

        alertDialog.setPositiveButton(R.string.list_plate_dialog_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                String url = Config.ApiUrl + "remove_plates/" + Utils.getMobile(getApplicationContext()) + "/" + propertyId + "/" + ids;
                rqv = new RequestVolley(url, getApplicationContext());

                if(pDialog != null) {
                    pDialog.setMessage(getString(R.string.list_plate_dialog_deleting_plates));
                    pDialog.show();
                }
                rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        if(pDialog != null && pDialog.isShowing()) {
                            Utils.safeDismissDialog(ListPlateActivity.this, pDialog);
                        }
                        try {
                            if(response.getString("result").equals("ACK")){
                                plate_adapter.removeItems(selected_items);
                                Utils.showToast(getApplicationContext(), getString(R.string.list_plate_dialog_plates_deleted_successfully));
                                if(mode != null){
                                    mode.finish();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(String errorType, String msg) {
                        if(pDialog != null && pDialog.isShowing()) {
                            Utils.safeDismissDialog(ListPlateActivity.this, pDialog);
                        }

                        if (!msg.equals("")) {
                            Utils.showToast(getApplicationContext(), msg);
                        }
                    }
                });
            }
        });

        alertDialog.setNegativeButton(R.string.list_plate_dialog_no, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                if(dialog != null) {
                    dialog.cancel();
                }
            }
        });
        alertDialog.show();

    }
}
