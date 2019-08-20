package com.android.messaging.notificationcleaner.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatImageView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.messaging.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class AnimatedNotificationHeader extends AnimatedNotificationItem
        implements AnimatedNotificationCollapse.OnItemCollapseFinish {

    public interface OnHeaderAnimationFinishListener {
        void onLastItemCollapsed();

        void onHeaderAnimated();
    }

    private static final long DURATION_BACKGROUND_CHANGE_AND_ICON_TRANSLATION = 500;
    private static final long DURATION_LAYOUT_BACKGROUND_CHANGE_AND_ICON_TRANSLATION = 250;
    private static final long DURATION_HEAD_TRANSLATE_AND_SHRINK = 500;
    private static final long DURATION_ICON_APPEAR = 200;
    private static final long DURATION_COLLAPSE_TOTAL = 1000;
    private static final long DURATION_HEAD_ICON_CHANGE_TO_SMALL = 400;
    private static final long DURATION_ELLIPSE_IMAGE_APPEAR = 300;

    private static final float RATIO_SCALE_X_HEADER_ENLARGE = 1.05f;
    private static final float RATIO_SCALE_SMALL_ICON_IN_HEADER = 0.7f;
    private static final float RATIO_SCALE_X_LAYOUT_SHRINK = 0.5f;
    private static final float RATIO_SCALE_ICON_TO_SMALL = 0.5f;
    private static final float RATIO_SCALE_ICON_TO_COLLAPSED_SMALL = 0.35f;

    private static final float PERCENT_HEADER_TITLE_WIDTH = 0.8f;
    private static final float PERCENT_HEADER_DESCRIPTION_WIDTH = 0.5f;
    private static final int BACKGROUND_COLOR_INT_END = 88;

    private OnHeaderAnimationFinishListener onHeaderAnimationFinishListener;
    private int collapsedItemNumber;

    public AnimatedNotificationHeader(Context context) {
        super(context, AnimatedNotificationGroup.ICON_IDS[0]);

        init();
    }

    public AnimatedNotificationHeader(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public void setOnHeaderAnimationFinishListener(OnHeaderAnimationFinishListener onHeaderAnimationFinishListener) {
        this.onHeaderAnimationFinishListener = onHeaderAnimationFinishListener;
    }

    @Override
    public void onItemCollapseFinish(int position) {
        collapsedItemNumber++;

        float headAppIconX = appIconImageView.getX();
        float headAppIconY = appIconImageView.getY();
        float headMarginRight = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_item_icon_margin_right);
        int headMarginFactor;
        int headWidthFactor;

        if (position == AnimatedNotificationGroup.NOTIFICATION_GROUP_STAY_ITEM_POSITION_1 + 1) {
            headMarginFactor = 1;
            headWidthFactor = 0;
        } else {
            headMarginFactor = position - AnimatedNotificationGroup.NOTIFICATION_GROUP_STAY_ITEM_POSITION_3 + 1;
            headWidthFactor = headMarginFactor - 1;
        }

        int headIconWidth = appIconImageView.getWidth() * 4 / 5;
        int headIconUnit = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_horizontal_icons_width);
        int headIconX = (int) (headAppIconX + headIconWidth + headIconUnit * headWidthFactor + headMarginRight * headMarginFactor);
        int headIconY = (int) (headAppIconY + getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_align_top));

        AppCompatImageView appIcon = new AppCompatImageView(getContext());
        appIcon.setImageResource(AnimatedNotificationGroup.ICON_IDS[position]);
        appIcon.setLayoutParams(new LayoutParams(headIconUnit, headIconUnit));
        this.addView(appIcon);
        ViewCompat.setX(appIcon, headIconX);
        ViewCompat.setY(appIcon, headIconY);

        int startY = headIconY + headIconUnit / 4;

        showIconInHeader(position, headIconY, appIcon, startY);
    }

    private void init() {
        LayoutParams containParams = (LayoutParams) itemContainerView.getLayoutParams();
        containParams.height = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_item_height);
        itemContainerView.setLayoutParams(containParams);

        titleView.getLayoutParams().width *= PERCENT_HEADER_TITLE_WIDTH;
        descriptionView.getLayoutParams().width *= PERCENT_HEADER_DESCRIPTION_WIDTH;
        requestLayout();
    }

    private void showIconInHeader(int position, int headEndY, AppCompatImageView appIcon, int headStartY) {
        PropertyValuesHolder alphaIconHolder = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
        PropertyValuesHolder yIconHolder = PropertyValuesHolder.ofFloat("y", headStartY, headEndY);

        ObjectAnimator iconAlphaAndTranslate = ObjectAnimator.ofPropertyValuesHolder(appIcon, alphaIconHolder, yIconHolder);
        iconAlphaAndTranslate.setDuration(DURATION_ICON_APPEAR);

        if (position == AnimatedNotificationGroup.ICON_IDS.length - 1) {
            iconAlphaAndTranslate.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    startIconMoveDownAnimation();

                    if (onHeaderAnimationFinishListener != null) {
                        onHeaderAnimationFinishListener.onLastItemCollapsed();
                    }
                }
            });
        }

        iconAlphaAndTranslate.start();
    }

    @Override
    protected Animator generateCollapseAnimation(int position, int headItemTop) {
        int itemTranslation = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_item_move_upwards);

        // background and enlarge
        AnimatorSet backgroundAndTranslationSet = getBackgroundChangeAnimator(itemTranslation);
        // icon translate and shrink
        ObjectAnimator iconTranslateAndScaleAnimator = getIconChangeAnimator();
        // layout shrink
        ObjectAnimator layoutShrinkAnimator = ObjectAnimator.ofFloat(titleAndDescriptionLayout, "scaleX", 1.0f, RATIO_SCALE_X_LAYOUT_SHRINK);
        layoutShrinkAnimator.setDuration(DURATION_COLLAPSE_TOTAL);
        // layout disappear
        ObjectAnimator layoutDisappearAnimator = ObjectAnimator.ofFloat(titleAndDescriptionLayout, "alpha", 1.0f, 0.0f);
        layoutDisappearAnimator.setStartDelay(DURATION_HEAD_TRANSLATE_AND_SHRINK);
        layoutDisappearAnimator.setDuration(DURATION_COLLAPSE_TOTAL - DURATION_HEAD_TRANSLATE_AND_SHRINK);

        AnimatorSet translateAndShrinkAnimatorSet = new AnimatorSet();
        translateAndShrinkAnimatorSet.playTogether(iconTranslateAndScaleAnimator, layoutShrinkAnimator, layoutDisappearAnimator);

        AnimatorSet headerAnimatorSet = new AnimatorSet();
        headerAnimatorSet.playSequentially(backgroundAndTranslationSet, translateAndShrinkAnimatorSet);

        return headerAnimatorSet;
    }

    @NonNull
    private ObjectAnimator getIconChangeAnimator() {
        final int iconMoveToRight = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_item_head_icon_move_to_right);
        final int iconMoveDown = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_item_head_icon_move_down);

        PropertyValuesHolder xScaleIconHolder = PropertyValuesHolder.ofFloat("scaleX", 1.0f, RATIO_SCALE_ICON_TO_SMALL);
        PropertyValuesHolder yScaleIconHolder = PropertyValuesHolder.ofFloat("scaleY", 1.0f, RATIO_SCALE_ICON_TO_SMALL);
        PropertyValuesHolder xTranslationIconHolder = PropertyValuesHolder.ofFloat("translationX", 0, iconMoveToRight);
        PropertyValuesHolder yTranslationIconHolder = PropertyValuesHolder.ofFloat("translationY", 0, iconMoveDown);

        ObjectAnimator iconTranslateAndScaleAnimator = ObjectAnimator.ofPropertyValuesHolder(
                appIconImageView, xScaleIconHolder, yScaleIconHolder, xTranslationIconHolder, yTranslationIconHolder);
        iconTranslateAndScaleAnimator.setDuration(DURATION_HEAD_TRANSLATE_AND_SHRINK);

        return iconTranslateAndScaleAnimator;
    }

    @NonNull
    private AnimatorSet getBackgroundChangeAnimator(int itemTranslation) {
        final GradientDrawable backgroundShapeDrawable = (GradientDrawable) itemContainerView.getBackground().mutate();

        ValueAnimator backgroundColorAnimator = ValueAnimator.ofInt(BACKGROUND_COLOR_INT_START, BACKGROUND_COLOR_INT_END);
        backgroundColorAnimator.addUpdateListener(animation -> {
            int colorValue = (int) animation.getAnimatedValue();
            backgroundShapeDrawable.setColor(Color.rgb(colorValue, colorValue, colorValue));
        });

        ObjectAnimator translationChangeAnimator = ObjectAnimator.ofFloat(this, "translationY", 0, -itemTranslation);
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(this, "scaleX", 1.0f, RATIO_SCALE_X_HEADER_ENLARGE);

        AnimatorSet backgroundAndTranslationSet = new AnimatorSet();
        backgroundAndTranslationSet.playTogether(backgroundColorAnimator, translationChangeAnimator, scaleXAnimator);
        backgroundAndTranslationSet.setDuration(DURATION_BACKGROUND_CHANGE_AND_ICON_TRANSLATION);

        return backgroundAndTranslationSet;
    }

    private void startIconMoveDownAnimation() {
        int itemStartTranslationY = -(int) getResources().getDimension(R.dimen.notification_cleaner_animated_item_move_upwards);

        ObjectAnimator translationXIconAnimator = ObjectAnimator.ofFloat(this, "translationY", itemStartTranslationY, 0);
        ObjectAnimator scaleXIconAnimator = ObjectAnimator.ofFloat(this, "scaleX", RATIO_SCALE_X_HEADER_ENLARGE, 1.0f);
        AnimatorSet translationAndScaleSet = new AnimatorSet();
        translationAndScaleSet.playTogether(translationXIconAnimator, scaleXIconAnimator);
        translationAndScaleSet.setDuration(DURATION_LAYOUT_BACKGROUND_CHANGE_AND_ICON_TRANSLATION);
        translationAndScaleSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startCollapseIconsInHeaderAnimation();
            }
        });
        translationAndScaleSet.start();
    }

    private void startCollapseIconsInHeaderAnimation() {
        List<Animator> iconAnimators = new ArrayList<>();
        int iconMoveDown = (int) getResources().getDimension(R.dimen.notification_cleaner_animated_item_head_icon_move_down);
        int iconMoveToLeft = (int) getResources().getDimension(R.dimen.notification_cleaner_animated_item_head_icon_move_left);

        // 原有icon变化
        addFirstIconAnimator(iconAnimators, iconMoveDown);
        // 添加后的icons变化
        addShowIconsAnimator(iconAnimators, iconMoveDown, iconMoveToLeft);
        // 剩余icon的处理
        addEllipseAndIconsAnimators(iconAnimators, iconMoveDown, iconMoveToLeft);

        addInterceptedTextAnimator(iconAnimators);

        startHeaderIconsAnimation(iconAnimators);
    }

    private void addInterceptedTextAnimator(List<Animator> iconAnimators) {
        float textMoveDown = (int) getResources().getDimension(R.dimen.notification_cleaner_animated_text_move_down);
        float textStartY = this.getY() - textMoveDown;
        TextView interceptedTextView = addInterceptedTextView(textStartY);

        PropertyValuesHolder alphaTextHolder = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
        PropertyValuesHolder yHolder = PropertyValuesHolder.ofFloat("y", textStartY, textStartY + textMoveDown / 2);
        ObjectAnimator textAnimator = ObjectAnimator.ofPropertyValuesHolder(interceptedTextView, alphaTextHolder, yHolder);
        textAnimator.setDuration(DURATION_HEAD_ICON_CHANGE_TO_SMALL);
        iconAnimators.add(textAnimator);
    }

    @NonNull
    private TextView addInterceptedTextView(float startY) {
        TextView interceptedTextView = new TextView(getContext());
        interceptedTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        interceptedTextView.setTextColor(getContext().getResources().getColor(android.R.color.white));
        interceptedTextView.setX(appIconImageView.getX() + getResources().getDimension(R.dimen.notification_cleaner_animated_item_icon_deviation));
        interceptedTextView.setY(startY);
        String numberText = NumberFormat.getInstance().format(AnimatedNotificationGroup.ICON_IDS.length - 3);
        String interceptedText = getResources().getString(R.string.notification_cleaner_intercepted, numberText);
        SpannableStringBuilder sb = new SpannableStringBuilder(interceptedText);
        sb.setSpan(new ForegroundColorSpan(0xFFF44336), interceptedText.indexOf(numberText),
                interceptedText.indexOf(numberText) + numberText.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        interceptedTextView.setText(sb);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        interceptedTextView.setLayoutParams(params);
        addView(interceptedTextView);
        return interceptedTextView;
    }

    private void startHeaderIconsAnimation(List<Animator> iconAnimators) {
        AnimatorSet iconsAnimatorSet = new AnimatorSet();
        iconsAnimatorSet.playTogether(iconAnimators);
        iconsAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onHeaderAnimationFinishListener.onHeaderAnimated();
            }
        });
        iconsAnimatorSet.start();
    }

    private void addEllipseAndIconsAnimators(List<Animator> iconAnimators, long iconMoveDown, long iconMoveLeft) {
        AppCompatImageView headIconImage = (AppCompatImageView) getChildAt(3);
        final AppCompatImageView ellipseImage = new AppCompatImageView(getContext());

        ellipseImage.setImageResource(R.drawable.notification_cleaner_block_ellipses);
        ellipseImage.setLayoutParams(headIconImage.getLayoutParams());
        ellipseImage.setX(headIconImage.getX() - iconMoveLeft);
        ellipseImage.setY(headIconImage.getY() + iconMoveDown);
        ellipseImage.setVisibility(View.INVISIBLE);
        addView(ellipseImage);

        PropertyValuesHolder alphaEllipseHolder = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
        PropertyValuesHolder xEllipseHolder = PropertyValuesHolder.ofFloat("x", headIconImage.getX() - iconMoveLeft, headIconImage.getX() - iconMoveLeft * 2);
        final ObjectAnimator ellipseAnimator = ObjectAnimator.ofPropertyValuesHolder(ellipseImage, alphaEllipseHolder, xEllipseHolder);
        ellipseAnimator.setDuration(DURATION_ELLIPSE_IMAGE_APPEAR);
        ellipseAnimator.setStartDelay(DURATION_HEAD_ICON_CHANGE_TO_SMALL - DURATION_ELLIPSE_IMAGE_APPEAR);
        ellipseAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                ellipseImage.postDelayed(() -> ellipseImage.setVisibility(View.VISIBLE), ellipseAnimator.getStartDelay());
            }
        });
        iconAnimators.add(ellipseAnimator);
    }

    private void addShowIconsAnimator(List<Animator> iconAnimators, long iconMoveDown, long iconMoveLeft) {
        for (int index = 1; index <= collapsedItemNumber; index++) {
            AppCompatImageView iconImage = (AppCompatImageView) this.getChildAt(index);
            PropertyValuesHolder scaleXIconHolder = PropertyValuesHolder.ofFloat("scaleX", 1.0f, RATIO_SCALE_SMALL_ICON_IN_HEADER);
            PropertyValuesHolder scaleYIconHolder = PropertyValuesHolder.ofFloat("scaleY", 1.0f, RATIO_SCALE_SMALL_ICON_IN_HEADER);
            PropertyValuesHolder yIconHolder = PropertyValuesHolder.ofFloat("y", iconImage.getY(), iconImage.getY() + iconMoveDown);
            PropertyValuesHolder xIconHolder = PropertyValuesHolder.ofFloat("x", iconImage.getX(), iconImage.getX() - iconMoveLeft * index);
            ObjectAnimator iconImageAnimator;
            if (index >= 3) {
                PropertyValuesHolder subIconAlpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f);
                iconImageAnimator = ObjectAnimator.ofPropertyValuesHolder(iconImage, scaleXIconHolder, scaleYIconHolder, xIconHolder, yIconHolder, subIconAlpha);
            } else {
                iconImageAnimator = ObjectAnimator.ofPropertyValuesHolder(iconImage, scaleXIconHolder, scaleYIconHolder, xIconHolder, yIconHolder);
            }
            iconImageAnimator.setDuration(DURATION_HEAD_ICON_CHANGE_TO_SMALL);
            iconAnimators.add(iconImageAnimator);
        }
    }

    private void addFirstIconAnimator(List<Animator> iconAnimators, long iconMoveDown) {
        PropertyValuesHolder firstIconXScale = PropertyValuesHolder.ofFloat("scaleX", RATIO_SCALE_ICON_TO_SMALL, RATIO_SCALE_ICON_TO_COLLAPSED_SMALL);
        PropertyValuesHolder firstIconYScale = PropertyValuesHolder.ofFloat("scaleY", RATIO_SCALE_ICON_TO_SMALL, RATIO_SCALE_ICON_TO_COLLAPSED_SMALL);
        PropertyValuesHolder firstYTranslation = PropertyValuesHolder.ofFloat("translationY", appIconImageView.getTranslationY(), appIconImageView.getTranslationY() + iconMoveDown);
        ObjectAnimator firstIconAnimator = ObjectAnimator.ofPropertyValuesHolder(appIconImageView, firstIconXScale, firstIconYScale, firstYTranslation);
        firstIconAnimator.setDuration(DURATION_HEAD_ICON_CHANGE_TO_SMALL);
        iconAnimators.add(firstIconAnimator);
    }
}
