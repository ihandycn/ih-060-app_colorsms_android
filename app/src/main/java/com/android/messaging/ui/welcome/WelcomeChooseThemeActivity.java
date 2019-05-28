package com.android.messaging.ui.welcome;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.customize.theme.ChooseThemePagerView;
import com.android.messaging.ui.customize.theme.ThemeInfo;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.util.BugleActivityUtil;
import com.android.messaging.util.BugleAnalytics;
import com.superapps.util.Preferences;

public class WelcomeChooseThemeActivity extends AppCompatActivity {

    public static final String PREF_KEY_WELCOME_CHOOSE_THEME_SHOWN = "pref_key_welcome_choose_theme_shown";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugleActivityUtil.adaptScreen4VerticalSlide(this, 360);
        setContentView(R.layout.welcome_choose_theme_activity);

        ChooseThemePagerView chooseThemePagerView = findViewById(R.id.choose_theme_pager_view);
        chooseThemePagerView.setOnApplyClickListener((View v) -> {
            BugleActivityUtil.cancelAdaptScreen(this);
            UIIntents.get().launchConversationListActivity(WelcomeChooseThemeActivity.this);
            finish();
        });

        BugleAnalytics.logEvent("Start_ChooseTheme_Show", true, true);
        Preferences.getDefault().putBoolean(PREF_KEY_WELCOME_CHOOSE_THEME_SHOWN, true);
    }

    @Override
    public void onBackPressed() {
        BugleAnalytics.logEvent("Start_ChooseTheme_Back", true);
        BugleActivityUtil.cancelAdaptScreen(this);

        // apply default theme
        ThemeUtils.applyTheme(ThemeInfo.getThemeInfo(ThemeUtils.getCurrentThemeName()), 0);
        UIIntents.get().launchConversationListActivity(WelcomeChooseThemeActivity.this);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

