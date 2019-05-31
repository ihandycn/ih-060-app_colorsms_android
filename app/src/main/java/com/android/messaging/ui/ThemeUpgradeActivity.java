package com.android.messaging.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.ui.customize.theme.ThemeSelectActivity;
import com.android.messaging.ui.view.MessagesTextView;
import com.android.messaging.util.BugleAnalytics;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;

public class ThemeUpgradeActivity extends HSAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_upgrade);
        MessagesTextView gotIt = findViewById(R.id.drag_hotseat_btn);
        gotIt.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.primary_color), getResources().getDimensionPixelSize(R.dimen.dialog_btn_corner_radius), true));
        gotIt.setOnClickListener(v -> {
            Navigations.startActivitySafely(ThemeUpgradeActivity.this,
                    new Intent(ThemeUpgradeActivity.this, ThemeSelectActivity.class));
            finish();
            BugleAnalytics.logEvent("Theme_Upgrade_Click");
        });

        findViewById(R.id.ic_close).setOnClickListener(v -> {
            finish();
            BugleAnalytics.logEvent("Theme_Upgrade_Close");
        });
        ViewGroup.LayoutParams layoutParams = ((ImageView) findViewById(R.id.iv_theme_upgrade_banner)).getLayoutParams();
        layoutParams.width = (int) (Dimensions.getPhoneWidth(this) * 0.82);
        layoutParams.height = layoutParams.width * 558 / 880;

        BugleAnalytics.logEvent("Theme_Upgrade_Show");
    }
}
