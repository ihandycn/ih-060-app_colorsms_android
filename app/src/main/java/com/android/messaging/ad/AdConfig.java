package com.android.messaging.ad;

import android.text.format.DateUtils;

import com.android.messaging.util.CommonUtils;
import com.ihs.commons.config.HSConfig;

import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

/**
 * Created by lizhe on 2019/5/24.
 */

public class AdConfig {

    private static volatile boolean sIsAllAdActivated;

    public static boolean isHomepageBannerAdEnabled() {
        return HSConfig.optBoolean(true, "Application", "SMSAd", "SMSHomepageBannerAd", "Enabled")
                && System.currentTimeMillis() - CommonUtils.getAppInstallTimeMillis()
                > HSConfig.optInteger(30, "Application", "SMSAd", "SMSHomepageBannerAd", "ShowAfterInstall") * DateUtils.MINUTE_IN_MILLIS;
    }

    public static boolean isDetailpageTopAdEnabled(){
        return HSConfig.optBoolean(false, "Application", "SMSAd", "SMSDetailspageTopAd", "Enabled")
                && System.currentTimeMillis() - CommonUtils.getAppInstallTimeMillis()
                > HSConfig.optInteger(30, "Application", "SMSAd", "SMSDetailspageTopAd", "ShowAfterInstall") * DateUtils.MINUTE_IN_MILLIS;
    }

    public static void deactiveAllAds() {
        AcbNativeAdManager.getInstance().deactivePlacementInProcess(AdPlacement.AD_BANNER);
        AcbInterstitialAdManager.getInstance().deactivePlacementInProcess(AdPlacement.AD_WIRE);
        AcbNativeAdManager.getInstance().deactivePlacementInProcess(AdPlacement.AD_DETAIL_NATIVE);
        sIsAllAdActivated = false;
    }

    public static void activeAllAdsReentrantly() {
        if (!sIsAllAdActivated) {
            sIsAllAdActivated = true;
            AcbNativeAdManager.getInstance().activePlacementInProcess(AdPlacement.AD_BANNER);
            AcbInterstitialAdManager.getInstance().activePlacementInProcess(AdPlacement.AD_WIRE);
            AcbNativeAdManager.getInstance().activePlacementInProcess(AdPlacement.AD_DETAIL_NATIVE);
        }
    }
}
