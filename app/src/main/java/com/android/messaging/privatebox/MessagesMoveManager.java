package com.android.messaging.privatebox;

import com.superapps.util.Threads;

import java.lang.ref.WeakReference;
import java.util.List;

public class MessagesMoveManager {

    public interface MessagesMoveListener {
        void onMoveStart();

        void onMoveEnd();
    }

    public static void moveConversations(List<String> conversationIdList,
                                         boolean moveToTelephony, MessagesMoveListener listener) {
        WeakReference<MessagesMoveListener> reference = new WeakReference<>(listener);
        listener.onMoveStart();
        if (moveToTelephony) {
            for (String conversationId : conversationIdList) {
                MoveConversationToTelephonyAction.moveToTelephony(conversationId);
            }
            if (reference.get() != null) {
                reference.get().onMoveEnd();
            }
        } else {
            for (String conversationId : conversationIdList) {
                MoveConversationToPrivateBoxAction.makeConversationPrivate(conversationId);
            }
            if (reference.get() != null) {
                reference.get().onMoveEnd();
            }
        }
    }
}
