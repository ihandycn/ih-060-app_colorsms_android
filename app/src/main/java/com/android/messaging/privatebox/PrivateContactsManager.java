package com.android.messaging.privatebox;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.sms.MmsSmsUtils;
import com.android.messaging.util.CheckPermissionUtil;
import com.android.messaging.util.PhoneUtils;
import com.google.common.base.Joiner;
import com.ihs.app.framework.HSApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class PrivateContactsManager {
    public static final PrivateContactsManager sInstance = new PrivateContactsManager();

    public static PrivateContactsManager getInstance() {
        return sInstance;
    }

    private PrivateContactsManager() {

    }

    public boolean isPrivateRecipient(List<String> recipients) {

        List<String> recipientList = new ArrayList<>();
        for (String recipient : recipients) {
            recipientList.add(PhoneUtils.getDefault().getCanonicalBySimLocale(recipient.trim()));
        }
        Collections.sort(recipientList, String::compareTo);
        String recipientStr = Joiner.on(",").join(recipientList);

        long threadId = -1;
        try {
            threadId = MmsSmsUtils.Threads.getOrCreateThreadId(HSApplication.getContext(), new HashSet<>(recipients));
        } catch (Exception e) {

        }
        DatabaseWrapper db = DataModel.get().getDatabase();
        Cursor cursor;

        try {
            cursor = db.query(PRIVATE_CONTACTS_TABLE, sProjection,
                    RECIPIENTS + "=? or " + THREAD_ID + "=?",
                    new String[]{recipientStr, String.valueOf(threadId)}, null, null, null);
        } catch (Exception e) {
            return false;
        }

        if (cursor == null) {
            return false;
        } else {
            boolean isPrivate = cursor.getCount() > 0;
            cursor.close();
            return isPrivate;
        }
    }

    public List<String> getPrivateRecipientList() {
        List<String> recipients = new ArrayList<>();
        DatabaseWrapper db = DataModel.get().getDatabase();
        Cursor cursor = db.query(PRIVATE_CONTACTS_TABLE, new String[]{RECIPIENTS},
                null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                recipients.add(cursor.getString(0));
            }
            cursor.close();
        }
        return recipients;
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

    public void removeThreadId(long threadId, List<String> recipients) {
        DatabaseWrapper db = DataModel.get().getDatabase();

        String recipientStr = null;
        if (recipients != null) {
            List<String> recipientList = new ArrayList<>();
            for (String recipient : recipients) {
                recipientList.add(PhoneUtils.getDefault().getCanonicalBySimLocale(recipient.trim()));
            }
            Collections.sort(recipientList, String::compareTo);
            recipientStr = Joiner.on(",").join(recipientList);
        }

        if (!TextUtils.isEmpty(recipientStr)) {
            db.delete(PRIVATE_CONTACTS_TABLE,
                    THREAD_ID + "=? or " + RECIPIENTS + "=?",
                    new String[]{String.valueOf(threadId), recipientStr});
        } else {
            db.delete(PRIVATE_CONTACTS_TABLE,
                    THREAD_ID + "=?", new String[]{String.valueOf(threadId)});
        }
    }

    //add from conversation list
    private void addPrivateRecipient(long threadId, List<String> recipients) {
        //check if the thread_id is in the table
        DatabaseWrapper db = DataModel.get().getDatabase();

        String recipientStr = null;
        if (recipients != null) {
            List<String> recipientList = new ArrayList<>();
            for (String recipient : recipients) {
                recipientList.add(PhoneUtils.getDefault().getCanonicalBySimLocale(recipient.trim()));
            }
            Collections.sort(recipientList, String::compareTo);
            recipientStr = Joiner.on(",").join(recipientList);
        }

        Cursor cursor;
        if (!TextUtils.isEmpty(recipientStr)) {
            cursor = db.query(PRIVATE_CONTACTS_TABLE, sProjection,
                    THREAD_ID + "=? and " + RECIPIENTS + "=?",
                    new String[]{String.valueOf(threadId), recipientStr}, null, null, null);
        } else {
            cursor = db.query(PRIVATE_CONTACTS_TABLE, sProjection,
                    THREAD_ID + "=?", new String[]{String.valueOf(threadId)},
                    null, null, null);
        }
        if (cursor == null) {
            return;
        }
        if (cursor.getCount() <= 0) {
            ContentValues values = new ContentValues();
            values.put(THREAD_ID, threadId);
            values.put(RECIPIENTS, recipientStr);
            db.insert(PRIVATE_CONTACTS_TABLE, null, values);
        }
        cursor.close();
    }

    public void updatePrivateContactsByConversationId(String conversationId, boolean isPrivate) {
        long threadId = BugleDatabaseOperations.getThreadId(DataModel.get().getDatabase(), conversationId);
        List<String> recipients = BugleDatabaseOperations.getRecipientsForConversation(
                DataModel.get().getDatabase(), conversationId);
        if (isPrivate) {
            addPrivateRecipient(threadId, recipients);
        } else {
            removeThreadId(threadId, recipients);
        }
    }

    public static final String _ID = "_id";
    public static final String CONTACT_ID = "contact_id";
    public static final String THREAD_ID = "thread_id";
    public static final String RECIPIENTS = "recipients";

    public static final String[] sProjection = {
            _ID, RECIPIENTS, CONTACT_ID, THREAD_ID
    };

    public static final String PRIVATE_CONTACTS_TABLE = "private_contact_table";

    public static final String CREATE_PRIVATE_CONTACTS_TABLE_SQL =
            "CREATE TABLE " + PRIVATE_CONTACTS_TABLE + "("
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + RECIPIENTS + " TEXT, "
                    + CONTACT_ID + " INT, "
                    + THREAD_ID + " INT )";
}
