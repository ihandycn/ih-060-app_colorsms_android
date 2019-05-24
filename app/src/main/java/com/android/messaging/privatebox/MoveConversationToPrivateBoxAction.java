package com.android.messaging.privatebox;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.android.messaging.R;
import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseHelper.MessageColumns;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.action.Action;
import com.android.messaging.util.Assert;
import com.android.messaging.util.PhoneUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.util.Toasts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MoveConversationToPrivateBoxAction extends Action implements Parcelable {
    private static final String KEY_CONVERSATION_ID_LIST = "conversation_id";
    private static final String KEY_UPDATE_PRIVATE_CONTACT = "update_contact";
    private static final String KEY_START_NOTIFICATION = "key_start_notification";
    private static final String KEY_END_NOTIFICATION = "key_end_notification";
    private static final String KEY_CONTACT_LIST = "key_contact_list";

    public static void moveAndUpdatePrivateContact(final List<String> conversationIdList, final String moveStartNotification,
                                                   final String moveEndNotification) {
        final MoveConversationToPrivateBoxAction action = new MoveConversationToPrivateBoxAction(conversationIdList);
        action.actionParameters.putBoolean(KEY_UPDATE_PRIVATE_CONTACT, true);
        action.actionParameters.putString(KEY_START_NOTIFICATION, moveStartNotification);
        action.actionParameters.putString(KEY_END_NOTIFICATION, moveEndNotification);
        action.start();
    }

    public static void moveByContact(List<String> contactList, final String moveStartNotification,
                                     final String moveEndNotification) {
        final MoveConversationToPrivateBoxAction action = new MoveConversationToPrivateBoxAction();
        action.actionParameters.putString(KEY_START_NOTIFICATION, moveStartNotification);
        action.actionParameters.putString(KEY_END_NOTIFICATION, moveEndNotification);
        action.actionParameters.putStringArrayList(KEY_CONTACT_LIST, (ArrayList<String>) contactList);
        action.start();
    }

    private MoveConversationToPrivateBoxAction(final List<String> conversationId) {
        super();
        actionParameters.putStringArrayList(KEY_CONVERSATION_ID_LIST, (ArrayList<String>) conversationId);
    }

    private MoveConversationToPrivateBoxAction() {
        super();
    }

    @Override
    protected Object executeAction() {
        final List<String> conversationIdList;
        boolean updatePrivateContact = actionParameters.getBoolean(KEY_UPDATE_PRIVATE_CONTACT, true);
        final List<String> messagesList = new ArrayList<>();
        String startNotification = actionParameters.getString(KEY_START_NOTIFICATION);
        String endNotification = actionParameters.getString(KEY_END_NOTIFICATION);

        if (actionParameters.containsKey(KEY_CONTACT_LIST)) {
            conversationIdList = new ArrayList<>();
            List<String> contactList = actionParameters.getStringArrayList(KEY_CONTACT_LIST);
            assert contactList != null;
            for (String recipient : contactList) {
                String phoneNumBySim = PhoneUtils.getDefault().getCanonicalBySimLocale(recipient);
                String phoneNumBySystem = PhoneUtils.getDefault().getCanonicalBySystemLocale(recipient);

                String participantId = BugleDatabaseOperations.getParticipantIdByName(phoneNumBySim);
                String conversationId =
                        BugleDatabaseOperations.getConversationIdForParticipantsGroup(Collections.singletonList(participantId));
                if (!TextUtils.isEmpty(conversationId)) {
                    conversationIdList.add(conversationId);
                }

                if (!phoneNumBySim.equals(phoneNumBySystem)) {
                    String conversationId1 =
                            BugleDatabaseOperations.getConversationIdForParticipantsGroup(
                                    Collections.singletonList(
                                            BugleDatabaseOperations.getParticipantIdByName(phoneNumBySim)));
                    if (!TextUtils.isEmpty(conversationId1)) {
                        conversationIdList.add(conversationId1);
                    }
                }
            }
            if (conversationIdList.size() == 0) {
                Toasts.showToast(R.string.private_box_add_to_success);
                if (!TextUtils.isEmpty(endNotification)) {
                    HSGlobalNotificationCenter.sendNotification(endNotification);
                }
                return null;
            }
        } else {
            conversationIdList = actionParameters.getStringArrayList(KEY_CONVERSATION_ID_LIST);
        }

        assert conversationIdList != null;
        for (String conversationId : conversationIdList) {
            if (!TextUtils.isEmpty(conversationId)) {
                if (BugleDatabaseOperations.updateConversationPrivateStatue(conversationId, true)) {
                    if (updatePrivateContact) {
                        PrivateContactsManager.getInstance().updatePrivateContactsByConversationId(conversationId, true);
                    }

                    addMessagesByConversationId(conversationId, messagesList);
                }
            }
        }
        MessagingContentProvider.notifyConversationListChanged();

        if (messagesList.size() == 0) {
            Toasts.showToast(R.string.private_box_add_to_success);
            if (!TextUtils.isEmpty(endNotification)) {
                HSGlobalNotificationCenter.sendNotification(endNotification);
            }
        } else {
            MoveMessageToPrivateBoxAction.moveMessagesToPrivateBox(messagesList, startNotification, endNotification);
        }
        return null;
    }

    private void addMessagesByConversationId(final String conversationId, final List<String> messageList) {
        final DatabaseWrapper db = DataModel.get().getDatabase();
        Assert.notNull(conversationId);

        Cursor cursor = db.query(DatabaseHelper.MESSAGES_TABLE,
                new String[]{MessageColumns._ID, MessageColumns.SMS_MESSAGE_URI},
                MessageColumns.CONVERSATION_ID + "=?",
                new String[]{conversationId},
                null, null, null);

        if (cursor == null) {
            return;
        }
        while (cursor.moveToNext()) {
            String messageUri = cursor.getString(1);
            if (!PrivateMessageManager.getInstance().isPrivateUri(messageUri)) {
                String messageId = cursor.getString(0);
                if (!TextUtils.isEmpty(messageId)) {
                    messageList.add(messageId);
                }
            }
        }
        cursor.close();
    }

    private MoveConversationToPrivateBoxAction(final Parcel in) {
        super(in);
    }

    public static final Creator<MoveConversationToPrivateBoxAction> CREATOR
            = new Creator<MoveConversationToPrivateBoxAction>() {
        @Override
        public MoveConversationToPrivateBoxAction createFromParcel(final Parcel in) {
            return new MoveConversationToPrivateBoxAction(in);
        }

        @Override
        public MoveConversationToPrivateBoxAction[] newArray(final int size) {
            return new MoveConversationToPrivateBoxAction[size];
        }
    };

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        writeActionToParcel(parcel, flags);
    }
}
