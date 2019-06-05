package com.android.messaging.ui.contact;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.android.messaging.R;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.ui.BugleActionBarActivity;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.messagebox.MessageBoxActivity;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.FabricUtils;
import com.android.messaging.util.UiUtils;
import com.android.messaging.util.ViewUtils;
import com.crashlytics.android.core.CrashlyticsCore;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.debug.CrashlyticsLog;
import com.superapps.util.Dimensions;
import com.superapps.util.IntegerBuckets;
import com.superapps.util.Preferences;

public class ContactPickerActivity extends BugleActionBarActivity implements
        ContactPickerFragment.ContactPickerFragmentHost,
        ViewTreeObserver.OnGlobalLayoutListener {

    // Fragment transactions cannot be performed after onSaveInstanceState() has been called since
    // it will cause state loss. We don't want to call commitAllowingStateLoss() since it's
    // dangerous. Therefore, we note when instance state is saved and avoid performing UI state
    // updates concerning fragments past that point.
    private boolean mInstanceStateSaved;

    // Tracks whether onPause is called.
    private boolean mIsPaused;

    private ViewGroup mContainer;

    private static final String PREF_KEY_CONVERSATION_ACTIVITY_SHOW_TIME = "pref_key_conversation_activity_show_time";

    private int mStatusBarHeight;
    private int mKeyboardHeight;
    private int mNavigationBarHeight;
    private MessageData mDraftData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_picker_activty);

        final Intent intent = getIntent();

        if (getIntent() != null && getIntent().getBooleanExtra(BugleNotifications.EXTRA_FROM_NOTIFICATION, false)) {
            BugleAnalytics.logEvent("SMS_Notifications_Clicked", true, true);
        }
        if (getIntent() != null) {
            mDraftData = getIntent().getParcelableExtra(UIIntents.UI_INTENT_EXTRA_DRAFT_DATA);
        }

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

        mInstanceStateSaved = false;


        ViewUtils.setMargins(findViewById(R.id.conversation_fragment_container),
                0, -Dimensions.getStatusBarHeight(HSApplication.getContext()), 0, 0);

        // Don't animate UI state change for initial setup.
        updateUiState(false /* animate */);

        mContainer = findViewById(R.id.conversation_and_compose_container);
        mContainer.getViewTreeObserver().addOnGlobalLayoutListener(this);


        BugleAnalytics.logEvent("SMS_ActiveUsers", true);

        mStatusBarHeight = Dimensions.getStatusBarHeight(this);
        mNavigationBarHeight = Dimensions.getNavigationBarHeight(this);
        mKeyboardHeight = UiUtils.getKeyboardHeight();

        long lastShowTime = Preferences.getDefault().getLong(PREF_KEY_CONVERSATION_ACTIVITY_SHOW_TIME, -1);
        if (lastShowTime != -1) {
            IntegerBuckets buckets = new IntegerBuckets(5, 10, 30, 60, 300, 600, 1800, 3600, 7200);
            BugleAnalytics.logEvent("Detailspage_Show_Interval", false, true, "interval",
                    buckets.getBucket((int) ((System.currentTimeMillis() - lastShowTime) / 1000)));
        }
        Preferences.getDefault().putLong(PREF_KEY_CONVERSATION_ACTIVITY_SHOW_TIME, System.currentTimeMillis());
    }


    private void updateUiState(final boolean animate) {
        if (mInstanceStateSaved || mIsPaused) {
            return;
        }
        final FragmentManager fragmentManager = getFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


        // Set up the contact picker fragment.
        ContactPickerFragment contactPickerFragment = getContactPicker();
        if (contactPickerFragment == null) {
            contactPickerFragment = new ContactPickerFragment();
            fragmentTransaction.add(R.id.contact_picker_fragment_container,
                    contactPickerFragment, ContactPickerFragment.FRAGMENT_TAG);
        }
        contactPickerFragment.setHost(this);
        contactPickerFragment.setContactPickingMode(ContactPickerFragment.MODE_PICK_INITIAL_CONTACT,
                animate);

        fragmentTransaction.commit();
        invalidateActionBar();
    }


    @Override
    public void updateActionBar(final ActionBar actionBar) {
        super.updateActionBar(actionBar);
        final ContactPickerFragment contactPicker = getContactPicker();
        if (contactPicker != null) {
            contactPicker.updateActionBar(actionBar);
        }
    }

    @Override // From ContactPickerFragmentHost
    public void onGetOrCreateNewConversation(final String conversationId) {
        Assert.isTrue(conversationId != null);

        if (TextUtils.isEmpty(conversationId)) {
            if (FabricUtils.isFabricInited()) {
                CrashlyticsCore.getInstance().logException(
                        new CrashlyticsLog("start conversation activity error : create conversation id is null"));
            }
            finish();
        } else {
            UIIntents.get().launchConversationActivity(
                    this, conversationId, mDraftData,
                    null, false, "", true);
            finish();
        }
    }

    @Override // From ContactPickerFragmentHost
    public void onBackButtonPressed() {
        onBackPressed();
    }

    @Override // From ContactPickerFragmentHost
    public void onInitiateAddMoreParticipants() {
    }


    @Override // From ContactPickerFragmentHost
    public void onParticipantCountChanged(final boolean canAddMoreParticipants) {
    }

    private ContactPickerFragment getContactPicker() {
        return (ContactPickerFragment) getFragmentManager().findFragmentByTag(
                ContactPickerFragment.FRAGMENT_TAG);
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
    public void onDisplayHeightChanged(final int heightSpecification) {
        super.onDisplayHeightChanged(heightSpecification);
        invalidateActionBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
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
}
