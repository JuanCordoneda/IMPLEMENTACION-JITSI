package com.safecard.android.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.safecard.android.R;
import com.safecard.android.adapters.PickPropertiesAdapter;
import com.safecard.android.listitems.ListItem;
import com.safecard.android.listitems.PickPropertyItem;
import com.safecard.android.listitems.TypePropertyItem;
import com.safecard.android.utils.DividerItemDecoration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PickPropertiesActivity extends AppCompatActivity implements PickPropertiesAdapter.OnItemClickListener {

    private static final String TAG = "PickPropertiesActivity";

    public static final String PROPERTIES = "PROPERTIES";

    public static final String SELECTED_PROPERTY_ID = "SELECTED_PROPERTY_ID";

    public static final String ONE_ITEM_IMMEDIATE_RESPONSE = "ONE_ITEM_IMMEDIATE_RESPONSE";
    public static final String TICKED_PROPERTY_IDS = "TICKED_PROPERTY_IDS";
    public static final String ARROWED_PROPERTY_IDS = "ARROWED_PROPERTY_IDS";
    public static final String GEOLOCATED_PROPERTY_IDS = "GEOLOCATED_PROPERTY_IDS";
    public static final String ICON_WARNING_PROPERTY_IDS = "ICON_WARNING_PROPERTY_IDS";

    public static final String WARNINGS = "WARNINGS";

    ArrayList<Integer> arrowedPropertyIds;
    List<ListItem> itemList = new ArrayList<>();
    boolean oneItemImmediateResponse;

    ArrayList<Warning> warnings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_properties);


        Intent intent = getIntent();
        ArrayList<InvitationCustomizationActivity.Property> properties;
        properties = new ArrayList<>();
        Serializable propertiesSerializable = intent.getSerializableExtra(PROPERTIES);
        if(propertiesSerializable instanceof ArrayList){
            properties = (ArrayList) propertiesSerializable;
        }

        warnings = new ArrayList<>();
        Serializable warningsSerializable = intent.getSerializableExtra(WARNINGS);
        if(warningsSerializable instanceof ArrayList){
            warnings = (ArrayList) warningsSerializable;
        }

        ArrayList<Integer> iconWarningPropertyIds = intent.getIntegerArrayListExtra(ICON_WARNING_PROPERTY_IDS);
        if(iconWarningPropertyIds == null){
            iconWarningPropertyIds = new ArrayList<>();
        }

        ArrayList<Integer> tickedPropertyIds = intent.getIntegerArrayListExtra(TICKED_PROPERTY_IDS);
        if(tickedPropertyIds == null){
            tickedPropertyIds = new ArrayList<>();
        }

        arrowedPropertyIds = intent.getIntegerArrayListExtra(ARROWED_PROPERTY_IDS);
        if(arrowedPropertyIds == null){
            arrowedPropertyIds = new ArrayList<>();
        }

        ArrayList<Integer> geolocatedPropertyIds = intent.getIntegerArrayListExtra(GEOLOCATED_PROPERTY_IDS);
        if(geolocatedPropertyIds == null){
            geolocatedPropertyIds = new ArrayList<>();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_shape);

        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText(R.string.activity_pick_properties_title);

        RecyclerView properties_list = (RecyclerView) findViewById(R.id.properties_list);
        properties_list.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        properties_list.setLayoutManager(mLayoutManager);

        properties_list.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        Map<String, List<InvitationCustomizationActivity.Property>> groupedProperties = new HashMap<>();

        for(int i = 0; i < properties.size(); i++){
            InvitationCustomizationActivity.Property property = properties.get(i);
            if(!groupedProperties.containsKey("Propiedades")){
                List<InvitationCustomizationActivity.Property> list = new ArrayList<>();
                groupedProperties.put("Propiedades", list);
            }

            List<InvitationCustomizationActivity.Property> list = groupedProperties.get("Propiedades");
            list.add(property);
        }

        groupedProperties = sortByKeys(groupedProperties);

        for(String type : groupedProperties.keySet()){
            TypePropertyItem item = new TypePropertyItem();
            item.setTypeProperty(type);
            itemList.add(item);

            for(InvitationCustomizationActivity.Property property : groupedProperties.get(type)){
                //Log.i(TAG,"property getname:" + property.getName());
                PickPropertyItem item2 = new PickPropertyItem();
                item2.setProperty(property);
                itemList.add(item2);
            }
        }

        PickPropertiesAdapter adapter = new PickPropertiesAdapter(itemList);
        adapter.setTickedPropertyIds(tickedPropertyIds);
        adapter.setWarnedPropertyIds(iconWarningPropertyIds);
        adapter.setArrowedPropertyIds(arrowedPropertyIds);
        adapter.setGeolocatedPropertyIds(geolocatedPropertyIds);
        adapter.setClickListener(this);

        properties_list.setAdapter(adapter);

        oneItemImmediateResponse = intent.getBooleanExtra(ONE_ITEM_IMMEDIATE_RESPONSE, false);
        if(oneItemImmediateResponse && properties.size() == 1){
            responseResult(properties.get(0), true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public static <K extends Comparable,V> Map<K,V> sortByKeys(Map<K,V> map){
        List<K> keys = new LinkedList<K>(map.keySet());
        Collections.reverse(keys);

        //LinkedHashMap will keep the keys in the order they are inserted
        //which is currently sorted on natural ordering
        Map<K,V> sortedMap = new LinkedHashMap<K,V>();
        for(K key: keys){
            sortedMap.put(key, map.get(key));
        }

        return sortedMap;
    }

    @Override
    public void OnItemClick(View v, int position) {
        ListItem item = itemList.get(position);
        if(item instanceof PickPropertyItem) {
            PickPropertyItem pickPropertyItem = (PickPropertyItem) item;
            InvitationCustomizationActivity.Property property = pickPropertyItem.getProperty();
            responseResult(property, false);
        }
    }

    public void responseResult(final InvitationCustomizationActivity.Property property, final boolean forceFinish) {
        Warning warning = getWarning(property.getId());
        if (warning != null) {
            //Log.i("warningMessage", "warningMessage::" + warning.getMessage());

            if(!warning.getMessage().equals("")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(Html.fromHtml(property.getName() + " - " + property.getCondoName()));
                builder.setMessage(warning.getMessage());
                builder.setCancelable(false);

                if(warning.canPropertyBeSelected) {
                    builder.setPositiveButton(R.string.activity_pick_properties_dialog_continue_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.putExtra(SELECTED_PROPERTY_ID, property.getId());
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });
                    builder.setNegativeButton(R.string.activity_pick_properties_dialog_cancel_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                }else {
                    builder.setPositiveButton(R.string.activity_pick_properties_dialog_ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (forceFinish) {
                                PickPropertiesActivity.this.finish();
                            }
                        }
                    });
                }
                builder.create().show();
            }
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(SELECTED_PROPERTY_ID, property.getId());
        setResult(RESULT_OK, intent);

        finish();

    }

    private Warning getWarning(int propertyId) {
        for(Warning warning : warnings){
            if(warning.getPropertyIds().contains(propertyId)){
                return warning;
            }
        }
        return null;
    }

    public static ArrayList<Integer> getIdList(ArrayList<InvitationCustomizationActivity.Property> properties){
        ArrayList<Integer> list = new ArrayList<>();
        for(InvitationCustomizationActivity.Property p: properties){
            list.add(p.getId());
        }
        return list;
    }

    public static class Warning implements Serializable {
        private ArrayList<Integer> propertyIds = new ArrayList<>();
        private String message = "";
        private boolean canPropertyBeSelected = false;

        public ArrayList<Integer> getPropertyIds() {
            return propertyIds;
        }

        public void setPropertyIds(ArrayList<Integer> propertyIds) {
            this.propertyIds = propertyIds;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setCanPropertyBeSelected(boolean b) {
            this.canPropertyBeSelected = b;
        }

        public boolean canPropertyBeSelected() {
            return canPropertyBeSelected;
        }
    }
}