package com.safecard.android.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.safecard.android.R;
import com.safecard.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by efajardo on 5/04/17.
 */

public class DefaultsAccessTypeAdapter extends SelectableAdapter<DefaultsAccessTypeAdapter.AccessDefaultTypeSectorViewHolder>{

    private ArrayList<JSONObject> arraySectors = new ArrayList<JSONObject>();
    private int mHouseId;
    private Context mContext;
    private Listener mListener;
    private JSONObject sector_json = new JSONObject();
    private JSONObject defaultSectorControl = new JSONObject();

    public interface Listener {
        void onItemClick(JSONObject sector_json, String preference, int position);
    }

    public DefaultsAccessTypeAdapter(ArrayList<JSONObject> myDataSet, int house_id, Context context, Listener listener){
        arraySectors = myDataSet;
        mContext = context;
        mListener = listener;
        mHouseId = house_id;

        try {
            if(Utils.getDefaults("defaultSectorControl", context) != null){
                defaultSectorControl = new JSONObject(Utils.getDefaults("defaultSectorControl", mContext));

                for(int i = 0; i < arraySectors.size(); i++){
                    if(!defaultSectorControl.has(mHouseId + "-" + arraySectors.get(i).getString("sector_id"))){
                        if(arraySectors.get(i).getInt("use_rc") == 1 && arraySectors.get(i).getJSONArray("gates").length() > 0){
                            defaultSectorControl.put(mHouseId + "-" + arraySectors.get(i).getString("sector_id"), "CR");
                        } else {
                            defaultSectorControl.put(mHouseId + "-" + arraySectors.get(i).getString("sector_id"), "QR");
                        }
                    }
                }

            } else {
                for(int i = 0; i < arraySectors.size(); i++){
                    if(arraySectors.get(i).getInt("use_rc") == 1 && arraySectors.get(i).getJSONArray("gates").length() > 0){
                        defaultSectorControl.put(mHouseId + "-" + arraySectors.get(i).getString("sector_id"), "CR");
                    } else {
                        defaultSectorControl.put(mHouseId + "-" + arraySectors.get(i).getString("sector_id"), "QR");
                    }
                }
            }

            Utils.setDefaults("defaultSectorControl", defaultSectorControl.toString(), context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static class AccessDefaultTypeSectorViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mSectorName;
        public ImageView mCrDefault, mQrDefault;
        public Boolean only_qr = false, only_rc = false;

        public AccessDefaultTypeSectorViewHolder(View v) {
            super(v);
            this.mSectorName = (TextView) v.findViewById(R.id.sector_name);
            this.mCrDefault = (ImageView) v.findViewById(R.id.cr_default);
            this.mQrDefault = (ImageView) v.findViewById(R.id.qr_default);
        }

        public void bind(final JSONObject sector_json, final int mHouseId, final JSONObject defaultSectorControl, final Listener listener, final int position, final Context mContext) {

            try {
                mSectorName.setText(sector_json.getString("sector_name"));

                if(sector_json.getInt("use_rc") == 0 || sector_json.getJSONArray("gates").length() == 0){
                    mCrDefault.setVisibility(View.GONE);
                    only_qr = true;
                } else {
                    mCrDefault.setVisibility(View.VISIBLE);
                    only_qr = false;
                }

                if(sector_json.getInt("use_qr") == 0){
                    mQrDefault.setVisibility(View.GONE);
                    only_rc = true;
                } else {
                    mQrDefault.setVisibility(View.VISIBLE);
                    only_rc = false;
                }

                if(defaultSectorControl.get(mHouseId + "-" + sector_json.getString("sector_id")).equals("CR")){
                    mCrDefault.getDrawable().mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                    mCrDefault.setAlpha(1.0f);
                    mQrDefault.getDrawable().mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                    mQrDefault.setAlpha(0.3f);
                } else {
                    mQrDefault.getDrawable().mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                    mQrDefault.setAlpha(1.0f);
                    mCrDefault.getDrawable().mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                    mCrDefault.setAlpha(0.3f);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            mCrDefault.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    mCrDefault.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                    mCrDefault.setAlpha(1.0f);
                    mQrDefault.getDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                    mQrDefault.setAlpha(0.3f);
                    listener.onItemClick(sector_json, "CR", position);

                    if(only_rc){
                        Utils.showToast(mContext, mContext.getString(R.string.adapter_defaults_access_type_only_remote_control));
                    }
                }
            });

            mQrDefault.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    mQrDefault.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                    mQrDefault.setAlpha(1.0f);
                    mCrDefault.getDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                    mCrDefault.setAlpha(0.3f);
                    listener.onItemClick(sector_json, "QR", position);

                    if(only_qr){
                        Utils.showToast(mContext, mContext.getString(R.string.adapter_defaults_access_type_only_qr_control));
                    }
                }
            });
        }
    }

    @Override
    public DefaultsAccessTypeAdapter.AccessDefaultTypeSectorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.access_default_type_sector_item, parent, false);

        return new DefaultsAccessTypeAdapter.AccessDefaultTypeSectorViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AccessDefaultTypeSectorViewHolder holder, int position) {
        sector_json = arraySectors.get(position);

        holder.bind(sector_json, mHouseId, defaultSectorControl, mListener, position, mContext);
    }

    @Override
    public int getItemCount() {
        return arraySectors.size();
    }

    public void changeDefaultControl(JSONObject default_control, int position){
        defaultSectorControl = default_control;
        notifyItemChanged(position);
    }

}
