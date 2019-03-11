package com.android.messaging.datamodel.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class MessageBoxItemData implements Parcelable {

    private String mConversationId;
    private String mSelfId;
    private String mAvatarUri;
    private String mConversationName;
    private String mContent;

    public MessageBoxItemData(@NonNull String conversationId,
                              @NonNull String selfId,
                              @NonNull String avatarUri,
                              @NonNull String conversationName,
                              @NonNull String content) {
        mConversationId = conversationId;
        mSelfId = selfId;
        mAvatarUri = avatarUri;
        mConversationName = conversationName;
        mContent = content;
    }


    public String getAvatarUri() {
        return mAvatarUri;
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

    protected MessageBoxItemData(Parcel in) {
        mConversationId = in.readString();
        mSelfId = in.readString();
        mAvatarUri = in.readString();
        mConversationName = in.readString();
        mContent = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mConversationId);
        dest.writeString(mSelfId);
        dest.writeString(mAvatarUri);
        dest.writeString(mConversationName);
        dest.writeString(mContent);
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

    @Override
    public String toString() {
        return "override this method to avoid flurry passing privacy information";
    }
}
