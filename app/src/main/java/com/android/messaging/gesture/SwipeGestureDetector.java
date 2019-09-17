package com.android.messaging.gesture;

import android.app.Activity;
import android.support.v4.view.MotionEventCompat;
import android.util.SparseArray;
import android.view.MotionEvent;

import com.android.messaging.BuildConfig;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

/**
 * Detects swipe gestures with one or two fingers.
 */
@SuppressWarnings("WeakerAccess")
public abstract class SwipeGestureDetector extends GestureDetector {

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean DEBUG_LOG = false && BuildConfig.DEBUG;

    /**
     * Minimum length of a swipe / screen HEIGHT
     */
    private static final float MIN_SWIPE_DISTANCE_RATIO = 0.20f;

    /**
     * Maximum "finger distance change" / screen WIDTH
     */
    private static final float MAX_FINGER_DISTTANCE_CHANGE_RATIO = 0.13f;

    /**
     * Minimum length of a swipe.
     */
    protected final int mMinSwipeDistance;

    /**
     * Maximum value of |d1 - d2| for a gesture to be defined as a two-finger swipe, where
     * <p>
     * - d1: distance between two fingers when the second finger is down
     * - d2: distance between two fingers when the first finger is up
     */
    private final int mMaxFingerDistanceChange;

    private float mOriginDis = -1.0f;

    protected int mSwipeCount;
    private SparseArray<Float> mCoordYMap = new SparseArray<>(2);
    private SparseArray<Float> mCoordXMap = new SparseArray<>(2);

    public SwipeGestureDetector(Activity activity, OnGestureListener listener) {
        super(activity, listener);

        // Setup thresholds
        //Point screenSize = Utils.getScreenSize(activity);
        mMinSwipeDistance = (int) (MIN_SWIPE_DISTANCE_RATIO * Dimensions.getPhoneWidth(HSApplication.getContext()));
        mMaxFingerDistanceChange = (int) (MAX_FINGER_DISTTANCE_CHANGE_RATIO * Dimensions.getPhoneWidth(HSApplication.getContext()));
    }

    public void reset() {
        mSwipeCount = 0;
        mCoordYMap.clear();
        mCoordXMap.clear();
        mOriginDis = -1.0f;
    }

    @Override
    public int onTouchEvent(MotionEvent ev) {
        if (DEBUG_LOG) {
            HSLog.v("SwipeUpGestureDetector.onTouchEvent", "Event: " + ev);
        }

        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN:
                reset();
                handlePointerEvent(0, MotionEvent.ACTION_DOWN, ev.getX(), ev.getY());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                handlePointerEvent(1, MotionEvent.ACTION_DOWN, ev.getX(), ev.getY());
                mOriginDis = twoFingerDistance(ev);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                handlePointerEvent(ev.getPointerId(ev.getActionIndex()), MotionEvent.ACTION_UP, ev.getX(ev.getActionIndex()), ev.getY(ev.getActionIndex()));
                float currentDis = twoFingerDistance(ev);
                if (Math.abs(mOriginDis - currentDis) > mMaxFingerDistanceChange) {
                    return DETECT_SCORE_RULED_OUT;
                }
                break;
            case MotionEvent.ACTION_UP:
                handlePointerEvent(ev.getPointerId(0), MotionEvent.ACTION_UP, ev.getX(), ev.getY());

                Float startX = mCoordXMap.get(0);
                reportEventOccurrence(ev.getX() - startX, ev.getY() - mCoordYMap.get(0));
                break;
        }
        return DETECT_SCORE_LIKELY;
    }

    private void handlePointerEvent(int pointerId, int action, float x, float y) {
        if (DEBUG_LOG) {
            HSLog.v("SwipeGestureDetector.handlePointerEvent", "Index: " + pointerId + ", type: " + action);
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mCoordYMap.put(pointerId, y);
                mCoordXMap.put(pointerId, x);
                break;
            case MotionEvent.ACTION_UP:
//                Float downY = mCoordYMap.get(pointerId);
//                if (downY == null) {
//                    downY = mCoordYMap.get(0);
//                }
//                if (downY != null) {
//                    if (getVerticalSwipeDistance(y, downY) > mMinSwipeDistance) {
//                        mSwipeCount++;
//                    }
//                }

                Float startX = mCoordXMap.get(pointerId);
                if (startX == null) {
                    startX = mCoordXMap.get(0);
                }
                if (startX != null) {
                    if (getHorizontalSwipeDistance(x, startX) > mMinSwipeDistance) {
                        mSwipeCount++;
                    }
                }
                break;
        }
    }

    protected abstract float getVerticalSwipeDistance(float y, float downY);

    protected abstract float getHorizontalSwipeDistance(float X, float startX);

    protected abstract void reportEventOccurrence(float xDistance, float yDistance);

    private float twoFingerDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
}
