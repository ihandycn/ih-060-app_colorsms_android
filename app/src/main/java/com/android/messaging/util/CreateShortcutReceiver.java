package com.android.messaging.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.messaging.R;
import com.ihs.app.framework.HSApplication;

public class CreateShortcutReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, HSApplication.getContext().getResources().getString(R.string.create_shortcut_has_been_create)
                , Toast.LENGTH_SHORT).show();

        BugleAnalytics.logEvent("SMS_Shortcut_Creat_Success", false, true);
    }
}