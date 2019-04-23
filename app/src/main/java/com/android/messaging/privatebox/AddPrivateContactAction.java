package com.android.messaging.privatebox;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.action.Action;
import com.android.messaging.sms.MmsSmsUtils;
import com.ihs.app.framework.HSApplication;

import java.util.ArrayList;
import java.util.List;

import static com.android.messaging.privatebox.PrivateContactsManager.PHONE_NUMBER;
import static com.android.messaging.privatebox.PrivateContactsManager.PRIVATE_CONTACTS_TABLE;
import static com.android.messaging.privatebox.PrivateContactsManager.THREAD_ID;
import static com.android.messaging.privatebox.PrivateContactsManager.sProjection;

public class AddPrivateContactAction extends Action {
    private static final String KEY_THREAD_ID = "thread_id";
    private static final String KEY_RECIPIENT = "recipient";

    public static void addPrivateRecipientsAndMoveMessages(final List<String> recipients) {
        ArrayList<Long> threadIdList = new ArrayList<>();
        //todo : check permission for no grant exception
        for (String recipient : recipients) {
            long threadId = MmsSmsUtils.Threads.getOrCreateThreadId(HSApplication.getContext(), recipient);
            if (threadId < 0) {
                continue;
            }
            threadIdList.add(threadId);
        }

        long[] threadIdArray = new long[threadIdList.size()];
        for (int i = 0; i < threadIdList.size(); i++) {
            threadIdArray[i] = threadIdList.get(i);
        }

        AddPrivateContactAction action = new AddPrivateContactAction();
        action.actionParameters.putLongArray(KEY_THREAD_ID, threadIdArray);
        action.actionParameters.putStringArrayList(KEY_RECIPIENT, (ArrayList<String>) recipients);
        action.start();
    }

    private AddPrivateContactAction() {
        super();
    }

    @Override
    protected Object executeAction() {
        long[] threadIdList = actionParameters.getLongArray(KEY_THREAD_ID);
        List<String> recipients = null;
        if (actionParameters.containsKey(KEY_RECIPIENT)) {
            recipients = actionParameters.getStringArrayList(KEY_RECIPIENT);
        }

        DatabaseWrapper db = DataModel.get().getDatabase();
        assert threadIdList != null;

        for (int i = 0; i < threadIdList.length; i++) {
            addThreadIdToPrivateTable(db, threadIdList[i], recipients == null ? null : recipients.get(i));
        }
        return null;
    }

    private void addThreadIdToPrivateTable(DatabaseWrapper db, long threadId, String recipient) {
        //check if the thread_id is in the table
        Cursor cursor = db.query(PRIVATE_CONTACTS_TABLE, sProjection, THREAD_ID + "=?",
                new String[]{String.valueOf(threadId)}, null, null, null);
        if (cursor == null) {
            return;
        }
        if (cursor.getCount() <= 0) {
            ContentValues values = new ContentValues();
            values.put(THREAD_ID, threadId);
            values.put(PHONE_NUMBER, recipient);
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
