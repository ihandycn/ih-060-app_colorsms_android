package com.android.messaging.gesture;

import android.app.Activity;
import android.view.MotionEvent;

/**
 * Abstract detector for gestures.
 */
public abstract class GestureDetector {

    protected static final int DETECT_SCORE_DETECTED = 42;
    protected static final int DETECT_SCORE_LIKELY = 23;
    protected static final int DETECT_SCORE_UNLIKELY = 8;
    protected static final int DETECT_SCORE_RULED_OUT = 0;

    public interface OnGestureListener {
        void onEvent(int eventType);
    }

    protected Activity mActivity;
    protected OnGestureListener mListener;

    public GestureDetector(Activity activity) {
        this(activity, null);
    }

    public GestureDetector(Activity activity, OnGestureListener listener) {
        mActivity = activity;
        setListener(listener);
    }

    void setListener(OnGestureListener listener) {
        mListener = listener;
    }

    /**
     * @return One of {@link #DETECT_SCORE_DETECTED},
     * {@link #DETECT_SCORE_LIKELY},
     * {@link #DETECT_SCORE_UNLIKELY},
     * {@link #DETECT_SCORE_RULED_OUT}
     */
    public abstract int onTouchEvent(MotionEvent ev);

    public abstract void reset();
}
