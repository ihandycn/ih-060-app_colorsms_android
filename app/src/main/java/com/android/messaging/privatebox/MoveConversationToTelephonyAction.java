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
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.util.Toasts;

import java.util.ArrayList;
import java.util.List;

public class MoveConversationToTelephonyAction extends Action {
    private static final String NOTIFICATION_KEY_MOVE_START = "key_move_start";
    private static final String NOTIFICATION_KEY_MOVE_END = "key_move_end";

    public static void moveToTelephony(final ArrayList<String> conversationId,
                                       String startNotificationName, String endNotificationName) {
        MoveConversationToTelephonyAction action = new MoveConversationToTelephonyAction();
        action.actionParameters.putStringArrayList(KEY_CONVERSATION_ID, conversationId);
        action.actionParameters.putString(NOTIFICATION_KEY_MOVE_START, startNotificationName);
        action.actionParameters.putString(NOTIFICATION_KEY_MOVE_END, endNotificationName);
        action.start();
    }

    private static final String KEY_CONVERSATION_ID = "conversation_id";

    private MoveConversationToTelephonyAction() {
        super();
    }

    @Override
    protected Object executeAction() {
        final List<String> conversationIdList = actionParameters.getStringArrayList(KEY_CONVERSATION_ID);
        List<String> messageIdList = new ArrayList<>();
        assert conversationIdList != null;
        for (String conversationId : conversationIdList) {
            if (!TextUtils.isEmpty(conversationId)) {
                if (BugleDatabaseOperations.updateConversationPrivateStatue(conversationId, false)) {
                    PrivateContactsManager.getInstance().updatePrivateContactsByConversationId(conversationId, false);
                    MessagingContentProvider.notifyConversationListChanged();
                    addMessagesByConversation(conversationId, messageIdList);
                }
            }
        }

        String end = actionParameters.getString(NOTIFICATION_KEY_MOVE_END);
        if (messageIdList.size() > 0) {
            String start = actionParameters.getString(NOTIFICATION_KEY_MOVE_START);
            MoveMessageToTelephonyAction.move(messageIdList, start, end);
        } else {
            if (!TextUtils.isEmpty(end)) {
                HSGlobalNotificationCenter.sendNotification(end);
            }
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
