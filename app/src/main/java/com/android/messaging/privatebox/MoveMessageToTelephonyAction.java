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

import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.action.Action;
import com.ihs.app.framework.HSApplication;

import java.util.ArrayList;
import java.util.List;

public class MoveMessageToTelephonyAction extends Action {

    public static void move(String messageId) {
        MoveMessageToTelephonyAction action = new MoveMessageToTelephonyAction(messageId);
        action.start();
    }

    private MoveMessageToTelephonyAction(String messageId) {
        actionParameters.putString("message_id", messageId);
    }

    @Override
    protected Object executeAction() {
        String messageId = actionParameters.getString("message_id");
        moveMessageToTelephony(messageId);
        return null;
    }

    private boolean moveMessageToTelephony(String msgId) {
        ContentResolver resolver = HSApplication.getContext().getContentResolver();
        ContentValues values = new ContentValues();
        DatabaseWrapper db = DataModel.get().getDatabase();

        Cursor c = db.query(DatabaseHelper.MESSAGES_TABLE,
                new String[]{DatabaseHelper.MessageColumns.SMS_MESSAGE_URI},
                DatabaseHelper.MessageColumns._ID + " =? ", new String[]{msgId},
                null, null, null);

        Uri messageUri = null;
        if (c != null && c.moveToFirst()) {
            if (!TextUtils.isEmpty(c.getString(0))) {
                messageUri = Uri.parse(c.getString(0));
            }
        }
        if (c != null) {
            c.close();
        }
        if (messageUri == null) {
            return false;
        }

        if (!messageUri.toString().contains("mms")) {
            Cursor localSmsCursor = resolver.query(messageUri,
                    PrivateSmsEntry.sProjection, null, null, null);
            if (localSmsCursor == null) {
                return false;
            }
            if (localSmsCursor.moveToFirst()) {
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
                    return false;
                }
            }
            localSmsCursor.close();
        } else {
            Cursor localMmsCursor = resolver.query(messageUri, PrivateMmsEntry.sProjection,
                    null, null, null);
            if (localMmsCursor == null) {
                return false;
            }
            if (localMmsCursor.moveToFirst()) {
                bindMmsValues(values, localMmsCursor);
                //insert into telephony
                Uri uri = resolver.insert(Telephony.Mms.CONTENT_URI, values);
                // update uri
                if (uri == null) {
                    localMmsCursor.close();
                    return false;
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
            }
            localMmsCursor.close();
        }
        return true;
    }

    private static void bindMmsValues(ContentValues values, Cursor localCursor) {
        values.put(Telephony.Mms.THREAD_ID,
                localCursor.getLong(localCursor.getColumnIndex(PrivateMmsEntry.THREAD_ID)));
        values.put(Telephony.Mms.DATE,
                localCursor.getLong(localCursor.getColumnIndex(PrivateMmsEntry.DATE)));
        values.put(Telephony.Mms.DATE_SENT,
                localCursor.getLong(localCursor.getColumnIndex(PrivateMmsEntry.DATE_SENT)));
        values.put(Telephony.Mms.MESSAGE_BOX,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.MESSAGE_BOX)));
        values.put(Telephony.Mms.READ,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.READ)));
        values.put(Telephony.Mms.MESSAGE_ID,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.MESSAGE_ID)));
        values.put(Telephony.Mms.SUBJECT,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.SUBJECT)));
        values.put(Telephony.Mms.SUBJECT_CHARSET,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.SUBJECT_CHARSET)));
        values.put(Telephony.Mms.CONTENT_TYPE,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.CONTENT_TYPE)));
        values.put(Telephony.Mms.CONTENT_LOCATION,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.CONTENT_LOCATION)));
        values.put(Telephony.Mms.EXPIRY,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.EXPIRY)));
        values.put(Telephony.Mms.MESSAGE_CLASS,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.MESSAGE_CLASS)));
        values.put(Telephony.Mms.MESSAGE_TYPE,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.MESSAGE_TYPE)));
        values.put(Telephony.Mms.MMS_VERSION,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.MMS_VERSION)));
        values.put(Telephony.Mms.MESSAGE_SIZE,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.MESSAGE_SIZE)));
        values.put(Telephony.Mms.PRIORITY,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.PRIORITY)));
        values.put(Telephony.Mms.READ_REPORT,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.READ_REPORT)));
        values.put(Telephony.Mms.REPORT_ALLOWED,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.REPORT_ALLOWED)));
        values.put(Telephony.Mms.RESPONSE_STATUS,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.RESPONSE_STATUS)));
        values.put(Telephony.Mms.STATUS,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.STATUS)));
        values.put(Telephony.Mms.TRANSACTION_ID,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.TRANSACTION_ID)));
        values.put(Telephony.Mms.RETRIEVE_STATUS,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.RETRIEVE_STATUS)));
        values.put(Telephony.Mms.RETRIEVE_TEXT,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.RETRIEVE_TEXT)));
        values.put(Telephony.Mms.RETRIEVE_TEXT_CHARSET,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.RETRIEVE_TEXT_CHARSET)));
        values.put(Telephony.Mms.READ_STATUS,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.READ_STATUS)));
        values.put(Telephony.Mms.CONTENT_CLASS,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.CONTENT_CLASS)));
        values.put(Telephony.Mms.RESPONSE_TEXT,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.RESPONSE_TEXT)));
        values.put(Telephony.Mms.DELIVERY_TIME,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.DELIVERY_TIME)));
        values.put(Telephony.Mms.DELIVERY_REPORT,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.DELIVERY_REPORT)));
        values.put(Telephony.Mms.LOCKED,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.LOCKED)));
        values.put(Telephony.Mms.SEEN,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.SEEN)));
        values.put(Telephony.Mms.TEXT_ONLY,
                localCursor.getInt(localCursor.getColumnIndex(PrivateMmsEntry.TEXT_ONLY)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            values.put(Telephony.Mms.SUBSCRIPTION_ID,
                    localCursor.getLong(localCursor.getColumnIndex(PrivateMmsEntry.SUBSCRIPTION_ID)));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            values.put(Telephony.Mms.CREATOR,
                    localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.CREATOR)));
        }
    }

    private static void bindSmsValues(ContentValues values, Cursor localCursor) {
        values.put(Telephony.Sms.THREAD_ID,
                localCursor.getLong(localCursor.getColumnIndex(PrivateSmsEntry.THREAD_ID)));
        values.put(Telephony.Sms.ADDRESS,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.ADDRESS)));
        values.put(Telephony.Sms.PERSON,
                localCursor.getInt(localCursor.getColumnIndex(PrivateSmsEntry.PERSON)));
        values.put(Telephony.Sms.DATE,
                localCursor.getLong(localCursor.getColumnIndex(PrivateSmsEntry.DATE)));
        values.put(Telephony.Sms.DATE_SENT,
                localCursor.getLong(localCursor.getColumnIndex(PrivateSmsEntry.DATE_SEND)));
        values.put(Telephony.Sms.PROTOCOL,
                localCursor.getInt(localCursor.getColumnIndex(PrivateSmsEntry.PROTOCOL)));
        values.put(Telephony.Sms.READ,
                localCursor.getInt(localCursor.getColumnIndex(PrivateSmsEntry.READ)));
        values.put(Telephony.Sms.STATUS,
                localCursor.getInt(localCursor.getColumnIndex(PrivateSmsEntry.STATUS)));
        values.put(Telephony.Sms.TYPE,
                localCursor.getInt(localCursor.getColumnIndex(PrivateSmsEntry.TYPE)));
        values.put(Telephony.Sms.REPLY_PATH_PRESENT,
                localCursor.getInt(localCursor.getColumnIndex(PrivateSmsEntry.REPLY_PATH_PRESENT)));
        values.put(Telephony.Sms.SUBJECT,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.SUBJECT)));
        values.put(Telephony.Sms.BODY,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.BODY)));
        values.put(Telephony.Sms.SERVICE_CENTER,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.SERVICE_CENTER)));
        values.put(Telephony.Sms.LOCKED,
                localCursor.getInt(localCursor.getColumnIndex(PrivateSmsEntry.LOCKED)));
        values.put(Telephony.Sms.ERROR_CODE,
                localCursor.getInt(localCursor.getColumnIndex(PrivateSmsEntry.ERROR_CODE)));
        values.put(Telephony.Sms.SEEN,
                localCursor.getInt(localCursor.getColumnIndex(PrivateSmsEntry.SEEN)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            values.put(Telephony.Sms.SUBSCRIPTION_ID,
                    localCursor.getLong(localCursor.getColumnIndex(PrivateSmsEntry.SUBSCRIPTION_ID)));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            values.put(Telephony.Sms.CREATOR,
                    localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.CREATOR)));
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