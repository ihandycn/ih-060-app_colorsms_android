package com.android.messaging.ui.customize.theme;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.BugleActivityUtil;
import com.android.messaging.util.UiUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.Navigations;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

public class ThemePreviewActivity extends HSAppCompatActivity {

    public static void startThemePreviewActivity(Context context, ThemeInfo themeInfo) {
        Intent intent = new Intent(context, ThemePreviewActivity.class);
        intent.putExtra("theme_info", themeInfo.mThemeKey);
        Navigations.startActivitySafely(context, intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugleActivityUtil.adaptScreen4VerticalSlide(this, 360);
        setContentView(R.layout.activity_theme_preview);

        UiUtils.setStatusBarColor(this, Color.WHITE);

        String themeKey = getIntent().getStringExtra("theme_info");
        ThemeInfo mThemeInfo = ThemeInfo.getThemeInfo(themeKey);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(mThemeInfo.name);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ThemePreviewPagerView pagerView = findViewById(R.id.pager_view);
        pagerView.setThemeInfo(mThemeInfo);

        pagerView.setOnApplyClickListener(v -> {
            Toasts.showToast(R.string.apply_theme_success);
            Threads.postOnMainThreadDelayed(()-> {
                UIIntents.get().launchConversationListActivity(this);
            finish();
            }, 500);
        });

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
