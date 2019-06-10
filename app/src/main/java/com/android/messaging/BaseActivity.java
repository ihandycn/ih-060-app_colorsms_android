package com.android.messaging;

import android.os.Bundle;

import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.DefaultSMSUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.utils.HSLog;

public abstract class BaseActivity extends HSAppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    private boolean mJustCreated = true;
    protected boolean mShouldFinishThisTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!DefaultSMSUtils.isDefaultSmsApp()) {
            UIIntents.get().launchWelcomeSetAsDefaultActivity(this);
            finish();
            mShouldFinishThisTime = true;
            HSLog.d(TAG, "Show welcome set as default");
        }
    }

    @Override protected void onResume() {
        super.onResume();

        if (!mJustCreated) {
            if (!DefaultSMSUtils.isDefaultSmsApp()) {
                UIIntents.get().launchWelcomeSetAsDefaultActivity(this);
                finish();
                HSLog.d(TAG, "Show welcome set as default");
            }
        }

        mJustCreated = false;
    }

    @Override public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_null, R.anim.slide_out_to_right_and_fade);
    }
}
