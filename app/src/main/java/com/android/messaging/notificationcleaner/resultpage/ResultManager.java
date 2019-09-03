package com.android.messaging.notificationcleaner.resultpage;

import com.android.messaging.ad.AdPlacement;
import com.android.messaging.notificationcleaner.LocalInterstitialAdPool;
import com.android.messaging.notificationcleaner.LocalNativeAdPool;

public class ResultManager {

    private static ResultManager instance;

    private ResultManager() {
    }

    public static synchronized ResultManager getInstance() {
        if (null == instance) {
            instance = new ResultManager();
        }
        return instance;
    }

    public void preLoadAds() {
        LocalNativeAdPool.getInstance().preload(AdPlacement.NOTIFICATION_CLEANER_AD_PLACEMENT_RESULT_PAGE_NATIVE);
        LocalInterstitialAdPool.getInstance().preload(AdPlacement.NOTIFICATION_CLEANER_AD_PLACEMENT_RESULT_PAGE_INTERSTITIAL);
    }
}
