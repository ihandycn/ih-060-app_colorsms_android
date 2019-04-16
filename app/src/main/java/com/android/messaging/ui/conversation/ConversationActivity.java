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

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.ActionMode;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ad.AdPlacement;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.ui.BugleActionBarActivity;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.contact.ContactPickerFragment;
import com.android.messaging.ui.contact.ContactPickerFragment.ContactPickerFragmentHost;
import com.android.messaging.ui.conversation.ConversationActivityUiState.ConversationActivityUiStateHost;
import com.android.messaging.ui.conversation.ConversationFragment.ConversationFragmentHost;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.ToolbarDrawables;
import com.android.messaging.ui.messagebox.MessageBoxActivity;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.CommonUtils;
import com.android.messaging.util.ContentType;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.UiUtils;
import com.android.messaging.util.ViewUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.util.Dimensions;
import com.superapps.util.IntegerBuckets;
import com.superapps.util.Preferences;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;

import java.util.List;

public class ConversationActivity extends BugleActionBarActivity
        implements ContactPickerFragmentHost, ConversationFragmentHost,
        ConversationActivityUiStateHost, ViewTreeObserver.OnGlobalLayoutListener {
    public static final int FINISH_RESULT_CODE = 1;
    public static final int DELETE_CONVERSATION_RESULT_CODE = 2;
    private static final String SAVED_INSTANCE_STATE_UI_STATE_KEY = "uistate";

    private static final String PREF_KEY_CONVERSATION_ACTIVITY_SHOW_TIME = "pref_key_conversation_activity_show_time";
    private static final String PREF_KEY_WIRE_AD_SHOW_TIME = "pref_key_wire_ad_show_time";

    private ConversationActivityUiState mUiState;

    // Fragment transactions cannot be performed after onSaveInstanceState() has been called since
    // it will cause state loss. We don't want to call commitAllowingStateLoss() since it's
    // dangerous. Therefore, we note when instance state is saved and avoid performing UI state
    // updates concerning fragments past that point.
    private boolean mInstanceStateSaved;

    // Tracks whether onPause is called.
    private boolean mIsPaused;
    private TextView mTitleTextView;
    private ViewGroup mContainer;

    private int mStatusBarHeight;
    private int mKeyboardHeight;
    private int mNavigationBarHeight;

    private AcbInterstitialAd mInterstitialAd;
    private long mCreateTime;
    private Toolbar toolbar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.conversation_activity);

        final Intent intent = getIntent();

        if (getIntent() != null && getIntent().getBooleanExtra(BugleNotifications.EXTRA_FROM_NOTIFICATION, false)) {
            BugleAnalytics.logEvent("SMS_Notifications_Clicked", true, true);
        }

        // Do our best to restore UI state from saved instance state.
        if (savedInstanceState != null) {
            mUiState = savedInstanceState.getParcelable(SAVED_INSTANCE_STATE_UI_STATE_KEY);
        } else {
            if (intent.
                    getBooleanExtra(UIIntents.UI_INTENT_EXTRA_GOTO_CONVERSATION_LIST, false)) {
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
        }

        // If saved instance state doesn't offer a clue, get the info from the intent.
        if (mUiState == null) {
            final String conversationId = intent.getStringExtra(
                    UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID);
            mUiState = new ConversationActivityUiState(conversationId);
        }
        mUiState.setHost(this);
        mInstanceStateSaved = false;

        initActionBar();

        ViewUtils.setMargins(findViewById(R.id.conversation_fragment_container),
                0, -Dimensions.getStatusBarHeight(HSApplication.getContext()), 0, 0);

        // Don't animate UI state change for initial setup.
        updateUiState(false /* animate */);

        mContainer = findViewById(R.id.conversation_and_compose_container);
        mContainer.getViewTreeObserver().addOnGlobalLayoutListener(this);

        // See if we're getting called from a widget to directly display an image or video
        final String extraToDisplay =
                intent.getStringExtra(UIIntents.UI_INTENT_EXTRA_ATTACHMENT_URI);
        if (!TextUtils.isEmpty(extraToDisplay)) {
            final String contentType =
                    intent.getStringExtra(UIIntents.UI_INTENT_EXTRA_ATTACHMENT_TYPE);
            final Rect bounds = UiUtils.getMeasuredBoundsOnScreen(mContainer);
            if (ContentType.isImageType(contentType)) {
                final Uri imagesUri = MessagingContentProvider.buildConversationImagesUri(
                        mUiState.getConversationId());
                UIIntents.get().launchFullScreenPhotoViewer(
                        this, Uri.parse(extraToDisplay), bounds, imagesUri);
            } else if (ContentType.isVideoType(contentType)) {
                UIIntents.get().launchFullScreenVideoViewer(this, Uri.parse(extraToDisplay));
            }
        }

        BugleAnalytics.logEvent("SMS_ActiveUsers", true);

        mStatusBarHeight = Dimensions.getStatusBarHeight(this);
        mNavigationBarHeight = Dimensions.getNavigationBarHeight(this);
        mKeyboardHeight = UiUtils.getKeyboardHeight();

        long lastShowTime = Preferences.getDefault().getLong(PREF_KEY_CONVERSATION_ACTIVITY_SHOW_TIME, -1);
        if (lastShowTime != -1) {
            IntegerBuckets buckets = new IntegerBuckets(5, 10, 30, 60, 300, 600, 1800, 3600, 7200);
            BugleAnalytics.logEvent("Detailspage_Show_Interval", "interval",
                    buckets.getBucket((int) ((System.currentTimeMillis() - lastShowTime) / 1000)));
        }
        Preferences.getDefault().putLong(PREF_KEY_CONVERSATION_ACTIVITY_SHOW_TIME, System.currentTimeMillis());
        mCreateTime = System.currentTimeMillis();
    }

    @Override
    public void onGlobalLayout() {
        Rect r = new Rect();
        mContainer.getWindowVisibleDisplayFrame(r);

        int screenHeight = mContainer.getRootView().getHeight();
        int heightDiff = screenHeight - (r.bottom - r.top);

        if (mKeyboardHeight == 0 && heightDiff > mStatusBarHeight + mNavigationBarHeight + Dimensions.pxFromDp(20)) {
            mKeyboardHeight = heightDiff - mStatusBarHeight - mNavigationBarHeight;
            UiUtils.updateKeyboardHeight(mKeyboardHeight);
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

    private void initActionBar() {
        View accessoryContainer = findViewById(R.id.accessory_container);
        ViewGroup.LayoutParams layoutParams = accessoryContainer.getLayoutParams();
        layoutParams.height = Dimensions.getStatusBarHeight(ConversationActivity.this) + Dimensions.pxFromDp(56);
        accessoryContainer.setLayoutParams(layoutParams);
        if (ToolbarDrawables.getToolbarBg() != null) {
            accessoryContainer.setBackground(ToolbarDrawables.getToolbarBg());
        } else {
            accessoryContainer.setBackgroundColor(PrimaryColors.getPrimaryColor());
        }

        View statusbarInset = findViewById(R.id.status_bar_inset);
        layoutParams = statusbarInset.getLayoutParams();
        layoutParams.height = Dimensions.getStatusBarHeight(ConversationActivity.this);
        statusbarInset.setLayoutParams(layoutParams);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTitleTextView = findViewById(R.id.toolbar_title);
        invalidateActionBar();
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
        outState.putParcelable(SAVED_INSTANCE_STATE_UI_STATE_KEY, mUiState.clone());
        mInstanceStateSaved = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

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
    public void onDisplayHeightChanged(final int heightSpecification) {
        super.onDisplayHeightChanged(heightSpecification);
        invalidateActionBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUiState != null) {
            mUiState.setHost(null);
        }
        mContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    @Override
    public void updateActionBar(final ActionBar actionBar) {
        super.updateActionBar(actionBar);
        final ConversationFragment conversation = getConversationFragment();
        final ContactPickerFragment contactPicker = getContactPicker();
        if (contactPicker != null && mUiState.shouldShowContactPickerFragment()) {
            contactPicker.updateActionBar(actionBar);
        } else if (conversation != null && mUiState.shouldShowConversationFragment()) {
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
            onNavigationUpPressed();
            return true;
        }
        BugleAnalytics.logEvent("SMS_DetailsPage_IconBack_Click", true);
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
        }
        super.onBackPressed();
    }

    private void showInterstitialAd() {
        final ConversationFragment conversationFragment = getConversationFragment();
        if (conversationFragment != null) {
            IntegerBuckets integerBuckets = new IntegerBuckets(5, 10, 15, 20, 30, 60, 120, 180, 300);
            BugleAnalytics.logEvent("Detailspage_Show_Details",
                    "length", integerBuckets.getBucket((int) ((System.currentTimeMillis() - mCreateTime) / 1000)),
                    "sendmessage", String.valueOf(conversationFragment.hasSentMessages()));
        }
        if (conversationFragment != null
                && HSConfig.optBoolean(false, "Application", "SMSAd", "SMSDetailspageFullAd", "Enabled")
                && System.currentTimeMillis() - Preferences.getDefault().getLong(PREF_KEY_WIRE_AD_SHOW_TIME, -1)
                > HSConfig.optInteger(5, "Application", "SMSAd", "SMSDetailspageFullAd", "MinInterval") * DateUtils.MINUTE_IN_MILLIS
                && System.currentTimeMillis() - CommonUtils.getAppInstallTimeMillis()
                > HSConfig.optInteger(2, "Application", "SMSAd", "SMSDetailspageFullAd", "ShowAfterInstall") * DateUtils.HOUR_IN_MILLIS) {
            List<AcbInterstitialAd> ads = AcbInterstitialAdManager.fetch(AdPlacement.AD_WIRE, 1);
            if (ads.size() > 0) {
                mInterstitialAd = ads.get(0);
                mInterstitialAd.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {
                    @Override public void onAdDisplayed() {

                    }

                    @Override public void onAdClicked() {
                        BugleAnalytics.logEvent("Detailspage_FullAd_Click");
                    }

                    @Override public void onAdClosed() {
                        mInterstitialAd.release();
                    }

                    @Override public void onAdDisplayFailed(AcbError acbError) {

                    }
                });
                mInterstitialAd.show();
                BugleAnalytics.logEvent("Detailspage_FullAd_Show", true);
                Preferences.getDefault().putLong(PREF_KEY_WIRE_AD_SHOW_TIME, System.currentTimeMillis());
            }
            BugleAnalytics.logEvent("Detailspage_FullAd_Should_Show", true);
        }
    }

    private ContactPickerFragment getContactPicker() {
        return (ContactPickerFragment) getFragmentManager().findFragmentByTag(
                ContactPickerFragment.FRAGMENT_TAG);
    }

    public ConversationFragment getConversationFragment() {
        return (ConversationFragment) getFragmentManager().findFragmentByTag(
                ConversationFragment.FRAGMENT_TAG);
    }

    @Override // From ContactPickerFragmentHost
    public void onGetOrCreateNewConversation(final String conversationId) {
        Assert.isTrue(conversationId != null);
        mUiState.onGetOrCreateConversation(conversationId);
    }

    @Override // From ContactPickerFragmentHost
    public void onBackButtonPressed() {
        onBackPressed();
    }

    @Override // From ContactPickerFragmentHost
    public void onInitiateAddMoreParticipants() {
        mUiState.onAddMoreParticipants();
    }


    @Override
    public void onParticipantCountChanged(final boolean canAddMoreParticipants) {
        mUiState.onParticipantCountUpdated(canAddMoreParticipants);
    }

    @Override // From ConversationFragmentHost
    public void onStartComposeMessage() {
        mUiState.onStartMessageCompose();
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

    @Override // From ConversationActivityUiStateListener
    public void onConversationContactPickerUiStateChanged(final int oldState, final int newState,
                                                          final boolean animate) {
        Assert.isTrue(oldState != newState);
        updateUiState(animate);
    }

    private void updateUiState(final boolean animate) {
        if (mInstanceStateSaved || mIsPaused) {
            return;
        }
        Assert.notNull(mUiState);
        final Intent intent = getIntent();
        final String conversationId = mUiState.getConversationId();

        final FragmentManager fragmentManager = getFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        final boolean needConversationFragment = mUiState.shouldShowConversationFragment();
        final boolean needContactPickerFragment = mUiState.shouldShowContactPickerFragment();
        ConversationFragment conversationFragment = getConversationFragment();

        // Set up the conversation fragment.
        if (needConversationFragment) {
            Assert.notNull(conversationId);
            if (conversationFragment == null) {
                conversationFragment = new ConversationFragment();
                fragmentTransaction.add(R.id.conversation_fragment_container,
                        conversationFragment, ConversationFragment.FRAGMENT_TAG);
                if (HSConfig.optBoolean(false, "Application", "SMSAd", "SMSDetailspageFullAd", "Enabled")
                        && System.currentTimeMillis() - Preferences.getDefault().getLong(PREF_KEY_WIRE_AD_SHOW_TIME, -1)
                        > HSConfig.optInteger(5, "Application", "SMSAd", "SMSDetailspageFullAd", "MinInterval") * DateUtils.MINUTE_IN_MILLIS
                        && System.currentTimeMillis() - CommonUtils.getAppInstallTimeMillis()
                        > HSConfig.optInteger(2, "Application", "SMSAd", "SMSDetailspageFullAd", "ShowAfterInstall") * DateUtils.HOUR_IN_MILLIS) {
                    AcbInterstitialAdManager.preload(1, AdPlacement.AD_WIRE);
                }
            }
            final MessageData draftData = intent.getParcelableExtra(
                    UIIntents.UI_INTENT_EXTRA_DRAFT_DATA);
            if (!needContactPickerFragment) {
                // Once the user has committed the audience,remove the draft data from the
                // intent to prevent reuse
                intent.removeExtra(UIIntents.UI_INTENT_EXTRA_DRAFT_DATA);
            }
            conversationFragment.setHost(this);
            conversationFragment.setConversationInfo(this, conversationId, draftData);
        } else if (conversationFragment != null) {
            // Don't save draft to DB when removing conversation fragment and switching to
            // contact picking mode.  The draft is intended for the new group.
            conversationFragment.suppressWriteDraft();
            fragmentTransaction.remove(conversationFragment);
        }

        // Set up the contact picker fragment.
        ContactPickerFragment contactPickerFragment = getContactPicker();
        if (needContactPickerFragment) {
            if (contactPickerFragment == null) {
                contactPickerFragment = new ContactPickerFragment();
                fragmentTransaction.add(R.id.contact_picker_fragment_container,
                        contactPickerFragment, ContactPickerFragment.FRAGMENT_TAG);
            }
            contactPickerFragment.setHost(this);
            contactPickerFragment.setContactPickingMode(mUiState.getDesiredContactPickingMode(),
                    animate);
        } else if (contactPickerFragment != null) {
            fragmentTransaction.remove(contactPickerFragment);
        }

        fragmentTransaction.commit();
        invalidateActionBar();
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
    }

    @Override
    public boolean shouldResumeComposeMessage() {
        return mUiState.shouldResumeComposeMessage();
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
