package com.android.messaging.ui;

import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.CreateShortcutUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.android.messaging.ui.view.MessagesTextView;

public class CreateShortcutActivity extends HSAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_shortcut);

        ImageView ivIcon = findViewById(R.id.create_shortcut_icon);
        ivIcon.setImageDrawable(CreateShortcutUtils.sIcon);

        findViewById(R.id.create_shortcut_cancel_btn).setOnClickListener((v) -> finish());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((ImageView) findViewById(R.id.create_shortcut_badge)).setImageResource(R.drawable.ic_launcher_with_shadow);
        }

        MessagesTextView gotIt = findViewById(R.id.create_shortcut_btn);
        gotIt.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.primary_color), getResources().getDimensionPixelSize(R.dimen.dialog_btn_corner_radius), true));
        gotIt.setOnClickListener(v -> {
            CreateShortcutUtils.addShortCut(HSApplication.getContext());
            finish();
            BugleAnalytics.logEvent("SMS_Alert_Shortcut_Click", "OSVersion",
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? "Other" : "Below8.0");
        });

        BugleAnalytics.logEvent("SMS_Alert_Shortcut_Show");
    }

    @Override public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
