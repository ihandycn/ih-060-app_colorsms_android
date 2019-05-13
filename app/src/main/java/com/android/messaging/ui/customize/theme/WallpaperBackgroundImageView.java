package com.android.messaging.ui.customize.theme;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

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
        int[] params = WallpaperSizeManager.getInstance().getWallpaperFrameSize();
        if (params != null) {
            int width = params[0];
            int height = params[1];
            int l1 = 1;
            l = l - (width - r + l1) / 2;
            r = r + (width - r + l1) / 2;
            b = t + height;
        }
        super.layout(l, t, r, b);
    }
}
