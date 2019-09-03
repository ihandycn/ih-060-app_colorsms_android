package com.android.messaging.notificationcleaner.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.messaging.R;

public abstract class AnimatedNotificationItem extends RelativeLayout {

    protected static final float PERCENT_MIN_TITLE_WIDTH = 0.5f;
    protected static final float PERCENT_MAX_TITLE_WIDTH = 0.9f;

    protected static final float PERCENT_MIN_DESCRIPTION_WIDTH = 0.3f;
    protected static final float PERCENT_MAX_DESCRIPTION_WIDTH = 0.7f;

    protected static final int BACKGROUND_COLOR_INT_START = 255;

    private static final long DURATION_ITEM_EXPAND = 240;
    private static final long DELAY_ITEM_EXPAND = DURATION_ITEM_EXPAND / 4;

    protected View rootView;
    protected View itemContainerView;
    protected View titleView;
    protected View descriptionView;
    protected LinearLayout titleAndDescriptionLayout;
    protected AppCompatImageView appIconImageView;

    // 在xml声明的item将会直接显示出来，不用等到调用动画start方法
    public AnimatedNotificationItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, 0);
    }

    public AnimatedNotificationItem(Context context, int iconResId) {
        super(context);

        init(context, iconResId);
    }

    protected void setRandomWidthForTitleAndDescription() {
        final float titleWidthPercent = (float) (Math.random() * (PERCENT_MAX_TITLE_WIDTH - PERCENT_MIN_TITLE_WIDTH) + PERCENT_MIN_TITLE_WIDTH);
        final float descriptionWidthPercent = (float) (Math.random() * (PERCENT_MAX_DESCRIPTION_WIDTH - PERCENT_MIN_DESCRIPTION_WIDTH) + PERCENT_MIN_DESCRIPTION_WIDTH);

        titleView.getLayoutParams().width *= titleWidthPercent;
        descriptionView.getLayoutParams().width *= descriptionWidthPercent;

        requestLayout();
    }

    private void init(Context context, int iconResId) {
        rootView = View.inflate(context, R.layout.notification_cleaner_animation_item, null);

        titleView = rootView.findViewById(R.id.title_notification_view);
        descriptionView = rootView.findViewById(R.id.description_notification_view);
        itemContainerView = rootView.findViewById(R.id.notification_item_container);
        titleAndDescriptionLayout = rootView.findViewById(R.id.title_and_description_layout);

        appIconImageView = rootView.findViewById(R.id.app_icon_iv);
        appIconImageView.setImageResource((iconResId == 0) ? AnimatedNotificationGroup.ICON_IDS[0] : iconResId);

        GradientDrawable gradientDrawable = (GradientDrawable) itemContainerView.getBackground().mutate();
        gradientDrawable.setColor(Color.argb(BACKGROUND_COLOR_INT_START, BACKGROUND_COLOR_INT_START,
            BACKGROUND_COLOR_INT_START, BACKGROUND_COLOR_INT_START));
        gradientDrawable.setCornerRadius(getResources().getDimension(R.dimen.notification_cleaner_animated_init_item_radius));

        addView(rootView);
    }

    protected abstract Animator generateCollapseAnimation(int position, int headItemTop);

    public AnimatorSet generateItemExpandAnimator(int index) {
        final int itemHeight = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_item_height);

        AnimatorSet itemExpandAnimatorSet = new AnimatorSet();

        ValueAnimator heightItemAnimator = ValueAnimator.ofInt(0, itemHeight);
        heightItemAnimator.addUpdateListener(valueAnimator -> {
            LayoutParams containParams = (LayoutParams) itemContainerView.getLayoutParams();
            containParams.height = (Integer) valueAnimator.getAnimatedValue();
            itemContainerView.setLayoutParams(containParams);
        });

        ObjectAnimator alphaItemAnimator = ObjectAnimator.ofFloat(this, "alpha", 0.0f, 1.0f);
        itemExpandAnimatorSet.playTogether(heightItemAnimator, alphaItemAnimator);
        itemExpandAnimatorSet.setInterpolator(new AccelerateInterpolator());
        itemExpandAnimatorSet.setDuration(DURATION_ITEM_EXPAND);
        itemExpandAnimatorSet.setStartDelay(DELAY_ITEM_EXPAND * index);

        return itemExpandAnimatorSet;
    }
}
