package com.android.messaging.privatebox;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.Telephony;

import java.util.Arrays;

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

    private static int s = 0;
    public static int _ID_INDEX = s++;
    public static int THREAD_ID_INDEX = s++;
    public static int ADDRESS_INDEX = s++;
    public static int PERSON_INDEX = s++;
    public static int DATE_INDEX = s++;
    public static int DATE_SEND_INDEX = s++;
    public static int PROTOCOL_INDEX = s++;
    public static int READ_INDEX = s++;
    public static int STATUS_INDEX = s++;
    public static int TYPE_INDEX = s++;
    public static int REPLY_PATH_PRESENT_INDEX = s++;
    public static int SUBJECT_INDEX = s++;
    public static int BODY_INDEX = s++;
    public static int SERVICE_CENTER_INDEX = s++;
    public static int LOCKED_INDEX = s++;
    public static int ERROR_CODE_INDEX = s++;
    public static int SEEN_INDEX = s++;
    public static int CREATOR_INDEX = s++;
    public static int SUBSCRIPTION_ID_INDEX = s++;

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
            ERROR_CODE,
            SEEN,
            //target Build.VERSION_CODES.LOLLIPOP
            CREATOR,
            //target Build.VERSION_CODES.LOLLIPOP_MR1
            SUBSCRIPTION_ID,
    };

    public static String[] getProjection() {
        int length = sProjection.length;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            length--;
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            length--;
        }
        return Arrays.copyOfRange(sProjection, 0, length);
    }

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
