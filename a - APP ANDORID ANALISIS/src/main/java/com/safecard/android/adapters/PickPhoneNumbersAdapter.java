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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by efajardo on 27/04/16.
 */
public class PickPhoneNumbersAdapter extends SelectableAdapter<RecyclerView.ViewHolder> {

    private List<ListItem> items;

    private OnItemClickListener clickListener = null;

    public interface OnItemClickListener {
        void OnItemClick(View v, int position);
    }

    public PickPhoneNumbersAdapter(List<ListItem> myDataSet){
        items = myDataSet;
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
            case ListItem.TYPE_GENERAL:
                View v1 = inflater.inflate(R.layout.pick_phone_numbers_item, parent, false);
                viewHolder = new PickPhoneNumbersViewHolder(v1);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder.getItemViewType() == ListItem.TYPE_GENERAL) {
            try {
                GeneralItem item = (GeneralItem) items.get(position);
                PickPhoneNumbersViewHolder holder = (PickPhoneNumbersViewHolder) viewHolder;
                holder.name.setText(item.getObj().getString("name"));
                holder.number.setText(item.getObj().getString("number"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class PickPhoneNumbersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView name, number;

        public PickPhoneNumbersViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            this.name = v.findViewById(R.id.name);
            this.number = v.findViewById(R.id.number);
        }

        @Override
        public void onClick(View v) {
            if(clickListener!=null){
                clickListener.OnItemClick(v, getAdapterPosition());
            }
        }
    }
}
