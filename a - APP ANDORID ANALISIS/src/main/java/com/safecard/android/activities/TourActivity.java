package com.safecard.android.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.safecard.android.R;
import com.safecard.android.utils.Utils;

public class TourActivity extends AppCompatActivity {
    static final private String TAG = "TourActivity";
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private ImageButton mNextBtn;
    private Button mFinishBtn;
    private LinearLayout mTourIndicators;

    String login;
    int[] indicators= { R.id.intro_indicator_0, R.id.intro_indicator_1, R.id.intro_indicator_2, R.id.intro_indicator_3 };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        /**
         * The {@link PagerAdapter} that will provide
         * fragments for each of the sections. We use a
         * {@link FragmentPagerAdapter} derivative, which will keep every
         * loaded fragment in memory. If this becomes too memory intensive, it
         * may be best to switch to a
         * {@link FragmentStatePagerAdapter}.
         */
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mNextBtn = (ImageButton) findViewById(R.id.intro_btn_next);
        mFinishBtn = (Button) findViewById(R.id.intro_btn_finish);

        mTourIndicators = (LinearLayout) findViewById(R.id.tour_indicators);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        if(Utils.getDefaults("finish_tour", getApplicationContext()) == null ||  Utils.getDefaults("finish_tour", getApplicationContext()).equals("0")){
            requestPermissions();
        }

        login = Utils.getDefaults("login", getApplicationContext());

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                int page = position;
                updateIndicators(page);
                if (position < 4){
                    mTourIndicators.setVisibility(View.VISIBLE);
                    mNextBtn.setVisibility(View.VISIBLE);
                    mFinishBtn.setVisibility(View.GONE);
                }else{
                    mTourIndicators.setVisibility(View.GONE);
                    mNextBtn.setVisibility(View.GONE);
                    mFinishBtn.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
            }
        });

        mFinishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blockButtonFor2Sec(mFinishBtn);
                Utils.setDefaults("finish_tour", "1", getApplicationContext());

                if(login == null) {
                    Intent intent = new Intent(getApplicationContext(), SmsActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), InitActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        });

    }

    private void blockButtonFor2Sec(final Button button) {
        button.setEnabled(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(true);
            }
        }, 2000);
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

    @Override
    public void onBackPressed() {
    }

    void updateIndicators(int position) {
        for (int i = 0; i < indicators.length; i++) {

            ImageView ind = (ImageView) findViewById(indicators[i]);

            ind.setBackgroundResource(i == position ? R.drawable.indicator_selected : R.drawable.indicator_unselected);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tour, container, false);
            ImageView tourImage = (ImageView) rootView.findViewById(R.id.tour_image);
            TextView tourText = rootView.findViewById(R.id.tour_text);

            int imageDrawable = R.drawable.tour0;
            String text = "";
            switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                case 0:
                    imageDrawable = R.drawable.tour0;
                    text = getActivity().getString(R.string.activity_tour_text_0);
                    break;
                case 1:
                    imageDrawable = R.drawable.tour1;
                    text = getActivity().getString(R.string.activity_tour_text_1);
                    break;
                case 2:
                    imageDrawable = R.drawable.tour2;
                    text = getActivity().getString(R.string.activity_tour_text_2);
                    break;
                case 3:
                    imageDrawable = R.drawable.tour3;
                    text = getActivity().getString(R.string.activity_tour_text_3);
                    break;
                case 4:
                    imageDrawable = R.drawable.tour4;
                    text = getActivity().getString(R.string.activity_tour_text_4);
                    break;
            }
            
            tourImage.setImageResource(imageDrawable);
            tourText.setText(text);

            return rootView;
        }

    }

    public void requestPermissions(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS }, 1);
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }
}
