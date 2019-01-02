package com.android.messaging.ui.emoji;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class EmojiPackageInfo implements Parcelable {

    public EmojiPackageType mEmojiPackageType;

    public String mTabIconUrl;
    public String mName;
    public String mBannerUrl;
    public List<BaseEmojiInfo> mEmojiInfoList;

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mEmojiPackageType == null ? -1 : this.mEmojiPackageType.ordinal());
        dest.writeString(this.mTabIconUrl);
        dest.writeString(this.mName);
        dest.writeString(this.mBannerUrl);
        dest.writeList(this.mEmojiInfoList);
    }

    public EmojiPackageInfo() {
    }

    private EmojiPackageInfo(Parcel in) {
        int tmpMEmojiPackageType = in.readInt();
        this.mEmojiPackageType = tmpMEmojiPackageType == -1 ? null : EmojiPackageType.values()[tmpMEmojiPackageType];
        this.mTabIconUrl = in.readString();
        this.mName = in.readString();
        this.mBannerUrl = in.readString();
        this.mEmojiInfoList = new ArrayList<>();
        in.readList(this.mEmojiInfoList, BaseEmojiInfo.class.getClassLoader());
    }

    public static final Parcelable.Creator<EmojiPackageInfo> CREATOR = new Parcelable.Creator<EmojiPackageInfo>() {
        @Override public EmojiPackageInfo createFromParcel(Parcel source) {
            return new EmojiPackageInfo(source);
        }

        @Override public EmojiPackageInfo[] newArray(int size) {
            return new EmojiPackageInfo[size];
        }
    };
}
