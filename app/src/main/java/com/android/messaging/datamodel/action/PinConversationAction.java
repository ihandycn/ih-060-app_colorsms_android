package com.android.messaging.datamodel.action;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.util.Assert;

public class PinConversationAction extends Action {

    public static void unpinConversation(final String conversationId) {
        final PinConversationAction action =
                new PinConversationAction(conversationId, false);
        action.start();
    }

    public static void pinConversation(final String conversationId) {
        final PinConversationAction action =
                new PinConversationAction(conversationId, true);
        action.start();
    }

    private static final String KEY_CONVERSATION_ID = "conversation_id";
    private static final String KEY_IS_PIN = "is_pin";

    protected PinConversationAction(
            final String conversationId, final boolean isPin) {
        Assert.isTrue(!TextUtils.isEmpty(conversationId));
        actionParameters.putString(KEY_CONVERSATION_ID, conversationId);
        actionParameters.putBoolean(KEY_IS_PIN, isPin);
    }

    @Override
    protected Object executeAction() {
        final String conversationId = actionParameters.getString(KEY_CONVERSATION_ID);
        final boolean isPin = actionParameters.getBoolean(KEY_IS_PIN);

        final DatabaseWrapper db = DataModel.get().getDatabase();
        db.beginTransaction();
        try {
            BugleDatabaseOperations.updateConversationPinStatues(
                    db, conversationId, isPin);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        MessagingContentProvider.notifyConversationListChanged();
        return null;
    }

    protected PinConversationAction(final Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<PinConversationAction> CREATOR
            = new Parcelable.Creator<PinConversationAction>() {
        @Override
        public PinConversationAction createFromParcel(final Parcel in) {
            return new PinConversationAction(in);
        }

        @Override
        public PinConversationAction[] newArray(final int size) {
            return new PinConversationAction[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeActionToParcel(dest, flags);
    }
}
