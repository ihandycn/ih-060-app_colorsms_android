package com.android.messaging.ui.customize.mainpage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;

import com.superapps.util.Dimensions;

class ViewSwipeHelper {
    private float mInitY;
    private int mContainerMinHeight = Dimensions.pxFromDp(32);
    private int mContainerMaxHeight;
    private int mMaxDistance;
    private boolean mIsBottomMode = false;
    private View mContainerView;

    @SuppressLint("ClickableViewAccessibility")
    ViewSwipeHelper(@NonNull View slider, @NonNull View container) {
        mContainerView = container;
        slider.setOnTouchListener((v, event) -> {
            if (v != slider) {
                return false;
            }

            if (mContainerMaxHeight <= 0) {
                if (mContainerView.getHeight() > 0) {
                    mContainerMaxHeight = mContainerView.getHeight();
                    mMaxDistance = mContainerMaxHeight - mContainerMinHeight;
                }
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mInitY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float translateY = event.getRawY() - mInitY;
                    if (mIsBottomMode) {
                        // bottom to top
                        if (translateY > 0) {
                            translateY = 0;
                        } else if (translateY < -mMaxDistance) {
                            translateY = -mMaxDistance;
                        }
                        mContainerView.setTranslationY(mMaxDistance + translateY);
                    } else {
                        //top to bottom
                        if (translateY < 0) {
                            translateY = 0;
                        } else if (translateY > mMaxDistance) {
                            translateY = mMaxDistance;
                        }
                        mContainerView.setTranslationY(translateY);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    float y = event.getRawY() - mInitY;
                    int targetTranslationY;
                    if (mIsBottomMode) {
                        if (y > -mMaxDistance / 2) {
                            targetTranslationY = mMaxDistance;
                        } else {
                            targetTranslationY = 0;
                        }
                    } else {
                        if (y > mMaxDistance / 2) {
                            targetTranslationY = mMaxDistance;
                        } else {
                            targetTranslationY = 0;
                        }
                    }
                    if (mContainerView.getTranslationY() == targetTranslationY) {
                        mIsBottomMode = targetTranslationY != 0;
                    } else {
                        Animator animator = getGestureEndAnimation(y, targetTranslationY);
                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mIsBottomMode = !(mContainerView.getTranslationY() == 0);
                            }
                        });
                        animator.start();
                    }
                    mInitY = 0;
                    break;
            }
            return true;
        });
    }

    private Animator getGestureEndAnimation(float currentPosition, float targetPosition) {
        int duration;
        Interpolator interpolator;
        if (currentPosition > targetPosition) {
            //move up
            ObjectAnimator animator = ObjectAnimator.ofFloat(mContainerView, "translationY", targetPosition);
            duration = (int) (600 * (currentPosition - targetPosition) * 1.0f / mMaxDistance);
            interpolator = PathInterpolatorCompat.create(0.18f, 0.99f, 0.36f, 1);
            animator.setDuration(duration);
            animator.setInterpolator(interpolator);
            return animator;
        } else {
            //move down
            ObjectAnimator animator = ObjectAnimator.ofFloat(mContainerView, "translationY", targetPosition);
            duration = (int) (520 * (targetPosition - currentPosition) * 1.0f / mMaxDistance);
            interpolator = PathInterpolatorCompat.create(0.32f, 0.66f, 0.11f, 0.97f);
            animator.setDuration(duration);
            animator.setInterpolator(interpolator);
            return animator;
        }
    }
}
