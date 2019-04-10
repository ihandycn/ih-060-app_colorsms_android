package com.android.messaging.privatebox;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.Telephony;

import static com.android.messaging.privatebox.PrivateMessageContentProvider.BASE_CONTENT_URI;
import static com.android.messaging.privatebox.PrivateMessageContentProvider.MMS_PATH;

public class PrivateMmsEntry {
    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(MMS_PATH).build();

    static Uri buildUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    static final String MMS_MESSAGE_TABLE = "mms_message_table";

    public static final class Inbox implements Telephony.BaseMmsColumns {
        private Inbox() {
        }

        public static final Uri
                CONTENT_URI = PrivateMmsEntry.CONTENT_URI.buildUpon().appendPath("inbox").build();
    }

    public static final class Sent implements Telephony.BaseMmsColumns {
        private Sent() {
        }

        public static final Uri
                CONTENT_URI = PrivateMmsEntry.CONTENT_URI.buildUpon().appendPath("sent").build();
    }

    public static final class Draft implements Telephony.BaseMmsColumns {
        private Draft() {
        }

        public static final Uri
                CONTENT_URI = PrivateMmsEntry.CONTENT_URI.buildUpon().appendPath("drafts").build();
    }

    public static final class Outbox implements Telephony.BaseMmsColumns {
        private Outbox() {
        }

        public static final Uri
                CONTENT_URI = PrivateMmsEntry.CONTENT_URI.buildUpon().appendPath("outbox").build();
    }

    public static String _ID = "_id";
    public static String THREAD_ID = Telephony.Mms.THREAD_ID;
    public static String DATE = Telephony.Mms.DATE;
    static String DATE_SENT = Telephony.Mms.DATE_SENT;
    static String MESSAGE_BOX = Telephony.Mms.MESSAGE_BOX;
    public static String READ = Telephony.Mms.READ;
    public static String MESSAGE_ID = Telephony.Mms.MESSAGE_ID;
    public static String SUBJECT = Telephony.Mms.SUBJECT;
    static String SUBJECT_CHARSET = Telephony.Mms.SUBJECT_CHARSET;
    public static String CONTENT_TYPE = Telephony.Mms.CONTENT_TYPE;
    public static String CONTENT_LOCATION = Telephony.Mms.CONTENT_LOCATION;
    public static String EXPIRY = Telephony.Mms.EXPIRY;
    public static String MESSAGE_CLASS = Telephony.Mms.MESSAGE_CLASS;
    public static String MESSAGE_TYPE = Telephony.Mms.MESSAGE_TYPE;
    public static String MMS_VERSION = Telephony.Mms.MMS_VERSION;
    public static String MESSAGE_SIZE = Telephony.Mms.MESSAGE_SIZE;
    public static String PRIORITY = Telephony.Mms.PRIORITY;
    public static String READ_REPORT = Telephony.Mms.READ_REPORT;
    public static String REPORT_ALLOWED = Telephony.Mms.REPORT_ALLOWED;
    public static String RESPONSE_STATUS = Telephony.Mms.RESPONSE_STATUS;
    public static String STATUS = Telephony.Mms.STATUS;
    public static String TRANSACTION_ID = Telephony.Mms.TRANSACTION_ID;
    public static String RETRIEVE_STATUS = Telephony.Mms.RETRIEVE_STATUS;
    public static String RETRIEVE_TEXT = Telephony.Mms.RETRIEVE_TEXT;
    static String RETRIEVE_TEXT_CHARSET = Telephony.Mms.RETRIEVE_TEXT_CHARSET;
    public static String READ_STATUS = Telephony.Mms.READ_STATUS;
    public static String CONTENT_CLASS = Telephony.Mms.CONTENT_CLASS;
    static String RESPONSE_TEXT = Telephony.Mms.RESPONSE_TEXT;
    static String DELIVERY_TIME = Telephony.Mms.DELIVERY_TIME;
    static String DELIVERY_REPORT = Telephony.Mms.DELIVERY_REPORT;
    static String LOCKED = Telephony.Mms.LOCKED;
    //Telephony.Mms.SUBSCRIPTION_ID
    public static String SUBSCRIPTION_ID = "sub_id";
    public static String SEEN = Telephony.Mms.SEEN;
    //Telephony.Mms.CREATOR
    public static String CREATOR = "creator";
    static String TEXT_ONLY = Telephony.Mms.TEXT_ONLY;

    public static String[] sProjection = {
            _ID,
            THREAD_ID,
            DATE,
            DATE_SENT,
            MESSAGE_BOX,
            READ,
            MESSAGE_ID,
            SUBJECT,
            SUBJECT_CHARSET,
            CONTENT_TYPE,
            CONTENT_LOCATION,
            EXPIRY,
            MESSAGE_CLASS,
            MESSAGE_TYPE,
            MMS_VERSION,
            MESSAGE_SIZE,
            PRIORITY,
            READ_REPORT,
            REPORT_ALLOWED,
            RESPONSE_STATUS,
            STATUS,
            TRANSACTION_ID,
            RETRIEVE_STATUS,
            RETRIEVE_TEXT,
            RETRIEVE_TEXT_CHARSET,
            READ_STATUS,
            CONTENT_CLASS,
            RESPONSE_TEXT,
            DELIVERY_TIME,
            DELIVERY_REPORT,
            LOCKED,
            SUBSCRIPTION_ID,
            SEEN,
            CREATOR,
            TEXT_ONLY
    };

    public static final String CREATE_MMS_TABLE_SQL =
            "CREATE TABLE " + MMS_MESSAGE_TABLE + "("
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + THREAD_ID + " TEXT,"
                    + DATE + " TEXT,"
                    + DATE_SENT + " TEXT,"
                    + MESSAGE_BOX + " TEXT,"
                    + READ + " TEXT, "
                    + MESSAGE_ID + " TEXT, "
                    + SUBJECT + " TEXT, "
                    + SUBJECT_CHARSET + " TEXT, "
                    + CONTENT_TYPE + " TEXT, "
                    + CONTENT_LOCATION + " TEXT, "
                    + EXPIRY + " TEXT, "
                    + MESSAGE_CLASS + " TEXT, "
                    + MESSAGE_TYPE + " TEXT, "
                    + MMS_VERSION + " TEXT, "
                    + MESSAGE_SIZE + " TEXT, "
                    + PRIORITY + " TEXT, "
                    + READ_REPORT + " TEXT, "
                    + REPORT_ALLOWED + " TEXT, "
                    + RESPONSE_STATUS + " TEXT, "
                    + STATUS + " TEXT, "
                    + TRANSACTION_ID + " TEXT, "
                    + RETRIEVE_STATUS + " TEXT, "
                    + RETRIEVE_TEXT + " TEXT, "
                    + RETRIEVE_TEXT_CHARSET + " TEXT, "
                    + READ_STATUS + " TEXT, "
                    + CONTENT_CLASS + " TEXT, "
                    + RESPONSE_TEXT + " TEXT, "
                    + DELIVERY_TIME + " TEXT, "
                    + DELIVERY_REPORT + " TEXT, "
                    + LOCKED + " TEXT, "
                    + SUBSCRIPTION_ID + " TEXT, "
                    + SEEN + " TEXT, "
                    + CREATOR + " TEXT, "
                    + TEXT_ONLY + " TEXT )";
}
