package com.android.messaging.ui.senddelaymessages;

import java.util.HashMap;

public class SendDelayMessagesManager {

    private static HashMap<String, SendDelayMessagesData> sSendDelayMessagesHashMap = new HashMap<>();

    public static void putSendDelayMessagesValue(String conversationId, SendDelayMessagesData sendDelayMessagesData ) {
        sSendDelayMessagesHashMap.put(conversationId, sendDelayMessagesData);
    }


    public static SendDelayMessagesData getIncompleteSendingDelayMessagesAction(String conversationId) {
        return sSendDelayMessagesHashMap.get(conversationId);
    }

    public static void remove(String conversationId) {
        sSendDelayMessagesHashMap.remove(conversationId);
    }

    public static class SendDelayMessagesData {

        private Runnable mSendDelayMessagesRunnable;
        private long mSendDelayMessagesAnimationStartTime;

        public Runnable getSendDelayMessagesRunnable() {
            return mSendDelayMessagesRunnable;
        }

        public void setSendDelayMessagesRunnable(Runnable sendDelayMessagesRunnable) {
            this.mSendDelayMessagesRunnable = sendDelayMessagesRunnable;
        }

        public long getLastSendDelayActionStartSystemTime() {
            return mSendDelayMessagesAnimationStartTime;
        }

        public void setLastSendDelayActionStartSystemTime(long sendDelayMessagesAnimationStartTime) {
            this.mSendDelayMessagesAnimationStartTime = sendDelayMessagesAnimationStartTime;
        }

    }
}
