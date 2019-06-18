package com.android.messaging.backup;

import android.os.Build;
import android.provider.Telephony;

import com.android.messaging.sms.MmsUtils;

import java.util.Arrays;

public class BackupDatabaseHelper {

    public static final String BACKUP_MESSAGE_TABLE = "bk_ms";

    public static class MessageColumn {
        static final String _ID = "key_id";
        //HIDDEN 1, user backup this message and use 'delete message 30 day ago'
        //same as the telephony sms table
        static final String HIDDEN = "hidden";
        static final String MESSAGE_ID = Telephony.Sms._ID;
        static final String THREAD_ID = Telephony.Sms.THREAD_ID;
        static final String ADDRESS = Telephony.Sms.ADDRESS;
        /**
         * The ID of the sender of the conversation, if present.
         * <P>Type: INTEGER (reference to item in {@code content://contacts/people})</P>
         */
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
        static final String ERROR_CODE = Telephony.Sms.ERROR_CODE;
        static final String SEEN = Telephony.Sms.SEEN;
        //Telephony.Sms.SUBSCRIPTION_ID
        static final String SUBSCRIPTION_ID = "sub_id";

        private static int s = 0 ;
        static final int INDEX_MESSAGE_ID = s++;
        static final int INDEX_THREAD_ID = s++;
        static final int INDEX_ADDRESS = s++;
        static final int INDEX_PERSON = s++;
        static final int INDEX_DATE = s++;
        static final int INDEX_DATE_SEND = s++;
        static final int INDEX_PROTOCOL = s++;
        static final int INDEX_READ = s++;
        static final int INDEX_STATUS = s++;
        static final int INDEX_TYPE = s++;
        static final int INDEX_REPLY_PATH_PRESENT = s++;
        static final int INDEX_SUBJECT = s++;
        static final int INDEX_BODY = s++;
        static final int INDEX_SERVICE_CENTER = s++;
        static final int INDEX_LOCKED = s++;
        static final int INDEX_ERROR_CODE = s++;
        static final int INDEX_SEEN = s++;
        static final int INDEX_SUBSCRIPTION_ID = s++;

        private static String[] sProjection;

        public static String[] getProjection() {
            if (sProjection == null) {
                String[] projection = new String[]{
                        MESSAGE_ID,
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
                        SUBSCRIPTION_ID
                };

                if (!MmsUtils.hasSmsDateSentColumn()) {
                    projection[INDEX_DATE_SEND] = Telephony.Sms.DATE;
                }

                int length = projection.length;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
                    length--;
                }
                sProjection = Arrays.copyOfRange(projection, 0, length);
            }
            return sProjection;
        }

        public static final String CREATE_BACKUP_TABLE_SQL = "CREATE TABLE " + BACKUP_MESSAGE_TABLE + "("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MESSAGE_ID + " INTEGER,"
                + THREAD_ID + " TEXT,"
                + ADDRESS + " TEXT,"
                + PERSON + " TEXT,"
                + DATE + " TEXT,"
                + DATE_SEND + " TEXT, "
                + PROTOCOL + " TEXT, "
                + READ + " INTEGER, "
                + SEEN + " INTEGER, "
                + STATUS + " TEXT, "
                + TYPE + " INTEGER, "
                + REPLY_PATH_PRESENT + " TEXT, "
                + SUBJECT + " TEXT, "
                + BODY + " TEXT, "
                + SERVICE_CENTER + " TEXT, "
                + LOCKED + " TEXT, "
                + ERROR_CODE + " TEXT, "
                + SUBSCRIPTION_ID + " TEXT, "
                + HIDDEN + " INT DEFAULT(0))";
    }
}
