package com.android.messaging.ui.welcome;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.customize.theme.ChooseThemePagerView;
import com.android.messaging.ui.customize.theme.ThemeDownloadManager;
import com.android.messaging.ui.customize.theme.ThemeInfo;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.util.BugleActivityUtil;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleFirebaseAnalytics;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

public class WelcomeChooseThemeActivity extends AppCompatActivity {

    public static final String PREF_KEY_WELCOME_CHOOSE_THEME_SHOWN = "pref_key_welcome_choose_theme_shown";
    private boolean mIsThemeAppling;
    private INotificationObserver mObserver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugleActivityUtil.adaptScreen4VerticalSlide(this, 360);
        setContentView(R.layout.welcome_choose_theme_activity);

        TextView title = findViewById(R.id.title);

        Interpolator interpolator = PathInterpolatorCompat.create(0.17f, 0.17f, 0.6f, 1);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(title, "alpha", 1, 0);
        alphaAnimator.setDuration(240);
        ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(title, "translationY", 0, Dimensions.pxFromDp(34));
        translationAnimator.setDuration(320);
        translationAnimator.setInterpolator(interpolator);

        ChooseThemePagerView chooseThemePagerView = findViewById(R.id.choose_theme_pager_view);
        chooseThemePagerView.setOnApplyClickListener(themeInfo -> {
            if (mIsThemeAppling) {
                return;
            }
            mIsThemeAppling = true;
            String copyAndMoveKey = ThemeDownloadManager.getInstance().getPrefKeyByThemeName(themeInfo.mThemeKey);
            if (!Preferences.getDefault().getBoolean(copyAndMoveKey, false)) {
                mObserver = (s, hsBundle) -> {
                    if (s.equals(copyAndMoveKey)) {
                        Threads.postOnThreadPoolExecutor(() -> ThemeUtils.applyThemeFirstTime(themeInfo,
                                () -> Threads.postOnMainThread(() -> {
                                    chooseThemePagerView.setVisibility(View.GONE);
                                    alphaAnimator.start();
                                    translationAnimator.start();
                                    translationAnimator.addListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            BugleActivityUtil.cancelAdaptScreen(WelcomeChooseThemeActivity.this);
//                                          UIIntents.get().launchConversationListActivity(WelcomeChooseThemeActivity.this);
                                            Navigations.startActivitySafely(WelcomeChooseThemeActivity.this,
                                                    new Intent(WelcomeChooseThemeActivity.this, NotificationGuideActivity.class));
                                            overridePendingTransition(0, 0);
                                            finish();
                                        }
                                    });
                                })));
                    }
                };
                HSGlobalNotificationCenter.addObserver(copyAndMoveKey, mObserver);
            } else {
                Threads.postOnThreadPoolExecutor(() -> ThemeUtils.applyThemeFirstTime(themeInfo,
                        () -> Threads.postOnMainThread(() -> {
                            chooseThemePagerView.setVisibility(View.GONE);
                            alphaAnimator.start();
                            translationAnimator.start();
                            translationAnimator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    BugleActivityUtil.cancelAdaptScreen(WelcomeChooseThemeActivity.this);
//                                  UIIntents.get().launchConversationListActivity(WelcomeChooseThemeActivity.this);
                                    Navigations.startActivitySafely(WelcomeChooseThemeActivity.this,
                                            new Intent(WelcomeChooseThemeActivity.this, NotificationGuideActivity.class));
                                    overridePendingTransition(0, 0);
                                    finish();
                                }
                            });
                        })));
            }
            BugleAnalytics.logEvent("Start_ChooseTheme_Apply", true, "theme", themeInfo.mThemeKey);
            BugleFirebaseAnalytics.logEvent("Start_ChooseTheme_Apply", "theme", themeInfo.mThemeKey);
        });

        BugleAnalytics.logEvent("Start_ChooseTheme_Show", true);
        BugleFirebaseAnalytics.logEvent("Start_ChooseTheme_Show");
        Preferences.getDefault().putBoolean(PREF_KEY_WELCOME_CHOOSE_THEME_SHOWN, true);
    }

    @Override
    public void onBackPressed() {
        if (mIsThemeAppling) {
            return;
        }
        mIsThemeAppling = true;
        BugleAnalytics.logEvent("Start_ChooseTheme_Back", true);
        BugleActivityUtil.cancelAdaptScreen(this);

        // apply default theme
        ThemeUtils.applyThemeFirstTime(ThemeInfo.getThemeInfo(ThemeUtils.getCurrentThemeName()),
                () -> UIIntents.get().launchConversationListActivity(WelcomeChooseThemeActivity.this));
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mObserver != null) {
            HSGlobalNotificationCenter.removeObserver(mObserver);
        }
    }
}

