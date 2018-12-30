package com.android.messaging.ui.emoji;

import android.os.Parcel;
import android.os.Parcelable;

public class StickerInfo extends BaseEmojiInfo implements Parcelable {

    public String mStickerUrl;
    public String mMagicUrl;
    public String mSoundUrl;
    public boolean mClickable;
    public boolean mIsDownloaded;


    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mStickerUrl);
        dest.writeString(this.mMagicUrl);
        dest.writeString(this.mSoundUrl);
        dest.writeByte(this.mClickable ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mIsDownloaded ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mEmojiType.value);
    }

    public StickerInfo() {
    }

    private StickerInfo(Parcel in) {
        this.mStickerUrl = in.readString();
        this.mMagicUrl = in.readString();
        this.mSoundUrl = in.readString();
        this.mClickable = in.readByte() != 0;
        this.mIsDownloaded = in.readByte() != 0;
        int value = in.readInt();
        this.mEmojiType = EmojiType.valueOfInt(value);
    }

    public static final Parcelable.Creator<StickerInfo> CREATOR = new Parcelable.Creator<StickerInfo>() {
        @Override public StickerInfo createFromParcel(Parcel source) {
            return new StickerInfo(source);
        }

        @Override public StickerInfo[] newArray(int size) {
            return new StickerInfo[size];
        }
    };
}
