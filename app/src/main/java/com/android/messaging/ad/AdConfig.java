package com.android.messaging.ad;

import android.text.format.DateUtils;

import com.android.messaging.Factory;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.CommonUtils;
import com.android.messaging.util.ExitAdAutopilotUtils;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Calendars;

import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

import static com.android.messaging.ui.conversationlist.ConversationListActivity.PREF_KEY_EXIT_WIRE_AD_SHOW_COUNT_IN_ONE_DAY;
import static com.android.messaging.ui.conversationlist.ConversationListActivity.PREF_KEY_EXIT_WIRE_AD_SHOW_TIME;

/**
 * Created by lizhe on 2019/5/24.
 */

public class AdConfig {

    private static volatile boolean sIsAllAdActivated;
    private static final BuglePrefs mPrefs = Factory.get().getApplicationPrefs();
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

    public static boolean isExitAdEnabled(){
        return !BillingManager.isPremiumUser()
                && HSConfig.optBoolean(true, "Application", "SMSAd", "SMSExitAd", "Enabled")
                && ExitAdAutopilotUtils.getIsExitAdSwitchOn()
                && System.currentTimeMillis() - CommonUtils.getAppInstallTimeMillis()
                > HSConfig.optInteger(2, "Application", "SMSAd", "SMSExitAd", "ShowAfterInstall") * DateUtils.HOUR_IN_MILLIS
                && !(Calendars.isSameDay(System.currentTimeMillis(), mPrefs.getLong(PREF_KEY_EXIT_WIRE_AD_SHOW_TIME, -1))
                && mPrefs.getInt(PREF_KEY_EXIT_WIRE_AD_SHOW_COUNT_IN_ONE_DAY, 0) == ExitAdAutopilotUtils.getExitAdShowMaxTimes());
    }

    public static void deactiveAllAds() {
        AcbNativeAdManager.getInstance().deactivePlacementInProcess(AdPlacement.AD_BANNER);
        AcbInterstitialAdManager.getInstance().deactivePlacementInProcess(AdPlacement.AD_WIRE);
        AcbInterstitialAdManager.getInstance().deactivePlacementInProcess(AdPlacement.AD_EXIT_WIRE);
        AcbNativeAdManager.getInstance().deactivePlacementInProcess(AdPlacement.AD_DETAIL_NATIVE);
        sIsAllAdActivated = false;
    }

    public static void activeAllAdsReentrantly() {
        if (!sIsAllAdActivated) {
            sIsAllAdActivated = true;
            AcbNativeAdManager.getInstance().activePlacementInProcess(AdPlacement.AD_BANNER);
            AcbInterstitialAdManager.getInstance().activePlacementInProcess(AdPlacement.AD_WIRE);
            AcbInterstitialAdManager.getInstance().activePlacementInProcess(AdPlacement.AD_EXIT_WIRE);
            AcbNativeAdManager.getInstance().activePlacementInProcess(AdPlacement.AD_DETAIL_NATIVE);
        }
    }
}
