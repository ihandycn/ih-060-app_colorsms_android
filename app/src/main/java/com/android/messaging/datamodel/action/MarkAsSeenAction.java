/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.messaging.datamodel.action;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseHelper.MessageColumns;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.privatebox.PrivateContactsManager;
import com.android.messaging.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Action used to mark all messages as seen
 */
public class MarkAsSeenAction extends Action implements Parcelable {
    private static final String TAG = LogUtil.BUGLE_DATAMODEL_TAG;
    private static final String KEY_CONVERSATION_ID = "conversation_id";
    private static final String KEY_JUST_PRIVATE_CONVERSATIONS = "just_move_private_conversation";
    private static final String KEY_JUST_NORMAL_CONVERSATIONS = "just_move_normal_conversation";
    private static final String KEY_CONVERSATION_ID_LIST = "conversation_list";

    /**
     * Mark all messages as seen.
     */
    public static void markAllAsSeen() {
        final MarkAsSeenAction action = new MarkAsSeenAction((String) null/*conversationId*/);
        action.start();
    }

    public static void markAllPrivateAsSeen() {
        final MarkAsSeenAction action = new MarkAsSeenAction((String) null/*conversationId*/);
        action.actionParameters.putBoolean(KEY_JUST_PRIVATE_CONVERSATIONS, true);
        action.start();
    }

    public static void markAllNormalAsSeen() {
        final MarkAsSeenAction action = new MarkAsSeenAction((String) null/*conversationId*/);
        action.actionParameters.putBoolean(KEY_JUST_NORMAL_CONVERSATIONS, true);
        action.start();
    }

    /**
     * Mark all messages of a given conversation as seen.
     */
    public static void markAsSeen(final String conversationId) {
        final MarkAsSeenAction action = new MarkAsSeenAction(conversationId);
        action.start();
    }

    public static void markAsSeen(final ArrayList<String> conversationList) {
        final MarkAsSeenAction action = new MarkAsSeenAction((String) null);
        action.actionParameters.putStringArrayList(KEY_CONVERSATION_ID_LIST, conversationList);
        action.start();
    }

    /**
     * ctor for MarkAsSeenAction.
     *
     * @param conversationId the conversation id for which to mark as seen, or null to mark all
     *                       messages as seen
     */
    public MarkAsSeenAction(final String conversationId) {
        actionParameters.putString(KEY_CONVERSATION_ID, conversationId);
    }

    @Override
    protected Object executeAction() {
        final String conversationId =
                actionParameters.getString(KEY_CONVERSATION_ID);
        final boolean hasSpecificConversation = !TextUtils.isEmpty(conversationId);

        // Everything in telephony should already have the seen bit set.
        // Possible exception are messages which did not have seen set and
        // were sync'ed into bugle.

        // Now mark the messages as seen in the bugle db
        final DatabaseWrapper db = DataModel.get().getDatabase();
        db.beginTransaction();
        int updateCount = 0;

        try {
            final ContentValues values = new ContentValues();
            values.put(MessageColumns.SEEN, 1);

            if (hasSpecificConversation) {
                updateCount = db.update(DatabaseHelper.MESSAGES_TABLE, values,
                        MessageColumns.SEEN + " != 1 AND " +
                                MessageColumns.CONVERSATION_ID + "=?",
                        new String[]{conversationId});
                if (updateCount > 0) {
                    MessagingContentProvider.notifyMessagesChanged(conversationId);
                }
            } else {
                if (actionParameters.containsKey(KEY_CONVERSATION_ID_LIST)) {
                    List<String> list = actionParameters.getStringArrayList(KEY_CONVERSATION_ID_LIST);
                    if (list == null || list.size() == 0) {
                        return null;
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append(list.get(0));
                    for (String s : list) {
                        sb.append(",").append(s);
                    }
                    updateCount = db.update(DatabaseHelper.MESSAGES_TABLE, values,
                            MessageColumns.SEEN + " != 1 AND " +
                                    MessageColumns.CONVERSATION_ID + " IN ( " + sb.toString() + " )",
                            null);
                    if (updateCount > 0) {
                        MessagingContentProvider.notifyMessagesChanged(conversationId);
                    }
                } else if (actionParameters.containsKey(KEY_JUST_PRIVATE_CONVERSATIONS)) {
                    Cursor cursor = db.query(DatabaseHelper.MESSAGES_TABLE,
                            new String[]{MessageColumns.CONVERSATION_ID},
                            MessageColumns.SEEN + " != 1 ", null,
                            MessageColumns.CONVERSATION_ID, null, null);
                    if (cursor != null) {
                        List<String> list = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            String covId = cursor.getString(0);
                            List<String> recipients = BugleDatabaseOperations.getRecipientsForConversation(db, covId);
                            boolean isPrivate = PrivateContactsManager.getInstance().isPrivateRecipient(recipients);
                            if (isPrivate) {
                                list.add(covId);
                            }
                        }
                        cursor.close();
                        StringBuilder sb = new StringBuilder();
                        if (list.size() > 0) {
                            for (String covId : list) {
                                if (sb.length() > 0) {
                                    sb.append(",");
                                }
                                sb.append(covId);
                            }

                            updateCount = db.update(DatabaseHelper.MESSAGES_TABLE, values,
                                    MessageColumns.SEEN + " != 1 AND "
                                            + MessageColumns.CONVERSATION_ID + " IN (" +
                                            sb.toString() + ")",
                                    null);
                            if (updateCount > 0) {
                                MessagingContentProvider.notifyMessagesChanged(conversationId);
                            }
                        }
                    }
                } else if (actionParameters.containsKey(KEY_JUST_NORMAL_CONVERSATIONS)) {
                    Cursor cursor = db.query(DatabaseHelper.MESSAGES_TABLE,
                            new String[]{MessageColumns.CONVERSATION_ID},
                            MessageColumns.SEEN + " != 1 ", null,
                            MessageColumns.CONVERSATION_ID, null, null);
                    if (cursor != null) {
                        List<String> list = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            String covId = cursor.getString(0);
                            List<String> recipients = BugleDatabaseOperations.getRecipientsForConversation(db, covId);
                            boolean isPrivate = PrivateContactsManager.getInstance().isPrivateRecipient(recipients);
                            if (isPrivate) {
                                list.add(covId);
                            }
                        }
                        cursor.close();
                        StringBuilder sb = new StringBuilder();
                        if (list.size() > 0) {
                            for (String covId : list) {
                                if (sb.length() > 0) {
                                    sb.append(",");
                                }
                                sb.append(covId);
                            }

                            updateCount = db.update(DatabaseHelper.MESSAGES_TABLE, values,
                                    MessageColumns.SEEN + " != 1 AND "
                                            + MessageColumns.CONVERSATION_ID + " NOT IN (" +
                                            sb.toString() + ")",
                                    null);

                        } else {
                            updateCount = db.update(DatabaseHelper.MESSAGES_TABLE, values,
                                    MessageColumns.SEEN + " != 1", null/*selectionArgs*/);
                        }
                        if (updateCount > 0) {
                            MessagingContentProvider.notifyMessagesChanged(conversationId);
                        }
                    }
                } else {
                    updateCount = db.update(DatabaseHelper.MESSAGES_TABLE, values,
                            MessageColumns.SEEN + " != 1", null/*selectionArgs*/);
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        // After marking messages as seen, update the notifications. This will
        // clear the now stale notifications.
        //if (updateCount > 0) {
        BugleNotifications.update(false/*silent*/, BugleNotifications.UPDATE_ALL);
        //}
        return null;
    }

    private MarkAsSeenAction(final Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<MarkAsSeenAction> CREATOR
            = new Parcelable.Creator<MarkAsSeenAction>() {
        @Override
        public MarkAsSeenAction createFromParcel(final Parcel in) {
            return new MarkAsSeenAction(in);
        }

        @Override
        public MarkAsSeenAction[] newArray(final int size) {
            return new MarkAsSeenAction[size];
        }
    };

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        writeActionToParcel(parcel, flags);
    }
}
