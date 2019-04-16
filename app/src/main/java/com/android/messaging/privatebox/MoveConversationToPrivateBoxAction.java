package com.android.messaging.privatebox;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseHelper.MessageColumns;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.action.Action;
import com.android.messaging.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class MoveConversationToPrivateBoxAction extends Action implements Parcelable {

    public static void moveAndUpdatePrivateContact(final String conversationId) {
        final MoveConversationToPrivateBoxAction action = new MoveConversationToPrivateBoxAction(conversationId);
        action.actionParameters.putBoolean(KEY_UPDATE_PRIVATE_CONTACT, true);
        action.start();
    }

    public static void move(final String conversationId) {
        final MoveConversationToPrivateBoxAction action = new MoveConversationToPrivateBoxAction(conversationId);
        action.actionParameters.putBoolean(KEY_UPDATE_PRIVATE_CONTACT, false);
        action.start();
    }

    private static final String KEY_CONVERSATION_ID = "conversation_id";
    private static final String KEY_UPDATE_PRIVATE_CONTACT = "update_contact";

    private MoveConversationToPrivateBoxAction(final String conversationId) {
        super();
        actionParameters.putString(KEY_CONVERSATION_ID, conversationId);
    }

    @Override
    protected Object executeAction() {
        final String conversationId = actionParameters.getString(KEY_CONVERSATION_ID);
        boolean updatePrivateContact = actionParameters.getBoolean(KEY_UPDATE_PRIVATE_CONTACT, true);
        if (!TextUtils.isEmpty(conversationId)) {
            if (BugleDatabaseOperations.updateConversationPrivateStatue(conversationId, true)) {
                MessagingContentProvider.notifyConversationListChanged();
                if (updatePrivateContact) {
                    PrivateContactsManager.getInstance().updatePrivateContactsByConversationId(conversationId, true);
                }
                moveMessagesToPrivateBox(conversationId);
            }
        }
        return null;
    }

    private void moveMessagesToPrivateBox(final String conversationId) {
        final DatabaseWrapper db = DataModel.get().getDatabase();
        Assert.notNull(conversationId);

        final List<String> messageIdList = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.MESSAGES_TABLE,
                new String[]{MessageColumns._ID},
                MessageColumns.CONVERSATION_ID + "=?",
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

        for (String messageId : messageIdList) {
            MoveMessageToPrivateBoxAction.moveMessageToPrivateBox(messageId);
        }
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
