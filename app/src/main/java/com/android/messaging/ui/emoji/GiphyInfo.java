package com.android.messaging.ui.emoji;

import android.graphics.Rect;
import android.os.Parcel;
import android.text.TextUtils;

import com.android.messaging.download.Downloader;

public class GiphyInfo extends BaseEmojiInfo {

    public int mGifOriginalWidth;
    public int mGifOriginalHeight;
    public String mFixedWidthGifUrl;

    public int mGifWidth;
    public int mGifHeight;
    public Rect mStartRect;

    public GiphyInfo() {
    }

    protected GiphyInfo(Parcel in) {
        mGifOriginalWidth = in.readInt();
        mGifOriginalHeight = in.readInt();
        mFixedWidthGifUrl = in.readString();
        mGifWidth = in.readInt();
        mGifHeight = in.readInt();
        mStartRect = in.readParcelable(Rect.class.getClassLoader());
    }

    @Override
    public String toString() {
        return mGifOriginalWidth + "|" + mGifOriginalHeight + "|" + mFixedWidthGifUrl ;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof GiphyInfo) {
            return TextUtils.equals(((GiphyInfo) obj).mFixedWidthGifUrl, mFixedWidthGifUrl);
        }

        return false;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mGifOriginalWidth);
        dest.writeInt(mGifOriginalHeight);
        dest.writeString(mFixedWidthGifUrl);
        dest.writeInt(mGifWidth);
        dest.writeInt(mGifHeight);
        dest.writeParcelable(mStartRect, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GiphyInfo> CREATOR = new Creator<GiphyInfo>() {
        @Override
        public GiphyInfo createFromParcel(Parcel in) {
            return new GiphyInfo(in);
        }

        @Override
        public GiphyInfo[] newArray(int size) {
            return new GiphyInfo[size];
        }
    };

    public static GiphyInfo unflatten(String flatten) {
        GiphyInfo result = new GiphyInfo();
        String[] split = flatten.split("\\|");
        if (split.length != 3) {
            throw new IllegalStateException("split.length must be 3!!!");
        }
        result.mGifOriginalWidth = Integer.parseInt(split[0]);
        result.mGifOriginalHeight = Integer.parseInt(split[1]);
        result.mFixedWidthGifUrl = split[2];

        return result;
    }

}
