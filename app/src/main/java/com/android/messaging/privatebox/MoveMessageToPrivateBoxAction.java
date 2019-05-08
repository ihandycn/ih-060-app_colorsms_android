package com.android.messaging.privatebox;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
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
import com.android.messaging.sms.MmsUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.util.Toasts;

import java.util.ArrayList;
import java.util.List;

import static com.android.messaging.datamodel.data.MessageData.PROTOCOL_SMS;


public class MoveMessageToPrivateBoxAction extends Action {
    private static final String MESSAGES_MOVE_START_NOTIFICATION = "messages_move_start_notification";
    private static final String MESSAGES_MOVE_END_NOTIFICATION = "messages_move_end_notification";

    private static final String KEY_MESSAGE_LIST = "message_list";

    static void moveMessagesToPrivateBox(List<String> messageList, final String moveStartNotification,
                                         final String moveEndNotification) {
        MoveMessageToPrivateBoxAction action = new MoveMessageToPrivateBoxAction();
        action.actionParameters.putStringArrayList(KEY_MESSAGE_LIST, (ArrayList<String>) messageList);
        action.actionParameters.putString(MESSAGES_MOVE_START_NOTIFICATION, moveStartNotification);
        action.actionParameters.putString(MESSAGES_MOVE_END_NOTIFICATION, moveEndNotification);
        action.start();
    }

    private MoveMessageToPrivateBoxAction() {
        super();
    }

    @Override
    protected Object executeAction() {
        moveToPrivateBox();
        return null;
    }

    private void moveToPrivateBox() {
        List<String> messageList = actionParameters.getStringArrayList(KEY_MESSAGE_LIST);
        DatabaseWrapper db = DataModel.get().getDatabase();
        ContentResolver resolver = HSApplication.getContext().getContentResolver();
        if (messageList == null) {
            return;
        }
        if (messageList.size() > 0 &&
                actionParameters.containsKey(MESSAGES_MOVE_START_NOTIFICATION)) {
            String notificationName = actionParameters.getString(MESSAGES_MOVE_START_NOTIFICATION);
            if (!TextUtils.isEmpty(notificationName)) {
                HSGlobalNotificationCenter.sendNotification(notificationName);
            }
        }
        try {
            for (String messageId : messageList) {
                Cursor cursor = db.query(DatabaseHelper.MESSAGES_TABLE,
                        new String[]{DatabaseHelper.MessageColumns.SMS_MESSAGE_URI, DatabaseHelper.MessageColumns.PROTOCOL},
                        DatabaseHelper.MessageColumns._ID + " =?", new String[]{messageId},
                        null, null, null);

                if (cursor == null) {
                    continue;
                }

                if (!cursor.moveToFirst()) {
                    cursor.close();
                    continue;
                }
                Uri telephonyUri = Uri.parse(cursor.getString(0));
                int protocol = cursor.getInt(1);
                boolean isSms = (protocol == PROTOCOL_SMS);
                cursor.close();

                if (isSms) {
                    Cursor smsCursor = resolver.query(telephonyUri, PrivateSmsEntry.getProjection(),
                            null, null, null);

                    if (smsCursor != null && smsCursor.moveToFirst()) {
                        ContentValues values = new ContentValues();
                        bindSmsValues(values, smsCursor);
                        Uri localUri = resolver.insert(PrivateSmsEntry.CONTENT_URI, values);
                        if (localUri == null) {
                            smsCursor.close();
                            continue;
                        }
                        values.clear();
                        values.put(DatabaseHelper.MessageColumns.SMS_MESSAGE_URI, localUri.toString());
                        BugleDatabaseOperations.updateMessageRow(db, messageId, values);
                        MmsUtils.deleteMessage(telephonyUri);
                    }
                    if (smsCursor != null) {
                        smsCursor.close();
                    }

                } else {
                    Cursor mmsCursor = resolver.query(telephonyUri, PrivateMmsEntry.getProjection(),
                            null, null, null);

                    if (mmsCursor != null && mmsCursor.moveToFirst()) {
                        //1. copy data in message table & update uri
                        ContentValues values = new ContentValues();
                        bindMmsValues(values, mmsCursor);
                        Uri localUri = resolver.insert(PrivateMmsEntry.CONTENT_URI, values);
                        if (localUri == null) {
                            mmsCursor.close();
                            continue;
                        }
                        values.clear();
                        values.put(DatabaseHelper.MessageColumns.SMS_MESSAGE_URI, localUri.toString());
                        BugleDatabaseOperations.updateMessageRow(db, messageId, values);

                        //2. change msg_id in part table in telephony
                        Cursor partCursor = db.query(DatabaseHelper.PARTS_TABLE, new String[]{DatabaseHelper.PartColumns.CONTENT_URI},
                                DatabaseHelper.PartColumns.MESSAGE_ID + "=?", new String[]{messageId},
                                null, null, null);

                        if (partCursor != null && partCursor.moveToFirst()) {
                            values.clear();
                            values.put(Telephony.Mms.Part.MSG_ID, -1 * ContentUris.parseId(localUri));
                            do {
                                String partUri = partCursor.getString(0);
                                if (!TextUtils.isEmpty(partUri) && partUri.contains("content://mms/part")) {
                                    HSApplication.getContext().getContentResolver().update(
                                            Uri.parse(partUri), values,
                                            null, null);
                                }
                            } while (partCursor.moveToNext());
                        }

                        if (partCursor != null) {
                            partCursor.close();
                        }

                        //3. copy data in addr table
                        Uri telephonyAddrUri = Uri.parse("content://mms/" + telephonyUri.getLastPathSegment() + "/addr");
                        Uri localAddrUri = Uri.parse(PrivateMmsEntry.CONTENT_URI + "/" + localUri.getLastPathSegment() + "/addr");
                        Cursor addressCursor = HSApplication.getContext().getContentResolver().query(
                                telephonyAddrUri, null, null, null, null);
                        if (addressCursor != null) {
                            if (addressCursor.moveToNext()) {
                                values.clear();
                                bindMmsAddrValues(values, addressCursor);
                                resolver.insert(localAddrUri, values);
                            }
                            addressCursor.close();
                        }

                        MmsUtils.deleteMessage(telephonyUri);
                    }
                    if (mmsCursor != null) {
                        mmsCursor.close();
                    }
                }
            }
        } finally {
            if (actionParameters.containsKey(MESSAGES_MOVE_END_NOTIFICATION)) {
                String notificationName = actionParameters.getString(MESSAGES_MOVE_END_NOTIFICATION);
                if (!TextUtils.isEmpty(notificationName)) {
                    HSGlobalNotificationCenter.sendNotification(notificationName);
                }
            }
            Toasts.showToast(R.string.private_box_add_success);
        }
    }

    private static void bindSmsValues(ContentValues values, Cursor localCursor) {
        for (int i = 1; i < localCursor.getColumnCount(); i++) {
            if (!localCursor.isNull(i)) {
                values.put(localCursor.getColumnName(i), localCursor.getString(i));
            }
        }
    }

    private static void bindMmsValues(ContentValues values, Cursor localCursor) {
        for (int i = 1; i < localCursor.getColumnCount(); i++) {
            if (!localCursor.isNull(i)) {
                values.put(localCursor.getColumnName(i), localCursor.getString(i));
            }
        }
    }

    private static void bindMmsAddrValues(ContentValues values, Cursor cursor) {
        for(int i = 0; i < cursor.getColumnCount(); i++) {
            if (!cursor.isNull(i) && PrivateMmsEntry.Addr.sSupportedFields.contains(cursor.getColumnName(i))) {
                values.put(cursor.getColumnName(i), cursor.getString(i));
            }
        }
    }

    private MoveMessageToPrivateBoxAction(final Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<MoveMessageToPrivateBoxAction> CREATOR
            = new Parcelable.Creator<MoveMessageToPrivateBoxAction>() {
        @Override
        public MoveMessageToPrivateBoxAction createFromParcel(final Parcel in) {
            return new MoveMessageToPrivateBoxAction(in);
        }

        @Override
        public MoveMessageToPrivateBoxAction[] newArray(final int size) {
            return new MoveMessageToPrivateBoxAction[size];
        }
    };

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        writeActionToParcel(parcel, flags);
    }
}
