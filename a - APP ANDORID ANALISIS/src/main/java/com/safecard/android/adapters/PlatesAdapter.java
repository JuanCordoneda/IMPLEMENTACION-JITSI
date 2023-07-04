package com.safecard.android.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.safecard.android.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by efajardo on 30/12/16.
 */

public class PlatesAdapter extends SelectableAdapter<PlatesAdapter.PlatesViewHolder>{

    private List<JSONObject> plates_array = new ArrayList<>();
    private Boolean display_checkbox = false;

    public PlatesAdapter(List<JSONObject> plates){
        plates_array = plates;
    }

    public static class PlatesViewHolder extends RecyclerView.ViewHolder{
        public TextView mPlate;
        public CheckBox mCheckbox;
        public LinearLayout mCheckboxLay;
        public PlatesViewHolder(View v){
            super(v);
            mPlate = (TextView) v.findViewById(R.id.plate);
            mCheckbox = (CheckBox) v.findViewById(R.id.check_box);
            mCheckboxLay = (LinearLayout) v.findViewById(R.id.checkbox_lay);
        }
    }

    @Override
    public PlatesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.plate_item, parent, false);

        return new PlatesAdapter.PlatesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PlatesViewHolder holder, int position) {
        try {
            if(plates_array != null){
                JSONObject plate_json = plates_array.get(position);// getJSONObject(position);
                holder.mPlate.setText(plate_json.getString("number"));

                holder.mCheckbox.setEnabled(false);
                holder.mCheckbox.setOnCheckedChangeListener(null);
                holder.mCheckbox.setChecked(isSelected(position));
                holder.mCheckboxLay.setVisibility(display_checkbox.equals(true) ? View.VISIBLE : View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return plates_array.size();
    }

    public List<JSONObject> getPlateArray(){
        return plates_array;
    }

    public void removeItem(int position) {
        plates_array.remove(position);
        notifyItemRemoved(position);
    }

    private void removeRange(int positionStart, int itemCount) {
        for (int i = 0; i < itemCount; ++i) {
            plates_array.remove(positionStart);
        }
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    public void removeItems(List<Integer> positions) {
        // Reverse-sort the list
        Collections.sort(positions, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return rhs - lhs;
            }
        });

        // Split the list in ranges
        while (!positions.isEmpty()) {
            if (positions.size() == 1) {
                removeItem(positions.get(0));
                positions.remove(0);
            } else {
                int count = 1;
                while (positions.size() > count && positions.get(count).equals(positions.get(count - 1) - 1)) {
                    ++count;
                }

                if (count == 1) {
                    removeItem(positions.get(0));
                } else {
                    removeRange(positions.get(count - 1), count);
                }

                for (int i = 0; i < count; ++i) {
                    positions.remove(0);
                }
            }
        }
    }

    public void displayCheckbox(Boolean display){
        display_checkbox = display;
        notifyDataSetChanged();
    }
}
