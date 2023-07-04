package com.safecard.android.listitems;

/**
 * Created by efajardo on 15/12/16.
 */

public class TypeInvitationItem extends ListItem {

    private String status_invitation;

    public String getStatusInvitation(){
        return status_invitation;
    }

    public void setTypeInvitation(String status_invitation){
        this.status_invitation = status_invitation;
    }

    @Override
    public int getType() {
        return TYPE_STATUS;
    }
}
