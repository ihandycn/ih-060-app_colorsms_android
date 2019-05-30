package com.android.messaging.util;

import android.database.ContentObserver;
import android.os.Handler;

import com.android.messaging.BuildConfig;
import com.android.messaging.Factory;
import com.android.messaging.ui.SetAsDefaultGuideActivity;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

public class DefaultSmsAppChangeObserver extends ContentObserver {
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public DefaultSmsAppChangeObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Threads.postOnMainThreadDelayed(new Runnable() {
            @Override
            public void run() {
                if (!PhoneUtils.getDefault().isDefaultSmsApp()) {
                    if (HSConfig.optBoolean(false, "Application", "SetDefaultAlert", "Switch")) {
                        SetAsDefaultGuideActivity.startActivity(HSApplication.getContext(), SetAsDefaultGuideActivity.DEFAULT_CHANGED);
                    }
                }

                if (BuildConfig.DEBUG) {
                    if (PhoneUtils.getDefault().isDefaultSmsApp()) {
                        Toasts.showToast("debug toast : sms_default_application_set");
                    } else {
                        Toasts.showToast("debug toast : sms_default_application_cleared");
                    }
                }
            }
        }, 500);
    }
}
