package com.android.messaging.util;

import android.database.ContentObserver;
import android.os.Handler;

import com.android.messaging.BuildConfig;
import com.android.messaging.Factory;
import com.android.messaging.ui.SetAsDefaultGuideActivity;
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
        if (!PhoneUtils.getDefault().isDefaultSmsApp()) {
            SetAsDefaultGuideActivity.startActivity(Factory.get().getApplicationContext(), SetAsDefaultGuideActivity.DEFAULT_CHANGED);
        }

        if (BuildConfig.DEBUG) {
            if (PhoneUtils.getDefault().isDefaultSmsApp()) {
                Toasts.showToast("debug toast : sms_default_application_set");
            } else {
                Toasts.showToast("debug toast : sms_default_application_cleared");
            }
        }
    }
}
