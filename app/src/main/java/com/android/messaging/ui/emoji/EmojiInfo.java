package com.android.messaging.ui.emoji;

import android.os.Parcel;

public class EmojiInfo extends BaseEmojiInfo {

    EmojiInfo() {
        mEmojiType = EmojiType.EMOJI;
    }

    String mEmoji;


    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mEmoji);
    }

    private EmojiInfo(Parcel in) {
        super(in);
        this.mEmoji = in.readString();
    }

    public static final Creator<EmojiInfo> CREATOR = new Creator<EmojiInfo>() {
        @Override public EmojiInfo createFromParcel(Parcel source) {
            return new EmojiInfo(source);
        }

        @Override public EmojiInfo[] newArray(int size) {
            return new EmojiInfo[size];
        }
    };
}
