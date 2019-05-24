package com.android.messaging.ui.customize.theme;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.BaseBugleFragmentActivity;
import com.android.messaging.util.BugleActivityUtil;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.UiUtils;
import com.superapps.util.Toasts;

public class ChooseThemeActivity extends BaseBugleFragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugleActivityUtil.adaptScreen4VerticalSlide(this, 360);
        setContentView(R.layout.activity_choose_theme);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.menu_theme));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ChooseThemePagerView pagerView = findViewById(R.id.pager_view);

        pagerView.setOnPageSelectedListener((position, themeColor) ->
                UiUtils.setTitleBarBackground(toolbar, ChooseThemeActivity.this, themeColor));
        pagerView.setOnApplyClickListener(v -> {
            Toasts.showToast(R.string.apply_theme_success);
            finish();
        });

        BugleAnalytics.logEvent("Customize_Theme_Show", true, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        BugleActivityUtil.cancelAdaptScreen(this);
    }

    @Override
    public void finish() {
        super.finish();
        BugleActivityUtil.cancelAdaptScreen(this);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
