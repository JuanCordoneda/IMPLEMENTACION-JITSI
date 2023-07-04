package com.safecard.android.utils;

import android.view.View;

/**
 * Created by efajardo on 10/11/16.
 */

public interface ClickListener {
    void onClick(View view, int position);

    void onLongClick(View view, int position);
}
