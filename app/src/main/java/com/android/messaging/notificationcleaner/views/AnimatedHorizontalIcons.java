package com.android.messaging.notificationcleaner.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.widget.LinearLayout;


import com.android.messaging.R;

import java.util.ArrayList;
import java.util.List;

public class AnimatedHorizontalIcons extends LinearLayout {

    public static final int[] SMALL_ICON_IDS = {
        R.drawable.notification_cleaner_guide_smallicon1, R.drawable.notification_cleaner_smallicon2, R.drawable.notification_cleaner_guide_smallicon3,
        R.drawable.notification_cleaner_guide_smallicon4, R.drawable.notification_cleaner_guide_smallicon5, R.drawable.notification_cleaner_smallicon6,
        R.drawable.notification_cleaner_smallicon7, R.drawable.notification_cleaner_smallicon8, R.drawable.notification_cleaner_guide_smallicon9
    };

    private static final long DURATION_ICONS_ANIMATOR = 120;
    private static final long DELAY_ICON_START_MOVE_ANIMATOR = DURATION_ICONS_ANIMATOR / 3;

    public AnimatedHorizontalIcons(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        final int iconSideLength = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_horizontal_icons_width);
        final int marginRight = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_icons_margin_right);

        for (int smallIconId : SMALL_ICON_IDS) {
            AppCompatImageView iconImage = new AppCompatImageView(getContext());
            iconImage.setVisibility(INVISIBLE);

            LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(iconSideLength, iconSideLength);
            layoutParams.rightMargin = marginRight;
            iconImage.setLayoutParams(layoutParams);
            iconImage.setImageResource(smallIconId);

            addView(iconImage);
        }
    }

    private List<Animator> generateExpandAnimators() {
        List<Animator> expandAnimators = new ArrayList<>();

        final int iconMarginRight = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_icons_margin_right);
        final int iconTransXValue = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_icons_trans_x_value);

        for (int index = 0; index < SMALL_ICON_IDS.length; index++) {
            final AppCompatImageView iconImageView = (AppCompatImageView) getChildAt(index);

            final ObjectAnimator alphaIconAnimator = ObjectAnimator.ofFloat(iconImageView, "alpha", 0f, 1f);
            final ObjectAnimator transXIconAnimator = ObjectAnimator.ofFloat(iconImageView,
                "translationX", 0f, iconTransXValue + iconMarginRight * index);

            final AnimatorSet iconAnimatorSet = new AnimatorSet();

            iconAnimatorSet.setDuration(DURATION_ICONS_ANIMATOR);
            iconAnimatorSet.setStartDelay(DELAY_ICON_START_MOVE_ANIMATOR * index);
            iconAnimatorSet.playTogether(alphaIconAnimator, transXIconAnimator);
            iconAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // 等待delay的时间再使image变可视状态
                    iconImageView.postDelayed(() ->
                            iconImageView.setVisibility(VISIBLE), iconAnimatorSet.getStartDelay());
                }
            });

            expandAnimators.add(iconAnimatorSet);
        }

        return expandAnimators;
    }

    private List<Animator> generateCollapseAnimators() {
        List<Animator> collapseAnimators = new ArrayList<>();

        final int iconWidth = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_horizontal_icons_width);
        final int marginRight = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_icons_margin_right);
        final int transXValue = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_icons_trans_x_value);

        for (int index = SMALL_ICON_IDS.length - 1; index >= 0; index--) {
            final AppCompatImageView iconImage = (AppCompatImageView) getChildAt(index);

            ObjectAnimator alphaIconAnimator;
            ObjectAnimator transXIconAnimator;

            switch (index) {
                case AnimatedNotificationGroup.NOTIFICATION_GROUP_STAY_ITEM_POSITION_1:
                    alphaIconAnimator = ObjectAnimator.ofFloat(iconImage, "alpha", 1.0f, 1.0f);
                    transXIconAnimator = ObjectAnimator.ofFloat(iconImage, "translationX",
                        transXValue + marginRight * index, -(iconWidth - transXValue));
                    break;
                case AnimatedNotificationGroup.NOTIFICATION_GROUP_STAY_ITEM_POSITION_3:

                    alphaIconAnimator = ObjectAnimator.ofFloat(iconImage, "alpha", 1.0f, 1.0f);
                    transXIconAnimator = ObjectAnimator.ofFloat(iconImage, "translationX",
                        transXValue + marginRight * index, -(iconWidth * 2 - transXValue) + marginRight);
                    break;
                default:
                    alphaIconAnimator = ObjectAnimator.ofFloat(iconImage, "alpha", 1.0f, 0.0f);
                    transXIconAnimator = ObjectAnimator.ofFloat(iconImage, "translationX",
                        transXValue + marginRight * index, 0);
                    break;
            }

            AnimatorSet iconAnimatorSet = new AnimatorSet();
            iconAnimatorSet.setDuration(DURATION_ICONS_ANIMATOR);
            iconAnimatorSet.setStartDelay(DELAY_ICON_START_MOVE_ANIMATOR * (SMALL_ICON_IDS.length - (index + 1)));
            iconAnimatorSet.playTogether(alphaIconAnimator, transXIconAnimator);

            collapseAnimators.add(iconAnimatorSet);
        }

        return collapseAnimators;
    }

    public void expandHorizontalIcons() {
        AnimatorSet expandIconsAnimatorSet = new AnimatorSet();
        expandIconsAnimatorSet.playTogether(generateExpandAnimators());
        expandIconsAnimatorSet.start();
    }

    public void collapseHorizontalIcons() {
        AnimatorSet collapseIconsAnimatorSet = new AnimatorSet();
        collapseIconsAnimatorSet.playTogether(generateCollapseAnimators());
        collapseIconsAnimatorSet.start();
    }
}
