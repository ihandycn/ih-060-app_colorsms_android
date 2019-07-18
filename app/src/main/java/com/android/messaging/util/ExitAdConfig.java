package com.android.messaging.util;

import android.text.format.DateUtils;

import com.android.messaging.Factory;
import com.android.messaging.ad.AdPlacement;
import com.android.messaging.ad.BillingManager;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Calendars;

import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;

import static com.android.messaging.ui.conversationlist.ConversationListActivity.PREF_KEY_EXIT_WIRE_AD_SHOW_COUNT_IN_ONE_DAY;
import static com.android.messaging.ui.conversationlist.ConversationListActivity.PREF_KEY_EXIT_WIRE_AD_SHOW_TIME;

public class ExitAdConfig {

    private static final BuglePrefs mPrefs = Factory.get().getApplicationPrefs();
    public static void preLoadExitAd() {

        if (!BillingManager.isPremiumUser()
                && HSConfig.optBoolean(true, "Application", "SMSAd", "SMSExitAd", "Enabled")
                && ExitAdAutopilotUtils.getIsExitAdSwitchOn()
                && System.currentTimeMillis() - CommonUtils.getAppInstallTimeMillis()
                > HSConfig.optInteger(2, "Application", "SMSAd", "SMSExitAd", "ShowAfterInstall") * DateUtils.HOUR_IN_MILLIS
                && !(Calendars.isSameDay(System.currentTimeMillis(), mPrefs.getLong(PREF_KEY_EXIT_WIRE_AD_SHOW_TIME, -1))
                && mPrefs.getInt(PREF_KEY_EXIT_WIRE_AD_SHOW_COUNT_IN_ONE_DAY, 0) == ExitAdAutopilotUtils.getExitAdShowMaxTimes())) {
            AcbInterstitialAdManager.preload(1, AdPlacement.AD_EXIT_WIRE);
        }
    }
}
