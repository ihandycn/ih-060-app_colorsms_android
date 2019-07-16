package com.android.messaging.ui.emoji;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Parcel;

import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.ui.emoji.utils.EmojiStyleDownloadManager;
import com.android.messaging.ui.emoji.utils.emoji.Emoji;
import com.superapps.util.Dimensions;

import java.io.File;

/**
 * EmojiInfo is used in UI
 */
public class EmojiInfo extends BaseEmojiInfo {

    public String mEmoji;

    public EmojiInfo[] mVariants;

    public final String mUnicode;

    public String mEmojiStyle;
    public String mResource;

    private EmojiInfo(String unicode) {
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

    public static EmojiInfo convert(Emoji emoji, String emojiStyle) {
        boolean useSystemStyle = emojiStyle.equals(EmojiManager.EMOJI_STYLE_SYSTEM);
        if (useSystemStyle && !emoji.isSupport()) {
            throw new IllegalArgumentException("the emoji unicode is not support in current system");
        }
        String unicode = emoji.getUnicode();
        // append 0xFEOF.  Force to show emoji colorful, not white-black
//        if(emoji.needToFix()){
//            unicode += new String(Character.toChars(0xFE0f));
//        }
        EmojiInfo info = new EmojiInfo(emoji.getUnicode());
        info.mEmoji = unicode;
        info.mResource = emoji.getResource();
        info.mEmojiStyle = emojiStyle;

        if (emoji.hasVariants()) {
            // if system doesn't support variants of emoji, skip it.
            for (Emoji item : emoji.getVariants()) {
                if (useSystemStyle && !item.isSupport()) {
                    return info;
                }
            }
            info.mVariants = new EmojiInfo[emoji.getVariants().size() + 1];
            int i = 0;
            // copy item 'info' as the first of variants, avoid skin change to affect variant array;
            EmojiInfo firstVariant = new EmojiInfo(emoji.getUnicode());
            firstVariant.mEmoji = info.mEmoji;
            firstVariant.mResource = info.mResource;
            firstVariant.mEmojiStyle = info.mEmojiStyle;

            info.mVariants[i++] = firstVariant;
            for (Emoji item : emoji.getVariants()) {
                EmojiInfo variant = new EmojiInfo(emoji.getUnicode());
                // the variant's unicode don't need to add 0xFE0f. Add the 0xFE0F will cause bad.
                variant.mEmoji = item.getUnicode();
                variant.mResource = item.getResource();
                variant.mEmojiStyle = info.mEmojiStyle;
                info.mVariants[i++] = variant;
            }
            for (int j = 0; j < info.mVariants.length; j++) {
                info.mVariants[j].mVariants = info.mVariants;
            }

        }
        return info;
    }

    public static EmojiInfo unflatten(String flatten, String style) {
        String[] split = flatten.split("\\|");
        EmojiInfo info = new EmojiInfo(split[0].trim());
        String[] curStr = split[1].split(";");
        info.mEmoji = curStr[0].trim();
        info.mResource = curStr[1].trim();
        info.mVariants = new EmojiInfo[split.length - 2];
        info.mEmojiStyle = style;
        for (int i = 2; i < split.length; i++) {
            EmojiInfo item = new EmojiInfo(split[0]);
            String[] varStr = split[i].split(";");
            item.mEmoji = varStr[0];
            item.mResource = varStr[1];
            item.mEmojiStyle = style;
            info.mVariants[i - 2] = item;
        }
        for (int i = 0; i < info.mVariants.length; i++) {
            info.mVariants[i].mVariants = info.mVariants;
        }
        return info;
    }

    // unicode | emoji_skin ; resource | variant_0 ; resource_0 | variant_1 : resource_0 | variant_2 ; resource_0....
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(mUnicode);
        builder.append("|");
        builder.append(mEmoji + ";" + mResource);
        for (EmojiInfo item : mVariants) {
            builder.append("|");
            builder.append(item.mEmoji + ";" + mResource);
        }
        return builder.toString();
    }

    public String getUnicode() {
        return mUnicode;
    }

    public Drawable getDrawable() {
        boolean useSystem = mEmojiStyle.equals(EmojiManager.EMOJI_STYLE_SYSTEM);
        if (useSystem) {
            return new EmojiDrawable(mEmoji);
        } else {
            return getDrawableFromFile();
        }
    }

    private Drawable getDrawableFromFile() {
        File dir = new File(EmojiStyleDownloadManager.getBaseDir(), mEmojiStyle);
        if(!dir.exists()){
            throw new RuntimeException("getDrawableFromFile: the resource file of emoji-style not exists");
        }
        File file = new File(dir.getAbsolutePath(), mResource + ".png");
        return Drawable.createFromPath(file.getAbsolutePath());
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

    private static class EmojiDrawable extends Drawable {

        private Paint mPaint;
        private String mUnicode;

        public EmojiDrawable(String unicode) {
            mPaint = new Paint();
            mUnicode = unicode;
        }

        @Override
        public void draw(Canvas canvas) {
            mPaint.setTextAlign(Paint.Align.LEFT);
            mPaint.setTextSize(Dimensions.pxFromDp(25));
//            Rect bounds = new Rect();
//            mPaint.getTextBounds(mUnicode, 0, mUnicode.length(), bounds);
            canvas.drawText(mUnicode, Dimensions.pxFromDp(1f), Dimensions.pxFromDp(25), mPaint);
        }

        @Override
        public void setAlpha(int alpha) {
            mPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            mPaint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}
