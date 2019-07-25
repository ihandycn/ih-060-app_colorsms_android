package com.android.messaging.ui.conversationlist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.ColorInt;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import com.superapps.util.Dimensions;

public class CustomizeGuideBackgroundView extends FrameLayout {

    private float mHighlightCircleProgress;
    private float mDimmedBackgroundProgress;
    private float mCenterY;
    private Paint mCirclePaint;
    private Paint mRingPaint;
    private float mCirclePaintAlpha;
    private float mRingPaintAlpha;
    private ArgbEvaluator mColorEvaluator;
    private ValueAnimator mCustomizeGuideBackgroundDimmedAnimator;
    private ValueAnimator mCustomizeGuideMenuFocusShrinkAnimator;
    private ValueAnimator mCustomizeGuideMenuFocusEnlargeAnimator;
    private ValueAnimator mCustomizeGuideBackgroundDimmedDismissAnimator;
    private static final @ColorInt
    int DIMMED_BACKGROUND_COLOR = 0xcc000000;
    int RING_COLOR = 0x66FFFFFF;

    public CustomizeGuideBackgroundView(Context context) {
        this(context, null);
    }

    public CustomizeGuideBackgroundView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomizeGuideBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        AppCompatActivity activity = (AppCompatActivity) context;
        if (activity instanceof ConversationListActivity){
            int actionBarHeight = 0;
            if (activity.getSupportActionBar() != null) {
                actionBarHeight = activity.getSupportActionBar().getHeight();
            }
            mCenterY = actionBarHeight * 0.5f + Dimensions.getStatusBarHeight(activity);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorEvaluator = new ArgbEvaluator();

        mCirclePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mCirclePaint.setStyle(Paint.Style.FILL);
        Interpolator backgroundInterpolator = PathInterpolatorCompat.create(0.32f,
                0.66f, 0.6f, 1);

        mRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingPaint.setStrokeWidth(Dimensions.pxFromDp(1.3f));
        mRingPaint.setColor(RING_COLOR);
        mRingPaintAlpha = 1.0f;

        mCustomizeGuideBackgroundDimmedAnimator = ValueAnimator.ofFloat(0, 1);
        mCustomizeGuideBackgroundDimmedAnimator.setDuration(200);
        mCustomizeGuideBackgroundDimmedAnimator.setInterpolator(backgroundInterpolator);
        mCustomizeGuideBackgroundDimmedAnimator.addUpdateListener(animation -> {
            mDimmedBackgroundProgress = (float) mCustomizeGuideBackgroundDimmedAnimator.getAnimatedValue();
            invalidate();
        });

        mCustomizeGuideMenuFocusShrinkAnimator = ValueAnimator.ofFloat(16.53f, 0.75f);
        mCustomizeGuideMenuFocusShrinkAnimator.setDuration(200);
        mCustomizeGuideMenuFocusShrinkAnimator.setInterpolator(backgroundInterpolator);
        mCustomizeGuideMenuFocusShrinkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCustomizeGuideMenuFocusEnlargeAnimator.start();
            }
        });

        mCustomizeGuideMenuFocusShrinkAnimator.addUpdateListener(animation -> {
            mHighlightCircleProgress = (float) mCustomizeGuideMenuFocusShrinkAnimator.getAnimatedValue();
            invalidate();
        });

        mCustomizeGuideMenuFocusEnlargeAnimator = ValueAnimator.ofFloat(0.75f, 1);
        mCustomizeGuideMenuFocusEnlargeAnimator.setDuration(120);
        mCustomizeGuideMenuFocusEnlargeAnimator.addUpdateListener(animation -> {
            mHighlightCircleProgress = (float) mCustomizeGuideMenuFocusEnlargeAnimator.getAnimatedValue();
            invalidate();
        });

        mCustomizeGuideBackgroundDimmedDismissAnimator = ValueAnimator.ofFloat(1, 0);
        mCustomizeGuideBackgroundDimmedDismissAnimator.setDuration(120);
        mCustomizeGuideBackgroundDimmedDismissAnimator.setStartDelay(40);
        mCustomizeGuideBackgroundDimmedDismissAnimator.addUpdateListener(animation -> {
            mDimmedBackgroundProgress = (float) mCustomizeGuideBackgroundDimmedDismissAnimator.getAnimatedValue();
            invalidate();
        });
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
        mCirclePaint.setAlpha((int) (mCirclePaintAlpha * 255));
        if (mRingPaintAlpha == 0) {
            mRingPaint.setAlpha(0);
        }
        canvas.drawColor((Integer) mColorEvaluator.evaluate(mDimmedBackgroundProgress,
                Color.TRANSPARENT, DIMMED_BACKGROUND_COLOR));
        canvas.drawCircle(Dimensions.pxFromDp(26f), mCenterY,
                Dimensions.pxFromDp(21.7f) * mHighlightCircleProgress, mCirclePaint);
        canvas.drawCircle(Dimensions.pxFromDp(26f), mCenterY,
                Dimensions.pxFromDp(21.7f) * mHighlightCircleProgress, mRingPaint);
        super.dispatchDraw(canvas);
    }

    public void startCustomizeGuideBackgroundAppearAnimation() {
        mCustomizeGuideBackgroundDimmedAnimator.start();
        mCustomizeGuideMenuFocusShrinkAnimator.start();
    }

    public void startCustomizeGuideBackgroundDismissAnimation() {
        mCirclePaintAlpha = 0;
        mRingPaintAlpha = 0;
        invalidate();
        mCustomizeGuideBackgroundDimmedDismissAnimator.start();
    }
}
