package com.android.messaging.notificationcleaner;

import android.support.annotation.Nullable;

import com.android.messaging.util.BugleAnalytics;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdLoader;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdLoader.AcbInterstitialAdLoadListener;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalInterstitialAdPool {

    private static final String TAG = LocalInterstitialAdPool.class.getSimpleName();

    private Map<String, AcbInterstitialAd> adMap = new HashMap<>();

    private static class LocalInterstitialAdPoolHolder {
        private static final LocalInterstitialAdPool instance = new LocalInterstitialAdPool();
    }

    public static LocalInterstitialAdPool getInstance() {
        return LocalInterstitialAdPoolHolder.instance;
    }

    public @Nullable
    AcbInterstitialAd fetch(String placementName) {
        AcbInterstitialAd interstitialAd = adMap.remove(placementName);
//        if (interstitialAd != null
//                && interstitialAd.isExpired()
//                && (!HSConfig.optBoolean(true, "Application", "AdsManager", "Placements", placementName, "ExpireEnabled"))) {
//            interstitialAd.release();
//            interstitialAd = null;
//        }
        if (interstitialAd != null) {
            BugleAnalytics.logEvent("Security_Ad_Expire_Rate", placementName, String.valueOf(interstitialAd.isExpired()));
        }
        return interstitialAd;
    }

    public void preload(String placementName) {
        HSLog.d(TAG, "preload ad, name is " + placementName);
        AcbInterstitialAdManager.getInstance().activePlacementInProcess(placementName);
        AcbInterstitialAd interstitialAd = adMap.get(placementName);
        if (interstitialAd != null && !interstitialAd.isExpired()) {
            HSLog.d(TAG, "ad already exists, just return. " + placementName);
            return;
        }

        AcbInterstitialAdLoader loader = AcbInterstitialAdManager.createLoaderWithPlacement(placementName);
        loader.load(1, new AcbInterstitialAdLoadListener() {
            @Override
            public void onAdReceived(AcbInterstitialAdLoader acbInterstitialAdLoader, List<AcbInterstitialAd> list) {
                if (list != null && !list.isEmpty()) {
                    adMap.put(placementName, list.get(0));
                }
            }

            @Override
            public void onAdFinished(AcbInterstitialAdLoader acbInterstitialAdLoader, AcbError acbError) {
                if (acbError != null) {
                    HSLog.d(TAG, "interstitial ad load error = " + acbError + " placement name = " + placementName);
                }
                HSLog.d(TAG, "interstitial ad load finish, placement name = " + placementName);
            }
        });
    }
}
