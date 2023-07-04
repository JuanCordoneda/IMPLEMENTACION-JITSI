package com.safecard.android.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.safecard.android.Consts;
import com.safecard.android.R;

public class MsgFragment extends Fragment {
    public static final String TAG = "MsgFragment";
    public static final String MSG_TYPE = "MSG_TYPE";
    public static final String REASON = "REASON";

    public static MsgFragment newInstance(String msgType, String reason) {
        MsgFragment fragment = new MsgFragment();
        Bundle bundle = new Bundle();
        bundle.putString(MSG_TYPE, msgType);
        bundle.putString(REASON, reason);
        fragment.setArguments(bundle);
        return fragment;
    }

    String msgType;
    String reason;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        msgType = getArguments().getString(MSG_TYPE);
        reason = getArguments().getString(REASON);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_access_msg, container, false);
        Log.d(TAG,"loadMsg");
        TextView msg = (TextView) rootView.findViewById(R.id.msg);
        ImageView msgImage = (ImageView) rootView.findViewById(R.id.msg_image);
        if (msgType.equals(Consts.MSG_TYPE_NO_ACTIVE)) {
            msg.setText(reason);
            msg.setVisibility(View.VISIBLE);
            msgImage.setImageResource(R.drawable.ic_property_block);
        }else if (msgType.equals(Consts.MSG_TYPE_NO_PROPERTIES)) {
            msg.setText(R.string.fragment_msg_you_have_no_properties);
            msg.setVisibility(View.VISIBLE);
            msgImage.setImageResource(R.drawable.ic_no_property);
        }else if (msgType.equals(Consts.MSG_TYPE_EXPIRED)) {
            msg.setText(R.string.fragment_msg_expired_invitation);
            msg.setVisibility(View.VISIBLE);
            msgImage.setImageResource(R.drawable.ic_no_property);
        }
        
        return rootView;
    }


}