package com.android.messaging.privatebox;

import java.util.List;

public class MessagesMoveManager {

    public interface MessagesMoveListener {
        void onMoveStart();

        void onMoveEnd();
    }

    public static void moveConversations(List<String> conversationIdList,
                                         boolean moveToTelephony, MessagesMoveListener listener) {
        listener.onMoveStart();
        if (moveToTelephony) {
            for (String conversationId : conversationIdList) {
                MoveConversationToTelephonyAction.moveToTelephony(conversationId);
            }
            listener.onMoveEnd();
        } else {
            for (String conversationId : conversationIdList) {
                MoveConversationToPrivateBoxAction.makeConversationPrivate(conversationId);
            }
            listener.onMoveEnd();
        }
    }
}
