package com.android.messaging.notificationcleaner.resultpage.transition;

import android.os.Handler;

import com.android.messaging.R;
import com.android.messaging.notificationcleaner.resultpage.AdUtils;
import com.android.messaging.notificationcleaner.resultpage.ResultPageActivity;
import com.android.messaging.notificationcleaner.resultpage.data.ResultConstants;
import com.android.messaging.util.BugleAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.util.Threads;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.common.utils.AcbError;

public abstract class BaseTransition {

    private AcbInterstitialAd mInterstitialAd;

    BaseTransition(AcbInterstitialAd ad) {
        mInterstitialAd = ad;
    }

    void popupInterstitialAdIfNeeded() {
        if (mInterstitialAd != null) {
            if (AdUtils.isFacebookAd(mInterstitialAd)) {
                HSGlobalNotificationCenter.sendNotification(ResultPageActivity.EVENT_PREPARE_TO_SHOW_INTERSTITIAL_AD);
                Threads.postOnMainThreadDelayed(this::popupInterstitialAd, 900);
            } else {
                popupInterstitialAd();
            }
        } else {
            onInterstitialAdClosed();
        }
    }

    private void popupInterstitialAd() {
        mInterstitialAd.setCustomTitle(HSApplication.getContext().getString(R.string.optimal));
        mInterstitialAd.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {
            @Override public void onAdDisplayed() {

            }

            @Override
            public void onAdClicked() {
                BugleAnalytics.logEvent("ResultPage_FullAd_Click", true);
            }

            @Override public void onAdClosed() {
                new Handler().postDelayed(() -> onInterstitialAdClosed(), 200);
                ResultPageActivity.sInterstitialAdShowTime = System.currentTimeMillis();
            }

            @Override public void onAdDisplayFailed(AcbError acbError) {

            }
        });
        try {
            mInterstitialAd.show(null);
        } catch (Exception e) {
            // facebook ads may cause "ad already started" exception
            // google ads may cause DeadObjectException or TransactionTooLargeException
            onInterstitialAdClosed();
        }
    }

    protected abstract void onInterstitialAdClosed();
}
