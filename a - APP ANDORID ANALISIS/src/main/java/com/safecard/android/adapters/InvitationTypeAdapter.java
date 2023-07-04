package com.safecard.android.adapters;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.safecard.android.R;
import com.safecard.android.fragments.InvitationListFragment;

/**
 * Created by efajardo on 10/06/16.
 */
public class InvitationTypeAdapter extends FragmentStatePagerAdapter {

    private Context context;

    final int PAGE_COUNT = 3;
    private String[] tabTitles;
    private static String INVITATIONS;
    InvitationListFragment invitationListFragment;

    InvitationListFragment.InvitationsActionListener LISTENER;

    public InvitationTypeAdapter(FragmentManager fm, Context c) {
        super(fm);
        context = c;
        tabTitles = new String[]{
                context.getString(R.string.adapter_invitation_type_received),
                context.getString(R.string.adapter_invitation_type_sent),
                context.getString(R.string.adapter_invitation_type_requested)};
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    public void setInvitations(String invitations){ this.INVITATIONS = invitations; }

    public void setListener(InvitationListFragment.InvitationsActionListener listener){
        this.LISTENER = listener;
    }

    @Override
    public Fragment getItem(int position) {
        invitationListFragment =  InvitationListFragment.newInstance(position, this.INVITATIONS, this.LISTENER);
        return invitationListFragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
