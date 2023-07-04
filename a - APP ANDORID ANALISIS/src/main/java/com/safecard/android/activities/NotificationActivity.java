package com.safecard.android.activities;
import android.content.Intent;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.safecard.android.Config;
import com.safecard.android.R;
import com.safecard.android.adapters.NotificationsAdapter;
import com.safecard.android.utils.DividerItemDecoration;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by efajardo on 04-11-15.
 */
public class NotificationActivity extends AppCompatActivity {

    private RecyclerView notif_list;
    private NotificationsAdapter mNotificationsAdapter;
    private TextView title;
    private Toolbar toolbar;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout swiperefresh;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_shape);

        swiperefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText(R.string.notification_title);

        notif_list = (RecyclerView) findViewById(R.id.listNotifications);
        notif_list.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        notif_list.setLayoutManager(mLayoutManager);
        notif_list.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        notif_list.setItemAnimator(new DefaultItemAnimator());

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });

        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateNotifications();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        populateNotifications();
    }

    private void populateNotifications(){

        final String url_notifications = Config.ApiUrl + "notifications/" +
                Utils.getMobile(getApplicationContext());

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

                        JSONArray notifications_array = new JSONArray(response.getString("notifications"));
                        ArrayList<JSONObject> notifications_list = new ArrayList<JSONObject>();

                        if(notifications_array.length() > 0){

                            for(int i = 0; i < notifications_array.length(); i++ ){
                                notifications_list.add(notifications_array.getJSONObject(i));
                            }

                            mNotificationsAdapter = new NotificationsAdapter(notifications_list);
                            notif_list.setAdapter(mNotificationsAdapter);
                        }
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

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if(swiperefresh.isRefreshing()){
            swiperefresh.setRefreshing(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }



}
