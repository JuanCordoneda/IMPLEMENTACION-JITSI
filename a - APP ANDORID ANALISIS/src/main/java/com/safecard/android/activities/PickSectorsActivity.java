package com.safecard.android.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.safecard.android.R;
import com.safecard.android.adapters.PickSectorAdapter;
import com.safecard.android.utils.DividerItemDecoration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PickSectorsActivity extends AppCompatActivity implements PickSectorAdapter.OnItemClickListener {

    public static final String ARG_SECTORS = "ARG_SECTORS";
    public static final String SELECTED_SECTORS_ID = "SELECTED_SECTORS_ID";
    public static final String TITLE = "TITLE";
    public static final String ARG_CARDINALITY = "ARG_CARDINALITY";
    public static final String PROPERTY_ID = "PROPERTY_ID";

    public static final int CARDINALITY_ONE = 1;
    public static final int CARDINALITY_MANY = 2;

    private int cardinality;
    private int propertyId;
    private PickSectorAdapter mAdapter;
    List<Sector> sectors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sectors);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_shape);

        Intent intent = getIntent();
        cardinality = intent.getIntExtra(ARG_CARDINALITY, CARDINALITY_ONE);


        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText("[TITLE]");
        String titleText = getIntent().getStringExtra(TITLE);
        if(titleText != null){
            title.setText(titleText);
        }

        propertyId = getIntent().getIntExtra(PROPERTY_ID, -1);

        Button btn_confirm = (Button) findViewById(R.id.btn_confirm);

        if(cardinality == CARDINALITY_ONE) {
            btn_confirm.setVisibility(View.GONE);
        }else if(cardinality == CARDINALITY_MANY) {
            btn_confirm.setVisibility(View.VISIBLE);
        }

        RecyclerView sector_list = (RecyclerView) findViewById(R.id.sectors_list);
        sector_list.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        sector_list.setLayoutManager(mLayoutManager);
        sector_list.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        sector_list.setItemAnimator(new DefaultItemAnimator());

        sectors = new ArrayList<>();
        Serializable SectorsSerializable = intent.getSerializableExtra(ARG_SECTORS);
        if(SectorsSerializable instanceof ArrayList){
            sectors = (ArrayList) SectorsSerializable;
        }
        ArrayList<Integer> selectedSectorsId = intent.getExtras().getIntegerArrayList(SELECTED_SECTORS_ID);

        mAdapter = new PickSectorAdapter(getApplicationContext(), sectors);

        sector_list.setAdapter(mAdapter);
        mAdapter.setClickListener(this);
        mAdapter.notifyDataSetChanged();

        for(int sectorId: selectedSectorsId){
            mAdapter.toggleSelectionBySectorId(sectorId);
        }


        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Integer> selectedSectorIds = mAdapter.getSelectedSectorIdList();

                if(selectedSectorIds.size() < 1){
                    AlertDialog.Builder builder = new AlertDialog.Builder(PickSectorsActivity.this);
                    builder.setTitle(R.string.activity_pick_sectors_dialog_title);

                    builder.setMessage(R.string.activity_pick_sectors_dialog_select_one_sector_at_least)
                            .setCancelable(false)
                            .setPositiveButton(R.string.activity_pick_sectors_dialog_ok_button, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                    return;
                }

                Intent intent = new Intent();
                intent.putExtra(SELECTED_SECTORS_ID, selectedSectorIds);
                intent.putExtra(PROPERTY_ID, propertyId);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public void onItemClick(View view, int position) {
        if(cardinality == CARDINALITY_ONE) {
            mAdapter.clearSelection();
            mAdapter.toggleSelection(position);
            Intent intent = new Intent();
            intent.putExtra(SELECTED_SECTORS_ID, mAdapter.getSelectedSectorIdList());
            intent.putExtra(PROPERTY_ID, propertyId);
            setResult(RESULT_OK, intent);
            finish();
        }else if(cardinality == CARDINALITY_MANY) {
            mAdapter.toggleSelection(position);
        }
    }

    public static class Sector implements Serializable {
        int id;
        String name;

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
    }
}
