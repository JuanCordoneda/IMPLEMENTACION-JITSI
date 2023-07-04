package com.safecard.android.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.safecard.android.R;
import com.safecard.android.adapters.CreditCardsAdapter;
import com.safecard.android.model.PaymentMethodModel;
import com.safecard.android.model.modelutils.GenericModelCallback;
import com.safecard.android.utils.ClickListener;
import com.safecard.android.utils.DividerItemDecoration;
import com.safecard.android.utils.RecyclerTouchListener;
import com.safecard.android.utils.Utils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;



    public class WebPayEnrollmentUpdateActivity extends AppCompatActivity {

        private static final String TAG = "WebPayEnrollmentUpAct";

        private FloatingActionButton addButton;
        private CreditCardsAdapter adapter;
        private RecyclerView recyclerView;
        private ProgressDialog pDialog;
        private ActionMode actionMode;
        private SwipeRefreshLayout swipeRefresh;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_webpay_enrollment_update);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_shape);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
            title.setText(R.string.activity_webpay_enrollment_update_title);

            addButton = (FloatingActionButton) findViewById(R.id.add_button);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), WebPayEnrollmentWebViewActivity.class);
                    startActivity(intent);
               }
            });

            /*FloatingActionButton b = (FloatingActionButton) findViewById(R.id.b);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PaymentMethodModel.payAuthorize(getApplicationContext(),5000);
                }
            });*/

            recyclerView = (RecyclerView) findViewById(R.id.list);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.addItemDecoration(new DividerItemDecoration(
                    this, LinearLayoutManager.VERTICAL));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setHasFixedSize(true);
            recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(),
                    recyclerView, new ClickListener() {
                @Override
                public void onClick(View view, int position) {
                    if (actionMode != null){
                        adapter.toggleSelection(position);
                        int count = adapter.getSelectedItemCount();
                        actionMode.setTitle(String.valueOf(count));
                        actionMode.invalidate();
                    }else{
                        final int id = adapter.getCreditCardId(position);
                        final String hint = adapter.getCreditCardHint(position);
                        AlertDialog.Builder builder = new AlertDialog.Builder(WebPayEnrollmentUpdateActivity.this);
                        builder.setTitle(R.string.activity_webpay_enrollment_update_dialog_title_default_credit_card);
                        builder.setMessage(String.format(getString(R.string.activity_webpay_enrollment_update_dialog_change_default_credit_card), hint));
                        builder.setCancelable(false);
                        builder.setPositiveButton(R.string.activity_webpay_enrollment_update_dialog_accept_button, new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int wich){
                                PaymentMethodModel.setDefaultPaymentMethodId(getApplicationContext(), id);
                                updateRecyclerView();
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton(R.string.activity_webpay_enrollment_update_dialog_cancel_button, new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int wich){
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert_dialog = builder.create();
                        alert_dialog.show();

                    }
                }

                @Override
                public void onLongClick(View view, int position) {

                }
            }));

            pDialog = new ProgressDialog(this);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(true);

            swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    onResume();
                }
            });

            /*if (!PaymentMethodModel.arePaymentMethodEnrolled(getApplicationContext())) {
                Intent intent = new Intent(getApplicationContext(), WebPayEnrollmentWebViewActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return;
            }*/

            updateRecyclerView();

        }

        @Override
        public void onSaveInstanceState(Bundle savedInstanceState) {
            super.onSaveInstanceState(savedInstanceState);
        }

        @Override
        public void onResume(){
            super.onResume();
            swipeRefresh.setRefreshing(true);
            PaymentMethodModel.udpateList(getApplicationContext(), new GenericModelCallback() {
                @Override
                public void callSuccess(Object... objects) {
                    updateRecyclerView();
                    if(swipeRefresh.isRefreshing()){
                        swipeRefresh.setRefreshing(false);
                    }
                }

                @Override
                public void callError(String errorType, String msg) {
                    if (!msg.equals("")){
                        Utils.showToast(getApplicationContext(), msg);
                    }
                    if(swipeRefresh.isRefreshing()){
                        swipeRefresh.setRefreshing(false);
                    }
                }
            });
        }

        public void updateRecyclerView(){
            adapter = new CreditCardsAdapter(PaymentMethodModel.getAllList(getApplicationContext()));
            int defaultPaymentMethodId = PaymentMethodModel.getDefaultPaymentMethodId(getApplicationContext());
            if(defaultPaymentMethodId > -1){
                adapter.setDefaultPaymentMethodId(defaultPaymentMethodId);
            }
            recyclerView.setAdapter(adapter);
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.menu_plate, menu);
            return super.onCreateOptionsMenu(menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            switch (id) {
                case R.id.action_remove_plate:

                    if (actionMode == null) {
                        actionMode = startSupportActionMode(new ActionModeCallback());
                        actionMode.setTitle("0");
                        adapter.displayCheckbox(true);
                        addButton.setVisibility(View.INVISIBLE);
                    }

                    return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private class ActionModeCallback implements ActionMode.Callback {
            @SuppressWarnings("unused")
            private final String TAG = WebPayEnrollmentUpdateActivity.ActionModeCallback.class.getSimpleName();

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate (R.menu.menu_confirm, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_confirm_remove:
                        if(adapter.getSelectedItemCount() > 0){
                            removeItem(mode);
                        } else {
                            Utils.showToast(getApplicationContext(),getString(R.string.activity_webpay_enrollment_update_must_choose_one_card_at_least));
                        }
                        return true;

                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                adapter.clearSelection();
                adapter.displayCheckbox(false);
                addButton.setVisibility(View.VISIBLE);
                actionMode = null;
            }
        }

        public void removeItem(final ActionMode actionMode){

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(WebPayEnrollmentUpdateActivity.this);
            alertDialog.setMessage(R.string.activity_webpay_enrollment_update_dialog_confirm_delete_cards);
            alertDialog.setCancelable(false);

            alertDialog.setPositiveButton(R.string.activity_webpay_enrollment_update_dialog_yes_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    pDialog.setMessage(getString(R.string.activity_webpay_enrollment_update_dialog_deleting_cards));
                    pDialog.show();

                    final List<String> ids = new ArrayList<>();
                    final List<Integer> selectedItems = adapter.getSelectedItems();
                    try {
                        for(int i = 0; i < selectedItems.size(); i++){
                            ids.add(adapter.getItems().get(selectedItems.get(i)).getString("id"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    for (final String id : ids) {
                        PaymentMethodModel.remove(getApplicationContext(), id, new GenericModelCallback() {
                            @Override
                            public void callSuccess(Object... objects) {
                                boolean result = (boolean) objects[0];
                                if (result) {
                                    adapter.removeItemById(id);
                                }
                                if (pDialog != null && pDialog.isShowing()){
                                    Utils.safeDismissDialog(WebPayEnrollmentUpdateActivity.this, pDialog);
                                }
                                actionMode.finish();
                            }

                            @Override
                            public void callError(String errorType, String msg) {
                                if (pDialog != null && pDialog.isShowing()){
                                    Utils.safeDismissDialog(WebPayEnrollmentUpdateActivity.this, pDialog);
                                }
                                if (!msg.equals("")){
                                    Utils.showToast(getApplicationContext(), msg);
                                }
                            }
                        });
                    }
                }
            });

            alertDialog.setNegativeButton(R.string.activity_webpay_enrollment_update_dialog_no_button, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            alertDialog.show();

        }
    }
