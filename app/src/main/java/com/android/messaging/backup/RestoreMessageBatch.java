package com.android.messaging.backup;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.Telephony;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.sms.MmsUtils;
import com.android.messaging.util.OsUtil;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

class RestoreMessageBatch {

    private final ArrayList<BackupSmsMessage> mSmsToAdd;

    RestoreMessageBatch(final ArrayList<BackupSmsMessage> smsToAdd) {
        mSmsToAdd = smsToAdd;
    }

    void updateTelephonyDatabase() {
        ContentResolver resolver = HSApplication.getContext().getContentResolver();
        // Store all the SMS messages
        for (final BackupSmsMessage sms : mSmsToAdd) {
            storeSms(resolver, sms);
        }
    }

    private void storeSms(final ContentResolver resolver, final BackupSmsMessage sms) {
        if (sms.mBody == null) {
            sms.mBody = "";
        }

        ContentValues values = new ContentValues();
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
        values.put(BackupDatabaseHelper.MessageColumn.SUBJECT, sms.mSubject);
        values.put(BackupDatabaseHelper.MessageColumn.BODY, sms.mBody);
        values.put(BackupDatabaseHelper.MessageColumn.SERVICE_CENTER, sms.mServiceCenter);
        values.put(BackupDatabaseHelper.MessageColumn.LOCKED, sms.mLocked);
        values.put(BackupDatabaseHelper.MessageColumn.ERROR_CODE, sms.mErrorCode);
        values.put(BackupDatabaseHelper.MessageColumn.SEEN, sms.mSeen);
        if (OsUtil.isAtLeastL_MR1()) {
            values.put(BackupDatabaseHelper.MessageColumn.SUBSCRIPTION_ID, sms.mSubId);
        }
        resolver.insert(Telephony.Sms.CONTENT_URI, values);
    }
}
