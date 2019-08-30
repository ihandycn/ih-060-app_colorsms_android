package com.android.messaging.notificationcleaner.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.BugleFiles;
import com.android.messaging.R;
import com.android.messaging.ad.AdPlacement;
import com.android.messaging.ad.BillingManager;
import com.android.messaging.notificationcleaner.BuglePackageManager;
import com.android.messaging.notificationcleaner.DateUtil;
import com.android.messaging.notificationcleaner.NotificationCleanerConstants;
import com.android.messaging.notificationcleaner.NotificationCleanerTest;
import com.android.messaging.notificationcleaner.NotificationCleanerUtil;
import com.android.messaging.notificationcleaner.animation.NotificationCleanerAnimatorUtils;
import com.android.messaging.notificationcleaner.animation.SpringInterpolator;
import com.android.messaging.notificationcleaner.data.BlockedNotificationDBHelper;
import com.android.messaging.notificationcleaner.data.BlockedNotificationInfo;
import com.android.messaging.notificationcleaner.data.NotificationCleanerProvider;
import com.android.messaging.notificationcleaner.resultpage.ResultManager;
import com.android.messaging.notificationcleaner.resultpage.util.ResultTransitionUtils;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.Typefaces;
import com.android.messaging.util.UiUtils;
import com.android.messaging.util.ViewUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Permissions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.nativead.AcbNativeAdLoader;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.viewholders.FlexibleViewHolder;

public class NotificationBlockedActivity extends BaseActivity
        implements FlexibleAdapter.OnItemSwipeListener, FlexibleAdapter.OnItemClickListener, INotificationObserver {

    public static final String START_FROM = "start_from";
    public static final String START_FROM_MAIN_PAGE = "main_page";
    public static final String START_FROM_NOTIFICATION_BAR = "notification_bar";
    public static final String START_FROM_GUIDE_BAR = "guide_bar";
    public static final String START_FROM_GUIDE_FULL = "guide_full";
    public static final String START_FROM_RESULT_AD_PAGE = "result_ad";

    public static final String NOTIFICATION_FINISH_SELF = "notification_finish_self";
    public static final String PREF_KEY_HAS_FAKE_NOTIFICATION_SHOWN = "PREF_KEY_HAS_FAKE_NOTIFICATION_SHOWN";

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (NOTIFICATION_FINISH_SELF.equals(s)) {
            finish();
        }
    }

    private static final long DURATION_ITEM_REMOVE_ANIMATION = 400;

    private FlexibleAdapter<AbstractFlexibleItem> mNotificationAdapter;
    private Button mClearAllBtn;
    private View mEmptyView;
    private View mIllustrationView;
    private FrameLayout mAdViewContainer;
    private AcbNativeAdLoader mAdLoader;
    private AcbNativeAd mNativeAd;
    private ProgressBar mProgressBar;
    private List<BlockedNotificationInfo> mNotificationDataList;

    private Handler handler = new Handler();

    private boolean mIsFromNotification;
    private boolean mIsBlockNotificationCountRecorded;
    private boolean mIsClearAll;
    private boolean mIsShownLogged = false;
    private boolean mIsAdChanceEventLogged = false;
    private boolean mIsAdShowEventLogged = false;
    private String mStartFrom;

    private ContentObserver mBlockListContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            if (mClearAllBtn.isClickable()) {
                fetchAndUpdateBlockNotificationList();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStartFrom = getIntent().getStringExtra(START_FROM);
        if (START_FROM_GUIDE_BAR.equals(mStartFrom)) {
            BugleAnalytics.logEvent("NotificationCleaner_GuidePush_Click", true);
        }

        if (!Permissions.isNotificationAccessGranted()
                || !NotificationCleanerProvider.isNotificationOrganizerSwitchOn()) {
            Intent intentGuide = new Intent(this, NotificationGuideActivity.class);
            intentGuide.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intentGuide.putExtra(START_FROM, mStartFrom);
            Navigations.startActivitySafely(this, intentGuide);
            finish();
            return;
        }

        setContentView(R.layout.activity_notification_cleaner_blocked);

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

        mIsFromNotification = START_FROM_NOTIFICATION_BAR.equals(mStartFrom);

        mClearAllBtn = findViewById(R.id.notification_btn_delete);
        mClearAllBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.primary_color),
                getResources().getColor(R.color.ripples_ripple_color), Dimensions.pxFromDp(3.3f),
                true, true));
        mClearAllBtn.setTypeface(Typefaces.getCustomSemiBold());
        mEmptyView = findViewById(R.id.notification_block_empty);
        mIllustrationView = findViewById(R.id.notification_block_illustration_page);
        mAdViewContainer = findViewById(R.id.ad_container);
        mProgressBar = findViewById(R.id.notification_block_progress_bar);

        mNotificationAdapter = new FlexibleAdapter<>(null, this);
        mNotificationAdapter.setRemoveOrphanHeaders(true);

        final RecyclerView notificationRecyclerView = findViewById(R.id.notification_block_recyclerview);
        notificationRecyclerView.setLayoutManager(new SmoothScrollLinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        notificationRecyclerView.setAdapter(mNotificationAdapter);
        notificationRecyclerView.setHasFixedSize(true);
        notificationRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mNotificationAdapter.setHandleDragEnabled(true)
                .setSwipeEnabled(true)
                .setDisplayHeadersAtStartUp(true)
                .showAllHeaders();

        if (!BillingManager.isPremiumUser()) {
            ResultManager.getInstance().preLoadAds();
        }

        mClearAllBtn.setOnClickListener(v -> {
            if (mNotificationAdapter.isEmpty()) {
                return;
            }

            BugleAnalytics.logEvent("NotificationCleaner_Homepage_BtnClick", true);
            NotificationCleanerTest.logNcHomepageBtnClick();
            mClearAllBtn.setClickable(false);

            final int itemCount = notificationRecyclerView.getChildCount();
            if (itemCount > 0) {
                final long itemAnimDuration = DURATION_ITEM_REMOVE_ANIMATION / itemCount;

                for (int i = 0; i < notificationRecyclerView.getChildCount(); i++) {
                    View childView = notificationRecyclerView.getChildAt(i);

                    childView.animate().translationX(-childView.getWidth())
                            .setDuration(DURATION_ITEM_REMOVE_ANIMATION / 2)
                            .setStartDelay(itemAnimDuration * i).start();

                    childView.animate().alpha(0).setDuration(DURATION_ITEM_REMOVE_ANIMATION / 2)
                            .setStartDelay(itemAnimDuration * i).start();
                }
            }

            handler.postDelayed(() -> {
                mIsClearAll = true;
                int clearSize = 0;
                if (null != mNotificationDataList) {
                    clearSize = mNotificationDataList.size();
                    mNotificationDataList.clear();
                }
                mNotificationAdapter.updateDataSet(null);
                ResultTransitionUtils.startForNotificationCleaner(NotificationBlockedActivity.this, clearSize);
                finish();
            }, DURATION_ITEM_REMOVE_ANIMATION);

            Threads.postOnThreadPoolExecutor(() -> getContentResolver().delete(NotificationCleanerProvider
                    .createBlockNotificationContentUri(HSApplication.getContext()), null, null));
        });

        getContentResolver().registerContentObserver(
                NotificationCleanerProvider.createBlockAppContentUri(HSApplication.getContext()),
                true, mBlockListContentObserver);

        if (NotificationCleanerProvider.isFirstGuide()) {
            findViewById(R.id.settings_for_whitelist).setVisibility(View.VISIBLE);
            handler.postDelayed(() -> {
                if (!isFinishing()) {
                    dismissSettingsGuideIfNeeded();
                }
            }, 8000);
            NotificationCleanerProvider.setFirstGuideFlag(false);

            if (mIsFromNotification) {
                HSApplication.getContext().sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            }
        }

        fetchAndUpdateBlockNotificationList();
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_FINISH_SELF, this);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        dismissSettingsGuideIfNeeded();
    }

    private void dismissSettingsGuideIfNeeded() {
        View settingsGuide = findViewById(R.id.settings_for_whitelist);
        if (settingsGuide != null && settingsGuide.getVisibility() == View.VISIBLE) {
            settingsGuide.animate()
                    .alpha(0)
                    .setDuration(350)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            settingsGuide.setVisibility(View.GONE);
                        }
                    }).start();
        }
    }

    private void updateBlockNotificationDataSet(List<BlockedNotificationInfo> blockedNotificationInfo) {
        if (!mIsShownLogged) {
            mIsShownLogged = true;
            Map<String, String> params = new HashMap<>();
            String from = null;
            switch (mStartFrom) {
                case START_FROM_GUIDE_BAR:
                    from = "guide_bar";
                    break;
                case START_FROM_MAIN_PAGE:
                    from = "menu";
                    break;
                case START_FROM_NOTIFICATION_BAR:
                    from = "bar";
                    break;
                case START_FROM_GUIDE_FULL:
                    from = "guide_full";
                    break;
            }
            if (from != null) {
                params.put("type", from);
                params.put("number", getStringByNotificationCount(blockedNotificationInfo.size()));
                BugleAnalytics.logEvent("NotificationCleaner_Homepage_Show", params);
                NotificationCleanerTest.logNcHomepageShow();
            }
        }

        if (!mIsAdChanceEventLogged) {
            mIsAdChanceEventLogged = true;
            BugleAnalytics.logEvent("NotificationCleaner_HomepageAd_Chance", true);
        }
        mProgressBar.setVisibility(View.GONE);
        if (null == blockedNotificationInfo) {
            return;
        }

        mNotificationDataList = blockedNotificationInfo;
        List<AbstractFlexibleItem> flexibleItems = new ArrayList<>();

        int lastHeaderDay = -1;
        HeaderItem headerItem = null;
        int count = 0;

        List<ApplicationInfo> applicationInfoList = BuglePackageManager.getInstance().getInstalledApplications();

        HSLog.d(NotificationCleanerConstants.TAG, "onPostExecute *** installed size = " + String.valueOf(null == applicationInfoList ? 0 : applicationInfoList.size()));

        for (BlockedNotificationInfo notificationData : mNotificationDataList) {
            boolean isContain = false;
            if (null == applicationInfoList || applicationInfoList.size() == 0) {
                isContain = true;
            } else {
                for (ApplicationInfo applicationInfo : applicationInfoList) {
                    if (null != applicationInfo && TextUtils.equals(notificationData.packageName, applicationInfo.packageName)) {
                        isContain = true;
                        break;
                    }
                }
            }
            HSLog.d(NotificationCleanerConstants.TAG, "onPostExecute *** installed mPackageName = " + notificationData.packageName + " isContain = " + isContain);

            if (NotificationCleanerUtil.FAKE_NOTIFICATION_PACKAGE_NAME_1.equals(notificationData.packageName)
                    || NotificationCleanerUtil.FAKE_NOTIFICATION_PACKAGE_NAME_2.equals(notificationData.packageName)) {
                isContain = true;
                if (!Preferences.get(BugleFiles.NOTIFICATION_CLEANER_PREFS).getBoolean(PREF_KEY_HAS_FAKE_NOTIFICATION_SHOWN, false)) {
                    notificationData.postTime = System.currentTimeMillis();
                    Preferences.get(BugleFiles.NOTIFICATION_CLEANER_PREFS).putBoolean(PREF_KEY_HAS_FAKE_NOTIFICATION_SHOWN, true);
                }
            }
            if (!isContain) {
                continue;
            }
            count++;
            int dayDiff = (int) DateUtil.daysFromNow(notificationData.postTime);
            if (dayDiff > lastHeaderDay) {
                String headText;
                if (dayDiff == 0) {
                    headText = getString(R.string.today);
                } else if (dayDiff == 1) {
                    headText = getString(R.string.yesterday);
                } else {
                    headText = DateUtil.getDaysAgoString(dayDiff);
                }
                headerItem = new HeaderItem(headText);
                lastHeaderDay = dayDiff;
            }
            HSLog.d(NotificationCleanerConstants.TAG, "onPostExecute *** add mPackageName = " + notificationData.packageName);
            flexibleItems.add(new NotificationAppItem(notificationData, headerItem));
        }

        mNotificationAdapter.updateDataSet(flexibleItems);
        mNotificationAdapter.setStickyHeaders(true);

        if (!flexibleItems.isEmpty() &&
                NotificationCleanerUtil.getNotificationBlockedActivityIllustratePageShowingState()
                        == NotificationCleanerUtil.NOTIFICATION_STATE_SHOWING) {
            NotificationCleanerUtil.setNotificationBlockedActivityIllustratePageShowingState(NotificationCleanerUtil.NOTIFICATION_STATE_SHOWED);
        }

        if (flexibleItems.isEmpty()
                && NotificationCleanerUtil.getNotificationBlockedActivityIllustratePageShowingState()
                == NotificationCleanerUtil.NOTIFICATION_STATE_SHOWING) {
            mEmptyView.setVisibility(View.GONE);
            mIllustrationView.setVisibility(View.VISIBLE);
        } else {
            mIllustrationView.setVisibility(View.GONE);
            mEmptyView.setVisibility(flexibleItems.isEmpty() ? View.VISIBLE : View.GONE);

            if (!flexibleItems.isEmpty() && !BillingManager.isPremiumUser()) {
                initAdView();
            }
        }

        if (flexibleItems.isEmpty()) {
            mClearAllBtn.setVisibility(View.GONE);
        } else {
            setActionButtonTranslation(count);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchAndUpdateBlockNotificationList() {
        HSLog.d(NotificationCleanerConstants.TAG, "fetchAndUpdateBlockNotificationList ***");
        Threads.execute(new AsyncTask<Void, Void, List<BlockedNotificationInfo>>() {

            @Override
            protected List<BlockedNotificationInfo> doInBackground(Void[] params) {
                return NotificationCleanerProvider.fetchBlockedAndTimeValidNotificationDataList(true);
            }

            @Override
            protected void onPostExecute(List<BlockedNotificationInfo> result) {
                updateBlockNotificationDataSet(result);

                if (!mIsBlockNotificationCountRecorded) {
                    mIsBlockNotificationCountRecorded = true;
                }
            }

        });
    }

    public void setActionButtonTranslation(int count) {
        mClearAllBtn.setText(getString(R.string.notification_cleaner_clean_all, count));
        if (mClearAllBtn.getVisibility() == View.GONE) {
            final float startTranslation = getResources().getDimensionPixelOffset(R.dimen.action_btn_anim_translation);
            final float toTranslation = 0;

            mClearAllBtn.setTranslationY(startTranslation);
            mClearAllBtn.setVisibility(View.VISIBLE);

            ViewPropertyAnimator mActionBtnAnimator = mClearAllBtn.animate()
                    .translationY(toTranslation)
                    .setDuration(NotificationCleanerAnimatorUtils.getShortAnimDuration() * 6)
                    .setInterpolator(new SpringInterpolator(0.3f))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            //ResultManager.getInstance().preLoadAdsAndMemInfo();
                        }
                    })
                    .setStartDelay(500);
            mActionBtnAnimator.start();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (mIsFromNotification) {
            HSApplication.getContext().sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        HSLog.d(NotificationCleanerConstants.TAG, "onResume(), start");
        if (mClearAllBtn.isClickable()) {
            fetchAndUpdateBlockNotificationList();
        } else if (null != mNotificationDataList && mIsClearAll) {
            updateBlockNotificationDataSet(mNotificationDataList);
        }

        HSLog.d(NotificationCleanerConstants.TAG, "onResume(), end");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notification_cleaner_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (id == R.id.notification_setting) {
            Navigations.startActivitySafely(this, new Intent(this, NotificationCleanerSettingActivity.class));
            overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HSLog.d(NotificationCleanerConstants.TAG, "onDestroy()");
        getContentResolver().unregisterContentObserver(mBlockListContentObserver);
        HSGlobalNotificationCenter.removeObserver(this);

        if (NotificationCleanerUtil.getNotificationBlockedActivityIllustratePageShowingState()
                == NotificationCleanerUtil.NOTIFICATION_STATE_SHOWING) {
            NotificationCleanerUtil.setNotificationBlockedActivityIllustratePageShowingState(NotificationCleanerUtil.NOTIFICATION_STATE_SHOWED);
        }

        if (mAdLoader != null) {
            mAdLoader.cancel();
            mAdLoader = null;
        }

        if (mNativeAd != null) {
            mNativeAd.release();
            mNativeAd = null;
        }
    }

    @Override
    public void onItemSwipe(int position, int direction) {
        final AbstractFlexibleItem item = mNotificationAdapter.getItem(position);
        if (!(item instanceof NotificationAppItem)) {
            return;
        }

        mNotificationAdapter.removeItem(position);

        Threads.postOnThreadPoolExecutor(() -> {
            NotificationAppItem notificationAppItem = (NotificationAppItem) item;
            HSApplication.getContext().getContentResolver().delete(
                    NotificationCleanerProvider.createBlockNotificationContentUri(HSApplication.getContext()),
                    BlockedNotificationDBHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(notificationAppItem.notificationData.idInDB)});
        });

        if (mNotificationAdapter.isEmpty()) {
            mClearAllBtn.setVisibility(View.GONE);
            //  BugleAnalytics.logEvent("NotificationCleaner_Act_Slide_LastOne");

            ResultTransitionUtils.startForNotificationCleaner(this, 1);
            finish();
        } else {
            mEmptyView.setVisibility(mNotificationAdapter.isEmpty() ? View.VISIBLE : View.GONE);
            int count = 0;
            for (int i = 0; i < mNotificationAdapter.getItemCount(); i++) {
                if (mNotificationAdapter.getItem(i) instanceof NotificationAppItem) {
                    count++;
                }
            }
            setActionButtonTranslation(count);
        }
    }

    @Override
    public void onActionStateChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onItemClick(int position) {

        HSLog.d(NotificationCleanerConstants.TAG, "onItemClick(), position = " + position);

        final AbstractFlexibleItem item = mNotificationAdapter.getItem(position);
        if (!(item instanceof NotificationAppItem)) {
            return false;
        }

        BlockedNotificationInfo blockedNotificationInfo = ((NotificationAppItem) item).getNotificationData();
        if (blockedNotificationInfo == null) {
            return false;
        }
        if (NotificationCleanerUtil.FAKE_NOTIFICATION_PACKAGE_NAME_2.equals(blockedNotificationInfo.packageName)
                || NotificationCleanerUtil.FAKE_NOTIFICATION_PACKAGE_NAME_1.equals(blockedNotificationInfo.packageName)) {
            return false;
        }

        PendingIntent contentIntent = blockedNotificationInfo.contentIntent;
        if (contentIntent != null) {
            try {
                contentIntent.send();
            } catch (PendingIntent.CanceledException e) {
                startActivityWithPackageName(blockedNotificationInfo.packageName);
            }
        } else {
            startActivityWithPackageName(blockedNotificationInfo.packageName);
        }

        Threads.execute(new AsyncTask<Void, Void, List<BlockedNotificationInfo>>() {
            @Override
            protected List<BlockedNotificationInfo> doInBackground(Void[] params) {
                NotificationAppItem notificationAppItem = (NotificationAppItem) item;

                getContentResolver().delete(
                        NotificationCleanerProvider.createBlockNotificationContentUri(getApplicationContext()),
                        BlockedNotificationDBHelper.COLUMN_ID + "=?",
                        new String[]{String.valueOf(notificationAppItem.notificationData.idInDB)});

                return NotificationCleanerProvider.fetchBlockedAndTimeValidNotificationDataList(true);
            }

            @Override
            protected void onPostExecute(List<BlockedNotificationInfo> result) {
                updateBlockNotificationDataSet(result);
            }
        });
        return false;
    }

    private void startActivityWithPackageName(String packageName) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent == null) {
            return;
        }

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Navigations.startActivitySafely(HSApplication.getContext(), launchIntent);
    }

    private String getStringByNotificationCount(int count) {
        if (count == 0) {
            return "0";
        } else if (count <= 5) {
            return "1-5";
        } else if (count <= 10) {
            return "6-10";
        } else {
            return ">10";
        }
    }

    private void initAdView() {
        if (mNativeAd != null) {
            return;
        }

        if (mNotificationAdapter.isEmpty()) {
            return;
        }
        mAdLoader = AcbNativeAdManager.createLoaderWithPlacement(AdPlacement.NOTIFICATION_CLEANER_AD_PLACEMENT_APP_MANAGER);
        mAdLoader.load(1, new AcbNativeAdLoader.AcbNativeAdLoadListener() {
            @Override
            public void onAdReceived(AcbNativeAdLoader acbNativeAdLoader, List<AcbNativeAd> list) {
                if (list.size() == 0) {
                    return;
                }
                mNativeAd = list.get(0);

                View adView = getLayoutInflater().inflate(R.layout.notification_cleaner_block_info_ad_layout,
                        mAdViewContainer, false);

                AcbNativeAdContainerView adContainer = new AcbNativeAdContainerView(HSApplication.getContext());

                adContainer.addContentView(adView);
                adContainer.setAdChoiceView(ViewUtils.findViewById(adView, R.id.ad_entry_adchoice));
                adContainer.setAdTitleView(ViewUtils.findViewById(adView, R.id.ad_entry_title));
                adContainer.setAdBodyView(ViewUtils.findViewById(adView, R.id.ad_entry_content));
                adContainer.setAdIconView(ViewUtils.findViewById(adView, R.id.ad_entry_icon));
                adContainer.setAdActionView(ViewUtils.findViewById(adView, R.id.ad_entry_button));
                adContainer.setAdPrimaryView(ViewUtils.findViewById(adView, R.id.ad_primary_view));

                ViewUtils.findViewById(adView, R.id.ad_entry_button).setBackground(
                        BackgroundDrawables.createBackgroundDrawable(Color.TRANSPARENT,
                                getResources().getColor(R.color.ripples_ripple_color), Dimensions.pxFromDp(1), 0xffffffff,
                                Dimensions.pxFromDp(3.3f), true, true));

                adContainer.fillNativeAd(mNativeAd, null);

                mAdViewContainer.removeAllViews();
                mAdViewContainer.setVisibility(View.VISIBLE);
                mAdViewContainer.addView(adContainer);

                if (!mIsAdShowEventLogged) {
                    mIsAdShowEventLogged = true;
                    NotificationCleanerTest.logNcHomepageAdShow();
                    BugleAnalytics.logEvent("NotificationCleaner_HomepageAd_Show", true);
                }
                mNativeAd.setNativeClickListener(acbAd ->
                        BugleAnalytics.logEvent("NotificationCleaner_HomepageAd_Click", true));
            }

            @Override
            public void onAdFinished(AcbNativeAdLoader acbNativeAdLoader, AcbError acbError) {
            }
        });

    }

    static class HeaderViewHolder extends FlexibleViewHolder {

        TextView mTitleTextView;

        HeaderViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);

            mTitleTextView = view.findViewById(R.id.notification_block_date);
        }
    }

    public class HeaderItem extends AbstractHeaderItem<HeaderViewHolder> {

        private String title;

        HeaderItem(String title) {
            super();

            setDraggable(true);

            this.title = title;
        }

        @Override
        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }

            if (object == null || getClass() != object.getClass()) {
                return false;
            }

            HeaderItem headerItem = (HeaderItem) object;
            return this.title.equals(headerItem.title);
        }

        @Override
        public int hashCode() {
            return title.hashCode();
        }


        @Override
        public int getLayoutRes() {
            return R.layout.notification_cleaner_block_group_item;
        }

        @Override
        public HeaderViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
            return new HeaderViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void bindViewHolder(FlexibleAdapter adapter, HeaderViewHolder holder, int position, List payloads) {
            holder.mTitleTextView.setText(title);
        }
    }

    private static class NotificationAppViewHolder extends FlexibleViewHolder {

        private final ViewGroup frontView;
        final ImageView appIconImageView;
        private final TextView appNameTextView;
        final TextView appDescriptionTextView;
        final TextView postedTimeTextView;

        private final View rearLeftView;
        private final View rearRightView;

        NotificationAppViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);

            frontView = view.findViewById(R.id.front_view);
            appIconImageView = view.findViewById(R.id.app_icon_image);
            appNameTextView = view.findViewById(R.id.app_name_text);
            appDescriptionTextView = view.findViewById(R.id.app_description_text);
            postedTimeTextView = view.findViewById(R.id.notification_posted_time);

            rearLeftView = view.findViewById(R.id.rear_left_view);
            rearRightView = view.findViewById(R.id.rear_right_view);
        }

        @Override
        public View getFrontView() {
            return frontView;
        }

        @Override
        public View getRearLeftView() {
            return rearLeftView;
        }

        @Override
        public View getRearRightView() {
            return rearRightView;
        }
    }

    private class NotificationAppItem extends AbstractFlexibleItem<NotificationAppViewHolder>
            implements ISectionable<NotificationAppViewHolder, HeaderItem> {

        private BlockedNotificationInfo notificationData;
        private HeaderItem headerItem;

        NotificationAppItem(BlockedNotificationInfo notificationData, HeaderItem headerItem) {
            this.notificationData = notificationData;
            this.headerItem = headerItem;

            setSelectable(false);
            setDraggable(false);
            setSwipeable(true);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }

            if (object == null || getClass() != object.getClass()) {
                return false;
            }

            NotificationAppItem notificationAppItem = (NotificationAppItem) object;
            return notificationData.idInDB == notificationAppItem.notificationData.idInDB;
        }

        @Override
        public int hashCode() {
            return (int) notificationData.idInDB;
        }

        @Override
        public HeaderItem getHeader() {
            return headerItem;
        }

        @Override
        public void setHeader(HeaderItem headerItem) {
            this.headerItem = headerItem;
        }

        @Override
        public int getLayoutRes() {
            return R.layout.notification_cleaner_block_app_item;
        }

        @Override
        public NotificationAppViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
            return new NotificationAppViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void bindViewHolder(FlexibleAdapter adapter, NotificationAppViewHolder notificationAppHolder, int position, List payloads) {
            notificationAppHolder.appIconImageView.setImageDrawable(
                    BuglePackageManager.getInstance().getApplicationIcon(notificationData.packageName));
            notificationAppHolder.appNameTextView.setText(notificationData.title);

            if (TextUtils.isEmpty(notificationData.text)) {
                notificationAppHolder.appDescriptionTextView.setVisibility(View.GONE);
            } else {
                notificationAppHolder.appDescriptionTextView.setVisibility(View.VISIBLE);
                notificationAppHolder.appDescriptionTextView.setText(notificationData.text);
            }

            notificationAppHolder.postedTimeTextView.setText(DateUtil.convertTimeStampToString(notificationData.postTime));
        }

        BlockedNotificationInfo getNotificationData() {
            return notificationData;
        }
    }
}