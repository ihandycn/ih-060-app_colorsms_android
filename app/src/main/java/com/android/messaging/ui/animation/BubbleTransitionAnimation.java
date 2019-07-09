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

package com.android.messaging.ui.animation;

import android.graphics.Rect;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.android.messaging.util.UiUtils;
import com.superapps.util.Threads;

/**
 * Animates viewToAnimate from startRect to the place where it is in the layout,  viewToAnimate
 * should be in its final destination location before startAfterLayoutComplete is called.
 * viewToAnimate will be drawn scaled and offset in a popupWindow.
 * This class handles the case where the viewToAnimate moves during the animation
 */
public class BubbleTransitionAnimation extends Animation {
    /**
     * The view we're animating
     */
    private final View mViewToAnimate;


    /**
     * The rect that we're animating to.  This can change during the animation
     */
    private final Rect mDestRect;


    public BubbleTransitionAnimation(final View viewToAnimate) {
        mViewToAnimate = viewToAnimate;
        mDestRect = new Rect();
        setDuration(200);
    }


    /**
     * Ensures the animation is ready before starting the animation.
     * viewToAnimate must first be layed out so we know where we will animate to
     */
    public void startAfterLayoutComplete() {
        // We want layout to occur, and then we immediately animate it in, so hide it initially to
        // reduce jank on the first frame
//        mViewToAnimate.setVisibility(View.INVISIBLE);
        mViewToAnimate.setAlpha(0);

        final Runnable startAnimation = new Runnable() {
            boolean mRunComplete = false;
            boolean mFirstTry = true;

            @Override
            public void run() {
                if (mRunComplete) {
                    return;
                }

                mViewToAnimate.getGlobalVisibleRect(mDestRect);
                // In Android views which are visible but haven't computed their size yet have a
                // size of 1x1 because anything with a size of 0x0 is considered hidden.  We can't
                // start the animation until after the size is greater than 1x1
                if (mDestRect.width() <= 1 || mDestRect.height() <= 1) {
                    // Layout hasn't occurred yet
                    if (!mFirstTry) {
                        // Give up if this is not the first try, since layout change still doesn't
                        // yield a size for the view. This is likely because the media picker is
                        // full screen so there's no space left for the animated view. We give up
                        // on animation, but need to make sure the view that was initially
                        // hidden is re-shown.
                        mViewToAnimate.setAlpha(1);
                        mViewToAnimate.setVisibility(View.VISIBLE);
                    } else {
                        mFirstTry = false;
                        UiUtils.doOnceAfterLayoutChange(mViewToAnimate, this);
                    }
                    return;
                }
                Threads.postOnMainThreadDelayed(() -> {
                    mRunComplete = true;
                    mViewToAnimate.startAnimation(BubbleTransitionAnimation.this);
                    mViewToAnimate.invalidate();
                }, 200);
            }
        };

        startAnimation.run();
    }

    @Override
    protected void applyTransformation(final float interpolatedTime, final Transformation t) {
        mViewToAnimate.setAlpha(interpolatedTime);
    }

    @Override
    public boolean willChangeTransformationMatrix() {
        return false;
    }

    @Override
    public boolean willChangeBounds() {
        return false;
    }
}
