package com.android.messaging.notificationcleaner;

import com.android.messaging.util.BugleAnalytics;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.nativead.AcbNativeAdLoader;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalNativeAdPool {

    private static final String TAG = LocalNativeAdPool.class.getSimpleName();

    private Map<String, AcbNativeAd> mAdMap = new HashMap<>();

    private static class LocalNativeAdPoolHolder {
        private static final LocalNativeAdPool instance = new LocalNativeAdPool();
    }

    public static LocalNativeAdPool getInstance() {
        return LocalNativeAdPoolHolder.instance;
    }

    public AcbNativeAd fetch(String placementName) {
        AcbNativeAd nativeAd = mAdMap.remove(placementName);
//        if (nativeAd != null
//                && nativeAd.isExpired()
//                && (!HSConfig.optBoolean(true, "Application", "AdsManager", "Placements", placementName, "ExpireEnabled"))) {
//            nativeAd.release();
//            nativeAd = null;
//        }
        if (nativeAd != null) {
            BugleAnalytics.logEvent("Security_Ad_Expire_Rate", placementName, String.valueOf(nativeAd.isExpired()));
        }
        return nativeAd;
    }

    public void preload(String placementName) {
        HSLog.d(TAG, "preload ad, name is " + placementName);

        AcbNativeAdManager.getInstance().activePlacementInProcess(placementName);
        AcbNativeAd nativeAd = mAdMap.get(placementName);
        if (nativeAd != null && !nativeAd.isExpired()) {
            HSLog.d(TAG, "ad already exists, just return. " + placementName);
            return;
        }

        AcbNativeAdLoader loader = AcbNativeAdManager.createLoaderWithPlacement(placementName);
        loader.load(1, new AcbNativeAdLoader.AcbNativeAdLoadListener() {
            @Override
            public void onAdReceived(AcbNativeAdLoader acbNativeAdLoader, List<AcbNativeAd> list) {
                if (list != null && !list.isEmpty()) {
                    mAdMap.put(placementName, list.get(0));
                }
            }

            @Override
            public void onAdFinished(AcbNativeAdLoader acbNativeAdLoader, AcbError acbError) {
                if (acbError != null) {
                    HSLog.d(TAG, "native ad load error = " + acbError + " placement name = " + placementName);
                }
                HSLog.d(TAG, "native ad load finish, placement name = " + placementName);
            }
        });
    }
}
