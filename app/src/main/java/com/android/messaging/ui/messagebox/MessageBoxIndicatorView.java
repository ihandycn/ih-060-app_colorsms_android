package com.android.messaging.ui.messagebox;


import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.android.messaging.R;
import com.superapps.util.Dimensions;

public class MessageBoxIndicatorView extends LinearLayout implements ViewPager.OnPageChangeListener {

    private final static float MAX_RATIO = 1.26f;

    private boolean mReveal;

    public MessageBoxIndicatorView(Context context) {
        super(context);
    }

    public MessageBoxIndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MessageBoxIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int mLastPosition = -1;

    public void initDot(int count, int selectedPosition) {
        if (count < 2) {
            return;
        }
        for (int i = 0; i < count; i++) {
            FrameLayout layout = new FrameLayout(getContext());
            int size = Dimensions.pxFromDp(7);
            LayoutParams params = new LayoutParams(size, size);
            addView(layout, params);

            View view = new View(getContext());
            view.setBackgroundResource(R.drawable.message_box_dot_bg_drawable);
            int dotSize = Dimensions.pxFromDp(5f);
            FrameLayout.LayoutParams dotParams = new FrameLayout.LayoutParams(dotSize, dotSize);
            dotParams.gravity = Gravity.CENTER;
            layout.addView(view, dotParams);
        }

        selectStatus(selectedPosition);
    }

    public void reveal() {
        if (!mReveal) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                float translationX = -Dimensions.pxFromDp((count - i) * 9 + 60);
                getChildAt(i).animate()
                        .translationXBy(translationX)
                        .setDuration(250L)
                        .setStartDelay(40L * i)
                        .setInterpolator(new OvershootInterpolator(3)).start();

                mReveal = true;
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (mLastPosition != position) {
            unSelectStatus(mLastPosition);
            selectStatus(position);

        }
    }

    private void selectStatus(int position) {
        if (position < getChildCount()) {
            View currentView = getChildAt(position);
            currentView.setSelected(true);
            currentView.setScaleY(MAX_RATIO);
            currentView.setScaleX(MAX_RATIO);
            mLastPosition = position;
        }
    }

    private void unSelectStatus(int position) {
        if (position < getChildCount() && position >= 0) {
            View lastView = getChildAt(position);
            lastView.setSelected(false);
            lastView.setScaleX(1f);
            lastView.setScaleY(1f);
        }
    }

    @Override
    public void removeAllViews() {
        super.removeAllViews();
        mLastPosition = -1;
        mReveal = false;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}

