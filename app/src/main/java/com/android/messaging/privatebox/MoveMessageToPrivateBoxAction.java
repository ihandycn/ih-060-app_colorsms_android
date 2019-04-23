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
                    Cursor smsCursor = resolver.query(telephonyUri, PrivateSmsEntry.sProjection,
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
                    Cursor mmsCursor = resolver.query(telephonyUri, PrivateMmsEntry.sProjection,
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
                                if (partCursor.getString(0) != null) {
                                    HSApplication.getContext().getContentResolver().update(
                                            Uri.parse(partCursor.getString(0)), values,
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
                            while (addressCursor.moveToFirst()) {
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
        values.put(PrivateSmsEntry.THREAD_ID,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.THREAD_ID)));
        values.put(PrivateSmsEntry.ADDRESS,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.ADDRESS)));
        String peopleId = localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.PERSON));
        if (!TextUtils.isEmpty(peopleId)) {
            values.put(PrivateSmsEntry.PERSON, peopleId);
        }
        values.put(PrivateSmsEntry.DATE,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.DATE)));
        values.put(PrivateSmsEntry.DATE_SEND,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.DATE_SENT)));
        values.put(PrivateSmsEntry.PROTOCOL,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.PROTOCOL)));
        values.put(PrivateSmsEntry.READ,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.READ)));
        values.put(PrivateSmsEntry.STATUS,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.STATUS)));
        values.put(PrivateSmsEntry.TYPE,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.TYPE)));
        values.put(PrivateSmsEntry.REPLY_PATH_PRESENT,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.REPLY_PATH_PRESENT)));
        values.put(PrivateSmsEntry.SUBJECT,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.SUBJECT)));
        values.put(PrivateSmsEntry.BODY,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.BODY)));
        values.put(PrivateSmsEntry.SERVICE_CENTER,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.SERVICE_CENTER)));
        values.put(PrivateSmsEntry.LOCKED,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.LOCKED)));
        values.put(PrivateSmsEntry.ERROR_CODE,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.ERROR_CODE)));
        values.put(PrivateSmsEntry.SEEN,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.SEEN)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            values.put(PrivateSmsEntry.SUBSCRIPTION_ID,
                    localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID)));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            values.put(PrivateSmsEntry.CREATOR,
                    localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.CREATOR)));
        }
    }

    private static void bindMmsValues(ContentValues values, Cursor localCursor) {
        values.put(PrivateMmsEntry.THREAD_ID,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.THREAD_ID)));
        values.put(PrivateMmsEntry.DATE,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.DATE)));
        values.put(PrivateMmsEntry.DATE_SENT,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.DATE_SENT)));
        values.put(PrivateMmsEntry.MESSAGE_BOX,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.MESSAGE_BOX)));
        values.put(PrivateMmsEntry.READ,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.READ)));
        values.put(PrivateMmsEntry.MESSAGE_ID,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.MESSAGE_ID)));
        values.put(PrivateMmsEntry.SUBJECT,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.SUBJECT)));
        values.put(PrivateMmsEntry.SUBJECT_CHARSET,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.SUBJECT_CHARSET)));
        values.put(PrivateMmsEntry.CONTENT_TYPE,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.CONTENT_TYPE)));
        values.put(PrivateMmsEntry.CONTENT_LOCATION,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.CONTENT_LOCATION)));
        values.put(PrivateMmsEntry.EXPIRY,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.EXPIRY)));
        values.put(PrivateMmsEntry.MESSAGE_CLASS,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.MESSAGE_CLASS)));
        values.put(PrivateMmsEntry.MESSAGE_TYPE,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.MESSAGE_TYPE)));
        values.put(PrivateMmsEntry.MMS_VERSION,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.MMS_VERSION)));
        values.put(PrivateMmsEntry.MESSAGE_SIZE,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.MESSAGE_SIZE)));
        values.put(PrivateMmsEntry.PRIORITY,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.PRIORITY)));
        values.put(PrivateMmsEntry.READ_REPORT,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.READ_REPORT)));
        values.put(PrivateMmsEntry.REPORT_ALLOWED,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.REPORT_ALLOWED)));
        values.put(PrivateMmsEntry.RESPONSE_STATUS,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.RESPONSE_STATUS)));
        values.put(PrivateMmsEntry.STATUS,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.STATUS)));
        values.put(PrivateMmsEntry.TRANSACTION_ID,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.TRANSACTION_ID)));
        values.put(PrivateMmsEntry.RETRIEVE_STATUS,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.RETRIEVE_STATUS)));
        values.put(PrivateMmsEntry.RETRIEVE_TEXT,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.RETRIEVE_TEXT)));
        values.put(PrivateMmsEntry.RETRIEVE_TEXT_CHARSET,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.RETRIEVE_TEXT_CHARSET)));
        values.put(PrivateMmsEntry.READ_STATUS,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.READ_STATUS)));
        values.put(PrivateMmsEntry.CONTENT_CLASS,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.CONTENT_CLASS)));
        values.put(PrivateMmsEntry.RESPONSE_TEXT,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.RESPONSE_TEXT)));
        values.put(PrivateMmsEntry.DELIVERY_TIME,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.DELIVERY_TIME)));
        values.put(PrivateMmsEntry.DELIVERY_REPORT,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.DELIVERY_REPORT)));
        values.put(PrivateMmsEntry.LOCKED,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.LOCKED)));
        values.put(PrivateMmsEntry.SEEN,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.SEEN)));
        values.put(PrivateMmsEntry.TEXT_ONLY,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.TEXT_ONLY)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            values.put(PrivateMmsEntry.SUBSCRIPTION_ID,
                    localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.SUBSCRIPTION_ID)));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            values.put(PrivateMmsEntry.CREATOR,
                    localCursor.getString(localCursor.getColumnIndex(Telephony.Mms.CREATOR)));
        }
    }

    private static void bindMmsAddrValues(ContentValues values, Cursor cursor) {
        values.put(PrivateMmsEntry.Addr.ADDRESS,
                cursor.getString(cursor.getColumnIndex(Telephony.Mms.Addr.ADDRESS)));
        values.put(PrivateMmsEntry.Addr.CHARSET,
                cursor.getString(cursor.getColumnIndex(Telephony.Mms.Addr.CHARSET)));
        values.put(PrivateMmsEntry.Addr.CONTACT_ID,
                cursor.getString(cursor.getColumnIndex(Telephony.Mms.Addr.CONTACT_ID)));
        values.put(PrivateMmsEntry.Addr.TYPE,
                cursor.getString(cursor.getColumnIndex(Telephony.Mms.Addr.TYPE)));
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
