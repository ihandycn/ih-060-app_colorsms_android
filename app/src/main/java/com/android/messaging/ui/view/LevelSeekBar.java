package com.android.messaging.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.android.messaging.R;
import com.superapps.util.Dimensions;

/**
 * A seek bar that draws its own track with scale lines.
 */
public class LevelSeekBar extends View {

    public static final int NORMAL_MARK = 0;
    public static final int ROUND_MARK = 1;

    private static final int DEFAULT_LEVEL_COUNT = 5;
    private static final int DEFAULT_LEVEL = 3;
    private static final int DEFAULT_TRACK_HEIGHT = Dimensions.pxFromDp(2.3f);
    private static final int DEFAULT_TRACK_BACKGROUND_HEIGHT = Dimensions.pxFromDp(2f);
    private static final int DEFAULT_TRACK_COLOR = 0xff1acc48;
    private static final int DEFAULT_TRACK_BACKGROUND_COLOR = 0xffd8dde2;
    private static final int DEFAULT_THUMB_RES_ID = R.drawable.settings_icon_size_slider;
    private static final int DEFAULT_SCALE_MARK_COLOR = 0xffffffff;
    private float mRoundRadius;

    public interface OnLevelChangeListener {
        void onLevelChanged(LevelSeekBar seekBar, int oldLevel, int newLevel, boolean fromUser);
    }

    private OnLevelChangeListener mOnLevelChangeListener;

    private int mScaleMark;
    private int mMarkColor;
    private int mLevelCount;
    private int mLevel = -1;
    private int mTrackHeight;
    private int mTrackBgHeight;
    private int mTrackColor;
    private int mTrackBgColor;
    private Drawable mThumbDrawable;

    private Paint mShapePaint;
    private RectF mRectF = new RectF();
    private boolean mIsRtl;

    public LevelSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.LevelSeekBar);
        mLevelCount = a.getInteger(R.styleable.LevelSeekBar_levelCount, DEFAULT_LEVEL_COUNT);
        mLevel = a.getInteger(R.styleable.LevelSeekBar_level, DEFAULT_LEVEL);
        mTrackHeight = a.getDimensionPixelSize(R.styleable.LevelSeekBar_trackHeight, DEFAULT_TRACK_HEIGHT);
        mTrackBgHeight = a.getDimensionPixelSize(R.styleable.LevelSeekBar_trackBackgroundHeight,
                DEFAULT_TRACK_BACKGROUND_HEIGHT);
        mTrackColor = a.getColor(R.styleable.LevelSeekBar_trackColor, DEFAULT_TRACK_COLOR);
        mTrackBgColor = a.getColor(R.styleable.LevelSeekBar_trackBackgroundColor, DEFAULT_TRACK_BACKGROUND_COLOR);
        mThumbDrawable = a.getDrawable(R.styleable.LevelSeekBar_thumb);
        if (mThumbDrawable == null) {
            mThumbDrawable = ContextCompat.getDrawable(context, DEFAULT_THUMB_RES_ID);
        }
        mScaleMark = a.getInt(R.styleable.LevelSeekBar_scaleMark, NORMAL_MARK);
        if (mScaleMark != NORMAL_MARK) {
            mMarkColor = a.getColor(R.styleable.LevelSeekBar_scaleMarkColor, DEFAULT_SCALE_MARK_COLOR);
        }
        a.recycle();
        mShapePaint = new Paint();
        mIsRtl = Dimensions.isRtl();
    }

    public int getScaleMark() {
        return mScaleMark;
    }

    public void setScaleMark(int scaleMark) {
        mScaleMark = scaleMark;
        invalidate();
    }

    public void setLevelCount(int levelCount) {
        mLevelCount = levelCount;
        invalidate();
    }

    public int getLevelCount() {
        return mLevelCount;
    }

    public void setLevel(int level) {
        setLevel(level, false);
    }

    private void setLevel(int level, boolean fromUser) {
        int oldLevel = mLevel;
        mLevel = level;
        if (mOnLevelChangeListener != null && (oldLevel != level || !fromUser)) {
            mOnLevelChangeListener.onLevelChanged(this, oldLevel, level, fromUser);
        }
        invalidate();
    }

    public void setOnLevelChangeListener(OnLevelChangeListener listener) {
        mOnLevelChangeListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        float gridDistance = (float) (getMeasuredWidth() - paddingLeft - paddingRight) / (mLevelCount - 1);
        int thumbX = Math.round(
                mIsRtl ? paddingLeft + width - mLevel * gridDistance : paddingLeft + mLevel * gridDistance);
        int trackBottom = (getMeasuredHeight() + mTrackHeight) / 2;
        final float cy = getMeasuredHeight() / 2;
        // Draw track
        mShapePaint.setColor(mTrackBgColor);
        if (mIsRtl) {
            mRectF.set(paddingLeft, trackBottom - mTrackBgHeight, thumbX, trackBottom);
        } else {
            mRectF.set(thumbX, trackBottom - mTrackBgHeight, paddingLeft + width, trackBottom);
        }
        mRoundRadius = mTrackBgHeight / 4f;
        doDrawTrack(canvas);

        // Draw track background
        mShapePaint.setColor(mTrackColor);
        if (mIsRtl) {
            mRectF.set(thumbX, trackBottom - mTrackHeight, paddingLeft + width, trackBottom);
        } else {
            mRectF.set(paddingLeft, trackBottom - mTrackHeight, thumbX, trackBottom);
        }
        mRoundRadius = mTrackHeight / 5f;
        doDrawTrack(canvas);

        // Draw scales
        float halfScaleWidth = mTrackHeight / 1.2f;
        for (int i = 0; i < mLevelCount; i++) {
            float x;
            if (i == 0) {
                x = mIsRtl ? paddingLeft + width : paddingLeft;
            } else if (i == mLevelCount - 1) {
                x = mIsRtl ? paddingLeft : paddingLeft + width;
            } else {
                x = mIsRtl ? paddingLeft + width - i * gridDistance : paddingLeft + i * gridDistance;
            }
            boolean onBackground = mIsRtl ? x < thumbX : x > thumbX;
            if (onBackground) {
                mShapePaint.setColor(mTrackBgColor);
                halfScaleWidth = mTrackBgHeight / 1.2f;
            }

            // first and last round divider needed.
            mShapePaint.setColor(mTrackBgColor);
            mShapePaint.setAntiAlias(true);
            canvas.drawCircle(x, cy, halfScaleWidth, mShapePaint);
        }

        // Draw scales background
        for (int i = 0; i < mLevel; i++) {
            float x = mIsRtl ? paddingLeft + width - i * gridDistance : paddingLeft + i * gridDistance;
            boolean onBackground = mIsRtl ? x < thumbX : x > thumbX;

            if (onBackground) {
                halfScaleWidth = mTrackBgHeight / 1.2f;
            }
            if (mScaleMark == ROUND_MARK) {
                mShapePaint.setColor(mMarkColor);
                mShapePaint.setAntiAlias(true);
                canvas.drawCircle(x, cy, halfScaleWidth, mShapePaint);
            }
        }

        // Draw thumb (fixed, thumbY now is centerY)
        int thumbY = (int) cy;
        int halfThumbWidth = mThumbDrawable.getIntrinsicWidth() / 2;
        int halfThumbHeight = mThumbDrawable.getIntrinsicHeight() / 2;
        mThumbDrawable.setBounds(thumbX - halfThumbWidth, thumbY - halfThumbHeight,
                thumbX + halfThumbWidth, thumbY + halfThumbHeight);
        mThumbDrawable.draw(canvas);
    }

    private void doDrawTrack(final Canvas canvas) {
        if (mScaleMark == ROUND_MARK) {
            canvas.drawRoundRect(mRectF, mRoundRadius, mRoundRadius, mShapePaint);
        } else {
            canvas.drawRect(mRectF, mShapePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float gridDistance = (float) (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / (mLevelCount - 1);
                int newLevel = Math.round((x - getPaddingLeft()) / gridDistance);
                if (mIsRtl) {
                    newLevel = mLevelCount - 1 - newLevel;
                }
                newLevel = Math.max(0, Math.min(newLevel, mLevelCount - 1));
                if (newLevel != mLevel) {
                    setLevel(newLevel, true);
                }
                return true;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }
}
