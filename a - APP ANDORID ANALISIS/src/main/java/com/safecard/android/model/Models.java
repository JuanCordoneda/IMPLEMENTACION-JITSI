package com.safecard.android.model;

import android.content.Context;

public class Models {
    private static AccessModel accessModel;

    public static AccessModel getAccessModel(Context context) {
        if(accessModel == null){
            accessModel = new AccessModel(context);
        }
        return accessModel;
    }

    public static void forceReloadModel(Context context) {
        accessModel = new AccessModel(context);
    }
}
