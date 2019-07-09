package com.android.messaging.ui.emoji;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.superapps.view.ViewPagerFixed;

public class ViewPagerWithoutAnim extends ViewPagerFixed {
    public ViewPagerWithoutAnim(Context context) {
        super(context);
    }

    public ViewPagerWithoutAnim(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @SuppressWarnings("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item, false);
    }
}
