package com.safecard.android.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.safecard.android.Config;
import com.safecard.android.Consts;
import com.safecard.android.R;
import com.safecard.android.adapters.InvitationTypeAdapter;
import com.safecard.android.apicallers.ApiCallback;
import com.safecard.android.apicallers.ContactPropertiesCaller;
import com.safecard.android.apicallers.InitApiCaller;
import com.safecard.android.fragments.InvitationListFragment;
import com.safecard.android.services.NotificationUtils2;
import com.safecard.android.utils.Contact;
import com.safecard.android.utils.Invitation;
import com.safecard.android.utils.RequestVolley;
import com.safecard.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InvitationActivity extends BaseActivity implements InvitationListFragment.InvitationsActionListener, TabLayout.OnTabSelectedListener {
	private static final String TAG = "InvitationActivity";

	public static final int PICK_CONTACT_REQUEST = 100;
	public static final int GET_CUSTOMIZED_INVITATION = 200;

	private Map<String, ArrayList<InvitationCustomizationActivity.Property>> contactProperties = new HashMap<>();

	public int mCurrentPage;
	public static String mobile;

	private Boolean block_api_load = false;

    ProgressDialog pDialog;

	private Toolbar toolbar;
	private TextView title;
	private ViewPager viewPager;
	private TabLayout tabInvitationType;
	private ImageView mMenuInvitations;
	static JSONObject login_json;
	InvitationTypeAdapter appSectionAdapter;
	SwipeRefreshLayout swiperefresh;

    @Override
    public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.invitations);
		setChildActivityCode(Consts.InvitationActivity);


		toolbar = (Toolbar) findViewById(R.id.toolbar);
		title = (TextView) toolbar.findViewById(R.id.toolbar_title);
		title.setText(R.string.invitation_title);
		setSupportActionBar(toolbar);

		swiperefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

		//mMenuInvitations = (ImageView) findViewById(R.id.menu_invitations);
		//mMenuInvitations.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_menu_invitacion_on));

		viewPager = (ViewPager) findViewById(R.id.viewpager);
		// Give the TabLayout the ViewPager*/
		tabInvitationType = (TabLayout) findViewById(R.id.tabs);

		appSectionAdapter = new InvitationTypeAdapter(getSupportFragmentManager(), InvitationActivity.this);

		/*viewPager.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return block_api_load;
			}
		});*/

		new InitApiCaller(getApplicationContext()).doCall(new ApiCallback() {
			@Override
			public void callSuccess(JSONObject json) {
			}

			@Override
			public void callError(String errorType, String msg) {
			}
		});

		swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				callApiInvitations(true, true);
			}
		});

		pDialog = new ProgressDialog(InvitationActivity.this);
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog.setMessage(getString(R.string.invitation_loading_wait_a_moment));
		pDialog.setCancelable(true);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		try {
			return super.onTouchEvent(event);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return false;
	}

	private BroadcastReceiver onNotice = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			callApiInvitations(false, false);
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		block_api_load = false;
		LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// java.lang.IllegalArgumentException: View not attached to window manager
		if (pDialog != null && pDialog.isShowing()) {
			Utils.safeDismissDialog(InvitationActivity.this, pDialog);
		}
		pDialog = null;
	}


	@Override
	public void onResume(){
		super.onResume();
		if (Utils.getDefaultBoolean(Consts.SHOW_UPDATE, getApplicationContext())) {
			Utils.updateApp(InvitationActivity.this);
		}
		Utils.appBlockChecks(this);

		callApiInvitations(true, true);

		IntentFilter iff = new IntentFilter(NotificationUtils2.UPDATE_INVITATION_LIST_ACTION);
		LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
	}

	public void callApiInvitations(final Boolean selectLastTab, Boolean loadPersistent){

		swiperefresh.setRefreshing(true);
		mCurrentPage = tabInvitationType.getSelectedTabPosition() < 0 ? 0 : tabInvitationType.getSelectedTabPosition();

		try {
			String login = Utils.getDefaults("login", getApplicationContext());
			login_json = new JSONObject(login);
			mobile = login_json.getString("mobile");
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e ){
			e.printStackTrace();
		}

		if(loadPersistent.equals(true)) {
			populateInvitations(selectLastTab);
		}

		/* 2. Consulto api */
		new InitApiCaller(getApplicationContext()).doCall(new ApiCallback() {
			@Override
			public void callSuccess(JSONObject response) {}

			@Override
			public void callError(String errorType, String msg) {
			}
		});

		/* Llamada asíncrona a INVITACIONES */
		final String url_invitation = Config.ApiUrl + "invitations/" + mobile;
		final RequestVolley rqv = new RequestVolley(url_invitation, getApplicationContext());
		rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
			@Override
			public void onSuccess(JSONObject response_invitations) {
				try {
					if(swiperefresh.isRefreshing()){
						swiperefresh.setRefreshing(false);
					}
					if (response_invitations.getString("result").equals("ACK") && block_api_load.equals(false)) {
						Utils.setDefaultJSONObject("invitations", response_invitations, getApplicationContext());
						populateInvitations(false);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(String errorType, String msg) {
				if(swiperefresh.isRefreshing()){
					swiperefresh.setRefreshing(false);
				}
				if (!msg.equals("")) {
					Utils.showToast(getApplicationContext(), msg);
				}
			}
		});
	}

	public void populateInvitations(Boolean selectLastTab){
		try {
			JSONObject invitations = Utils.getDefaultJSONObject("invitations", getApplicationContext());

			if(invitations.length() > 0) {
				if (selectLastTab.equals(false)) {
					mCurrentPage = tabInvitationType.getSelectedTabPosition();
				}

				appSectionAdapter = new InvitationTypeAdapter(getSupportFragmentManager(), InvitationActivity.this);

				appSectionAdapter.setInvitations(invitations.toString());
				appSectionAdapter.setListener(this);

				viewPager.setAdapter(appSectionAdapter);
				viewPager.setCurrentItem(mCurrentPage);

				tabInvitationType.setupWithViewPager(viewPager);
				tabInvitationType.getTabAt(mCurrentPage < 0 ? 0 : mCurrentPage).select();

				tabInvitationType.setOnTabSelectedListener(this);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeInvitations(String invitations_ids, final ActionMode mode) {
		Log.i(TAG,"removeInvitations");
		final String url_residents = Config.ApiUrl + "delete_invitations/" + mobile + "/" + invitations_ids;
		final RequestVolley rqv = new RequestVolley(url_residents,getApplicationContext());
		pDialog.setCancelable(false);
		pDialog.setMessage(getString(R.string.invitation_deleting_invitations_wait_a_moment));
		pDialog.show();
		rqv.requestApi(new RequestVolley.VolleyJsonCallback(){
			@Override
			public void onSuccess(JSONObject response) {
				try {
					if (pDialog != null && pDialog.isShowing()) {
						Utils.safeDismissDialog(InvitationActivity.this, pDialog);
					}
					if (response.getString("result").equals("ACK")) {

						Utils.showToast(getApplicationContext(), getString(R.string.invitation_invitations_deleted_successfully));
						mode.finish();
						callApiInvitations(true, false);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(String errorType, String msg) {
				if (pDialog != null && pDialog.isShowing()) {
					Utils.safeDismissDialog(InvitationActivity.this, pDialog);
				}
				if (!msg.equals("")) {
					Utils.showToast(getApplicationContext(), msg);
				}
			}
		});
	}

	@Override
	public void shareInvitation(final String invitation_id) {

		final String url_residents = Config.ApiUrl + "get_invitation_url/" + mobile + "/" + invitation_id;
		final RequestVolley rqv = new RequestVolley(url_residents,getApplicationContext());
		pDialog.setCancelable(false);
		pDialog.setMessage(getString(R.string.invitation_loading_invitation_wait_a_moment));
		pDialog.show();
		rqv.requestApi(new RequestVolley.VolleyJsonCallback(){
			@Override
			public void onSuccess(JSONObject response) {
				if (pDialog != null && pDialog.isShowing()) {
					Utils.safeDismissDialog(InvitationActivity.this, pDialog);
				}
				try {

					if(response.getString("result").equals("ACK")){

						String url;
						if(response.has("url") &&
								!("".equals(response.getString("url")))){
							url = response.getString("url");
						} else {
							url = response.getString("long_url");
						}

						Intent share_invitation = new Intent(getApplicationContext(), ShareInvitationActivity.class);
						share_invitation.putExtra("URLWSP", url);
						share_invitation.putExtra("RESEND", true);
						share_invitation.putExtra("INVITATION_ID", invitation_id);
						startActivity(share_invitation);
					} else {
						Utils.showToast(getApplicationContext(), response.getString("msg"));
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(String errorType, String msg) {
				Utils.safeDismissDialog(InvitationActivity.this, pDialog);
				if (pDialog != null && pDialog.isShowing()) {
					Utils.safeDismissDialog(InvitationActivity.this, pDialog);
				}
				if (!msg.equals("")) {
					Utils.showToast(getApplicationContext(), msg);
				}
			}
		});
	}

	public void blockApi(Boolean flag){

		block_api_load = flag;

		/*LinearLayout tabStrip = (LinearLayout) tabInvitationType.getChildAt(0);
		tabStrip.setEnabled(!block);
		for(int i = 0; i < tabStrip.getChildCount(); i++){
			tabStrip.getChildAt(i).setClickable(!block);
		}*/

	}

	@Override
	public void onTabSelected(TabLayout.Tab tab) {
		//Log.i(TAG, "SELECTED: " + tab.getPosition());
		viewPager.setCurrentItem(tab.getPosition());
		if (tab.getPosition() != 1 && InvitationListFragment.actionMode != null){
			InvitationListFragment.actionMode.finish();
		}

		if(tab.getPosition() != 1 && InvitationListFragment.customMenu != null){
			InvitationListFragment.customMenu.clear();
		}
	}

	@Override
	public void onTabUnselected(TabLayout.Tab tab) {
	}

	@Override
	public void onTabReselected(TabLayout.Tab tab) {

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		Log.i(TAG, "onActivityResult requestCode:"+ requestCode);

		if (requestCode == PICK_CONTACT_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {

				Serializable serializedContacts = intent.getSerializableExtra(PickContactsActivity.CONTACTS);
				if(serializedContacts instanceof ArrayList ){
					ArrayList contactsList = (ArrayList) serializedContacts;
					if (contactsList.size() == 1 && contactsList.get(0) instanceof Contact) {
						Contact selectedContact = (Contact) contactsList.get(0);
						showInvitationCustomizationActivity(selectedContact);
					}
				}

			}
		}

		if (requestCode == GET_CUSTOMIZED_INVITATION) {
			if (resultCode == Activity.RESULT_OK) {
				Serializable inv = intent.getSerializableExtra(InvitationCustomizationActivity.INVITATION);
				String organizerMobile = intent.getStringExtra(InvitationCustomizationActivity.ORGANIZER_MOBILE);
				if(inv instanceof Invitation){
					sendInvitationRequest((Invitation) inv, organizerMobile);
				}
			}
		}
	}

	private void showInvitationCustomizationActivity(final Contact contact) {
		final List<String> names = new ArrayList<>();
		names.add(contact.getName() + " (" + contact.getMobile() + ")");

		pDialog.setCancelable(true);
		pDialog.setMessage(getString(R.string.invitation_wait_a_moment));
		pDialog.show();

		new ContactPropertiesCaller(getApplicationContext(), contact.getMobile()).doCall(new ApiCallback() {
			@Override
			public void callSuccess(JSONObject json) {
				if (pDialog != null && pDialog.isShowing()) {
					Utils.safeDismissDialog(InvitationActivity.this, pDialog);
				}

				try {
					//Log.i(TAG, "ContactPropertiesCaller: "+ json);
					ArrayList<InvitationCustomizationActivity.Property> properties
							= InvitationCustomizationActivity.getPropertiesListFromJson(json.getJSONArray("properties"),true);
					properties = InvitationCustomizationActivity.filterActiveAndAvailableForInvitations(properties);
					contactProperties.put(contact.getMobile(), properties);

					Invitation invitation = new Invitation();
					if(properties.size() > 0){
						int defaultPropertyId = properties.get(0).getId();
						if(defaultPropertyId >= 0){
							invitation.setPropertyId(defaultPropertyId);
							InvitationCustomizationActivity.selectAllSectors(invitation, properties);
						}
					}

					Intent intent = new Intent(getApplicationContext(), InvitationCustomizationActivity.class);
					intent.putExtra(InvitationCustomizationActivity.TITLE, getString(R.string.invitation_invitation_customization_title));
					intent.putExtra(InvitationCustomizationActivity.NAMES, (ArrayList) names);
					intent.putExtra(InvitationCustomizationActivity.PROPERTIES, properties);
					intent.putExtra(InvitationCustomizationActivity.INVITATION, invitation);
					intent.putExtra(InvitationCustomizationActivity.ORGANIZER_MOBILE, contact.getMobile());
					intent.putExtra(InvitationCustomizationActivity.SUMMARY_TYPE, Consts.SUMMARY_TYPE_FROM);
					startActivityForResult(intent, GET_CUSTOMIZED_INVITATION);
					//Log.i(TAG, "startActivityForResult code:"+ GET_CUSTOMIZED_INVITATION);

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void callError(String errorType, String msg) {
				if (pDialog != null && pDialog.isShowing()) {
					Utils.safeDismissDialog(InvitationActivity.this, pDialog);
				}
				Utils.showToast(getApplicationContext(), msg);
			}
		});
	}

	public void sendInvitationRequest(Invitation invitation, String organizerMobile){

		final List<String> guestMobiles = new ArrayList<>();
		guestMobiles.add(Utils.getMobile(getApplicationContext()));

		final List<String> guestNames = new ArrayList<>();
		guestNames.add(Utils.getUserFullName(getApplicationContext()));

		ArrayList<InvitationCustomizationActivity.Property> properties
				= contactProperties.get(organizerMobile);

		InvitationCustomizationActivity.Property property
				= InvitationCustomizationActivity.getProperty(properties, invitation.getPropertyId());

		String startPattern = "yyyy-MM-dd'T'00:00:00";
		String endPattern = "yyyy-MM-dd'T'23:59:59";
		if(invitation.isCustomTime()){
			startPattern = "yyyy-MM-dd'T'HH:mm:00";
			endPattern = "yyyy-MM-dd'T'HH:mm:59";
		}
		String startDate = new SimpleDateFormat(startPattern)
				.format(invitation.getStartDateTimeCalendar().getTime());
		String endDate = new SimpleDateFormat(endPattern)
				.format(invitation.getEndDateTimeCalendar().getTime());

		String subject = "_";
		if (!invitation.getName().equals("")) {
			subject = Utils.encodeForURL(invitation.getName());
		}

		String days = "_";
		if (invitation.isCustomDaysOfWeek()) {
			days = invitation.getDaysOfWeekValidsAsString();
		}

		String sectors = "_";
		if(invitation.getIdsSelectedSectors().size() > 0){
			sectors = invitation.getIdsSelectedSectorsAsString();
		}

		String plate = "_";
		if(invitation.isPlateNumberUsed()){
			plate = invitation.getPlateNumber();
		}

		//request_invitation/:mobile/:mobile_organizer/:condo_id/:house_id/:start_date/:end_date/:relation_id/:subject?/:days?/:allowed_sectors?/:plate?

		String url = Config.ApiUrl + "request_invitation/" +
				Utils.getMobile(getApplicationContext()) + "/" +
				organizerMobile + "/" +
				property.getCondoId() + "/" +
				property.getId() + "/" +
				startDate + "/" +
				endDate + "/" +
				"3" + "/" +
				subject + "/" +
				days + "/" +
				sectors + "/" +
				plate;


		pDialog.setCancelable(true);
		pDialog.setMessage(getString(R.string.invitation_wait_a_moment2));
		pDialog.show();

		RequestVolley rqv = new RequestVolley(url, getApplicationContext());
		rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
			@Override
			public void onSuccess(final JSONObject response) {
				if (pDialog != null && pDialog.isShowing()) {
					Utils.safeDismissDialog(InvitationActivity.this, pDialog);
				}
				try {
					if (response.getString("result").equals("ACK")) {
						Utils.showToast(getApplicationContext(), getString(R.string.invitation_request_sent));
					} else {
						Utils.showToast(getApplicationContext(), response.getString("msg"));
					}
				} catch (JSONException e) {
					if (pDialog != null && pDialog.isShowing()) {
						Utils.safeDismissDialog(InvitationActivity.this, pDialog);
					}
					e.printStackTrace();
				}
			}

			@Override
			public void onError(String errorType, String msg) {
				if(!msg.equals("")){
					Utils.showToast(getApplicationContext(), msg);
				}

				if (pDialog != null && pDialog.isShowing()) {
					Utils.safeDismissDialog(InvitationActivity.this, pDialog);
				}
			}
		});

	}


	public void acceptInvitation(int invitationId) {

		String url = Config.ApiUrl + "accept_invitation/" +
				Utils.getMobile(getApplicationContext()) + "/" +
				invitationId;

		pDialog.setCancelable(true);
		pDialog.setMessage(getString(R.string.invitation_accepting_invitation_request));
		pDialog.show();

		RequestVolley rqv = new RequestVolley(url, getApplicationContext());
		rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
			@Override
			public void onSuccess(final JSONObject response) {
				if (pDialog != null && pDialog.isShowing()) {
					Utils.safeDismissDialog(InvitationActivity.this, pDialog);
				}
				try {
					if (response.getString("result").equals("ACK")) {
						Utils.showToast(getApplicationContext(), getString(R.string.invitation_request_accepted));
					} else {
						Utils.showToast(getApplicationContext(), response.getString("msg"));
					}
				} catch (JSONException e) {
					if (pDialog != null && pDialog.isShowing()) {
						Utils.safeDismissDialog(InvitationActivity.this, pDialog);
					}
					e.printStackTrace();
				}
				callApiInvitations(true, false);
			}

			@Override
			public void onError(String errorType, String msg) {
				if (!msg.equals("")) {
					Utils.showToast(getApplicationContext(), msg);
				}

				if (pDialog != null && pDialog.isShowing()) {
					Utils.safeDismissDialog(InvitationActivity.this, pDialog);
				}
			}
		});
	}

	public void rejectInvitation(int invitationId) {

		String url = Config.ApiUrl + "reject_invitation/" +
				Utils.getMobile(getApplicationContext()) + "/" +
				invitationId;

		pDialog.setCancelable(true);
		pDialog.setMessage(getString(R.string.invitation_wait_a_moment3));
		pDialog.show();

		RequestVolley rqv = new RequestVolley(url, getApplicationContext());
		rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
			@Override
			public void onSuccess(final JSONObject response) {
				if (pDialog != null && pDialog.isShowing()) {
					Utils.safeDismissDialog(InvitationActivity.this, pDialog);
				}
				try {
					if (response.getString("result").equals("ACK")) {
						Utils.showToast(getApplicationContext(), getString(R.string.invitation_request_rejected));
					} else {
						Utils.showToast(getApplicationContext(), response.getString("msg"));
					}
				} catch (JSONException e) {
					if (pDialog != null && pDialog.isShowing()) {
						Utils.safeDismissDialog(InvitationActivity.this, pDialog);
					}
					e.printStackTrace();
				}
				callApiInvitations(true, false);
			}

			@Override
			public void onError(String errorType, String msg) {
				if (!msg.equals("")) {
					Utils.showToast(getApplicationContext(), msg);
				}

				if (pDialog != null && pDialog.isShowing()) {
					Utils.safeDismissDialog(InvitationActivity.this, pDialog);
				}
			}
		});
	}

	public void resendInvitation(int invitationId) {

		String url = Config.ApiUrl + "request_invitation/" +
				Utils.getMobile(getApplicationContext()) + "/" +
				invitationId;

		pDialog.setCancelable(true);
		pDialog.setMessage(getString(R.string.invitation_wait_a_moment4));
		pDialog.show();

		RequestVolley rqv = new RequestVolley(url, getApplicationContext());
		rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
			@Override
			public void onSuccess(final JSONObject response) {
				if (pDialog != null && pDialog.isShowing()) {
					Utils.safeDismissDialog(InvitationActivity.this, pDialog);
				}
				try {
					if (response.getString("result").equals("ACK")) {
						Utils.showToast(getApplicationContext(), getString(R.string.invitation_request_sent_again));
					} else {
						Utils.showToast(getApplicationContext(), response.getString("msg"));
					}
				} catch (JSONException e) {
					if (pDialog != null && pDialog.isShowing()) {
						Utils.safeDismissDialog(InvitationActivity.this, pDialog);
					}
					e.printStackTrace();
				}
				callApiInvitations(true, false);
			}

			@Override
			public void onError(String errorType, String msg) {
				if (!msg.equals("")) {
					Utils.showToast(getApplicationContext(), msg);
				}

				if (pDialog != null && pDialog.isShowing()) {
					Utils.safeDismissDialog(InvitationActivity.this, pDialog);
				}
			}
		});
	}

	public void remindInvitation(int invitationId) {

		String url = Config.ApiUrl + "reminder_invitation/" +
				Utils.getMobile(getApplicationContext()) + "/" +
				invitationId;

		pDialog.setCancelable(true);
		pDialog.setMessage(getString(R.string.invitation_wait_a_moment5));
		pDialog.show();

		RequestVolley rqv = new RequestVolley(url, getApplicationContext());
		rqv.requestApi(new RequestVolley.VolleyJsonCallback() {
			@Override
			public void onSuccess(final JSONObject response) {
				if (pDialog != null && pDialog.isShowing()) {
					Utils.safeDismissDialog(InvitationActivity.this, pDialog);
				}
				try {
					if (response.getString("result").equals("ACK")) {
						Utils.showToast(getApplicationContext(), getString(R.string.invitation_request_notified_again));
					} else {
						Utils.showToast(getApplicationContext(), response.getString("msg"));
					}
				} catch (JSONException e) {
					if (pDialog != null && pDialog.isShowing()) {
						Utils.safeDismissDialog(InvitationActivity.this, pDialog);
					}
					e.printStackTrace();
				}
				callApiInvitations(true, false);
			}

			@Override
			public void onError(String errorType, String msg) {
				if (!msg.equals("")) {
					Utils.showToast(getApplicationContext(), msg);
				}

				if (pDialog != null && pDialog.isShowing()) {
					Utils.safeDismissDialog(InvitationActivity.this, pDialog);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_invitation_base, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_invite:
				startActivity(new Intent(getApplicationContext(), ContactsActivity.class));
				finish();
				overridePendingTransition(0, 0);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

