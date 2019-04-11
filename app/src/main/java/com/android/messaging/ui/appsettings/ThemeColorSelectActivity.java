package com.android.messaging.ui.appsettings;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.ui.ConversationDrawables;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.UiUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class ThemeColorSelectActivity extends BaseActivity {

    private static int[] COLOR_IMG_IDS = new int[]{
            R.id.iv_theme_select_1,
            R.id.iv_theme_select_2,
            R.id.iv_theme_select_3,
            R.id.iv_theme_select_4,
            R.id.iv_theme_select_5,
            R.id.iv_theme_select_6,
            R.id.iv_theme_select_7,
            R.id.iv_theme_select_8,
            R.id.iv_theme_select_9,
            R.id.iv_theme_select_10,
            R.id.iv_theme_select_11,
            R.id.iv_theme_select_12
    };

    public static int[] COLORS = new int[]{
            0xff37a63b,
            0xff338ee4,
            0xff0098a6,
            0xffd74315,
            0xff744fdc,
            0xffe54da7,
            0xffba43d4,
            0xffc62827,
            0xfff0ce0f,
            0xff315eaf,
            0xff87c932,
            0xffe8b437
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting_theme_select);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.setTitleBarBackground(toolbar, this);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.menu_theme_color));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // setup color select
        for (int i = 0; i < COLORS.length; i++) {
            findViewById(COLOR_IMG_IDS[i]).setBackground(
                    BackgroundDrawables.createBackgroundDrawable(COLORS[i], Dimensions.pxFromDp(31), true));
            final int k = i;
            findViewById(COLOR_IMG_IDS[i]).setOnClickListener(v -> {

                PrimaryColors.changePrimaryColor(COLORS[k]);

                refreshSelectStatus();

                // clear media caches
                Factory.get().reclaimMemory();
                // update drawable color cache
                ConversationDrawables.get().updateDrawables();
                // notify main page recreate
                HSGlobalNotificationCenter.sendNotification(ConversationListActivity.EVENT_MAINPAGE_RECREATE);

                BugleAnalytics.logEvent("Customize_ThemeColor_Change", true, "color", String.valueOf(getSelectedIndex()));
                UiUtils.setTitleBarBackground(toolbar, this);

            });
        }

        // initial refresh
        refreshSelectStatus();

        BugleAnalytics.logEvent("Customize_ThemeColor_Show", true);
    }


    private void refreshSelectStatus() {
        int primaryColor = PrimaryColors.getPrimaryColor();
        for (int i = 0; i < COLORS.length; i++) {
            if (primaryColor == COLORS[i]) {
                ((ImageView) findViewById(COLOR_IMG_IDS[i])).setImageResource(R.drawable.ic_theme_color_selected);
            } else {
                ((ImageView) findViewById(COLOR_IMG_IDS[i])).setImageResource(android.R.color.transparent);
            }
        }
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

    public static int getSelectedIndex() {
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == PrimaryColors.getPrimaryColor()) {
                return i;
            }
        }
        return -1;
    }
}
