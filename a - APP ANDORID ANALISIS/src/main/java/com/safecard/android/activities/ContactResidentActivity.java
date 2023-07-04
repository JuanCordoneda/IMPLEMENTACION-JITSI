package com.safecard.android.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.safecard.android.R;
import com.safecard.android.fragments.ContactsListFragment;

public class ContactResidentActivity extends AppCompatActivity implements
        ContactsListFragment.OnContactsInteractionListener {

    public String mobiles = "" ,names = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_resident);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_shape);
        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText(R.string.contact_resident_title);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void onContactSelected(String phoneNumbers, String contactNames) {
        mobiles = phoneNumbers.replace(",","");
        names = contactNames.replace(",","");

        if(!names.equals("")){
            Intent returnIntent = new Intent();
            returnIntent.putExtra("resident_name", names);
            returnIntent.putExtra("resident_mobile", mobiles);
            setResult(ConfirmResidentActivity.PICK_SECTOR);
            setResult(ConfirmResidentActivity.RESULT_OK, returnIntent);
            finish();

        }
    }

    @Override
    public void onSelectionCleared() {
        /*if (isTwoPaneLayout && mContactDetailFragment != null) {
            mContactDetailFragment.setContact(null);
        }*/
    }

    @Override
    public boolean onSearchRequested() {
        // Don't allow another search if this activity instance is already showing
        // search results. Only used pre-HC.
        //return !isSearchResultView && super.onSearchRequested();
        return false;
    }

}
