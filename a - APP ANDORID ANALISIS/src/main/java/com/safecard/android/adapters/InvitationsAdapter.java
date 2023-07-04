package com.safecard.android.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.safecard.android.R;
import com.safecard.android.fragments.InvitationListFragment;
import com.safecard.android.listitems.GeneralItem;
import com.safecard.android.listitems.ListItem;
import com.safecard.android.listitems.TypeInvitationItem;
import com.safecard.android.utils.CustomTypefaceSpan;
import com.safecard.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.safecard.android.fragments.InvitationListFragment.RECEIVED_INVITATION_REQUESTS;

public class InvitationsAdapter extends SelectableAdapter<RecyclerView.ViewHolder> {

	public static final int REQUEST_REJECT = 100001;
	public static final int REQUEST_ACCEPT = 100002;
	public static final int REQUEST_REMIND = 100003;
	public static final int REQUEST_RESEND = 100004;

	private List<ListItem> consolidatedInvitationList = new ArrayList<>();
	private GeneralViewHolder holder;
	private String invitationType;
	private Listener mListener;
	private Context mContext;

	private Boolean display_checkbox = false;

	public InvitationsAdapter(List<ListItem> consolidatedList, Listener listener, Context context) {
		consolidatedInvitationList = consolidatedList;
		mListener = listener;
		mContext = context;
	}

	@Override
	public int getItemViewType(int position) {
		return consolidatedInvitationList.get(position).getType();
	}

	public void setInvitationType(String type){
		invitationType = type;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// create a new view
		RecyclerView.ViewHolder viewHolder = null;
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());

		//View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.invitation_item, parent, false);

		switch(viewType){
			case ListItem.TYPE_GENERAL:

				View v1 = inflater.inflate(R.layout.invitation_item, parent, false);
				viewHolder = new GeneralViewHolder(v1);
				break;

			case ListItem.TYPE_STATUS:

				View v2 = inflater.inflate(R.layout.invitation_status_item, parent, false);
				viewHolder = new StatusViewHolder(v2);
				break;
		}

		return viewHolder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
		try {
			//invitation_json = arrayInvitations.getJSONObject(position);
			switch(viewHolder.getItemViewType()){
				case ListItem.TYPE_GENERAL:
					GeneralItem generalItem = (GeneralItem) consolidatedInvitationList.get(position);

					holder = (GeneralViewHolder) viewHolder;
					JSONObject invitation_json = generalItem.getObj();

					holder.mRecinto.setText(String.format("%s - %s",
							invitation_json.getString("house_name"),
							invitation_json.getString("condo_name")));
					String invitationSubject = mContext.getString(R.string.adapter_invitations_no_subject);
					if (!invitation_json.getString("subject").equals("")){
						invitationSubject= invitation_json.getString("subject");
					}
					String subjectStr = String.format(
							mContext.getString(R.string.adapter_invitations_subject),
							invitationSubject);
					holder.mSubject.setText(subjectStr);

					// El deleted manda por sobre active
					//bloqueo usuario > active = 0 Bloqueada
					//bloqueo propiedad > active = 0 Bloqueada
					//eliminar usuario de propiedad > deleted = 1, active = 0 (Eliminada)
					//fecha termino vencida expired = 1 (Vencida)
					//invitacion eliminada, deleted = 1 (Eliminada)


					holder.requestAcceptedTag.setVisibility(View.GONE);


					if (invitation_json.has("request_item_type")) {
						//la invitation es de seccion solicitadas

						holder.mShareInv.setVisibility(View.GONE);
						holder.mDetailInv.setVisibility(View.GONE);
						holder.mStatus.setVisibility(View.GONE);
						holder.mViewInv.setVisibility(View.GONE);
						holder.mOrganizerName.setTypeface(null, Typeface.NORMAL);
						holder.btnRequestLayout.setVisibility(View.GONE);
						holder.btnRemindContainer.setVisibility(View.GONE);
						holder.btnReRequestContainer.setVisibility(View.GONE);

						if (InvitationListFragment.RECEIVED_INVITATION_REQUESTS.equals(invitation_json.getString("request_item_type"))) {
							holder.btnRequestLayout.setVisibility(View.VISIBLE);
						}else if (InvitationListFragment.PENDING_INVITATION_REQUESTS.equals(invitation_json.getString("request_item_type"))) {
							holder.btnRemindContainer.setVisibility(View.VISIBLE);
						}else if (InvitationListFragment.REJECTED_INVITATION_REQUESTS.equals(invitation_json.getString("request_item_type"))) {
							holder.btnReRequestContainer.setVisibility(View.VISIBLE);
						}
					}
					else if (Integer.parseInt(invitation_json.getString("deleted")) == 1) {
						holder.mShareInv.setVisibility(View.GONE);
						holder.mDetailInv.setVisibility(View.GONE);
						holder.mStatus.setVisibility(View.VISIBLE);
						holder.mViewInv.setVisibility(View.GONE);
						holder.mOrganizerName.setTypeface(null, Typeface.NORMAL);

						holder.mStatus.setText(R.string.adapter_invitations_deleted);

						holder.btnRequestLayout.setVisibility(View.GONE);
						holder.btnRemindContainer.setVisibility(View.GONE);
						holder.btnReRequestContainer.setVisibility(View.GONE);
					} else if (Integer.parseInt(invitation_json.getString("active")) == 0) {
						holder.mShareInv.setVisibility(View.GONE);
						holder.mDetailInv.setVisibility(View.GONE);
						holder.mStatus.setVisibility(View.VISIBLE);
						holder.mViewInv.setVisibility(View.GONE);
						holder.mOrganizerName.setTypeface(null, Typeface.NORMAL);

						holder.mStatus.setText(R.string.adapter_invitations_blocked);

						holder.btnRequestLayout.setVisibility(View.GONE);
						holder.btnRemindContainer.setVisibility(View.GONE);
						holder.btnReRequestContainer.setVisibility(View.GONE);
					} else if (Integer.parseInt(invitation_json.getString("expired")) == 1) {
						holder.mShareInv.setVisibility(View.GONE);
						holder.mDetailInv.setVisibility(View.GONE);
						holder.mStatus.setVisibility(View.VISIBLE);
						holder.mViewInv.setVisibility(View.GONE);
						holder.mOrganizerName.setTypeface(null, Typeface.NORMAL);

						holder.mStatus.setText(R.string.adapter_invitations_expired);

						holder.btnRequestLayout.setVisibility(View.GONE);
						holder.btnRemindContainer.setVisibility(View.GONE);
						holder.btnReRequestContainer.setVisibility(View.GONE);
					} else {
						holder.mShareInv.setVisibility(View.VISIBLE);
						holder.mDetailInv.setVisibility(View.VISIBLE);
						holder.mStatus.setVisibility(View.GONE);

						holder.btnRequestLayout.setVisibility(View.GONE);
						holder.btnRemindContainer.setVisibility(View.GONE);
						holder.btnReRequestContainer.setVisibility(View.GONE);

						if(Utils.getDefaults("invitations_viewed", mContext) != null){
							JSONObject invitations_viewed = new JSONObject(Utils.getDefaults("invitations_viewed", mContext));
							if(!invitations_viewed.has(String.valueOf(invitation_json.getInt("id"))) || invitations_viewed.get(String.valueOf(invitation_json.getInt("id"))).equals(false)){
								holder.mViewInv.setVisibility(View.VISIBLE);
								holder.mOrganizerName.setTypeface(null, Typeface.BOLD);
							} else {
								holder.mViewInv.setVisibility(View.GONE);
							}
						} else {
							holder.mOrganizerName.setTypeface(null, Typeface.BOLD);
						}
					}

					switch(invitationType){
						case InvitationListFragment.RECEIVED_INVITATIONS:
							if(invitation_json.has("requested")
									&& invitation_json.getInt("requested") == 1) {
								holder.requestAcceptedTag.setVisibility(View.VISIBLE);
							}
							holder.mOrganizerName.setText(invitation_json.getString("organizer_name"));
							holder.mShareInv.setVisibility(View.GONE);
							break;
						case InvitationListFragment.SENT_INVITATIONS:
							if(invitation_json.has("requested")
									&& invitation_json.getInt("requested") == 1) {
								holder.requestAcceptedTag.setVisibility(View.VISIBLE);
							}
							holder.mOrganizerName.setText(invitation_json.getString("guest_name"));
							holder.mDetailInv.setVisibility(View.GONE);
							holder.mViewInv.setVisibility(View.GONE);
							break;
						case InvitationListFragment.REQUESTED_INVITATIONS:

							holder.mOrganizerName.setText(invitation_json.getString("guest_name"));
							holder.mDetailInv.setVisibility(View.GONE);
							holder.mViewInv.setVisibility(View.GONE);
							
							if (invitation_json.has("request_item_type")) {
								String requestItemType = invitation_json.getString("request_item_type");
								if (InvitationListFragment.PENDING_INVITATION_REQUESTS.equals(requestItemType)
										|| InvitationListFragment.REJECTED_INVITATION_REQUESTS.equals(requestItemType)) {
									holder.mOrganizerName.setText(invitation_json.getString("organizer_name"));
								}
							}

							break;
					}

					String[] days_tmp = invitation_json.getString("days").split("~", -1);

					holder.mDaysList.setVisibility(days_tmp.length < 7 ? View.VISIBLE : View.GONE);

					Typeface typefaceRobotoBold = ResourcesCompat.getFont(mContext, R.font.roboto_bold);
					Typeface typefaceRobotoRegular = ResourcesCompat.getFont(mContext, R.font.roboto_regular);
					CustomTypefaceSpan typefaceSpanBold = new CustomTypefaceSpan(typefaceRobotoBold);
					CustomTypefaceSpan typefaceSpanRegular = new CustomTypefaceSpan(typefaceRobotoRegular);

					List selectedDays = Arrays.asList(days_tmp);
					String[] dayLetters = new String[]{
							mContext.getString(R.string.sunday_first_letter),
							mContext.getString(R.string.monday_first_letter),
							mContext.getString(R.string.tuesday_first_letter),
							mContext.getString(R.string.wednesday_first_letter),
							mContext.getString(R.string.thursday_first_letter),
							mContext.getString(R.string.friday_first_letter),
							mContext.getString(R.string.saturday_first_letter)};

					TextView[] dayTextViews = {
							holder.mDom, holder.mLun, holder.mMar, holder.mMie,
							holder.mJue, holder.mVie, holder.mSab
					};

					for(int i = 0; i < 7; i++) {
						CustomTypefaceSpan tf = typefaceSpanRegular;
						int color = ContextCompat.getColor(mContext, R.color.darkergray);
						if(selectedDays.contains(i+"")){
							tf = typefaceSpanBold;
							color = Color.WHITE;
						}
						dayTextViews[i].setTextColor(color);
						SpannableStringBuilder sb = new SpannableStringBuilder();
						sb.append(dayLetters[i]);
						sb.setSpan(tf, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
						dayTextViews[i].setText(sb);
					}

					//Agrega fecha y hora. Agrega "todos los dias de la semana" solo si es para todos
					holder.mStartEndDate.setText(
							formatDateTimeInvitation(
									invitation_json.getString("start_date"),
									invitation_json.getString("end_date"),
									invitation_json.getString("days"),
									mContext));

					holder.mPlate.setVisibility(View.GONE);
					if(invitation_json.has("plates") && invitation_json.getJSONArray("plates").length() > 0){
						String plate = String.format(mContext.getString(R.string.adapter_invitations_plate), invitation_json.getJSONArray("plates").get(0));
						holder.mPlate.setVisibility(View.VISIBLE);
						holder.mPlate.setText(plate);
					}

					final String invitation_id_final = invitation_json.getString("id");

					holder.mShareInv.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mListener.shareInvitation(invitation_id_final);
						}
					});

					holder.mCheckBox.setOnCheckedChangeListener(null);

					holder.mCheckBox.setChecked(isSelected(position));
					holder.mCheckboxLay.setVisibility(display_checkbox.equals(true) ? View.VISIBLE : View.GONE);

					break;
				case ListItem.TYPE_STATUS:
					TypeInvitationItem typeInvitationItem = (TypeInvitationItem) consolidatedInvitationList.get(position);

					StatusViewHolder statusViewHolder = (StatusViewHolder) viewHolder;

					statusViewHolder.mStatus.setText(typeInvitationItem.getStatusInvitation());

					break;
			}


		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	// View holder for general row item
	public class GeneralViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{
		public TextView mOrganizerName, mStartEndDate, mPlate, mRecinto, mSubject, mStatus, requestAcceptedTag;
		public TextView mLun, mMar, mMie, mJue, mVie, mSab, mDom;
		public LinearLayout mDaysList, mCheckboxLay, btnRequestLayout, btnRemindContainer, btnReRequestContainer;
		public ImageView mShareInv, mDetailInv, mViewInv;
		public CheckBox mCheckBox;

		public TextView btnRequestReject, btnRequestAccept, btnRemind, btnReRequest;

		public GeneralViewHolder(View v) {
			super(v);
			v.setOnClickListener(this);
			this.mSubject = (TextView) v.findViewById(R.id.subject);
			this.mOrganizerName = (TextView) v.findViewById(R.id.organizer_name);
			this.mStartEndDate = (TextView) v.findViewById(R.id.start_end_date);
			this.mPlate = (TextView) v.findViewById(R.id.plate);
			this.mRecinto = (TextView) v.findViewById(R.id.recinto);
			this.mStatus = (TextView) v.findViewById(R.id.status_invitation);
			this.requestAcceptedTag = (TextView) v.findViewById(R.id.request_accepted_tag);
			this.mShareInv = (ImageView) v.findViewById(R.id.share_inv);
			this.mDetailInv = (ImageView) v.findViewById(R.id.detail_inv);
			this.mViewInv = (ImageView) v.findViewById(R.id.view_inv);
			this.mCheckBox = (CheckBox) v.findViewById(R.id.check_box);
			this.mDaysList = (LinearLayout) v.findViewById(R.id.days_list);
			this.mCheckboxLay = (LinearLayout) v.findViewById(R.id.checkbox_lay);
			this.btnRequestLayout = v.findViewById(R.id.btn_request);
			this.btnRequestReject = v.findViewById(R.id.btn_request_reject);
			this.btnRequestReject.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mListener!=null){
						mListener.OnSubItemClick(v, getAdapterPosition(), InvitationsAdapter.REQUEST_REJECT);
					}
				}
			});
			this.btnRequestAccept = v.findViewById(R.id.btn_request_accept);
			this.btnRequestAccept.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mListener!=null){
						mListener.OnSubItemClick(v, getAdapterPosition(), InvitationsAdapter.REQUEST_ACCEPT);
					}
				}
			});
			this.btnRemindContainer = v.findViewById(R.id.btn_remind_container);
			this.btnRemind = v.findViewById(R.id.btn_remind);
			this.btnRemind.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mListener!=null){
						mListener.OnSubItemClick(v, getAdapterPosition(), InvitationsAdapter.REQUEST_REMIND);
					}
				}
			});
			this.btnReRequestContainer = v.findViewById(R.id.btn_re_request_container);
			this.btnReRequest = v.findViewById(R.id.btn_re_request);
			this.btnReRequest.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mListener!=null){
						mListener.OnSubItemClick(v, getAdapterPosition(), InvitationsAdapter.REQUEST_RESEND);
					}
				}
			});

			this.mLun = (TextView) v.findViewById(R.id.lun);
			this.mMar = (TextView) v.findViewById(R.id.mar);
			this.mMie = (TextView) v.findViewById(R.id.mie);
			this.mJue = (TextView) v.findViewById(R.id.jue);
			this.mVie = (TextView) v.findViewById(R.id.vie);
			this.mSab = (TextView) v.findViewById(R.id.sab);
			this.mDom = (TextView) v.findViewById(R.id.dom);
		}

		@Override
		public void onClick(View v) {
			if(mListener!=null){
				mListener.OnItemClick(v, getAdapterPosition());
			}
		}
	}

	public class StatusViewHolder extends RecyclerView.ViewHolder {
		public TextView mStatus;

		public StatusViewHolder(View v){
			super(v);

			this.mStatus = (TextView) v.findViewById(R.id.status);
		}
	}

	public interface Listener{
		void shareInvitation(String invitation_id);
		void toggleSelection(int position);
		void OnItemClick(View v, int position);
		void OnSubItemClick(View v, int position, int subItemActionType);
	}

	@Override
	public int getItemCount() {
		return consolidatedInvitationList != null ? consolidatedInvitationList.size() : 0;
	}

	public void showCheckboxes(){
		display_checkbox = true;
		notifyDataSetChanged();
	}

	public void hideCheckboxes(){
		display_checkbox = false;
		notifyDataSetChanged();
	}

	public static String formatDateTimeInvitation(String start_date_inv, String end_date_inv, String days_inv, Context context){
		String daysHoursString = context.getString(R.string.adapter_invitations_not_available);

		try {
			SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			Date date1 = dt.parse(start_date_inv);
			Date date2 = dt.parse(end_date_inv);

			if(date1 != null && date2 != null){
				long duration = date2.getTime() - date1.getTime();
				long daysDuration = TimeUnit.DAYS.convert(duration, TimeUnit.MILLISECONDS);

				SimpleDateFormat start_date = new SimpleDateFormat("dd MMM yyyy", new Locale("es"));
				SimpleDateFormat end_date = new SimpleDateFormat("dd MMM yyyy", new Locale("es"));

				SimpleDateFormat start_time = new SimpleDateFormat("HH:mm");
				SimpleDateFormat end_time = new SimpleDateFormat("HH:mm");

				String startDateStr = start_date.format(date1);
				String endDateStr = end_date.format(date2);
				String startTimeStr = start_time.format(date1);
				String endTimeStr = end_time.format(date2);

				String daysString = context.getString(R.string.adapter_invitations_from) + ": " + startDateStr + "\n" +
						context.getString(R.string.adapter_invitations_to) + ": " + endDateStr;
				String hoursString = context.getString(R.string.adapter_invitations_all_day);
				if(!startTimeStr.equals("00:00") || !endTimeStr.equals("23:59")){
					hoursString = String.format(context.getString(R.string.adapter_invitations_between), startTimeStr, endTimeStr);
				}
				daysHoursString = daysString + "\n" + hoursString;

				if(daysDuration == 0){// 1 dia
					daysString = String.format(context.getString(R.string.adapter_invitations_date), startDateStr);
					daysHoursString = daysString + "\n" + hoursString;
				} else if(daysDuration == 1){// 2 dias
					if(!startTimeStr.equals("00:00") || !endTimeStr.equals("23:59")){
						daysHoursString = String.format(context.getString(R.string.adapter_invitations_two_days_plus_hours),
								startDateStr, startTimeStr, endDateStr, endTimeStr);
					}
				} else {// 3 o mas dias
					String[] daysArr = days_inv.split("~");
					if (daysArr.length >= 7) {
						daysHoursString += "\n" + context.getString(R.string.adapter_invitations_every_day_of_week);
					}
				}
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return daysHoursString;

	}
}