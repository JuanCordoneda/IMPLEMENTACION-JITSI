package com.safecard.android.listitems;

/**
 * Created by efajardo on 15/12/16.
 */

public abstract class ListItem {
    public static final int TYPE_STATUS = 0;
    public static final int TYPE_GENERAL = 1;
    public static final int TYPE_PROPERTY = 2;
    public static final int TYPE_PICK_PROPERTY = 3;
    public static final int TYPE_INVITATION_REQUEST = 4;

    abstract public int getType();
}
