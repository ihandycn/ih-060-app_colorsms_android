package com.android.messaging.backup;

import android.database.sqlite.SQLiteException;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.util.BugleGservices;
import com.android.messaging.util.BugleGservicesKeys;
import com.android.messaging.util.OsUtil;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;

public class BackupSyncManager {
    static final long SYNC_FAILED = Long.MIN_VALUE;
    private static final String PREF_KEY_BACKUP_SYNC = "backup_synchronizing";
    private static final String TAG = "-->>";

    private static BackupSyncManager sInstance;

    public interface BackupSyncListener {
        void onSyncStart();

        void onSyncFailed();

        void onSyncSuccess();
    }

    public static BackupSyncManager get() {
        if (sInstance == null) {
            sInstance = new BackupSyncManager();
        }
        return sInstance;
    }

    private BackupSyncManager() {

    }

    public long sync() {
        if (!OsUtil.hasSmsPermission()) {
            return SYNC_FAILED;
        }

        final BugleGservices bugleGservices = BugleGservices.get();
        final DatabaseWrapper db = DataModel.get().getDatabase();

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

        final ArrayList<BackupSmsMessage> smsToAdd = new ArrayList<>();
        // List of local SMS to remove
        final ArrayList<BackupSmsMessage> messagesToDelete = new ArrayList<>();

        final BackupSyncCursorPair cursors = new BackupSyncCursorPair();

        // Actually compare the messages using cursor pair
        long lastTimestampMillis = syncCursorPair(db, cursors, smsToAdd,
                messagesToDelete, maxMessagesToScan, maxMessagesToUpdate);


        // If comparison succeeds bundle up the changes for processing in ActionService
        if (lastTimestampMillis == SYNC_FAILED) {
            return SYNC_FAILED;
        }

        final int messagesUpdated = smsToAdd.size() + messagesToDelete.size();

        // Perform local database changes in one transaction
        if (messagesUpdated > 0) {
            final BackupSyncMessageBatch batch = new BackupSyncMessageBatch(smsToAdd, messagesToDelete);
            batch.updateLocalDatabase();
        }
        return lastTimestampMillis;
    }

    private long syncCursorPair(final DatabaseWrapper db, final BackupSyncCursorPair cursors,
                                final ArrayList<BackupSmsMessage> smsToAdd,
                                final ArrayList<BackupSmsMessage> messagesToDelete, final int maxMessagesToScan,
                                final int maxMessagesToUpdate) {
        long lastTimestampMillis;

        try {
            cursors.query(db);
            lastTimestampMillis = cursors.scan(maxMessagesToScan, maxMessagesToUpdate, smsToAdd, messagesToDelete);
        } catch (final SQLiteException e) {
            HSLog.e(TAG, "SyncMessagesAction: Database exception\n" + e);
            lastTimestampMillis = SYNC_FAILED;
        } catch (final Exception e) {
            HSLog.e(TAG, "SyncMessagesAction: unexpected failure in scan\n" + e.getMessage());
            lastTimestampMillis = SYNC_FAILED;
        } finally {
            if (cursors != null) {
                cursors.close();
            }
        }

        return lastTimestampMillis;
    }
}
