package com.android.messaging.privatebox.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.privatebox.PrivateSettingManager;
import com.android.messaging.ui.appsettings.SettingItemView;
import com.android.messaging.util.UiUtils;
import com.superapps.util.Threads;

public class HideTheIconActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hide_the_icon);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.setTitleBarBackground(toolbar, this);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(R.string.private_box_setting_entrance_summary);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initItemViews();
    }

    private void initItemViews() {
        LottieAnimationView lottie = findViewById(R.id.private_hide_icon_lottie);
        Threads.postOnMainThreadDelayed(lottie::playAnimation, 300);

        SettingItemView hideIconView = findViewById(R.id.private_hide_icon_item);
        hideIconView.setChecked(PrivateSettingManager.isPrivateBoxIconHidden());
        hideIconView.setOnItemClickListener(() -> PrivateSettingManager.setIconHidden(hideIconView.isChecked()));
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
