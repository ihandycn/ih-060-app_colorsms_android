package com.android.messaging.ui.wallpaper;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.superapps.util.Dimensions;

public class BackgroundImageView extends AppCompatImageView {

    public BackgroundImageView(Context context) {
        super(context);
    }

    public BackgroundImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BackgroundImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = Dimensions.getPhoneWidth(getContext());
        int height = Dimensions.getPhoneHeight(getContext()) -
                Dimensions.getStatusBarHeight(getContext()) -
                Dimensions.pxFromDp(56) - Dimensions.pxFromDp(48);
        setMeasuredDimension(width, height);
    }
}
