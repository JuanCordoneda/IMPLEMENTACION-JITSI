package com.safecard.android.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.safecard.android.fragments.MsgFragment;

public class RCQRMsgAdapter extends FragmentStatePagerAdapter {
    private String msgType;
    private String msg;

    public RCQRMsgAdapter(FragmentManager fm, String msgType, String msg) {
        super(fm);
        this.msgType = msgType;
        this.msg = msg;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Fragment getItem(int position) {
        return MsgFragment
                .newInstance(msgType, msg);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }
}
