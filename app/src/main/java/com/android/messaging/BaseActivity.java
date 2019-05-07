package com.android.messaging;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.PhoneUtils;
import com.ihs.commons.utils.HSLog;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    private boolean mJustCreated = true;
    protected boolean mShouldFinishThisTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!PhoneUtils.getDefault().isDefaultSmsApp()) {
            UIIntents.get().launchWelcomeSetAsDefaultActivity(this);
            finish();
            mShouldFinishThisTime = true;
            HSLog.d(TAG, "Show welcome set as default");
        }
    }

    @Override protected void onResume() {
        super.onResume();

        if (!mJustCreated) {
            if (!PhoneUtils.getDefault().isDefaultSmsApp()) {
                UIIntents.get().launchWelcomeSetAsDefaultActivity(this);
                finish();
                HSLog.d(TAG, "Show welcome set as default");
            }
        }

        mJustCreated = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
