package com.android.messaging.ui.senddelaymessages;

import com.android.messaging.datamodel.action.InsertNewMessageAction;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.ui.appsettings.SendDelaySettings;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.PhoneUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.Threads;

import java.util.HashMap;

public class SendDelayMessagesManager {
    public static final String DELAYED_SENDING_MESSAGE_COMPLETE = "delayed.sending.message.complete";
    public static final String BUNDLE_KEY_CONVERSATION_ID = "bundle.key.conversation.id";

    public static void sendMessageWithDelay(MessageData message,
                                            String conversationId,
                                            boolean isDefaultSelf) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!OsUtil.isAtLeastL_MR1() || message.getSelfId() == null) {
                    InsertNewMessageAction.insertNewMessage(message);
                } else {
                    final int systemDefaultSubId = PhoneUtils.getDefault().getDefaultSmsSubscriptionId();
                    if (systemDefaultSubId != ParticipantData.DEFAULT_SELF_SUB_ID && isDefaultSelf) {
                        // Lock the sub selection to the system default SIM as soon as the user clicks on
                        // the send button to avoid races between this and when InsertNewMessageAction is
                        // actually executed on the data model thread, during which the user can potentially
                        // change the system default SIM in Settings.
                        InsertNewMessageAction.insertNewMessage(message, systemDefaultSubId);
                    } else {
                        InsertNewMessageAction.insertNewMessage(message);
                    }
                }
                HSBundle bundle = new HSBundle();
                bundle.putString(BUNDLE_KEY_CONVERSATION_ID, conversationId);
                HSGlobalNotificationCenter.sendNotification(DELAYED_SENDING_MESSAGE_COMPLETE, bundle);
                remove(conversationId);
            }
        };

        Threads.postOnMainThreadDelayed(runnable, SendDelaySettings.getSendDelayInSecs() * 1000);
        SendMessageWork sendDelayMessagesData = new SendMessageWork(runnable, System.currentTimeMillis());
        sSendDelayMessagesHashMap.put(conversationId, sendDelayMessagesData);
    }

    private static HashMap<String, SendMessageWork> sSendDelayMessagesHashMap = new HashMap<>();

    public static SendMessageWork getIncompleteSendingDelayMessagesAction(String conversationId) {
        return sSendDelayMessagesHashMap.get(conversationId);
    }

    public static void remove(String conversationId) {
        SendMessageWork work = sSendDelayMessagesHashMap.get(conversationId);
        if (work != null) {
            Threads.removeOnMainThread(work.getSendDelayMessagesRunnable());
        }
        sSendDelayMessagesHashMap.remove(conversationId);
    }

    public static class SendMessageWork implements Runnable {

        private Runnable mSendDelayMessagesRunnable;
        private long mSendDelayMessagesAnimationStartTime;

        private SendMessageWork(Runnable sendDelayMessagesRunnable, long sendDelayMessagesAnimationStartTime ) {
            mSendDelayMessagesRunnable = sendDelayMessagesRunnable;
            mSendDelayMessagesAnimationStartTime = sendDelayMessagesAnimationStartTime;
        }

        public Runnable getSendDelayMessagesRunnable() {
            return mSendDelayMessagesRunnable;
        }

        public long getLastSendDelayActionStartSystemTime() {
            return mSendDelayMessagesAnimationStartTime;
        }

        @Override
        public void run() {
            mSendDelayMessagesRunnable.run();
        }
    }
}
