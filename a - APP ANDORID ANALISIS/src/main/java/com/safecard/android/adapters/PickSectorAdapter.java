package com.safecard.android.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.safecard.android.R;
import com.safecard.android.activities.PickSectorsActivity;

import java.util.ArrayList;
import java.util.List;

public class PickSectorAdapter extends SelectableAdapter<PickSectorAdapter.SectorViewHolder> {

    private List<PickSectorsActivity.Sector> sectors;
    private Context mContext;

    private PickSectorAdapter.OnItemClickListener clickListener = null;

    public PickSectorAdapter(Context context, List<PickSectorsActivity.Sector> sectors){
        this.mContext = context;
        this.sectors = sectors;
    }

    public void setClickListener(PickSectorAdapter.OnItemClickListener clickListener){
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
    public void onBindViewHolder(PickSectorAdapter.SectorViewHolder holder, int position) {

        if(null != sectors) {
            PickSectorsActivity.Sector sector = sectors.get(position);
            holder.mSectorName.setText(sector.getName());
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
    }

    @Override
    public int getItemCount() {
        return sectors.size();
    }

    public void toggleSelectionBySectorId(int sectorId) {
        for(int i = 0; i < sectors.size(); i++){
            if(sectors.get(i).getId() == sectorId){
                toggleSelection(i);
            }
        }
    }

    public ArrayList<Integer> getSelectedSectorIdList() {
        ArrayList<Integer> result = new ArrayList<>();
        for(int i = 0; i < sectors.size(); i++){
            if(isSelected(i)){
                result.add(sectors.get(i).getId());
            }
        }
        return result;
    }

}
