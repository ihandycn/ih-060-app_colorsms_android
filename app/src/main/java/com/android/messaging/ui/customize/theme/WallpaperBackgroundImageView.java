package com.android.messaging.ui.customize.theme;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.superapps.util.Dimensions;

public class WallpaperBackgroundImageView extends AppCompatImageView {

    public WallpaperBackgroundImageView(Context context) {
        super(context);
    }

    public WallpaperBackgroundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WallpaperBackgroundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        b = Dimensions.getPhoneHeight(getContext()) - Dimensions.pxFromDp(56) - Dimensions.getStatusBarHeight(getContext());
        super.layout(l, t, r, b);
    }
}
