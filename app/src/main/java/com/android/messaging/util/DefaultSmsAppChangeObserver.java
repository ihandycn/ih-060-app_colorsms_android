package com.android.messaging.util;

import android.database.ContentObserver;
import android.os.Handler;

import com.android.messaging.BuildConfig;
import com.android.messaging.ui.SetAsDefaultGuideActivity;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

public class DefaultSmsAppChangeObserver extends ContentObserver {
    private boolean mIsDefaultSmsBefore;
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public DefaultSmsAppChangeObserver(Handler handler) {
        super(handler);
        mIsDefaultSmsBefore = DefaultSMSUtils.isDefaultSmsApp();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Threads.postOnMainThreadDelayed(new Runnable() {
            @Override
            public void run() {
                boolean isDefault = DefaultSMSUtils.isDefaultSmsApp();
                if (!isDefault && mIsDefaultSmsBefore) {
                    if (HSConfig.optBoolean(false, "Application", "SetDefaultAlert", "Switch")) {
                        SetAsDefaultGuideActivity.startActivity(HSApplication.getContext(), SetAsDefaultGuideActivity.DEFAULT_CHANGED);
                    }
                }

                mIsDefaultSmsBefore = isDefault;
                if (BuildConfig.DEBUG) {
                    if (isDefault) {
                        Toasts.showToast("debug toast : sms_default_application_set");
                    } else {
                        Toasts.showToast("debug toast : sms_default_application_cleared");
                    }
                }
            }
        }, 500);
    }
}
