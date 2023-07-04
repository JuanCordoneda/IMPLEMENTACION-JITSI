package com.safecard.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.safecard.android.R;
import com.safecard.android.adapters.PickPhoneNumbersAdapter;
import com.safecard.android.adapters.PickPropertiesAdapter;
import com.safecard.android.listitems.GeneralItem;
import com.safecard.android.listitems.ListItem;
import com.safecard.android.utils.DividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PickPhoneNumbersActivity extends AppCompatActivity implements PickPhoneNumbersAdapter.OnItemClickListener {

    private static final String TAG = "PickPhoneNumbersAct";

    public static final String PHONE_NUMBERS_JSON = "PHONE_NUMBERS_JSON";
    public static final String ONE_ITEM_IMMEDIATE_RESPONSE = "ONE_ITEM_IMMEDIATE_RESPONSE";
    public static final String RESPONSE_PHONE_NUMBER = "RESPONSE_PHONE_NUMBER";

    List<ListItem> itemList = new ArrayList<>();
    boolean oneItemImmediateResponse;
    private JSONArray numbers = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_phone_numbers);


        Intent intent = getIntent();
        String jsonNumbers = intent.getStringExtra(PHONE_NUMBERS_JSON);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_shape);

        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(R.string.activity_pick_phone_numbers_title);

        RecyclerView phone_number_list = findViewById(R.id.phone_number_list);
        phone_number_list.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        phone_number_list.setLayoutManager(mLayoutManager);

        phone_number_list.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        try {
            numbers = new JSONArray(jsonNumbers);

            for (int i = 0; i < numbers.length(); i++) {
                JSONObject phone = numbers.getJSONObject(i);
                GeneralItem item = new GeneralItem();
                item.setObj(phone);
                itemList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        oneItemImmediateResponse = intent.getBooleanExtra(ONE_ITEM_IMMEDIATE_RESPONSE, false);
        if (oneItemImmediateResponse && itemList.size() == 1) {
            ListItem item = itemList.get(0);
            if (item instanceof GeneralItem) {
                responseResult(((GeneralItem) item).getObj());
            }
        }

        PickPhoneNumbersAdapter adapter = new PickPhoneNumbersAdapter(itemList);
        adapter.setClickListener(this);
        phone_number_list.setAdapter(adapter);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void OnItemClick(View v, int position) {
        ListItem item = itemList.get(position);
        if (item instanceof GeneralItem) {
            responseResult(((GeneralItem) item).getObj());
        }
    }

    public void responseResult(JSONObject phone) {
        try {
            Intent intent = new Intent();
            intent.putExtra(RESPONSE_PHONE_NUMBER, phone.getString("number"));
            setResult(RESULT_OK, intent);
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}