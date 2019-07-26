package com.android.messaging.ui.customize.theme;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class ToolbarBackgroundImageView extends AppCompatImageView {

    private int mLeft;
    private int mRight;
    private int mTop;
    private int mBottom;

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
    public void layout(int l, int t, int r, int b) {
//        int[] params = WallpaperSizeManager.getInstance().getToolbarFrameSize();
//        if (params != null) {
//            int width = params[0];
//            int height = params[1];
//            int l1 = 1;
//            l = l - (width - r + l1) / 2;
//            r = r + (width - r + l1) / 2;
//            t = t - (height - (b - t));
//            mLeft = l;
//            mRight = r;
//            mTop = t;
//            mBottom = b;
//        }
        super.layout(l, t, r, b);
    }

    @Override
    protected void onDraw(Canvas canvas) {
     //   setFrame(mLeft, mTop, mRight, mBottom);
        super.onDraw(canvas);
    }
}
