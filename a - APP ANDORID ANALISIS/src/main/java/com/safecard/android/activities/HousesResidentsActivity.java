package com.safecard.android.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.safecard.android.Config;
import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.adapters.HousesResidentsAdapter;
import com.safecard.android.utils.DividerItemDecoration;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;



public class HousesResidentsActivity extends AppCompatActivity implements HousesResidentsAdapter.OnItemClickListener{

    private static final String TAG = "HousesResidentsActivity";
    private Toolbar toolbar;
    private TextView title;
    private RecyclerView listHousesResidents;
    private LinearLayoutManager mLayoutManager;

    JSONObject user_json, access_selected;
    String mobile;
    RequestVolley rqv;
    ArrayList<JSONObject> houses_residents_list;
    SwipeRefreshLayout swiperefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_houses_residents);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_shape);

        title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText(R.string.houses_residents_title);

        listHousesResidents = (RecyclerView) findViewById(R.id.list_houses_residents);
        listHousesResidents.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        listHousesResidents.setLayoutManager(mLayoutManager);

        listHousesResidents.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        try {
            JSONObject login_json = new JSONObject(Utils.getDefaults("login", getApplicationContext()));
            user_json = new JSONObject(Utils.getDefaults("user", getApplicationContext()));
            mobile = login_json.getString("mobile");
        } catch(JSONException e){
            e.printStackTrace();
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        swiperefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swiperefresh.setEnabled(false);


    }



    @Override
    public void onResume(){
        super.onResume();
        loadPersistent();
        updateFromApi();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    public void updateFromApi(){
        swiperefresh.setRefreshing(true);
        String url_house_residents = Config.ApiUrl + "residences/" + mobile;
        //pDialog.show();
        rqv = new RequestVolley(url_house_residents, getApplicationContext());
        rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                if(swiperefresh.isRefreshing()){
                    swiperefresh.setRefreshing(false);
                }
                try{
                    if (response.getString("result").equals("ACK")) {
                        Utils.setDefaultJSONObject("residences", response, getApplicationContext());
                        loadPersistent();
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
                JSONObject residences = Utils.getDefaultJSONObject("residences", getApplicationContext());
                if (!msg.equals("") && residences.length() == 0) {
                    Utils.showToast(getApplicationContext(), msg);
                }
            }
        });
    }

    public void loadPersistent(){

        JSONObject residences = Utils.getDefaultJSONObject("residences", getApplicationContext());
        if(residences.length() > 0) {
            try {
                houses_residents_list = new ArrayList<>();

                JSONArray properties_array = new JSONArray(residences.getString("properties"));
                for (int i = 0; i < properties_array.length(); i++) {
                    try {
                        houses_residents_list.add(properties_array.getJSONObject(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                HousesResidentsAdapter HousesResidentsAdapter = new HousesResidentsAdapter(houses_residents_list);
                listHousesResidents.setAdapter(HousesResidentsAdapter);
                HousesResidentsAdapter.setClickListener(this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onIemClick(View v, int position) {
        JSONObject aux = houses_residents_list.get(position);
        try {
            JSONArray houses = new JSONArray(user_json.getString("properties"));
            for(int i = 0; i < houses.length(); i++){
                if(aux.getInt("house_id") == houses.getJSONObject(i).getInt("house_id")){
                    access_selected = houses.getJSONObject(i);
                }
            }

            if(aux.getBoolean("active") == true && (aux.getInt("owner") == 1 || aux.getInt("admin") == 1)){
                Intent residents_intent = new Intent(getApplicationContext(), ResidentsActivity.class);
                residents_intent.putExtra("access_selected", access_selected.toString());
                residents_intent.putExtra("fromActivity", Consts.ProfileActivity);
                startActivity(residents_intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                Utils.showToast(getApplicationContext(), getString(R.string.houses_residents_no_privileges));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
