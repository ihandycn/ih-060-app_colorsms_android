package com.android.messaging.ui.emoji.utils;

import com.android.messaging.ui.emoji.EmojiPackageInfo;
import com.superapps.util.Threads;

import java.util.List;

public class LoadEmojiManager{
    public static final String LOAD_EMOJI_DATA = "load_emoji_data";
    public static final String DATA_EMOJI = "data_emoji";
    public static final String DATA_STICKER = "data_sticker";

    private static LoadEmojiManager loadEmojiManager = new LoadEmojiManager();
    private boolean mIsDataPrepared = false;
    private List<EmojiPackageInfo> mEmojiData;
    private List<EmojiPackageInfo> mStickerData;

    private LoadEmojiManager(){}

    public static LoadEmojiManager getInstance(){
        return loadEmojiManager;
    }

    public void getEmojiData(EmojiDataCallback callback){
        if(mIsDataPrepared){
            callback.onDataPrepared(mEmojiData, mStickerData);
            return ;
        }
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                mEmojiData = EmojiDataProducer.loadEmojiData();
                mStickerData = EmojiDataProducer.loadStickerData();
                Threads.postOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDataPrepared(mEmojiData, mStickerData);
                        mIsDataPrepared = true;
                    }
                });
            }
        });
    }


    public interface EmojiDataCallback {
        void onDataPrepared(List<EmojiPackageInfo> emojiData, List<EmojiPackageInfo> stickerData);
    }

}
