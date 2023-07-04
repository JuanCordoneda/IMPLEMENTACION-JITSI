package com.safecard.android.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.safecard.android.fragments.RCQRFragment;

public class RCQRParkingAdapter extends FragmentStatePagerAdapter {
    public RCQRParkingAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Fragment getItem(int position) {
        return RCQRFragment.newInstanceParking();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }
}
