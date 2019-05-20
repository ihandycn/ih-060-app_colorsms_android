package com.android.messaging.ui.customize.theme;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.android.messaging.ui.customize.PrimaryColors;


public class ThemeDownloadingView extends View {
    private Paint mPaint;
    private int mThemeColor;
    private int mBaseColor = 0xffd9dfe4;
    private int mPercent;
    private int mBorderSize = 4;
    private RectF mRectF;

    public ThemeDownloadingView(Context context) {
        super(context);
    }

    public ThemeDownloadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemeDownloadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        mThemeColor = PrimaryColors.getPrimaryColor();
    }

    public void updatePercent(int percent) {
        mPercent = percent;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();
        if (width > 0) {
            float center = width / 2.0f;
            float radius = (width - mBorderSize) * 1.0f / 2;
            mPaint.setColor(mBaseColor);
            canvas.drawCircle(center, center, radius, mPaint);

            mPaint.setColor(mThemeColor);
            if (mRectF == null) {
                mRectF = new RectF(0, 0, width, height);
            }
            canvas.drawArc(mRectF, 0, mPercent * 360, false, mPaint);
        }
    }
}
