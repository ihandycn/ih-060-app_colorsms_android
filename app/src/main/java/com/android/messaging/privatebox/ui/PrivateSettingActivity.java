package com.android.messaging.privatebox.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.privatebox.PrivateSettingManager;
import com.android.messaging.ui.appsettings.SettingItemView;
import com.android.messaging.util.UiUtils;
import com.superapps.util.Navigations;

public class PrivateSettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_setting);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.setTitleBarBackground(toolbar, this);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(R.string.private_box_setting);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initItemViews();
    }

    private void initItemViews() {
        SettingItemView securityView = findViewById(R.id.private_setting_security);
        securityView.setOnItemClickListener(() -> {
            Intent intent = new Intent(PrivateSettingActivity.this, PrivateBoxSetPasswordActivity.class);
            intent.putExtra(PrivateBoxSetPasswordActivity.INTENT_EXTRA_RESET_PASSWORD, true);
            Navigations.startActivitySafely(PrivateSettingActivity.this, intent);
        });

        SettingItemView entranceView = findViewById(R.id.private_setting_entrance);
        entranceView.setOnItemClickListener(
                () -> Navigations.startActivitySafely(this,
                        new Intent(this, HideTheIconActivity.class)));

        SettingItemView notificationView = findViewById(R.id.private_setting_notifications);
        notificationView.setChecked(PrivateSettingManager.isNotificationEnable());
        notificationView.setOnItemClickListener(() -> PrivateSettingManager.setNotificationEnable(notificationView.isChecked()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
