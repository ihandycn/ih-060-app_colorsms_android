package com.android.messaging.privatebox;

import android.database.Cursor;
import android.net.Uri;

import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;

public class PrivateMessageManager {
    private static PrivateMessageManager sInstance = new PrivateMessageManager();

    public static PrivateMessageManager getInstance() {
        return sInstance;
    }

    private PrivateMessageManager() {

    }

    public boolean isPrivateConversationId(String conversationId) {
        DatabaseWrapper db = DataModel.get().getDatabase();
//        Cursor cursor = db.query(DatabaseHelper.CONVERSATIONS_TABLE,
//                new String[]{DatabaseHelper.ConversationColumns.IS_PRIVATE},
//                DatabaseHelper.ConversationColumns._ID + "=?", new String[]{conversationId},
//                null, null, null);
//        int privateValue = 0;
//        if (cursor != null && cursor.moveToFirst()) {
//            privateValue = cursor.getInt(0);
//        }
//        if (cursor != null) {
//            cursor.close();
//        }
//        return privateValue == 1;
        long threadId = BugleDatabaseOperations.getThreadId(db, conversationId);
        return PrivateContactsManager.getInstance().isPrivateThreadId(threadId);
    }

    public boolean isPrivateUri(String messageUri) {
        if (messageUri != null) {
            return messageUri.contains(PrivateMessageContentProvider.CONTENT_AUTHORITY);
        }
        return false;
    }

    public boolean isPrivateThreadId(long threadId) {
        return PrivateContactsManager.getInstance().isPrivateThreadId(threadId);
    }
}
