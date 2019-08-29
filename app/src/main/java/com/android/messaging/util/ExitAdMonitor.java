package com.android.messaging.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.android.messaging.BugleApplication;
import com.facebook.ads.AudienceNetworkActivity;
import com.google.android.gms.ads.AdActivity;
import com.ihs.commons.utils.HSLog;
import com.mopub.mobileads.MoPubActivity;
import com.mopub.mobileads.MraidActivity;
import com.mopub.mobileads.MraidVideoPlayerActivity;

public class ExitAdMonitor {
    private static ExitAdMonitor sInstance = new ExitAdMonitor();

    public static ExitAdMonitor getInstance() {
        return sInstance;
    }

    private Activity mExitAd;
    private boolean mIsEnabled;

    private ExitAdMonitor() {

    }

    public void registerExitAdMonitorForExitAdActivity(final BugleApplication application) {

        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                if (mIsEnabled
                        && (activity instanceof MoPubActivity
                        || activity instanceof AdActivity
                        || activity instanceof AudienceNetworkActivity
                        || activity instanceof MraidActivity
                        || activity instanceof MraidVideoPlayerActivity)) {
                    mExitAd = activity;
                }
                HSLog.d("lifecycle_callback", "onActivityCreated");
            }

            @Override
            public void onActivityStarted(Activity activity) {
                HSLog.d("lifecycle_callback", "onActivityStarted");
            }

            @Override
            public void onActivityResumed(Activity activity) {
                HSLog.d("lifecycle_callback", "onActivityResumed");
            }

            @Override
            public void onActivityPaused(Activity activity) {
                HSLog.d("lifecycle_callback", "onActivityPaused");
            }

            @Override
            public void onActivityStopped(Activity activity) {
                HSLog.d("lifecycle_callback", "onActivityStopped");
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                if (mIsEnabled
                        && (activity instanceof MoPubActivity
                        || activity instanceof AdActivity
                        || activity instanceof AudienceNetworkActivity
                        || activity instanceof MraidActivity
                        || activity instanceof MraidVideoPlayerActivity)) {
                    mExitAd = null;
                    mIsEnabled = false;
                }
                HSLog.d("lifecycle_callback", "onActivityDestroyed");
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }
        });
    }

    public void setEnabled(boolean isEnAbled) {
        mIsEnabled = isEnAbled;
    }

    public void finishExitAdActivity() {
        if (mExitAd != null) {
            mExitAd.finish();
            mExitAd = null;
        }
    }
}
