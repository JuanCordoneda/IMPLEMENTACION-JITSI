package com.safecard.android.activities;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.safecard.android.Consts;
import com.safecard.android.R;

public abstract class BaseActivity extends AppCompatActivity {

    protected int ChildActivityCode = -1;

    @Override
    protected void onResume() {
        super.onResume();

        LinearLayout menuContacts = findViewById(R.id.menu_invite);
        LinearLayout menuInvitations = findViewById(R.id.menu_invitations);
        LinearLayout menuAccess = findViewById(R.id.menu_access);
        LinearLayout menuPayment = findViewById(R.id.menu_payment);

        View menuContactsLine = findViewById(R.id.menu_invite_line);
        View menuInvitationsLine = findViewById(R.id.menu_invitations_line);
        View menuAccessLine = findViewById(R.id.menu_access_line);
        View menuPaymentLine = findViewById(R.id.menu_payment_line);

        View menuContactsText = findViewById(R.id.menu_invite_text);
        View menuInvitationsText = findViewById(R.id.menu_invitations_text);
        View menuAccessText = findViewById(R.id.menu_access_text);
        View menuPaymentText = findViewById(R.id.menu_payment_text);

        View menuContactsIc = findViewById(R.id.menu_invite_ic);
        View menuInvitationsIc = findViewById(R.id.menu_invitations_ic);
        View menuAccessIc = findViewById(R.id.menu_access_ic);
        View menuPaymentIc = findViewById(R.id.menu_payment_ic);


        menuContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ContactsActivity.class));
                finish();
                overridePendingTransition(0, 0);
            }
        });

        menuInvitations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), InvitationActivity.class));
                finish();
                overridePendingTransition(0, 0);
            }
        });

        menuAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AccessActivity.class));
                finish();
                overridePendingTransition(0, 0);
            }
        });

        menuPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ParkingActivity.class));
                finish();
                overridePendingTransition(0, 0);
            }
        });


        menuContactsLine.setVisibility(View.INVISIBLE);
        menuInvitationsLine.setVisibility(View.INVISIBLE);
        menuAccessLine.setVisibility(View.INVISIBLE);
        menuPaymentLine.setVisibility(View.INVISIBLE);

        float offAlpha = (float) 0.8;
        menuContactsText.setAlpha(offAlpha);
        menuInvitationsText.setAlpha(offAlpha);
        menuAccessText.setAlpha(offAlpha);
        menuPaymentText.setAlpha(offAlpha);

        menuContactsIc.setAlpha(offAlpha);
        menuInvitationsIc.setAlpha(offAlpha);
        menuAccessIc.setAlpha(offAlpha);
        menuPaymentIc.setAlpha(offAlpha);

        if (ChildActivityCode == Consts.ContactsActivity) {
            menuContactsLine.setVisibility(View.VISIBLE);
            menuContactsText.setAlpha(1);
            menuContactsIc.setAlpha(1);
            menuContacts.setOnClickListener(null);
        }
        if (ChildActivityCode == Consts.InvitationActivity) {
            menuInvitationsLine.setVisibility(View.VISIBLE);
            menuInvitationsText.setAlpha(1);
            menuInvitationsIc.setAlpha(1);
            menuInvitations.setOnClickListener(null);
        }
        if (ChildActivityCode == Consts.AccessActivity) {
            menuAccessLine.setVisibility(View.VISIBLE);
            menuAccessText.setAlpha(1);
            menuAccessIc.setAlpha(1);
            menuAccess.setOnClickListener(null);
        }
        if (ChildActivityCode == Consts.ParkingActivity) {
            menuPaymentLine.setVisibility(View.VISIBLE);
            menuPaymentText.setAlpha(1);
            menuPaymentIc.setAlpha(1);
            menuPayment.setOnClickListener(null);
        }
    }

    public void setChildActivityCode(int ChildActivityCode) {
        this.ChildActivityCode = ChildActivityCode;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}