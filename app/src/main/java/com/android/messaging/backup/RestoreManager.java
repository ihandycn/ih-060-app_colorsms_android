package com.android.messaging.backup;

import android.database.sqlite.SQLiteException;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.util.BugleGservices;
import com.android.messaging.util.BugleGservicesKeys;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.OsUtil;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.List;

public class RestoreManager {
    //when restore remote is backup file, local is telephony
    static final long SYNC_FAILED = Long.MIN_VALUE;
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

    public void restore(List<BackupSmsMessage> restoredSms) {
        if (!OsUtil.hasSmsPermission()) {
            // Sync requires READ_SMS permission
            return;
        }

        final BugleGservices bugleGservices = BugleGservices.get();

        final int maxMessagesToScan = bugleGservices.getInt(
                BugleGservicesKeys.SMS_SYNC_BATCH_MAX_MESSAGES_TO_SCAN,
                BugleGservicesKeys.SMS_SYNC_BATCH_MAX_MESSAGES_TO_SCAN_DEFAULT);

        final int initialMaxMessagesToUpdate = 1000000;
        final int smsSyncSubsequentBatchSizeMin = bugleGservices.getInt(
                BugleGservicesKeys.SMS_SYNC_BATCH_SIZE_MIN,
                BugleGservicesKeys.SMS_SYNC_BATCH_SIZE_MIN_DEFAULT);
        final int smsSyncSubsequentBatchSizeMax = bugleGservices.getInt(
                BugleGservicesKeys.SMS_SYNC_BATCH_SIZE_MAX,
                BugleGservicesKeys.SMS_SYNC_BATCH_SIZE_MAX_DEFAULT);

        // Cap sync size to GServices limits
        final int maxMessagesToUpdate = Math.max(smsSyncSubsequentBatchSizeMin,
                Math.min(initialMaxMessagesToUpdate, smsSyncSubsequentBatchSizeMax));

        // Sms messages to store
        final ArrayList<BackupSmsMessage> smsToAdd = new ArrayList<>();

        // Cursors
        final RestoreSyncCursorPair cursors = new RestoreSyncCursorPair();

        // Actually compare the messages using cursor pair
        long lastTimestampMillis = syncCursorPair(cursors, restoredSms, smsToAdd, maxMessagesToUpdate);


        // If comparison succeeds bundle up the changes for processing in ActionService
        if (lastTimestampMillis == SYNC_FAILED) {
            return;
        }

        final int messagesUpdated = smsToAdd.size();

        // Perform local database changes in one transaction
        if (messagesUpdated > 0) {
            final RestoreMessageBatch batch = new RestoreMessageBatch(smsToAdd);
            batch.updateTelephonyDatabase();
        }
    }

    private long syncCursorPair(final RestoreSyncCursorPair cursors,
                                final List<BackupSmsMessage> restoreList,
                                final ArrayList<BackupSmsMessage> smsToAdd,
                                final int maxMessagesToUpdate) {
        long lastTimestampMillis;

        try {
            cursors.query();
            lastTimestampMillis = cursors.scan(restoreList, maxMessagesToUpdate, smsToAdd);
        } catch (final SQLiteException e) {
            HSLog.e(TAG, "restore backup: Database exception " + e.getMessage());
            // Let's abort
            lastTimestampMillis = SYNC_FAILED;
        } catch (final Exception e) {
            HSLog.e(TAG, "restore backup: unexpected failure in scan " + e.getMessage());
            lastTimestampMillis = SYNC_FAILED;
        } finally {
            if (cursors != null) {
                cursors.close();
            }
        }

        return lastTimestampMillis;
    }
}
