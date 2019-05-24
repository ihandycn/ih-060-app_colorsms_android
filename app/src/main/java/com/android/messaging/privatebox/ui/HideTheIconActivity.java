package com.android.messaging.privatebox.ui;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.privatebox.AppPrivateLockManager;
import com.android.messaging.privatebox.PrivateSettingManager;
import com.android.messaging.ui.appsettings.SettingItemView;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.UiUtils;
import com.superapps.util.Threads;

public class HideTheIconActivity extends BaseActivity {

    private LottieAnimationView mLottie;

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

    @Override
    protected void onDestroy() {
        if (mLottie != null) {
            mLottie.cancelAnimation();
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        AppPrivateLockManager.getInstance().checkLockStateAndSelfVerify();
        super.onStart();
    }

    private void initItemViews() {
        mLottie = findViewById(R.id.private_hide_icon_lottie);
        mLottie.setRepeatCount(ValueAnimator.INFINITE);
        Threads.postOnMainThreadDelayed(mLottie::playAnimation, 300);

        SettingItemView hideIconView = findViewById(R.id.private_hide_icon_item);
        hideIconView.setChecked(PrivateSettingManager.isPrivateBoxIconHidden());
        hideIconView.setOnItemClickListener(() -> {
            PrivateSettingManager.setIconHidden(hideIconView.isChecked());
            BugleAnalytics.logEvent("PrivateBox_HideTheIcon_BtnClick", "type",
                    hideIconView.isChecked() ? "ON" : "OFF");
        });
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
