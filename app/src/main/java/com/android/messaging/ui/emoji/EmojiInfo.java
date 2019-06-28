package com.android.messaging.ui.emoji;

import android.os.Parcel;

import com.android.messaging.ui.emoji.utils.emoji.Emoji;

/**
 * EmojiInfo is used in UI
 */
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
        if(!emoji.isSupport()){
            throw new IllegalArgumentException("the emoji unicode is not support in current system");
        }
        String unicode = emoji.getUnicode();
        // append 0xFEOF.  Force to show emoji colorful, not white-black
        if(emoji.needToFix()){
            unicode += new String(Character.toChars(0xFE0f));
        }
        EmojiInfo info = new EmojiInfo(emoji.getUnicode());
        info.mEmoji = unicode;

        if (emoji.hasVariants()) {
            for(Emoji item : emoji.getVariants()) {
                if (!item.isSupport()) {
                    return info;
                }
            }
            info.mVariants = new EmojiInfo[emoji.getVariants().size() + 1];
            int i = 0;
//            info.mVariants[i++] = info;
            // copy item 'info' as the first of variants, avoid skin change to affect variant array;
            EmojiInfo firstVariant = new EmojiInfo(emoji.getUnicode());
            firstVariant.mEmoji = info.mEmoji;
            info.mVariants[i++] = firstVariant;
            for (Emoji item : emoji.getVariants()) {
                EmojiInfo variant = new EmojiInfo(emoji.getUnicode());
                // the variant's unicode don't need to add 0xFE0f. Add the 0xFE0F will cause bad.
                variant.mEmoji = item.getUnicode();
                info.mVariants[i++] = variant;
            }
            for (int j = 0; j < info.mVariants.length; j++) {
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
