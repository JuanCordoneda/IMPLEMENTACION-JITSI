package com.safecard.android.listitems;

/**
 * Created by efajardo on 10/04/17.
 */

public class TypePropertyItem extends ListItem {

    private String property_type;

    public String getTypeProperty(){
        return property_type;
    }

    public void setTypeProperty(String property_type){
        this.property_type = property_type;
    }

    @Override
    public int getType() {
        return TYPE_PROPERTY;
    }
}
