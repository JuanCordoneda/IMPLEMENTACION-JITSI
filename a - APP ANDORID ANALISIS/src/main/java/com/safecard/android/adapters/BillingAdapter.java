package com.safecard.android.adapters;

import android.content.Context;
import android.graphics.Color;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.safecard.android.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

public class BillingAdapter extends SelectableAdapter<BillingAdapter.BillingViewHolder> {
    private static final String TAG = "BillingAdapter";

    private String[] months;

    private final DecimalFormat amountFormatter;
    //private JSONArray items = new JSONArray();
    private ArrayList<JSONObject> items = new ArrayList<JSONObject>();

    private boolean loading = false;
    private OnLoadMoreListener onLoadMoreListener;
    private int progressIndex = -1;
    private Listener listener;

    public void addProgress() {
        progressIndex = items.size();
        add(null);
    }

    public void removeProgress() {
        if(progressIndex >= 0) {
            remove(progressIndex);
            progressIndex = -1;
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public BillingAdapter(Listener listener) {
        items = new ArrayList<>();

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator(',');
        otherSymbols.setGroupingSeparator('.');
        amountFormatter = new DecimalFormat( "$###,###.###", otherSymbols);

        this.listener = listener;
    }

    public JSONObject getItem(int position){
        return items.get(position);
    }

    public void setData(ArrayList<JSONObject> myDataSet) {
        items.addAll(myDataSet);
    }

    public void add(JSONObject object) {
        items.add(object);
        notifyItemInserted(items.size() - 1);
    }

    public void remove(int index) {
        items.remove(index);
        notifyItemRemoved(items.size());
    }

    public void removeAll() {
        int i = items.size();
        items.clear();
        notifyItemRangeRemoved(0, i);
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        final RecyclerView.LayoutManager mLayoutManager = recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(items.size()>0 && !loading && !recyclerView.canScrollVertically(1)){
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore();
                        loading = true;
                    }
                }
            }
        });
    }

    public void setLoaded() {
        loading = false;
    }

    public static class BillingViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout item;
        public TextView month, day, time, parking, info, amount, amountMessage, paymentMethod;

        public BillingViewHolder(View v) {
            super(v);
            this.month = (TextView) v.findViewById(R.id.month);
            this.day = (TextView) v.findViewById(R.id.day);
            this.time = (TextView) v.findViewById(R.id.time);
            this.parking = (TextView) v.findViewById(R.id.parking);
            this.info = (TextView) v.findViewById(R.id.info);
            this.amount = (TextView) v.findViewById(R.id.amount);
            this.amountMessage = (TextView) v.findViewById(R.id.amount_message);
            this.paymentMethod = (TextView) v.findViewById(R.id.payment_method);
            this.item = (LinearLayout) v.findViewById(R.id.item);
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    @Override
    public BillingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.billing_item, parent, false);
        getStrings(v.getContext());
        return new BillingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(BillingAdapter.BillingViewHolder holder, final int position) {
        final JSONObject item = items.get(position);
        try {
            Log.i(TAG, "item:" + item.toString());
            holder.parking.setText(item.getString("name"));
            holder.month.setText(getMonthStr(item.getString("last_attempt_to_collect_date")));
            holder.day.setText(getDay(item.getString("last_attempt_to_collect_date")));
            holder.time.setText(getTime(item.getString("last_attempt_to_collect_date")));
            holder.amount.setText(amountFormatter.format(item.getInt("amount")));

            holder.info.setText(String.format(
                    "Acceso: %s\nSalida: %s\nTiempo a cobrar: %s min.",
                    getFormatedDateTime(item.getString("start_time")),
                    getFormatedDateTime(item.getString("finish_time")),
                    item.getString("time")));

            if(!item.getString("payment_method_type_id").equals("")
                    && !item.get("hint").equals("")){
                holder.paymentMethod.setVisibility(View.VISIBLE);
                holder.paymentMethod.setText(String.format("  %s", item.getString("hint")));
                holder.paymentMethod.setCompoundDrawablesWithIntrinsicBounds(
                        getIcon(item.getInt("payment_method_type_id")),
                        0, 0, 0);
            }else{
                holder.paymentMethod.setVisibility(View.GONE);
                holder.paymentMethod.setText("");
            }
            holder.amountMessage.setText("");
            if(item.getInt("paid") < 1) {
                if(item.getInt("failed_charge") == 1) {
                    holder.amountMessage.setText("Pendiente");
                    holder.amountMessage.setTextColor(Color.rgb(240,82,74));
                }else{
                    holder.amountMessage.setText("Procesando");
                    holder.amountMessage.setTextColor(Color.rgb(247,202,24));
                }
            }
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.OnItemClick(v, position);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface Listener{
        void OnItemClick(View v, int position);
    }

    private int getIcon(int paymentMethodTypeId) {
        switch(paymentMethodTypeId){
            case 1:
                return R.drawable.visa;
            case 2:
                return R.drawable.mastercard;
            case 3:
                return R.drawable.magna;
            case 4:
                return R.drawable.americanexpress;
            case 5:
                return R.drawable.dinersclub;
            default:
                return R.drawable.ic_change_qr;
        }
    }

    private String getTime(String time) {
        if(time.equals("")) {
            return "";
        }
        return time.substring(11,16);
    }

    private String getDay(String time) {
        if(time.equals("")) {
            return "";
        }
        return time.substring(8,10);
    }

    private String getMonthMM(String time) {
        if(time.equals("")) {
            return "";
        }
        return time.substring(5,7);
    }

    private String getYear(String time) {
        if(time.equals("")) {
            return "";
        }
        return time.substring(0,4);
    }

    private String getMonthStr(String time) {
        if(time.equals("")) {
            return "";
        }
        return months[Integer.parseInt(time.substring(5,7)) - 1];
    }

    private String getFormatedDateTime(String time) {
        if(time.equals("")) {
            return "";
        }
        String date = String.format("%s/%s/%s",
                getDay(time), getMonthMM(time), getYear(time));

        return String.format("%s %s", date, getTime(time));
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    private void getStrings(Context context) {
        months = new String[]{
                context.getString(R.string.adapter_defaults_access_type_month_jan),
                context.getString(R.string.adapter_defaults_access_type_month_feb),
                context.getString(R.string.adapter_defaults_access_type_month_mar),
                context.getString(R.string.adapter_defaults_access_type_month_apr),
                context.getString(R.string.adapter_defaults_access_type_month_may),
                context.getString(R.string.adapter_defaults_access_type_month_jun),
                context.getString(R.string.adapter_defaults_access_type_month_jul),
                context.getString(R.string.adapter_defaults_access_type_month_aug),
                context.getString(R.string.adapter_defaults_access_type_month_sep),
                context.getString(R.string.adapter_defaults_access_type_month_oct),
                context.getString(R.string.adapter_defaults_access_type_month_nov),
                context.getString(R.string.adapter_defaults_access_type_month_dec)};
    }
}
