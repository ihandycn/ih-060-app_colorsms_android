package com.android.messaging.ui.sendmessagesdelay;

import java.util.HashMap;

public class SendMessagesDelayManager {

    private static HashMap<String, SendMessagesDelayData> sSendMessagesDelayHashmap = new HashMap<>();

    public static void putSendMessagesDelayValue(String converstionId, SendMessagesDelayData sendMessagesDelayData ) {
        sSendMessagesDelayHashmap.put(converstionId, sendMessagesDelayData);
    }


    public static SendMessagesDelayData getSendMessagesDelayValue(String conversationId) {
        return sSendMessagesDelayHashmap.get(conversationId);
    }

    public static void remove(String conversationId) {
        sSendMessagesDelayHashmap.remove(conversationId);
    }

    public static class SendMessagesDelayData {

        private Runnable mRunnable;
        private Long mLastSendDelayActionStartSystemTime;
        private boolean isFragmentDestroyed;

        public boolean isFragmentDestroyed() {
            return isFragmentDestroyed;
        }

        public void setFragmentDestroyed(boolean fragmentDestroyed) {
            isFragmentDestroyed = fragmentDestroyed;
        }

        public Runnable getRunnable() {
            return mRunnable;
        }

        public void setRunnable(Runnable runnable) {
            this.mRunnable = runnable;
        }

        public Long getLastSendDelayActionStartSystemTime() {
            return mLastSendDelayActionStartSystemTime;
        }

        public void setLastSendDelayActionStartSystemTime(Long lastSendDelayActionStartSystemTime) {
            this.mLastSendDelayActionStartSystemTime = lastSendDelayActionStartSystemTime;
        }

    }
}
