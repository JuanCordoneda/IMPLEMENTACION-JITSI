package com.safecard.android.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.safecard.android.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by alonso on 07/12/17.
 */

public class CreditCardsAdapter extends SelectableAdapter<CreditCardsAdapter.CreditCardsViewHolder> {

    private List<JSONObject> items;
    private Boolean display_checkbox = false;
    private int defaultPaymentMethodId = 1;

    public CreditCardsAdapter(List<JSONObject> items) {
        this.items = items;
    }

    public void setDefaultPaymentMethodId(int defaultPaymentMethodId) {
        this.defaultPaymentMethodId = defaultPaymentMethodId;
    }

    public int getCreditCardId(int i) {
        int result = -1;
        try {
            result = items.get(i).getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
    public String getCreditCardHint(int i) {
        String result = "";
        try {
            result = items.get(i).getString("hint");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static class CreditCardsViewHolder extends RecyclerView.ViewHolder {
        public TextView number;
        public CheckBox mCheckbox;
        public LinearLayout mCheckboxLay;
        public ImageView tic;

        public CreditCardsViewHolder(View v) {
            super(v);
            number = (TextView) v.findViewById(R.id.number);
            mCheckbox = (CheckBox) v.findViewById(R.id.check_box);
            mCheckboxLay = (LinearLayout) v.findViewById(R.id.checkbox_lay);
            tic = (ImageView) v.findViewById(R.id.ic_tic);
        }
    }

    @Override
    public CreditCardsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.credit_card_item, parent, false);
        return new CreditCardsAdapter.CreditCardsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CreditCardsViewHolder holder, int position) {
        try {
            if (items != null && items.size() > position) {
                //for (int i = 0; i < items.size(); ++i) {
                //    positions.remove(0);
                //}
                JSONObject item = items.get(position);
                holder.number.setText("  " + item.getString("hint"));

                holder.number.setCompoundDrawablesWithIntrinsicBounds(getIcon(item.getInt("payment_method_type_id")), 0, 0, 0);

                holder.mCheckbox.setEnabled(false);
                holder.mCheckbox.setOnCheckedChangeListener(null);
                holder.mCheckbox.setChecked(isSelected(position));
                holder.mCheckboxLay.setVisibility(display_checkbox.equals(true) ? View.VISIBLE : View.GONE);
                holder.tic.setVisibility(View.INVISIBLE);
                if(defaultPaymentMethodId == item.getInt("id")){
                    holder.tic.setVisibility(View.VISIBLE);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<JSONObject> getItems() {
        return items;
    }

    public void removeItems(List<Integer> positions) {
        Collections.sort(positions, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return rhs - lhs;
            }
        });
        while (!positions.isEmpty()) {
            int count = 1;
            while (positions.size() > count &&
                    positions.get(count).equals(positions.get(count - 1) - 1)) {
                ++count;
            }
            int positionStart = positions.get(count - 1);
            for (int i = 0; i < count; ++i) {
                items.remove(positionStart);
                Log.i("CreditCardsAdapter", "positionStart" + positionStart);
            }
            if (count == 1) {
                notifyItemRemoved(positionStart);
            } else {
                notifyItemRangeRemoved(positionStart, count);
            }
            for (int i = 0; i < count; ++i) {
                positions.remove(0);
            }
        }
    }

    public void removeItemById(String id) {
        try {
            for (int i = 0; i < items.size(); ++i) {
                if (items.get(i).getString("id").equals(id)) {
                    items.remove(i);
                    notifyItemRangeRemoved(i, 1);
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void displayCheckbox(Boolean display){
        display_checkbox = display;
        notifyDataSetChanged();
    }
}
