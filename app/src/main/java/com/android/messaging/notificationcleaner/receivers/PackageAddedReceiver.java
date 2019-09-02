package com.android.messaging.notificationcleaner.receivers;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.android.messaging.notificationcleaner.BuglePackageManager;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.PostOnNextFrameReceiver;

public class PackageAddedReceiver extends PostOnNextFrameReceiver {

    private static final String TAG = PackageAddedReceiver.class.getName();

    @Override
    public void onPostReceive(Context context, Intent intent) {
        try {
            if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())
                    && !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                String packageName = "";
                if (null != intent.getData()) {
                    packageName = intent.getData().getSchemeSpecificPart();
                }

                if (TextUtils.isEmpty(packageName)) {
                    return;
                }

                BuglePackageManager.getInstance().updateInstalledApplications();
            }
        } catch (Exception e) {
            e.printStackTrace();
            HSLog.d(TAG, "PackageAddedReceiver onReceive e = " + e);
        }
    }
}
