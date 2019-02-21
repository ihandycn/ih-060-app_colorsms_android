package com.android.messaging.ui.appsettings;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.customize.PrimaryColors;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class ThemeSelectActivity extends HSAppCompatActivity {

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

    private static int[] COLORS = new int[]{
            0xff1acc48,
            0xff0083fe,
            0xff16c7d3,
            0xffff7e2a,
            0xff7646ff,
            0xfff846c0,
            0xffd619ff,
            0xfff93b4b,
            0xfff5d20d,
            0xff000000,
            0xff81de09,
            0xfff6bd01
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting_theme_select);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.theme_select_title));
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

                // notify main page recreate
                HSGlobalNotificationCenter.sendNotification(ConversationListActivity.EVENT_MAINPAGE_RECREATE);
            });
        }

        // initial refresh
        refreshSelectStatus();
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
}
