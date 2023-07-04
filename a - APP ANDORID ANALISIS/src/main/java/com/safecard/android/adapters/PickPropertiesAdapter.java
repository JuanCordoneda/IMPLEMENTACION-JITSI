package com.safecard.android.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.safecard.android.R;
import com.safecard.android.activities.InvitationCustomizationActivity;
import com.safecard.android.listitems.GeneralItem;
import com.safecard.android.listitems.ListItem;
import com.safecard.android.listitems.PickPropertyItem;
import com.safecard.android.listitems.TypePropertyItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by efajardo on 27/04/16.
 */
public class PickPropertiesAdapter extends SelectableAdapter<RecyclerView.ViewHolder> {

    private List<ListItem> items;

    private OnItemClickListener clickListener = null;
    private ArrayList<Integer> tickedPropertyIds;
    private ArrayList<Integer> warnedPropertyIds;
    private ArrayList<Integer> arrowedPropertyIds;
    private ArrayList<Integer> geolocatedPropertyIds;

    public interface OnItemClickListener {
        void OnItemClick(View v, int position);
    }

    //private String access_selected = "access_selected";

    public PickPropertiesAdapter(List<ListItem> myDataSet){
        items = myDataSet;
        tickedPropertyIds = new ArrayList<>();
        warnedPropertyIds = new ArrayList<>();
        arrowedPropertyIds = new ArrayList<>();
        geolocatedPropertyIds = new ArrayList<>();
    }

    public void setClickListener(OnItemClickListener clickListener){
        this.clickListener = clickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        Log.i("PickPropertiesAdapter", "viewType:"+ viewType);
        switch(viewType){
            case ListItem.TYPE_PICK_PROPERTY:
                View v1 = inflater.inflate(R.layout.pick_property_item, parent, false);
                viewHolder = new GeneralViewHolder(v1);
                break;
            case ListItem.TYPE_PROPERTY: //este el el tipo de los titulos de seccion
                View v2 = inflater.inflate(R.layout.property_type_item, parent, false);
                viewHolder = new TypeViewHolder(v2);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        switch(viewHolder.getItemViewType()){

            case ListItem.TYPE_PICK_PROPERTY:

                PickPropertyItem pickPropertyItem = (PickPropertyItem) items.get(position);
                InvitationCustomizationActivity.Property property = pickPropertyItem.getProperty();

                GeneralViewHolder holder = (GeneralViewHolder) viewHolder;
                holder.mHouseStudent.setText(property.getName());
                holder.mCondo.setText(property.getCondoName());

                holder.ic_tic.setVisibility(View.GONE);
                holder.ic_warning.setVisibility(View.GONE);
                holder.ic_arrow.setVisibility(View.GONE);
                holder.geolocated.setVisibility(View.GONE);

                if (tickedPropertyIds.contains(property.getId())) {
                    holder.ic_tic.setVisibility(View.VISIBLE);
                }

                if (warnedPropertyIds.contains(property.getId())) {
                    holder.ic_warning.setVisibility(View.VISIBLE);
                }

                if (arrowedPropertyIds.contains(property.getId())) {
                    holder.ic_arrow.setVisibility(View.VISIBLE);
                }

                if (geolocatedPropertyIds.contains(property.getId())) {
                    holder.geolocated.setVisibility(View.VISIBLE);
                }

                break;
            case ListItem.TYPE_PROPERTY:
                TypePropertyItem typePropertyItem = (TypePropertyItem) items.get(position);
                TypeViewHolder typeViewHolder = (TypeViewHolder) viewHolder;
                typeViewHolder.mTypeProperty.setText(typePropertyItem.getTypeProperty());

                break;
        }


    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setTickedPropertyIds(ArrayList<Integer> tickedPropertyIds){
        this.tickedPropertyIds = tickedPropertyIds;
    }

    public void setWarnedPropertyIds(ArrayList<Integer> warnedPropertyIds){
        this.warnedPropertyIds = warnedPropertyIds;
    }

    public void setArrowedPropertyIds(ArrayList<Integer> arrowedPropertyIds){
        this.arrowedPropertyIds = arrowedPropertyIds;
    }

    public void setGeolocatedPropertyIds(ArrayList<Integer> propertyIds){
        this.geolocatedPropertyIds = propertyIds;
    }

    public class GeneralViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView mHouseStudent, mCondo, geolocated;
        public ImageView ic_tic, ic_warning, ic_arrow;
        public GeneralViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            this.mHouseStudent = v.findViewById(R.id.house_name);
            this.mCondo = v.findViewById(R.id.condo_name);
            this.ic_tic = v.findViewById(R.id.ic_tic);
            this.ic_warning = v.findViewById(R.id.ic_warning);
            this.ic_arrow =  v.findViewById(R.id.ic_arrow);
            this.geolocated = v.findViewById(R.id.geolocated);
        }

        @Override
        public void onClick(View v) {
            if(clickListener!=null){
                clickListener.OnItemClick(v, getAdapterPosition());
            }
        }
    }

    public class TypeViewHolder extends RecyclerView.ViewHolder {
        public TextView mTypeProperty;
        public TypeViewHolder(View v){
            super(v);
            this.mTypeProperty = (TextView) v.findViewById(R.id.type_property);
        }
    }
}
