package com.android.messaging.backup;

import android.support.annotation.IntDef;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupInfo {
    public static final int LOCAL = 1;
    public static final int CLOUD = 2;
    public static final int BOTH = 3;

    @IntDef({LOCAL, CLOUD, BOTH})
    @interface BackupLocationType {
    }

    private int mLocationType;
    private String mBackupKey;

    public BackupInfo(@BackupLocationType int locationType, String backupKey) {
        mLocationType = locationType;
        mBackupKey = backupKey;
    }

    public int getLocationType() {
        return mLocationType;
    }

    public String getBackupTimeStr() {
        if (mBackupKey == null) {
            return null;
        }
        try {
            long time = Long.parseLong(mBackupKey);
            Date date = new Date(time);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return format.format(date);
        } catch (Exception e) {
            return mBackupKey;
        }
    }

    public String getKey() {
        return mBackupKey;
    }
}
