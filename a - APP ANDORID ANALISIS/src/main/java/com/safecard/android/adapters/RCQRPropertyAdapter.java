package com.safecard.android.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.safecard.android.fragments.RCQRFragment;
import com.safecard.android.model.dataobjects.PropertyData;

public class RCQRPropertyAdapter extends FragmentStatePagerAdapter {
    private PropertyData property;
    String accessType;

    public RCQRPropertyAdapter(FragmentManager fm, PropertyData property, String accessType) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.property = property;
        this.accessType = accessType;
    }

    @Override
    public int getCount() {
        return property.getAllowedSectors().size();
    }

    @Override
    public Fragment getItem(int position) {
        return RCQRFragment
                .newInstance(property.getAllowedSectors().get(position), accessType);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return property.getAllowedSectors().get(position).getSectorName();
    }
}
