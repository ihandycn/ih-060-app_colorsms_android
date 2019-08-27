package com.android.messaging.notificationcleaner.resultpage.pagestate;

import android.content.Intent;

import com.android.messaging.notificationcleaner.resultpage.transition.ITransition;

import net.appcloudbox.ads.base.AcbInterstitialAd;

public interface IPageState {

    int getBackgroundColor();

    String getTitle();

    ITransition getTransition(Intent intent, AcbInterstitialAd interstitialAd);

    void recordShowTime();
}
