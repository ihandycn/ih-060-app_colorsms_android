package com.android.messaging.ui.emoji;

import android.os.Parcel;

import com.android.messaging.ui.emoji.utils.emoji.Emoji;

public class EmojiInfo extends BaseEmojiInfo {

    public String mEmoji;

    public EmojiInfo[] mVariants;

    public final String mUnicode;

    public EmojiInfo(String unicode) {
        this.mUnicode = unicode;
        mEmojiType = EmojiType.EMOJI;
        mVariants = new EmojiInfo[0];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean hasVariant() {
        return mVariants.length != 0;
    }

    public static EmojiInfo convert(Emoji emoji) {
        EmojiInfo info = new EmojiInfo(emoji.getUnicode());
        info.mEmoji = emoji.getUnicode();
        if(emoji.hasVariants()) {
            info.mVariants = new EmojiInfo[emoji.getVariants().size() + 1];
            int i = 0;
            info.mVariants[i++] = info;
            for (Emoji item : emoji.getVariants()) {
                EmojiInfo variant = new EmojiInfo(emoji.getUnicode());
                variant.mEmoji = item.getUnicode();
                info.mVariants[i++] = variant;
            }
            for (int j = 1; j < info.mVariants.length; j++) {
                info.mVariants[j].mVariants = info.mVariants;
            }
        }
        return info;
    }

    public static EmojiInfo unflatten(String flatten) {
        String[] split = flatten.split("\\|");
        EmojiInfo info = new EmojiInfo(split[0]);
        info.mEmoji = split[1];
        info.mVariants = new EmojiInfo[split.length - 2];
        for (int i = 2; i < split.length; i++) {
            EmojiInfo item = new EmojiInfo(split[0]);
            item.mEmoji = split[i];
            info.mVariants[i - 2] = item;
        }
        for (int i = 0; i < info.mVariants.length; i++) {
            info.mVariants[i].mVariants = info.mVariants;
        }
        return info;
    }

    // unicode | emoji_skin | variant_0 | variant_1 | variant_2....
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(mUnicode);
        builder.append("|");
        builder.append(mEmoji);
        for (EmojiInfo item : mVariants) {
            builder.append("|");
            builder.append(item.mEmoji);
        }
        return builder.toString();
    }

    public String getUnicode() {
        return mUnicode;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mEmoji);
        dest.writeString(this.mUnicode);
        dest.writeParcelableArray(this.mVariants, flags);
    }

    private EmojiInfo(Parcel in) {
        super(in);
        this.mEmoji = in.readString();
        this.mUnicode = in.readString();
        this.mVariants = (EmojiInfo[]) in.readParcelableArray(EmojiInfo.class.getClassLoader());
    }

    public static final Creator<EmojiInfo> CREATOR = new Creator<EmojiInfo>() {
        @Override
        public EmojiInfo createFromParcel(Parcel source) {
            return new EmojiInfo(source);
        }

        @Override
        public EmojiInfo[] newArray(int size) {
            return new EmojiInfo[size];
        }
    };
}
