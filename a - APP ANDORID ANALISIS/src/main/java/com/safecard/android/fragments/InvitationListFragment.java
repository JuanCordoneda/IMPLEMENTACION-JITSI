package com.safecard.android.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.safecard.android.R;
import com.safecard.android.activities.AccessVisitActivity;
import com.safecard.android.activities.InvitationActivity;
import com.safecard.android.activities.PickContactsActivity;
import com.safecard.android.adapters.InvitationsAdapter;
import com.safecard.android.listitems.GeneralItem;
import com.safecard.android.listitems.ListItem;
import com.safecard.android.listitems.TypeInvitationItem;
import com.safecard.android.utils.DividerItemDecoration;
import com.safecard.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class InvitationListFragment extends Fragment implements InvitationsAdapter.Listener {
    public static final String TAG = "InvitationList_Fragment";
    public static final String ARG_SECTION_NUMBER = "section_number";
    public static final String ARG_INVITATIONS = "ARG_INVITATIONS";


    public static final String ACTIVE_INVITATIONS = "ACTIVE_INVITATIONS";
    public static final String INACTIVE_INVITATIONS = "INACTIVE_INVITATIONS";

    public static final String RECEIVED_INVITATIONS = "RECEIVED_INVITATIONS";
    public static final String SENT_INVITATIONS = "SENT_INVITATIONS";
    public static final String REQUESTED_INVITATIONS = "REQUESTED_INVITATIONS";

    public static final String RECEIVED_INVITATION_REQUESTS = "RECEIVED_INVITATION_REQUESTS";
    public static final String PENDING_INVITATION_REQUESTS = "PENDING_INVITATION_REQUESTS";
    public static final String REJECTED_INVITATION_REQUESTS = "REJECTED_INVITATION_REQUESTS";


    private int mPage;
    private String mInvitations;

    private JSONArray inv_array;

    private List<ListItem> consolidatedReceivedList = new ArrayList<>();
    private List<ListItem> consolidatedSendList = new ArrayList<>();
    private List<ListItem> consolidatedRequestList = new ArrayList<>();

    private InvitationsAdapter InvAdapter;

    private InvitationsActionListener mListener;

    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    public static ActionMode actionMode;
    public static Menu customMenu;

    public static InvitationListFragment newInstance(int page, String invitations, InvitationsActionListener listener) {
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, page);
        args.putString(ARG_INVITATIONS, invitations);
        InvitationListFragment fragment = new InvitationListFragment();
        fragment.setArguments(args);
        fragment.setListener(listener);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_SECTION_NUMBER);
        mInvitations = getArguments().getString(ARG_INVITATIONS);
        //Log.i(TAG,"mInvitations");
        //Log.i(TAG,mInvitations);

        try {
            JSONObject invitations_aux = new JSONObject(mInvitations);
            JSONArray inv_arr_aux = new JSONArray(invitations_aux.getString("enviadas"));
            setHasOptionsMenu(mPage == 1 && inv_arr_aux.length() > 0);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_invitation_sent, menu);
        customMenu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch(id) {
            case R.id.action_remove_invitation:
                actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionModeCallback);
                actionMode.setTitle("0");
                InvAdapter.showCheckboxes();
                mListener.blockApi(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_invitations_list, container, false);
        Button btnRequestInvitation = rootView.findViewById(R.id.btn_request_invitation);
        btnRequestInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PickContactsActivity.class);
                intent.putExtra(PickContactsActivity.ARG_CARDINALITY, PickContactsActivity.CARDINALITY_ONE);
                int requestCode = InvitationActivity.PICK_CONTACT_REQUEST;
                getActivity().startActivityForResult(intent, requestCode);
                Log.i(TAG, "startActivityForResult code: " + requestCode);
            }
        });

        btnRequestInvitation.setVisibility(View.GONE);
        try{
            JSONObject invitations_json_aux = new JSONObject(mInvitations);

            switch(mPage){
                case 0:

                    inv_array = new JSONArray(invitations_json_aux.getString("recibidas"));
                    Map<String, List<JSONObject>> groupedReceived = groupInvitationsIntoHashMap(inv_array);

                    for(String type : groupedReceived.keySet()){
                        TypeInvitationItem tii = new TypeInvitationItem();

                        tii.setTypeInvitation(getTypeLabel(type));
                        consolidatedReceivedList.add(tii);

                        for(JSONObject list_inv_obj : groupedReceived.get(type)){
                            GeneralItem generalItem = new GeneralItem();
                            generalItem.setObj(list_inv_obj);
                            consolidatedReceivedList.add(generalItem);
                        }
                    }

                    InvAdapter = new InvitationsAdapter(consolidatedReceivedList, this, getContext());
                    InvAdapter.setInvitationType(RECEIVED_INVITATIONS);
                    break;

                case 1:
                    inv_array = new JSONArray(invitations_json_aux.getString("enviadas"));

                    Map<String, List<JSONObject>> groupedSend = groupInvitationsIntoHashMap(inv_array);

                    for(String type : groupedSend.keySet()){
                        TypeInvitationItem tii = new TypeInvitationItem();
                        tii.setTypeInvitation(getTypeLabel(type));
                        consolidatedSendList.add(tii);

                        for(JSONObject list_inv_obj : groupedSend.get(type)){
                            GeneralItem generalItem = new GeneralItem();
                            generalItem.setObj(list_inv_obj);
                            consolidatedSendList.add(generalItem);
                        }
                    }

                    InvAdapter = new InvitationsAdapter(consolidatedSendList, this, getContext());
                    InvAdapter.setInvitationType(SENT_INVITATIONS);
                    break;

                case 2:
                    btnRequestInvitation.setVisibility(View.VISIBLE);
                    inv_array = new JSONArray(invitations_json_aux.getString("solicitadas"));

                    Map<String, List<JSONObject>> groupedRequested = groupInvitationRequestsIntoHashMap(inv_array);

                    for(String type : groupedRequested.keySet()){
                        TypeInvitationItem tii = new TypeInvitationItem();
                        tii.setTypeInvitation(getTypeLabel(type));
                        consolidatedRequestList.add(tii);

                        for(JSONObject list_inv_obj : groupedRequested.get(type)){
                            GeneralItem generalItem = new GeneralItem();
                            generalItem.setObj(list_inv_obj);
                            consolidatedRequestList.add(generalItem);
                        }
                    }

                    InvAdapter = new InvitationsAdapter(consolidatedRequestList, this, getContext());
                    InvAdapter.setInvitationType(REQUESTED_INVITATIONS);
                    break;
            }
            RelativeLayout empty_invitation = (RelativeLayout) rootView.findViewById(R.id.empty_invitation);
            RecyclerView invitationsList = (RecyclerView) rootView.findViewById(R.id.invitations_list);

            if(inv_array.length() == 0){
                invitationsList.setVisibility(View.GONE);
                empty_invitation.setVisibility(View.VISIBLE);
            } else {
                invitationsList.setVisibility(View.VISIBLE);
                empty_invitation.setVisibility(View.GONE);

                invitationsList.setHasFixedSize(true);

                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
                invitationsList.setLayoutManager(mLayoutManager);

                invitationsList.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
                invitationsList.setAdapter(InvAdapter);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rootView;
    }


    private String getTypeLabel(String type) {
        switch (type){
            case ACTIVE_INVITATIONS:
                return getString(R.string.fragment_invitation_list_invitations_active);
            case INACTIVE_INVITATIONS:
                return getString(R.string.fragment_invitation_list_invitations_inactive);
            case RECEIVED_INVITATION_REQUESTS:
                return getString(R.string.fragment_invitation_list_invitations_requested_received);
            case PENDING_INVITATION_REQUESTS:
                return getString(R.string.fragment_invitation_list_invitations_requested_pending);
            case REJECTED_INVITATION_REQUESTS:
                return getString(R.string.fragment_invitation_list_invitations_requested_rejected);
            default:
                return "";
        }
    }

    private Map<String, List<JSONObject>> groupInvitationsIntoHashMap(JSONArray invitations_array){
        final Map<String, List<JSONObject>> groupedHashMap = new HashMap<String, List<JSONObject>>();

        try {
            for(int i = 0; i < invitations_array.length(); i++){
                JSONObject inv_obj = invitations_array.getJSONObject(i);
                if (inv_obj.getInt("deleted") == 1 || inv_obj.getInt("active") == 0 || inv_obj.getInt("expired") == 1) {
                    if(groupedHashMap.containsKey(INACTIVE_INVITATIONS)){
                        groupedHashMap.get(INACTIVE_INVITATIONS).add(inv_obj);
                    } else {
                        List<JSONObject> list = new ArrayList<>();
                        list.add(inv_obj);
                        groupedHashMap.put(INACTIVE_INVITATIONS, list);
                    }
                } else {
                    if(groupedHashMap.containsKey(ACTIVE_INVITATIONS)){
                        groupedHashMap.get(ACTIVE_INVITATIONS).add(inv_obj);
                    } else {
                        List<JSONObject> list = new ArrayList<>();
                        list.add(inv_obj);
                        groupedHashMap.put(ACTIVE_INVITATIONS, list);
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sortByKeys(groupedHashMap);
    }

    private Map<String, List<JSONObject>> groupInvitationRequestsIntoHashMap(JSONArray invitationsArray){
        final Map<String, List<JSONObject>> groupedHashMap = new HashMap<String, List<JSONObject>>();

        String userMobile = Utils.getMobile(getActivity());
        try {
            for(int i = 0; i < invitationsArray.length(); i++){
                JSONObject item = invitationsArray.getJSONObject(i);

                if (item.getInt("active") == 1
                        && userMobile.equals(item.getString("organizer_mobile"))) {
                    if(!groupedHashMap.containsKey(RECEIVED_INVITATION_REQUESTS)){
                        groupedHashMap.put(RECEIVED_INVITATION_REQUESTS, new ArrayList<JSONObject>());
                    }
                    item.put("request_item_type", RECEIVED_INVITATION_REQUESTS);
                    groupedHashMap.get(RECEIVED_INVITATION_REQUESTS).add(item);
                }
                else if (item.getInt("active") == 1
                        && !userMobile.equals(item.getString("organizer_mobile"))
                        && item.getInt("status") == 0) {
                    if(!groupedHashMap.containsKey(PENDING_INVITATION_REQUESTS)){
                        groupedHashMap.put(PENDING_INVITATION_REQUESTS, new ArrayList<JSONObject>());
                    }
                    item.put("request_item_type", PENDING_INVITATION_REQUESTS);
                    groupedHashMap.get(PENDING_INVITATION_REQUESTS).add(item);
                }
                else if (item.getInt("active") == 1
                        && !userMobile.equals(item.getString("organizer_mobile"))
                        && item.getInt("status") != 0) {
                    if(!groupedHashMap.containsKey(REJECTED_INVITATION_REQUESTS)){
                        groupedHashMap.put(REJECTED_INVITATION_REQUESTS, new ArrayList<JSONObject>());
                    }
                    item.put("request_item_type", REJECTED_INVITATION_REQUESTS);
                    groupedHashMap.get(REJECTED_INVITATION_REQUESTS).add(item);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sortByKeys(groupedHashMap);
    }


    public static <K extends Comparable,V> Map<K,V> sortByKeys(Map<K,V> map){
        List<K> keys = new LinkedList<K>(map.keySet());
        Collections.sort(keys);

        //LinkedHashMap will keep the keys in the order they are inserted
        //which is currently sorted on natural ordering
        Map<K,V> sortedMap = new LinkedHashMap<K,V>();
        for(K key: keys){
            sortedMap.put(key, map.get(key));
        }

        return sortedMap;
    }

    @Override
    public void toggleSelection(int position) {
        InvAdapter.toggleSelection(position);
        int count = InvAdapter.getSelectedItemCount();

        actionMode.setTitle(String.valueOf(count));
        actionMode.invalidate();
    }

    @Override
    public void OnItemClick(View v, int position) {
        try {

            switch (mPage) {
                case 0:
                    if(consolidatedReceivedList.get(position).getType() == ListItem.TYPE_GENERAL){
                        GeneralItem generalItem = (GeneralItem) consolidatedReceivedList.get(position);
                        JSONObject inv_json = generalItem.getObj();
                        //Recibidas
                        if (Integer.parseInt(inv_json.getString("active")) == 0
                                || Integer.parseInt(inv_json.getString("expired")) == 1
                                || Integer.parseInt(inv_json.getString("deleted")) == 1){
                            Log.i(TAG, inv_json.toString());

                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            //if(inv_json.getString("type").equals("student")) {
                            //    builder.setMessage("Invitación deshabilitada. Comuníquese con el apoderado.");
                            //}else {
                            builder.setMessage(R.string.fragment_invitation_list_dialog_content_disabled_invitation);
                            //}
                            builder.setTitle(R.string.fragment_invitation_list_dialog_title_disabled_invitation)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.fragment_invitation_list_dialog_ok_button, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });

                            AlertDialog alert = builder.create();
                            alert.show();

                        } else {
                            Intent inv_remote = new Intent(getContext(), AccessVisitActivity.class);
                            inv_remote.putExtra("invObj", inv_json.toString());

                            //InvAdapter.notifyDataSetChanged();

                            // Lógica para marcar invitaciones vistas por usuario
                            JSONObject invitations_viewed;
                            if(Utils.getDefaults("invitations_viewed", getContext()) != null){
                                invitations_viewed = new JSONObject(Utils.getDefaults("invitations_viewed", getContext()));
                                if(!invitations_viewed.has(String.valueOf(inv_json.getInt("id")))){
                                    invitations_viewed.put(String.valueOf(inv_json.getInt("id")), true);
                                }
                            } else {
                                invitations_viewed = new JSONObject();
                                invitations_viewed.put(String.valueOf(inv_json.getInt("id")), true);
                            }
                            Utils.setDefaults("invitations_viewed", invitations_viewed.toString(), getContext());
                            startActivity(inv_remote);
                        }
                    }
                    break;
                case 1:
                    //Enviadas sólo para eliminar
                    if(consolidatedSendList.get(position).getType() == ListItem.TYPE_GENERAL){
                        if (actionMode != null){
                            toggleSelection(position);
                        }
                    }

                    break;

                case 2:
                    if(consolidatedRequestList.get(position).getType() == ListItem.TYPE_GENERAL){
                        if (actionMode != null){
                            toggleSelection(position);
                        }
                    }

                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnSubItemClick(View v, int position, int subItemActionType) {
        try {
            if(mPage ==  2
                    && consolidatedRequestList.get(position).getType() == ListItem.TYPE_GENERAL){

                GeneralItem generalItem = (GeneralItem) consolidatedRequestList.get(position);
                JSONObject inv_json = generalItem.getObj();
                int invitationId = inv_json.getInt("id");
                InvitationActivity activity = (InvitationActivity) getActivity();
                switch (subItemActionType) {
                    case InvitationsAdapter.REQUEST_ACCEPT:
                        Log.i(TAG, "REQUEST_ACCEPT");
                        activity.acceptInvitation(invitationId);
                        break;
                    case InvitationsAdapter.REQUEST_REJECT:
                        Log.i(TAG, "REQUEST_REJECT");
                        activity.rejectInvitation(invitationId);
                        break;
                    case InvitationsAdapter.REQUEST_REMIND:
                        Log.i(TAG, "REQUEST_REMIND");
                        activity.remindInvitation(invitationId);
                        break;
                    case InvitationsAdapter.REQUEST_RESEND:
                        Log.i(TAG, "REQUEST_RESEND");
                        activity.resendInvitation(invitationId);
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setListener(InvitationsActionListener listener) {
        this.mListener = listener;
    }

    @Override
    public void shareInvitation(String invitation_id) {
        mListener.shareInvitation(invitation_id);
    }

    public interface InvitationsActionListener{
        void removeInvitations(String invitations_ids, ActionMode mode);
        void shareInvitation(String invitation_id);
        void blockApi(Boolean flag);
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @SuppressWarnings("unused")
        private final String TAG = ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate (R.menu.menu_confirm, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_confirm_remove:

                    if(getActivity() == null){
                        return false;
                    }

                    if(InvAdapter.getSelectedItemCount() > 0){

                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

                        String invitations_ids = "";
                        final List<Integer> selected_items = InvAdapter.getSelectedItems();
                        try {
                            for(int i = 0; i < selected_items.size(); i++){
                                GeneralItem generalItem = (GeneralItem) consolidatedSendList.get(selected_items.get(i));
                                JSONObject inv_json = generalItem.getObj();
                                invitations_ids += inv_json.getString("id")+",";
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        alertDialog.setMessage(R.string.fragment_invitation_list_dialog_content_delete_confirmation);
                        alertDialog.setCancelable(false);
                        //alertDialog.setTitle(R.string.fragment_invitation_list_dialog_title_delete_confirmation);
                        final String invs_ids = invitations_ids;
                        alertDialog.setPositiveButton(R.string.fragment_invitation_list_dialog_ok_button2, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int which) {
                                mListener.removeInvitations(invs_ids.substring(0, invs_ids.length() - 1), mode);
                            }
                        });

                        alertDialog.setNegativeButton(R.string.fragment_invitation_list_dialog_no_button, new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which){
                                dialog.cancel();
                            }
                        });
                        alertDialog.show();

                    } else {
                        Utils.showToast(getActivity().getApplicationContext(), getString(R.string.fragment_invitation_list_must_select_one_invitation_at_least));
                    }
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            InvAdapter.hideCheckboxes();
            InvAdapter.clearSelection();
            mListener.blockApi(false);
            actionMode = null;
        }
    }
}
