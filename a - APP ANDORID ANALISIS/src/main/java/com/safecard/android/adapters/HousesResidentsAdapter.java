package com.safecard.android.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.safecard.android.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by efajardo on 19/01/17.
 */

public class HousesResidentsAdapter extends SelectableAdapter<HousesResidentsAdapter.HousesResidentsViewHolder> {

    private List<JSONObject> data_houses_residents = new ArrayList<>();
    private OnItemClickListener clickListener = null;

    public interface OnItemClickListener{
        void onIemClick(View v, int position);
    }

    public HousesResidentsAdapter(List<JSONObject> houses_residents){
        data_houses_residents = houses_residents;
    }

    public void setClickListener(OnItemClickListener clickListener){
        this.clickListener = clickListener;
    }

    public class HousesResidentsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mHouseName, mCondoName, mResidents;
        public ImageView mIcon;

        public HousesResidentsViewHolder(View v){
            super(v);
            v.setOnClickListener(this);
            mHouseName = (TextView) v.findViewById(R.id.house_name);
            mCondoName = (TextView) v.findViewById(R.id.condo_name);
            mResidents = (TextView) v.findViewById(R.id.residents);
            mIcon = (ImageView) v.findViewById(R.id.ic_icon);
        }

        @Override
        public void onClick(View v) {
            if(clickListener != null){
                clickListener.onIemClick(v, getAdapterPosition());
            }
        }
    }

    @Override
    public HousesResidentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.house_residents_item, parent, false);

        return new HousesResidentsAdapter.HousesResidentsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(HousesResidentsViewHolder holder, int position) {
        try {
            if(data_houses_residents != null){
                JSONObject house_residents_json = data_houses_residents.get(position);// getJSONObject(position);
                holder.mHouseName.setText(house_residents_json.getString("house_name"));
                holder.mCondoName.setText(house_residents_json.getString("condo_name"));

                JSONArray residents = new JSONArray(house_residents_json.getString("residents"));
                String residents_name = "";
                if(residents.length() > 0){
                    for(int i = 0; i < residents.length(); i++){
                        residents_name += residents.getJSONObject(i).getString("name") + " " + residents.getJSONObject(i).getString("lastName")+", ";
                    }
                    holder.mResidents.setText(residents_name.substring(0, residents_name.length() - 2));
                } else {
                    holder.mResidents.setText("");
                }

                if(house_residents_json.getBoolean("active") == true && (house_residents_json.getInt("admin") == 1 || house_residents_json.getInt("owner") == 1)){
                    holder.mIcon.setImageResource(R.drawable.ic_chevron_right_white_24dp);
                } else {
                    holder.mIcon.setImageResource(R.drawable.com_mixpanel_android_ic_warning);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return data_houses_residents.size();
    }
}
