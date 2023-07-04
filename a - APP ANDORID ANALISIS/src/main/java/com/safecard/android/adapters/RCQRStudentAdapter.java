package com.safecard.android.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.safecard.android.fragments.RCQRFragment;
import com.safecard.android.model.dataobjects.StudentData;

public class RCQRStudentAdapter extends FragmentStatePagerAdapter {
    private StudentData student;
    String accessType;
    public RCQRStudentAdapter(FragmentManager fm, StudentData student, String accessType) {
        super(fm);
        this.student = student;
        this.accessType = accessType;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Fragment getItem(int position) {
        return RCQRFragment
                .newInstance(student, this.accessType);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }
}
