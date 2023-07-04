package com.safecard.android.utils;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class CustomTypefaceSpan extends MetricAffectingSpan
{
    private Typeface typeFace;

    public CustomTypefaceSpan(Typeface typeFace) {
        this.typeFace = typeFace;
    }

    @Override
    public void updateMeasureState(TextPaint p) {
        p.setTypeface(typeFace);

    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setTypeface(typeFace);
    }
}