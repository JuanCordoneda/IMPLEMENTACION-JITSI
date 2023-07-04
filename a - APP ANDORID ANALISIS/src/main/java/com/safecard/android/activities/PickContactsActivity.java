package com.safecard.android.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.safecard.android.R;
import com.safecard.android.fragments.PickContactsFragment;
import com.safecard.android.utils.Contact;
import com.safecard.android.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class PickContactsActivity extends AppCompatActivity implements PickContactsFragment.OnContactsInteractionListener {
    private static final String TAG = "PickContactsActivity";
    public static final String ARG_CARDINALITY = "ARG_CARDINALITY";

    public static final int CARDINALITY_ONE = 1;
    public static final int CARDINALITY_MANY = 2;


    public static final String CONTACTS = "CONTACTS";

    Button choiceDoneButton;
    ArrayList<Contact> selectedContacts;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_contacts);

        Intent intent = getIntent();
        final int cardinality = intent.getIntExtra(ARG_CARDINALITY, CARDINALITY_ONE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final TextView title = toolbar.findViewById(R.id.toolbar_title);
        toolbar.setNavigationIcon(R.drawable.ic_shape);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });

        if(cardinality == CARDINALITY_ONE) {
            title.setText(R.string.activity_pick_contacts_title_singular);
        }else if(cardinality == CARDINALITY_MANY) {
            title.setText(R.string.activity_pick_contacts_title_plural);
        }

        choiceDoneButton = findViewById(R.id.choice_done_button);

        choiceDoneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                int count = 0;
                if(selectedContacts != null) {
                    count = selectedContacts.size();
                }

                if(cardinality == CARDINALITY_ONE && count == 0) {
                        Utils.showToast(getApplicationContext(), getString(R.string.activity_pick_contacts_select_a_contact));
                }else if(cardinality == CARDINALITY_ONE && count > 1) {
                    Utils.showToast(getApplicationContext(), getString(R.string.activity_pick_contacts_select_only_one_contact));
                }else if(cardinality == CARDINALITY_MANY && count == 0) {
                    Utils.showToast(getApplicationContext(), getString(R.string.activity_pick_contacts_select_one_contact_at_least));
                }else {
                    Intent intent = new Intent();
                    //data.putExtra("mobiles", selectedContactsToMobiles(selectedContacts));
                    intent.putExtra(CONTACTS, selectedContacts);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });


        FragmentManager mFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        //fragTransaction.remove(fragment);
        fragmentTransaction.replace(R.id.contacts_list_container,  new PickContactsFragment()).commit();
        //fragmentTransaction.executePendingTransactions();
    }

    @Override
    public void onSelectedContactsChanged(ArrayList<Contact> selectedContacts) {
        this.selectedContacts = selectedContacts;
        String text = String.format(Locale.getDefault(),
                getString(R.string.activity_pick_contacts_counting_continue_button), String.valueOf(this.selectedContacts.size()));
        choiceDoneButton.setText(text);
    }

}


