package com.android.messaging.ui.emoji;

import android.os.Parcel;

public class EmojiInfo extends BaseEmojiInfo {

    public EmojiInfo() {
        mEmojiType = EmojiType.EMOJI;
    }

    public String mEmoji;


    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mEmoji);
    }

    public static EmojiInfo unflatten(String flatten){
        EmojiInfo info = new EmojiInfo();
        info.mEmoji = flatten;
        return info;
    }

    @Override
    public String toString() {
        return mEmoji;
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
