/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.messaging.ui.conversationlist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnItemTouchListener;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;

import com.android.messaging.R;
import com.android.messaging.util.Assert;
import com.superapps.util.Dimensions;

/**
 * Animation and touch helper class for Conversation List swipe.
 */
public class ConversationListSwipeHelper implements OnItemTouchListener {
    private static final int UNIT_SECONDS = 1000;
    private static final boolean ANIMATING = true;

    private static final float ERROR_FACTOR_MULTIPLIER = 1.2f;
    private static final float PERCENTAGE_OF_WIDTH_TO_DISMISS = 0.08f;
    private static final float FLING_PERCENTAGE_OF_WIDTH_TO_DISMISS = 0.02f;
    private static final int OPTIONS_VIEW_WIDTH = Dimensions.pxFromDp(78.7f) * 2;

    private static final long DEFAULT_RESTORE_ANIMATION_DURATION = 520;
    private static final long DEFAULT_SHOW_ANIMATION_DURATION = 600;
    private static final long MAX_TRANSLATION_ANIMATION_DURATION = 600;

    private final RecyclerView mRecyclerView;
    private final int mTouchSlop;
    private final int mMinimumFlingVelocity;
    private final int mMaximumFlingVelocity;
    private final boolean mIsRtl = Dimensions.isRtl();

    private final Interpolator mShowOrDismissOptionsInterpolator =
            PathInterpolatorCompat.create(0.26f, 1f, 0.48f, 1);
    private final Interpolator mShowOptionsReboundInterpolator =
            PathInterpolatorCompat.create(0.32f, 0.94f, 0.6f, 1);

    /* Valid throughout a single gesture. */
    private VelocityTracker mVelocityTracker;
    private float mInitialX;
    private float mInitialY;
    private float mInitTranslationX;
    private boolean mIsSwiping;
    private ConversationListItemView mListItemView;

    public ConversationListSwipeHelper(final RecyclerView recyclerView) {
        mRecyclerView = recyclerView;

        final Context context = mRecyclerView.getContext();
        final Resources res = context.getResources();

        final ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mTouchSlop = viewConfiguration.getScaledPagingTouchSlop();
        mMaximumFlingVelocity = Math.min(
                viewConfiguration.getScaledMaximumFlingVelocity(),
                res.getInteger(R.integer.swipe_max_fling_velocity_px_per_s));
        mMinimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
    }

    @Override
    public boolean onInterceptTouchEvent(final RecyclerView recyclerView, final MotionEvent event) {
        if (event.getPointerCount() > 1) {
            // Ignore subsequent pointers.
            return false;
        }

        // We are not yet tracking a swipe gesture. Begin detection by spying on
        // touch events bubbling down to our children.
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                final View viewAtPoint = mRecyclerView.findChildViewUnder(event.getX(), event.getY());
                animateDismissOtherItemOptions(viewAtPoint);
                if (!hasGestureSwipeTarget()) {
                    onGestureStart();

                    mVelocityTracker.addMovement(event);
                    mInitialX = event.getX();
                    mInitialY = event.getY();
                    if (viewAtPoint instanceof ConversationListItemView) {
                        final ConversationListItemView child = (ConversationListItemView) viewAtPoint;
                        if (child != null && child.isSwipeAnimatable()) {
                            // Begin detecting swipe on the target for the rest of the gesture.
                            mListItemView = child;
                            mInitTranslationX = mListItemView.getSwipeTranslationX();
                            if (mListItemView.isAnimating()) {
                                mListItemView = null;
                            }
                        }
                    } else {
                        mListItemView = null;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (hasValidGestureSwipeTarget()) {
                    mVelocityTracker.addMovement(event);

                    final int historicalCount = event.getHistorySize();
                    // First consume the historical events, then consume the current ones.
                    for (int i = 0; i < historicalCount + 1; i++) {
                        float currX;
                        float currY;
                        if (i < historicalCount) {
                            currX = event.getHistoricalX(i);
                            currY = event.getHistoricalY(i);
                        } else {
                            currX = event.getX();
                            currY = event.getY();
                        }
                        final float deltaX = currX - mInitialX;
                        final float deltaY = currY - mInitialY;
                        final float absDeltaX = Math.abs(deltaX);
                        final float absDeltaY = Math.abs(deltaY);

                        if (!mIsSwiping && absDeltaY > mTouchSlop
                                && absDeltaY > (ERROR_FACTOR_MULTIPLIER * absDeltaX)) {
                            // Stop detecting swipe for the remainder of this gesture.
                            onGestureEnd();
                            return false;
                        }

                        if (absDeltaX > mTouchSlop && mListItemView.isSwipeAnimatable()) {
                            // Swipe detected. Return true so we can handle the gesture in
                            // onTouchEvent.
                            mIsSwiping = true;

                            // We don't want to suddenly jump the slop distance.
                            mInitialX = event.getX();
                            mInitialY = event.getY();

                            onSwipeGestureStart(mListItemView);
                            return true;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (hasGestureSwipeTarget()) {
                    onGestureEnd();
                }
                break;
        }

        // Start intercepting touch events from children if we detect a swipe.
        return mIsSwiping;
    }

    @Override
    public void onTouchEvent(final RecyclerView recyclerView, final MotionEvent event) {
        // We should only be here if we intercepted the touch due to swipe.
        Assert.isTrue(mIsSwiping);

        // We are now tracking a swipe gesture.
        mVelocityTracker.addMovement(event);

        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_MOVE:
                if (hasValidGestureSwipeTarget()) {
                    //if (mListItemView.isSwipeOptionsShowing()) {
                    mListItemView.setSwipeTranslationX(mInitTranslationX + event.getX() - mInitialX);
                    //}
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (hasValidGestureSwipeTarget()) {
                    final float maxVelocity = mMaximumFlingVelocity;
                    mVelocityTracker.computeCurrentVelocity(UNIT_SECONDS, maxVelocity);
                    final float velocityX = getLastComputedXVelocity();

                    final boolean fastEnough = isTargetSwipedFastEnough();
                    final boolean farEnough = isTargetSwipedFarEnough();

                    final boolean shouldShowOption = (fastEnough || farEnough);

                    if (shouldShowOption) {
                        if (fastEnough) {
                            animateShowOption(mListItemView, velocityX);
                        } else {
                            animateShowOption(mListItemView);
                        }
                    } else {
                        animateRestore(mListItemView, velocityX);
                    }

                    onSwipeGestureEnd(mListItemView);
                } else {
                    onGestureEnd();
                }
                break;
//                if (hasValidGestureSwipeTarget()) {
//                    animateRestore(mListItemView, 0f);
//                    onSwipeGestureEnd(mListItemView);
//                } else {
//                    onGestureEnd();
//                }
//                break;
        }
    }


    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    /**
     * We have started to intercept a series of touch events.
     */
    private void onGestureStart() {
        mIsSwiping = false;
        // Work around bug in RecyclerView that sends two identical ACTION_DOWN
        // events to #onInterceptTouchEvent.
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.clear();
    }

    /**
     * The series of touch events has been detected as a swipe.
     * <p>
     * Now that the gesture is a swipe, we will begin translating the view of the
     * given viewHolder.
     */
    private void onSwipeGestureStart(final ConversationListItemView itemView) {
        mRecyclerView.getParent().requestDisallowInterceptTouchEvent(true);
        setHardwareAnimatingLayerType(itemView, ANIMATING);
        itemView.setAnimating(true);
    }

    /**
     * The current swipe gesture is complete.
     */
    private void onSwipeGestureEnd(final ConversationListItemView itemView) {
        // Balances out onSwipeGestureStart.
        itemView.setAnimating(false);

        onGestureEnd();
    }

    /**
     * The series of touch events has ended in an {@link MotionEvent#ACTION_UP}
     * or {@link MotionEvent#ACTION_CANCEL}.
     */
    private void onGestureEnd() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
        mIsSwiping = false;
        mListItemView = null;
    }

    public boolean animateDismissOtherItemOptions(View touchedView) {
        boolean hasViewNeedDismiss = false;
        for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
            if (mRecyclerView.getChildAt(i) instanceof ConversationListItemView) {
                ConversationListItemView view = (ConversationListItemView) mRecyclerView.getChildAt(i);
                if (view != touchedView && view.getSwipeTranslationX() != 0) {
                    animateRestore(view, 0);
                    hasViewNeedDismiss = true;
                }
            }
        }
        return hasViewNeedDismiss;
    }

    /**
     * A swipe animation has started.
     */
    private void onSwipeAnimationStart(final ConversationListItemView itemView) {
        // Disallow interactions.
        itemView.setAnimating(true);
        ViewCompat.setHasTransientState(itemView, true);
        setHardwareAnimatingLayerType(itemView, ANIMATING);
    }

    /**
     * The swipe animation has ended.
     */
    private void onSwipeAnimationEnd(final ConversationListItemView itemView) {
        // Restore interactions.
        itemView.setAnimating(false);
        ViewCompat.setHasTransientState(itemView, false);
        setHardwareAnimatingLayerType(itemView, !ANIMATING);
    }

    /**
     * Animate the dismissal of the given item. The given velocityX is taken into consideration for
     * the animation duration. Whether the item is dismissed to the left or right is dependent on
     * the given velocityX.
     */
    private void animateShowOption(final ConversationListItemView itemView, final float velocityX) {
        Assert.isTrue(velocityX != 0);
        animateShowOption(itemView, true, velocityX);
    }

    /**
     * Animate the dismissal of the given item. The velocityX is assumed to be 0.
     */
    private void animateShowOption(final ConversationListItemView itemView) {
        animateShowOption(itemView, true, 0f);
    }

    /**
     * Animate the dismissal of the given item.
     */
    private void animateShowOption(final ConversationListItemView itemView,
                                   final boolean showOption, final float velocityX) {

        onSwipeAnimationStart(itemView);

        final float animateTo = showOption ? mIsRtl ? OPTIONS_VIEW_WIDTH : -OPTIONS_VIEW_WIDTH : 0;
        final long duration;
        ObjectAnimator animator;
        if (mIsRtl ? itemView.getSwipeTranslationX() > OPTIONS_VIEW_WIDTH
                : itemView.getSwipeTranslationX() < -OPTIONS_VIEW_WIDTH) {
            animator = getSwipeTranslationXAnimator(
                    itemView, animateTo, mShowOptionsReboundInterpolator);
            animator.setDuration(400);
        } else {
            animator = getSwipeTranslationXAnimator(
                    itemView, animateTo, mShowOrDismissOptionsInterpolator);
            if (velocityX != 0) {
                final float deltaX = animateTo - itemView.getSwipeTranslationX();
                duration = calculateTranslationDuration(deltaX, velocityX);
                animator.setDuration(duration);
            }
        }

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                onSwipeAnimationEnd(itemView);
            }
        });
        animator.start();
    }

    /**
     * Animate the bounce back of the given item.
     */
    private void animateRestore(final ConversationListItemView itemView,
                                final float velocityX) {
        onSwipeAnimationStart(itemView);

        final float translationX = itemView.getSwipeTranslationX();
        final ObjectAnimator animator = getSwipeTranslationXAnimator(
                itemView, 0f, mShowOrDismissOptionsInterpolator);

        if (velocityX != 0 // Has velocity.
                && velocityX > 0 != translationX > 0) { // Right direction.
            long duration = calculateTranslationDuration(translationX, velocityX);
            animator.setDuration(duration);
        }
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                onSwipeAnimationEnd(itemView);
            }
        });
        animator.start();
    }

    /**
     * Create and start an animator that animates the given view's translationX
     * from its current value to the value given by animateTo.
     */
    private ObjectAnimator getSwipeTranslationXAnimator(final ConversationListItemView itemView,
                                                        final float animateTo, final TimeInterpolator interpolator) {
        final ObjectAnimator animator =
                ObjectAnimator.ofFloat(itemView, "swipeTranslationX", animateTo);
        animator.setDuration(animateTo == 0 ? DEFAULT_RESTORE_ANIMATION_DURATION : DEFAULT_SHOW_ANIMATION_DURATION);
        animator.setInterpolator(interpolator);
        return animator;
    }

    /**
     * Determine if the swipe has enough velocity to be dismissed.
     */
    private boolean isTargetSwipedFastEnough() {
        final float velocityX = getLastComputedXVelocity();
        final float velocityY = mVelocityTracker.getYVelocity();
        final float minVelocity = mMinimumFlingVelocity;
        final float translationX = mListItemView.getSwipeTranslationX();
        final float width = mListItemView.getWidth();
        return (Math.abs(velocityX) > minVelocity) // Fast enough.
                && (Math.abs(velocityX) > Math.abs(velocityY)) // Not unintentional.
                && (velocityX > 0) == (translationX > 0) // Right direction.
                && Math.abs(translationX) >
                FLING_PERCENTAGE_OF_WIDTH_TO_DISMISS * width; // Enough movement.
    }

    /**
     * Only used during a swipe gesture. Determine if the swipe has enough distance to be
     * dismissed.
     */
    private boolean isTargetSwipedFarEnough() {
        final float velocityX = getLastComputedXVelocity();

        final float translationX = mListItemView.getSwipeTranslationX();
        final float width = mListItemView.getWidth();

        //return Math.abs(translationX) > PERCENTAGE_OF_WIDTH_TO_DISMISS * width; // Enough movement.
        return (velocityX == 0 || (velocityX > 0) == (translationX > 0)) // Right direction.
                && Math.abs(translationX) > PERCENTAGE_OF_WIDTH_TO_DISMISS * width; // Enough movement.
    }

    private long calculateTranslationDuration(final float deltaPosition, final float velocity) {
        Assert.isTrue(velocity != 0);
        final float durationInSeconds = Math.abs(deltaPosition / velocity);
        return Math.min((int) (durationInSeconds * UNIT_SECONDS), MAX_TRANSLATION_ANIMATION_DURATION);
    }

    private boolean hasGestureSwipeTarget() {
        return mListItemView != null;
    }

    private boolean hasValidGestureSwipeTarget() {
        return hasGestureSwipeTarget() && mListItemView.getParent() == mRecyclerView;
    }

    /**
     * Enable a hardware layer for the it view and build that layer.
     */
    private void setHardwareAnimatingLayerType(final ConversationListItemView itemView,
                                               final boolean animating) {
        if (animating) {
            itemView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            if (itemView.getWindowToken() != null) {
                itemView.buildLayer();
            }
        } else {
            itemView.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }

    private float getLastComputedXVelocity() {
        return mVelocityTracker.getXVelocity();
    }
}