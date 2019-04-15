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

import com.android.messaging.datamodel.BugleDatabaseOperations;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.action.Action;
import com.android.messaging.sms.MmsUtils;
import com.ihs.app.framework.HSApplication;

import static com.android.messaging.datamodel.data.MessageData.PROTOCOL_SMS;


public class MoveMessageToPrivateBoxAction extends Action {

    static void moveMessageToPrivateBox(String messageId) {
        MoveMessageToPrivateBoxAction action = new MoveMessageToPrivateBoxAction(messageId);
        action.start();
    }

    private MoveMessageToPrivateBoxAction(final String messageId) {
        super();
        actionParameters.putString("message_id", messageId);
    }

    @Override
    protected Object executeAction() {
        moveToPrivateBox();
        return null;
    }

    private boolean moveToPrivateBox() {
        String messageId = actionParameters.getString("message_id");
        DatabaseWrapper db = DataModel.get().getDatabase();
        ContentResolver resolver = HSApplication.getContext().getContentResolver();
        Cursor cursor = db.query(DatabaseHelper.MESSAGES_TABLE,
                new String[]{DatabaseHelper.MessageColumns.SMS_MESSAGE_URI, DatabaseHelper.MessageColumns.PROTOCOL},
                DatabaseHelper.MessageColumns._ID + " =?", new String[]{messageId},
                null, null, null);

        if (cursor == null) {
            return false;
        }

        if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
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
                    return false;
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
                ContentValues values = new ContentValues();
                bindMmsValues(values, mmsCursor);
                Uri localUri = resolver.insert(PrivateMmsEntry.CONTENT_URI, values);
                if (localUri == null) {
                    mmsCursor.close();
                    return false;
                }
                values.clear();
                values.put(DatabaseHelper.MessageColumns.SMS_MESSAGE_URI, localUri.toString());
                BugleDatabaseOperations.updateMessageRow(db, messageId, values);

                //set part msg_id
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
                //copy message addr
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
        return true;
    }

    private static void bindSmsValues(ContentValues values, Cursor localCursor) {
        values.put(PrivateSmsEntry.THREAD_ID,
                localCursor.getString(localCursor.getColumnIndex(Telephony.Sms.THREAD_ID)));
        values.put(Telephony.Sms.ADDRESS,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.ADDRESS)));
        values.put(Telephony.Sms.PERSON,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.PERSON)));
        values.put(Telephony.Sms.DATE,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.DATE)));
        values.put(Telephony.Sms.DATE_SENT,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.DATE_SEND)));
        values.put(Telephony.Sms.PROTOCOL,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.PROTOCOL)));
        values.put(Telephony.Sms.READ,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.READ)));
        values.put(Telephony.Sms.STATUS,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.STATUS)));
        values.put(Telephony.Sms.TYPE,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.TYPE)));
        values.put(Telephony.Sms.REPLY_PATH_PRESENT,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.REPLY_PATH_PRESENT)));
        values.put(Telephony.Sms.SUBJECT,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.SUBJECT)));
        values.put(Telephony.Sms.BODY,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.BODY)));
        values.put(Telephony.Sms.SERVICE_CENTER,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.SERVICE_CENTER)));
        values.put(Telephony.Sms.LOCKED,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.LOCKED)));
        values.put(Telephony.Sms.ERROR_CODE,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.ERROR_CODE)));
        values.put(Telephony.Sms.SEEN,
                localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.SEEN)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            values.put(Telephony.Sms.SUBSCRIPTION_ID,
                    localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.SUBSCRIPTION_ID)));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            values.put(Telephony.Sms.CREATOR,
                    localCursor.getString(localCursor.getColumnIndex(PrivateSmsEntry.CREATOR)));
        }
    }

    private static void bindMmsValues(ContentValues values, Cursor localCursor) {
        values.put(Telephony.Mms.THREAD_ID,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.THREAD_ID)));
        values.put(Telephony.Mms.DATE,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.DATE)));
        values.put(Telephony.Mms.DATE_SENT,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.DATE_SENT)));
        values.put(Telephony.Mms.MESSAGE_BOX,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.MESSAGE_BOX)));
        values.put(Telephony.Mms.READ,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.READ)));
        values.put(Telephony.Mms.MESSAGE_ID,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.MESSAGE_ID)));
        values.put(Telephony.Mms.SUBJECT,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.SUBJECT)));
        values.put(Telephony.Mms.SUBJECT_CHARSET,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.SUBJECT_CHARSET)));
        values.put(Telephony.Mms.CONTENT_TYPE,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.CONTENT_TYPE)));
        values.put(Telephony.Mms.CONTENT_LOCATION,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.CONTENT_LOCATION)));
        values.put(Telephony.Mms.EXPIRY,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.EXPIRY)));
        values.put(Telephony.Mms.MESSAGE_CLASS,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.MESSAGE_CLASS)));
        values.put(Telephony.Mms.MESSAGE_TYPE,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.MESSAGE_TYPE)));
        values.put(Telephony.Mms.MMS_VERSION,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.MMS_VERSION)));
        values.put(Telephony.Mms.MESSAGE_SIZE,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.MESSAGE_SIZE)));
        values.put(Telephony.Mms.PRIORITY,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.PRIORITY)));
        values.put(Telephony.Mms.READ_REPORT,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.READ_REPORT)));
        values.put(Telephony.Mms.REPORT_ALLOWED,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.REPORT_ALLOWED)));
        values.put(Telephony.Mms.RESPONSE_STATUS,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.RESPONSE_STATUS)));
        values.put(Telephony.Mms.STATUS,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.STATUS)));
        values.put(Telephony.Mms.TRANSACTION_ID,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.TRANSACTION_ID)));
        values.put(Telephony.Mms.RETRIEVE_STATUS,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.RETRIEVE_STATUS)));
        values.put(Telephony.Mms.RETRIEVE_TEXT,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.RETRIEVE_TEXT)));
        values.put(Telephony.Mms.RETRIEVE_TEXT_CHARSET,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.RETRIEVE_TEXT_CHARSET)));
        values.put(Telephony.Mms.READ_STATUS,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.READ_STATUS)));
        values.put(Telephony.Mms.CONTENT_CLASS,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.CONTENT_CLASS)));
        values.put(Telephony.Mms.RESPONSE_TEXT,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.RESPONSE_TEXT)));
        values.put(Telephony.Mms.DELIVERY_TIME,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.DELIVERY_TIME)));
        values.put(Telephony.Mms.DELIVERY_REPORT,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.DELIVERY_REPORT)));
        values.put(Telephony.Mms.LOCKED,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.LOCKED)));
        values.put(Telephony.Mms.SEEN,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.SEEN)));
        values.put(Telephony.Mms.TEXT_ONLY,
                localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.TEXT_ONLY)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            values.put(Telephony.Mms.SUBSCRIPTION_ID,
                    localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.SUBSCRIPTION_ID)));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            values.put(Telephony.Mms.CREATOR,
                    localCursor.getString(localCursor.getColumnIndex(PrivateMmsEntry.CREATOR)));
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
