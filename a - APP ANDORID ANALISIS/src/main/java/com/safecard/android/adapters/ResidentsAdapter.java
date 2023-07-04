package com.safecard.android.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.safecard.android.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by efajardo on 29/04/16.
 */
public class ResidentsAdapter extends SelectableAdapter<ResidentsAdapter.ResidentsViewHolder> {

    private JSONArray mResidents = new JSONArray();

    private OnItemClickListener clickListener;

    public interface OnItemClickListener{
        void OnItemClick(View v, int position);
    }

    public ResidentsAdapter(JSONArray residentsObj) {
        mResidents = residentsObj;
    }

    public void setClickListener(OnItemClickListener clickListener){
        this.clickListener = clickListener;
    }

    public class ResidentsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case
        public TextView mResidentName, mResidentMobile, mResidentType, mResidentStatus, mResidentRegistered;
        public ResidentsViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            //mSectorName = v;
            this.mResidentName = (TextView) v.findViewById(R.id.resident_name);
            this.mResidentMobile = (TextView) v.findViewById(R.id.resident_mobile);
            this.mResidentType = (TextView) v.findViewById(R.id.resident_type);
            this.mResidentStatus = (TextView) v.findViewById(R.id.resident_status);
            this.mResidentRegistered = (TextView) v.findViewById(R.id.resident_registered);
        }

        @Override
        public void onClick(View v) {
            if(clickListener!=null){
                clickListener.OnItemClick(v, getAdapterPosition());
            }
        }
    }

    @Override
    public ResidentsAdapter.ResidentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.residents_item, parent, false);

        return new ResidentsAdapter.ResidentsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ResidentsAdapter.ResidentsViewHolder holder, int position) {
        try {
            JSONObject resident_json = mResidents.getJSONObject(position);
            holder.mResidentName.setText(String.format("%s %s", resident_json.getString("name"), resident_json.getString("lastName")));
            holder.mResidentMobile.setText(resident_json.getString("mobile"));

            holder.mResidentStatus.setVisibility(View.GONE);
            holder.mResidentRegistered.setVisibility(View.GONE);

            //Tipo de residente
            if(resident_json.getString("owner").equals("1")){
                holder.mResidentType.setText(R.string.adapter_resident_owner);
            } else if(resident_json.getString("admin").equals("1")){
                holder.mResidentType.setText(R.string.adapter_resident_resident_admin);
            } else {
                holder.mResidentType.setText(R.string.adapter_resident_resident);
            }

            //Residente bloqueado/activo
            if(resident_json.getString("active").equals("0")){
                holder.mResidentStatus.setText(R.string.adapter_resident_blocked_resident);
                holder.mResidentStatus.setVisibility(View.VISIBLE);
            }

            //Residente registrado
            if(resident_json.getString("registered").equals("0")){
                holder.mResidentMobile.setVisibility(View.GONE);
                holder.mResidentName.setText(resident_json.getString("mobile"));
                holder.mResidentRegistered.setVisibility(View.VISIBLE);
                holder.mResidentRegistered.setText(R.string.adapter_resident_user_not_registered);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mResidents.length();
    }
}
