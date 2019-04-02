package com.android.messaging.datamodel.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class MessageBoxItemData implements Parcelable {

    private String mConversationId;
    private String mSelfId;
    private String mParticipantId;
    private String mPhoneNumber;
    private String mConversationName;
    private String mContent; // we use empty content to indicate MMS;
    private long mReceivedTimestamp;

    public MessageBoxItemData(@NonNull String conversationId,
                              @NonNull String selfId,
                              @NonNull String participantId,
                              @NonNull String phoneNumber,
                              @NonNull String conversationName,
                              @NonNull String content,
                              @NonNull long receivedTimestamp) {
        mConversationId = conversationId;
        mSelfId = selfId;
        mParticipantId = participantId;
        mPhoneNumber = phoneNumber;
        mConversationName = conversationName;
        mContent = content;
        mReceivedTimestamp = receivedTimestamp;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public String getContent() {
        return mContent;
    }

    public String getConversationId() {
        return mConversationId;
    }

    public String getSelfId() {
        return mSelfId;
    }

    public String getConversationName() {
        return mConversationName;
    }

    public String getParticipantId() {
        return mParticipantId;
    }

    public long getReceivedTimestamp() {
        return mReceivedTimestamp;
    }

    protected MessageBoxItemData(Parcel in) {
        mConversationId = in.readString();
        mSelfId = in.readString();
        mParticipantId = in.readString();
        mPhoneNumber = in.readString();
        mConversationName = in.readString();
        mContent = in.readString();
        mReceivedTimestamp = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mConversationId);
        dest.writeString(mSelfId);
        dest.writeString(mParticipantId);
        dest.writeString(mPhoneNumber);
        dest.writeString(mConversationName);
        dest.writeString(mContent);
        dest.writeLong(mReceivedTimestamp);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MessageBoxItemData> CREATOR = new Creator<MessageBoxItemData>() {
        @Override
        public MessageBoxItemData createFromParcel(Parcel in) {
            return new MessageBoxItemData(in);
        }

        @Override
        public MessageBoxItemData[] newArray(int size) {
            return new MessageBoxItemData[size];
        }
    };

    // override this method to avoid flurry passing privacy information
    @Override
    public String toString() {
        return "xxx";
    }
}
