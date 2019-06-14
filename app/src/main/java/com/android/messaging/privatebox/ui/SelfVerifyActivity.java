package com.android.messaging.privatebox.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.privatebox.AppPrivateLockManager;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.UIIntentsImpl;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.UiUtils;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Navigations;

public class SelfVerifyActivity extends VerifyActivity {
    public static final String INTENT_KEY_ACTIVITY_ENTRANCE = "entrance";
    public static final String INTENT_KEY_ENTRANCE_CONVERSATION_ID = "conversation_id";

    public static final String ENTRANCE_NOTIFICATION = "Notification";
    public static final String ENTRANCE_NOTIFICATION_TO_CONVERSATION_LIST = "Notification to list";
    public static final String ENTRANCE_MENU = "Menu";
    public static final String ENTRANCE_CREATE_ICON = "CreateIcon";
    public static final String ENTRANCE_APPLICATION_STOP = "ApplicationStop";

    private String mEntrance;

    public static void startVerifyActivityForAppLocked() {
        Intent intent = new Intent(HSApplication.getContext(), SelfVerifyActivity.class);
        intent.putExtra(INTENT_KEY_ACTIVITY_ENTRANCE, ENTRANCE_APPLICATION_STOP);
        Navigations.startActivitySafely(HSApplication.getContext(), intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.setTitleBarBackground(toolbar, this);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.menu_privacy_box));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        UiUtils.setTitleBarBackground(toolbar, this);
        BugleAnalytics.logEvent("PrivateBox_UnlockPage_Show", true,
                "entrance", getIntent().getStringExtra(INTENT_KEY_ACTIVITY_ENTRANCE));
        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_KEY_ACTIVITY_ENTRANCE)) {
            mEntrance = intent.getStringExtra(INTENT_KEY_ACTIVITY_ENTRANCE);
        }
        if ((intent.hasExtra(INTENT_KEY_ENTRANCE_CONVERSATION_ID)
                && ENTRANCE_NOTIFICATION.equals(mEntrance))
                || ENTRANCE_NOTIFICATION_TO_CONVERSATION_LIST.equals(mEntrance)) {
            BugleAnalytics.logEvent("Notifications_Clicked_PrivateBox");
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_self_verify;
    }

    @Override
    protected Drawable getProtectedAppIcon() {
        return null;
    }

    @Override
    protected String getProtectedAppName() {
        return null;
    }

    @Override
    protected Drawable getPanelAppIcon() {
        return getResources().getDrawable(R.mipmap.ic_launcher);
    }

    @Override
    protected int getFingerprintTipColor() {
        return 0xff376FE4;
    }

    @Override
    protected void onUnlockSucceed() {
        super.onUnlockSucceed();
        AppPrivateLockManager.getInstance().unlockAppLock();
        BugleAnalytics.logEvent("PrivateBox_UnlockPage_Unlock", true,
                "entrance", getIntent().getStringExtra(INTENT_KEY_ACTIVITY_ENTRANCE));
        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_KEY_ENTRANCE_CONVERSATION_ID)
                && ENTRANCE_NOTIFICATION.equals(mEntrance)) {
            UIIntents.get().launchConversationListActivity(this);
            Navigations.startActivitySafely(this,
                    new Intent(this, PrivateConversationListActivity.class));
            overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
            UIIntents.get().launchConversationActivity(
                    this, intent.getStringExtra(INTENT_KEY_ENTRANCE_CONVERSATION_ID), null,
                    null,
                    false);
        } else if (ENTRANCE_NOTIFICATION_TO_CONVERSATION_LIST.equals(mEntrance)) {
            UIIntents.get().launchConversationListActivity(this);
            Navigations.startActivitySafely(this,
                    new Intent(this, PrivateConversationListActivity.class));
            overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
        } else if (ENTRANCE_APPLICATION_STOP.equals(mEntrance)) {
            //do nothing
        } else {
            Navigations.startActivitySafely(SelfVerifyActivity.this,
                    new Intent(SelfVerifyActivity.this, PrivateConversationListActivity.class));
            overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
        }
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (ENTRANCE_NOTIFICATION.equals(mEntrance)
                || ENTRANCE_NOTIFICATION_TO_CONVERSATION_LIST.equals(mEntrance)
                || ENTRANCE_APPLICATION_STOP.equals(mEntrance)) {
            UIIntentsImpl.get().launchConversationListActivity(this);
        }
        finish();
    }
}
