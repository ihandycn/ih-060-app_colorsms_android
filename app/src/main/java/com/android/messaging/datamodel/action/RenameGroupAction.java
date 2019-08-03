package com.android.messaging.datamodel.action;

import android.os.Parcel;
import android.os.Parcelable;

import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;

public class RenameGroupAction extends Action {

    public static void renameGroup(String conversationId, String name){
        new RenameGroupAction(conversationId, name).start();
    }

    private static final String KEY_CONVERSATION_ID = "conversation_id";
    private static final String KEY_NAME = "group_name";

    protected RenameGroupAction(String conversationId, String name) {
        actionParameters.putString(KEY_CONVERSATION_ID, conversationId);
        actionParameters.putString(KEY_NAME, name);
    }

    @Override
    protected Object executeAction() {
        final String conversationId = actionParameters.getString(KEY_CONVERSATION_ID);
        final String newName = actionParameters.getString(KEY_NAME);
        final DatabaseWrapper db = DataModel.get().getDatabase();
        db.beginTransaction();
        try {
            BugleDatabaseOperations.updateGroupName(db, conversationId, newName);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        MessagingContentProvider.notifyConversationListChanged();
        return null;
    }


    protected RenameGroupAction(final Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<RenameGroupAction> CREATOR = new Parcelable.Creator<RenameGroupAction>(){

        @Override
        public RenameGroupAction createFromParcel(Parcel source) {
            return new RenameGroupAction(source);
        }

        @Override
        public RenameGroupAction[] newArray(int size) {
            return new RenameGroupAction[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeActionToParcel(dest, flags);
    }
}
