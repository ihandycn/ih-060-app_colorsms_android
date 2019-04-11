package com.android.messaging.privatebox;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.privatebox.ui.PrivateConversationListActivity;
import com.android.messaging.util.UiUtils;
import com.superapps.util.Navigations;

public class SelfVerifyActivity extends VerifyActivity {

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

        RelativeLayout mainContainer = findViewById(R.id.lock_container);
        mainContainer.setBackgroundResource(R.color.primary_color);
    }

    @Override protected int getLayoutResId() {
        return R.layout.activity_self_verify;
    }

    @Override
    protected Drawable getBackgroundDrawable() {
        return getResources().getDrawable(R.color.primary_color);
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

    @Override protected int getFingerprintTipColor() {
        return 0xff376FE4;
    }

    @Override
    protected void onUnlockSucceed() {
        super.onUnlockSucceed();

        Navigations.startActivitySafely(SelfVerifyActivity.this,
                new Intent(SelfVerifyActivity.this, PrivateConversationListActivity.class));
        finish();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void backPressed() {
        finish();
    }
}
