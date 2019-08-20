package com.android.messaging.notificationcleaner.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.android.messaging.R;

@SuppressLint("ViewConstructor")
public class AnimatedNotificationCollapse extends AnimatedNotificationItem {

    public interface OnItemCollapseFinish {
        void onItemCollapseFinish(int index);
    }

    private static final long DURATION_TRANSLATE_TO_ROUND = 550;
    private static final long DURATION_ITEM_MOVE_UP_AND_ENLARGE = 500;
    private static final long DELAY_ITEM_COLLAPSE_TRANSLATE = 150;
    private static final long DELAY_ITEM_COLLAPSE = 100;

    private static final float RATIO_SCALE_ENLARGE_TO = 1.05f;

    private OnItemCollapseFinish onItemCollapseFinishListener;

    public AnimatedNotificationCollapse(Context context, int iconResId) {
        super(context, iconResId);
        setRandomWidthForTitleAndDescription();
    }

    @Override
    protected Animator generateCollapseAnimation(final int position, int headItemTop) {
        ObjectAnimator scaleAndTranslateAnimator = getScaleAndTranslateAnimator(position);
        ObjectAnimator alphaTitleAndDescription = getLayoutDisappearAnimator();
        ObjectAnimator translateToTop = ObjectAnimator.ofFloat(this, "y", getY(), headItemTop);

        translateToTop.setStartDelay(DELAY_ITEM_COLLAPSE_TRANSLATE);
        ValueAnimator radiusChangeAnimator = getRadiusChangeAnimator();
        ValueAnimator widthChangeAnimator = getWidthChangeAnimator();

        AnimatorSet scaleTranslateToRound = new AnimatorSet();
        scaleTranslateToRound.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleTranslateToRound.setDuration(DURATION_TRANSLATE_TO_ROUND);
        scaleTranslateToRound.playTogether(alphaTitleAndDescription, translateToTop, radiusChangeAnimator, widthChangeAnimator);

        AnimatorSet itemShrinkSet = new AnimatorSet();
        itemShrinkSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(View.INVISIBLE);
                onItemCollapseFinishListener.onItemCollapseFinish(position);
            }
        });
        itemShrinkSet.playSequentially(scaleAndTranslateAnimator, scaleTranslateToRound);

        return itemShrinkSet;
    }

    @NonNull
    private ValueAnimator getWidthChangeAnimator() {
        ValueAnimator widthChangeAnimator = ValueAnimator.ofInt(getWidth(), getHeight());
        widthChangeAnimator.addUpdateListener(animation -> {
            int widthValue = (int) animation.getAnimatedValue();
            LayoutParams layoutParams = (LayoutParams) rootView.getLayoutParams();
            layoutParams.width = widthValue;
            rootView.setLayoutParams(layoutParams);
        });
        return widthChangeAnimator;
    }

    @NonNull
    private ValueAnimator getRadiusChangeAnimator() {
        final GradientDrawable backgroundShapeDrawable = (GradientDrawable) itemContainerView.getBackground().mutate();
        int initRadius = (int) getResources().getDimension(R.dimen.notification_cleaner_animated_init_item_radius);
        ValueAnimator radiusChangeAnimator = ValueAnimator.ofInt(initRadius, getWidth() / 2);
        radiusChangeAnimator.addUpdateListener(animation -> {
            int radiusValue = (int) animation.getAnimatedValue();
            backgroundShapeDrawable.setCornerRadius(radiusValue);
        });
        return radiusChangeAnimator;
    }

    @NonNull
    private ObjectAnimator getLayoutDisappearAnimator() {
        ObjectAnimator alphaLayoutAnimator = ObjectAnimator.ofFloat(titleAndDescriptionLayout, "alpha", 1.0f, 0.0f);
        alphaLayoutAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                LayoutParams layoutParams = (LayoutParams) rootView.getLayoutParams();
                layoutParams.addRule(CENTER_HORIZONTAL);
                layoutParams.setMargins(0, 0, 0, 0);
                rootView.setLayoutParams(layoutParams);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                titleAndDescriptionLayout.setVisibility(View.GONE);
            }
        });
        alphaLayoutAnimator.setDuration(DURATION_TRANSLATE_TO_ROUND / 5);
        return alphaLayoutAnimator;
    }

    @NonNull
    private ObjectAnimator getScaleAndTranslateAnimator(int position) {
        int itemTranslation = (int) getResources().getDimension(R.dimen.notification_cleaner_animated_item_move_upwards);

        PropertyValuesHolder xScaleHolder = PropertyValuesHolder.ofFloat("scaleX", 1.0f, RATIO_SCALE_ENLARGE_TO);
        PropertyValuesHolder yTranslationHolder = PropertyValuesHolder.ofFloat("translationY", 0, itemTranslation);

        ObjectAnimator scaleAndTranslateAnimator = ObjectAnimator.ofPropertyValuesHolder(this, xScaleHolder, yTranslationHolder);
        scaleAndTranslateAnimator.setStartDelay(DELAY_ITEM_COLLAPSE * position);
        scaleAndTranslateAnimator.setDuration(DURATION_ITEM_MOVE_UP_AND_ENLARGE);

        return scaleAndTranslateAnimator;
    }

    public void setOnItemCollapseFinishListener(OnItemCollapseFinish onItemCollapseFinishListener) {
        this.onItemCollapseFinishListener = onItemCollapseFinishListener;
    }
}
