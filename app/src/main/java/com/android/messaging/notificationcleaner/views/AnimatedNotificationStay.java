package com.android.messaging.notificationcleaner.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.android.messaging.R;

@SuppressLint("ViewConstructor")
public class AnimatedNotificationStay extends AnimatedNotificationItem {

    private static final int DURATION_CHANGE_TO_SHADOW = 500;
    private static final int DURATION_CHANGE_TO_COMMON = 200;
    private static final int DURATION_TRANSLATE_Y = 300;

    private static final int BACKGROUND_COLOR_INT_END = 200;

    public AnimatedNotificationStay(Context context, int iconResId) {
        super(context, iconResId);
        setRandomWidthForTitleAndDescription();
    }

    @Override
    protected Animator generateCollapseAnimation(int position, int headItemTop) {
        ValueAnimator backgroundChangeAnimator = getBackgroundChangeAnimator(BACKGROUND_COLOR_INT_START, BACKGROUND_COLOR_INT_END);
        backgroundChangeAnimator.setDuration(DURATION_CHANGE_TO_SHADOW);
        return backgroundChangeAnimator;
    }

    public Animator getCollapseItemAnimator(float toY) {
        ValueAnimator backgroundChangeAnimator = getBackgroundChangeAnimator(BACKGROUND_COLOR_INT_END, BACKGROUND_COLOR_INT_START);
        backgroundChangeAnimator.setDuration(DURATION_CHANGE_TO_COMMON);
        ObjectAnimator translateYAnimator = ObjectAnimator.ofFloat(this, "y", getY(), toY);
        translateYAnimator.setDuration(DURATION_TRANSLATE_Y);
        translateYAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        AnimatorSet stayAnimatorSet = new AnimatorSet();
        stayAnimatorSet.playSequentially(backgroundChangeAnimator, translateYAnimator);
        return stayAnimatorSet;
    }

    @NonNull
    private ValueAnimator getBackgroundChangeAnimator(int from, int to) {
        final GradientDrawable shapeDrawable = (GradientDrawable) itemContainerView.getBackground().mutate();
        shapeDrawable.setCornerRadius(getResources().getDimension(R.dimen.notification_cleaner_animated_init_item_radius));
        ValueAnimator backgroundChangeAnimator = ValueAnimator.ofInt(from, to);
        backgroundChangeAnimator.addUpdateListener(animation -> {
            int colorValue = (int) animation.getAnimatedValue();
            shapeDrawable.setColor(Color.rgb(colorValue, colorValue, colorValue));
        });
        return backgroundChangeAnimator;
    }

}
