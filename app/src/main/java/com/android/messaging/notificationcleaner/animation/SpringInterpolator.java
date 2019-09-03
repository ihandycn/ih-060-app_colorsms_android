package com.android.messaging.notificationcleaner.animation;

import android.animation.TimeInterpolator;

public class SpringInterpolator implements TimeInterpolator {

    private float mFactor;

    public SpringInterpolator(float factor) {
        mFactor = factor;
    }

    @Override
    public float getInterpolation(float t) {
        return (float) (Math.pow(2, -15 * t) * Math.sin((t - mFactor/ 4) * (2 * Math.PI) / mFactor) + 1);
    }
}
