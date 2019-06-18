package com.android.messaging.backup;

import android.database.sqlite.SQLiteException;

import com.android.messaging.util.CheckPermissionUtil;
import com.android.messaging.util.OsUtil;
import com.ihs.commons.utils.HSLog;

import java.util.List;

public class RestoreManager {
    //when restore remote is backup file, local is telephony
    private static final String TAG = "-->>";

    private static RestoreManager sInstance;

    public static RestoreManager get() {
        if (sInstance == null) {
            sInstance = new RestoreManager();
        }
        return sInstance;
    }

    private RestoreManager() {

    }

    public void restore(List<BackupSmsMessage> restoredSms, BackupManager.MessageRestoreToDBListener listener) {
        if (!OsUtil.hasSmsPermission() || !CheckPermissionUtil.isSmsPermissionGranted()) {
            // Sync requires READ_SMS permission
            listener.onRestoreFailed();
            return;
        }
        listener.onRestoreStart();

        // Cursors
        final RestoreSyncCursorPair cursors = new RestoreSyncCursorPair();

        // Actually compare the messages using cursor pair and restore to telephony
        long lastTimestampMillis;
        try {
            cursors.query();
            lastTimestampMillis = cursors.scanAndRestore(restoredSms, listener);
        } catch (final SQLiteException e) {
            HSLog.e(TAG, "restore backup: Database exception " + e.getMessage());
            // Let's abort
            lastTimestampMillis = RestoreSyncCursorPair.SYNC_FAILED;
        } catch (final Exception e) {
            HSLog.e(TAG, "restore backup: unexpected failure in scanAndRestore " + e.getMessage());
            lastTimestampMillis = RestoreSyncCursorPair.SYNC_FAILED;
        } finally {
            if (cursors != null) {
                cursors.close();
            }
        }

        if (lastTimestampMillis == RestoreSyncCursorPair.SYNC_COMPLETE) {
            listener.onRestoreSuccess();
        } else {
            listener.onRestoreFailed();
        }
    }
}
