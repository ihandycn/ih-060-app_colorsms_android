package com.android.messaging.backup;

import android.content.ContentValues;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.sms.MmsUtils;
import com.android.messaging.util.OsUtil;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

class BackupSyncMessageBatch {
    private static final String TAG = "-->>";

    private final ArrayList<BackupSmsMessage> mSmsToAdd;

    private final ArrayList<BackupSmsMessage> mMessagesToDelete;

    BackupSyncMessageBatch(final ArrayList<BackupSmsMessage> smsToAdd,
                           final ArrayList<BackupSmsMessage> messagesToDelete) {
        mSmsToAdd = smsToAdd;
        mMessagesToDelete = messagesToDelete;
    }

    void updateLocalDatabase() {
        final DatabaseWrapper db = DataModel.get().getDatabase();
        db.beginTransaction();
        try {
            // Store all the SMS messages
            for (final BackupSmsMessage sms : mSmsToAdd) {
                storeSms(db, sms);
            }

            // Batch delete local messages
            batchDelete(db, BackupDatabaseHelper.BACKUP_MESSAGE_TABLE,
                    BackupDatabaseHelper.MessageColumn.MESSAGE_ID,
                    messageListToIds(mMessagesToDelete));

            for (final BackupSmsMessage message : mMessagesToDelete) {
                    HSLog.v(TAG, "SyncMessageBatch: Deleted message " + message.getTelephonyId()
                            + " for SMS with timestamp "
                            + message.getTimestampInMillis());
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private static String[] messageListToIds(final List<BackupSmsMessage> messagesToDelete) {
        final String[] ids = new String[messagesToDelete.size()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = Long.toString(messagesToDelete.get(i).getTelephonyId());
        }
        return ids;
    }

    /**
     * Store the SMS message into local database.
     *
     * @param sms
     */
    private void storeSms(final DatabaseWrapper db, final BackupSmsMessage sms) {
        if (sms.mBody == null) {
            sms.mBody = "";
        }

        ContentValues values = new ContentValues();
        values.put(BackupDatabaseHelper.MessageColumn.MESSAGE_ID, sms.mMessageId);
        values.put(BackupDatabaseHelper.MessageColumn.THREAD_ID, sms.mThreadId);
        values.put(BackupDatabaseHelper.MessageColumn.ADDRESS, sms.mAddress);
        values.put(BackupDatabaseHelper.MessageColumn.PERSON, sms.mPerson);
        values.put(BackupDatabaseHelper.MessageColumn.DATE, sms.mDate);
        values.put(BackupDatabaseHelper.MessageColumn.DATE_SEND, sms.mDateSend);
        values.put(BackupDatabaseHelper.MessageColumn.PROTOCOL, sms.mProtocol);
        values.put(BackupDatabaseHelper.MessageColumn.READ, sms.mRead);
        values.put(BackupDatabaseHelper.MessageColumn.STATUS, sms.mStatus);
        values.put(BackupDatabaseHelper.MessageColumn.TYPE, sms.mType);
        values.put(BackupDatabaseHelper.MessageColumn.REPLY_PATH_PRESENT, sms.mReplyPathPresent);
        values.put(BackupDatabaseHelper.MessageColumn.SUBJECT, sms.mSubject);
        values.put(BackupDatabaseHelper.MessageColumn.BODY, sms.mBody);
        values.put(BackupDatabaseHelper.MessageColumn.SERVICE_CENTER, sms.mServiceCenter);
        values.put(BackupDatabaseHelper.MessageColumn.LOCKED, sms.mLocked);
        values.put(BackupDatabaseHelper.MessageColumn.ERROR_CODE, sms.mErrorCode);
        values.put(BackupDatabaseHelper.MessageColumn.SEEN, sms.mSeen);
        if (OsUtil.isAtLeastL_MR1()) {
            values.put(BackupDatabaseHelper.MessageColumn.SUBSCRIPTION_ID, sms.mSubId);
        }
        db.insert(BackupDatabaseHelper.BACKUP_MESSAGE_TABLE, null, values);
    }

    /**
     * Batch delete database rows by matching a column with a list of values, usually some
     * kind of IDs.
     *
     * @param table
     * @param column
     * @param ids
     * @return Total number of deleted messages
     */
    private static int batchDelete(final DatabaseWrapper db, final String table,
                                   final String column, final String[] ids) {
        int totalDeleted = 0;
        final int totalIds = ids.length;
        for (int start = 0; start < totalIds; start += MmsUtils.MAX_IDS_PER_QUERY) {
            final int end = Math.min(start + MmsUtils.MAX_IDS_PER_QUERY, totalIds); //excluding
            final int count = end - start;
            final String batchSelection = String.format(
                    Locale.US,
                    "%s IN %s",
                    column,
                    MmsUtils.getSqlInOperand(count));
            final String[] batchSelectionArgs = Arrays.copyOfRange(ids, start, end);
            final int deleted = db.delete(
                    table,
                    batchSelection,
                    batchSelectionArgs);
            totalDeleted += deleted;
        }
        return totalDeleted;
    }
}
