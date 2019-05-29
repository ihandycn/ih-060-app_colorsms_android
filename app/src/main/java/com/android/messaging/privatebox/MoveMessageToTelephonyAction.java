package com.android.messaging.privatebox;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Telephony;
import android.text.TextUtils;

import com.android.messaging.R;
import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.action.Action;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.util.Toasts;

import java.util.ArrayList;
import java.util.List;

public class MoveMessageToTelephonyAction extends Action {
    private static final String NOTIFICATION_KEY_MOVE_START = "key_move_start";
    private static final String NOTIFICATION_KEY_MOVE_END = "key_move_end";
    private static final String MESSAGE_ID = "message_id";

    public static void move(List<String> messageIdList, String startNotificationName, String endNotificationName) {
        MoveMessageToTelephonyAction action = new MoveMessageToTelephonyAction();
        action.actionParameters.putStringArrayList(MESSAGE_ID, (ArrayList<String>) messageIdList);
        action.actionParameters.putString(NOTIFICATION_KEY_MOVE_START, startNotificationName);
        action.actionParameters.putString(NOTIFICATION_KEY_MOVE_END, endNotificationName);
        action.start();
    }

    private MoveMessageToTelephonyAction() {
        super();
    }

    @Override
    protected Object executeAction() {
        moveMessageToTelephony();
        return null;
    }

    private void moveMessageToTelephony() {
        List<String> msgIdList = actionParameters.getStringArrayList(MESSAGE_ID);
        ContentResolver resolver = HSApplication.getContext().getContentResolver();
        ContentValues values = new ContentValues();
        DatabaseWrapper db = DataModel.get().getDatabase();

        assert msgIdList != null;
        if (msgIdList.size() > 0 && actionParameters.containsKey(NOTIFICATION_KEY_MOVE_START)) {
            String start = actionParameters.getString(NOTIFICATION_KEY_MOVE_START);
            if (!TextUtils.isEmpty(start)) {
                HSGlobalNotificationCenter.sendNotification(start);
            }
        }

        try {
            for (String msgId : msgIdList) {
                Cursor c = db.query(DatabaseHelper.MESSAGES_TABLE,
                        new String[]{DatabaseHelper.MessageColumns.SMS_MESSAGE_URI,
                                DatabaseHelper.MessageColumns.CONVERSATION_ID},
                        DatabaseHelper.MessageColumns._ID + " =? ", new String[]{msgId},
                        null, null, null);

                Uri messageUri = null;
                String conversationId = null;
                if (c != null && c.moveToFirst()) {
                    String uriValue = c.getString(0);
                    if (!TextUtils.isEmpty(uriValue)) {
                        messageUri = Uri.parse(uriValue);
                    }
                    conversationId = c.getString(1);
                }
                if (c != null) {
                    c.close();
                }
                if (messageUri == null) {
                    return;
                }

                if (!messageUri.toString().contains("mms")) {
                    Cursor localSmsCursor = resolver.query(messageUri,
                            PrivateSmsEntry.getProjection(), null, null, null);
                    if (localSmsCursor == null) {
                        return;
                    }
                    if (localSmsCursor.moveToFirst()) {
                        values.clear();
                        values.put(Telephony.Sms.THREAD_ID, PrivateMessageManager.getInstance().getValidThreadId(
                                localSmsCursor.getLong(PrivateSmsEntry.THREAD_ID_INDEX), db, conversationId));
                        bindSmsValues(values, localSmsCursor);
                        //insert sms into telephony
                        Uri uri = resolver.insert(Telephony.Sms.CONTENT_URI, values);
                        //update uri in local db
                        if (uri != null) {
                            values.clear();
                            values.put(DatabaseHelper.MessageColumns.SMS_MESSAGE_URI, uri.toString());
                            BugleDatabaseOperations.updateMessageRow(db, msgId, values);
                            resolver.delete(messageUri, null, null);
                        } else {
                            localSmsCursor.close();
                            return;
                        }
                    }
                    localSmsCursor.close();
                } else {
                    Cursor localMmsCursor = resolver.query(messageUri, PrivateMmsEntry.getProjection(),
                            null, null, null);
                    if (localMmsCursor == null) {
                        return;
                    }
                    if (localMmsCursor.moveToFirst()) {
                        values.clear();
                        // we want to get a valid thread id
                        values.put(Telephony.Mms.THREAD_ID, PrivateMessageManager.getInstance().getValidThreadId(
                                localMmsCursor.getLong(PrivateMmsEntry.THREAD_ID_INDEX), db, conversationId));
                        bindMmsValues(values, localMmsCursor);
                        //insert into telephony
                        Uri uri = resolver.insert(Telephony.Mms.CONTENT_URI, values);
                        // update uri
                        if (uri == null) {
                            localMmsCursor.close();
                            return;
                        }

                        values.clear();
                        values.put(DatabaseHelper.MessageColumns.SMS_MESSAGE_URI, uri.toString());
                        BugleDatabaseOperations.updateMessageRow(db, msgId, values);
                        resolver.delete(messageUri, null, null);

                        values.clear();
                        values.put(Telephony.Mms.Part.MSG_ID, ContentUris.parseId(uri));
                        //getPartUris
                        List<String> partUris = new ArrayList<>();
                        Cursor cursor = db.query(DatabaseHelper.PARTS_TABLE,
                                new String[]{DatabaseHelper.PartColumns.CONTENT_URI},
                                DatabaseHelper.PartColumns.MESSAGE_ID + "=?", new String[]{msgId},
                                null, null, null);

                        if (cursor != null) {
                            while (cursor.moveToNext()) {
                                if (!TextUtils.isEmpty(cursor.getString(0))) {
                                    partUris.add(cursor.getString(0));
                                }
                            }
                            cursor.close();
                        }

                        for (String partUri : partUris) {
                            if (!TextUtils.isEmpty(partUri) && partUri.contains("content://mms/part")) {
                                HSApplication.getContext().getContentResolver()
                                        .update(Uri.parse(partUri), values, null, null);
                            }
                        }
                        //copy address data
                        long telephonyMsgId = ContentUris.parseId(uri);
                        Uri localUri = Uri.parse(PrivateMmsEntry.CONTENT_URI + "/" + msgId + "/addr");
                        Uri telephonyUri = Uri.parse("content://mms/" + telephonyMsgId + "/addr");
                        cursor = resolver.query(localUri, null, null, null, null);
                        if (cursor != null) {
                            while (cursor.moveToNext()) {
                                values.clear();
                                bindMmsAddrValues(values, cursor);
                                resolver.insert(telephonyUri, values);
                            }
                            cursor.close();
                            resolver.delete(localUri, null, null);
                        }
                    }
                    localMmsCursor.close();
                }
            }
        } finally {
            if (actionParameters.containsKey(NOTIFICATION_KEY_MOVE_END)) {
                String end = actionParameters.getString(NOTIFICATION_KEY_MOVE_END);
                if (!TextUtils.isEmpty(end)) {
                    HSGlobalNotificationCenter.sendNotification(end);
                }
            }
            Toasts.showToast(R.string.private_box_move_from_success);
        }
    }

    private void bindMmsValues(ContentValues values, Cursor localCursor) {
        for (int i = 2; i < localCursor.getColumnCount(); i++) {
            if (!localCursor.isNull(i)) {
                values.put(localCursor.getColumnName(i), localCursor.getString(i));
            }
        }
    }

    private void bindSmsValues(ContentValues values, Cursor localCursor) {
        for (int i = 2; i < localCursor.getColumnCount(); i++) {
            if (!localCursor.isNull(i)) {
                values.put(localCursor.getColumnName(i), localCursor.getString(i));
            }
        }
    }

    private void bindMmsAddrValues(ContentValues values, Cursor cursor) {
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            if (!cursor.isNull(i) && PrivateMmsEntry.Addr.sSupportedFields.contains(cursor.getColumnName(i))) {
                values.put(cursor.getColumnName(i), cursor.getString(i));
            }
        }
    }

    private MoveMessageToTelephonyAction(final Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<MoveMessageToTelephonyAction> CREATOR
            = new Parcelable.Creator<MoveMessageToTelephonyAction>() {
        @Override
        public MoveMessageToTelephonyAction createFromParcel(final Parcel in) {
            return new MoveMessageToTelephonyAction(in);
        }

        @Override
        public MoveMessageToTelephonyAction[] newArray(final int size) {
            return new MoveMessageToTelephonyAction[size];
        }
    };

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        writeActionToParcel(parcel, flags);
    }
}
