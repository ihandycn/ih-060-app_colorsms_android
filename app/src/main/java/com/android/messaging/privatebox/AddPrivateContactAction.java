package com.android.messaging.privatebox;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;

import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.action.Action;
import com.android.messaging.privatebox.ui.addtolist.ContactsSelectActivity;
import com.android.messaging.sms.MmsSmsUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

import java.util.ArrayList;
import java.util.List;

import static com.android.messaging.privatebox.PrivateContactsManager.PHONE_NUMBER;
import static com.android.messaging.privatebox.PrivateContactsManager.PRIVATE_CONTACTS_TABLE;
import static com.android.messaging.privatebox.PrivateContactsManager.THREAD_ID;
import static com.android.messaging.privatebox.PrivateContactsManager.sProjection;

public class AddPrivateContactAction extends Action {
    private static final String KEY_THREAD_ID = "thread_id";
    private static final String KEY_RESPONSE_CODE = "response_code";
    private static final String KEY_RECIPIENT = "recipient";
    private static final String KEY_SHOULD_SEND_GLOBAL_NOTIFICATION = "send_notification";
    private static final String KEY_SHOULD_MOVE_MESSAGES = "move_message";

    static void addPrivateThreadId(final long threadId) {
        AddPrivateContactAction action = new AddPrivateContactAction();
        action.actionParameters.putLongArray(KEY_THREAD_ID, new long[]{threadId});
        action.start();
    }

    public static void addPrivateRecipientsAndMoveMessages(final List<String> recipients, final long responseCode) {
        ArrayList<Long> threadIdList = new ArrayList<>();
        //todo : check permission for no grant exception
        for (String recipient : recipients) {
            long threadId = MmsSmsUtils.Threads.getOrCreateThreadId(HSApplication.getContext(), recipient);
            if (threadId < 0) {
                continue;
            }
            threadIdList.add(threadId);
        }

        AddPrivateContactAction action = new AddPrivateContactAction();
        action.actionParameters.putBoolean(KEY_SHOULD_SEND_GLOBAL_NOTIFICATION, true);
        action.actionParameters.putBoolean(KEY_SHOULD_MOVE_MESSAGES, true);
        long[] threadIdArray = new long[threadIdList.size()];
        for (int i = 0; i < threadIdList.size(); i++) {
            threadIdArray[i] = threadIdList.get(i);
        }
        action.actionParameters.putLongArray(KEY_THREAD_ID, threadIdArray);
        action.actionParameters.putLong(KEY_RESPONSE_CODE, responseCode);
        action.actionParameters.putStringArrayList(KEY_RECIPIENT, (ArrayList<String>) recipients);
        action.start();
    }

    private AddPrivateContactAction() {
        super();
    }

    @Override
    protected Object executeAction() {
        long[] threadIdList = actionParameters.getLongArray(KEY_THREAD_ID);
        List<String> recipients = null;
        if (actionParameters.containsKey(KEY_RECIPIENT)) {
            recipients = actionParameters.getStringArrayList(KEY_RECIPIENT);
        }
        boolean shouldSendNotification = actionParameters.getBoolean(KEY_SHOULD_SEND_GLOBAL_NOTIFICATION, false);
        boolean shouldMoveMessages = actionParameters.getBoolean(KEY_SHOULD_MOVE_MESSAGES, false);
        long responseCode = actionParameters.getLong(KEY_RESPONSE_CODE, 0);

        DatabaseWrapper db = DataModel.get().getDatabase();
        assert threadIdList != null;

        //1. add thread id into private table
        for (int i = 0; i < threadIdList.length; i++) {
            addThreadIdToPrivateTable(db, threadIdList[i], recipients == null ? null : recipients.get(i));
        }

        //2. move messages if need
        if (shouldMoveMessages) {
            List<String> messageList = new ArrayList<>();
            // get all messages need to be moved;
            for (long threadId : threadIdList) {
                Cursor cursor = db.query(DatabaseHelper.CONVERSATIONS_TABLE, new String[]{"count(*)"},
                        DatabaseHelper.ConversationColumns.SMS_THREAD_ID + "=?",
                        new String[]{String.valueOf(threadId)}, null, null, null);
                int count = 0;
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        count = cursor.getInt(0);
                    }
                    cursor.close();
                }
                //if count > 0, conversation and messages need to be moved;
                if (count > 0) {
                    String conversationId = BugleDatabaseOperations.getExistingConversation(db, threadId, false);
                    cursor = db.query(DatabaseHelper.MESSAGES_TABLE, new String[]{DatabaseHelper.MessageColumns._ID},
                            DatabaseHelper.MessageColumns.CONVERSATION_ID + "=" + conversationId,
                            null, null, null, null);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            messageList.add(cursor.getString(0));
                        }
                        cursor.close();
                    }
                    if (conversationId != null) {
                        MoveConversationToPrivateBoxAction.move(conversationId);
                    }
                }
            }

            if (messageList.size() > 0) {
                if (shouldSendNotification) {
                    HSGlobalNotificationCenter.sendNotification(ContactsSelectActivity.EVENT_MESSAGES_MOVE_START + responseCode);
                }
                MoveMessageToPrivateBoxAction.moveMessagesToPrivateBox(messageList, responseCode);
            } else {
                if (shouldSendNotification) {
                    HSGlobalNotificationCenter.sendNotification(ContactsSelectActivity.EVENT_MESSAGES_MOVE_END + responseCode);
                }
            }
        }
        return null;
    }

    private void addThreadIdToPrivateTable(DatabaseWrapper db, long threadId, String recipient) {
        //check if the thread_id is in the table
        Cursor cursor = db.query(PRIVATE_CONTACTS_TABLE, sProjection, THREAD_ID + "=?",
                new String[]{String.valueOf(threadId)}, null, null, null);
        if (cursor == null) {
            return;
        }
        if (cursor.getCount() <= 0) {
            ContentValues values = new ContentValues();
            values.put(THREAD_ID, threadId);
            values.put(PHONE_NUMBER, recipient);
            db.insert(PRIVATE_CONTACTS_TABLE, null, values);
        }
        cursor.close();
    }

    private AddPrivateContactAction(final Parcel in) {
        super(in);
    }

    public static final Creator<AddPrivateContactAction> CREATOR
            = new Creator<AddPrivateContactAction>() {
        @Override
        public AddPrivateContactAction createFromParcel(final Parcel in) {
            return new AddPrivateContactAction(in);
        }

        @Override
        public AddPrivateContactAction[] newArray(final int size) {
            return new AddPrivateContactAction[size];
        }
    };

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        writeActionToParcel(parcel, flags);
    }
}
