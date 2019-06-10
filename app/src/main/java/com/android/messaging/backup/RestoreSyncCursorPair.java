package com.android.messaging.backup;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.provider.Telephony.Sms;

import com.android.messaging.mmslib.SqliteWrapper;
import com.android.messaging.sms.MmsUtils;
import com.android.messaging.util.Assert;
import com.google.common.collect.Sets;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
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
    static final long SYNC_STARTING = Long.MAX_VALUE;

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

    long scan(final List<BackupSmsMessage> backupFileMessages,
              final int maxMessagesToUpdate,
              final ArrayList<BackupSmsMessage> smsToAdd) {
        Iterator<BackupSmsMessage> remoteSmsList = backupFileMessages.iterator();
        final Set<BackupSmsMessage> matchedLocalMessages = Sets.newHashSet();
        final Set<BackupSmsMessage> matchedRemoteMessages = Sets.newHashSet();
        long lastTimestampMillis = SYNC_STARTING;
        // Seed the initial values of remote and local messages for comparison
        BackupSmsMessage remoteMessage = remoteSmsList.hasNext() ? remoteSmsList.next() : null;
        BackupSmsMessage localMessage = mLocalCursorIterator.next();
        // Iterate through messages on both sides in reverse time order
        // Import messages in remote not in local
        while (smsToAdd.size() < maxMessagesToUpdate) {
            if (remoteMessage == null) {
                // No more message in remote
                lastTimestampMillis = SYNC_COMPLETE;
                break;
            } else if (localMessage != null && remoteMessage != null &&
                    localMessage.getTimestampInMillis()
                            > remoteMessage.getTimestampInMillis()) {
                lastTimestampMillis = Math.min(lastTimestampMillis, localMessage.getTimestampInMillis());
                // Advance to next local message
                localMessage = mLocalCursorIterator.next();
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
                remoteMessage = remoteSmsList.hasNext() ? remoteSmsList.next() : null;
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
                        remoteMessagePeek.getTimestampInMillis() != matchedTimestamp) &&
                        (localMessagePeek == null ||
                                localMessagePeek.getTimestampInMillis() != matchedTimestamp)) {
                    // Optimize the common case where only one message on each side
                    // that matches the same timestamp
                    if (!remoteMessage.equals(localMessage)) {
                        // both remote and local messages just has one message in this same time,
                        // compare and add
                        saveMessageToAdd(smsToAdd, remoteMessage);
                    } else {
                    }
                    // Get next local and remote messages
                    localMessage = localMessagePeek;
                    remoteMessage = remoteMessagePeek;
                } else {
                    // Rare case in which multiple messages are in the same timestamp
                    // on either or both sides
                    // Gather all the matched remote messages
                    matchedRemoteMessages.clear();
                    matchedRemoteMessages.add(remoteMessage);
                    remoteMessage = remoteMessagePeek;
                    while (remoteMessage != null &&
                            remoteMessage.getTimestampInMillis() == matchedTimestamp) {
                        Assert.isTrue(!matchedRemoteMessages.contains(remoteMessage));
                        matchedRemoteMessages.add(remoteMessage);
                        remoteMessage = remoteSmsList.hasNext() ? remoteSmsList.next() : null;
                    }
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
                    // Add messages remote only
                    for (final BackupSmsMessage msg : Sets.difference(
                            matchedRemoteMessages, matchedLocalMessages)) {
                        saveMessageToAdd(smsToAdd, msg);
                    }
                }
            }
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

    private void saveMessageToAdd(final List<BackupSmsMessage> smsToAdd,
                                  final BackupSmsMessage message) {
        smsToAdd.add(message);
    }
}
