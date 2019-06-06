package com.android.messaging.datamodel.action;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.util.Assert;
import com.android.messaging.util.LogUtil;

public class LockMessageAction extends Action{

    public static void unlockMessage(final String conversationId, final String messageId) {
        final LockMessageAction action =
                new LockMessageAction(conversationId, messageId, false);
        action.start();
    }

    public static void lockMessage(final String conversationId, final String messageId) {
        final LockMessageAction action =
                new LockMessageAction(conversationId, messageId, true);
        action.start();
    }

    private static final String KEY_CONVERSATION_ID = "conversation_id";
    private static final String KEY_MESSAGE_ID = "message_id";
    private static final String KEY_IS_LOCK = "is_lock";

    protected LockMessageAction(final String conversationId, final String messageId, final boolean isLock) {
        Assert.isTrue(!TextUtils.isEmpty(conversationId));
        actionParameters.putString(KEY_CONVERSATION_ID, conversationId);
        actionParameters.putBoolean(KEY_IS_LOCK, isLock);
        actionParameters.putString(KEY_MESSAGE_ID, messageId);
    }

    @Override
    protected Object executeAction() {
        final String conversationId = actionParameters.getString(KEY_CONVERSATION_ID);
        final String messageId = actionParameters.getString(KEY_MESSAGE_ID);
        final boolean isLock = actionParameters.getBoolean(KEY_IS_LOCK);

        final DatabaseWrapper db = DataModel.get().getDatabase();
        db.beginTransaction();
        try {
            BugleDatabaseOperations.updateMessageLockStatus(
                    db, messageId, isLock);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        MessagingContentProvider.notifyMessagesChanged(conversationId);
        return null;
    }

    protected LockMessageAction(final Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<LockMessageAction> CREATOR
            = new Parcelable.Creator<LockMessageAction>() {
        @Override
        public LockMessageAction createFromParcel(final Parcel in) {
            return new LockMessageAction(in);
        }

        @Override
        public LockMessageAction[] newArray(final int size) {
            return new LockMessageAction[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeActionToParcel(dest, flags);
    }
}
