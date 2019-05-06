package com.android.messaging.privatebox;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.action.Action;
import com.android.messaging.sms.MmsSmsUtils;
import com.android.messaging.util.PhoneUtils;
import com.ihs.app.framework.HSApplication;

import java.util.ArrayList;
import java.util.List;

import static com.android.messaging.privatebox.PrivateContactsManager.PRIVATE_CONTACTS_TABLE;
import static com.android.messaging.privatebox.PrivateContactsManager.RECIPIENTS;
import static com.android.messaging.privatebox.PrivateContactsManager.THREAD_ID;
import static com.android.messaging.privatebox.PrivateContactsManager.sProjection;

public class AddPrivateContactAction extends Action {
    private static final String KEY_RECIPIENT = "recipient";

    public static void addPrivateRecipientsAndMoveMessages(final List<String> recipients) {
        //todo : check permission for no grant exception

        AddPrivateContactAction action = new AddPrivateContactAction();
        action.actionParameters.putStringArrayList(KEY_RECIPIENT, (ArrayList<String>) recipients);
        action.start();
    }

    private AddPrivateContactAction() {
        super();
    }

    @Override
    protected Object executeAction() {
        List<String> recipients = actionParameters.getStringArrayList(KEY_RECIPIENT);
        DatabaseWrapper db = DataModel.get().getDatabase();

        for (String recipient : recipients) {
            addThreadIdToPrivateTable(db, recipient);
        }
        return null;
    }

    private void addThreadIdToPrivateTable(DatabaseWrapper db, String recipient) {
        //check if the thread_id is in the table
        long threadId = MmsSmsUtils.Threads.getOrCreateThreadId(HSApplication.getContext(), recipient.trim());
        String formatRecipient = PhoneUtils.getDefault().getCanonicalBySimLocale(recipient.trim());
        Cursor cursor = db.query(PRIVATE_CONTACTS_TABLE, sProjection,
                RECIPIENTS + "=? AND " + THREAD_ID + "=?",
                new String[]{formatRecipient, String.valueOf(threadId)}, null, null, null);
        if (cursor == null) {
            return;
        }
        if (cursor.getCount() <= 0) {
            ContentValues values = new ContentValues();
            values.put(THREAD_ID, threadId);
            values.put(RECIPIENTS, formatRecipient);
            db.insert(PRIVATE_CONTACTS_TABLE, null, values);
        }
        cursor.close();
    }

    private AddPrivateContactAction(final Parcel in) {
        super(in);
    }

    public static final Creator<AddPrivateContactAction> CREATOR
            = new Creator<AddPrivateContactAction>() {
        @Override
        public AddPrivateContactAction createFromParcel(final Parcel in) {
            return new AddPrivateContactAction(in);
        }

        @Override
        public AddPrivateContactAction[] newArray(final int size) {
            return new AddPrivateContactAction[size];
        }
    };

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        writeActionToParcel(parcel, flags);
    }
}
