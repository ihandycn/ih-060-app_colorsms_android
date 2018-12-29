package com.android.messaging.glide;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;
import com.bumptech.glide.util.ByteBufferUtil;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * Transcode glide {@link GifDrawable} ot android-gif-drawable {@link pl.droidsonroids.gif.GifDrawable}.
 */
@SuppressWarnings("WeakerAccess")
public class EmojiDrawableTranscoder implements ResourceTranscoder<GifDrawable, pl.droidsonroids.gif.GifDrawable> {

    @Nullable
    @Override
    public Resource<pl.droidsonroids.gif.GifDrawable> transcode(@NonNull Resource<GifDrawable> toTranscode, @NonNull Options options) {
        pl.droidsonroids.gif.GifDrawable emojiDrawable = null;
        try {
            GifDrawable gifData = toTranscode.get();
            ByteBuffer byteBuffer = gifData.getBuffer();
            emojiDrawable = new pl.droidsonroids.gif.GifDrawable(ByteBufferUtil.toBytes(byteBuffer));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new EmojiDrawableResource(emojiDrawable, toTranscode.getSize());
    }

    @SuppressWarnings("WeakerAccess")
    private static class EmojiDrawableResource implements Resource<pl.droidsonroids.gif.GifDrawable> {
        private final pl.droidsonroids.gif.GifDrawable mDrawable;
        private final int mSize;

        EmojiDrawableResource(pl.droidsonroids.gif.GifDrawable drawable, int size) {
            mDrawable = drawable;
            mSize = size;
        }

        @Override public Class<pl.droidsonroids.gif.GifDrawable> getResourceClass() {
            return pl.droidsonroids.gif.GifDrawable.class;
        }

        @Override
        public pl.droidsonroids.gif.GifDrawable get() {
            return mDrawable;
        }

        @Override
        public int getSize() {
            return mSize;
        }

        @Override
        public void recycle() {
            if (mDrawable != null) {
                mDrawable.recycle();
            }
        }
    }
}
