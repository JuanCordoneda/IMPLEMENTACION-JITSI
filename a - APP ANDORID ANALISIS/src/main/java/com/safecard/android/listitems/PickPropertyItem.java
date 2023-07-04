package com.safecard.android.listitems;

import com.safecard.android.activities.InvitationCustomizationActivity;

import org.json.JSONObject;

/**
 * Created by efajardo on 10/04/17.
 */

public class PickPropertyItem extends ListItem {

    private InvitationCustomizationActivity.Property property;

    public InvitationCustomizationActivity.Property getProperty(){
        return property;
    }

    public void setProperty(InvitationCustomizationActivity.Property property){
        this.property = property;
    }

    @Override
    public int getType() {
        return TYPE_PICK_PROPERTY;
    }
}
