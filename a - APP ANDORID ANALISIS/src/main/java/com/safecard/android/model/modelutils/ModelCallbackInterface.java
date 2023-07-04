package com.safecard.android.model.modelutils;

/**
 * Created by alonso on 21-06-17.
 */

public interface ModelCallbackInterface {
    void callSuccess(Object... objects);
    void callError(String errorType, String msg);
}