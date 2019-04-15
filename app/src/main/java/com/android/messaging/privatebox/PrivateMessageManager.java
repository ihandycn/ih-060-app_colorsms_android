package com.android.messaging.privatebox;

import android.text.TextUtils;

import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;

public class PrivateMessageManager {
    private static PrivateMessageManager sInstance = new PrivateMessageManager();

    public static PrivateMessageManager getInstance() {
        return sInstance;
    }

    private PrivateMessageManager() {

    }

    public boolean isPrivateConversationId(String conversationId) {
        if (TextUtils.isEmpty(conversationId)) {
            return false;
        }
        DatabaseWrapper db = DataModel.get().getDatabase();
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
