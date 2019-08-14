package com.android.messaging.backup;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.Telephony;
import android.provider.Telephony.Sms;
import android.text.TextUtils;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.mmslib.SqliteWrapper;
import com.android.messaging.sms.MmsUtils;
import com.android.messaging.util.OsUtil;
import com.google.common.collect.Sets;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.security.cert.Extension;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Class holding a pair of cursors - one for local db and one for telephony provider - allowing
 * synchronous stepping through messages as part of sync.
 */
class RestoreSyncCursorPair {
    private static final String TAG = "-->>";

    static final long SYNC_COMPLETE = -1L;
    static final long SYNC_FAILED = Long.MIN_VALUE;
    private static final long SYNC_STARTING = Long.MAX_VALUE;

    private LocalCursorIterator mLocalCursorIterator;

    private final String mLocalSelection;

    RestoreSyncCursorPair() {
        mLocalSelection = getSmsTypeSelectionSql();
    }

    void query() {
        // Load local messages in the sync window
        mLocalCursorIterator = new LocalCursorIterator(mLocalSelection);
    }

    void close() {
        if (mLocalCursorIterator != null) {
            mLocalCursorIterator.close();
        }
    }

    long scanAndRestore(final List<BackupSmsMessage> backupFileMessages,
                        BackupManager.MessageRestoreToDBListener listener) {
        ContentResolver resolver = HSApplication.getContext().getContentResolver();
        int currentRemoteIndex = 0;
        Iterator<BackupSmsMessage> remoteSmsList = backupFileMessages.iterator();
        final Set<BackupSmsMessage> matchedLocalMessages = Sets.newHashSet();
        long lastTimestampMillis = SYNC_STARTING;
        // Seed the initial values of remote and local messages for comparison
        BackupSmsMessage remoteMessage = remoteSmsList.hasNext() ? remoteSmsList.next() : null;
        BackupSmsMessage localMessage = mLocalCursorIterator.next();
        // Iterate through messages on both sides in reverse time order
        // Import messages in remote not in local
        while (true) {
            if (remoteMessage == null) {
                // No more message in remote
                lastTimestampMillis = SYNC_COMPLETE;
                break;
            } else if (localMessage != null &&
                    localMessage.getTimestampInMillis() > remoteMessage.getTimestampInMillis()) {
                lastTimestampMillis = Math.min(lastTimestampMillis, localMessage.getTimestampInMillis());
                // Advance to next local message
                localMessage = mLocalCursorIterator.next();
            } else if (localMessage == null || (localMessage != null &&
                    localMessage.getTimestampInMillis() < remoteMessage.getTimestampInMillis())) {
                // Found a remote message that is not in local db
                // Add the remote message
                storeSms(resolver, remoteMessage);
                lastTimestampMillis = Math.min(lastTimestampMillis,
                        remoteMessage.getTimestampInMillis());
                // Advance to next remote message
                remoteMessage = remoteSmsList.hasNext() ? remoteSmsList.next() : null;
                currentRemoteIndex++;
            } else {
                // Found remote and local messages at the same timestamp
                final long matchedTimestamp = localMessage.getTimestampInMillis();
                lastTimestampMillis = Math.min(lastTimestampMillis, matchedTimestamp);
                // Get the next local and remote messages
                BackupSmsMessage remoteMessagePeek = remoteSmsList.hasNext() ? remoteSmsList.next() : null;
                final BackupSmsMessage localMessagePeek = mLocalCursorIterator.next();
                // Check if only one message on each side matches the current timestamp
                // by looking at the next messages on both sides. If they are either null
                // (meaning no more messages) or having a different timestamp. We want
                // to optimize for this since this is the most common case when majority
                // of the messages are in sync (so they one-to-one pair up at each timestamp),
                // by not allocating the data structures required to compare a set of
                // messages from both sides.
                if ((remoteMessagePeek == null ||
                        remoteMessagePeek.getTimestampInMillis() != matchedTimestamp)
                        && (localMessagePeek == null ||
                        localMessagePeek.getTimestampInMillis() != matchedTimestamp)) {
                    // Optimize the common case where only one message on each side
                    // that matches the same timestamp
                    if (!remoteMessage.equals(localMessage)) {
                        // both remote and local messages just has one message in this same time,
                        // compare and add
                        storeSms(resolver, remoteMessage);
                    }
                    // Get next local and remote messages
                    localMessage = localMessagePeek;
                    remoteMessage = remoteMessagePeek;
                    currentRemoteIndex++;
                } else {
                    // Rare case in which multiple messages are in the same timestamp
                    // on either or both sides
                    remoteMessage = remoteMessagePeek;
                    // Gather all the matched local messages
                    matchedLocalMessages.clear();
                    matchedLocalMessages.add(localMessage);
                    localMessage = localMessagePeek;
                    while (localMessage != null &&
                            localMessage.getTimestampInMillis() == matchedTimestamp) {
                        if (!matchedLocalMessages.contains(localMessage)) {
                            matchedLocalMessages.add(localMessage);
                        }
                        localMessage = mLocalCursorIterator.next();
                    }

                    while (remoteMessage != null && remoteMessage.getTimestampInMillis() == matchedTimestamp) {
                        boolean needAdd = true;
                        for (BackupSmsMessage sms : matchedLocalMessages) {
                            if (sms.equals(remoteMessage)) {
                                needAdd = false;
                                break;
                            }
                        }
                        if (needAdd) {
                            storeSms(resolver, remoteMessage);
                        }
                        remoteMessage = remoteSmsList.hasNext() ? remoteSmsList.next() : null;
                        currentRemoteIndex++;
                    }
                }
            }
            int finalCurrentRemoteIndex = currentRemoteIndex;
            listener.onRestoreUpdate(finalCurrentRemoteIndex);
        }
        return lastTimestampMillis;
    }

    private static final String ORDER_BY_DATE_DESC = Sms.DATE + " DESC";

    /**
     * This class provides the same DatabaseMessage interface over a local SMS db message
     */
    private static BackupSmsMessage getLocalDatabaseMessage(final Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        BackupSmsMessage smsMessage = new BackupSmsMessage();
        smsMessage.load(cursor);
        return smsMessage;
    }

    /**
     * The buffered cursor iterator for local SMS
     */
    private static class LocalCursorIterator {
        private Cursor mCursor;

        LocalCursorIterator(final String selection)
                throws SQLiteException {
            try {
                final Context context = HSApplication.getContext();
                mCursor = SqliteWrapper.query(
                        context,
                        context.getContentResolver(),
                        Sms.CONTENT_URI,
                        BackupDatabaseHelper.MessageColumn.getProjection(),
                        selection,
                        null,
                        ORDER_BY_DATE_DESC);
                if (mCursor == null) {
                    HSLog.w(TAG, "SyncCursorPair: Telephony SMS query returned null cursor; "
                            + "need to cancel sync");
                    throw new RuntimeException("Null cursor from Telephony SMS query");
                }
            } catch (final SQLiteException e) {
                HSLog.e(TAG, "SyncCursorPair: failed to query telephony messages" + e);
                throw e;
            }
        }

        public BackupSmsMessage next() {
            if (mCursor != null && mCursor.moveToNext()) {
                return getLocalDatabaseMessage(mCursor);
            }
            return null;
        }

        public int getCount() {
            return (mCursor == null ? 0 : mCursor.getCount());
        }

        public int getPosition() {
            return (mCursor == null ? 0 : mCursor.getPosition());
        }

        public void close() {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
        }
    }

    /**
     * Type selection for importing sms messages. Only SENT and INBOX messages are imported.
     *
     * @return The SQL selection for importing sms messages
     */
    public static String getSmsTypeSelectionSql() {
        return MmsUtils.getSmsTypeSelectionSql();
    }

    private void storeSms(final ContentResolver resolver, final BackupSmsMessage sms) {
        if (sms.mBody == null) {
            sms.mBody = "";
        }

        ContentValues values = new ContentValues();
        // values.put(BackupDatabaseHelper.MessageColumn.THREAD_ID, sms.mThreadId);
        values.put(BackupDatabaseHelper.MessageColumn.ADDRESS, sms.mAddress);
        values.put(BackupDatabaseHelper.MessageColumn.PERSON, sms.mPerson);
        values.put(BackupDatabaseHelper.MessageColumn.DATE, sms.mDate);
        values.put(BackupDatabaseHelper.MessageColumn.DATE_SEND, sms.mDateSend);
        values.put(BackupDatabaseHelper.MessageColumn.PROTOCOL, sms.mProtocol);
        values.put(BackupDatabaseHelper.MessageColumn.READ, sms.mRead);
        values.put(BackupDatabaseHelper.MessageColumn.STATUS, sms.mStatus);
        values.put(BackupDatabaseHelper.MessageColumn.TYPE, sms.mType);
        values.put(BackupDatabaseHelper.MessageColumn.REPLY_PATH_PRESENT, sms.mReplyPathPresent);
        if (!TextUtils.isEmpty(sms.mSubject)) {
            values.put(BackupDatabaseHelper.MessageColumn.SUBJECT, sms.mSubject);
        }
        values.put(BackupDatabaseHelper.MessageColumn.BODY, sms.mBody);
        values.put(BackupDatabaseHelper.MessageColumn.SERVICE_CENTER, sms.mServiceCenter);
        values.put(BackupDatabaseHelper.MessageColumn.LOCKED, sms.mLocked);
        values.put(BackupDatabaseHelper.MessageColumn.ERROR_CODE, sms.mErrorCode);
        values.put(BackupDatabaseHelper.MessageColumn.SEEN, sms.mSeen);
        if (OsUtil.isAtLeastL_MR1()) {
            values.put(BackupDatabaseHelper.MessageColumn.SUBSCRIPTION_ID, sms.mSubId);
        }

        Uri uri = resolver.insert(Telephony.Sms.CONTENT_URI, values);
        DatabaseWrapper db = DataModel.get().getDatabase();
        updateMessageData(db, uri, sms);

        db.delete(BackupDatabaseHelper.BACKUP_MESSAGE_TABLE,
                BackupDatabaseHelper.MessageColumn.ADDRESS + "=? AND "
                        + BackupDatabaseHelper.MessageColumn.DATE + "=? AND "
                        + BackupDatabaseHelper.MessageColumn.BODY + "=? ",
                new String[]{sms.mAddress, String.valueOf(sms.getTimestampInMillis()), sms.mBody});
    }

    public void updateMessageData(DatabaseWrapper db, Uri telephonyUri, BackupSmsMessage sms) {
        String selectSql = "SELECT " + DatabaseHelper.MESSAGES_TABLE + "." + DatabaseHelper.MessageColumns._ID
                + " FROM " + DatabaseHelper.MESSAGES_TABLE + " LEFT JOIN " + DatabaseHelper.PARTS_TABLE
                + " ON (" + DatabaseHelper.PARTS_TABLE + "." + DatabaseHelper.PartColumns.MESSAGE_ID
                + " = " + DatabaseHelper.MESSAGES_TABLE + "." + DatabaseHelper.MessageColumns._ID + ") "
                + " WHERE " + DatabaseHelper.MessageColumns.RECEIVED_TIMESTAMP + "=" + sms.getTimestampInMillis()
                + " AND " + DatabaseHelper.PartColumns.TEXT + " =? "
                + " AND " + DatabaseHelper.MessageColumns.IS_LOCKED + "=1";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(selectSql, new String[]{sms.mBody});
            if (cursor.getCount() == 0) {
                return;
            }
            while (cursor.moveToNext()) {
                String messageId = cursor.getString(0);
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.MessageColumns.IS_DELETED, 0);
                values.put(DatabaseHelper.MessageColumns.SMS_MESSAGE_URI, telephonyUri.toString());
                db.update(DatabaseHelper.MESSAGES_TABLE, values,
                        DatabaseHelper.MessageColumns._ID + "=?", new String[]{messageId});
            }
        } catch (Exception ignored) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
