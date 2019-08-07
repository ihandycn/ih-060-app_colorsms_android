package com.android.messaging.ui.emoji.utils;

import com.android.messaging.ui.emoji.GiphyInfo;
import com.giphy.sdk.core.models.Image;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.core.models.enums.MediaType;
import com.giphy.sdk.core.network.api.CompletionHandler;
import com.giphy.sdk.core.network.api.GPHApi;
import com.giphy.sdk.core.network.api.GPHApiClient;
import com.giphy.sdk.core.network.response.ListMediaResponse;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.android.messaging.ui.emoji.utils.EmojiDataProducer.GIPHY_CATEGORY_TREND;

public class GiphyListManager {
    public static final int BUCKET_COUNT = 10;

    private final HashMap<String, List<GiphyInfo>> mGiphyListCache;
    private GPHApi mClient = new GPHApiClient("6eDzBQcmlIqYEtuulH1o3TvQja0oLnBs");

    public interface GiphyListFetchCallBack {
        void onGiphyListFetched(List<GiphyInfo> giphyList);
    }

    private static final GiphyListManager sInstance = new GiphyListManager();

    public static GiphyListManager getInstance() {
        return sInstance;
    }

    private GiphyListManager() {
        mGiphyListCache = new HashMap<>(15);
    }

    public void getGiphyList(String category, int offset, GiphyListFetchCallBack callBack) {
        List<GiphyInfo> list = mGiphyListCache.get(category);
        if (list == null) {
            list = new ArrayList<>();
            mGiphyListCache.put(category, list);
            search(category, offset, list, callBack);
        } else {
            if (offset >= list.size()) {
                search(category, offset, list, callBack);
            } else {
                callBack.onGiphyListFetched(list);
            }
        }

    }

    public void getTrendingGiphyList(int offset, GiphyListFetchCallBack callBack) {
        List<GiphyInfo> list = mGiphyListCache.get(GIPHY_CATEGORY_TREND);
        if (list == null) {
            list = new ArrayList<>();
            mGiphyListCache.put(GIPHY_CATEGORY_TREND, list);
            trend(offset, list, callBack);
        } else {
            if (offset >= list.size()) {
                trend(offset, list, callBack);
            } else {
                callBack.onGiphyListFetched(list);
            }
        }
    }

    private void trend(int offset, List<GiphyInfo> list, GiphyListFetchCallBack callBack) {
        mClient.trending(MediaType.gif, BUCKET_COUNT, offset, null, new CompletionHandler<ListMediaResponse>() {
            @Override
            public void onComplete(ListMediaResponse result, Throwable e) {
                onLoadComplete(result, list, callBack);
            }
        });
    }

    private void search(String category, int offset, List<GiphyInfo> list, GiphyListFetchCallBack callBack) {
        mClient.search(category, MediaType.gif, BUCKET_COUNT, offset, null, null, null, new CompletionHandler<ListMediaResponse>() {
            @Override
            public void onComplete(ListMediaResponse result, Throwable e) {
                onLoadComplete(result, list, callBack);
            }
        });
    }

    private void onLoadComplete(ListMediaResponse result,
                                List<GiphyInfo> list,
                                GiphyListFetchCallBack callBack) {
        if (result == null) {
            // Do what you want to do with the error
        } else {
            if (result.getData() != null) {
                for (Media media : result.getData()) {
                    GiphyInfo giphyInfo = new GiphyInfo();
                    Image image = media.getImages().getFixedWidthDownsampled();
                    giphyInfo.mFixedWidthGifUrl = image.getGifUrl();
                    giphyInfo.mGifOriginalWidth = image.getWidth();
                    giphyInfo.mGifOriginalHeight = image.getHeight();
                    list.add(giphyInfo);
                }

                if (callBack != null) {
                    callBack.onGiphyListFetched(list);
                }
            } else {
                HSLog.e("giphy error", "No results found");
            }
        }
    }
}



