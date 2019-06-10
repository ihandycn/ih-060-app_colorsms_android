package com.android.messaging.backup;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.provider.Telephony.Sms;

import com.android.messaging.backup.BackupMessages.BackupMessage;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.mmslib.SqliteWrapper;
import com.android.messaging.sms.MmsUtils;
import com.android.messaging.util.Assert;
import com.google.common.collect.Sets;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Class holding a pair of cursors - one for local db and one for telephony provider - allowing
 * synchronous stepping through messages as part of sync.
 */
class BackupSyncCursorPair {
    private static final String TAG = "-->>";

    static final long SYNC_COMPLETE = -1L;
    static final long SYNC_STARTING = Long.MAX_VALUE;

    private CursorIterator mLocalCursorIterator;
    private CursorIterator mRemoteCursorsIterator;

    private final String mLocalSelection;
    private final String mRemoteSmsSelection;

    BackupSyncCursorPair() {
        mLocalSelection = LOCAL_MESSAGES_SELECTION;
        mRemoteSmsSelection = getSmsTypeSelectionSql();
    }

    void query(final DatabaseWrapper db) {
        // Load local messages in the sync window
        mLocalCursorIterator = new LocalCursorIterator(db, mLocalSelection);
        // Load remote messages in the sync window
        mRemoteCursorsIterator = new RemoteCursorsIterator(mRemoteSmsSelection);
    }

    void close() {
        if (mLocalCursorIterator != null) {
            mLocalCursorIterator.close();
        }
        if (mRemoteCursorsIterator != null) {
            mRemoteCursorsIterator.close();
        }
    }

    long scan(final int maxMessagesToScan,
              final int maxMessagesToUpdate, final ArrayList<BackupSmsMessage> smsToAdd,
              final ArrayList<BackupSmsMessage> messagesToDelete) {
        // Set of local messages matched with the timestamp of a remote message
        final Set<BackupMessage> matchedLocalMessages = Sets.newHashSet();
        // Set of remote messages matched with the timestamp of a local message
        final Set<BackupMessage> matchedRemoteMessages = Sets.newHashSet();
        long lastTimestampMillis = SYNC_STARTING;
        // Number of messages scanned local and remote
        int localCount = 0;
        int remoteCount = 0;
        // Seed the initial values of remote and local messages for comparison
        BackupMessage remoteMessage = mRemoteCursorsIterator.next();
        BackupMessage localMessage = mLocalCursorIterator.next();
        // Iterate through messages on both sides in reverse time order
        // Import messages in remote not in local, delete messages in local not in remote
        while (localCount + remoteCount < maxMessagesToScan && smsToAdd.size()
                + messagesToDelete.size() < maxMessagesToUpdate) {
            if (remoteMessage == null && localMessage == null) {
                // No more message on both sides - scan complete
                lastTimestampMillis = SYNC_COMPLETE;
                break;
            } else if ((remoteMessage == null && localMessage != null) ||
                    (localMessage != null && remoteMessage != null &&
                            localMessage.getTimestampInMillis()
                                    > remoteMessage.getTimestampInMillis())) {
                // Found a local message that is not in remote db
                // Delete the local message
                messagesToDelete.add((BackupSmsMessage) localMessage);
                lastTimestampMillis = Math.min(lastTimestampMillis, localMessage.getTimestampInMillis());
                // Advance to next local message
                localMessage = mLocalCursorIterator.next();
                localCount += 1;
            } else if ((localMessage == null && remoteMessage != null) ||
                    (localMessage != null && remoteMessage != null &&
                            localMessage.getTimestampInMillis()
                                    < remoteMessage.getTimestampInMillis())) {
                // Found a remote message that is not in local db
                // Add the remote message
                saveMessageToAdd(smsToAdd, remoteMessage);
                lastTimestampMillis = Math.min(lastTimestampMillis,
                        remoteMessage.getTimestampInMillis());
                // Advance to next remote message
                remoteMessage = mRemoteCursorsIterator.next();
                remoteCount += 1;
            } else {
                // Found remote and local messages at the same timestamp
                final long matchedTimestamp = localMessage.getTimestampInMillis();
                lastTimestampMillis = Math.min(lastTimestampMillis, matchedTimestamp);
                // Get the next local and remote messages
                final BackupMessage remoteMessagePeek = mRemoteCursorsIterator.next();
                final BackupMessage localMessagePeek = mLocalCursorIterator.next();
                // Check if only one message on each side matches the current timestamp
                // by looking at the next messages on both sides. If they are either null
                // (meaning no more messages) or having a different timestamp. We want
                // to optimize for this since this is the most common case when majority
                // of the messages are in sync (so they one-to-one pair up at each timestamp),
                // by not allocating the data structures required to compare a set of
                // messages from both sides.
                if ((remoteMessagePeek == null ||
                        remoteMessagePeek.getTimestampInMillis() != matchedTimestamp) &&
                        (localMessagePeek == null ||
                                localMessagePeek.getTimestampInMillis() != matchedTimestamp)) {
                    // Optimize the common case where only one message on each side
                    // that matches the same timestamp
                    if (!remoteMessage.equals(localMessage)) {
                        // local != remote
                        // Delete local message
                        messagesToDelete.add((BackupSmsMessage) localMessage);
                        // Add remote message
                        saveMessageToAdd(smsToAdd, remoteMessage);
                    }
                    // Get next local and remote messages
                    localMessage = localMessagePeek;
                    remoteMessage = remoteMessagePeek;
                    localCount += 1;
                    remoteCount += 1;
                } else {
                    // Rare case in which multiple messages are in the same timestamp
                    // on either or both sides
                    // Gather all the matched remote messages
                    matchedRemoteMessages.clear();
                    matchedRemoteMessages.add(remoteMessage);
                    remoteCount += 1;
                    remoteMessage = remoteMessagePeek;
                    while (remoteMessage != null &&
                            remoteMessage.getTimestampInMillis() == matchedTimestamp) {
                        Assert.isTrue(!matchedRemoteMessages.contains(remoteMessage));
                        matchedRemoteMessages.add(remoteMessage);
                        remoteCount += 1;
                        remoteMessage = mRemoteCursorsIterator.next();
                    }
                    // Gather all the matched local messages
                    matchedLocalMessages.clear();
                    matchedLocalMessages.add(localMessage);
                    localCount += 1;
                    localMessage = localMessagePeek;
                    while (localMessage != null &&
                            localMessage.getTimestampInMillis() == matchedTimestamp) {
                        if (matchedLocalMessages.contains(localMessage)) {
                            // Duplicate message is local database is deleted
                            messagesToDelete.add((BackupSmsMessage) localMessage);
                        } else {
                            matchedLocalMessages.add(localMessage);
                        }
                        localCount += 1;
                        localMessage = mLocalCursorIterator.next();
                    }
                    // Delete messages local only
                    for (final BackupMessage msg : Sets.difference(
                            matchedLocalMessages, matchedRemoteMessages)) {
                        messagesToDelete.add((BackupSmsMessage) msg);
                    }
                    // Add messages remote only
                    for (final BackupMessage msg : Sets.difference(
                            matchedRemoteMessages, matchedLocalMessages)) {
                        saveMessageToAdd(smsToAdd, msg);
                    }
                }
            }
        }
        return lastTimestampMillis;
    }

    /**
     * An iterator for a database cursor
     */
    interface CursorIterator {
        /**
         * Move to next element in the cursor
         *
         * @return The next element (which becomes the current)
         */
        BackupMessage next();

        /**
         * Close the cursor
         */
        void close();

        /**
         * Get the position
         */
        int getPosition();

        /**
         * Get the count
         */
        int getCount();
    }

    private static final String ORDER_BY_DATE_DESC = Sms.DATE + " DESC";

    private static final String LOCAL_MESSAGES_SELECTION = BackupDatabaseHelper.MessageColumn.HIDDEN + " != 1";

    private static final String ORDER_BY_TIMESTAMP_DESC =
            BackupDatabaseHelper.MessageColumn.DATE + " DESC";

    /**
     * The buffered cursor iterator for local SMS
     */
    private static class LocalCursorIterator implements CursorIterator {
        private Cursor mCursor;
        private final DatabaseWrapper mDatabase;

        LocalCursorIterator(final DatabaseWrapper database, final String selection)
                throws SQLiteException {
            mDatabase = database;
            try {
                mCursor = mDatabase.query(
                        BackupDatabaseHelper.BACKUP_MESSAGE_TABLE,
                        BackupDatabaseHelper.MessageColumn.getProjection(),
                        selection,
                        null,
                        null,
                        null,
                        ORDER_BY_TIMESTAMP_DESC);
            } catch (final SQLiteException e) {
                HSLog.e(TAG, "SyncCursorPair: failed to query local sms" + e);
                throw e;
            }
        }

        @Override
        public BackupMessage next() {
            if (mCursor != null && mCursor.moveToNext()) {
                BackupSmsMessage sms = new BackupSmsMessage();
                sms.load(mCursor);
                return sms;
            }
            return null;
        }

        @Override
        public int getCount() {
            return (mCursor == null ? 0 : mCursor.getCount());
        }

        @Override
        public int getPosition() {
            return (mCursor == null ? 0 : mCursor.getPosition());
        }

        @Override
        public void close() {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
        }
    }

    /**
     * The cursor iterator for remote sms.
     * Since SMS and MMS are stored in different tables in telephony provider,
     * this class merges the two cursors and provides a unified view of messages
     * from both cursors. Note that the order is DESC.
     */
    private static class RemoteCursorsIterator implements CursorIterator {
        private Cursor mSmsCursor;
        private BackupMessage mNextSms;

        RemoteCursorsIterator(final String smsSelection)
                throws SQLiteException {
            mSmsCursor = null;
            try {
                final Context context = HSApplication.getContext();
                mSmsCursor = SqliteWrapper.query(
                        context,
                        context.getContentResolver(),
                        Sms.CONTENT_URI,
                        BackupDatabaseHelper.MessageColumn.getProjection(),
                        smsSelection,
                        null /* selectionArgs */,
                        ORDER_BY_DATE_DESC);
                if (mSmsCursor == null) {
                    throw new RuntimeException("Null cursor from remote SMS query");
                }

                mSmsCursor.moveToPosition(-1);

                // Move to the first element in the combined stream from both cursors
                mNextSms = getSmsCursorNext();
            } catch (final SQLiteException e) {
                throw e;
            }
        }

        @Override
        public BackupMessage next() {
            BackupMessage result = null;
            if (mNextSms != null) {
                result = mNextSms;
                mNextSms = getSmsCursorNext();
            }
            return result;
        }

        private BackupMessage getSmsCursorNext() {
            if (mSmsCursor != null && mSmsCursor.moveToNext()) {
                BackupSmsMessage smsMessage = new BackupSmsMessage();
                smsMessage.load(mSmsCursor);
                return smsMessage;
            }
            return null;
        }

        @Override
        // Return approximate cursor position allowing for read ahead on two cursors (hence -1)
        public int getPosition() {
            return (mSmsCursor == null ? 0 : mSmsCursor.getPosition()) - 1;
        }

        @Override
        public int getCount() {
            return (mSmsCursor == null ? 0 : mSmsCursor.getCount());
        }

        @Override
        public void close() {
            if (mSmsCursor != null) {
                mSmsCursor.close();
                mSmsCursor = null;
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

    private void saveMessageToAdd(final List<BackupSmsMessage> smsToAdd,
                                  final BackupMessage message) {
        smsToAdd.add((BackupSmsMessage) message);
    }

}
