package com.safecard.android.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.safecard.android.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by efajardo on 04-11-15.
 */
public class NotificationsAdapter extends SelectableAdapter<NotificationsAdapter.NotificationsViewHolder> {

    private ArrayList<JSONObject> notifications = new ArrayList<JSONObject>();

    public NotificationsAdapter(ArrayList<JSONObject> myDataSet) { notifications = myDataSet; }

    public static class NotificationsViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView mIcon;
        public TextView mDate, mTime, mMessage;

        public NotificationsViewHolder(View v) {
            super(v);
            this.mIcon = (ImageView) v.findViewById(R.id.icon);
            this.mDate = (TextView) v.findViewById(R.id.date);
            this.mTime = (TextView) v.findViewById(R.id.time);
            this.mMessage = (TextView) v.findViewById(R.id.message);
        }
    }

    @Override
    public NotificationsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);

        return new NotificationsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(NotificationsViewHolder holder, int position) {
        JSONObject notification = notifications.get(position);

        try {

            if(notification.getInt("type") == 1){
                holder.mIcon.setImageResource(R.drawable.ic_email);
            } else {
                holder.mIcon.setImageResource(R.drawable.ic_history_white);
            }

            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date1 = null;
            try {
                date1 = dt.parse(notification.getString("local_date"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat date_format = new SimpleDateFormat("dd MMM yyyy");
            SimpleDateFormat time_format = new SimpleDateFormat("HH:mm");

            holder.mDate.setText(date_format.format(date1));
            holder.mTime.setText(time_format.format(date1)+" hrs");

            holder.mMessage.setText(notification.getString("message"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }
}
