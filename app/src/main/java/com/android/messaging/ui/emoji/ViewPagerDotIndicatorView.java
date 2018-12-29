package com.android.messaging.ui.emoji;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.android.messaging.R;
import com.superapps.util.Dimensions;

public class ViewPagerDotIndicatorView extends LinearLayout implements ViewPager.OnPageChangeListener {

    private final static float MAX_RATIO = 1.26f;

    public ViewPagerDotIndicatorView(Context context) {
        super(context);
    }

    public ViewPagerDotIndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewPagerDotIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int mLastPosition;

    public void initDot(int count, int selectedPosition) {
        for (int i = 0; i < count; i++) {
            FrameLayout layout = new FrameLayout(getContext());
            int size = Dimensions.pxFromDp(7);
            int halfMargin = Dimensions.pxFromDp(2);
            LayoutParams params = new LayoutParams(size, size);
            params.leftMargin = halfMargin;
            params.rightMargin = halfMargin;
            addView(layout, params);

            View view = new View(getContext());
            view.setBackgroundResource(R.drawable.emoji_dot_bg_drawable);
            int dotSize = Dimensions.pxFromDp(5f);
            FrameLayout.LayoutParams dotParams = new FrameLayout.LayoutParams(dotSize, dotSize);
            dotParams.gravity = Gravity.CENTER;
            layout.addView(view, dotParams);
        }

        selectStatus(selectedPosition);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (mLastPosition != position) {
            unSelectStatus(mLastPosition);
            selectStatus(position);
            mLastPosition = position;
        }
    }

    private void selectStatus(int position) {
        View currentView = getChildAt(position);
        currentView.setSelected(true);
        currentView.setScaleY(MAX_RATIO);
        currentView.setScaleX(MAX_RATIO);
    }

    private void unSelectStatus(int position) {
        View lastView = getChildAt(position);
        lastView.setSelected(false);
        lastView.setScaleX(1f);
        lastView.setScaleY(1f);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
