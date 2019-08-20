package com.android.messaging.notificationcleaner.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.android.messaging.R;

import java.util.ArrayList;
import java.util.List;

public class AnimatedNotificationGroup extends LinearLayout {

    public interface OnAnimationFinishListener {
        void onExpandFinish();

        void onStayItemCollapseFinish();
    }

    private static final int NOTIFICATION_GROUP_FIRST_POSITION = 0;

    public static final int NOTIFICATION_GROUP_STAY_ITEM_POSITION_1 = 1;
    public static final int NOTIFICATION_GROUP_STAY_ITEM_POSITION_3 = 3;

    public static final int[] ICON_IDS = {
        R.drawable.notification_cleaner_guide_icon_1, R.drawable.notification_cleaner_guide_icon_2, R.drawable.notification_cleaner_guide_icon_3,
        R.drawable.notification_cleaner_guide_icon_4, R.drawable.notification_cleaner_guide_icon_5, R.drawable.notification_cleaner_guide_icon_6,
        R.drawable.notification_cleaner_guide_icon_7, R.drawable.notification_cleaner_guide_icon_8, R.drawable.notification_cleaner_guide_icon_9
    };

    private OnAnimationFinishListener onAnimationFinishListener;

    public AnimatedNotificationGroup(Context context) {
        super(context);

        init();
    }

    public AnimatedNotificationGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public AnimatedNotificationGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public void setOnAnimationFinishListener(OnAnimationFinishListener onAnimationFinishListener) {
        this.onAnimationFinishListener = onAnimationFinishListener;
    }

    public void expandNotificationItems() {
        List<Animator> expandNotificationsSet = new ArrayList<>();

        for (int index = 0; index < ICON_IDS.length; index++) {
            expandNotificationsSet.add(((AnimatedNotificationItem) getChildAt(index)).generateItemExpandAnimator(index));
        }

        AnimatorSet expandNotificationAnimatorSet = new AnimatorSet();
        expandNotificationAnimatorSet.playTogether(expandNotificationsSet);
        expandNotificationAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onAnimationFinishListener == null) {
                    return;
                }

                onAnimationFinishListener.onExpandFinish();
            }
        });

        expandNotificationAnimatorSet.start();
    }

    public void collapseNotificationItems(AnimatedNotificationHeader topAnimatedHeader) {
        final int headTop = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_group_head_top);
        final int headItemTop = (int) getChildAt(0).getY() - headTop;

        List<Animator> notificationsAnimators = new ArrayList<>();
        notificationsAnimators.add(topAnimatedHeader.generateCollapseAnimation(0, headItemTop));

        for (int index = 0; index < ICON_IDS.length; index++) {
            AnimatedNotificationItem animatedNotificationItem = (AnimatedNotificationItem) getChildAt(index);

            if (animatedNotificationItem instanceof AnimatedNotificationCollapse) {
                AnimatedNotificationCollapse animatedNotificationCollapseItem = (AnimatedNotificationCollapse) animatedNotificationItem;
                animatedNotificationCollapseItem.setOnItemCollapseFinishListener(topAnimatedHeader);
            }

            if (index == 0) {
                animatedNotificationItem.setVisibility(INVISIBLE);
            } else {
                notificationsAnimators.add(animatedNotificationItem.generateCollapseAnimation(index, headItemTop));
            }
        }

        AnimatorSet collapseAnimatorSet = new AnimatorSet();
        collapseAnimatorSet.playTogether(notificationsAnimators);

        collapseAnimatorSet.start();
    }

    public void collapseStayItems() {
        final int animatedItemMarginTop = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_grout_stay_margin_top);
        final int animatedItemHeight = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_item_height);

        AnimatedNotificationStay animatedNotificationStayItem1 = (AnimatedNotificationStay) getChildAt(NOTIFICATION_GROUP_STAY_ITEM_POSITION_1);
        AnimatedNotificationStay animatedNotificationStayItem3 = (AnimatedNotificationStay) getChildAt(NOTIFICATION_GROUP_STAY_ITEM_POSITION_3);

        Animator collapseItemAnimator1 = animatedNotificationStayItem1.getCollapseItemAnimator(animatedNotificationStayItem1.getY());
        Animator collapseItemAnimator3 = animatedNotificationStayItem3.getCollapseItemAnimator(animatedNotificationStayItem1.getY()
            + animatedItemMarginTop + animatedItemHeight);

        AnimatorSet collapseItemAnimatorSet = new AnimatorSet();
        collapseItemAnimatorSet.playTogether(collapseItemAnimator1, collapseItemAnimator3);
        collapseItemAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onAnimationFinishListener == null) {
                    return;
                }

                onAnimationFinishListener.onStayItemCollapseFinish();
            }
        });

        collapseItemAnimatorSet.start();
    }

    private void init() {
        final int itemMarginLeftAndRight = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_item_left_and_right);
        final int itemMarginTop = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_item_margin_top);

        LayoutParams animatedItemParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        animatedItemParams.setMargins(itemMarginLeftAndRight, itemMarginTop, itemMarginLeftAndRight, 0);

        for (int index = 0; index < ICON_IDS.length; index++) {
            AnimatedNotificationItem animationNotificationItem;

            switch (index) {

                case NOTIFICATION_GROUP_FIRST_POSITION:

                    animationNotificationItem = new AnimatedNotificationHeader(getContext());

                    break;

                case NOTIFICATION_GROUP_STAY_ITEM_POSITION_1:
                case NOTIFICATION_GROUP_STAY_ITEM_POSITION_3:

                    animationNotificationItem = new AnimatedNotificationStay(getContext(), ICON_IDS[index]);

                    break;

                default:

                    animationNotificationItem = new AnimatedNotificationCollapse(getContext(), ICON_IDS[index]);

                    break;
            }

            addView(animationNotificationItem, index, animatedItemParams);
        }
    }
}
