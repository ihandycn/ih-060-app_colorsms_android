package com.android.messaging.ui.customize.theme;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class ToolbarBackgroundImageView extends AppCompatImageView {

    public ToolbarBackgroundImageView(Context context) {
        super(context);
    }

    public ToolbarBackgroundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToolbarBackgroundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        int[] params = WallpaperSizeManager.getInstance().getToolbarFrameSize();
        if (params != null) {
            int width = params[0];
            int height = params[1];
            int l1 = 1;
            l = l - (width - r + l1) / 2;
            r = r + (width - r + l1) / 2;
            t = t - (height - (b - t));
        }
        super.layout(l, t, r, b);
    }
}
