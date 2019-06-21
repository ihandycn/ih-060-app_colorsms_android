package com.android.messaging.backup;

public class BackupMessages {
    public static abstract class BackupMessage {
        public abstract long getTimestampInMillis();

        public abstract long getTelephonyId();

        @Override
        public boolean equals(final Object other) {
            if (other == null || !(other instanceof BackupMessage)) {
                return false;
            }
            final BackupMessage otherDbMsg = (BackupMessage) other;
            if (otherDbMsg.getTimestampInMillis() != getTimestampInMillis() ||
                    otherDbMsg.getTelephonyId() != getTelephonyId()) {
                return false;
            }
            return true;
        }
    }
}
// Sms copy
//        message.mMessageId
//        message.mThreadId
//        message.mAddress
//        message.mPerson
//        message.mDate
//        message.mDateSend
//        message.mProtocol
//        message.mRead
//        message.mSeen
//        message.mStatus
//        message.mType
//        message.mReplyPathPresent
//        message.mSubject
//        message.mBody
//        message.mServiceCenter
//        message.mLocked
//        message.mErrorCode
//        if (OsUtil.isAtLeastL_MR1()) {
//            message.mSubId
//        }
