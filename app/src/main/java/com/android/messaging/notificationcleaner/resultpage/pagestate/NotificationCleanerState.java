package com.android.messaging.notificationcleaner.resultpage.pagestate;

import android.content.Intent;
import android.support.v4.content.ContextCompat;

import com.android.messaging.R;
import com.android.messaging.notificationcleaner.resultpage.ResultPageActivity;
import com.android.messaging.notificationcleaner.resultpage.transition.DefaultTransition;
import com.android.messaging.notificationcleaner.resultpage.transition.ITransition;
import com.ihs.app.framework.HSApplication;

import net.appcloudbox.ads.base.AcbInterstitialAd;

public class NotificationCleanerState implements IPageState {

    @Override public int getBackgroundColor() {
        return ContextCompat.getColor(HSApplication.getContext(), R.color.primary_color);
    }

    @Override public String getTitle() {
        return HSApplication.getContext().getString(R.string.notification_cleaner_title);
    }

    @Override public ITransition getTransition(Intent intent, AcbInterstitialAd interstitialAd) {
        return new DefaultTransition(interstitialAd,
                intent.getIntExtra(ResultPageActivity.EXTRA_KEY_CLEAR_NOTIFICATIONS_COUNT, 1));
    }

    @Override public void recordShowTime() {

    }
}
