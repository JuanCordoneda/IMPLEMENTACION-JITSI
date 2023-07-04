package com.safecard.android.adapters;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.safecard.android.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LogsAdapter extends SelectableAdapter<LogsAdapter.LogsViewHolder> {

	private static final String TAG = "LogsAdapter";
	//private JSONArray logs = new JSONArray();
	private ArrayList<JSONObject> logs = new ArrayList<JSONObject>();

	private boolean loading = false;
	private OnLoadMoreListener onLoadMoreListener;
	private int progressIndex = -1;

	public void addProgress() {
		progressIndex = logs.size();
		add(null);
	}

	public void removeProgress() {
		if(progressIndex >= 0) {
			remove(progressIndex);
			progressIndex = -1;
		}
	}

	private Context context;
	
	public interface OnLoadMoreListener {
		void onLoadMore();
	}
	public LogsAdapter(Context c) {
		logs = new ArrayList<JSONObject>();
		context = c;
	}
	public void setData(ArrayList<JSONObject> myDataSet) {
		logs.addAll(myDataSet);
	}

	public void add(JSONObject object) {
		logs.add(object);
		notifyItemInserted(logs.size() - 1);
	}

	public void remove(int index) {
		logs.remove(index);
		notifyItemRemoved(logs.size());
	}

	public void removeAll() {
		int i = logs.size();
		logs.clear();
		notifyItemRangeRemoved(0,i);
	}

	public void setRecyclerView(RecyclerView recyclerView) {
		final RecyclerView.LayoutManager mLayoutManager = recyclerView.getLayoutManager();
		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				if(logs.size()>0 && !loading && !recyclerView.canScrollVertically(1)){
					if (onLoadMoreListener != null) {
						onLoadMoreListener.onLoadMore();
						loading = true;
					}
				}
			}
		});
	}

	public void setLoaded() {
		loading = false;
	}

	public static class LogsViewHolder extends RecyclerView.ViewHolder {
		public LinearLayout mLinearLayout1, mImgContainer, mLoadingLayout;
		public ImageView mType;
		public TextView mDateLog, mTimeLog, mRelation, mGuestName, mAuthorizedBy, mSectorGate;

		public LogsViewHolder(View v) {
			super(v);
			this.mType = (ImageView) v.findViewById(R.id.type);
			this.mDateLog = (TextView) v.findViewById(R.id.date_log);
			this.mTimeLog = (TextView) v.findViewById(R.id.time_log);
			this.mRelation = (TextView) v.findViewById(R.id.relation);
			this.mGuestName = (TextView) v.findViewById(R.id.guest_name);
			this.mAuthorizedBy = (TextView) v.findViewById(R.id.authorized_by);
			this.mSectorGate = (TextView) v.findViewById(R.id.sector_gate);
			this.mLinearLayout1 = (LinearLayout) v.findViewById(R.id.linear_layout1);
			this.mImgContainer = (LinearLayout) v.findViewById(R.id.img_container);
			this.mLoadingLayout = (LinearLayout) v.findViewById(R.id.loading_layout);
		}
	}

	public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
		this.onLoadMoreListener = onLoadMoreListener;
	}

	@Override
	public LogsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// create a new view
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.logs_list, parent, false);

		return new LogsViewHolder(v);
	}

	@Override
	public void onBindViewHolder(LogsAdapter.LogsViewHolder holder, int position) {


		JSONObject log = logs.get(position);
		if (log != null){
			holder.mLinearLayout1.setVisibility(View.VISIBLE);
			holder.mImgContainer.setVisibility(View.VISIBLE);
			holder.mLoadingLayout.setVisibility(View.GONE);
		}else{
			holder.mLinearLayout1.setVisibility(View.GONE);
			holder.mImgContainer.setVisibility(View.GONE);
			holder.mLoadingLayout.setVisibility(View.VISIBLE);
			return;
		}
		try {
			String relation_name = "";

			if(log.getInt("is_service") == 1){

				relation_name = String.format(context.getString(R.string.adapter_log_service), log.getString("relation"));
				switch(Integer.parseInt(log.getString("type"))){
					case 0:
						holder.mType.setImageResource(R.drawable.ic_out_alert);
						relation_name = log.getString("relation");
						break;
					case 1:
						holder.mType.setImageResource(R.drawable.ic_alert);
						relation_name = log.getString("relation");
						break;
				}
			} else {
				switch(Integer.parseInt(log.getString("type"))){
					case 0:
						holder.mType.setImageResource(R.drawable.ic_out);
						relation_name = log.getString("relation");
						break;
					case 1:
						holder.mType.setImageResource(R.drawable.ic_in);
						relation_name = log.getString("relation");
						break;
					case 2:
						holder.mType.setImageResource(R.drawable.ic_alert);
						relation_name = context.getString(R.string.adapter_log_not_out_yet);
						break;
				}
			}

			if(log.getString("guest_name").equals("")){
				holder.mGuestName.setVisibility(View.GONE);
			} else {
				holder.mGuestName.setVisibility(View.VISIBLE);
				holder.mGuestName.setText(log.getString("guest_name"));
			}

			SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			Date date1 = null;
			try {
				date1 = dt.parse(log.getString("datetime"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			SimpleDateFormat date_format = new SimpleDateFormat("dd MMM yyyy");
			SimpleDateFormat time_format = new SimpleDateFormat("HH:mm");

			holder.mRelation.setText(relation_name);

			holder.mAuthorizedBy.setText(String.format(context.getString(R.string.adapter_log_authorized_by), log.getString("organizer_name")));
			String sector_gate = "";


			if(!log.getString("sector_name").equals("")){
				sector_gate = log.getString("sector_name") + " ";
			}

			if(!log.getString("gate_name").equals("")){
				sector_gate += log.getString("gate_name");
			}

			if(sector_gate.equals("")) {
				holder.mSectorGate.setVisibility(View.GONE);
			} else {
				holder.mSectorGate.setVisibility(View.VISIBLE);
				holder.mSectorGate.setText(sector_gate);
			}

			holder.mDateLog.setText(date_format.format(date1));
			holder.mTimeLog.setText(String.format(context.getString(R.string.adapter_log_hours), time_format.format(date1)));

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getItemCount() {
		return logs.size();
	}
}
