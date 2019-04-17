package com.android.messaging.ui.welcome;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.customize.theme.ChooseThemePagerView;
import com.android.messaging.util.BugleActivityUtil;
import com.android.messaging.util.BugleAnalytics;

public class WelcomeChooseThemeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugleActivityUtil.adaptScreen4VerticalSlide(this, 360);
        setContentView(R.layout.welcome_choose_theme_activity);

        ChooseThemePagerView chooseThemePagerView = findViewById(R.id.choose_theme_pager_view);
        chooseThemePagerView.setOnApplyClickListener((View v) -> {
            BugleActivityUtil.cancelAdaptScreen(this);
            UIIntents.get().launchConversationListActivity(WelcomeChooseThemeActivity.this);
            BugleAnalytics.logEvent("Start_ChooseTheme_Apply", true);
            finish();
        });

        BugleAnalytics.logEvent("Start_ChooseTheme_Show", true);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

