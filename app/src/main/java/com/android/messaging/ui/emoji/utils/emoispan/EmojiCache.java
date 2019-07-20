package com.android.messaging.ui.emoji.utils.emoispan;

import android.graphics.drawable.Drawable;
import android.util.LruCache;

import com.android.messaging.ui.emoji.BaseEmojiInfo;
import com.android.messaging.ui.emoji.EmojiInfo;
import com.android.messaging.ui.emoji.EmojiPackageInfo;
import com.android.messaging.ui.emoji.utils.EmojiDataProducer;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.ihs.app.framework.HSApplication;

import java.util.List;

public class EmojiCache {
    private int mCacheSize = 1500;
    private int mCacheVersion;
    private LruCache<String, Drawable> mMemoryCache;

    private static EmojiCache INSTANCE = new EmojiCache();

    public static EmojiCache getInstance() {
        return INSTANCE;
    }

    private EmojiCache() {
        mCacheVersion = HSApplication.getCurrentLaunchInfo().appVersionCode;
        initMemoryCache();
    }

    private void initMemoryCache() {
        mMemoryCache = new LruCache<String, Drawable>(mCacheSize) {
            @Override
            protected int sizeOf(String key, Drawable value) {
                return 1;
            }
        };
        preload();
    }

    private void preload() {
        List<EmojiInfo> recentPage = EmojiDataProducer.loadEmojiRecentData();
        EmojiPackageInfo firstPage = EmojiDataProducer.loadEmojiData(EmojiManager.getEmojiStyle()).get(0);
        for (EmojiInfo info : recentPage) {
            addToCache(info.getDrawable(), info.mResource);
        }
        for (BaseEmojiInfo item : firstPage.mEmojiInfoList) {
            EmojiInfo info = (EmojiInfo) item;
            addToCache(info.getDrawable(), info.mResource);
        }
    }

    public void addToCache(final Drawable drawable, final String resource) {
        if(drawable != null) {
            mMemoryCache.put(resource, drawable);
        }
    }

    public Drawable getFromCache(EmojiInfo emojiInfo) {
        Drawable drawable = mMemoryCache.get(emojiInfo.mResource);
        if (drawable == null) {
            Drawable drawableCreated = emojiInfo.getDrawable();
            if (drawableCreated != null) {
                addToCache(drawableCreated, emojiInfo.mResource);
            }
            return drawableCreated;
        } else {
            return drawable;
        }
    }

    public void flush() {
        initMemoryCache();
    }
}
