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
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

import static com.android.messaging.privatebox.PrivateContactsManager.PHONE_NUMBER;
import static com.android.messaging.privatebox.PrivateContactsManager.PRIVATE_CONTACTS_TABLE;
import static com.android.messaging.privatebox.PrivateContactsManager.THREAD_ID;
import static com.android.messaging.privatebox.PrivateContactsManager.sProjection;

public class AddPrivateContactAction extends Action {

    public static void addPrivateContact(final long threadId) {
        AddPrivateContactAction action = new AddPrivateContactAction();
        action.actionParameters.putLong(KEY_THREAD_ID, threadId);
        action.actionParameters.putString(KEY_RECIPIENT, null);
        action.start();
    }

    public static void addPrivateContactAndMoveMessages(final long threadId, final String recipient,
                                                        boolean sendGlobalNotification) {
        AddPrivateContactAction action = new AddPrivateContactAction();
        action.actionParameters.putLong(KEY_THREAD_ID, threadId);
        action.actionParameters.putString(KEY_RECIPIENT, recipient);
        action.actionParameters.putBoolean(KEY_SHOULD_SEND_GLOBAL_NOTIFICATION, sendGlobalNotification);
        action.actionParameters.putBoolean(KEY_SHOULD_MOVE_MESSAGES, true);
        action.start();
    }

    private AddPrivateContactAction() {
        super();
    }

    private static final String KEY_THREAD_ID = "thread_id";
    private static final String KEY_RECIPIENT = "recipient";
    private static final String KEY_SHOULD_SEND_GLOBAL_NOTIFICATION = "send_notification";
    private static final String KEY_SHOULD_MOVE_MESSAGES = "move_message";

    @Override
    protected Object executeAction() {
        addThreadId();
        return null;
    }

    private boolean addThreadId() {
        long threadId = actionParameters.getLong(KEY_THREAD_ID);
        String recipient = actionParameters.getString(KEY_RECIPIENT);
        boolean shouldSendNotification = actionParameters.getBoolean(KEY_SHOULD_SEND_GLOBAL_NOTIFICATION, false);
        boolean shouldMoveMessages = actionParameters.getBoolean(KEY_SHOULD_MOVE_MESSAGES, false);

        boolean insertSuccess;
        DatabaseWrapper db = DataModel.get().getDatabase();
        //check if the thread_id is in the table
        Cursor cursor = db.query(PRIVATE_CONTACTS_TABLE, sProjection, THREAD_ID + "=?",
                new String[]{String.valueOf(threadId)}, null, null, null);
        if (cursor == null) {
            return false;
        }
        if (cursor.getCount() > 0) {
            insertSuccess = true;
        } else {
            ContentValues values = new ContentValues();
            values.put(THREAD_ID, threadId);
            values.put(PHONE_NUMBER, recipient);
            insertSuccess = db.insert(PRIVATE_CONTACTS_TABLE, null, values) >= 0;
        }
        cursor.close();

        if (shouldMoveMessages) {
            Cursor cursor1 = db.query(DatabaseHelper.CONVERSATIONS_TABLE, new String[]{"count(*)"},
                    DatabaseHelper.ConversationColumns.SMS_THREAD_ID + "=?",
                    new String[]{String.valueOf(threadId)}, null, null, null);
            int count = 0;
            if (cursor1 != null) {
                if (cursor1.moveToFirst()) {
                    count = cursor1.getInt(0);
                }
                cursor1.close();
            }
            //if count > 0, move messages to private box;
            if (count > 0) {
                if (shouldSendNotification) {
                    HSGlobalNotificationCenter.sendNotification(ContactsSelectActivity.EVENT_MESSAGES_MOVE_START);
                }
                String conversationId = BugleDatabaseOperations.getExistingConversation(db, threadId, false);
                if (conversationId != null) {
                    MoveConversationToPrivateBoxAction.move(conversationId);
                }
            }
        }
        return insertSuccess;
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
