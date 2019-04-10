package com.android.messaging.privatebox;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.action.Action;
import com.android.messaging.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class MoveConversationToTelephonyAction extends Action {

    public static void moveToTelephony(final String conversationId) {
        new MoveConversationToTelephonyAction(conversationId).start();
    }

    private static final String KEY_CONVERSATION_ID = "conversation_id";

    private MoveConversationToTelephonyAction(String conversationId) {
        super();
        actionParameters.putString(KEY_CONVERSATION_ID, conversationId);
    }

    @Override
    protected Object executeAction() {
        final String conversationId = actionParameters.getString(KEY_CONVERSATION_ID);
        if (!TextUtils.isEmpty(conversationId)) {
            if (BugleDatabaseOperations.updateConversationPrivateStatue(conversationId, false)) {
                MessagingContentProvider.notifyConversationListChanged();
            }
            moveMessagesToTelephony(conversationId);
        }
        return null;
    }

    private void moveMessagesToTelephony(final String conversationId) {
        final DatabaseWrapper db = DataModel.get().getDatabase();
        Assert.notNull(conversationId);

        final List<String> messageIdList = new ArrayList<>();

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
            try {
                if (!TextUtils.isEmpty(messageId)) {
                    messageIdList.add(messageId);
                }
            } catch (Exception e) {

            }
        }

        cursor.close();

        for (String messageId : messageIdList) {
            MoveMessageToTelephonyAction.move(messageId);
        }
    }

    private MoveConversationToTelephonyAction(final Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<MoveConversationToTelephonyAction> CREATOR
            = new Parcelable.Creator<MoveConversationToTelephonyAction>() {
        @Override
        public MoveConversationToTelephonyAction createFromParcel(final Parcel in) {
            return new MoveConversationToTelephonyAction(in);
        }

        @Override
        public MoveConversationToTelephonyAction[] newArray(final int size) {
            return new MoveConversationToTelephonyAction[size];
        }
    };

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        writeActionToParcel(parcel, flags);
    }
}
