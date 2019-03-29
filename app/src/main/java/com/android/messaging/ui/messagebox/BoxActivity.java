package com.android.messaging.ui.messagebox;

import android.os.Bundle;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.util.BugleAnalytics;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;

import static com.android.messaging.ui.messagebox.MessageBoxActivity.NOTIFICATION_FINISH_MESSAGE_BOX;

public class BoxActivity extends BaseActivity implements INotificationObserver {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.box_activity);
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (NOTIFICATION_FINISH_MESSAGE_BOX.equals(s)) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BugleAnalytics.logEvent("SMS_PopUp_Close", true);
        HSGlobalNotificationCenter.removeObserver(this);
    }
}
