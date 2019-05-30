package com.android.messaging.ui.sendmessagesdelay;

import java.util.HashMap;

public class SendMessagesDelayManager {

    private static HashMap<String, SendMessagesDelayData> sSendMessagesDelayHashMap = new HashMap<>();

    public static void putSendMessagesDelayValue(String conversationId, SendMessagesDelayData sendMessagesDelayData ) {
        sSendMessagesDelayHashMap.put(conversationId, sendMessagesDelayData);
    }


    public static SendMessagesDelayData getSendMessagesDelayValue(String conversationId) {
        return sSendMessagesDelayHashMap.get(conversationId);
    }

    public static void remove(String conversationId) {
        sSendMessagesDelayHashMap.remove(conversationId);
    }

    public static class SendMessagesDelayData {

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
