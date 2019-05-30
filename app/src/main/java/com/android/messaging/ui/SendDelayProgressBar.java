package com.android.messaging.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.superapps.util.Dimensions;

import static android.graphics.Paint.Style.STROKE;

public class SendDelayProgressBar extends View {
    private final static float SEND_DELAY_PROGRESS_BAR_INITIAL_DIRECTION = 270.0f;
    private int mOutsideColor;    //进度的颜色
    private float mOutsideRadius;    //外圆半径大小
    private int mInsideColor;    //背景颜色
    private float mProgressWidth;    //圆环的宽度
    private int mMaxProgress;    //最大进度
    private float mProgress;    //当前进度
    private float mDirection;
    private int mCirclePoint;
    private RectF mOval;
    private float mInitialProgress;

    private Paint mPaint;
    private ValueAnimator mSendDelayCircleBarAnimator;

    public SendDelayProgressBar(Context context) {
        this(context, null);
    }

    public SendDelayProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SendDelayProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray sendDelayCircleProgressBar = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SendDelayCircleProgressBar, defStyleAttr, 0);
        mOutsideColor = sendDelayCircleProgressBar.getColor(R.styleable.SendDelayCircleProgressBar_outside_color, PrimaryColors.getPrimaryColor());
        mOutsideRadius = sendDelayCircleProgressBar.getDimension(R.styleable.SendDelayCircleProgressBar_outside_radius, Dimensions.pxFromDp(17.0f));
        mInsideColor = sendDelayCircleProgressBar.getColor(R.styleable.SendDelayCircleProgressBar_inside_color, getResources().getColor(R.color.inside_circle_color));
        mProgressWidth = sendDelayCircleProgressBar.getDimension(R.styleable.SendDelayCircleProgressBar_progress_width, Dimensions.pxFromDp(2.7f));
        mProgress = sendDelayCircleProgressBar.getFloat(R.styleable.SendDelayCircleProgressBar_progress, 0.0f);
        mMaxProgress = sendDelayCircleProgressBar.getInt(R.styleable.SendDelayCircleProgressBar_max_progress, 100);
        mDirection = sendDelayCircleProgressBar.getFloat(R.styleable.SendDelayCircleProgressBar_direction, SEND_DELAY_PROGRESS_BAR_INITIAL_DIRECTION);

        mInitialProgress = 0;
        sendDelayCircleProgressBar.recycle();
        mPaint = new Paint();
        mOval = new RectF(mCirclePoint - mOutsideRadius, mCirclePoint - mOutsideRadius, mCirclePoint + mOutsideRadius, mCirclePoint + mOutsideRadius);
        setProgress(mMaxProgress);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        int size = MeasureSpec.getSize(widthMeasureSpec);
        int mode = MeasureSpec.getMode(widthMeasureSpec);

        if (mode == MeasureSpec.EXACTLY) {
            width = size;
        } else {
            width = (int) ((2 * mOutsideRadius) + mProgressWidth);
        }
        size = MeasureSpec.getSize(heightMeasureSpec);
        mode = MeasureSpec.getMode(heightMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            height = size;
        } else {
            height = (int) ((2 * mOutsideRadius) + mProgressWidth);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCirclePoint = getMeasuredWidth() / 2;
        mOval.left = mCirclePoint - mOutsideRadius;
        mOval.top = mCirclePoint - mOutsideRadius;
        mOval.right = mCirclePoint + mOutsideRadius;
        mOval.bottom = mCirclePoint + mOutsideRadius;

        mPaint.setColor(mInsideColor);
        mPaint.setStyle(STROKE);
        mPaint.setStrokeWidth(mProgressWidth);
        mPaint.setAntiAlias(true);
        canvas.drawCircle(mCirclePoint, mCirclePoint, mOutsideRadius, mPaint);

        mPaint.setColor(mOutsideColor);
        canvas.drawArc(mOval, SEND_DELAY_PROGRESS_BAR_INITIAL_DIRECTION, 360 * (mInitialProgress / mMaxProgress),false, mPaint);
        canvas.drawArc(mOval, mDirection, 360 * (mProgress / mMaxProgress), false, mPaint);

    }

    public void startAnimation(long duration){
        if (duration != 0) {
            mSendDelayCircleBarAnimator.setDuration(1000 * duration);
            mSendDelayCircleBarAnimator.start();
        }
    }

    public void resetAnimation () {
        mSendDelayCircleBarAnimator.end();
        mProgress = 0;
        mInitialProgress = 0;
        invalidate();
    }

    public boolean isRunning() {
        return mSendDelayCircleBarAnimator.isRunning();
    }

    public float getProgress() {
        return mProgress;
    }

    public void setProgress(float progress){
        Interpolator sendDelayCircleBarInterpolator = PathInterpolatorCompat.create(0.33f,0.00f,0.67f,1.00f);
        mSendDelayCircleBarAnimator = ValueAnimator.ofFloat(0, progress);
        mInitialProgress = 100 - progress;
        mDirection = SEND_DELAY_PROGRESS_BAR_INITIAL_DIRECTION + mInitialProgress * 3.6f;
        mSendDelayCircleBarAnimator.addUpdateListener(animation -> {
            mProgress = (float) mSendDelayCircleBarAnimator.getAnimatedValue();
            invalidate();
        });
        mSendDelayCircleBarAnimator.setInterpolator(sendDelayCircleBarInterpolator);
        invalidate();
    }
}
