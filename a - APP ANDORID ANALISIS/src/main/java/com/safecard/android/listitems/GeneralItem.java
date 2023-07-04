package com.safecard.android.listitems;

import org.json.JSONObject;

/**
 * Created by efajardo on 15/12/16.
 */

public class GeneralItem extends ListItem {

    private JSONObject obj_array;

    public JSONObject getObj(){
        return obj_array;
    }

    public void setObj(JSONObject obj_json){
        this.obj_array = obj_json;
    }

    @Override
    public int getType() {
        return TYPE_GENERAL;
    }
}
