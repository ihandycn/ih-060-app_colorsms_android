package com.android.messaging.backup;

import android.support.annotation.IntDef;

public class BackupInfo {
    public static final int LOCAL = 1;
    public static final int CLOUD = 2;

    @IntDef({LOCAL, CLOUD})
    public @interface BackupLocationType {
    }

    private int mLocationType;
    private String mBackupKey;
    private String mLocationString;

    public BackupInfo(@BackupLocationType int locationType, String backupKey, String locationStr) {
        mLocationType = locationType;
        mBackupKey = backupKey;
        mLocationString = locationStr;
    }

    public int getLocationType() {
        return mLocationType;
    }

    public String getBackupTimeStr() {
        return null;
    }

    public String getKey() {
        return mBackupKey;
    }

    public String getLocationString() {
        return null;
    }
}
