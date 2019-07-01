package com.android.messaging.ui.emoji.utils;

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

public class GiphyListManager {
    public static final int BUCKET_COUNT = 10;

    private final HashMap<String, List<Media>> mGiphyListCache;
    private GPHApi mClient = new GPHApiClient("6eDzBQcmlIqYEtuulH1o3TvQja0oLnBs");

    public interface GiphyListFetchCallBack {
        void onGiphyListFetched(List<Media> giphyList);
    }

    private static final GiphyListManager sInstance = new GiphyListManager();

    public static GiphyListManager getInstance() {
        return sInstance;
    }

    private GiphyListManager() {
        mGiphyListCache = new HashMap<>(15);
    }

    public void getGiphyList(String category, int offset, GiphyListFetchCallBack callBack) {
        List<Media> list = mGiphyListCache.get(category);
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

    private void search(String category, int offset, List<Media> list, GiphyListFetchCallBack callBack) {
        mClient.search(category, MediaType.gif, BUCKET_COUNT, offset, null, null, null, new CompletionHandler<ListMediaResponse>() {
            @Override
            public void onComplete(ListMediaResponse result, Throwable e) {
                if (result == null) {
                    // Do what you want to do with the error
                } else {
                    if (result.getData() != null) {
                        list.addAll(result.getData());
                        if (callBack != null) {
                            callBack.onGiphyListFetched(list);
                        }
                        HSLog.d("giphy result", result.getData().toString());
                    } else {
                        HSLog.e("giphy error", "No results found");
                    }
                }
            }
        });
    }

}



