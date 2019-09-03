package com.android.messaging.notificationcleaner.animation;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

import com.ihs.app.framework.HSApplication;

import java.util.WeakHashMap;

public class NotificationCleanerAnimatorUtils {

    private static WeakHashMap<Animator, Object> sAnimators = new WeakHashMap<>();

    private static Animator.AnimatorListener sEndAnimListener = new Animator.AnimatorListener() {
        public void onAnimationStart(Animator animation) {
            sAnimators.put(animation, null);
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            sAnimators.remove(animation);
        }

        public void onAnimationCancel(Animator animation) {
            sAnimators.remove(animation);
        }
    };

    private static long sShortAnimDuration;
    static {
        sShortAnimDuration = HSApplication.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    public static long getShortAnimDuration() {
        return sShortAnimDuration;
    }

    public static void cancelOnDestroyActivity(Animator a) {
        a.addListener(sEndAnimListener);
    }

    public static ValueAnimator ofFloat(View target, float... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setFloatValues(values);
        cancelOnDestroyActivity(anim);
        return anim;
    }
}
