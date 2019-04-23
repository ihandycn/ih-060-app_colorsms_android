package com.android.messaging.privatebox;

import android.content.ContentValues;
import android.database.Cursor;

import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.sms.MmsSmsUtils;
import com.ihs.app.framework.HSApplication;

public class PrivateContactsManager {
    public static final PrivateContactsManager sInstance = new PrivateContactsManager();

    public static PrivateContactsManager getInstance() {
        return sInstance;
    }

    private PrivateContactsManager() {

    }

    public boolean isPrivateRecipient(String recipient) {
        //todo: check permission before use getOrCreateThreadId
        long threadId = MmsSmsUtils.Threads.getOrCreateThreadId(HSApplication.getContext(), recipient);
        if (threadId < 0) {
            return false;
        }
        return isPrivateThreadId(threadId);
    }

    public boolean isPrivateThreadId(long threadId) {
        DatabaseWrapper db = DataModel.get().getDatabase();
        Cursor cursor = db.query(PRIVATE_CONTACTS_TABLE, sProjection, THREAD_ID + "=?",
                new String[]{String.valueOf(threadId)}, null, null, null);
        if (cursor == null) {
            return false;
        } else {
            boolean isPrivate = cursor.getCount() > 0;
            cursor.close();
            return isPrivate;
        }
    }

    public void removeThreadId(long threadId) {
        DatabaseWrapper db = DataModel.get().getDatabase();
        db.delete(PRIVATE_CONTACTS_TABLE, THREAD_ID + "=?", new String[]{String.valueOf(threadId)});
    }

    private void addThreadId(long threadId) {
        //check if the thread_id is in the table
        DatabaseWrapper db = DataModel.get().getDatabase();
        Cursor cursor = db.query(PRIVATE_CONTACTS_TABLE, sProjection, THREAD_ID + "=?",
                new String[]{String.valueOf(threadId)}, null, null, null);
        if (cursor == null) {
            return;
        }
        if (cursor.getCount() <= 0) {
            ContentValues values = new ContentValues();
            values.put(THREAD_ID, threadId);
            db.insert(PRIVATE_CONTACTS_TABLE, null, values);
        }
        cursor.close();
    }

    public void updatePrivateContactsByConversationId(String conversationId, boolean isPrivate) {
        long threadId = BugleDatabaseOperations.getThreadId(DataModel.get().getDatabase(), conversationId);
        if (isPrivate) {
            //AddPrivateContactAction.addPrivateThreadId(new long[]{threadId});
            addThreadId(threadId);
        } else {
            removeThreadId(threadId);
        }
    }

    public static final String _ID = "_id";
    public static final String PHONE_NUMBER = "recipient";
    public static final String CONTACT_ID = "contact_id";
    public static final String THREAD_ID = "thread_id";

    public static final String[] sProjection = {
            _ID, PHONE_NUMBER, CONTACT_ID, THREAD_ID
    };

    public static final String PRIVATE_CONTACTS_TABLE = "private_contact_table";

    public static final String CREATE_PRIVATE_CONTACTS_TABLE_SQL =
            "CREATE TABLE " + PRIVATE_CONTACTS_TABLE + "("
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + PHONE_NUMBER + " TEXT, "
                    + CONTACT_ID + " INT, "
                    + THREAD_ID + " INT )";
}
