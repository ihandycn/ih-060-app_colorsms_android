package com.android.messaging.privatebox;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.android.messaging.R;
import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.action.Action;
import com.android.messaging.util.Assert;
import com.android.messaging.util.PhoneUtils;
import com.superapps.util.Toasts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MoveRecipientsToTelephonyAction extends Action {

    public static void moveToTelephony(String recipient) {
        MoveRecipientsToTelephonyAction action = new MoveRecipientsToTelephonyAction();
        action.actionParameters.putString(KEY_RECIPIENTS, recipient);
        action.start();
    }

    private static final String KEY_RECIPIENTS = "recipients";

    private MoveRecipientsToTelephonyAction() {
        super();
    }

    @Override
    protected Object executeAction() {
        final String recipient = actionParameters.getString(KEY_RECIPIENTS);
        List<String> messageIdList = new ArrayList<>();
        if (!TextUtils.isEmpty(recipient)) {
            String phoneNumBySim = PhoneUtils.getDefault().getCanonicalBySimLocale(recipient);

            String participantId = BugleDatabaseOperations.getParticipantIdByName(phoneNumBySim);
            String conversationId =
                    BugleDatabaseOperations.getConversationIdForParticipantsGroup(Collections.singletonList(participantId));

            if (BugleDatabaseOperations.updateConversationPrivateStatue(conversationId, false)) {
                PrivateContactsManager.getInstance().updatePrivateContactsByConversationId(conversationId, false);
                MessagingContentProvider.notifyConversationListChanged();
                addMessagesByConversation(conversationId, messageIdList);
            }
        }

        if (messageIdList.size() > 0) {
            MoveMessageToTelephonyAction.move(messageIdList, "", "");
        } else {
            Toasts.showToast(R.string.private_box_move_from_success);
        }
        return null;
    }

    private void addMessagesByConversation(final String conversationId, final List<String> messageIdList) {
        final DatabaseWrapper db = DataModel.get().getDatabase();
        Assert.notNull(conversationId);

        Cursor cursor = db.query(DatabaseHelper.MESSAGES_TABLE,
                new String[]{DatabaseHelper.MessageColumns._ID},
                DatabaseHelper.MessageColumns.CONVERSATION_ID + "=?",
                new String[]{conversationId},
                null, null, null);

        if (cursor == null) {
            return;
        }
        while (cursor.moveToNext()) {
            String messageId = cursor.getString(0);
            if (!TextUtils.isEmpty(messageId)) {
                messageIdList.add(messageId);
            }
        }
        cursor.close();
    }

    private MoveRecipientsToTelephonyAction(final Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<MoveRecipientsToTelephonyAction> CREATOR
            = new Parcelable.Creator<MoveRecipientsToTelephonyAction>() {
        @Override
        public MoveRecipientsToTelephonyAction createFromParcel(final Parcel in) {
            return new MoveRecipientsToTelephonyAction(in);
        }

        @Override
        public MoveRecipientsToTelephonyAction[] newArray(final int size) {
            return new MoveRecipientsToTelephonyAction[size];
        }
    };

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        writeActionToParcel(parcel, flags);
    }
}
