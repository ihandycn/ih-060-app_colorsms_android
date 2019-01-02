package com.android.messaging.ui.emoji;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class BaseEmojiInfo implements Parcelable {
    public EmojiType mEmojiType;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mEmojiType == null ? -1 : this.mEmojiType.ordinal());
    }

    public BaseEmojiInfo() {
    }

    protected BaseEmojiInfo(Parcel in) {
        int tmpMEmojiType = in.readInt();
        this.mEmojiType = tmpMEmojiType == -1 ? null : EmojiType.values()[tmpMEmojiType];
    }

}
