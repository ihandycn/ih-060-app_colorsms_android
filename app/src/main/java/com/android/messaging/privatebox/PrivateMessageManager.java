package com.android.messaging.privatebox;

import android.text.TextUtils;

import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.sms.MmsSmsUtils;
import com.android.messaging.sms.MmsUtils;
import com.ihs.app.framework.HSApplication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
        List<String> recipients = BugleDatabaseOperations.getRecipientsForConversation(db, conversationId);
        return PrivateContactsManager.getInstance().isPrivateRecipient(recipients);
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


    public long getValidThreadId(long threadId, DatabaseWrapper db, String conversationId) {
        List<String> recipientList = MmsUtils.getRecipientsByThread(threadId);
        if (recipientList != null && recipientList.size() > 0) {
            return threadId;
        } else {
            if (TextUtils.isEmpty(conversationId)) {
                return threadId;
            }
            List<String> recipients = BugleDatabaseOperations.getRecipientsForConversation(db, conversationId);
            return MmsSmsUtils.Threads.getOrCreateThreadId(HSApplication.getContext(), new HashSet<>(recipients));
        }
    }
}
