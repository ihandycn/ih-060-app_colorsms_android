package com.android.messaging.ui.sendmessagesdelay;

import java.util.HashMap;

public class SendMessagesDelayManager {

    private static HashMap<String, SendMessagesDelayData> sendMessagesDelayHashmap = new HashMap<>();

    public static void putSendMessagesDelayValue(String converstionId, SendMessagesDelayData sendMessagesDelayData ) {
        sendMessagesDelayHashmap.put(converstionId, sendMessagesDelayData);
    }


    public static SendMessagesDelayData getSendMessagesDelayValue(String conversationId) {
        return sendMessagesDelayHashmap.get(conversationId);
    }

    public static void remove(String conversationId) {
        sendMessagesDelayHashmap.remove(conversationId);
    }

    public static class SendMessagesDelayData {

        private Runnable runnable;
        private Long systemTime;
        private boolean isFragmentDestroyed;

        public boolean isFragmentDestroyed() {
            return isFragmentDestroyed;
        }

        public void setFragmentDestroyed(boolean fragmentDestroyed) {
            isFragmentDestroyed = fragmentDestroyed;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        public void setRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        public Long getSystemTime() {
            return systemTime;
        }

        public void setSystemTime(Long systemTime) {
            this.systemTime = systemTime;
        }

    }
}
