package com.android.messaging.util;

import android.text.format.DateUtils;

import com.android.messaging.Factory;
import com.android.messaging.ad.AdPlacement;
import com.android.messaging.ad.BillingManager;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;

import static com.android.messaging.ui.conversation.ConversationActivity.PREF_KEY_WIRE_AD_SHOW_TIME_FOR_EXIT_WIRE_AD;
import static com.android.messaging.ui.conversationlist.ConversationListActivity.PREF_KEY_EXIT_WIRE_AD_SHOW_TIME;

public class ExitAdConfig {
    private static final BuglePrefs mPrefs = Factory.get().getApplicationPrefs();

    public static void preLoadExitAd() {

        if (!BillingManager.isPremiumUser()
                && HSConfig.optBoolean(true, "Application", "SMSAd", "SMSExitAd", "Enabled")
                && ExitAdAutopilotUtils.getIsExitAdSwitchOn()
                && System.currentTimeMillis() - CommonUtils.getAppInstallTimeMillis()
                > HSConfig.optInteger(2, "Application", "SMSAd", "SMSExitAd", "ShowAfterInstall") * DateUtils.HOUR_IN_MILLIS
                && System.currentTimeMillis() - mPrefs.getLong(PREF_KEY_EXIT_WIRE_AD_SHOW_TIME, -1)
                >= HSConfig.optInteger(5, "Application", "SMSAd", "SMSExitAd", "MinInterval") * DateUtils.MINUTE_IN_MILLIS
                && System.currentTimeMillis() - mPrefs.getLong(PREF_KEY_WIRE_AD_SHOW_TIME_FOR_EXIT_WIRE_AD, -1)
                >= 20 * DateUtils.SECOND_IN_MILLIS) {
            HSLog.d("AdTest", "AcbInterstitialAdManager.preload(1, AdPlacement.AD_EXIT_WIRE);");
            AcbInterstitialAdManager.preload(1, AdPlacement.AD_EXIT_WIRE);
        }
    }
}
