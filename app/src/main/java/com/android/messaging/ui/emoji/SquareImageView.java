package com.android.messaging.ui.emoji;

import android.content.Context;
import android.util.AttributeSet;

import pl.droidsonroids.gif.GifImageView;

public class SquareImageView extends GifImageView {

    private boolean mMeasurementLocked;

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth, measuredWidth);
        if (measuredWidth > 0) {
            mMeasurementLocked = true;
        }
    }

    @Override
    public void requestLayout() {
        if (!mMeasurementLocked) {
            super.requestLayout();
        }
    }
}
