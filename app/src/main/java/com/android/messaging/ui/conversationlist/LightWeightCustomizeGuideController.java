package com.android.messaging.ui.conversationlist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.R;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.CommonUtils;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;

import static com.android.messaging.ui.conversationlist.ConversationListActivity.PREF_KEY_MAIN_DRAWER_OPENED;

public class LightWeightCustomizeGuideController implements CustomizeGuide {
    private static final String PREF_KEY_SHOULD_SHOW_CUSTOMIZE_GUIDE = "pref_show_customize_guide";

    @SuppressLint("ClickableViewAccessibility")
    public void showGuideIfNeed(ConversationListActivity activity) {
        if (!Preferences.getDefault().getBoolean(PREF_KEY_SHOULD_SHOW_CUSTOMIZE_GUIDE, true)) {
            return;
        }
        if (!CommonUtils.isNewUser()) {
            return;
        }
        if (Preferences.getDefault().getBoolean(PREF_KEY_MAIN_DRAWER_OPENED, false)) {
            return;
        }

        @SuppressLint("InflateParams") View customizeGuideView = LayoutInflater.from(activity).inflate(R.layout.light_weight_customize_guide_layout, null, false);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        FrameLayout guideContainer = customizeGuideView.findViewById(R.id.customize_guide_container);
        FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) guideContainer.getLayoutParams();
        param.setMarginStart(Dimensions.pxFromDp(31 - 12.7f));
        int actionBarHeight = 0;
        if (activity.getSupportActionBar() != null) {
            actionBarHeight = activity.getSupportActionBar().getHeight();
        }
        param.topMargin = actionBarHeight + Dimensions.getStatusBarHeight(activity) - Dimensions.pxFromDp(25);
        guideContainer.setLayoutParams(param);
        guideContainer.setAlpha(0);

        activity.addContentView(customizeGuideView, params);
        Preferences.getDefault().putBoolean(PREF_KEY_SHOULD_SHOW_CUSTOMIZE_GUIDE, false);
        BugleAnalytics.logEvent("SMS_MenuGuide_Show", true);

        LottieAnimationView guideLottie = customizeGuideView.findViewById(R.id.customize_guide_lottie);

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(guideContainer, "alpha", 0, 1);
        alphaAnimator.setDuration(120);

        guideContainer.setPivotX(Dimensions.pxFromDp(12.7f));
        guideContainer.setPivotY(Dimensions.pxFromDp(12.7f));

        Interpolator interpolator = PathInterpolatorCompat.create(0.17f, 0.12f, 0.67f, 1);

        ObjectAnimator enlargeXAnimator = ObjectAnimator.ofFloat(guideContainer, "scaleX", 0.5f, 1.1f);
        enlargeXAnimator.setDuration(200);
        enlargeXAnimator.setInterpolator(interpolator);

        ObjectAnimator enlargeYAnimator = ObjectAnimator.ofFloat(guideContainer, "scaleY", 0.5f, 1.1f);
        enlargeYAnimator.setDuration(200);
        enlargeYAnimator.setInterpolator(interpolator);

        ObjectAnimator shrinkXAnimator = ObjectAnimator.ofFloat(guideContainer, "scaleX", 1.1f, 1);
        shrinkXAnimator.setDuration(120);
        ObjectAnimator shrinkYAnimator = ObjectAnimator.ofFloat(guideContainer, "scaleY", 1.1f, 1);
        shrinkYAnimator.setDuration(120);

        enlargeXAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                shrinkXAnimator.start();
                shrinkYAnimator.start();
            }
        });

        shrinkXAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                guideLottie.playAnimation();
            }
        });

        ObjectAnimator dismissEnlargeXAnimator = ObjectAnimator.ofFloat(guideContainer, "scaleX", 1, 1.1f);
        dismissEnlargeXAnimator.setDuration(80);
        dismissEnlargeXAnimator.setInterpolator(interpolator);
        ObjectAnimator dismissEnlargeYAnimator = ObjectAnimator.ofFloat(guideContainer, "scaleY", 1, 1.1f);
        dismissEnlargeYAnimator.setDuration(80);
        dismissEnlargeYAnimator.setInterpolator(interpolator);

        ObjectAnimator dismissShrinkXAnim = ObjectAnimator.ofFloat(guideContainer, "scaleX", 1.1f, 0.5f);
        dismissShrinkXAnim.setDuration(120);
        dismissShrinkXAnim.setStartDelay(80);
        ObjectAnimator dismissShrinkYAnim = ObjectAnimator.ofFloat(guideContainer, "scaleY", 1.1f, 0.5f);
        dismissShrinkYAnim.setDuration(120);
        dismissShrinkYAnim.setStartDelay(80);

        ObjectAnimator dismissAlphaAnim = ObjectAnimator.ofFloat(guideContainer, "alpha", 1, 0);
        dismissAlphaAnim.setDuration(80);
        dismissAlphaAnim.setStartDelay(120);

        dismissAlphaAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                customizeGuideView.setVisibility(View.GONE);
            }
        });

        customizeGuideView.findViewById(R.id.customize_guide_clickable_container).setOnClickListener(v -> {
            if (guideLottie.isAnimating()) {
                guideLottie.setProgress(1);
                guideLottie.cancelAnimation();
            }

            if (enlargeXAnimator.isRunning() || shrinkXAnimator.isRunning()) {
                customizeGuideView.setVisibility(View.GONE);
                enlargeXAnimator.cancel();
                enlargeYAnimator.cancel();
                shrinkXAnimator.cancel();
                shrinkYAnimator.cancel();
                alphaAnimator.cancel();
            } else {
                dismissEnlargeXAnimator.start();
                dismissEnlargeYAnimator.start();
                dismissShrinkXAnim.start();
                dismissShrinkYAnim.start();
                dismissAlphaAnim.start();
            }
        });

        customizeGuideView.setOnTouchListener((v, event) -> {
            if (guideLottie.isAnimating()) {
                guideLottie.setProgress(1);
                guideLottie.cancelAnimation();
            }

            if (enlargeXAnimator.isRunning() || shrinkXAnimator.isRunning()) {
                customizeGuideView.setVisibility(View.GONE);
                enlargeXAnimator.cancel();
                enlargeYAnimator.cancel();
                shrinkXAnimator.cancel();
                shrinkYAnimator.cancel();
                alphaAnimator.cancel();
                return false;
            }

            dismissEnlargeXAnimator.start();
            dismissEnlargeYAnimator.start();
            dismissShrinkXAnim.start();
            dismissShrinkYAnim.start();
            dismissAlphaAnim.start();
            return false;
        });

        alphaAnimator.start();
        enlargeXAnimator.start();
        enlargeYAnimator.start();

        logGuideShow();
    }


    @Override
    public void logGuideShow() {
        NavigationViewGuideTest.logGuideShow();
    }

    @Override
    public boolean closeCustomizeGuide(boolean openDrawer) {
        return false;
    }
}

