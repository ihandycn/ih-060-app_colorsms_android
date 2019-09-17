package com.android.messaging.gesture;

import android.app.Activity;

/**
 * Detects swipe gestures with one or two fingers.
 */
public class SwipeRightGestureDetector extends SwipeGestureDetector {

    public static final int EVENT_SWIPE_RIGHT_ONE_FINGER = 20;
    public static final int EVENT_SWIPE_RIGHT_TWO_FINGERS = 21;

    public SwipeRightGestureDetector(Activity activity) {
        this(activity, null);
    }

    public SwipeRightGestureDetector(Activity activity, OnGestureListener listener) {
        super(activity, listener);
    }

    @Override
    protected float getVerticalSwipeDistance(float y, float downY) {
        return 0;
    }

    @Override
    protected float getHorizontalSwipeDistance(float x, float startX) {
        return x - startX;
    }

    @Override
    protected void reportEventOccurrence(float xDistance, float yDistance) {
//        if (mSwipeCount == 1 && mListener != null) {
//            mListener.onEvent(EVENT_SWIPE_RIGHT_ONE_FINGER);
//        } else if (mSwipeCount >= 2 && mListener != null) {
//            mListener.onEvent(EVENT_SWIPE_RIGHT_TWO_FINGERS);
//        }
        if (mListener != null && xDistance > mMinSwipeDistance && yDistance < mMinSwipeDistance) {
            mListener.onEvent(EVENT_SWIPE_RIGHT_ONE_FINGER);
        }
    }
}
