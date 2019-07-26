/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.messaging.ui.conversationlist;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Telephony;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewGroupCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewPropertyAnimator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ad.AdConfig;
import com.android.messaging.ad.AdPlacement;
import com.android.messaging.ad.BillingManager;
import com.android.messaging.annotation.VisibleForAnimation;
import com.android.messaging.backup.BackupAutopilotUtils;
import com.android.messaging.backup.ui.BackupRestoreActivity;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.binding.Binding;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.data.AdItemData;
import com.android.messaging.datamodel.data.ConversationListData;
import com.android.messaging.datamodel.data.ConversationListData.ConversationListDataListener;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.mmslib.SqliteWrapper;
import com.android.messaging.privatebox.PrivateBoxSettings;
import com.android.messaging.privatebox.ui.PrivateBoxSetPasswordActivity;
import com.android.messaging.privatebox.ui.SelfVerifyActivity;
import com.android.messaging.sms.MmsUtils;
import com.android.messaging.ui.BugleAnimationTags;
import com.android.messaging.ui.ListEmptyView;
import com.android.messaging.ui.SnackBarInteraction;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.WallpaperDrawables;
import com.android.messaging.ui.customize.mainpage.ChatListCustomizeManager;
import com.android.messaging.ui.customize.theme.CreateIconDrawable;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.ui.view.MessagesTextView;
import com.android.messaging.util.AccessibilityUtil;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleFirebaseAnalytics;
import com.android.messaging.util.CommonUtils;
import com.android.messaging.util.ImeUtil;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.UiUtils;
import com.android.messaging.util.ViewUtils;
import com.google.common.annotations.VisibleForTesting;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdIconView;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.nativead.AcbNativeAdLoader;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;
import net.appcloudbox.autopilot.AutopilotEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.android.messaging.ui.conversationlist.CustomizeGuideController.PREF_KEY_SHOULD_SHOW_CUSTOMIZE_GUIDE;


/**
 * Shows a list of conversations.
 */
public class ConversationListFragment extends Fragment implements ConversationListDataListener,
        ConversationListItemView.HostInterface {
    public static final String PREF_KEY_BACKUP_SHOW_BANNER_GUIDE = "pref_key_backup_banner_guide_hide";
    private static final String PREF_KEY_BACKUP_BANNER_GUIDE_SHOW_COUNT = "pref_key_backup_banner_guide_show_count";
    private static final String BUNDLE_ARCHIVED_MODE = "archived_mode";
    private static final String BUNDLE_FORWARD_MESSAGE_MODE = "forward_message_mode";
    private static final boolean VERBOSE = false;

    private boolean mArchiveMode;
    private boolean mBlockedAvailable;
    private boolean mForwardMessageMode;
    private ViewGroup mAdContainer;
    private LinearLayoutManager manager;
    private boolean switchAd;
    private boolean adFirstPrepared = true;
    private boolean conversationFirstUpdated = true;
    private boolean isFirstOnResume = true;

    private AcbNativeAd mNativeAd;
    private AcbNativeAdLoader mNativeAdLoader;

    public interface ConversationListFragmentHost {
        void onConversationClick(final ConversationListData listData,
                                 final ConversationListItemData conversationListItemData,
                                 final boolean isLongClick,
                                 final ConversationListItemView conversationView);

        void onCreateConversationClick();

        boolean isConversationSelected(final String conversationId);

        boolean isSwipeAnimatable();

        boolean isSelectionMode();

        boolean hasWindowFocus();
    }

    private ConversationListFragmentHost mHost;
    private ConversationListRecyclerView mRecyclerView;
    private ImageView mStartNewConversationButton;
    private ListEmptyView mEmptyListMessageView;
    private ConversationListAdapter mAdapter;
    private ConstraintLayout mBackupBannerGuideContainer;

    // Saved Instance State Data - only for temporal data which is nice to maintain but not
    // critical for correctness.
    private static final String SAVED_INSTANCE_STATE_LIST_VIEW_STATE_KEY =
            "conversationListViewState";
    private Parcelable mListState;

    @VisibleForTesting
    final Binding<ConversationListData> mListBinding = BindingBase.createBinding(this);

    public static ConversationListFragment createArchivedConversationListFragment() {
        return createConversationListFragment(BUNDLE_ARCHIVED_MODE);
    }

    public static ConversationListFragment createForwardMessageConversationListFragment() {
        return createConversationListFragment(BUNDLE_FORWARD_MESSAGE_MODE);
    }

    public static ConversationListFragment createConversationListFragment(String modeKeyName) {
        final ConversationListFragment fragment = new ConversationListFragment();
        final Bundle bundle = new Bundle();
        bundle.putBoolean(modeKeyName, true);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * {@inheritDoc} from Fragment
     */
    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        mListBinding.getData().init(getLoaderManager(), mListBinding);
        mAdapter = new ConversationListAdapter(getActivity(), this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        if (activity instanceof ConversationListActivity) {
            if (((ConversationListActivity) activity).getExitAdShown()) {
                return;
            }
        }
        if (mRecyclerView != null) {
            if (mRecyclerView.canScrollVertically(-1)) {
                BugleAnalytics.logEvent("SMS_Messages_Show_NotOnTop", true);
            } else {
                switchAd = false;
                if (!isFirstOnResume) {
                    if (mAdapter.getItemCount() > 0) {
                        changeTopBackupGuideState();
                    }
                    if (mBackupBannerGuideContainer.getVisibility() == View.GONE) {
                        tryShowTopNativeAd();
                    }
                }
            }
        }
        Assert.notNull(mHost);
        setScrolledToNewestConversationIfNeeded();
        updateUi();
        isFirstOnResume = false;
    }

    public void setScrolledToNewestConversationIfNeeded() {
        if (!mArchiveMode
                && !mForwardMessageMode
                && isScrolledToFirstConversation()
                && mHost.hasWindowFocus()) {
            mListBinding.getData().setScrolledToNewestConversation(true);
        }
    }

    private boolean isScrolledToFirstConversation() {
        int firstItemPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                .findFirstCompletelyVisibleItemPosition();
        return firstItemPosition == 0;
    }

    /**
     * {@inheritDoc} from Fragment
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mListBinding.unbind();
        mHost = null;
        if (mNativeAdLoader != null) {
            mNativeAdLoader.cancel();
        }
        if (mNativeAd != null) {
            mNativeAd.release();
        }
    }

    /**
     * {@inheritDoc} from Fragment
     */
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.conversation_list_fragment,
                container, false);
        mRecyclerView = rootView.findViewById(android.R.id.list);
        mEmptyListMessageView = rootView.findViewById(R.id.no_conversations_view);
        mEmptyListMessageView.setImageHint(R.drawable.ic_oobe_conv_list);
        mBackupBannerGuideContainer = rootView.findViewById(R.id.backup_banner_guide_container);
        ImageView conversationListBg = rootView.findViewById(R.id.conversation_list_bg);
        Drawable customDrawable = ChatListCustomizeManager.getWallpaperDrawable();
        Drawable bgDrawable = customDrawable != null ? customDrawable : WallpaperDrawables.getConversationListWallpaperDrawable();
        getActivity().getWindow().getDecorView().setBackground(null);
        if (bgDrawable == null) {
            getActivity().getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        } else {
            conversationListBg.setImageDrawable(bgDrawable);
        }

        // The default behavior for default layout param generation by LinearLayoutManager is to
        // provide width and height of WRAP_CONTENT, but this is not desirable for
        // ConversationListFragment; the view in each row should be a width of MATCH_PARENT so that
        // the entire row is tappable.
        final Activity activity = getActivity();
        manager = new LinearLayoutManager(activity) {
            @Override
            public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        };
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        mRecyclerView.setItemAnimator(defaultItemAnimator);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            int mCurrentState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

            private boolean mUpDetected;
            private boolean mDownDetected;

            private boolean isFirstConversationVisible = true;

            @Override
            public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
                if (mCurrentState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
                        || mCurrentState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    ImeUtil.get().hideImeKeyboard(getActivity(), mRecyclerView);
                }

                if (!isFirstConversationVisible && isScrolledToFirstConversation()) {
                    BugleAnalytics.logEvent("SMS_Messages_SlideUpToTop");
                    if (switchAd) {
                        if (mBackupBannerGuideContainer.getVisibility() == View.GONE) {
                            tryShowTopNativeAd();
                            switchAd = false;
                        }
                    }
                }

                isFirstConversationVisible = isScrolledToFirstConversation();
                if (isFirstConversationVisible) {
                    setScrolledToNewestConversationIfNeeded();
                } else {
                    mListBinding.getData().setScrolledToNewestConversation(false);
                }

                if (dy != 0) {
                    if (dy > 0 && !mUpDetected) {
                        mUpDetected = true;
                        BugleAnalytics.logEvent("SMS_Messages_SlideUp");
                    }
                    if (dy < 0 && !mDownDetected) {
                        mDownDetected = true;
                        BugleAnalytics.logEvent("SMS_Messages_SlideDown");
                    }
                }
            }

            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                mCurrentState = newState;

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Reset
                    mUpDetected = false;
                    mDownDetected = false;
                }
            }
        });

        mRecyclerView.addOnItemTouchListener(new ConversationListSwipeHelper(mRecyclerView));

        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(SAVED_INSTANCE_STATE_LIST_VIEW_STATE_KEY);
        }

        mStartNewConversationButton = rootView.findViewById(
                R.id.start_new_conversation_button);
        Drawable drawable = CreateIconDrawable.getCreateIconDrawable();
        if (drawable != null) {
            mStartNewConversationButton.setImageDrawable(drawable);
        } else {
            mStartNewConversationButton.setImageResource(R.drawable.ic_new_conversation_white);
        }
        mStartNewConversationButton.setBackgroundDrawable(BackgroundDrawables.
                createBackgroundDrawable(PrimaryColors.getEditButtonColor(),
                        Dimensions.pxFromDp(28),
                        true));
        if (mArchiveMode || mForwardMessageMode) {
            mStartNewConversationButton.setVisibility(View.GONE);
        } else {
            mStartNewConversationButton.setVisibility(View.VISIBLE);
            mStartNewConversationButton.setOnClickListener(clickView -> {
                ConversationListActivity.logFirstComeInClickEvent("create");
                BugleAnalytics.logEvent("SMS_CreateMessage_ButtonClick", true);
                BugleFirebaseAnalytics.logEvent("SMS_CreateMessage_ButtonClick");
                mHost.onCreateConversationClick();
            });
            mStartNewConversationButton.setOnLongClickListener(v -> {
                if (!PrivateBoxSettings.getIsPrivateBoxEnabled()) {
                    return true;
                }

                if (PrivateBoxSettings.isAnyPasswordSet()) {
                    Intent intent = new Intent(getActivity(), SelfVerifyActivity.class);
                    intent.putExtra(SelfVerifyActivity.INTENT_KEY_ACTIVITY_ENTRANCE,
                            SelfVerifyActivity.ENTRANCE_CREATE_ICON);
                    Navigations.startActivitySafely(getActivity(), intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
                } else {
                    Navigations.startActivitySafely(getActivity(),
                            new Intent(getActivity(), PrivateBoxSetPasswordActivity.class));
                    getActivity().overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
                }
                return true;
            });
        }
        ViewCompat.setTransitionName(mStartNewConversationButton, BugleAnimationTags.TAG_FABICON);

        // The root view has a non-null background, which by default is deemed by the framework
        // to be a "transition group," where all child views are animated together during an
        // activity transition. However, we want each individual items in the recycler view to
        // show explode animation themselves, so we explicitly tag the root view to be a non-group.
        ViewGroupCompat.setTransitionGroup(rootView, false);

        if (!Preferences.getDefault().getBoolean("pref_key_first_loading_show", false)) {
            mEmptyListMessageView.setTextHint(R.string.conversation_list_first_sync_text);
            mEmptyListMessageView.setVisibility(View.VISIBLE);
            mEmptyListMessageView.setIsImageVisible(false);
            mEmptyListMessageView.setIsLoadingAnimationVisible(true);
            mEmptyListMessageView.setIsVerticallyCentered(true);
            Preferences.getDefault().putBoolean("pref_key_first_loading_show", true);
        }

        setHasOptionsMenu(true);
        return rootView;
    }

    private void changeTopBackupGuideState() {
        Preferences prefFile = Preferences.getDefault();
        //check show times
        int backupBannerGuideShowCount = prefFile.getInt(PREF_KEY_BACKUP_BANNER_GUIDE_SHOW_COUNT, 0);
        if (backupBannerGuideShowCount >= HSConfig.optInteger(6, "Application", "BackupRestore", "RecommendBannerTimes")) {
            mBackupBannerGuideContainer.setVisibility(View.GONE);
            return;
        }

        // check new user
        if (!CommonUtils.isNewUser()) {
            return;
        }

        // check top guide enable
        boolean topGuideEnable = prefFile.getBoolean(PREF_KEY_BACKUP_SHOW_BANNER_GUIDE, true);
        if (!topGuideEnable) {
            mBackupBannerGuideContainer.setVisibility(View.GONE);
            return;
        }

        // check backup activity opened
        boolean backupActivityShown = prefFile.
                getBoolean(BackupRestoreActivity.PREF_KEY_BACKUP_ACTIVITY_SHOWN, false);
        if (backupActivityShown) {
            mBackupBannerGuideContainer.setVisibility(View.GONE);
            return;
        }

        prefFile.putInt(PREF_KEY_BACKUP_BANNER_GUIDE_SHOW_COUNT, backupBannerGuideShowCount + 1);
        BugleAnalytics.logEvent("BackupTopGuide_Show", true);

        TextView title = mBackupBannerGuideContainer.findViewById(R.id.backup_banner_title);
        if (mBackupBannerGuideContainer.getVisibility() == View.VISIBLE) {

            Threads.postOnThreadPoolExecutor(() -> {
                WeakReference<TextView> tv = new WeakReference<>(title);

                final Context context = HSApplication.getContext();
                Cursor cursor = SqliteWrapper.query(
                        context,
                        context.getContentResolver(),
                        Telephony.Sms.CONTENT_URI,
                        new String[]{"COUNT(*)"},
                        MmsUtils.getSmsTypeSelectionSql(),
                        null,
                        Telephony.Sms.DATE + " DESC");

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int count = cursor.getInt(0);
                        Threads.postOnMainThread(() -> {
                            if (tv.get() != null && isAdded()) {
                                tv.get().setText(HSApplication.getContext().getString(R.string.backup_banner_guide_title, count));
                            }
                        });
                    }
                    cursor.close();
                }
            });
            return;
        }

        if (BackupAutopilotUtils.getIsBackupSwitchOn()) {
            mBackupBannerGuideContainer.setVisibility(View.VISIBLE);
        } else {
            mBackupBannerGuideContainer.setVisibility(View.GONE);
        }

        title.setText(HSApplication.getContext().getString(R.string.backup_banner_guide_title, 30));
        Threads.postOnThreadPoolExecutor(() -> {
            WeakReference<TextView> tv = new WeakReference<>(title);

            final Context context = HSApplication.getContext();
            Cursor cursor = SqliteWrapper.query(
                    context,
                    context.getContentResolver(),
                    Telephony.Sms.CONTENT_URI,
                    new String[]{"COUNT(*)"},
                    MmsUtils.getSmsTypeSelectionSql(),
                    null,
                    Telephony.Sms.DATE + " DESC");
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int count = cursor.getInt(0);
                    Threads.postOnMainThread(() -> {
                        if (tv.get() != null && isAdded()) {
                            tv.get().setText(HSApplication.getContext().getString(R.string.backup_banner_guide_title, count));
                        }
                    });
                }
                cursor.close();
            }
        });


        MessagesTextView backupBannerButton = mBackupBannerGuideContainer.findViewById(R.id.backup_banner_button);
        backupBannerButton.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE,
                Dimensions.pxFromDp(3.3f), true));
        backupBannerButton.setOnClickListener(v -> {
            BackupRestoreActivity.startBackupRestoreActivity(getActivity(), BackupRestoreActivity.ENTRANCE_TOP_GUIDE);
            getActivity().overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
            mBackupBannerGuideContainer.setVisibility(View.GONE);
            tryShowTopNativeAd();
            switchAd = false;
            BugleAnalytics.logEvent("BackupTopGuide_Click", true);
        });

        mBackupBannerGuideContainer.setOnClickListener(v -> backupBannerButton.performClick());

        ImageView backupBannerGuideCloseButton = mBackupBannerGuideContainer.findViewById(R.id.backup_banner_close);
        backupBannerGuideCloseButton.setBackground(BackgroundDrawables.
                createTransparentBackgroundDrawable(0x33000000, Dimensions.pxFromDp(12)));
        backupBannerGuideCloseButton.setOnClickListener(v -> {
            mBackupBannerGuideContainer.setVisibility(View.GONE);
            Preferences.getDefault().putBoolean(PREF_KEY_BACKUP_SHOW_BANNER_GUIDE, false);
            tryShowTopNativeAd();
            switchAd = false;
        });
    }

    private boolean isAdLoading = false;

    private void tryShowTopNativeAd() {
        HSLog.d("try show top native ad");
        if (BillingManager.isPremiumUser()) {
            return;
        }

        if (!AdConfig.isHomepageBannerAdEnabled()) {
            return;
        }
        if (isAdLoading) {
            return;
        }
        if (mNativeAd != null) {
            mNativeAd.release();
        }
        if (mNativeAdLoader != null) {
            mNativeAdLoader.cancel();
        }

        BugleAnalytics.logEvent("SMS_Messages_BannerAd_Should_Show", true);
        BugleFirebaseAnalytics.logEvent("SMS_Messages_BannerAd_Should_Show");
        AutopilotEvent.logTopicEvent("topic-768lyi3sp", "bannerad_chance");

        List<AcbNativeAd> nativeAds = AcbNativeAdManager.fetch(AdPlacement.AD_BANNER, 1);
        if (nativeAds.size() > 0) {
            mNativeAd = nativeAds.get(0);
            mNativeAd.setNativeClickListener(
                    acbAd -> {
                        BugleAnalytics.logEvent("SMS_Messages_BannerAd_Click", true,
                                "theme", String.valueOf(ThemeUtils.isDefaultTheme()));
                        BugleFirebaseAnalytics.logEvent("SMS_Messages_BannerAd_Click",
                                "theme", String.valueOf(ThemeUtils.isDefaultTheme()));
                    });
            showTopNativeAd();
        } else {
            mNativeAdLoader = AcbNativeAdManager.createLoaderWithPlacement(AdPlacement.AD_BANNER);
            mNativeAdLoader.load(1, new AcbNativeAdLoader.AcbNativeAdLoadListener() {
                @Override
                public void onAdReceived(AcbNativeAdLoader acbNativeAdLoader, List<AcbNativeAd> list) {
                    if (list.size() > 0) {
                        mNativeAd = list.get(0);
                        mNativeAd.setNativeClickListener(
                                acbAd -> {
                                    BugleAnalytics.logEvent("SMS_Messages_BannerAd_Click", true,
                                            "theme", String.valueOf(ThemeUtils.isDefaultTheme()));
                                    BugleFirebaseAnalytics.logEvent("SMS_Messages_BannerAd_Click",
                                            "theme", String.valueOf(ThemeUtils.isDefaultTheme()));
                                });
                        showTopNativeAd();
                    }
                    isAdLoading = false;
                }

                @Override
                public void onAdFinished(AcbNativeAdLoader acbNativeAdLoader, AcbError acbError) {
                    isAdLoading = false;
                }
            });
            isAdLoading = true;
        }
    }

    private void showTopNativeAd() {
        HSLog.d("show top native ad");

        if (mNativeAd == null) {
            return;
        }
        if (mAdContainer == null) {
            mAdContainer = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.conversation_list_header, mRecyclerView, false);
        }

        if (getActivity() == null || UiUtils.isDestroyed(getActivity())) {
            return;
        }

        final View adView = LayoutInflater.from(getActivity()).inflate(R.layout.item_conversation_list_ad, mAdContainer, false);

        AcbNativeAdContainerView mAdContentView = new AcbNativeAdContainerView(mAdContainer.getContext());
        mAdContentView.addContentView(adView);

        AcbNativeAdIconView icon = ViewUtils.findViewById(adView, R.id.banner_icon_image);
        icon.setShapeMode(1);
        icon.setRadius(Dimensions.pxFromDp(20));
        mAdContentView.setAdIconView(icon);
        TextView title = ViewUtils.findViewById(adView, R.id.banner_title);
        title.setTextColor(ConversationColors.get().getListTitleColor());
        ChatListCustomizeManager.changeViewColorIfNeed(title);
        mAdContentView.setAdTitleView(title);
        TextView description = ViewUtils.findViewById(adView, R.id.banner_des);
        description.setTextColor(ConversationColors.get().getListSubtitleColor());
        ChatListCustomizeManager.changeViewColorIfNeed(description);
        mAdContentView.setAdBodyView(description);

        TextView actionBtn = ViewUtils.findViewById(adView, R.id.banner_action);
        mAdContentView.setAdActionView(actionBtn);
        actionBtn.setTextColor(Color.parseColor(ThemeUtils.getCurrentTheme().listTimeColor));
        ChatListCustomizeManager.changeViewColorIfNeed(actionBtn);

        FrameLayout choice = ViewUtils.findViewById(adView, R.id.ad_choice);
        mAdContentView.setAdChoiceView(choice);
        mAdContainer.removeAllViews();
        mAdContainer.addView(mAdContentView);

        ImageView ivAdPreview = adView.findViewById(R.id.icon_ad_preview);
        ivAdPreview.getDrawable().setColorFilter(ConversationColors.get().getListTimeColor(), PorterDuff.Mode.SRC_ATOP);
        ChatListCustomizeManager.changeDrawableColorIfNeed(ivAdPreview.getDrawable(), false);

        mAdContentView.hideAdCorner();
        mAdContentView.fillNativeAd(mNativeAd);

        if (!mAdapter.hasHeader()) {
            mAdapter.setHeader(mAdContainer);
            if (manager.findFirstCompletelyVisibleItemPosition() == 0) {
                mRecyclerView.scrollToPosition(0);
            }
        }
        BugleAnalytics.logEvent("SMS_Messages_BannerAd_Show", true,
                "theme", String.valueOf(ThemeUtils.isDefaultTheme()));
        BugleFirebaseAnalytics.logEvent("SMS_Messages_BannerAd_Show",
                "theme", String.valueOf(ThemeUtils.isDefaultTheme()));
        AutopilotEvent.logTopicEvent("topic-768lyi3sp", "bannerad_show");
    }

    public void disableTopNativeAd() {
        if (mNativeAd != null) {
            mNativeAd.release();
            mNativeAd = null;
        }
        if (mNativeAdLoader != null) {
            mNativeAdLoader.cancel();
            mNativeAdLoader = null;
        }
        if (mAdapter.hasHeader()) {
            mAdapter.setHeader(null);
        }
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        if (VERBOSE) {
            LogUtil.v(LogUtil.BUGLE_TAG, "Attaching List");
        }
        final Bundle arguments = getArguments();
        if (arguments != null) {
            mArchiveMode = arguments.getBoolean(BUNDLE_ARCHIVED_MODE, false);
            mForwardMessageMode = arguments.getBoolean(BUNDLE_FORWARD_MESSAGE_MODE, false);
        }
        mListBinding.bind(DataModel.get().createConversationListData(activity, this, mArchiveMode));
    }


    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mListState != null) {
            outState.putParcelable(SAVED_INSTANCE_STATE_LIST_VIEW_STATE_KEY, mListState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mListState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        mListBinding.getData().setScrolledToNewestConversation(false);

        if (mNativeAdLoader != null) {
            mNativeAdLoader.cancel();
            isAdLoading = false;
        }
        switchAd = true;
    }

    /**
     * Call this immediately after attaching the fragment
     */
    public void setHost(final ConversationListFragmentHost host) {
        Assert.isNull(mHost);
        mHost = host;
    }

    @Override
    public void onConversationListCursorUpdated(final ConversationListData data,
                                                final Cursor cursor) {
        mListBinding.ensureBound(data);

        ArrayList<Object> dataList = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ConversationListItemData itemData = new ConversationListItemData();
                itemData.bind(cursor);
                if (!itemData.isPrivate()) {
                    dataList.add(itemData);
                }
            } while (cursor.moveToNext());
        }

        if (conversationFirstUpdated) {
            conversationFirstUpdated = false;
            boolean hasPinConversation = false;
            int archivedCount = 0;
            if (!dataList.isEmpty()) {
                for (Object object : dataList) {
                    if (object instanceof ConversationListItemData) {
                        ConversationListItemData itemData = (ConversationListItemData) object;
                        if (itemData.getIsArchived()) {
                            archivedCount++;
                        }
                        if (itemData.isPinned()) {
                            hasPinConversation = true;
                        }
                    }
                }
                changeTopBackupGuideState();
            }

            if (mArchiveMode) {
                BugleAnalytics.logEvent("Archive_Homepage_Show", true,
                        "num", String.valueOf(archivedCount));
            } else {
                HSBundle hsBundle = new HSBundle();
                hsBundle.putBoolean(ConversationListActivity.HAS_PIN_CONVERSATION, hasPinConversation);
                HSGlobalNotificationCenter.sendNotification(ConversationListActivity.FIRST_LOAD, hsBundle);
            }
        }

        if (dataList.size() > 0 && mAdapter.hasHeader()) {
            dataList.add(0, new AdItemData());
        }
        mAdapter.setDataList(dataList);
        HSLog.d("conversation list has : " + dataList.size());
        if (adFirstPrepared && !dataList.isEmpty()) {
            if (mBackupBannerGuideContainer.getVisibility() == View.GONE) {
                if (Preferences.getDefault().getBoolean(PREF_KEY_SHOULD_SHOW_CUSTOMIZE_GUIDE, true)) {
                    Threads.postOnMainThreadDelayed(this::tryShowTopNativeAd, 2000);
                } else {
                    tryShowTopNativeAd();
                }
                adFirstPrepared = false;
            }
        }
        updateEmptyListUi(cursor == null || dataList.size() == 0);
    }

    @Override
    public void setBlockedParticipantsAvailable(final boolean blockedAvailable) {
        mBlockedAvailable = blockedAvailable;
    }

    public void updateUi() {
        mAdapter.notifyDataSetChanged();
    }

    public void startMultiSelectMode() {
        if (mArchiveMode || mForwardMessageMode) {
            return;
        }
        mStartNewConversationButton.animate().scaleX(0.5f).scaleY(0.5f).alpha(0).setDuration(200).start();
    }

    public void exitMultiSelectMode() {
        if (mArchiveMode || mForwardMessageMode) {
            return;
        }
        mStartNewConversationButton.animate().scaleX(1f).scaleY(1f).alpha(1).setDuration(200).start();
    }

    public void hideBackupBannerGuide() {
        mBackupBannerGuideContainer.setVisibility(View.GONE);
    }

    /**
     * {@inheritDoc} from ConversationListItemView.HostInterface
     */
    @Override
    public void onConversationClicked(final ConversationListItemData conversationListItemData,
                                      final boolean isLongClick, final ConversationListItemView conversationView) {
        final ConversationListData listData = mListBinding.getData();
        mHost.onConversationClick(listData, conversationListItemData, isLongClick,
                conversationView);
    }

    /**
     * {@inheritDoc} from ConversationListItemView.HostInterface
     */
    @Override
    public boolean isConversationSelected(final String conversationId) {
        return mHost.isConversationSelected(conversationId);
    }

    @Override
    public boolean isSwipeAnimatable() {
        return mHost.isSwipeAnimatable();
    }

    // Show and hide empty list UI as needed with appropriate text based on view specifics
    private void updateEmptyListUi(final boolean isEmpty) {
        HSLog.d("update empty list " + isEmpty);
        if (isEmpty) {
            int emptyListText;
            boolean isFirstSynsCompleted = true;
            if (!mListBinding.getData().getHasFirstSyncCompleted()) {
                emptyListText = R.string.conversation_list_first_sync_text;
                isFirstSynsCompleted = false;
            } else if (mArchiveMode) {
                emptyListText = R.string.archived_conversation_list_empty_text;
            } else {
                emptyListText = R.string.conversation_list_empty_text;
            }
            mEmptyListMessageView.setTextHint(emptyListText);
            mEmptyListMessageView.setVisibility(View.VISIBLE);
            mEmptyListMessageView.setIsImageVisible(isFirstSynsCompleted);
            mEmptyListMessageView.setIsLoadingAnimationVisible(!isFirstSynsCompleted);
            mEmptyListMessageView.setIsVerticallyCentered(true);

        } else {
            // stop loading animation
            mEmptyListMessageView.setIsLoadingAnimationVisible(false);
            mEmptyListMessageView.setVisibility(View.GONE);
        }
    }

    @Override
    public List<SnackBarInteraction> getSnackBarInteractions() {
        final List<SnackBarInteraction> interactions = new ArrayList<>(1);
        final SnackBarInteraction fabInteraction =
                new SnackBarInteraction.BasicSnackBarInteraction(mStartNewConversationButton);
        interactions.add(fabInteraction);
        return interactions;
    }

    private ViewPropertyAnimator getNormalizedFabAnimator() {
        return mStartNewConversationButton.animate()
                .setInterpolator(UiUtils.DEFAULT_INTERPOLATOR)
                .setDuration(getActivity().getResources().getInteger(
                        R.integer.fab_animation_duration_ms));
    }

    public ViewPropertyAnimator dismissFab() {
        // To prevent clicking while animating.
        mStartNewConversationButton.setEnabled(false);
        final MarginLayoutParams lp =
                (MarginLayoutParams) mStartNewConversationButton.getLayoutParams();
        final float fabWidthWithLeftRightMargin = mStartNewConversationButton.getWidth()
                + lp.leftMargin + lp.rightMargin;
        final int direction = AccessibilityUtil.isLayoutRtl(mStartNewConversationButton) ? -1 : 1;
        return getNormalizedFabAnimator().translationX(direction * fabWidthWithLeftRightMargin);
    }

    public ViewPropertyAnimator showFab() {
        return getNormalizedFabAnimator().translationX(0).withEndAction(() -> {
            // Re-enable clicks after the animation.
            mStartNewConversationButton.setEnabled(true);
        });
    }

    public View getHeroElementForTransition() {
        return mArchiveMode ? null : mStartNewConversationButton;
    }

    @VisibleForAnimation
    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public void startFullScreenPhotoViewer(
            final Uri initialPhoto, final Rect initialPhotoBounds, final Uri photosUri) {
        UIIntents.get().launchFullScreenPhotoViewer(
                getActivity(), initialPhoto, initialPhotoBounds, photosUri);
    }

    @Override
    public void startFullScreenVideoViewer(final Uri videoUri) {
        UIIntents.get().launchFullScreenVideoViewer(getActivity(), videoUri);
    }

    @Override
    public boolean isSelectionMode() {
        return mHost != null && mHost.isSelectionMode();
    }

    @Override
    public boolean isArchived() {
        return mArchiveMode;
    }

}
