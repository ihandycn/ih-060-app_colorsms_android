package com.android.messaging.datamodel.action;

import android.os.Parcel;
import android.os.Parcelable;

import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;

import java.util.ArrayList;
import java.util.List;

public class PinConversationAction extends Action {

    public static void unpinConversation(final String conversationId) {
        ArrayList<String> list = new ArrayList<>();
        list.add(conversationId);
        unpinConversation(list);
    }

    public static void unpinConversation(final ArrayList<String> conversationId) {
        new PinConversationAction(conversationId, false).start();
    }

    public static void pinConversation(final String conversationId) {
        ArrayList<String> list = new ArrayList<>();
        list.add(conversationId);
        pinConversation(list);
    }

    public static void pinConversation(final ArrayList<String> conversationId) {
        new PinConversationAction(conversationId, true).start();
    }

    private static final String KEY_CONVERSATION_ID = "conversation_id";
    private static final String KEY_IS_PIN = "is_pin";

    protected PinConversationAction(
            final ArrayList<String> conversationId, final boolean isPin) {
        actionParameters.putStringArrayList(KEY_CONVERSATION_ID, conversationId);
        actionParameters.putBoolean(KEY_IS_PIN, isPin);
    }

    @Override
    protected Object executeAction() {
        final List<String> conversationIdList = actionParameters.getStringArrayList(KEY_CONVERSATION_ID);
        final boolean isPin = actionParameters.getBoolean(KEY_IS_PIN);
        long time = System.currentTimeMillis();
        final DatabaseWrapper db = DataModel.get().getDatabase();
        db.beginTransaction();
        try {
            for (String conversationId : conversationIdList) {
                BugleDatabaseOperations.updateConversationPinStatues(db, conversationId, time, isPin);
            }
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
