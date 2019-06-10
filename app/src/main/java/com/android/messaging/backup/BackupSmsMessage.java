package com.android.messaging.backup;

import android.database.Cursor;
import android.text.TextUtils;

import com.android.messaging.util.OsUtil;

public class BackupSmsMessage extends BackupMessages.BackupMessage{

    public long mId;
    public long mMessageId;
    public long mThreadId;
    public String mAddress;
    public long mPerson;
    public long mDate;
    public long mDateSend;
    public int mProtocol;
    public int mRead;
    public int mSeen;
    public int mStatus;
    public int mType;
    public int mReplyPathPresent;
    public String mSubject;
    public String mBody;
    public String mServiceCenter;
    public int mLocked;
    public int mErrorCode;
    public int mSubId;

    public void load(Cursor cursor) {
        mMessageId = cursor.getLong(BackupDatabaseHelper.MessageColumn.INDEX_MESSAGE_ID);
        mThreadId = cursor.getLong(BackupDatabaseHelper.MessageColumn.INDEX_THREAD_ID);
        mAddress = cursor.getString(BackupDatabaseHelper.MessageColumn.INDEX_ADDRESS);
        mPerson = cursor.getLong(BackupDatabaseHelper.MessageColumn.INDEX_PERSON);
        mDate = cursor.getLong(BackupDatabaseHelper.MessageColumn.INDEX_DATE);
        mDateSend = cursor.getLong(BackupDatabaseHelper.MessageColumn.INDEX_DATE_SEND);
        mProtocol = cursor.getInt(BackupDatabaseHelper.MessageColumn.INDEX_PROTOCOL);
        mRead = cursor.getInt(BackupDatabaseHelper.MessageColumn.INDEX_READ);
        mSeen = cursor.getInt(BackupDatabaseHelper.MessageColumn.INDEX_SEEN);
        mStatus = cursor.getInt(BackupDatabaseHelper.MessageColumn.INDEX_STATUS);
        mType = cursor.getInt(BackupDatabaseHelper.MessageColumn.INDEX_TYPE);
        mReplyPathPresent = cursor.getInt(BackupDatabaseHelper.MessageColumn.INDEX_REPLY_PATH_PRESENT);
        mSubject = cursor.getString(BackupDatabaseHelper.MessageColumn.INDEX_SUBJECT);
        mBody = cursor.getString(BackupDatabaseHelper.MessageColumn.INDEX_BODY);
        mServiceCenter = cursor.getString(BackupDatabaseHelper.MessageColumn.INDEX_SERVICE_CENTER);
        mLocked = cursor.getInt(BackupDatabaseHelper.MessageColumn.INDEX_LOCKED);
        mErrorCode = cursor.getInt(BackupDatabaseHelper.MessageColumn.INDEX_ERROR_CODE);
        if (OsUtil.isAtLeastL_MR1()) {
            mSubId = cursor.getInt(BackupDatabaseHelper.MessageColumn.INDEX_SUBSCRIPTION_ID);
        }
    }

    @Override
    public long getTimestampInMillis() {
        return mDate;
    }

    @Override
    public long getTelephonyId() {
        return mMessageId;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof BackupSmsMessage)) {
            return false;
        }
        final BackupSmsMessage otherDbMsg = (BackupSmsMessage) other;

        if (otherDbMsg.getTimestampInMillis() != getTimestampInMillis()) {
            return false;
        }

        if (otherDbMsg.getTelephonyId() != getTelephonyId()) {
            return false;
        }

        if (TextUtils.isEmpty(mBody) && TextUtils.isEmpty(otherDbMsg.mBody)
                && otherDbMsg.mBody.equals(mBody)) {
            return false;
        }

        if (TextUtils.isEmpty(mAddress) && TextUtils.isEmpty(otherDbMsg.mAddress)
                && otherDbMsg.mAddress.equals(mAddress)) {
            return false;
        }
        return true;
    }
}
