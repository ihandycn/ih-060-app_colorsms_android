package com.android.messaging.ui.customize.theme;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.View;

import com.android.messaging.R;
import com.superapps.util.Dimensions;

public class ThemeDownloadingView extends View {

    interface DownloadingAnimationEndListener {
        void onAnimationEnd();
    }

    private Paint mPaint;
    private int mThemeColor;
    private float mEndPercent = 0;
    private float mCurrentPercent = 0;
    private float mSpeed;
    private int mBorderSize = Dimensions.pxFromDp(1.3f);
    private RectF mRectF;
    private Choreographer mChoreographer;
    private Choreographer.FrameCallback mCallback;
    private DownloadingAnimationEndListener mEndListener;

    public ThemeDownloadingView(Context context) {
        super(context);
        init();
    }

    public ThemeDownloadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ThemeDownloadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mBorderSize);
        mThemeColor = getResources().getColor(R.color.primary_color);
    }

    public void updatePercent(float percent) {
        if (Math.abs(percent - 0) < 0.00001) {
            mEndPercent = 0;
            mSpeed = 0;
            return;
        }

        if (mEndPercent < percent) {
            mEndPercent = percent;
            mSpeed = (mEndPercent - mCurrentPercent) / 15;
        }
    }

    public void setEndListener(DownloadingAnimationEndListener listener) {
        mEndListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();
        if (width > 0) {
            float center = width / 2.0f;
            float radius = (width - mBorderSize) * 1.0f / 2;
            int mBaseColor = 0xffd9dfe4;
            mPaint.setColor(mBaseColor);
            canvas.drawCircle(center, center, radius, mPaint);

            if (mEndPercent > 0) {
                if (mCurrentPercent < mEndPercent) {
                    mCurrentPercent = mCurrentPercent + mSpeed;
                }
                mPaint.setColor(mThemeColor);
                if (mRectF == null) {
                    float offset = mBorderSize * 1.0f / 2;
                    mRectF = new RectF(offset, offset, width - offset, height - offset);
                }
                canvas.drawArc(mRectF, -90, mCurrentPercent * 360, false, mPaint);
                if (1 - mCurrentPercent < 0.00001) {
                    mCurrentPercent = 1;
                    mEndListener.onAnimationEnd();
                }
            } else {
                mCurrentPercent = 0;
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mChoreographer = Choreographer.getInstance();
        mCallback = frameTimeNanos -> {
            mChoreographer.postFrameCallback(mCallback);
            if ((Math.abs(mEndPercent - 0) < 0.00001 && Math.abs(mCurrentPercent - 0) > 0.00001)
                    || mEndPercent - mCurrentPercent > 0.00001) {
                invalidate();
            }
        };
        mChoreographer.postFrameCallback(mCallback);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mCallback != null) {
            mChoreographer.removeFrameCallback(mCallback);
        }
    }
}
