package com.safecard.android.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
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
 * Created by efajardo on 7/11/16.
 */

public class SectorAdapter extends SelectableAdapter<SectorAdapter.SectorViewHolder> {

    private JSONArray mDataset;
    private Context mContext;
    private SectorAdapter.OnItemClickListener clickListener = null;

    public SectorAdapter(Context context, JSONArray myDataSet){
        mContext = context;
        mDataset = myDataSet;
    }

    public void setClickListener(SectorAdapter.OnItemClickListener clickListener){
        this.clickListener = clickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class SectorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case
        public TextView mSectorName;
        public SectorViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            //mSectorName = v;
            this.mSectorName = (TextView) v.findViewById(R.id.sector_name);
        }

        @Override
        public void onClick(View v) {
          if(clickListener != null){
              clickListener.onItemClick(v, getAdapterPosition());
          }
        }
    }

    @Override
    public SectorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sector_item, parent, false);

        return new SectorViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SectorAdapter.SectorViewHolder holder, int position) {
        try {

            if(null != mDataset) {
                JSONObject sector_json = mDataset.optJSONObject(position);
                holder.mSectorName.setText(sector_json.getString("sector_name"));
            }

            if(isSelected(position)){

                int tintColor = ContextCompat.getColor(mContext, R.color.colorAccent);

                Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_tic);
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable.mutate(), tintColor);

                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                holder.mSectorName.setCompoundDrawables(null, null, drawable, null);

            } else {
                holder.mSectorName.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.length();
    }

}
