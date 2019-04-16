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
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewGroupCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewPropertyAnimator;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ad.AdPlacement;
import com.android.messaging.annotation.VisibleForAnimation;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.binding.Binding;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.data.AdItemData;
import com.android.messaging.datamodel.data.ConversationListData;
import com.android.messaging.datamodel.data.ConversationListData.ConversationListDataListener;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.ui.BugleAnimationTags;
import com.android.messaging.ui.ListEmptyView;
import com.android.messaging.ui.SnackBarInteraction;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.WallpaperDrawables;
import com.android.messaging.util.AccessibilityUtil;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.HierarchyTreeChangeListener;
import com.android.messaging.util.ImeUtil;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.UiUtils;
import com.google.common.annotations.VisibleForTesting;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.IntegerBuckets;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import net.appcloudbox.ads.base.ContainerView.AcbContentLayout;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.expressad.AcbExpressAdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows a list of conversations.
 */
public class ConversationListFragment extends Fragment implements ConversationListDataListener,
        ConversationListItemView.HostInterface {
    private static final String BUNDLE_ARCHIVED_MODE = "archived_mode";
    private static final String BUNDLE_FORWARD_MESSAGE_MODE = "forward_message_mode";
    private static final boolean VERBOSE = false;

    private boolean mArchiveMode;
    private boolean mBlockedAvailable;
    private boolean mForwardMessageMode;
    private ViewGroup adContainer;
    private AcbExpressAdView expressAdView;
    private LinearLayoutManager manager;
    private boolean switchAd;
    private boolean adFirstPrepared = true;
    private boolean conversationFirstUpdated = true;

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
    private RecyclerView mRecyclerView;
    private ImageView mStartNewConversationButton;
    private ListEmptyView mEmptyListMessageView;
    private ConversationListAdapter mAdapter;

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
        if (mRecyclerView != null) {
            if (mRecyclerView.canScrollVertically(-1)) {
                BugleAnalytics.logEvent("SMS_Messages_Show_NotOnTop", true);
            } else {
                if (HSConfig.optBoolean(true, "Application", "SMSAd", "SMSHomepageBannerAd")) {
                    BugleAnalytics.logEvent("SMS_Messages_BannerAd_Should_Show", true);
                    switchAd = false;
                    if (expressAdView != null) {
                        expressAdView.switchAd();
                    }
                }
            }
        }
        Assert.notNull(mHost);
        setScrolledToNewestConversationIfNeeded();
        updateUi();
        if (!adFirstPrepared) {
            prepareAd();
        }
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
        if (expressAdView != null) {
            expressAdView.destroy();
            expressAdView = null;
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
        ImageView conversationListBg = rootView.findViewById(R.id.conversation_list_bg);
        conversationListBg.setImageDrawable(WallpaperDrawables.getWallpaperBg());
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
                    if (expressAdView != null && switchAd) {
                        expressAdView.switchAd();
                        BugleAnalytics.logEvent("SMS_Messages_BannerAd_Should_Show", true);
                        switchAd = false;
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

        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(SAVED_INSTANCE_STATE_LIST_VIEW_STATE_KEY);
        }

        mStartNewConversationButton = (ImageView) rootView.findViewById(
                R.id.start_new_conversation_button);
        mStartNewConversationButton.setBackgroundDrawable(BackgroundDrawables.
                createBackgroundDrawable(PrimaryColors.getEditButtonColor(),
                        Dimensions.pxFromDp(28),
                        true));
        if (mArchiveMode) {
            mStartNewConversationButton.setVisibility(View.GONE);
        } else {
            mStartNewConversationButton.setVisibility(View.VISIBLE);
            mStartNewConversationButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View clickView) {
                    ConversationListActivity.logFirstComeInClickEvent("create");
                    BugleAnalytics.logEvent("SMS_CreateMessage_ButtonClick", true);
                    mHost.onCreateConversationClick();
                }
            });
        }
        ViewCompat.setTransitionName(mStartNewConversationButton, BugleAnimationTags.TAG_FABICON);

        // The root view has a non-null background, which by default is deemed by the framework
        // to be a "transition group," where all child views are animated together during an
        // activity transition. However, we want each individual items in the recycler view to
        // show explode animation themselves, so we explicitly tag the root view to be a non-group.
        ViewGroupCompat.setTransitionGroup(rootView, false);

        setHasOptionsMenu(true);
        if (HSConfig.optBoolean(true, "Application", "SMSAd", "SMSHomepageBannerAd")) {
            initAd();
        }
        return rootView;
    }


    private void initAd() {
        adContainer = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.conversation_list_header, mRecyclerView, false);
        expressAdView = new AcbExpressAdView(HSApplication.getContext(), AdPlacement.AD_BANNER);
        expressAdView.setCustomLayout(new AcbContentLayout(R.layout.custom_banner)
                .setActionId(R.id.banner_action)
                .setIconId(R.id.banner_icon_image)
                .setTitleId(R.id.banner_title)
                .setDescriptionId(R.id.banner_des)
        );
        expressAdView.setAutoSwitchAd(AcbExpressAdView.AutoSwitchAd_None);
        expressAdView.setOnHierarchyChangeListener(HierarchyTreeChangeListener.wrap(new ViewGroup.OnHierarchyChangeListener() {
            @Override public void onChildViewAdded(View parent, View child) {
                try {
                    if (child instanceof RelativeLayout
                            && ((RelativeLayout) child).getChildCount() == 1
                            && ((RelativeLayout) child).getChildAt(0) instanceof AcbNativeAdContainerView) {
                        AcbNativeAdContainerView nativeAdContainerView = (AcbNativeAdContainerView) ((RelativeLayout) child).getChildAt(0);
                        nativeAdContainerView.getChildAt(1).setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                }

                Threads.postOnMainThread(() -> {
                    TextView title = expressAdView.findViewById(R.id.banner_title);
                    title.setTextColor(ConversationColors.get().getListTitleColor());
                    TextView subtitle = expressAdView.findViewById(R.id.banner_des);
                    subtitle.setTextColor(ConversationColors.get().getListSubtitleColor());
                });
            }

            @Override public void onChildViewRemoved(View parent, View child) {

            }
        }));
        expressAdView.setExpressAdViewListener(new AcbExpressAdView.AcbExpressAdViewListener() {
            @Override
            public void onAdShown(AcbExpressAdView acbExpressAdView) {
                BugleAnalytics.logEvent("SMS_Messages_BannerAd_Show", true);
            }

            @Override
            public void onAdClicked(AcbExpressAdView acbExpressAdView) {
                BugleAnalytics.logEvent("SMS_Messages_BannerAd_Click", true);
            }
        });
        adContainer.addView(expressAdView);

    }

    private void prepareAd() {
        if (expressAdView == null)
            return;
        expressAdView.prepareAd(new AcbExpressAdView.PrepareAdListener() {
            @Override
            public void onAdReady(AcbExpressAdView acbExpressAdView) {
                if (!mAdapter.hasHeader()) {
                    mAdapter.setHeader(adContainer);
                    if (manager.findFirstCompletelyVisibleItemPosition() == 0) {
                        mRecyclerView.scrollToPosition(0);
                    }
                }
            }

            @Override
            public void onPrepareAdFailed(AcbExpressAdView acbExpressAdView, AcbError acbError) {

            }
        });
        adFirstPrepared = false;
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
        switchAd = true;
        mListState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        mListBinding.getData().setScrolledToNewestConversation(false);
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
                dataList.add(itemData);
            } while (cursor.moveToNext());
        }

        if (conversationFirstUpdated) {
            conversationFirstUpdated = false;
            boolean hasPinConversation = false;
            if (!dataList.isEmpty()) {
                for (Object object : dataList) {
                    if (object instanceof ConversationListItemData) {
                        ConversationListItemData itemData = (ConversationListItemData) object;
                        if (itemData.isPinned()) {
                            hasPinConversation = true;
                            break;
                        }
                    }
                }
            }
            HSBundle hsBundle = new HSBundle();
            hsBundle.putBoolean(ConversationListActivity.HAS_PIN_CONVERSATION, hasPinConversation);
            HSGlobalNotificationCenter.sendNotification(ConversationListActivity.FIRST_LOAD, hsBundle);
        }

        if (mAdapter.hasHeader()) {
            dataList.add(0, new AdItemData());
        }
        mAdapter.setDataList(dataList);
        if (adFirstPrepared && !dataList.isEmpty()) {
            prepareAd();
        }
        updateEmptyListUi(cursor == null || cursor.getCount() == 0);
        if (cursor != null && cursor.getCount() > 0) {
            Preferences.getDefault().doOnce(new Runnable() {
                @Override
                public void run() {
                    IntegerBuckets buckets = new IntegerBuckets(10, 20, 30, 40, 50);
                    BugleAnalytics.logEvent("SMS_FirstLoading_Success", true, "Number", buckets.getBucket(cursor.getCount()));
                }
            }, "pref_key_first_loading_conversation_count");
        }
    }

    @Override
    public void setBlockedParticipantsAvailable(final boolean blockedAvailable) {
        mBlockedAvailable = blockedAvailable;
    }

    public void updateUi() {
        mAdapter.notifyDataSetChanged();

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
            HSGlobalNotificationCenter.sendNotification(ConversationListActivity.SHOW_EMOJI);
        }
    }

    @Override
    public List<SnackBarInteraction> getSnackBarInteractions() {
        final List<SnackBarInteraction> interactions = new ArrayList<SnackBarInteraction>(1);
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
        return getNormalizedFabAnimator().translationX(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                // Re-enable clicks after the animation.
                mStartNewConversationButton.setEnabled(true);
            }
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
}
