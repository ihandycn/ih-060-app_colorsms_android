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

package com.android.messaging.ui.conversation;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ad.AdPlacement;
import com.android.messaging.ad.BillingManager;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.ui.BugleActionBarActivity;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.conversation.ConversationFragment.ConversationFragmentHost;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.ToolbarDrawables;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.ui.messagebox.MessageBoxActivity;
import com.android.messaging.ui.view.MessagesTextView;
import com.android.messaging.ui.wallpaper.WallpaperManager;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleApplicationPrefs;
import com.android.messaging.util.BugleFirebaseAnalytics;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.CommonUtils;
import com.android.messaging.util.ContentType;
import com.android.messaging.util.FabricUtils;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.UiUtils;
import com.android.messaging.util.ViewUtils;
import com.crashlytics.android.core.CrashlyticsCore;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.superapps.debug.CrashlyticsLog;
import com.superapps.util.Dimensions;
import com.superapps.util.IntegerBuckets;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;
import net.appcloudbox.autopilot.AutopilotEvent;

import java.util.List;

public class ConversationActivity extends BugleActionBarActivity
        implements ConversationFragmentHost, ViewTreeObserver.OnGlobalLayoutListener {

    private final static String TAG = ConversationActivity.class.getName();
    public static final int FINISH_RESULT_CODE = 1;
    public static final int DELETE_CONVERSATION_RESULT_CODE = 2;
    private static final String SAVED_INSTANCE_STATE_UI_STATE_KEY = "uistate";

    public static final String PREF_KEY_FIRST_IN_CONVERSATION_PAGE = "pref_key_first_in_conversation_page";
    private static final String PREF_KEY_CONVERSATION_ACTIVITY_SHOW_TIME = "pref_key_conversation_activity_show_time";
    private static final String PREF_KEY_WIRE_AD_SHOW_TIME = "pref_key_wire_ad_show_time";
    public static final String PREF_KEY_WIRE_AD_SHOW_TIME_FOR_EXIT_WIRE_AD = "pref_key_wire_ad_show_time_for_exit_wire_ad";

    // Fragment transactions cannot be performed after onSaveInstanceState() has been called since
    // it will cause state loss. We don't want to call commitAllowingStateLoss() since it's
    // dangerous. Therefore, we note when instance state is saved and avoid performing UI state
    // updates concerning fragments past that point.
    private boolean mInstanceStateSaved;

    // Tracks whether onPause is called.
    private boolean mIsPaused;
    private TextView mTitleTextView;
    private ViewGroup mContainer;

    private int mKeyboardHeight;

    private AcbInterstitialAd mInterstitialAd;
    private long mCreateTime;
    private boolean fromCreateConversation;
    private String mConversationId;
    private boolean mNeedShowGuide = false;

    private BuglePrefs bugleApplicationPrefs = BugleApplicationPrefs.getApplicationPrefs();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // preload emoji pref file
        Preferences.get(EmojiManager.PREF_FILE_NAME);

        setContentView(R.layout.conversation_activity);

        final Intent intent = getIntent();
        fromCreateConversation = intent.getBooleanExtra(UIIntents.UI_INTENT_EXTRA_FROM_CREATE_CONVERSATION, false);

        if (fromCreateConversation) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        if (intent.getBooleanExtra(BugleNotifications.EXTRA_FROM_NOTIFICATION, false)) {
            BugleAnalytics.logEvent("SMS_Notifications_Clicked", true);
            BugleFirebaseAnalytics.logEvent("SMS_Notifications_Clicked");
            AutopilotEvent.logTopicEvent("topic-768lyi3sp", "notification_clicked");
        }

        if (intent.getBooleanExtra(UIIntents.UI_INTENT_EXTRA_GOTO_CONVERSATION_LIST, false)) {
            // See the comment in BugleWidgetService.getViewMoreConversationsView() why this
            // is unfortunately necessary. The Bugle desktop widget can display a list of
            // conversations. When there are more conversations that can be displayed in
            // the widget, the last item is a "More conversations" item. The way widgets
            // are built, the list items can only go to a single fill-in intent which points
            // to this ConversationActivity. When the user taps on "More conversations", we
            // really want to go to the ConversationList. This code makes that possible.
            finish();
            final Intent convListIntent = new Intent(this, ConversationListActivity.class);
            convListIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(convListIntent);
            return;
        }

        mConversationId = intent.getStringExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID);
        if (TextUtils.isEmpty(mConversationId)) {
            if (FabricUtils.isFabricInited()) {
                CrashlyticsCore.getInstance().logException(
                        new CrashlyticsLog("start conversation activity error : conversation id is null"));
            }
            finish();
            return;
        }
        mInstanceStateSaved = false;

        initActionBar();

        ViewUtils.setMargins(findViewById(R.id.conversation_fragment_container),
                0, -Dimensions.getStatusBarHeight(HSApplication.getContext()), 0, 0);

        if (Preferences.getDefault().getBoolean(PREF_KEY_FIRST_IN_CONVERSATION_PAGE, true))
        {
            Preferences.getDefault().putBoolean(PREF_KEY_FIRST_IN_CONVERSATION_PAGE, false);
            mNeedShowGuide = true;
        }
        initConversationFragment();

        // See if we're getting called from a widget to directly display an image or video
        final String extraToDisplay =
                intent.getStringExtra(UIIntents.UI_INTENT_EXTRA_ATTACHMENT_URI);
        if (!TextUtils.isEmpty(extraToDisplay)) {
            final String contentType =
                    intent.getStringExtra(UIIntents.UI_INTENT_EXTRA_ATTACHMENT_TYPE);
            final Rect bounds = UiUtils.getMeasuredBoundsOnScreen(mContainer);
            if (ContentType.isImageType(contentType)) {
                final Uri imagesUri = MessagingContentProvider.buildConversationImagesUri(mConversationId);
                UIIntents.get().launchFullScreenPhotoViewer(
                        this, Uri.parse(extraToDisplay), bounds, imagesUri);
            } else if (ContentType.isVideoType(contentType)) {
                UIIntents.get().launchFullScreenVideoViewer(this, Uri.parse(extraToDisplay));
            }
        }

        BugleAnalytics.logEvent("SMS_ActiveUsers", true);

        mKeyboardHeight = UiUtils.getKeyboardHeight();
        if (mKeyboardHeight <= 0) {
            mContainer = findViewById(R.id.conversation_and_compose_container);
            mContainer.getViewTreeObserver().addOnGlobalLayoutListener(this);
        }

        long lastShowTime = bugleApplicationPrefs.getLong(PREF_KEY_CONVERSATION_ACTIVITY_SHOW_TIME, -1);
        if (lastShowTime != -1) {
            IntegerBuckets buckets = new IntegerBuckets(5, 10, 30, 60, 300, 600, 1800, 3600, 7200);
            BugleAnalytics.logEvent("Detailspage_Show_Interval", "interval",
                    buckets.getBucket((int) ((System.currentTimeMillis() - lastShowTime) / 1000)));
            BugleFirebaseAnalytics.logEvent("Detailspage_Show_Interval", "interval",
                    buckets.getBucket((int) ((System.currentTimeMillis() - lastShowTime) / 1000)));
        }
        bugleApplicationPrefs.putLong(PREF_KEY_CONVERSATION_ACTIVITY_SHOW_TIME, System.currentTimeMillis());
        mCreateTime = System.currentTimeMillis();

        if (mNeedShowGuide) {
            Threads.postOnMainThreadDelayed(new Runnable() {
                @Override
                public void run() {
                    showSettingGuide();
                }
            }, 600);
        }
    }

    private void showSettingGuide() {
        BugleAnalytics.logEvent("SMS_Detailspage_Guide_Show", true);
        ViewGroup root = this.findViewById(android.R.id.content);
        FrameLayout container = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.conversation_setting_guide_view, root, false);
        MessagesTextView tv = container.findViewById(R.id.view);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) tv.getLayoutParams();
        lp.topMargin = Dimensions.pxFromDp(33.3f) + Dimensions.getStatusBarHeight(this);
        lp.rightMargin = Dimensions.pxFromDp(30.3f);
        tv.setLayoutParams(lp);

        // start animation
        AnimationSet animationSet = new AnimationSet(false);
        Animation alpha = new AlphaAnimation(0, 100);
        alpha.setDuration(80);
        Animation scale = new ScaleAnimation(0.6f, 1.03f, 0.6f, 1.03f, Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0);
        scale.setDuration(160);
        scale.setInterpolator(PathInterpolatorCompat.create(0.32f, 0.66f, 0.6f, 1f));
        Animation scale2 = new ScaleAnimation(1f, 1 / 1.03f, 1f, 1 / 1.03f, Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0);
        scale2.setDuration(70);
        scale2.setStartOffset(160);
        animationSet.addAnimation(scale);
        animationSet.addAnimation(scale2);
        animationSet.addAnimation(alpha);
        tv.startAnimation(animationSet);

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // end animation
                AnimationSet animationSet = new AnimationSet(false);
                Animation alpha = new AlphaAnimation(100, 0);
                alpha.setDuration(80);
                alpha.setStartOffset(120);
                Animation scale = new ScaleAnimation(1f, 1.06f, 1f, 1.06f, Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0);
                scale.setDuration(100);
                scale.setInterpolator(PathInterpolatorCompat.create(0.32f, 0.66f, 0.6f, 1f));
                Animation scale2 = new ScaleAnimation(1f, 0.6f / 1.06f, 1f, 0.6f / 1.06f, Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0);
                scale2.setDuration(100);
                scale2.setStartOffset(100);
                animationSet.addAnimation(scale);
                animationSet.addAnimation(scale2);
                animationSet.addAnimation(alpha);
                tv.startAnimation(animationSet);

                animationSet.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (ConversationActivity.this.isDestroyed()) {
                            return;
                        }
                        root.removeView(container);
                        Threads.postOnMainThreadDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ConversationFragment fragment = getConversationFragment();
                                if (fragment != null) {
                                    fragment.unBLockAd();
                                } else {
                                    HSLog.e(TAG, "beginAdLoad failed: fragment is null");
                                }
                            }
                        }, 500);

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
            }
        });


        root.addView(container);


    }

    @Override
    public void onGlobalLayout() {
        if (mContainer != null && mKeyboardHeight == 0) {
            Rect r = new Rect();
            View view = getWindow().getDecorView();
            view.getWindowVisibleDisplayFrame(r);
            int heightDiff = mContainer.getHeight() - r.height() -
                    Dimensions.getStatusBarHeight(this);
            if (heightDiff > Dimensions.pxFromDp(120)) {
                mKeyboardHeight = heightDiff;
                UiUtils.updateKeyboardHeight(mKeyboardHeight);
            }
        }
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        // set custom title visibility gone, when start MultiSelectActionMode etc.
        mTitleTextView.setVisibility(View.GONE);
        findViewById(R.id.selection_mode_bg).setVisibility(View.VISIBLE);
        return super.startActionMode(callback);
    }

    @Override
    public void dismissActionMode() {
        super.dismissActionMode();
        mTitleTextView.setVisibility(View.VISIBLE);
    }

    private void refreshActionBarBg() {
        View accessoryContainer = findViewById(R.id.accessory_container);
        Drawable toolbarBg = ToolbarDrawables.getToolbarBg();
        if (toolbarBg != null
                && WallpaperManager.getWallpaperPathByConversationId(mConversationId) == null) {
            ImageView ivAccessoryBg = accessoryContainer.findViewById(R.id.accessory_bg);
            ivAccessoryBg.setVisibility(View.VISIBLE);
            ivAccessoryBg.setImageDrawable(toolbarBg);
        } else {
            accessoryContainer.setBackgroundColor(PrimaryColors.getPrimaryColor());
            accessoryContainer.findViewById(R.id.accessory_bg).setVisibility(View.GONE);
        }
    }

    private void initActionBar() {
        View accessoryContainer = findViewById(R.id.accessory_container);
        ViewGroup.LayoutParams layoutParams = accessoryContainer.getLayoutParams();
        layoutParams.height = Dimensions.getStatusBarHeight(ConversationActivity.this) + Dimensions.pxFromDp(56);
        accessoryContainer.setLayoutParams(layoutParams);

        View statusbarInset = findViewById(R.id.status_bar_inset);
        layoutParams = statusbarInset.getLayoutParams();
        layoutParams.height = Dimensions.getStatusBarHeight(ConversationActivity.this);
        statusbarInset.setLayoutParams(layoutParams);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTitleTextView = findViewById(R.id.toolbar_title);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        // After onSaveInstanceState() is called, future changes to mUiState won't update the UI
        // anymore, because fragment transactions are not allowed past this point.
        // For an activity recreation due to orientation change, the saved instance state keeps
        // using the in-memory copy of the UI state instead of writing it to parcel as an
        // optimization, so the UI state values may still change in response to, for example,
        // focus change from the framework, making mUiState and actual UI inconsistent.
        // Therefore, save an exact "snapshot" (clone) of the UI state object to make sure the
        // restored UI state ALWAYS matches the actual restored UI components.
        mInstanceStateSaved = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        final ConversationFragment conversationFragment = getConversationFragment();
        if (conversationFragment != null) {
            conversationFragment.onActivityStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshActionBarBg();
        // we need to reset the mInstanceStateSaved flag since we may have just been restored from
        // a previous onStop() instead of an onDestroy().
        mInstanceStateSaved = false;
        mIsPaused = false;
        HSGlobalNotificationCenter.sendNotification(MessageBoxActivity.NOTIFICATION_FINISH_MESSAGE_BOX);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsPaused = true;
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        final ConversationFragment conversationFragment = getConversationFragment();
        // When the screen is turned on, the last used activity gets resumed, but it gets
        // window focus only after the lock screen is unlocked.
        if (hasFocus && conversationFragment != null) {
            conversationFragment.setConversationFocus();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mContainer != null) {
            mContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }

    @Override
    public void updateActionBar(final ActionBar actionBar) {
        super.updateActionBar(actionBar);
        final ConversationFragment conversation = getConversationFragment();
        if (conversation != null) {
            conversation.updateActionBar(actionBar, mTitleTextView);
        }

        if (getActionMode() == null) {
            findViewById(R.id.selection_mode_bg).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            if (TextUtils.isEmpty(menuItem.getTitle())) {
                HSGlobalNotificationCenter.sendNotification(ConversationFragment.RESET_ITEM);
            }
            return true;
        }
        if (menuItem.getItemId() == android.R.id.home) {
            BugleAnalytics.logEvent("SMS_DetailsPage_IconBack_Click", true);
            onNavigationUpPressed();
            return true;
        }
        return false;
    }

    public void onNavigationUpPressed() {
        // Let the conversation fragment handle the navigation up press.
        final ConversationFragment conversationFragment = getConversationFragment();
        if (conversationFragment != null && conversationFragment.onNavigationUpPressed()) {
            return;
        }
        showInterstitialAd();
        onFinishCurrentConversation();
    }

    @Override
    public void onBackPressed() {
        // Let the conversation fragment handle the back press.
        final ConversationFragment conversationFragment = getConversationFragment();
        if (conversationFragment != null && conversationFragment.onBackPressed()) {
            return;
        }

        showInterstitialAd();
        if (conversationFragment != null) {
            BugleAnalytics.logEvent("Detailspage_Back", "type", "back");
            BugleFirebaseAnalytics.logEvent("Detailspage_Back", "type", "back");
        }
        super.onBackPressed();
    }

    private void showInterstitialAd() {
        if (BillingManager.isPremiumUser()) {
            return;
        }

        final ConversationFragment conversationFragment = getConversationFragment();
        if (conversationFragment != null) {
            IntegerBuckets integerBuckets = new IntegerBuckets(5, 10, 15, 20, 30, 60, 120, 180, 300);
            BugleAnalytics.logEvent("Detailspage_Show_Details",
                    "length", integerBuckets.getBucket((int) ((System.currentTimeMillis() - mCreateTime) / 1000)),
                    "sendmessage", String.valueOf(conversationFragment.hasSentMessages()));
            BugleFirebaseAnalytics.logEvent("Detailspage_Show_Details",
                    "length", integerBuckets.getBucket((int) ((System.currentTimeMillis() - mCreateTime) / 1000)),
                    "sendmessage", String.valueOf(conversationFragment.hasSentMessages()));
        }
        if (conversationFragment != null
                && HSConfig.optBoolean(false, "Application", "SMSAd", "SMSDetailspageFullAd", "Enabled")
                && System.currentTimeMillis() - bugleApplicationPrefs.getLong(PREF_KEY_WIRE_AD_SHOW_TIME, -1)
                > HSConfig.optInteger(5, "Application", "SMSAd", "SMSDetailspageFullAd", "MinInterval") * DateUtils.MINUTE_IN_MILLIS
                && System.currentTimeMillis() - CommonUtils.getAppInstallTimeMillis()
                > HSConfig.optInteger(2, "Application", "SMSAd", "SMSDetailspageFullAd", "ShowAfterInstall") * DateUtils.HOUR_IN_MILLIS) {
            List<AcbInterstitialAd> ads = AcbInterstitialAdManager.fetch(AdPlacement.AD_WIRE, 1);
            if (ads.size() > 0) {
                mInterstitialAd = ads.get(0);
                mInterstitialAd.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {
                    @Override
                    public void onAdDisplayed() {

                    }

                    @Override
                    public void onAdClicked() {
                        BugleAnalytics.logEvent("Detailspage_FullAd_Click", true);
                        BugleFirebaseAnalytics.logEvent("Detailspage_FullAd_Click");
                    }

                    @Override
                    public void onAdClosed() {
                        mInterstitialAd.release();
                    }

                    @Override
                    public void onAdDisplayFailed(AcbError acbError) {

                    }
                });
                mInterstitialAd.setSoundEnable(false);
                mInterstitialAd.show();
                BugleAnalytics.logEvent("Detailspage_FullAd_Show", true);
                BugleFirebaseAnalytics.logEvent("Detailspage_FullAd_Show");
                AutopilotEvent.logTopicEvent("topic-768lyi3sp", "fullad_show");
                bugleApplicationPrefs.putLong(PREF_KEY_WIRE_AD_SHOW_TIME, System.currentTimeMillis());
                bugleApplicationPrefs.putLong(PREF_KEY_WIRE_AD_SHOW_TIME_FOR_EXIT_WIRE_AD, System.currentTimeMillis());
            }
            BugleAnalytics.logEvent("Detailspage_FullAd_Should_Show", true);
            BugleFirebaseAnalytics.logEvent("Detailspage_FullAd_Should_Show");
            AutopilotEvent.logTopicEvent("topic-768lyi3sp", "fullad_chance");
        }
    }

    public ConversationFragment getConversationFragment() {
        return (ConversationFragment) getFragmentManager().findFragmentByTag(
                ConversationFragment.FRAGMENT_TAG);
    }

    @Override // From ConversationFragmentHost
    public void onStartComposeMessage() {
    }

    @Override // From ConversationFragmentHost
    public void onConversationMetadataUpdated() {
        invalidateActionBar();
    }

    @Override // From ConversationFragmentHost
    public void onConversationMessagesUpdated(final int numberOfMessages) {
    }

    @Override // From ConversationFragmentHost
    public void onConversationParticipantDataLoaded(final int numberOfParticipants) {
    }

    @Override // From ConversationFragmentHost
    public boolean isActiveAndFocused() {
        return !mIsPaused && hasWindowFocus();
    }

    @Override
    public boolean isFromCreateConversation() {
        return fromCreateConversation;
    }

    public String getConversationName() {
        return getIntent().getStringExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_NAME);
    }

    private void initConversationFragment() {
        if (mInstanceStateSaved || mIsPaused) {
            return;
        }
        final Intent intent = getIntent();
        final String conversationId = mConversationId;

        final FragmentManager fragmentManager = getFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        ConversationFragment conversationFragment = getConversationFragment();

        Assert.notNull(conversationId);
        if (conversationFragment == null) {
            conversationFragment = new ConversationFragment();

            if (mNeedShowGuide) {
                conversationFragment.blockAd();
            }

            fragmentTransaction.add(R.id.conversation_fragment_container,
                    conversationFragment, ConversationFragment.FRAGMENT_TAG);
            if (HSConfig.optBoolean(false, "Application", "SMSAd", "SMSDetailspageFullAd", "Enabled")
                    && System.currentTimeMillis() - bugleApplicationPrefs.getLong(PREF_KEY_WIRE_AD_SHOW_TIME, -1)
                    > HSConfig.optInteger(5, "Application", "SMSAd", "SMSDetailspageFullAd", "MinInterval") * DateUtils.MINUTE_IN_MILLIS
                    && System.currentTimeMillis() - CommonUtils.getAppInstallTimeMillis()
                    > HSConfig.optInteger(2, "Application", "SMSAd", "SMSDetailspageFullAd", "ShowAfterInstall") * DateUtils.HOUR_IN_MILLIS) {
                AcbInterstitialAdManager.preload(1, AdPlacement.AD_WIRE);
            }

            // todo @zhe.li.1 重构新建短信的代码，这块暂时隐藏键盘
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            View view = getCurrentFocus();
            if (view == null) {
                view = new View(this);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        final MessageData draftData = intent.getParcelableExtra(
                UIIntents.UI_INTENT_EXTRA_DRAFT_DATA);
//      if (!needContactPickerFragment) {
        // Once the user has committed the audience,remove the draft data from the
        // intent to prevent reuse
        intent.removeExtra(UIIntents.UI_INTENT_EXTRA_DRAFT_DATA);
//      }
        conversationFragment.setHost(this);
        conversationFragment.setConversationInfo(this, conversationId, draftData);

        fragmentTransaction.commit();
    }

    @Override
    public void onFinishCurrentConversation() {
        // Simply finish the current activity. The current design is to leave any empty
        // conversations as is.
        if (OsUtil.isAtLeastL()) {
            finishAfterTransition();
        } else {
            finish();
        }
        BugleAnalytics.logEvent("Detailspage_Back", "type", "back_icon");
        BugleFirebaseAnalytics.logEvent("Detailspage_Back", "type", "back_icon");
    }

    @Override
    public boolean shouldResumeComposeMessage() {
        return false;
    }

    public boolean shouldShowContactPickerFragment() {
        return false;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        if (requestCode == ConversationFragment.REQUEST_CHOOSE_ATTACHMENTS &&
                resultCode == RESULT_OK) {
            final ConversationFragment conversationFragment = getConversationFragment();
            if (conversationFragment != null) {
                conversationFragment.onAttachmentChoosen();
            } else {
                LogUtil.e(LogUtil.BUGLE_TAG, "ConversationFragment is missing after launching " +
                        "AttachmentChooserActivity!");
            }
        } else if (resultCode == FINISH_RESULT_CODE) {
            finish();
        } else if (resultCode == DELETE_CONVERSATION_RESULT_CODE) {
            final ConversationFragment conversationFragment = getConversationFragment();
            conversationFragment.deleteConversation();
            finish();
        }
    }
}
