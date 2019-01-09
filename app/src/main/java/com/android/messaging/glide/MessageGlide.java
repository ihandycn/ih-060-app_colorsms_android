package com.android.messaging.glide;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

import java.io.InputStream;

import me.jessyan.progressmanager.ProgressManager;
import okhttp3.OkHttpClient;

/**
 * Global configurations for Glide.
 */

@GlideModule
@SuppressWarnings("unused")
public class MessageGlide extends AppGlideModule {

    private static final String GLIDE_CACHE_DIR = "glide";
    private static final int DISK_CACHE_SIZE = 256 * 1024 * 1024; // 256 MB

    private static final int GLIDE_MEMORY_CACHE_SIZE_MAIN =
            (int) Math.max(Runtime.getRuntime().maxMemory() / 16, 10 * 1024 * 1024);

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {

        // Configure disk & memory caches
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context,
                GLIDE_CACHE_DIR, DISK_CACHE_SIZE));

        builder.setMemoryCache(new LruResourceCache(GLIDE_MEMORY_CACHE_SIZE_MAIN));

        // Configure default Bitmap config
        builder.setDefaultRequestOptions(RequestOptions.formatOf(DecodeFormat.PREFER_ARGB_8888));
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.register(GifDrawable.class, pl.droidsonroids.gif.GifDrawable.class, new EmojiDrawableTranscoder());

        OkHttpClient client = ProgressManager.getInstance().with(new OkHttpClient.Builder()).build();
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
