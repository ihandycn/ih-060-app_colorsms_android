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
import com.android.messaging.sms.MmsSmsUtils;
import com.android.messaging.sms.MmsUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.util.Toasts;

import java.util.ArrayList;
import java.util.HashSet;
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
                            HSApplication.getContext().getContentResolver()
                                    .update(Uri.parse(partUri), values, null, null);
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
            Toasts.showToast(R.string.private_box_add_success);
        }
    }

    private static void bindMmsValues(ContentValues values, Cursor localCursor) {
        values.put(Telephony.Mms.DATE, localCursor.getLong(PrivateMmsEntry.DATE_INDEX));
        values.put(Telephony.Mms.DATE_SENT, localCursor.getLong(PrivateMmsEntry.DATE_SENT_INDEX));
        values.put(Telephony.Mms.MESSAGE_BOX, localCursor.getInt(PrivateMmsEntry.MESSAGE_BOX_INDEX));
        values.put(Telephony.Mms.READ, localCursor.getInt(PrivateMmsEntry.READ_INDEX));
        values.put(Telephony.Mms.MESSAGE_ID, localCursor.getString(PrivateMmsEntry.MESSAGE_ID_INDEX));
        values.put(Telephony.Mms.SUBJECT, localCursor.getString(PrivateMmsEntry.SUBJECT_INDEX));
        values.put(Telephony.Mms.SUBJECT_CHARSET, localCursor.getInt(PrivateMmsEntry.SUBJECT_CHARSET_INDEX));
        values.put(Telephony.Mms.CONTENT_TYPE, localCursor.getString(PrivateMmsEntry.CONTENT_TYPE_INDEX));
        values.put(Telephony.Mms.CONTENT_LOCATION, localCursor.getString(PrivateMmsEntry.CONTENT_LOCATION_INDEX));
        values.put(Telephony.Mms.EXPIRY, localCursor.getInt(PrivateMmsEntry.EXPIRY_INDEX));
        values.put(Telephony.Mms.MESSAGE_CLASS, localCursor.getString(PrivateMmsEntry.MESSAGE_CLASS_INDEX));
        values.put(Telephony.Mms.MESSAGE_TYPE, localCursor.getInt(PrivateMmsEntry.MESSAGE_TYPE_INDEX));
        values.put(Telephony.Mms.MMS_VERSION, localCursor.getInt(PrivateMmsEntry.MMS_VERSION_INDEX));
        values.put(Telephony.Mms.MESSAGE_SIZE, localCursor.getInt(PrivateMmsEntry.MESSAGE_SIZE_INDEX));
        values.put(Telephony.Mms.PRIORITY, localCursor.getInt(PrivateMmsEntry.PRIORITY_INDEX));
        values.put(Telephony.Mms.READ_REPORT, localCursor.getInt(PrivateMmsEntry.READ_REPORT_INDEX));
        values.put(Telephony.Mms.REPORT_ALLOWED, localCursor.getInt(PrivateMmsEntry.REPORT_ALLOWED_INDEX));
        values.put(Telephony.Mms.RESPONSE_STATUS, localCursor.getInt(PrivateMmsEntry.RESPONSE_STATUS_INDEX));
        values.put(Telephony.Mms.STATUS, localCursor.getInt(PrivateMmsEntry.STATUS_INDEX));
        values.put(Telephony.Mms.TRANSACTION_ID, localCursor.getString(PrivateMmsEntry.TRANSACTION_ID_INDEX));
        values.put(Telephony.Mms.RETRIEVE_STATUS, localCursor.getInt(PrivateMmsEntry.RETRIEVE_STATUS_INDEX));
        values.put(Telephony.Mms.RETRIEVE_TEXT, localCursor.getString(PrivateMmsEntry.RETRIEVE_TEXT_INDEX));
        values.put(Telephony.Mms.RETRIEVE_TEXT_CHARSET, localCursor.getInt(PrivateMmsEntry.RETRIEVE_TEXT_CHARSET_INDEX));
        values.put(Telephony.Mms.READ_STATUS, localCursor.getInt(PrivateMmsEntry.READ_STATUS_INDEX));
        values.put(Telephony.Mms.CONTENT_CLASS, localCursor.getInt(PrivateMmsEntry.CONTENT_CLASS_INDEX));
        values.put(Telephony.Mms.RESPONSE_TEXT, localCursor.getString(PrivateMmsEntry.RESPONSE_TEXT_INDEX));
        values.put(Telephony.Mms.DELIVERY_TIME, localCursor.getInt(PrivateMmsEntry.DELIVERY_TIME_INDEX));
        values.put(Telephony.Mms.DELIVERY_REPORT, localCursor.getInt(PrivateMmsEntry.DELIVERY_REPORT_INDEX));
        values.put(Telephony.Mms.LOCKED, localCursor.getInt(PrivateMmsEntry.LOCKED_INDEX));
        values.put(Telephony.Mms.SEEN, localCursor.getInt(PrivateMmsEntry.SEEN_INDEX));
        values.put(Telephony.Mms.TEXT_ONLY, localCursor.getInt(PrivateMmsEntry.TEXT_ONLY_INDEX));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            values.put(Telephony.Mms.CREATOR, localCursor.getString(PrivateMmsEntry.CREATOR_INDEX));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            values.put(Telephony.Mms.SUBSCRIPTION_ID, localCursor.getLong(PrivateMmsEntry.SUBSCRIPTION_ID_INDEX));
        }
    }

    private static void bindSmsValues(ContentValues values, Cursor localCursor) {
        values.put(Telephony.Sms.ADDRESS,
                localCursor.getString(PrivateSmsEntry.ADDRESS_INDEX));
        //Telephony.Sms.Inbox.PERSON relates to the id of the deprecated Contacts.People._ID
        String personId = localCursor.getString(PrivateSmsEntry.PERSON_INDEX);
        if (!TextUtils.isEmpty(personId)) {
            values.put(Telephony.Sms.PERSON, personId);
        }
        values.put(Telephony.Sms.DATE,
                localCursor.getLong(PrivateSmsEntry.DATE_INDEX));
        values.put(Telephony.Sms.DATE_SENT,
                localCursor.getLong(PrivateSmsEntry.DATE_SEND_INDEX));
        values.put(Telephony.Sms.PROTOCOL,
                localCursor.getInt(PrivateSmsEntry.PROTOCOL_INDEX));
        values.put(Telephony.Sms.READ,
                localCursor.getInt(PrivateSmsEntry.READ_INDEX));
        values.put(Telephony.Sms.STATUS,
                localCursor.getInt(PrivateSmsEntry.STATUS_INDEX));
        values.put(Telephony.Sms.TYPE,
                localCursor.getInt(PrivateSmsEntry.TYPE_INDEX));
        values.put(Telephony.Sms.REPLY_PATH_PRESENT,
                localCursor.getInt(PrivateSmsEntry.REPLY_PATH_PRESENT_INDEX));
        values.put(Telephony.Sms.SUBJECT,
                localCursor.getString(PrivateSmsEntry.SUBJECT_INDEX));
        values.put(Telephony.Sms.BODY,
                localCursor.getString(PrivateSmsEntry.BODY_INDEX));
        values.put(Telephony.Sms.SERVICE_CENTER,
                localCursor.getString(PrivateSmsEntry.SERVICE_CENTER_INDEX));
        values.put(Telephony.Sms.LOCKED,
                localCursor.getInt(PrivateSmsEntry.LOCKED_INDEX));
        values.put(Telephony.Sms.ERROR_CODE,
                localCursor.getInt(PrivateSmsEntry.ERROR_CODE_INDEX));
        values.put(Telephony.Sms.SEEN,
                localCursor.getInt(PrivateSmsEntry.SEEN_INDEX));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            values.put(Telephony.Sms.CREATOR,
                    localCursor.getString(PrivateSmsEntry.CREATOR_INDEX));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            values.put(Telephony.Sms.SUBSCRIPTION_ID,
                    localCursor.getLong(PrivateSmsEntry.SUBSCRIPTION_ID_INDEX));
        }
    }

    private static void bindMmsAddrValues(ContentValues values, Cursor cursor) {
        values.put(PrivateMmsEntry.Addr.ADDRESS,
                cursor.getString(cursor.getColumnIndex(Telephony.Mms.Addr.ADDRESS)));
        values.put(PrivateMmsEntry.Addr.CHARSET,
                cursor.getString(cursor.getColumnIndex(Telephony.Mms.Addr.CHARSET)));
        values.put(PrivateMmsEntry.Addr.CONTACT_ID,
                cursor.getString(cursor.getColumnIndex(Telephony.Mms.Addr.CONTACT_ID)));
        values.put(PrivateMmsEntry.Addr.MSG_ID,
                cursor.getString(cursor.getColumnIndex(Telephony.Mms.Addr.MSG_ID)));
        values.put(PrivateMmsEntry.Addr.TYPE,
                cursor.getString(cursor.getColumnIndex(Telephony.Mms.Addr.TYPE)));
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
