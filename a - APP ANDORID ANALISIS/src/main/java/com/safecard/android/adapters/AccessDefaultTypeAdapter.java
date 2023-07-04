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
 * Created by efajardo on 12/12/16.
 */

public class AccessDefaultTypeAdapter extends SelectableAdapter<AccessDefaultTypeAdapter.AccessDefaultTypeViewHolder> {

    private ArrayList<JSONObject> arrayProperties = new ArrayList<JSONObject>();
    private Context mContext;
    private Listener mListener;
    private JSONObject property_json = new JSONObject();
    private JSONObject defaultControl = new JSONObject();

    public interface Listener {
        void onItemClick(JSONObject property_json, String preference, int position);
    }

    public AccessDefaultTypeAdapter(ArrayList<JSONObject> myDataSet, Context context, Listener listener){
        arrayProperties = myDataSet;
        mContext = context;
        mListener = listener;

        try {
            if(Utils.getDefaults("defaultControl", context) != null){
                defaultControl = new JSONObject(Utils.getDefaults("defaultControl", mContext));
            } else {
                for(int i = 0; i < arrayProperties.size(); i++){
                    defaultControl.put(arrayProperties.get(i).getString("house_id"), "CR");
                }
                Utils.setDefaults("defaultControl", defaultControl.toString(), context);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static class AccessDefaultTypeViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mHouseName, mCondoName;
        public ImageView mCrDefault, mQrDefault;

        public AccessDefaultTypeViewHolder(View v) {
            super(v);
            this.mHouseName = (TextView) v.findViewById(R.id.house_name);
            this.mCondoName = (TextView) v.findViewById(R.id.condo_name);
            this.mCrDefault = (ImageView) v.findViewById(R.id.cr_default);
            this.mQrDefault = (ImageView) v.findViewById(R.id.qr_default);
        }

        public void bind(final JSONObject property_json, final JSONObject defaultControl, final Listener listener, final int position) {

            try {
                mCondoName.setText(property_json.getString("condo_name"));
                mHouseName.setText(property_json.getString("house_name"));

                if(defaultControl.get(property_json.getString("house_id")).equals("CR")){
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
                    listener.onItemClick(property_json, "CR", position);
                }
            });

            mQrDefault.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    mQrDefault.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                    mQrDefault.setAlpha(1.0f);
                    mCrDefault.getDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                    mCrDefault.setAlpha(0.3f);
                    listener.onItemClick(property_json, "QR", position);
                }
            });
        }
    }

    @Override
    public AccessDefaultTypeAdapter.AccessDefaultTypeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.access_default_type_item, parent, false);

        return new AccessDefaultTypeAdapter.AccessDefaultTypeViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return arrayProperties.size();
    }

    @Override
    public void onBindViewHolder(final AccessDefaultTypeAdapter.AccessDefaultTypeViewHolder holder, int position) {
        property_json = arrayProperties.get(position);

        holder.bind(property_json, defaultControl, mListener, position);
    }

    public void changeDefaultControl(JSONObject default_control, int position){
        defaultControl = default_control;
        notifyItemChanged(position);
    }
}
