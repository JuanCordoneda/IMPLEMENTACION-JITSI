package com.safecard.android.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.safecard.android.R;
import com.safecard.android.adapters.SectorAdapter;
import com.safecard.android.utils.DividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;

public class SectorsActivity extends AppCompatActivity implements SectorAdapter.OnItemClickListener {

    private RecyclerView sector_list;
    private SectorAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Toolbar toolbar;
    private TextView title;
    private Button btn_confirm;

    JSONArray sectors_array;
    ArrayList<String> selectedItems = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sectors);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_shape);

        title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText(R.string.activity_sectors_title);

        btn_confirm = (Button) findViewById(R.id.btn_confirm);

        sector_list = (RecyclerView) findViewById(R.id.sectors_list);
        sector_list.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        sector_list.setLayoutManager(mLayoutManager);
        sector_list.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        sector_list.setItemAnimator(new DefaultItemAnimator());

        Bundle extras = getIntent().getExtras();
        try {
            sectors_array = new JSONArray(extras.getString("SECTORS"));
            selectedItems = extras.getStringArrayList("SELECTED_ITEMS");

            mAdapter = new SectorAdapter(getApplicationContext(), sectors_array);
            sector_list.setAdapter(mAdapter);
            mAdapter.setClickListener(this);
            mAdapter.notifyDataSetChanged();

            for(int k = 0; k < selectedItems.size(); k++){
                if(selectedItems.get(k).equals("true")){
                    toggleSelection(k);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                selectedItems.clear();
                int total_selected = 0;
                for(int j = 0; j < sectors_array.length(); j++){
                    selectedItems.add(j, mAdapter.isSelected(j) ? "true" : "false");
                    if(mAdapter.isSelected(j)){
                        total_selected++;
                    }
                }

                if(total_selected > 0){
                    returnIntent.putExtra("SELECTED_ITEMS", selectedItems);
                    setResult(ConfirmResidentActivity.PICK_SECTOR);
                    setResult(ConfirmResidentActivity.RESULT_OK, returnIntent);
                    finish();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SectorsActivity.this);
                    builder.setTitle(R.string.activity_sectors_dialog_title);

                    builder.setMessage(R.string.activity_sectors_dialog_select_one_sector_at_least)
                            .setCancelable(false)
                            .setPositiveButton(R.string.activity_sectors_dialog_ok_button, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }

            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
    }

    @Override
    public void onItemClick(View view, int position) {
        toggleSelection(position);
    }
}
