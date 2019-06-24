package com.android.messaging.ui.emoji;

import android.graphics.Rect;
import android.os.Parcel;

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
        return result;
    }

}
