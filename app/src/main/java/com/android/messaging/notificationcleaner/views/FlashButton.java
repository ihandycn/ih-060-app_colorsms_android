package com.android.messaging.notificationcleaner.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;

import com.android.messaging.R;
import com.superapps.util.Dimensions;

public class FlashButton extends android.support.v7.widget.AppCompatButton {
    private Bitmap mFlashBitmap;
    private Bitmap mFlashBound;
    private Paint mPaint;

    private PorterDuffXfermode mPorterDuffXfermode;

    private float mFlashTranslation;
    private float mFlashLeft;

    private boolean mEnableFlash;
    private boolean mBlockFlash;

    private ValueAnimator mFlashAnim;

    private int mRepeatCount = ValueAnimator.INFINITE;

    public FlashButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFlashBitmap = drawableToBitmap(context.getResources().getDrawable(R.drawable.button_flash));
        mFlashLeft = -mFlashBitmap.getWidth();

        mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
    }

    public void startFlash() {
        mEnableFlash = true;

        mFlashAnim = ValueAnimator.ofFloat(0, 1);
        mFlashAnim.addUpdateListener(animation -> {
            if (animation.getAnimatedFraction() < (450 / (float) 1450)) {
                mFlashLeft = -mFlashBitmap.getWidth() + mFlashTranslation * animation.getAnimatedFraction() * (1450 / (float) 450);
                invalidate();
            }
        });

        mFlashAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                if (!mEnableFlash) {
                    animation.cancel();
                    return;
                }

                mPorterDuffXfermode = new PorterDuffXfermode(mBlockFlash ? PorterDuff.Mode.CLEAR : PorterDuff.Mode.SRC_IN);
            }
        });

        mFlashAnim.setRepeatCount(mRepeatCount);
        mFlashAnim.setRepeatMode(ValueAnimator.RESTART);
        mFlashAnim.setDuration(1450).setInterpolator(new LinearInterpolator());
        mFlashAnim.start();
    }

    public void setRepeatCount(int mRepeatCount) {
        this.mRepeatCount = mRepeatCount;
    }

    public void stopFlash() {
        mEnableFlash = false;
        if (mFlashAnim != null) {
            mFlashAnim.setRepeatCount(0);
            mFlashAnim.end();
            mFlashAnim.cancel();
            mFlashAnim.removeAllListeners();
            mFlashAnim.removeAllUpdateListeners();
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        if (width <= 0 || height <= 0) {
            return;
        }

        float scale = height / (float) mFlashBitmap.getHeight();

        if (scale > 10) {
            return;
        }

        mFlashBitmap = getBitmap(mFlashBitmap, (int) (mFlashBitmap.getWidth() * scale), height);
        mFlashTranslation = width + 2 * mFlashBitmap.getWidth();

        mFlashBound = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas rectCanvas = new Canvas(mFlashBound);
        rectCanvas.drawRect(new RectF(0, 0, width, height), mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mBlockFlash = true;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mBlockFlash = false;
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mFlashBound == null) {
            return;
        }

        int layerId = canvas.saveLayer(0, 0, getWidth(), getHeight(), mPaint, Canvas.ALL_SAVE_FLAG);

        canvas.drawBitmap(mFlashBitmap, mFlashLeft, 0, mPaint);

        mPaint.setXfermode(mPorterDuffXfermode);

        canvas.drawBitmap(mFlashBound, 0, 0, mPaint);

        mPaint.setXfermode(null);

        canvas.restoreToCount(layerId);
    }

    public static Bitmap getBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleW = (float) width / w;
        float scaleH = (float) height / h;
        matrix.postScale(scaleW, scaleH);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }

    public static Bitmap drawableToBitmap(@NonNull Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        final int width = drawable.getIntrinsicWidth();
        final int height = drawable.getIntrinsicHeight();

        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);

        drawable.setBounds(0, 0, width, height);
        drawable.draw(new Canvas(bitmap));

        return bitmap;
    }
}