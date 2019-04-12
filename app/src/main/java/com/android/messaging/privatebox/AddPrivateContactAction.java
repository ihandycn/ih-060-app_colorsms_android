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
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.util.Notifications;
import com.superapps.util.Threads;

import static com.android.messaging.privatebox.PrivateContactsManager.PHONE_NUMBER;
import static com.android.messaging.privatebox.PrivateContactsManager.PRIVATE_CONTACTS_TABLE;
import static com.android.messaging.privatebox.PrivateContactsManager.THREAD_ID;
import static com.android.messaging.privatebox.PrivateContactsManager.sProjection;

public class AddPrivateContactAction extends Action {

    public static void addPrivateContact(final long threadId, final String recipient) {
        new AddPrivateContactAction(threadId, recipient).start();
    }

    private static final String KEY_THREAD_ID = "thread_id";
    private static final String KEY_RECIPIENT = "recipient";

    private AddPrivateContactAction(long threadId, String recipient) {
        super();
        actionParameters.putLong(KEY_THREAD_ID, threadId);
        actionParameters.putString(KEY_RECIPIENT, recipient);
    }

    @Override
    protected Object executeAction() {
        long threadId = actionParameters.getLong(KEY_THREAD_ID);
        String recipient = actionParameters.getString(KEY_RECIPIENT);
        addThreadId(threadId, recipient);
        return null;
    }

    public boolean addThreadId(long threadId, String recipient) {
        boolean insertSuccess;
        DatabaseWrapper db = DataModel.get().getDatabase();
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

        ContentValues privateValues = new ContentValues();
        privateValues.put(DatabaseHelper.ConversationColumns.IS_PRIVATE, 1);
        int count = db.update(DatabaseHelper.CONVERSATIONS_TABLE, privateValues,
                DatabaseHelper.ConversationColumns.SMS_THREAD_ID + "=?",
                new String[]{String.valueOf(threadId)});
        //if count > 0, move messages to private box;
        if (count > 0) {
            HSGlobalNotificationCenter.sendNotification(ContactsSelectActivity.EVENT_MESSAGES_MOVE_START);
            String conversationId = BugleDatabaseOperations.getExistingConversation(db, threadId, false);
            if (conversationId != null) {
                MoveConversationToPrivateBoxAction.makeConversationPrivate(conversationId);
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
