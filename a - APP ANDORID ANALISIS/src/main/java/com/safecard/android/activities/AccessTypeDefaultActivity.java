package com.safecard.android.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.TextView;

import com.safecard.android.R;
import com.safecard.android.adapters.AccessDefaultTypeAdapter;
import com.safecard.android.utils.DividerItemDecoration;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AccessTypeDefaultActivity extends AppCompatActivity implements AccessDefaultTypeAdapter.Listener {
    private String TAG = "AccessTypeDefaultActivity";
    private Toolbar toolbar;
    private TextView title;
    private RecyclerView properties_list;
    private LinearLayoutManager mLayoutManager;
    private JSONObject default_control = new JSONObject();

    private AccessDefaultTypeAdapter AccessDefaultAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_type_default);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title = (TextView) findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.activity_access_type_default_title));
        toolbar.setNavigationIcon(R.drawable.ic_shape);

        properties_list = (RecyclerView) findViewById(R.id.properties_list);
        properties_list.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        properties_list.setLayoutManager(mLayoutManager);

        properties_list.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        ArrayList<JSONObject> properties_array = new ArrayList<JSONObject>();
        JSONArray houses_array;

        try{
            String user = Utils.getDefaults("user", getApplicationContext());
            JSONObject user_json = new JSONObject(user);

            houses_array = new JSONArray(user_json.getString("properties"));
            for(int i = 0; i < houses_array.length(); i++){
                properties_array.add(houses_array.getJSONObject(i));
            }

            AccessDefaultAdapter = new AccessDefaultTypeAdapter(properties_array, AccessTypeDefaultActivity.this, this);
            properties_list.setAdapter(AccessDefaultAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    public void onItemClick(JSONObject property_json, String preference, int position) {
        try {
            default_control = new JSONObject(Utils.getDefaults("defaultControl", getApplicationContext()));
            default_control.put(property_json.getString("house_id"), preference);
            Utils.setDefaults("defaultControl", default_control.toString(), getApplicationContext());
            AccessDefaultAdapter.changeDefaultControl(default_control, position);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
