package com.android.messaging.ui.emoji;

import android.os.Parcel;
import android.os.Parcelable;

public class GiphyInfo extends BaseEmojiInfo {

    private int mGifOriginalWidth;
    private int mGifOriginalHeight;
    private int mFixedWidthGifUrl;

    protected GiphyInfo(Parcel in) {
        mGifOriginalWidth = in.readInt();
        mGifOriginalHeight = in.readInt();
        mFixedWidthGifUrl = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mGifOriginalWidth);
        dest.writeInt(mGifOriginalHeight);
        dest.writeInt(mFixedWidthGifUrl);
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
}
