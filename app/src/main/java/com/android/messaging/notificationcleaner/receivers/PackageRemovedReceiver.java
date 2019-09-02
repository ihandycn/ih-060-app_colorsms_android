package com.android.messaging.notificationcleaner.receivers;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.android.messaging.notificationcleaner.BuglePackageManager;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Packages;
import com.superapps.util.PostOnNextFrameReceiver;

public class PackageRemovedReceiver extends PostOnNextFrameReceiver {

    private static final String TAG = PackageRemovedReceiver.class.getName();

    @Override
    public void onPostReceive(Context context, Intent intent) {
        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            try {
                if (null != intent.getData()) {
                    String packageName = intent.getData().getSchemeSpecificPart();
                    if (Packages.isPackageInstalled(packageName)) {
                        return;
                    }
                }
                BuglePackageManager.getInstance().updateInstalledApplications();
            } catch (Exception e) {
                e.printStackTrace();
                HSLog.e(TAG, "PackageRemovedReceiver onReceive e = " + e);
            }
        }
    }
}
