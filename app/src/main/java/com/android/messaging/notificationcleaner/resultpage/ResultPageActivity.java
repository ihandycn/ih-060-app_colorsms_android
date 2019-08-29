package com.android.messaging.notificationcleaner.resultpage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ad.AdPlacement;
import com.android.messaging.notificationcleaner.LocalInterstitialAdPool;
import com.android.messaging.notificationcleaner.LocalNativeAdPool;
import com.android.messaging.notificationcleaner.NotificationCleanerTest;
import com.android.messaging.notificationcleaner.NotificationCleanerUtil;
import com.android.messaging.notificationcleaner.activity.NotificationBlockedActivity;
import com.android.messaging.notificationcleaner.resultpage.content.AdContent;
import com.android.messaging.notificationcleaner.resultpage.content.IContent;
import com.android.messaging.notificationcleaner.resultpage.content.OptimizedContent;
import com.android.messaging.notificationcleaner.resultpage.pagestate.IPageState;
import com.android.messaging.notificationcleaner.resultpage.pagestate.NotificationCleanerState;
import com.android.messaging.notificationcleaner.resultpage.transition.ITransition;
import com.android.messaging.notificationcleaner.resultpage.views.AdLoadingView;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.Typefaces;
import com.android.messaging.util.UiUtils;
import com.android.messaging.util.ViewUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.Navigations;
import com.superapps.util.Threads;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.base.AcbNativeAd;

import hugo.weaving.DebugLog;

public class ResultPageActivity extends HSAppCompatActivity implements INotificationObserver {

    public static final String TAG = "ResultPageActivity";
    public static final String EVENT_PREPARE_TO_SHOW_INTERSTITIAL_AD = "event_prepare_to_show_interstitial_ad";
    public static long sInterstitialAdShowTime = 0;
    public static long sResultPageContentShowTime = 0;

    public static final String EXTRA_KEY_CLEAR_NOTIFICATIONS_COUNT = "EXTRA_KEY_CLEAR_NOTIFICATIONS_COUNT";

    private AcbNativeAd mAd;
    private AcbInterstitialAd mInterstitialAd;
    private IPageState mPageState;
    private boolean mIsResultPageShow;
    private MenuItem mExitBtn;

    private ResultContentType mContentType;
    private IContent mContent;

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_page);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.setStatusBarColor(this, getResources().getColor(R.color.primary_color));
        toolbar.setBackgroundColor(getResources().getColor(R.color.primary_color));
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setTypeface(Typefaces.getCustomSemiBold());
        title.setText(getString(R.string.notification_cleaner_title));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        if (null != intent) {
            mPageState = new NotificationCleanerState();
        } else {
            finish();
        }

        findViewById(R.id.bg_view).setBackgroundColor(mPageState.getBackgroundColor());
        NotificationCleanerUtil.insertFakeNotificationsIfNeeded();
        BugleAnalytics.logEvent("ResultPage_Show", true);
        BugleAnalytics.logEvent("ResultPage_FullAd_Chance", true);
        BugleAnalytics.logEvent("ResultPage_DoneAd_Chance", true);
        NotificationCleanerTest.logResultpageShow();

        HSGlobalNotificationCenter.addObserver(EVENT_PREPARE_TO_SHOW_INTERSTITIAL_AD, this);
    }

    @DebugLog
    @SuppressLint("NewApi")
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mIsResultPageShow) {
            return;
        }

        mPageState.recordShowTime();

        mInterstitialAd = LocalInterstitialAdPool.getInstance().fetch(AdPlacement.NOTIFICATION_CLEANER_AD_PLACEMENT_RESULT_PAGE_INTERSTITIAL);
        if (mInterstitialAd != null) {
            BugleAnalytics.logEvent("ResultPage_FullAd_Show", true);
            NotificationCleanerTest.logResultpageFulladShow();
        }

        mAd = LocalNativeAdPool.getInstance().fetch(AdPlacement.NOTIFICATION_CLEANER_AD_PLACEMENT_RESULT_PAGE_NATIVE);
        if (mAd != null) {
            mContentType = ResultContentType.AD;
        } else {
            mContentType = ResultContentType.OPTIMAL;
        }

        if (mInterstitialAd == null && mAd == null) {
            mContentType = ResultContentType.OPTIMAL;
        }

      //  ResultManager.getInstance().preLoadAds();

        mContent = getContent(mContentType);
        ITransition transitionState = mPageState.getTransition(getIntent(), mInterstitialAd);
        transitionState.setContent(mContent);

        ViewGroup transitionContainer = ViewUtils.findViewById(this, R.id.transition_view_container);
        transitionContainer.removeAllViews();
        getLayoutInflater().inflate(transitionState.getLayoutId(), transitionContainer, true);
        transitionState.onFinishInflateTransitionView(transitionContainer);

        mIsResultPageShow = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.result_page, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mExitBtn = menu.findItem(R.id.action_bar_exit);
        if (mExitBtn != null) {
            mExitBtn.setVisible(false);
            mExitBtn.setOnMenuItemClickListener(menuItem -> {
                //onBackFromAdContent();
                backToNCPageIfNeeded();
                return false;
            });
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public void onAdContentShown() {
        if (mExitBtn != null) {
            mExitBtn.setVisible(true);
        }
    }

    private void onBackFromAdContent() {
        Intent intent = new Intent(this, NotificationBlockedActivity.class);
        intent.putExtra(NotificationBlockedActivity.START_FROM,
                NotificationBlockedActivity.START_FROM_RESULT_AD_PAGE);
        Navigations.startActivitySafely(this, intent);
        finish();
//        mContentType = OPTIMAL;
//        mContent = getContent(mContentType);
//        mContent.initView(this);
//
//        Threads.postOnMainThreadDelayed(() -> {
//            ViewGroup container = ViewUtils.findViewById(this, R.id.ad_view_container);
//            container.animate()
//                    .alpha(0f)
//                    .scaleX(0.7f)
//                    .scaleY(0.7f)
//                    .setDuration(200)
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            if (mExitBtn != null) {
//                                mExitBtn.setVisible(false);
//                            }
//                            mContent.startAnimation();
//                        }
//                    })
//                    .start();
//        }, 500);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backToNCPageIfNeeded();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//        if (mContentType == ResultContentType.AD) {
//            onBackFromAdContent();
//            return;
//        }
//
//        finish();
        backToNCPageIfNeeded();
    }

    private void backToNCPageIfNeeded() {
        if (NotificationCleanerUtil.getNotificationBlockedActivityIllustratePageShowingState() < NotificationCleanerUtil.NOTIFICATION_STATE_SHOWED) {
            NotificationCleanerUtil.setNotificationBlockedActivityIllustratePageShowingState(NotificationCleanerUtil.NOTIFICATION_STATE_SHOWING);
        }
        Intent intent = new Intent(this, NotificationBlockedActivity.class);
        intent.putExtra(NotificationBlockedActivity.START_FROM, NotificationBlockedActivity.START_FROM_RESULT_AD_PAGE);
        Navigations.startActivitySafely(this, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsResultPageShow = false;

        if (mAd != null) {
            mAd.release();
            mAd = null;
        }

        if (mInterstitialAd != null) {
            mInterstitialAd.setInterstitialAdListener(null);
            mInterstitialAd.release();
        }

        if (mContent != null) {
            mContent.onActivityDestroy();
        }

        HSGlobalNotificationCenter.removeObserver(this);
    }

    private IContent getContent(ResultContentType type) {
        IContent contentState = null;
        switch (type) {
            case AD:
                contentState = new AdContent(mAd);
                break;
            case OPTIMAL:
                contentState = new OptimizedContent();
                break;
        }
        return contentState;
    }

    public int getBackgroundColor() {
        return mPageState.getBackgroundColor();
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case EVENT_PREPARE_TO_SHOW_INTERSTITIAL_AD:
                AdLoadingView adLoadingView = new AdLoadingView(this);
                FrameLayout adLoadingContainer = findViewById(R.id.ad_loading_view_container);
                adLoadingContainer.addView(adLoadingView, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                Threads.postOnMainThreadDelayed(adLoadingContainer::removeAllViews, 1200);
                break;
        }
    }
}
