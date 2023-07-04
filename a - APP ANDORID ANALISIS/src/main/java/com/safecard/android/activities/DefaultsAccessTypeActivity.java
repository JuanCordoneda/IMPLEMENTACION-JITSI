package com.safecard.android.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.TextView;

import com.safecard.android.R;
import com.safecard.android.adapters.DefaultsAccessTypeAdapter;
import com.safecard.android.utils.DividerItemDecoration;
import com.safecard.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DefaultsAccessTypeActivity extends AppCompatActivity implements DefaultsAccessTypeAdapter.Listener {
    final private static String TAG = "DefaultsAccessTypeActivity";

    private DefaultsAccessTypeAdapter accessDefaultAdapter;
    private int propertyId;

    public static final String PROPERTY_ID = "PROPERTY_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_defaults_access_type);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_shape);

        TextView title = (TextView) findViewById(R.id.toolbar_title);
        TextView house_name = (TextView) findViewById(R.id.house_name);

        Bundle extras = getIntent().getExtras();

        title.setText(getString(R.string.access_default_sector_activity_title));

        ArrayList<JSONObject> sectors_array = new ArrayList<JSONObject>();
        try {

            propertyId = extras.getInt(PROPERTY_ID);
            JSONObject propertyJson = Utils.getUserPropertyJsonById(getApplicationContext(), propertyId);
            house_name.setText(String.format("%s - %s", propertyJson.getString("house_name"), propertyJson.getString("condo_name")));

            for(int i = 0; i < propertyJson.getJSONArray("barriers").length(); i++){
                sectors_array.add(propertyJson.getJSONArray("barriers").getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        toolbar.setNavigationIcon(R.drawable.ic_shape);

        RecyclerView sectors_list = (RecyclerView) findViewById(R.id.sectors_list);
        sectors_list.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        sectors_list.setLayoutManager(mLayoutManager);

        sectors_list.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        accessDefaultAdapter = new DefaultsAccessTypeAdapter(sectors_array, propertyId, DefaultsAccessTypeActivity.this, this);
        sectors_list.setAdapter(accessDefaultAdapter);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onItemClick(JSONObject sector_json, String preference, int position) {
        try {
            JSONObject defaultAccessType = Utils.getDefaultJSONObject("defaultSectorControl", getApplicationContext());
            defaultAccessType.put(propertyId + "-" + sector_json.getInt("sector_id"), preference);
            Utils.setDefaultJSONObject("defaultSectorControl", defaultAccessType, getApplicationContext());
            accessDefaultAdapter.changeDefaultControl(defaultAccessType, position);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
