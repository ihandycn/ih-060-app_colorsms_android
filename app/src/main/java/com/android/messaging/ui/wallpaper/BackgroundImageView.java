package com.android.messaging.ui.wallpaper;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class BackgroundImageView extends AppCompatImageView {

    private int width;
    private int height;

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
        if (width == 0 && height == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            setMeasuredDimension(width, height);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (width == 0 && height == 0 && (w > 0 || h > 0)) {
            width = w;
            height = h;
        }
    }
}
