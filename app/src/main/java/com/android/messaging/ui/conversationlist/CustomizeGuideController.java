package com.android.messaging.ui.conversationlist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.CommonUtils;
import com.android.messaging.util.UiUtils;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import static com.android.messaging.ui.conversationlist.ConversationListActivity.PREF_KEY_MAIN_DRAWER_OPENED;

class CustomizeGuideController implements CustomizeGuide {

    private ConversationListActivity mHost;

    private ObjectAnimator mDismissXAnim;
    private ObjectAnimator mDismissYAnim;
    private ObjectAnimator mDismissAlphaAnim;
    private ObjectAnimator mBackgroundDismissAlphaAnim;
    private ObjectAnimator mMenuFocusDismissAlphaAnim;
    private static final String PREF_KEY_SHOULD_SHOW_CUSTOMIZE_GUIDE = "pref_show_customize_guide";

    @SuppressLint("ClickableViewAccessibility")
    public void showGuideIfNeed(ConversationListActivity activity) {
        mHost = activity;
        if (!Preferences.getDefault().getBoolean(PREF_KEY_SHOULD_SHOW_CUSTOMIZE_GUIDE, true)) {
            return;
        }
        if (!CommonUtils.isNewUser()) {
            return;
        }
        if (Preferences.getDefault().getBoolean(PREF_KEY_MAIN_DRAWER_OPENED, false)) {
            return;
        }

        @SuppressLint("InflateParams") View customizeGuideView = LayoutInflater.from(activity).inflate(R.layout.customize_guide_layout, null, false);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        FrameLayout transparentBackground = customizeGuideView.findViewById(R.id.black_background_transparent);
        TextView guideButton = customizeGuideView.findViewById(R.id.customize_guide_confirm_button);
        guideButton.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, UiUtils.getColorDark(Color.WHITE), Dimensions.pxFromDp(1.3f),
                activity.getApplicationContext().getResources().getColor(R.color.customize_guide_button_stroke_color), Dimensions.pxFromDp(19.8f),
                false, true));

        ConstraintLayout guideContainer = customizeGuideView.findViewById(R.id.customize_guide_container);
        FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) guideContainer.getLayoutParams();
        int actionBarHeight = 0;
        if (activity.getSupportActionBar() != null) {
            actionBarHeight = activity.getSupportActionBar().getHeight();
        }
        param.topMargin = actionBarHeight + Dimensions.getStatusBarHeight(activity) + Dimensions.pxFromDp(25);
        guideContainer.setLayoutParams(param);
        guideContainer.setAlpha(0);

        ImageView guideMenuFocus = customizeGuideView.findViewById(R.id.customize_guide_menu_focus);
        FrameLayout.LayoutParams guideMenuFocusParam = (FrameLayout.LayoutParams) guideMenuFocus.getLayoutParams();
        guideMenuFocusParam.topMargin = (int) (Dimensions.getStatusBarHeight(activity) + (actionBarHeight - Dimensions.pxFromDp(43.3f)) * 0.5);
        guideMenuFocus.setLayoutParams(guideMenuFocusParam);
        guideMenuFocus.setScaleX(16.53f);
        guideMenuFocus.setScaleY(16.53f);

        activity.addContentView(customizeGuideView, params);
        Preferences.getDefault().putBoolean(PREF_KEY_SHOULD_SHOW_CUSTOMIZE_GUIDE, false);
        BugleAnalytics.logEvent("SMS_MenuGuide_Show", true);


        //Background disappear animation
        ObjectAnimator backgroundAlphaAnimator = ObjectAnimator.ofFloat(transparentBackground, "alpha", 0, 1);
        backgroundAlphaAnimator.setDuration(240);

        Interpolator backgroundInterpolator = PathInterpolatorCompat.create(0.32f, -0.66f, 0.6f, 1);

        ObjectAnimator menuFocusShrinkXAnimator = ObjectAnimator.ofFloat(guideMenuFocus, "scaleX", 16.53f, 0.75f);
        menuFocusShrinkXAnimator.setDuration(240);
        menuFocusShrinkXAnimator.setInterpolator(backgroundInterpolator);

        ObjectAnimator menuFocusShrinkYAnimator = ObjectAnimator.ofFloat(guideMenuFocus, "scaleY", 16.53f, 0.75f);
        menuFocusShrinkYAnimator.setDuration(240);
        menuFocusShrinkYAnimator.setInterpolator(backgroundInterpolator);

        ObjectAnimator menuFocusEnlargeXAnimator = ObjectAnimator.ofFloat(guideMenuFocus, "scaleX", 0.75f, 1);
        menuFocusEnlargeXAnimator.setDuration(120);

        ObjectAnimator menuFocusEnlargeYAnimator = ObjectAnimator.ofFloat(guideMenuFocus, "scaleY", 0.75f, 1);
        menuFocusEnlargeYAnimator.setDuration(120);

        menuFocusShrinkXAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                menuFocusEnlargeXAnimator.start();
                menuFocusEnlargeYAnimator.start();
            }
        });

        //Dialog appear animation
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(guideContainer, "alpha", 0, 1);
        alphaAnimator.setDuration(240);
        alphaAnimator.setStartDelay(440);

        guideContainer.setPivotX(Dimensions.pxFromDp(0));
        guideContainer.setPivotY(Dimensions.pxFromDp(0));

        Interpolator dialogInterpolator = PathInterpolatorCompat.create(0.32f, 0.66f, 0.6f, 1);

        ObjectAnimator enlargeXAnimator = ObjectAnimator.ofFloat(guideContainer, "scaleX", 0.5f, 1.01f);
        enlargeXAnimator.setDuration(240);
        enlargeXAnimator.setInterpolator(dialogInterpolator);
        enlargeXAnimator.setStartDelay(440);

        ObjectAnimator enlargeYAnimator = ObjectAnimator.ofFloat(guideContainer, "scaleY", 0.5f, 1.01f);
        enlargeYAnimator.setDuration(240);
        enlargeYAnimator.setInterpolator(dialogInterpolator);
        enlargeYAnimator.setStartDelay(440);

        ObjectAnimator shrinkXAnimator = ObjectAnimator.ofFloat(guideContainer, "scaleX", 1.01f, 1);
        shrinkXAnimator.setDuration(120);

        ObjectAnimator shrinkYAnimator = ObjectAnimator.ofFloat(guideContainer, "scaleY", 1.01f, 1);
        shrinkYAnimator.setDuration(120);

        enlargeXAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                shrinkXAnimator.start();
                shrinkYAnimator.start();
            }
        });

        //Dialog dismiss animation
        mDismissXAnim = ObjectAnimator.ofFloat(transparentBackground, "scaleX", 1, 0.4f);
        mDismissXAnim.setDuration(200);
        mDismissYAnim = ObjectAnimator.ofFloat(guideContainer, "scaleY", 1, 0.4f);
        mDismissYAnim.setDuration(200);
        mDismissAlphaAnim = ObjectAnimator.ofFloat(guideContainer, "alpha", 1, 0);
        mDismissAlphaAnim.setDuration(80);
        mDismissAlphaAnim.setStartDelay(80);

        mDismissAlphaAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                customizeGuideView.setVisibility(View.GONE);
            }
        });

        //Background dismiss animation
        mBackgroundDismissAlphaAnim = ObjectAnimator.ofFloat(transparentBackground, "alpha", 1, 0);
        mBackgroundDismissAlphaAnim.setDuration(120);
        mBackgroundDismissAlphaAnim.setStartDelay(40);
        mMenuFocusDismissAlphaAnim = ObjectAnimator.ofFloat(guideMenuFocus, "alpha", 1, 0);
        mMenuFocusDismissAlphaAnim.setDuration(120);
        mMenuFocusDismissAlphaAnim.setStartDelay(40);

        guideButton.setOnClickListener(v -> {
            closeCustomizeGuide(true);
        });

        guideContainer.setOnClickListener(v -> {
            if (enlargeXAnimator.isRunning() || shrinkXAnimator.isRunning()) {
                customizeGuideView.setVisibility(View.GONE);
                enlargeXAnimator.cancel();
                enlargeYAnimator.cancel();
                shrinkXAnimator.cancel();
                shrinkYAnimator.cancel();
                alphaAnimator.cancel();
            } else {
                closeCustomizeGuide(true);
            }
        });

        customizeGuideView.setOnTouchListener((v, event) -> {
            if (enlargeXAnimator.isRunning() || shrinkXAnimator.isRunning()) {
                customizeGuideView.setVisibility(View.GONE);
                enlargeXAnimator.cancel();
                enlargeYAnimator.cancel();
                shrinkXAnimator.cancel();
                shrinkYAnimator.cancel();
                alphaAnimator.cancel();
                return false;
            }
            closeCustomizeGuide(false);
            return false;
        });

        backgroundAlphaAnimator.start();
        menuFocusShrinkXAnimator.start();
        menuFocusShrinkYAnimator.start();
        alphaAnimator.start();
        enlargeXAnimator.start();
        enlargeYAnimator.start();
        logGuideShow();
    }


    @Override
    public void logGuideShow() {
        NavigationViewGuideTest.logGuideShow();
        BugleAnalytics.logEvent("Menu_Guide_Show");
    }

    @Override
    public boolean closeCustomizeGuide(boolean openDrawer) {
        if (mDismissXAnim == null) {
            return false;
        }

        if (mDismissXAnim.isRunning()) {
            return false;
        }

        mDismissXAnim.start();
        mDismissYAnim.start();
        mDismissAlphaAnim.start();
        mBackgroundDismissAlphaAnim.start();
        mMenuFocusDismissAlphaAnim.start();

        if (openDrawer) {
            mDismissAlphaAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mHost.openDrawer();
                    BugleAnalytics.logEvent("Menu_Show_AfterGuide");
                }
            });
        }
        return true;
    }
}
