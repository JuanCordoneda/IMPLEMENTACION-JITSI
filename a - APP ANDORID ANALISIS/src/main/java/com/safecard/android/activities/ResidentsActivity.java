package com.safecard.android.activities;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.safecard.android.Config;
import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.adapters.ResidentsAdapter;
import com.safecard.android.utils.DividerItemDecoration;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ResidentsActivity extends AppCompatActivity implements ResidentsAdapter.OnItemClickListener{

    private static final String TAG = "ResidentsActivity";
    private TextView title_access, title_condo;
    private RecyclerView residents_list;
    private RecyclerView.LayoutManager mLayoutManager;
    private ResidentsAdapter mResidentsAdapter;
    private Toolbar toolbar;

    private JSONObject access_selected;
    private String mobile, house_id;
    private int fromActivity;
    private Boolean is_admin, heritance;

    private FloatingActionButton add_resident;
    //private ProgressDialog pDialog;
    SwipeRefreshLayout swiperefresh;

    ArrayList<JSONObject> residents_list_aux = new ArrayList<JSONObject>();
    private boolean isOwner;
    private boolean isResidentAdmin;

    private String appUserType = Consts.USER_TYPE_RESIDENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.residents);

        //pDialog = new ProgressDialog(ResidentsActivity.this);
        //pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //pDialog.setMessage("Cargando residentes.");
        //pDialog.setCancelable(false);

        add_resident = (FloatingActionButton) findViewById(R.id.add_resident);
        add_resident.setVisibility(View.INVISIBLE);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_shape);

        title_access = (TextView) toolbar.findViewById(R.id.toolbar_title_access);
        title_condo = (TextView) toolbar.findViewById(R.id.toolbar_title_access_condo);

        Bundle extras = getIntent().getExtras();
        fromActivity = extras.getInt("fromActivity");

        try{
            switch (fromActivity){
                case Consts.ProfileActivity:
                    access_selected = new JSONObject(extras.getString("access_selected"));
                    break;
                case Consts.ConfirmResidentActivity:
                    access_selected = new JSONObject(extras.getString("access_selected"));
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        residents_list = (RecyclerView) findViewById(R.id.residents_list);
        residents_list.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        residents_list.setLayoutManager(mLayoutManager);
        residents_list.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        residents_list.setItemAnimator(new DefaultItemAnimator());

        try {

            JSONObject login_json = new JSONObject(Utils.getDefaults("login", getApplicationContext()));
            mobile = login_json.getString("mobile");
            house_id = access_selected.getString("house_id");
            title_access.setText(access_selected.getString("house_name"));
            title_condo.setText(access_selected.getString("condo_name"));

        } catch (JSONException e) {
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
                if(!heritance){
                    Utils.showToast(getApplicationContext(), getString(R.string.no_privileges));
                } else {
                    Intent confirm_resident_intent = new Intent(getApplicationContext(), ConfirmResidentActivity.class);
                    confirm_resident_intent.putExtra("access_selected", access_selected.toString());
                    confirm_resident_intent.putExtra("new_resident", true);
                    confirm_resident_intent.putExtra("heritance", heritance);
                    confirm_resident_intent.putExtra("is_admin", is_admin);
                    startActivity(confirm_resident_intent);
                }
            }
        });

        swiperefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swiperefresh.setEnabled(false);
    }

    @Override
    protected void onResume(){
        super.onResume();

        //loadPersistent();
        updateFromApi();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    public void loadPersistent(){
        Log.i(TAG,"loadPersistent");
        try {
            if(Utils.getDefaults("residents", getApplicationContext()) != null) {
                JSONObject response = new JSONObject(Utils.getDefaults("residents", getApplicationContext()));
                populate(response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateFromApi(){
        /* 2. Consulto api */
        final String url_residents = Config.ApiUrl + "residents/" + mobile + "/" + house_id;

        final RequestVolley rqv = new RequestVolley(url_residents,getApplicationContext());

        swiperefresh.setRefreshing(true);

        rqv.requestApi(new RequestVolley.VolleyJsonCallback(){
            @Override
            public void onSuccess(JSONObject response) {
                Log.i(TAG,"callApiResidents onSuccess");
                try {
                    if(swiperefresh.isRefreshing()){
                        swiperefresh.setRefreshing(false);
                    }
                    if (response.getString("result").equals("ACK")) {
                        Utils.setDefaults("residents", response.toString(), getApplicationContext());
                        populate(response);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorType, String msg) {
                if(swiperefresh.isRefreshing()){
                    swiperefresh.setRefreshing(false);
                }
                if (!msg.equals("")) {
                    Utils.showToast(getApplicationContext(), msg);
                }
            }
        });
    }

    private void populate(JSONObject response){
        Log.i(TAG,"populateResidents");
        try {
            JSONObject user_resident = new JSONObject(response.getString("resident"));
            heritance = (user_resident.getInt("admin") == 1 || user_resident.getInt("owner") == 1) ? true : false;
            is_admin = (user_resident.getInt("admin") == 1 && user_resident.getInt("owner") == 0) ? true : false;

            this.appUserType = Consts.USER_TYPE_RESIDENT;
            if(user_resident.getInt("owner") == 1){
                this.appUserType = Consts.USER_TYPE_OWNER;
            }else if (user_resident.getInt("admin") == 1){
                this.appUserType = Consts.USER_TYPE_RESIDENT_ADMIN;
            }

            add_resident.setVisibility(View.INVISIBLE);
            if(this.appUserType.equals(Consts.USER_TYPE_OWNER) ||
                    this.appUserType.equals(Consts.USER_TYPE_RESIDENT_ADMIN)){
                add_resident.setVisibility(View.VISIBLE);
            }

            JSONArray residents_array = new JSONArray(response.getString("residents"));

            if(residents_array.length() > 0){
                mResidentsAdapter = new ResidentsAdapter(residents_array);
                residents_list.setAdapter(mResidentsAdapter);
                mResidentsAdapter.setClickListener(this);
                residents_list_aux.clear();
                for(int i = 0; i < residents_array.length(); i++ ){
                    residents_list_aux.add(residents_array.getJSONObject(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void OnItemClick(View v, int position) {
        try {
            if(!this.appUserType.equals(Consts.USER_TYPE_OWNER) &&
                    !this.appUserType.equals(Consts.USER_TYPE_RESIDENT_ADMIN)){
                Utils.showToast(getApplicationContext(), getString(R.string.activity_residents_no_privileges));
                return;
            }

            JSONObject resident = new JSONObject(residents_list_aux.get(position).toString());

            if(resident.getInt("owner") == 1){
                Utils.showToast(getApplicationContext(), getString(R.string.activity_residents_owner_cant_be_edited));
            } else if(resident.getInt("admin") == 1 && this.appUserType.equals(Consts.USER_TYPE_RESIDENT_ADMIN)){
                Utils.showToast(getApplicationContext(), getString(R.string.activity_residents_no_privileges));
            }else {
                Intent edit_resident_intent = new Intent(getApplicationContext(), ConfirmResidentActivity.class);
                edit_resident_intent.putExtra("resident", resident.toString());
                edit_resident_intent.putExtra("access_selected", access_selected.toString());
                edit_resident_intent.putExtra("new_resident", false);
                edit_resident_intent.putExtra("heritance", heritance);
                edit_resident_intent.putExtra("is_admin", is_admin);
                //residents_list_aux.clear();
                startActivity(edit_resident_intent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
