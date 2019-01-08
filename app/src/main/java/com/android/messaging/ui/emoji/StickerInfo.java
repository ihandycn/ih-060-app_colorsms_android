package com.android.messaging.ui.emoji;

import android.graphics.Rect;
import android.os.Parcel;
import android.text.TextUtils;

import com.android.messaging.download.Downloader;

public class StickerInfo extends BaseEmojiInfo {

    public String mStickerUrl;
    public String mMagicUrl;
    public String mSoundUrl;
    boolean mClickable;
    public boolean mIsDownloaded;
    public String mPackageName;

    public int mStickerHeight;
    public int mStickerWidth;
    public Rect mStartRect;

    public static String getNumFromUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        boolean isStart = false;
        char[] chars = url.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {
            char c = chars[i];
            if (c == '.') {
                isStart = true;
                continue;
            }
            if (c == '/') {
                break;
            }
            if (isStart) {
                result.insert(0, c);
            }
        }

        return result.toString();
    }

    public static StickerInfo unflatten(String flatten) {
        StickerInfo result = new StickerInfo();
        String[] split = flatten.split("\\|");
        if (split.length != 5) {
            throw new IllegalStateException("split.lenght must be 5!!!");
        }
        result.mEmojiType = EmojiType.valueOfInt(Integer.valueOf(split[0]));
        result.mStickerUrl = split[1];
        result.mMagicUrl = split[2];
        result.mSoundUrl = split[3];
        result.mPackageName = split[4];
        if (result.mEmojiType == EmojiType.STICKER_MAGIC && !TextUtils.isEmpty(result.mMagicUrl)) {
            result.mIsDownloaded = Downloader.getInstance().isDownloaded(result.mMagicUrl);
        }

        return result;
    }

    @Override
    public String toString() {
        return mEmojiType.value + "|" + mStickerUrl + "|" + mMagicUrl + "|" + mSoundUrl + "|" + mPackageName;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mStickerUrl);
        dest.writeString(this.mMagicUrl);
        dest.writeString(this.mSoundUrl);
        dest.writeByte(this.mClickable ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mIsDownloaded ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mStickerHeight);
        dest.writeInt(this.mStickerWidth);
        dest.writeParcelable(this.mStartRect, flags);
        dest.writeString(this.mPackageName);
    }

    public StickerInfo() {
    }

    protected StickerInfo(Parcel in) {
        super(in);
        this.mStickerUrl = in.readString();
        this.mMagicUrl = in.readString();
        this.mSoundUrl = in.readString();
        this.mClickable = in.readByte() != 0;
        this.mIsDownloaded = in.readByte() != 0;
        this.mStickerHeight = in.readInt();
        this.mStickerWidth = in.readInt();
        this.mStartRect = in.readParcelable(Rect.class.getClassLoader());
        this.mPackageName = in.readString();
    }

    public static final Creator<StickerInfo> CREATOR = new Creator<StickerInfo>() {
        @Override public StickerInfo createFromParcel(Parcel source) {
            return new StickerInfo(source);
        }

        @Override public StickerInfo[] newArray(int size) {
            return new StickerInfo[size];
        }
    };
}
