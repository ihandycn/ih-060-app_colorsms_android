package com.android.messaging.datamodel.action;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.util.Assert;

public class MarkAllAsReadAction extends Action {

    public static void markAllAsRead() {
        final MarkAllAsReadAction action = new MarkAllAsReadAction();
        action.start();
    }

    private MarkAllAsReadAction() {
    }

    @Override
    protected Object executeAction() {
        Assert.isNotMainThread();

        final DatabaseWrapper db = DataModel.get().getDatabase();

        // Update local db
        db.beginTransaction();
        try {
            final ContentValues values = new ContentValues();
            values.put(DatabaseHelper.MessageColumns.READ, 1);
            values.put(DatabaseHelper.MessageColumns.SEEN, 1);     // if they read it, they saw it

            db.update(DatabaseHelper.MESSAGES_TABLE, values,
                    "(" + DatabaseHelper.MessageColumns.READ + " !=1 OR " +
                            DatabaseHelper.MessageColumns.SEEN + " !=1 )", null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        // After marking messages as read, update the notifications. This will
        // clear the now stale notifications.
        MessagingContentProvider.notifyConversationListChanged();
        BugleNotifications.update(false/*silent*/, BugleNotifications.UPDATE_ALL);
        return null;
    }


    private MarkAllAsReadAction(final Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<MarkAllAsReadAction> CREATOR
            = new Parcelable.Creator<MarkAllAsReadAction>() {
        @Override
        public MarkAllAsReadAction createFromParcel(final Parcel in) {
            return new MarkAllAsReadAction(in);
        }

        @Override
        public MarkAllAsReadAction[] newArray(final int size) {
            return new MarkAllAsReadAction[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeActionToParcel(dest, flags);
    }
}
