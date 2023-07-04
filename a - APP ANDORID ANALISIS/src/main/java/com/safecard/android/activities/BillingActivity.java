package com.safecard.android.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.safecard.android.Config;
import com.safecard.android.R;
import com.safecard.android.adapters.BillingAdapter;
import com.safecard.android.apicallers.ApiCallback;
import com.safecard.android.apicallers.ChargeApiCaller;
import com.safecard.android.utils.DividerItemDecoration;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



/**
 * Created by Alonso Gaete on 22-06-18.
 */
public class BillingActivity extends AppCompatActivity implements BillingAdapter.Listener {
    private static final String TAG = "BillingActivity";
    private BillingAdapter adapter;
    private SwipeRefreshLayout swiperefresh;
    private int page = 0;
    private TextView lastMovements;
    private Button actionButton;
    private ProgressDialog pDialog;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_shape);

        swiperefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.activity_billing_title));

        lastMovements = (TextView) findViewById(R.id.last_movements);
        RecyclerView list = (RecyclerView) findViewById(R.id.list);
        list.setHasFixedSize(true);


        pDialog = new ProgressDialog(BillingActivity.this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage(getString(R.string.activity_billing_sending_payment_authorization));
        pDialog.setCancelable(true);

        actionButton = (Button) findViewById(R.id.action_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if(pDialog.isShowing()) {
                    return;
                }
                pDialog.show();
                new ChargeApiCaller(getApplicationContext()).doCall(new ApiCallback() {
                    @Override
                    public void callSuccess(JSONObject json) {
                        hideDialog();
                        Utils.showToast(getApplicationContext(),
                                getString(R.string.activity_billing_successfully_charged));
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        finish();
                    }

                    @Override
                    public void callError(String errorType, String msg) {
                        hideDialog();
                        Utils.showToast(getApplicationContext(), msg);
                    }
                });
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });

        adapter =  new BillingAdapter(this);
        adapter.setRecyclerView(list);
        adapter.setOnLoadMoreListener(new BillingAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                Handler handler = new Handler();
                //Log.i(TAG, "onLoadMore");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Log.i(TAG, "onLoadMore postDelayed");
                        page++;
                        callApi();
                    }
                }, 1);
            }
        });

        list.setItemAnimator(new DefaultItemAnimator());
        list.setHasFixedSize(true);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        list.setAdapter(adapter);

        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resetList();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        resetList();
        actionButton.setVisibility(View.GONE);
        if(!Utils.getDefaultBoolean("active_parking", getApplicationContext())) {
            actionButton.setVisibility(View.VISIBLE);
        }
    }

    public void hideDialog(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(pDialog != null && pDialog.isShowing()) {
                    Utils.safeDismissDialog(BillingActivity.this, pDialog);
                }
            }
        }, 1000);
    }


    public void resetList(){
        adapter.removeAll();
        page = 0;
        callApi();
    }

    private void callApi(){
        final String url_notifications = Config.ApiUrl + "billings/" +
                Utils.getMobile(getApplicationContext()) + "/" + page;

        final RequestVolley rqv = new RequestVolley(url_notifications,getApplicationContext());

        swiperefresh.setRefreshing(true);

        rqv.requestApi(new RequestVolley.VolleyJsonCallback(){
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if(swiperefresh.isRefreshing()){
                        swiperefresh.setRefreshing(false);
                    }

                    if (response.getString("result").equals("ACK")) {
                        JSONArray json = new JSONArray(response.getString("billings"));
                        populate(json);
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
            }
        });
    }

    public void populate(final JSONArray json) {
        //Log.i(TAG, "populate" + json.length());
        //Log.i(TAG, "json:" + json.toString());

        for(int i = 0; i< json.length(); i++){
            try {
                adapter.add(json.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        adapter.setLoaded();
        lastMovements.setText(
                String.format(getString(R.string.activity_billing_last_x_movements), adapter.getItemCount()));

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(swiperefresh.isRefreshing()){
            swiperefresh.setRefreshing(false);
        }

        if(pDialog != null && pDialog.isShowing()) {
            Utils.safeDismissDialog(BillingActivity.this, pDialog);
        }
        pDialog = null;
    }

    @Override
    public void OnItemClick(View v, int position) {
        try {
            JSONObject item = adapter.getItem(position);
            String url = item.getString("dte_url");
            if(!url.equals("")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
