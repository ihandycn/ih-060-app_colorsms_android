package com.android.messaging.privatebox;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.Telephony;

import static com.android.messaging.privatebox.PrivateMessageContentProvider.BASE_CONTENT_URI;
import static com.android.messaging.privatebox.PrivateMessageContentProvider.SMS_PATH;

public class PrivateSmsEntry implements BaseColumns {

    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(SMS_PATH).build();

    static Uri buildUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    static final String SMS_MESSAGE_TABLE = "sms_message_table";

    static final String _ID = Telephony.Sms._ID;
    static final String THREAD_ID = Telephony.Sms.THREAD_ID;
    static final String ADDRESS = Telephony.Sms.ADDRESS;
    static final String PERSON = Telephony.Sms.PERSON;
    static final String DATE = Telephony.Sms.DATE;
    static final String DATE_SEND = Telephony.Sms.DATE_SENT;
    static final String PROTOCOL = Telephony.Sms.PROTOCOL;
    static final String READ = Telephony.Sms.READ;
    static final String STATUS = Telephony.Sms.STATUS;
    static final String TYPE = Telephony.Sms.TYPE;
    static final String REPLY_PATH_PRESENT = Telephony.Sms.REPLY_PATH_PRESENT;
    static final String SUBJECT = Telephony.Sms.SUBJECT;
    static final String BODY = Telephony.Sms.BODY;
    static final String SERVICE_CENTER = Telephony.Sms.SERVICE_CENTER;
    static final String LOCKED = Telephony.Sms.LOCKED;
    //Telephony.Sms.SUBSCRIPTION_ID
    static final String SUBSCRIPTION_ID = "sub_id";
    static final String ERROR_CODE = Telephony.Sms.ERROR_CODE;
    //Telephony.Sms.CREATOR
    static final String CREATOR = "creator";
    static final String SEEN = Telephony.Sms.SEEN;

    public static final String[] sProjection = {
            _ID,
            THREAD_ID,
            ADDRESS,
            PERSON,
            DATE,
            DATE_SEND,
            PROTOCOL,
            READ,
            STATUS,
            TYPE,
            REPLY_PATH_PRESENT,
            SUBJECT,
            BODY,
            SERVICE_CENTER,
            LOCKED,
            SUBSCRIPTION_ID,
            ERROR_CODE,
            CREATOR,
            SEEN
    };

    public static final String CREATE_SMS_TABLE_SQL =
            "CREATE TABLE " + SMS_MESSAGE_TABLE + "("
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + THREAD_ID + " TEXT,"
                    + ADDRESS + " TEXT,"
                    + PERSON + " TEXT,"
                    + DATE + " TEXT,"
                    + DATE_SEND + " TEXT, "
                    + PROTOCOL + " TEXT, "
                    + READ + " TEXT, "
                    + STATUS + " TEXT, "
                    + TYPE + " TEXT, "
                    + REPLY_PATH_PRESENT + " TEXT, "
                    + SUBJECT + " TEXT, "
                    + BODY + " TEXT, "
                    + SERVICE_CENTER + " TEXT, "
                    + LOCKED + " TEXT, "
                    + SUBSCRIPTION_ID + " TEXT, "
                    + ERROR_CODE + " TEXT, "
                    + CREATOR + " TEXT, "
                    + SEEN + " TEXT)";
}
