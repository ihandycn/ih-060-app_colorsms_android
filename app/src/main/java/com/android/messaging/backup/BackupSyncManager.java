package com.android.messaging.backup;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.provider.Telephony;
import android.text.TextUtils;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.mmslib.SqliteWrapper;
import com.android.messaging.sms.MmsUtils;
import com.android.messaging.util.OsUtil;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.List;

public class BackupSyncManager {
    static final long SYNC_FAILED = Long.MIN_VALUE;
    private static final String ORDER_BY_DATE_DESC = Telephony.Sms.DATE + " DESC";
    private static final String TAG = "-->>";

    private static BackupSyncManager sInstance;

    public static BackupSyncManager get() {
        if (sInstance == null) {
            sInstance = new BackupSyncManager();
        }
        return sInstance;
    }

    private BackupSyncManager() {

    }

    public long sync() {
        if (!OsUtil.hasSmsPermission()) {
            return SYNC_FAILED;
        }
        final DatabaseWrapper db = DataModel.get().getDatabase();
        db.delete(BackupDatabaseHelper.BACKUP_MESSAGE_TABLE,
                BackupDatabaseHelper.MessageColumn.HIDDEN + "=0",
                null);

        List<BackupSmsMessage> addList = new ArrayList<>();
        Cursor cursor = null;
        try {
            final Context context = HSApplication.getContext();
            cursor = SqliteWrapper.query(
                    context,
                    context.getContentResolver(),
                    Telephony.Sms.CONTENT_URI,
                    BackupDatabaseHelper.MessageColumn.getProjection(),
                    MmsUtils.getSmsTypeSelectionSql(),
                    null /* selectionArgs */,
                    ORDER_BY_DATE_DESC);
            if (cursor == null) {
                return SYNC_FAILED;
            }

            if (cursor.getCount() == 0) {
                return 0;
            }
            while (cursor.moveToNext()) {
                BackupSmsMessage smsMessage = new BackupSmsMessage();
                smsMessage.load(cursor);
                addList.add(smsMessage);
            }
        } catch (final SQLiteException e) {
            HSLog.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        try {
            db.beginTransaction();
            for (BackupSmsMessage sms : addList) {
                storeSms(db, sms);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            return SYNC_FAILED;
        } finally {
            db.endTransaction();
        }

        return addList.size();
    }

    private void storeSms(final DatabaseWrapper db, final BackupSmsMessage sms) {
        if (sms.mBody == null) {
            sms.mBody = "";
        }

        ContentValues values = new ContentValues();
        values.put(BackupDatabaseHelper.MessageColumn.MESSAGE_ID, sms.mMessageId);
        values.put(BackupDatabaseHelper.MessageColumn.THREAD_ID, sms.mThreadId);
        values.put(BackupDatabaseHelper.MessageColumn.ADDRESS, sms.mAddress);
        values.put(BackupDatabaseHelper.MessageColumn.PERSON, sms.mPerson);
        values.put(BackupDatabaseHelper.MessageColumn.DATE, sms.mDate);
        values.put(BackupDatabaseHelper.MessageColumn.DATE_SEND, sms.mDateSend);
        values.put(BackupDatabaseHelper.MessageColumn.PROTOCOL, sms.mProtocol);
        values.put(BackupDatabaseHelper.MessageColumn.READ, sms.mRead);
        values.put(BackupDatabaseHelper.MessageColumn.STATUS, sms.mStatus);
        values.put(BackupDatabaseHelper.MessageColumn.TYPE, sms.mType);
        values.put(BackupDatabaseHelper.MessageColumn.REPLY_PATH_PRESENT, sms.mReplyPathPresent);
        if (TextUtils.isEmpty(sms.mSubject)) {
            values.put(BackupDatabaseHelper.MessageColumn.SUBJECT, "");
        }
        values.put(BackupDatabaseHelper.MessageColumn.BODY, sms.mBody);
        values.put(BackupDatabaseHelper.MessageColumn.SERVICE_CENTER, sms.mServiceCenter);
        values.put(BackupDatabaseHelper.MessageColumn.LOCKED, sms.mLocked);
        values.put(BackupDatabaseHelper.MessageColumn.ERROR_CODE, sms.mErrorCode);
        values.put(BackupDatabaseHelper.MessageColumn.SEEN, sms.mSeen);
        if (OsUtil.isAtLeastL_MR1()) {
            values.put(BackupDatabaseHelper.MessageColumn.SUBSCRIPTION_ID, sms.mSubId);
        }
        db.insert(BackupDatabaseHelper.BACKUP_MESSAGE_TABLE, null, values);
    }
}
