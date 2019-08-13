package com.android.messaging.scheduledmessage;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.action.Action;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.ui.conversationlist.ArchivedConversationListActivity;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.PhoneUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.Threads;

import java.util.ArrayList;

import static com.android.messaging.ui.senddelaymessages.SendDelayMessagesManager.BUNDLE_KEY_CONVERSATION_ID;
import static com.android.messaging.ui.senddelaymessages.SendDelayMessagesManager.DELAYED_SENDING_MESSAGE_COMPLETE;

public class InsertScheduledMessageAction extends Action implements Parcelable {

    public static void insertNewMessage(final MessageData message, boolean isDefaultSelf) {
        InsertScheduledMessageAction action;
        if (!OsUtil.isAtLeastL_MR1() || message.getSelfId() == null) {
            action = new InsertScheduledMessageAction(message);
        } else {
            final int systemDefaultSubId = PhoneUtils.getDefault().getDefaultSmsSubscriptionId();
            if (systemDefaultSubId != ParticipantData.DEFAULT_SELF_SUB_ID && isDefaultSelf) {
                action = new InsertScheduledMessageAction(message, systemDefaultSubId);
            } else {
                action = new InsertScheduledMessageAction(message);
            }
        }
        action.start();
    }

    private static final String KEY_SUB_ID = "sub_id";
    private static final String KEY_MESSAGE = "message";

    private InsertScheduledMessageAction(final MessageData message) {
        this(message, ParticipantData.DEFAULT_SELF_SUB_ID);
    }

    private InsertScheduledMessageAction(final MessageData message, final int subId) {
        super();
        actionParameters.putParcelable(KEY_MESSAGE, message);
        actionParameters.putInt(KEY_SUB_ID, subId);
    }

    /**
     * Add message to database in pending state and queue actual sending
     */
    @Override
    protected Object executeAction() {
        MessageData message = actionParameters.getParcelable(KEY_MESSAGE);
        final DatabaseWrapper db = DataModel.get().getDatabase();
        final String conversationId = message.getConversationId();

        final ParticipantData self = getSelf(db, conversationId, message);
        if (self == null) {
            return null;
        }
        message.bindSelfId(self.getId());
        if (message.getParticipantId() == null) {
            message.bindParticipantId(self.getId());
        }

        long timestamp = System.currentTimeMillis();
        final ArrayList<String> recipients =
                BugleDatabaseOperations.getRecipientsForConversation(db, conversationId);
        if (recipients.size() < 1) {
            return null;
        }

        BugleNotifications.markMessagesAsRead(conversationId);

        final boolean isSms = (message.getProtocol() == MessageData.PROTOCOL_SMS);
        if (isSms) {
            if (recipients.size() > 1) {
                timestamp++;
            }
            insertScheduledMessage(message, timestamp);
        } else {
            final long timestampRoundedToSecond = 1000 * ((timestamp + 500) / 1000);
            insertScheduledMessage(message, timestampRoundedToSecond);
            BugleDatabaseOperations.updateDraftMessageData(db, conversationId,
                    message, BugleDatabaseOperations.UPDATE_MODE_CLEAR_DRAFT);
        }

        if (!TextUtils.isEmpty(message.getMessageId())) {
            MessageScheduleManager.addScheduledTask(Long.parseLong(message.getMessageId()), message.getScheduledTime());
        }

        MessagingContentProvider.notifyConversationListChanged();
        Threads.postOnMainThread(() -> {
            HSBundle bundle = new HSBundle();
            bundle.putString(BUNDLE_KEY_CONVERSATION_ID, conversationId);
            HSGlobalNotificationCenter.sendNotification(DELAYED_SENDING_MESSAGE_COMPLETE, bundle);
        });
        return message;
    }

    private ParticipantData getSelf(
            final DatabaseWrapper db, final String conversationId, final MessageData message) {
        ParticipantData self;
        final int requestedSubId = actionParameters.getInt(
                KEY_SUB_ID, ParticipantData.DEFAULT_SELF_SUB_ID);
        if (requestedSubId != ParticipantData.DEFAULT_SELF_SUB_ID) {
            self = BugleDatabaseOperations.getOrCreateSelf(db, requestedSubId);
        } else {
            String selfId = message.getSelfId();
            if (selfId == null) {
                final ConversationListItemData conversation =
                        ConversationListItemData.getExistingConversation(db, conversationId);
                if (conversation != null) {
                    selfId = conversation.getSelfId();
                } else {
                    LogUtil.w(LogUtil.BUGLE_DATAMODEL_TAG, "Conversation " + conversationId +
                            "already deleted before sending draft message " +
                            message.getMessageId() + ". Aborting InsertNewMessageAction.");
                    return null;
                }
            }

            final ParticipantData unboundSelf = BugleDatabaseOperations.getExistingParticipant(
                    db, selfId);
            if (unboundSelf.getSubId() == ParticipantData.DEFAULT_SELF_SUB_ID
                    && OsUtil.isAtLeastL_MR1()) {
                final int defaultSubId = PhoneUtils.getDefault().getDefaultSmsSubscriptionId();
                self = BugleDatabaseOperations.getOrCreateSelf(db, defaultSubId);
            } else {
                self = unboundSelf;
            }
        }
        return self;
    }

    private String insertScheduledMessage(final MessageData message, final long timestamp) {
        final DatabaseWrapper db = DataModel.get().getDatabase();
        db.beginTransaction();
        try {
            message.updateScheduledMessageStatus(timestamp);

            BugleDatabaseOperations.insertNewMessageInTransaction(db, message);

            ArchivedConversationListActivity.logUnarchiveEvent(db, message.getConversationId(), "schedule_message");

            BugleDatabaseOperations.updateConversationMetadataInTransaction(db,
                    message.getConversationId(), message.getMessageId(), timestamp,
                    false, false);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        MessagingContentProvider.notifyMessagesChanged(message.getConversationId());
        MessagingContentProvider.notifyPartsChanged();

        return message.getMessageId();
    }

    private InsertScheduledMessageAction(final Parcel in) {
        super(in);
    }

    public static final Creator<InsertScheduledMessageAction> CREATOR
            = new Creator<InsertScheduledMessageAction>() {
        @Override
        public InsertScheduledMessageAction createFromParcel(final Parcel in) {
            return new InsertScheduledMessageAction(in);
        }

        @Override
        public InsertScheduledMessageAction[] newArray(final int size) {
            return new InsertScheduledMessageAction[size];
        }
    };

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        writeActionToParcel(parcel, flags);
    }
}
