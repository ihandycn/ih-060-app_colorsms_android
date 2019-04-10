package com.android.messaging.privatebox.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.messaging.R;
import com.android.messaging.datamodel.action.DeleteConversationAction;
import com.android.messaging.datamodel.action.UpdateConversationOptionsAction;
import com.android.messaging.datamodel.action.UpdateDestinationBlockedAction;
import com.android.messaging.datamodel.data.ConversationListData;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.ui.BaseAlertDialog;
import com.android.messaging.ui.SnackBar;
import com.android.messaging.ui.SnackBarInteraction;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.contact.AddContactsConfirmationDialog;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.UiUtils;
import com.ihs.app.framework.HSApplication;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static com.android.messaging.util.DisplayUtils.getString;

public class PrivateMultiSelectActionModeCallback implements Callback {
    private HashSet<String> mBlockedSet;

    public static class SelectedConversation {
        public final String conversationId;
        public final long timestamp;
        public final String icon;
        public final String otherParticipantNormalizedDestination;
        public final CharSequence participantLookupKey;
        public final boolean isGroup;
        public final boolean isArchived;
        public final boolean notificationEnabled;
        public final boolean isPin;

        public SelectedConversation(ConversationListItemData data) {
            conversationId = data.getConversationId();
            timestamp = data.getTimestamp();
            icon = data.getIcon();
            otherParticipantNormalizedDestination = data.getOtherParticipantNormalizedDestination();
            participantLookupKey = data.getParticipantLookupKey();
            isGroup = data.getIsGroup();
            isArchived = data.getIsArchived();
            notificationEnabled = data.getNotificationEnabled();
            isPin = data.isPinned();
        }
    }

    private final ArrayMap<String, SelectedConversation> mSelectedConversations;

    private WeakReference<MultiSelectConversationListActivity> mWeakActivity;
    private MenuItem mAddContactMenuItem;
    private MenuItem mBlockMenuItem;
    private MenuItem mNotificationOnMenuItem;
    private MenuItem mNotificationOffMenuItem;
    private MenuItem mPinMenuItem;
    private MenuItem mCancelPinMenuItem;
    private boolean mHasInflated;

    public PrivateMultiSelectActionModeCallback(final MultiSelectConversationListActivity activity) {
        mWeakActivity = new WeakReference<>(activity);
        mSelectedConversations = new ArrayMap<>();
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        actionMode.getMenuInflater().inflate(R.menu.conversation_list_fragment_select_menu, menu);
        mAddContactMenuItem = menu.findItem(R.id.action_add_contact);
        mBlockMenuItem = menu.findItem(R.id.action_block);
        mNotificationOffMenuItem = menu.findItem(R.id.action_notification_off);
        mNotificationOnMenuItem = menu.findItem(R.id.action_notification_on);
        mPinMenuItem = menu.findItem(R.id.action_pin);
        mCancelPinMenuItem = menu.findItem(R.id.action_cancel_pin);
        mHasInflated = true;
        updateActionIconsVisibility();
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete:
                onActionBarDelete(mSelectedConversations.values());
                return true;
            case R.id.action_notification_off:
                onActionBarNotification(mSelectedConversations.values(), false);
                return true;
            case R.id.action_notification_on:
                onActionBarNotification(mSelectedConversations.values(), true);
                return true;
            case R.id.action_add_contact:
                Assert.isTrue(mSelectedConversations.size() == 1);
                onActionBarAddContact(mSelectedConversations.valueAt(0));
                return true;
            case R.id.action_block:
                onActionBarBlock(mSelectedConversations.values());
                return true;
            case android.R.id.home:
                mWeakActivity.get().exitMultiSelectState();
                return true;
            case R.id.action_pin:
                onPin(mSelectedConversations.values(), true);
                return true;
            case R.id.action_cancel_pin:
                onPin(mSelectedConversations.values(), false);
                return true;
            case R.id.action_menu:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        mSelectedConversations.clear();
        mHasInflated = false;
    }

    public void toggleSelect(final ConversationListData listData,
                             final ConversationListItemData conversationListItemData) {
        Assert.notNull(conversationListItemData);
        mBlockedSet = listData.getBlockedParticipants();
        final String id = conversationListItemData.getConversationId();
        if (mSelectedConversations.containsKey(id)) {
            mSelectedConversations.remove(id);
        } else {
            mSelectedConversations.put(id, new SelectedConversation(conversationListItemData));
        }

        if (mSelectedConversations.isEmpty()) {
            if (mWeakActivity.get() != null) {
                mWeakActivity.get().exitMultiSelectState();
            }
        } else {
            updateActionIconsVisibility();
        }
    }

    public boolean isSelected(final String selectedId) {
        return mSelectedConversations.containsKey(selectedId);
    }

    private void updateActionIconsVisibility() {
        if (!mHasInflated) {
            return;
        }

        if (mSelectedConversations.size() == 1) {
            final SelectedConversation conversation = mSelectedConversations.valueAt(0);
            // The look up key is a key given to us by contacts app, so if we have a look up key,
            // we know that the participant is already in contacts.
            final boolean isInContacts = !TextUtils.isEmpty(conversation.participantLookupKey);
            mAddContactMenuItem.setVisible(!conversation.isGroup && !isInContacts);
            // ParticipantNormalizedDestination is always null for group conversations.
            final String otherParticipant = conversation.otherParticipantNormalizedDestination;
            mBlockMenuItem.setVisible(otherParticipant != null
                    && !mBlockedSet.contains(otherParticipant));
        } else {
            //group conversation  don't show block
            for (SelectedConversation conversation : mSelectedConversations.values()) {
                if (TextUtils.isEmpty(conversation.otherParticipantNormalizedDestination)) {
                    mBlockMenuItem.setVisible(false);
                    break;
                }
            }
            mAddContactMenuItem.setVisible(false);
        }

        boolean hasCurrentlyOnNotification = false;
        boolean hasCurrentlyOffNotification = false;
        boolean hasPinConversation = false;
        boolean hasUnpinConversation = false;
        final Iterable<SelectedConversation> conversations = mSelectedConversations.values();
        for (final SelectedConversation conversation : conversations) {
            if (conversation.isPin) {
                hasPinConversation = true;
            } else {
                hasUnpinConversation = true;
            }
            if (conversation.notificationEnabled) {
                hasCurrentlyOnNotification = true;
            } else {
                hasCurrentlyOffNotification = true;
            }
        }
        // If we have notification off conversations we show on button, if we have notification on
        // conversation we show off button. We can show both if we have a mixture.
        mNotificationOffMenuItem.setVisible(hasCurrentlyOnNotification);
        mNotificationOnMenuItem.setVisible(hasCurrentlyOffNotification);
        mPinMenuItem.setVisible(hasUnpinConversation);
        mCancelPinMenuItem.setVisible(hasPinConversation);
    }

    public void onActionBarDelete(final Collection<PrivateMultiSelectActionModeCallback.SelectedConversation> conversations) {

        final MultiSelectConversationListActivity activity = mWeakActivity.get();
        if (activity == null) {
            return;
        }
        if (!PhoneUtils.getDefault().isDefaultSmsApp()) {
            // TODO: figure out a good way to combine this with the implementation in
            // ConversationFragment doing similar things

            UiUtils.showSnackBarWithCustomAction(activity,
                    activity.getWindow().getDecorView().getRootView(),
                    getString(R.string.requires_default_sms_app),
                    SnackBar.Action.createCustomAction(() -> {
                        final Intent intent = UIIntents.get().getChangeDefaultSmsAppIntent(activity);
                        activity.startActivityForResult(intent, 1);
                    }, getString(R.string.requires_default_sms_change_button)), null, null);
            return;
        }

        new BaseAlertDialog.Builder(activity)
                .setTitle(HSApplication.getContext().getResources().getQuantityString(
                        R.plurals.delete_conversations_confirmation_dialog_title,
                        conversations.size()))
                .setPositiveButton(R.string.delete_conversation_confirmation_button, (dialog, button) -> {
                    for (PrivateMultiSelectActionModeCallback.SelectedConversation conversation : mSelectedConversations.values()) {
                        DeleteConversationAction.deleteConversation(
                                conversation.conversationId, conversation.timestamp);
                    }
                    activity.exitMultiSelectState();
                })
                .setNegativeButton(R.string.delete_conversation_decline_button, null)
                .show();
    }

    public void onActionBarNotification(final Iterable<PrivateMultiSelectActionModeCallback.SelectedConversation> conversations,
                                        final boolean isNotificationOn) {

        for (final PrivateMultiSelectActionModeCallback.SelectedConversation conversation : conversations) {
            UpdateConversationOptionsAction.enableConversationNotifications(
                    conversation.conversationId, isNotificationOn);
        }
        final MultiSelectConversationListActivity activity = mWeakActivity.get();
        if (activity == null) {
            return;
        }

        final int textId = isNotificationOn ?
                R.string.notification_on_toast_message : R.string.notification_off_toast_message;
        final String message = HSApplication.getContext().getResources().getString(textId, 1);
        UiUtils.showSnackBar(activity, activity.findViewById(android.R.id.list), message, null,
                SnackBar.Action.SNACK_BAR_UNDO, activity.getSnackBarInteractions());
        activity.exitMultiSelectState();
    }

    public void onActionBarAddContact(final PrivateMultiSelectActionModeCallback.SelectedConversation conversation) {
        final MultiSelectConversationListActivity activity = mWeakActivity.get();
        if (activity == null) {
            return;
        }

        final Uri avatarUri;
        if (conversation.icon != null) {
            avatarUri = Uri.parse(conversation.icon);
        } else {
            avatarUri = null;
        }
        final AddContactsConfirmationDialog dialog =
                AddContactsConfirmationDialog.newInstance(avatarUri, conversation.otherParticipantNormalizedDestination);
        UiUtils.showDialogFragment(activity, dialog);
        activity.exitMultiSelectState();
        BugleAnalytics.logEvent("SMS_EditMode_AddContact_BtnClick", true);
    }

    public void onActionBarBlock(final Collection<PrivateMultiSelectActionModeCallback.SelectedConversation> conversation) {
        final MultiSelectConversationListActivity activity = mWeakActivity.get();
        if (activity == null) {
            return;
        }
        for (PrivateMultiSelectActionModeCallback.SelectedConversation selectedConversation : conversation) {
            UpdateDestinationBlockedAction.updateDestinationBlocked(
                    selectedConversation.otherParticipantNormalizedDestination, true,
                    selectedConversation.conversationId,
                    (action, success, block, destination) -> {
                    });
        }
        activity.exitMultiSelectState();
        Toast.makeText(activity, R.string.update_destination_blocked, Toast.LENGTH_LONG).show();
    }

    public void onPin(Collection<PrivateMultiSelectActionModeCallback.SelectedConversation> conversations, boolean pin) {

    }
}
