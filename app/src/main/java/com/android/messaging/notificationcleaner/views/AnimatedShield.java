package com.android.messaging.notificationcleaner.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.messaging.R;

public class AnimatedShield extends RelativeLayout {

    public interface OnAnimationFinishListener {
        void onCrossLineAnimationFinish();
    }

    private class CrossLineLayout extends RelativeLayout {
        private static final int ANGLE_ROTATE = 135;
        private static final float RATIO_SCALE_CROSS_LINE = 0.65f;
        private static final long DURATION_LINE_CROSS = 150;

        private ImageView crossLineImageView;

        public CrossLineLayout(Context context) {
            super(context);
            init();
        }

        public CrossLineLayout(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        public CrossLineLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            int crossLineWidth = (int) getResources().getDimension(R.dimen.notification_cleaner_animated_cross_line_width);
            int crossLineMarginTop = (int) getResources().getDimension(R.dimen.notification_cleaner_animated_cross_line_marginTop);
            int adjustTranslationX = (int) getResources().getDimension(R.dimen.notification_cleaner_animated_cross_line_adjust_x);

            initCrossLineLayout(crossLineWidth, crossLineMarginTop, adjustTranslationX);
            initCrossLine(crossLineWidth);
        }

        private void initCrossLine(int imageWidthPixels) {
            crossLineImageView = new ImageView(getContext());
            LayoutParams crossLineParams = new LayoutParams(imageWidthPixels, ViewGroup.LayoutParams.WRAP_CONTENT);
            crossLineImageView.setLayoutParams(crossLineParams);
            crossLineImageView.setImageResource(R.drawable.notification_cleaner_animated_cross_line);
            crossLineImageView.setScaleType(ImageView.ScaleType.MATRIX);
            this.addView(crossLineImageView);
        }

        private void initCrossLineLayout(int imageWidthPixels, int imageMarginTop, int adjustX) {
            LayoutParams crossLineParams = new LayoutParams(imageWidthPixels, LayoutParams.WRAP_CONTENT);
            crossLineParams.setMargins(0, imageMarginTop, 0, 0);
            crossLineParams.addRule(CENTER_HORIZONTAL);
            this.setLayoutParams(crossLineParams);
            this.setVisibility(View.INVISIBLE);
            this.setRotation(ANGLE_ROTATE);
            ViewCompat.setTranslationX(this, adjustX);
            ViewCompat.setScaleX(this, RATIO_SCALE_CROSS_LINE);
            ViewCompat.setScaleY(this, RATIO_SCALE_CROSS_LINE);
        }

        public void startCrossLineAnimation() {
            final int crossLineWidth = (int) getResources().getDimension(R.dimen.notification_cleaner_animated_cross_line_width);
            final int crossLineMarginTop = (int) getResources().getDimension(R.dimen.notification_cleaner_animated_cross_line_marginTop);

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(crossLineWidth, 0.0f);
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    startCollapseAnimation();
                    if (onAnimationFinishListener != null) {
                        onAnimationFinishListener.onCrossLineAnimationFinish();
                    }
                }
            });
            valueAnimator.addUpdateListener(animation -> {
                float marginRight = (float) animation.getAnimatedValue();
                LayoutParams crossLineParams = (LayoutParams) crossLineImageView.getLayoutParams();
                crossLineParams.setMargins(0, crossLineMarginTop, (int) marginRight, 0);
                crossLineImageView.setLayoutParams(crossLineParams);
            });
            valueAnimator.setDuration(DURATION_LINE_CROSS);
            valueAnimator.start();
        }
    }

    private static final long DURATION_SHIELD_ENLARGE_AND_ROTATE = 500;
    private static final int DURATION_SHIELD_COLLAPSE = 2000;
    private static final long DELAY_SHIELD_ROTATION = DURATION_SHIELD_ENLARGE_AND_ROTATE / 4;
    private static final int DELAY_CROSS_LINE_START = 350;

    private static final float SCALE_FACTOR_ENLARGE_TO = 1.6f;
    private static final float RATIO_SCALE_ENLARGE_BEFORE_COLLAPSE = 1.8f;
    private static final int ANGLE_SHIELD_ROTATE = 360;

    private static final float RATIO_SHIELD_END_POSITION = 7f / 12;

    private CrossLineLayout crossLineLayout;
    private AppCompatImageView shieldImageView;

    private int shieldStartY;
    private int shieldEndY;

    private OnAnimationFinishListener onAnimationFinishListener;

    public void setOnAnimationFinishListener(OnAnimationFinishListener onAnimationFinishListener) {
        this.onAnimationFinishListener = onAnimationFinishListener;
    }

    public AnimatedShield(Context context) {
        super(context);
        init();
    }

    public AnimatedShield(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimatedShield(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void enlargeAndRotateAnimation() {
        enlargeAnimatorOperation();
    }

    private void startCollapseAnimation() {
        PropertyValuesHolder shieldEnlargeScaleX = PropertyValuesHolder.ofFloat("scaleX", SCALE_FACTOR_ENLARGE_TO, RATIO_SCALE_ENLARGE_BEFORE_COLLAPSE);
        PropertyValuesHolder shieldEnlargeScaleY = PropertyValuesHolder.ofFloat("scaleY", SCALE_FACTOR_ENLARGE_TO, RATIO_SCALE_ENLARGE_BEFORE_COLLAPSE);
        final ObjectAnimator shieldEnlargeAnimator = ObjectAnimator.ofPropertyValuesHolder(this, shieldEnlargeScaleX, shieldEnlargeScaleY);
        shieldEnlargeAnimator.setDuration(DURATION_SHIELD_COLLAPSE * 2 / 3);

        int toX = -getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_big_icon_left);
        int toY = -getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_item_height_small);

        int fromX = (int) this.getX();
        int fromY = (int) this.getY();
        PropertyValuesHolder shieldShrinkScaleX = PropertyValuesHolder.ofFloat("scaleX", RATIO_SCALE_ENLARGE_BEFORE_COLLAPSE, 0.3f);
        PropertyValuesHolder shieldShrinkScaleY = PropertyValuesHolder.ofFloat("scaleY", RATIO_SCALE_ENLARGE_BEFORE_COLLAPSE, 0.3f);
        PropertyValuesHolder shieldY = PropertyValuesHolder.ofFloat("y", fromY, toY);
        ObjectAnimator shieldShrinkAnimator = ObjectAnimator.ofPropertyValuesHolder(this, shieldShrinkScaleX, shieldShrinkScaleY, shieldY);
        ObjectAnimator shieldXAnimator = ObjectAnimator.ofFloat(this, "x", fromX, toX);
        shieldXAnimator.setInterpolator(new AccelerateInterpolator());

        AnimatorSet shieldTranslationAnimatorSet = new AnimatorSet();
        shieldTranslationAnimatorSet.setDuration(DURATION_SHIELD_COLLAPSE / 3);
        shieldTranslationAnimatorSet.playTogether(shieldShrinkAnimator, shieldXAnimator);

        final AnimatorSet shieldAnimatorSet = new AnimatorSet();
        shieldAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                shieldImageView.setImageResource(R.drawable.notification_cleaner_shield_without_shadow);

                // AnimatedShied 和 AnimatedNotificationHeader move down 保持一致
                final int moveDown = getResources().getDimensionPixelSize(R.dimen.notification_cleaner_animated_move_down);
                final long moveDownDuration = 250;

                ObjectAnimator moveDownAnimator = ObjectAnimator.ofFloat(AnimatedShield.this,
                        "translationY", getTranslationY(), getTranslationY() + moveDown);

                moveDownAnimator.setDuration(moveDownDuration);
                moveDownAnimator.setStartDelay(200);
                moveDownAnimator.start();
            }
        });
        shieldAnimatorSet.playSequentially(shieldEnlargeAnimator, shieldTranslationAnimatorSet);
        shieldAnimatorSet.start();
    }

    private void init() {
        initShield();
        initCrossLine();
        initViewTree();
    }

    private void initCrossLine() {
        crossLineLayout = new CrossLineLayout(getContext());
        this.addView(crossLineLayout);
    }

    private void initViewTree() {
        ViewTreeObserver vto = this.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                shieldStartY = (int) getY();
                shieldEndY = (int) (getHeight() * RATIO_SHIELD_END_POSITION);

                AnimatedShield.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void initShield() {
        LayoutParams imageParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        shieldImageView = new AppCompatImageView(getContext());
        shieldImageView.setLayoutParams(imageParams);
        shieldImageView.setImageResource(R.drawable.notification_cleaner_shield_with_shadow);
        shieldImageView.setVisibility(ViewGroup.INVISIBLE);
        this.addView(shieldImageView);
    }

    private void enlargeAnimatorOperation() {
        AnimatorSet shieldAnimator = new AnimatorSet();

        // step1: double shield's size with animation
        PropertyValuesHolder xScaleShield = PropertyValuesHolder.ofFloat("scaleX", 0, SCALE_FACTOR_ENLARGE_TO);
        PropertyValuesHolder yScaleShield = PropertyValuesHolder.ofFloat("scaleY", 0, SCALE_FACTOR_ENLARGE_TO);
        PropertyValuesHolder yTranslationShield = PropertyValuesHolder.ofFloat("y", shieldStartY, shieldEndY);

        ObjectAnimator scaleAndTranslateAnimator = ObjectAnimator.ofPropertyValuesHolder(this, xScaleShield, yScaleShield, yTranslationShield)
                .setDuration(DURATION_SHIELD_ENLARGE_AND_ROTATE);

        // step2: enlarge and rotate
        ObjectAnimator rotationYShieldAnimator = ObjectAnimator.ofFloat(this, "rotationY", 0, ANGLE_SHIELD_ROTATE)
                .setDuration(DURATION_SHIELD_ENLARGE_AND_ROTATE - DELAY_SHIELD_ROTATION);
        rotationYShieldAnimator.setInterpolator(new DecelerateInterpolator());
        rotationYShieldAnimator.setStartDelay(DELAY_SHIELD_ROTATION);

        shieldAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                shieldImageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                crossLineLayout.postDelayed(() -> crossLineLayout.startCrossLineAnimation(), DELAY_CROSS_LINE_START);
            }
        });
        shieldAnimator.playTogether(scaleAndTranslateAnimator, rotationYShieldAnimator);
        shieldAnimator.start();
    }
}
