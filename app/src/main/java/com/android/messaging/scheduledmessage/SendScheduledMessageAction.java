package com.android.messaging.scheduledmessage;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Telephony;
import android.text.TextUtils;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.SyncManager;
import com.android.messaging.datamodel.action.Action;
import com.android.messaging.datamodel.action.SendMessageAction;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.privatebox.PrivateMessageManager;
import com.android.messaging.privatebox.PrivateSmsEntry;
import com.android.messaging.sms.MmsUtils;
import com.android.messaging.ui.conversationlist.ArchivedConversationListActivity;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.PhoneUtils;

import java.util.ArrayList;

public class SendScheduledMessageAction extends Action implements Parcelable {

    public static void sendMessage(final String messageId) {
        SendScheduledMessageAction action = new SendScheduledMessageAction(messageId);
        action.start();
    }

    private static final String KEY_MESSAGE_ID = "message_id";

    private SendScheduledMessageAction(final String messageId) {
        super();
        actionParameters.putString(KEY_MESSAGE_ID, messageId);
    }

    @Override
    protected Object executeAction() {
        String messageId = actionParameters.getString(KEY_MESSAGE_ID);

        DatabaseWrapper db = DataModel.get().getDatabase();
        MessageData message = BugleDatabaseOperations.readMessage(db, messageId);

        final String conversationId = message.getConversationId();

        final long timestamp = System.currentTimeMillis();
        final ArrayList<String> recipients = BugleDatabaseOperations.getRecipientsForConversation(db, conversationId);
        if (recipients.size() < 1) {
            return null;
        }

        final boolean isSms = (message.getProtocol() == MessageData.PROTOCOL_SMS);
        final ParticipantData self = getSelf(db, conversationId, message);
        if (self == null) {
            return null;
        }
        message.bindSelfId(self.getId());
        final int subId = self.getSubId();
        if (isSms) {
            if (recipients.size() > 1) {
                updateBroadcastSmsMessage(conversationId, message, subId, timestamp, recipients);

                for (final String recipient : recipients) {
                    // Start actual sending
                    MessageData messageData = insertSendingSmsMessage(message, subId, recipient,
                            timestamp, conversationId);
                    SendMessageAction.queueForSendInBackground(messageData.getMessageId(), this);
                }

                MessagingContentProvider.notifyConversationListChanged();
                return message;
            } else {
                updateSingleSendingSmsMessage(message, subId, recipients.get(0), timestamp);
            }
        } else {
            updateSendingMmsMessage(conversationId, message, timestamp);
        }
        SendMessageAction.queueForSendInBackground(messageId, this);
        return message;
    }

    private ParticipantData getSelf(
            final DatabaseWrapper db, final String conversationId, final MessageData message) {
        ParticipantData self;
        String selfId = message.getSelfId();
        if (selfId == null) {
            final ConversationListItemData conversation =
                    ConversationListItemData.getExistingConversation(db, conversationId);
            if (conversation != null) {
                selfId = conversation.getSelfId();
            } else {
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
        return self;
    }

    private void updateBroadcastSmsMessage(final String conversationId,
                                           final MessageData message, final int subId, final long laterTimestamp,
                                           final ArrayList<String> recipients) {
        final Context context = Factory.get().getApplicationContext();
        final DatabaseWrapper db = DataModel.get().getDatabase();

        // Inform sync that message is being added at timestamp
        final SyncManager syncManager = DataModel.get().getSyncManager();
        syncManager.onNewMessageInserted(laterTimestamp);

        final long threadId = BugleDatabaseOperations.getThreadId(db, conversationId);
        final String address = TextUtils.join(" ", recipients);

        final String messageText = message.getMessageText();
        // Insert message into telephony database sms message table
        boolean isPrivateMessage = PrivateMessageManager.getInstance().isPrivateConversationId(conversationId);
        final Uri messageUri = MmsUtils.insertSmsMessage(context,
                isPrivateMessage ? PrivateSmsEntry.CONTENT_URI : Telephony.Sms.CONTENT_URI,
                subId,
                address,
                messageText,
                laterTimestamp,
                Telephony.Sms.STATUS_COMPLETE,
                Telephony.Sms.MESSAGE_TYPE_SENT, threadId);
        if (messageUri != null && !TextUtils.isEmpty(messageUri.toString())) {
            db.beginTransaction();
            try {
                message.updateSendingMessage(conversationId, messageUri, laterTimestamp);
                message.markMessageSent(laterTimestamp);

                BugleDatabaseOperations.updateMessageInTransaction(db, message);

                BugleDatabaseOperations.updateConversationMetadataInTransaction(db,
                        conversationId, message.getMessageId(), laterTimestamp,
                        false /* senderBlocked */, false /* shouldAutoSwitchSelfId */);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            MessagingContentProvider.notifyMessagesChanged(conversationId);
            MessagingContentProvider.notifyPartsChanged();
        }
    }

    private MessageData updateSendingMmsMessage(final String conversationId,
                                                final MessageData message, final long timestamp) {
        final DatabaseWrapper db = DataModel.get().getDatabase();
        db.beginTransaction();
        try {
            message.updateSendingMessage(conversationId, null, timestamp);

            BugleDatabaseOperations.updateMessageInTransaction(db, message);
            BugleDatabaseOperations.updateConversationMetadataInTransaction(db,
                    conversationId, message.getMessageId(), timestamp,
                    false, false);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        MessagingContentProvider.notifyMessagesChanged(conversationId);
        MessagingContentProvider.notifyPartsChanged();

        return message;
    }

    private MessageData insertSendingSmsMessage(final MessageData content, final int subId,
                                                final String recipient, final long timestamp,
                                                final String sendingConversationId) {
        final Context context = Factory.get().getApplicationContext();

        // Inform sync that message is being added at timestamp
        final SyncManager syncManager = DataModel.get().getSyncManager();
        syncManager.onNewMessageInserted(timestamp);

        final DatabaseWrapper db = DataModel.get().getDatabase();

        // Send a single message
        long threadId;
        String conversationId;
        if (sendingConversationId == null) {
            // For 1:1 message generated sending broadcast need to look up threadId+conversationId
            threadId = MmsUtils.getOrCreateSmsThreadId(context, recipient);
            conversationId = BugleDatabaseOperations.getOrCreateConversationFromRecipient(
                    db, threadId, false /* sender blocked */,
                    ParticipantData.getFromRawPhoneBySimLocale(recipient, subId));
        } else {
            // Otherwise just look up threadId
            threadId = BugleDatabaseOperations.getThreadId(db, sendingConversationId);
            conversationId = sendingConversationId;
        }

        final String messageText = content.getMessageText();

        // Insert message into telephony database sms message table
        boolean isPrivateMessage = PrivateMessageManager.getInstance().isPrivateConversationId(conversationId);

        final Uri messageUri = MmsUtils.insertSmsMessage(context,
                isPrivateMessage ? PrivateSmsEntry.CONTENT_URI : Telephony.Sms.CONTENT_URI,
                subId,
                recipient,
                messageText,
                timestamp,
                Telephony.Sms.STATUS_NONE,
                Telephony.Sms.MESSAGE_TYPE_SENT, threadId);

        MessageData message = null;
        if (messageUri != null && !TextUtils.isEmpty(messageUri.toString())) {
            db.beginTransaction();
            try {
                message = MessageData.createDraftSmsMessage(conversationId,
                        content.getSelfId(), messageText);
                message.updateSendingMessage(conversationId, messageUri, timestamp);
                message.setIsDeliveryReportOpen(content.getIsDeliveryReportOpen());

                BugleDatabaseOperations.insertNewMessageInTransaction(db, message);

                // Do not update the conversation summary to reflect autogenerated 1:1 messages
                if (sendingConversationId != null) {
                    ArchivedConversationListActivity.logUnarchiveEvent(db, conversationId, "receive_message");

                    BugleDatabaseOperations.updateConversationMetadataInTransaction(db,
                            conversationId, message.getMessageId(), timestamp,
                            false, false);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            MessagingContentProvider.notifyMessagesChanged(conversationId);
            MessagingContentProvider.notifyPartsChanged();
        }

        return message;
    }

    private MessageData updateSingleSendingSmsMessage(final MessageData message, final int subId,
                                                      final String recipient, final long timestamp) {
        final Context context = Factory.get().getApplicationContext();

        // Inform sync that message is being added at timestamp
        final SyncManager syncManager = DataModel.get().getSyncManager();
        syncManager.onNewMessageInserted(timestamp);

        final DatabaseWrapper db = DataModel.get().getDatabase();

        // Send a single message
        String conversationId = message.getConversationId();
        long threadId = BugleDatabaseOperations.getThreadId(db, conversationId);

        final String messageText = message.getMessageText();

        // Insert message into telephony database sms message table
        boolean isPrivateMessage = PrivateMessageManager.getInstance().isPrivateConversationId(conversationId);

        final Uri messageUri = MmsUtils.insertSmsMessage(context,
                isPrivateMessage ? PrivateSmsEntry.CONTENT_URI : Telephony.Sms.CONTENT_URI,
                subId,
                recipient,
                messageText,
                timestamp,
                Telephony.Sms.STATUS_NONE,
                Telephony.Sms.MESSAGE_TYPE_SENT, threadId);

        if (messageUri != null && !TextUtils.isEmpty(messageUri.toString())) {
            db.beginTransaction();
            try {
                message.updateSendingMessage(conversationId, messageUri, timestamp);

                BugleDatabaseOperations.updateMessageInTransaction(db, message);

                BugleDatabaseOperations.updateConversationMetadataInTransaction(db,
                        conversationId, message.getMessageId(), timestamp,
                        false, false);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            MessagingContentProvider.notifyMessagesChanged(conversationId);
            MessagingContentProvider.notifyPartsChanged();
        }

        return message;
    }

    private SendScheduledMessageAction(final Parcel in) {
        super(in);
    }

    public static final Creator<SendScheduledMessageAction> CREATOR
            = new Creator<SendScheduledMessageAction>() {
        @Override
        public SendScheduledMessageAction createFromParcel(final Parcel in) {
            return new SendScheduledMessageAction(in);
        }

        @Override
        public SendScheduledMessageAction[] newArray(final int size) {
            return new SendScheduledMessageAction[size];
        }
    };

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        writeActionToParcel(parcel, flags);
    }
}
