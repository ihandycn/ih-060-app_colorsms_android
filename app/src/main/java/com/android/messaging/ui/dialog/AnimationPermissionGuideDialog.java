package com.android.messaging.ui.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.welcome.PermissionGuideView;
import com.android.messaging.util.AnimationUtils;
import com.android.messaging.util.ViewUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Compats;
import com.superapps.util.Dimensions;

public abstract class AnimationPermissionGuideDialog extends BasePermissionGuideDialog {

    private static final long DURATION_CLICK_APPEAR = 220;
    private static final long DURATION_CLICK_APPEAR_HUAWEI = 300;
    private static final long DURATION_CLICK_DISAPPEAR = 300;
    private static final long DURATION_FIRST_OPEN = 470;

    private static final float TRANSLATE_APPEAR_DISTANCE_X = 195.0f;
    private static final float TRANSLATE_APPEAR_DISTANCE_Y = 152.0f;

    private static final float TRANSLATE_DISAPPEAR_DISTANCE_X = 168.0f;
    private static final float TRANSLATE_DISAPPEAR_DISTANCE_Y = 106.0f;

    private static final float SCALE_PIVOT_X_VALUE = 1.025f;
    private static final float SCALE_PIVOT_Y_VALUE = 0.52f;
    private static final long DELAY_DISAPPEAR = 70;

    private OvershootInterpolator mOvershootInterpolator = new OvershootInterpolator();
    private OvershootInterpolator mClickOvershootInterpolator = new OvershootInterpolator(1.1f);
    private AccelerateInterpolator mAccelerateInterpolator = new AccelerateInterpolator();
    private Interpolator mDefaultInterpolator;

    private PermissionGuideView mPermissionGuideView;
    private View mPermissionGuideContentView;
    private View mPermissionGuideBaseView;

    private boolean mIsDismissAnimating;
    private boolean mIsHuaWeiDevice;
    private float mDensity;

    protected Context mContext;

    protected enum AnimationType {
        SingleLayer,
        DoubleLayer,
    }

    protected abstract String getTitle();

    protected abstract String getContent();

    protected abstract String getDescription();

    protected abstract void onActionButtonClick(View v);

    protected abstract void onBackClick();

    protected abstract AnimationType getAnimationType();

    protected boolean isShowConfirmDialog() {
        return false;
    }

    public AnimationPermissionGuideDialog(final Context context) {
        this (context, null);
    }

    public AnimationPermissionGuideDialog (final Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimationPermissionGuideDialog(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        View.inflate(context, R.layout.permission_guide_animation, this);
        mPermissionGuideView = ViewUtils.findViewById(this, R.id.permission_guide_view);
        // init content
        mPermissionGuideView.setFingerView(ViewUtils.findViewById(this, R.id.finger_iv));
        mPermissionGuideView.setTitleText(getTitle());
        mPermissionGuideView.setContentText(getContent());
        mPermissionGuideView.setLastTitleText(getTitle());
        mPermissionGuideView.setShowConfirmDialog(isShowConfirmDialog());

        TextView descriptionTv = ViewUtils.findViewById(this, R.id.permission_guide_description_tv);
        descriptionTv.setText(getDescription());

        mIsHuaWeiDevice = Compats.IS_HUAWEI_DEVICE;

        this.mDensity = getResources().getDisplayMetrics().density;
        mDefaultInterpolator = PathInterpolatorCompat.create(0.25f, 0.1f, 0.25f, 1f);

        mPermissionGuideContentView = ViewUtils.findViewById(this, R.id.permission_guide_container);
        mPermissionGuideBaseView = ViewUtils.findViewById(this, R.id.permission_guide_base_rl);
        ViewUtils.findViewById(this, R.id.got_it_tv).setBackground(BackgroundDrawables.
                createBackgroundDrawable(context.getResources().getColor(R.color.primary_color),
                        Dimensions.pxFromDp(6), true));
        ViewUtils.findViewById(this, R.id.got_it_tv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionButtonClick(v);
                dismissGuideDialog();
            }
        });
    }

    @Override
    public void onAddedToWindow() {
        if (mIsShowImmediately) {
            setVisibility(View.VISIBLE);
            mPermissionGuideContentView.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        public boolean onPreDraw() {
                            mPermissionGuideContentView.getViewTreeObserver().removeOnPreDrawListener(this);

                            ValueAnimator translateAnimator = ValueAnimator.ofFloat(1.0f, 0f);
                            translateAnimator.setDuration(DURATION_CLICK_APPEAR);
                            translateAnimator.setInterpolator(mDefaultInterpolator);
                            translateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    float floatValue = (float) animation.getAnimatedValue();
                                    mPermissionGuideContentView.setTranslationX(TRANSLATE_APPEAR_DISTANCE_X * AnimationPermissionGuideDialog.this.mDensity * floatValue);
                                    mPermissionGuideContentView.setTranslationY(TRANSLATE_APPEAR_DISTANCE_Y * AnimationPermissionGuideDialog.this.mDensity * floatValue);
                                    mPermissionGuideContentView.setAlpha(1 - floatValue);
                                }
                            });

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                Animator circularReveal = ViewAnimationUtils.createCircularReveal(mPermissionGuideContentView, mPermissionGuideContentView.getWidth() / 2 + Dimensions.pxFromDp(20),
                                        mPermissionGuideContentView.getHeight() / 2, 0, mPermissionGuideContentView.getWidth() / 2);
                                circularReveal.setDuration(DURATION_CLICK_APPEAR);
                                circularReveal.setInterpolator(mAccelerateInterpolator);

                                AnimatorSet animatorSet = new AnimatorSet();
                                animatorSet.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                        super.onAnimationStart(animation);
                                        mPermissionGuideContentView.setVisibility(View.VISIBLE);
                                        mPermissionGuideContentView.setAlpha(1.0f);
                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        if (null != mPermissionGuideView) {
                                            if (getAnimationType() == AnimationType.SingleLayer) {
                                                mPermissionGuideView.startSingleLayerAnimation(true);
                                            } else {
                                                mPermissionGuideView.startDoubleLayerAnim(true);

                                            }
                                        }
                                        mPermissionGuideContentView.setTranslationX(0);
                                        mPermissionGuideContentView.setTranslationY(0);
                                    }
                                });
                                animatorSet.playTogether(translateAnimator, circularReveal);
                                animatorSet.start();
                            } else {
                                ScaleAnimation scaleAnimation = new ScaleAnimation(0f, 1.0f, 0f, 1.0f,
                                        Animation.RELATIVE_TO_SELF, SCALE_PIVOT_X_VALUE, Animation.RELATIVE_TO_SELF, SCALE_PIVOT_Y_VALUE);
                                scaleAnimation.setDuration(mIsHuaWeiDevice ? DURATION_CLICK_APPEAR_HUAWEI : DURATION_CLICK_APPEAR);
                                scaleAnimation.setInterpolator(mIsHuaWeiDevice ? mDefaultInterpolator : mClickOvershootInterpolator);
                                scaleAnimation.setAnimationListener(new AnimationUtils.AnimationListenerAdapter() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                        super.onAnimationStart(animation);
                                        mPermissionGuideContentView.setVisibility(View.VISIBLE);
                                        mPermissionGuideContentView.setAlpha(1.0f);
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        super.onAnimationEnd(animation);
                                        if (null != mPermissionGuideView) {
                                            if (getAnimationType() == AnimationType.SingleLayer) {
                                                mPermissionGuideView.startSingleLayerAnimation(true);
                                            } else {
                                                mPermissionGuideView.startDoubleLayerAnim(true);

                                            }
                                        }
                                    }
                                });
                                mPermissionGuideContentView.startAnimation(scaleAnimation);
                            }
                            return true;
                        }
                    });
            startBgAnimation(0, 194, DURATION_CLICK_APPEAR);
        } else {
            // First Open
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    setVisibility(View.VISIBLE);
                    startBgAnimation(0, 194, DURATION_FIRST_OPEN);
                    if (null != mPermissionGuideContentView) {
                        ScaleAnimation scaleAnimation = new ScaleAnimation(0.69f, 1.0f, 0.87f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        scaleAnimation.setDuration(DURATION_FIRST_OPEN);
                        scaleAnimation.setInterpolator(mOvershootInterpolator);

                        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
                        alphaAnimation.setDuration(DURATION_FIRST_OPEN);
                        alphaAnimation.setInterpolator(mOvershootInterpolator);

                        AnimationSet animationSet = new AnimationSet(false);
                        animationSet.addAnimation(scaleAnimation);
                        animationSet.addAnimation(alphaAnimation);
                        animationSet.setAnimationListener(new AnimationUtils.AnimationListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                super.onAnimationStart(animation);
                                mPermissionGuideContentView.setVisibility(View.VISIBLE);
                                mPermissionGuideContentView.setAlpha(1.0f);
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                super.onAnimationEnd(animation);
                                if (null != mPermissionGuideView) {
                                    if (getAnimationType() == AnimationType.SingleLayer) {
                                        mPermissionGuideView.startSingleLayerAnimation(true);
                                    } else {
                                        mPermissionGuideView.startDoubleLayerAnim(true);

                                    }
                                }
                            }
                        });
                        mPermissionGuideContentView.startAnimation(animationSet);
                    }
                }
            }, mIsHuaWeiDevice ? ESTIMATED_ACTIVITY_SWITCH_TIME - 50 : ESTIMATED_ACTIVITY_SWITCH_TIME - 400);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            onBackClick();
            dismissGuideDialog();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    private void dismissGuideDialog() {
        if (mIsDismissAnimating) {
            return;
        }
        mIsDismissAnimating = true;

        mPermissionGuideView.onDismiss();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ValueAnimator translateAnimator = ValueAnimator.ofFloat(0f, 1.0f);
            translateAnimator.setDuration(DURATION_CLICK_DISAPPEAR);
            translateAnimator.setInterpolator(mDefaultInterpolator);
            translateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float floatValue = (float) animation.getAnimatedValue();
                    mPermissionGuideContentView.setTranslationX(TRANSLATE_DISAPPEAR_DISTANCE_X * AnimationPermissionGuideDialog.this.mDensity * floatValue);
                    mPermissionGuideContentView.setTranslationY(TRANSLATE_DISAPPEAR_DISTANCE_Y * AnimationPermissionGuideDialog.this.mDensity * floatValue);
                    mPermissionGuideContentView.setAlpha(1 - floatValue);
                }
            });

            int viewWidth = mPermissionGuideContentView.getWidth();
            int viewHeight = mPermissionGuideContentView.getHeight();
            int startRadius = (int) Math.sqrt(viewWidth * viewWidth / 4 + viewHeight * viewHeight / 4) + Dimensions.pxFromDp(10);
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(mPermissionGuideContentView, viewWidth / 2,
                    viewHeight / 2, startRadius, 0);
            circularReveal.setDuration(DURATION_CLICK_DISAPPEAR);
            circularReveal.setInterpolator(mDefaultInterpolator);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mPermissionGuideContentView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    onDisappearAnimationEnd();
                    mPermissionGuideContentView.setTranslationX(0);
                    mPermissionGuideContentView.setTranslationY(0);
                }
            });
            animatorSet.playTogether(translateAnimator, circularReveal);
            animatorSet.start();
        } else {
            TranslateAnimation translateAnimation = new TranslateAnimation(0f, TRANSLATE_DISAPPEAR_DISTANCE_X * this.mDensity, 0, TRANSLATE_DISAPPEAR_DISTANCE_Y * this.mDensity);
            translateAnimation.setDuration(DURATION_CLICK_DISAPPEAR);
            translateAnimation.setInterpolator(mAccelerateInterpolator);

            ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0f, 1.0f, 0f,
                    Animation.RELATIVE_TO_SELF, SCALE_PIVOT_X_VALUE, Animation.RELATIVE_TO_SELF, SCALE_PIVOT_Y_VALUE);
            scaleAnimation.setDuration(DURATION_CLICK_DISAPPEAR - DELAY_DISAPPEAR);
            scaleAnimation.setStartOffset(DELAY_DISAPPEAR);
            scaleAnimation.setInterpolator(mAccelerateInterpolator);

            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0f);
            alphaAnimation.setDuration(DURATION_CLICK_DISAPPEAR - DELAY_DISAPPEAR);
            alphaAnimation.setStartOffset(DELAY_DISAPPEAR);
            alphaAnimation.setInterpolator(mAccelerateInterpolator);

            AnimationSet animationSet = new AnimationSet(false);
            animationSet.addAnimation(translateAnimation);
            animationSet.addAnimation(scaleAnimation);
            animationSet.addAnimation(alphaAnimation);
            animationSet.setAnimationListener(new AnimationUtils.AnimationListenerAdapter() {
                @Override
                public void onAnimationStart(Animation animation) {
                    super.onAnimationStart(animation);
                    mPermissionGuideContentView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);
                    onDisappearAnimationEnd();
                }
            });
            mPermissionGuideContentView.startAnimation(animationSet);
        }

        startBgAnimation(194, 0, DURATION_CLICK_DISAPPEAR);
    }

    private void startBgAnimation(int startAlpha, int endAlpha, long duration) {
        ValueAnimator bgColorAnimator = ValueAnimator.ofInt(startAlpha, endAlpha);
        bgColorAnimator.setDuration(duration);
        bgColorAnimator.setInterpolator(mDefaultInterpolator);
        bgColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int alpha = (int) animation.getAnimatedValue();
                mPermissionGuideBaseView.setBackgroundColor(ColorUtils.setAlphaComponent(0xFF000000, alpha));
            }
        });
        bgColorAnimator.start();
    }

    private void onDisappearAnimationEnd() {
        mPermissionGuideContentView.setVisibility(View.INVISIBLE);
        postDelayed(new Runnable() {
            @Override
            public void run() {
//                PermissionGuideManager.getInstance().removePermissionGuide(true);
                mIsDismissAnimating = false;
            }
        }, 25);
    }
}
