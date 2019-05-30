package com.android.messaging.ui.senddelaymessages;

import java.util.HashMap;

public class SendDelayMessagesManager {

    private static HashMap<String, SendDelayMessagesData> sSendDelayMessagesHashMap = new HashMap<>();

    public static SendDelayMessagesData getIncompleteSendingDelayMessagesAction(String conversationId) {
        return sSendDelayMessagesHashMap.get(conversationId);
    }

    public static void remove(String conversationId) {
        sSendDelayMessagesHashMap.remove(conversationId);
    }

    public static void insertIncompleteSendingDelayMessagesAction(String conversationId, long sendDelayAnimationStartTime, Runnable sendDelayRunnable) {
        SendDelayMessagesManager.SendDelayMessagesData sendDelayMessagesData = new SendDelayMessagesManager.SendDelayMessagesData(sendDelayRunnable, sendDelayAnimationStartTime);
        sSendDelayMessagesHashMap.put(conversationId, sendDelayMessagesData);
    }

    public static class SendDelayMessagesData {

        private Runnable mSendDelayMessagesRunnable;
        private long mSendDelayMessagesAnimationStartTime;

        private SendDelayMessagesData(Runnable mSendDelayMessagesRunnable, long mSendDelayMessagesAnimationStartTime) {
            this.mSendDelayMessagesRunnable = mSendDelayMessagesRunnable;
            this.mSendDelayMessagesAnimationStartTime = mSendDelayMessagesAnimationStartTime;
        }

        public Runnable getSendDelayMessagesRunnable() {
            return mSendDelayMessagesRunnable;
        }

        public long getLastSendDelayActionStartSystemTime() {
            return mSendDelayMessagesAnimationStartTime;
        }
    }
}
