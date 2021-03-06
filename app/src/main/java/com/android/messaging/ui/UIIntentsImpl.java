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
package com.android.messaging.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.android.ex.photo.Intents.PhotoViewIntentBuilder;
import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.ConversationImagePartsView;
import com.android.messaging.datamodel.MediaScratchFileProvider;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.data.MessageBoxItemData;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.privatebox.ui.SelfVerifyActivity;
import com.android.messaging.receiver.NotificationReceiver;
import com.android.messaging.receiver.RemoteInputReceiver;
import com.android.messaging.sms.MmsSmsUtils;
import com.android.messaging.ui.appsettings.ApnEditorActivity;
import com.android.messaging.ui.appsettings.ApnSettingsActivity;
import com.android.messaging.ui.appsettings.SettingActivity;
import com.android.messaging.ui.appsettings.SettingAdvancedActivity;
import com.android.messaging.ui.appsettings.SettingsSimSelectActivity;
import com.android.messaging.ui.attachmentchooser.AttachmentChooserActivity;
import com.android.messaging.ui.contact.ContactPickerActivity;
import com.android.messaging.ui.conversation.ConversationActivity;
import com.android.messaging.ui.conversation.LaunchConversationActivity;
import com.android.messaging.ui.conversationlist.ArchivedConversationListActivity;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.conversationlist.ForwardMessageActivity;
import com.android.messaging.ui.conversationsettings.PeopleAndOptionsActivity;
import com.android.messaging.ui.customize.CustomBubblesActivity;
import com.android.messaging.ui.debug.DebugMmsConfigActivity;
import com.android.messaging.ui.messagebox.MessageBoxActivity;
import com.android.messaging.ui.photoviewer.BuglePhotoViewActivity;
import com.android.messaging.ui.smsshow.SmsShowActivity;
import com.android.messaging.ui.smsshow.SmsShowDetailActivity;
import com.android.messaging.ui.welcome.WelcomePermissionActivity;
import com.android.messaging.ui.welcome.WelcomeSetAsDefaultActivity;
import com.android.messaging.ui.welcome.WelcomeStartActivity;
import com.android.messaging.util.Assert;
import com.android.messaging.util.ContentType;
import com.android.messaging.util.ConversationIdSet;
import com.android.messaging.util.TransitionUtils;
import com.android.messaging.util.UriUtil;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Navigations;

import static com.android.messaging.datamodel.NotificationServiceV18.EXTRA_FROM_OVERRIDE_SYSTEM_SMS_NOTIFICATION_ACTION;

/**
 * A central repository of Intents used to start activities.
 */
public class UIIntentsImpl extends UIIntents {
    private static final String CELL_BROADCAST_LIST_ACTIVITY =
            "com.android.cellbroadcastreceiver.CellBroadcastListActivity";
    private static final String CALL_TARGET_CLICK_KEY = "touchPoint";
    private static final String CALL_TARGET_CLICK_EXTRA_KEY =
            "android.telecom.extra.OUTGOING_CALL_EXTRAS";
    private static final String MEDIA_SCANNER_CLASS =
            "com.android.providers.media.MediaScannerService";
    private static final String MEDIA_SCANNER_PACKAGE = "com.android.providers.media";
    private static final String MEDIA_SCANNER_SCAN_ACTION = "android.media.IMediaScannerService";

    /**
     * Get an intent which takes you to a conversation
     */
    private Intent getConversationActivityIntent(final Context context,
                                                 final String conversationId, final MessageData draft,
                                                 final boolean withCustomTransition) {
        return getConversationActivityIntent(context, conversationId, draft, withCustomTransition, "", false);
    }

    private Intent getConversationActivityIntent(final Context context,
                                                 final String conversationId, final MessageData draft,
                                                 final boolean withCustomTransition,
                                                 final String conversationName, final boolean autoOpenIme) {
        final Intent intent = new Intent(context, ConversationActivity.class);

        // Always try to reuse the same ConversationActivity in the current task so that we don't
        // have two conversation activities in the back stack.
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Otherwise we're starting a new conversation
        if (conversationId != null) {
            intent.putExtra(UI_INTENT_EXTRA_CONVERSATION_ID, conversationId);
        }
        if (!TextUtils.isEmpty(conversationName)) {
            intent.putExtra(UI_INTENT_EXTRA_CONVERSATION_NAME, conversationName);
        }
        if (draft != null) {
            intent.putExtra(UI_INTENT_EXTRA_DRAFT_DATA, draft);

            // If draft attachments came from an external content provider via a share intent, we
            // need to propagate the URI permissions through to ConversationActivity. This requires
            // putting the URIs into the ClipData (setData also works, but accepts only one URI).
            ClipData clipData = null;
            for (final MessagePartData partData : draft.getParts()) {
                if (partData.isAttachment()) {
                    final Uri uri = partData.getContentUri();
                    if (clipData == null) {
                        clipData = ClipData.newRawUri("Attachments", uri);
                    } else {
                        clipData.addItem(new ClipData.Item(uri));
                    }
                }
            }
            if (clipData != null) {
                intent.setClipData(clipData);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
        if (withCustomTransition) {
            intent.putExtra(UI_INTENT_EXTRA_WITH_CUSTOM_TRANSITION, true);
        }

        if (!(context instanceof Activity)) {
            // If the caller supplies an application context, and not an activity context, we must
            // include this flag
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        if (autoOpenIme) {
            intent.putExtra(UI_INTENT_EXTRA_FROM_MESSAGE_BOX_TO_CONVERSATION, true);
        }
        return intent;
    }

    @Override
    public void launchWelcomeStartActivity(final Context context) {
        final Intent intent = new Intent(context, WelcomeStartActivity.class);
        context.startActivity(intent, TransitionUtils.getTransitionInBundle(context));
    }

    @Override
    public void launchWelcomePermissionActivity(final Context context) {
        final Intent intent = new Intent(context, WelcomePermissionActivity.class);
        context.startActivity(intent, TransitionUtils.getTransitionInBundle(context));
    }

    @Override
    public void launchWelcomeSetAsDefaultActivity(final Context context) {
        final Intent intent = new Intent(context, WelcomeSetAsDefaultActivity.class);
        context.startActivity(intent, TransitionUtils.getTransitionInBundle(context));
    }

    /**
     * Get an intent which takes you to the conversation list
     */
    private Intent getConversationListActivityIntent(final Context context) {
        Intent intent = new Intent(context, ConversationListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    @Override
    public void launchConversationListActivity(final Context context) {
        final Intent intent = getConversationListActivityIntent(context);
        context.startActivity(intent);
    }

    /**
     * Get an intent which shows the low storage warning activity.
     */
    private Intent getSmsStorageLowWarningActivityIntent(final Context context) {
        return new Intent(context, SmsStorageLowWarningActivity.class);
    }

    @Override
    public void launchConversationActivity(final Context context,
                                           final String conversationId, final MessageData draft) {
        final Intent intent = getConversationActivityIntent(context, conversationId, draft, false);
        context.startActivity(intent, TransitionUtils.getTransitionInBundle(context));
    }

    @Override
    public void launchConversationActivity(final Context context,
                                           final String conversationId, final MessageData draft,
                                           final String conversationName, final boolean fromCreate) {
        final Intent intent = getConversationActivityIntent(context, conversationId, draft,
                false, conversationName, false);
        intent.putExtra(UI_INTENT_EXTRA_FROM_CREATE_CONVERSATION, fromCreate);
        context.startActivity(intent, TransitionUtils.getTransitionInBundle(context));
    }

    @Override
    public void launchConversationActivityNewTask(
            final Context context, final String conversationId) {
        final Intent intent = getConversationActivityIntent(context, conversationId, null,
                false /* withCustomTransition */);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent, TransitionUtils.getTransitionInBundle(context));
    }

    @Override
    public void launchConversationActivityWithParentStack(final Context context,
                                                          final String conversationId, final String smsBody) {
        final MessageData messageData = TextUtils.isEmpty(smsBody)
                ? null
                : MessageData.createDraftSmsMessage(conversationId, null, smsBody);
        TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(
                        getConversationActivityIntent(context, conversationId, messageData,
                                false /* withCustomTransition */))
                .startActivities();
    }

    @Override
    public void launchConversationActivityWithParentStackFromMessageBox(final Context context,
                                                                        final String conversationId, final String smsBody) {
        final MessageData messageData = TextUtils.isEmpty(smsBody)
                ? null
                : MessageData.createDraftSmsMessage(conversationId, null, smsBody);
        TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(
                        getConversationActivityIntent(context, conversationId, messageData,
                                false /* withCustomTransition */, "", true))
                .startActivities();
    }

    @Override
    public void launchCreateNewConversationActivity(final Context context,
                                                    final MessageData draft) {
        final Intent intent = new Intent(context, ContactPickerActivity.class);
        if (draft != null) {
            intent.putExtra(UI_INTENT_EXTRA_DRAFT_DATA, draft);
        }
        context.startActivity(intent, TransitionUtils.getTransitionInBundle(context));
    }

    @Override
    public void launchDebugMmsConfigActivity(final Context context) {
        context.startActivity(new Intent(context, DebugMmsConfigActivity.class),
                TransitionUtils.getTransitionInBundle(context));
    }

    @Override
    public void launchAddContactActivity(final Context context, final String destination) {
        final Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        final String destinationType = MmsSmsUtils.isEmailAddress(destination) ?
                Intents.Insert.EMAIL : Intents.Insert.PHONE;
        intent.setType(Contacts.CONTENT_ITEM_TYPE);
        intent.putExtra(destinationType, destination);
        startExternalActivity(context, intent);
    }

    @Override
    public void launchSettingsSimSelectActivity(final Context context) {
        final Intent intent = new Intent(context, SettingsSimSelectActivity.class);
        context.startActivity(intent, TransitionUtils.getTransitionInBundle(context));
    }

    @Override
    public void launchArchivedConversationsActivity(final Context context) {
        final Intent intent = new Intent(context, ArchivedConversationListActivity.class);
        context.startActivity(intent, TransitionUtils.getTransitionInBundle(context));
    }

    @Override
    public void launchBlockedParticipantsActivity(final Context context) {
        final Intent intent = new Intent(context, BlockedParticipantsActivity.class);
        context.startActivity(intent, TransitionUtils.getTransitionInBundle(context));
    }

    @Override
    public void launchDocumentImagePicker(final Fragment fragment) {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, MessagePartData.ACCEPTABLE_IMAGE_TYPES);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(ContentType.IMAGE_UNSPECIFIED);

        fragment.startActivityForResult(intent, REQUEST_PICK_IMAGE_FROM_DOCUMENT_PICKER);
    }

    @Override
    public void launchPeopleAndOptionsActivity(Activity context, String conversationId, String conversationName) {
        final Intent intent = new Intent(context, PeopleAndOptionsActivity.class);
        intent.putExtra(UI_INTENT_EXTRA_CONVERSATION_ID, conversationId);
        intent.putExtra(UI_INTENT_EXTRA_CONVERSATION_NAME, conversationName);
        context.startActivityForResult(intent, 0, TransitionUtils.getTransitionInBundle(context));
    }

    @Override
    public void launchPhoneCallActivity(final Context context, final String phoneNumber,
                                        final Point clickPosition) {
        final Intent intent = new Intent(Intent.ACTION_CALL,
                Uri.parse(UriUtil.SCHEME_TEL + phoneNumber));
        final Bundle extras = new Bundle();
        extras.putParcelable(CALL_TARGET_CLICK_KEY, clickPosition);
        intent.putExtra(CALL_TARGET_CLICK_EXTRA_KEY, extras);
        startExternalActivity(context, intent);
    }

    @Override
    public void launchClassZeroActivity(final Context context, final ContentValues messageValues) {
        final Intent classZeroIntent = new Intent(context, ClassZeroActivity.class)
                .putExtra(UI_INTENT_EXTRA_MESSAGE_VALUES, messageValues)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        context.startActivity(classZeroIntent, TransitionUtils.getTransitionInBundle(context));
    }

    @Override
    public void launchForwardMessageActivity(final Context context, final MessageData message) {
        final Intent forwardMessageIntent = new Intent(context, ForwardMessageActivity.class)
                .putExtra(UI_INTENT_EXTRA_DRAFT_DATA, message);
        context.startActivity(forwardMessageIntent, TransitionUtils.getTransitionInBundle(context));
    }

    @Override
    public void launchVCardDetailActivity(final Context context, final Uri vcardUri) {
        final Intent vcardDetailIntent = new Intent(context, VCardDetailActivity.class)
                .putExtra(UI_INTENT_EXTRA_VCARD_URI, vcardUri);
        context.startActivity(vcardDetailIntent, TransitionUtils.getTransitionInBundle(context));
    }

    @Override
    public void launchSaveVCardToContactsActivity(final Context context, final Uri vcardUri) {
        Assert.isTrue(MediaScratchFileProvider.isMediaScratchSpaceUri(vcardUri));
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(vcardUri, ContentType.TEXT_VCARD.toLowerCase());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startExternalActivity(context, intent);
    }

    @Override
    public void launchAttachmentChooserActivity(final Activity activity,
                                                final String conversationId, final int requestCode) {
        final Intent intent = new Intent(activity, AttachmentChooserActivity.class);
        intent.putExtra(UI_INTENT_EXTRA_CONVERSATION_ID, conversationId);
        activity.startActivityForResult(intent, requestCode, TransitionUtils.getTransitionInBundle(activity));
    }

    @Override
    public void launchFullScreenVideoViewer(final Context context, final Uri videoUri) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // So we don't see "surrounding" images in Gallery
        intent.putExtra("SingleItemOnly", true);
        intent.setDataAndType(videoUri, ContentType.VIDEO_UNSPECIFIED);
        startExternalActivity(context, intent);
    }

    @Override
    public void launchFullScreenPhotoViewer(final Activity activity, final Uri initialPhoto,
                                            final Rect initialPhotoBounds, final Uri photosUri) {
        final PhotoViewIntentBuilder builder =
                com.android.ex.photo.Intents.newPhotoViewIntentBuilder(
                        activity, BuglePhotoViewActivity.class);
        builder.setPhotosUri(photosUri.toString());
        builder.setInitialPhotoUri(initialPhoto.toString());
        builder.setProjection(ConversationImagePartsView.PhotoViewQuery.PROJECTION);

        // Set the location of the imageView so that the photoviewer can animate from that location
        // to full screen.
        builder.setScaleAnimation(initialPhotoBounds.left, initialPhotoBounds.top,
                initialPhotoBounds.width(), initialPhotoBounds.height());

        builder.setDisplayThumbsFullScreen(false);
        builder.setMaxInitialScale(8);
        activity.startActivity(builder.build());
    }

    @Override
    public void launchSettingsActivity(final Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent, TransitionUtils.getTransitionInBundle(context));
    }

    @Override
    public void launchPerSubscriptionSettingsActivity(final Context context, final int subId,
                                                      final String settingTitle) {
        final Intent intent = getPerSubscriptionSettingsIntent(context, subId, settingTitle);
        context.startActivity(intent, TransitionUtils.getTransitionInBundle(context));
    }

    @Override
    public void launchSmsShowDetailActivity(Context context, int id, String smsShowUrl) {
        final Intent intent = new Intent(context, SmsShowDetailActivity.class);
        intent.putExtra(UI_INTENT_EXTRA_SMS_SHOW_ID, id);
        intent.putExtra(UI_INTENT_EXTRA_SMS_SHOW_URL, smsShowUrl);

        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.smsshow_detail_in, R.anim.smsshow_detail_out);
        }
        context.startActivity(intent, TransitionUtils.getTransitionInBundle(context));
    }

    @Override
    public void launchSmsShowActivity() {
        final Intent intent = new Intent(Factory.get().getApplicationContext(), SmsShowActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Factory.get().getApplicationContext().startActivity(intent);
    }

    @Override
    public void launchDragHotSeatActivity(Context context) {
        Intent intent = new Intent(context, DragHotSeatActivity.class);
        context.startActivity(intent);
    }

    public void launchCustomBubblesActivity(Context context, String conversationId) {
        final Intent intent = new Intent(context, CustomBubblesActivity.class);
        if (!TextUtils.isEmpty(conversationId)) {
            intent.putExtra(UI_INTENT_EXTRA_CONVERSATION_ID, conversationId);
        }
        context.startActivity(intent, TransitionUtils.getTransitionInBundle(context));
    }

    @Override
    public void launchMessageBoxActivity(Context context, MessageBoxItemData itemData) {
        final Intent intent = new Intent(context, MessageBoxActivity.class);
        intent.putExtra(UI_INTENT_EXTRA_MESSAGE_BOX_ITEM, itemData);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(context, R.anim.fade_in, R.anim.fade_out);
        context.startActivity(intent, options.toBundle());
    }

    @Override
    public Intent getViewUrlIntent(final String url) {
        final Uri uri = Uri.parse(url);
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    @Override
    public void broadcastConversationSelfIdChange(final Context context,
                                                  final String conversationId, final String conversationSelfId) {
        final Intent intent = new Intent(CONVERSATION_SELF_ID_CHANGE_BROADCAST_ACTION);
        intent.putExtra(UI_INTENT_EXTRA_CONVERSATION_ID, conversationId);
        intent.putExtra(UI_INTENT_EXTRA_CONVERSATION_SELF_ID, conversationSelfId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public PendingIntent getPendingIntentForConversationListActivity(final Context context) {
        final Intent intent = getConversationListActivityIntent(context);
        return getPendingIntentWithParentStack(context, intent, 0);
    }

    @Override
    public PendingIntent getPendingIntentForConversationListActivityFromNotification(final Context context) {
        final Intent intent = getConversationListActivityIntent(context);
        intent.putExtra(BugleNotifications.EXTRA_FROM_NOTIFICATION, true);
        return getPendingIntentWithParentStack(context, intent, 0);
    }

    @Override
    public PendingIntent getPendingIntentForPrivateConversationListActivityFromNotification(final Context context) {
        final Intent intent = new Intent(context, SelfVerifyActivity.class);
        intent.putExtra(SelfVerifyActivity.INTENT_KEY_ACTIVITY_ENTRANCE,
                SelfVerifyActivity.ENTRANCE_NOTIFICATION_TO_CONVERSATION_LIST);
        return getPendingIntentWithParentStack(context, intent, 0);
    }

    @Override
    public PendingIntent getPendingIntentForConversationActivity(final Context context,
                                                                 final String conversationId, final MessageData draft) {
        final Intent intent = getConversationActivityIntent(context, conversationId, draft,
                false /* withCustomTransition */);
        // Ensure that the platform doesn't reuse PendingIntents across conversations
        intent.setData(MessagingContentProvider.buildConversationMetadataUri(conversationId));
        return getPendingIntentWithParentStack(context, intent, 0);
    }

    @Override
    public PendingIntent getPendingIntentForConversationActivityFromNotification(final Context context,
                                                                                 final String conversationId, final MessageData draft) {
        final Intent intent = getConversationActivityIntent(context, conversationId, draft,
                false /* withCustomTransition */);
        // Ensure that the platform doesn't reuse PendingIntents across conversations
        intent.setData(MessagingContentProvider.buildConversationMetadataUri(conversationId));
        intent.putExtra(BugleNotifications.EXTRA_FROM_NOTIFICATION, true);
        return getPendingIntentWithParentStack(context, intent, 0);
    }

    @Override
    public PendingIntent getPendingIntentForConversationActivityFromFakeDefaultSmsNotification(final Context context,
                                                                                               final String conversationId,
                                                                                               boolean isNotificationAction) {
        Intent intent = getConversationActivityIntent(context, conversationId, null,
                false /* withCustomTransition */);
        // Ensure that the platform doesn't reuse PendingIntents across conversations
        intent.setData(MessagingContentProvider.buildConversationMetadataUri(conversationId));
        if (isNotificationAction) {
            HSLog.d("NotificationListener", "isNotificationAction = true");
            intent.putExtra(EXTRA_FROM_OVERRIDE_SYSTEM_SMS_NOTIFICATION_ACTION, true);
        } else {
            HSLog.d("NotificationListener", "isNotificationAction = false");
        }
        intent.putExtra(UI_INTENT_EXTRA_CONVERSATION_ID_AFTER_DEFAULT_SET, conversationId);
        return getPendingIntentWithParentStack(context, intent, 0, isNotificationAction);
    }

    @Override
    public PendingIntent getPendingIntentForPrivateConversationActivityFromNotification(final Context context, final String conversationId) {
        final Intent intent = new Intent(context, SelfVerifyActivity.class);
        intent.putExtra(SelfVerifyActivity.INTENT_KEY_ACTIVITY_ENTRANCE,
                SelfVerifyActivity.ENTRANCE_NOTIFICATION);
        intent.putExtra(SelfVerifyActivity.INTENT_KEY_ENTRANCE_CONVERSATION_ID, conversationId);
        return getPendingIntentWithParentStack(context, intent, 0);
    }

    @Override
    public Intent getIntentForConversationActivity(final Context context,
                                                   final String conversationId, final MessageData draft) {
        final Intent intent = getConversationActivityIntent(context, conversationId, draft,
                false /* withCustomTransition */);
        return intent;
    }

    @Override
    public PendingIntent getPendingIntentForSendingMessageToConversation(final Context context,
                                                                         final String conversationId, final String selfId, final boolean requiresMms,
                                                                         final int requestCode,
                                                                         final int notificationId) {
        final Intent intent = new Intent(context, RemoteInputReceiver.class);
        intent.setAction(Intent.ACTION_SENDTO);
        // Ensure that the platform doesn't reuse PendingIntents across conversations
        intent.setData(MessagingContentProvider.buildConversationMetadataUri(conversationId));
        intent.putExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID, conversationId);
        intent.putExtra(UIIntents.UI_INTENT_EXTRA_SELF_ID, selfId);
        intent.putExtra(UIIntents.UI_INTENT_EXTRA_REQUIRES_MMS, requiresMms);
        intent.putExtra(UIIntents.UI_INTENT_EXTRA_NOTIFICATION_ID, notificationId);
        return PendingIntent.getBroadcast(HSApplication.getContext(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public PendingIntent getPendingIntentForClearingNotifications(final Context context,
                                                                  final int updateTargets, final ConversationIdSet conversationIdSet,
                                                                  final int requestCode) {
        final Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(ACTION_RESET_NOTIFICATIONS);
        intent.putExtra(UI_INTENT_EXTRA_NOTIFICATIONS_UPDATE, updateTargets);
        if (conversationIdSet != null) {
            intent.putExtra(UI_INTENT_EXTRA_CONVERSATION_ID_SET,
                    conversationIdSet.getDelimitedString());
        }
        return PendingIntent.getBroadcast(context,
                requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Gets a PendingIntent associated with an Intent to start an Activity. All notifications
     * that starts an Activity must use this method to get a PendingIntent, which achieves two
     * goals:
     * 1. The target activities will be created, with any existing ones destroyed. This ensures
     * we don't end up with multiple instances of ConversationListActivity, for example.
     * 2. The target activity, when launched, will have its backstack correctly constructed so
     * back navigation will work correctly.
     */
    private static PendingIntent getPendingIntentWithParentStack(final Context context,
                                                                 final Intent intent, final int requestCode) {
        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (plus the Intent itself)
        stackBuilder.addNextIntentWithParentStack(intent);
        final PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
        return resultPendingIntent;
    }

    private static PendingIntent getPendingIntentWithParentStack(final Context context,
                                                                 final Intent intent, final int requestCode, boolean isNotificationAction) {
        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (plus the Intent itself)
        stackBuilder.addNextIntentWithParentStack(intent);
        if (isNotificationAction){
            return stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_ONE_SHOT);
        } else {
            return stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    @Override
    public Intent getRingtonePickerIntent(final String title, final Uri existingUri,
                                          final Uri defaultUri, final int toneType) {
        return new Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, toneType)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, title)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existingUri)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, defaultUri);
    }

    @Override
    public PendingIntent getPendingIntentForLowStorageNotifications(final Context context) {
        final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        final Intent conversationListIntent = getConversationListActivityIntent(context);
        taskStackBuilder.addNextIntent(conversationListIntent);
        taskStackBuilder.addNextIntentWithParentStack(
                getSmsStorageLowWarningActivityIntent(context));

        return taskStackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public PendingIntent getPendingIntentForSecondaryUserNewMessageNotification(
            final Context context) {
        return getPendingIntentForConversationListActivity(context);
    }

    @Override
    public Intent getWirelessAlertsIntent() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName(CMAS_COMPONENT, CELL_BROADCAST_LIST_ACTIVITY));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    public Intent getApnEditorIntent(final Context context, final String rowId, final int subId) {
        final Intent intent = new Intent(context, ApnEditorActivity.class);
        intent.putExtra(UI_INTENT_EXTRA_APN_ROW_ID, rowId);
        intent.putExtra(UI_INTENT_EXTRA_SUB_ID, subId);
        return intent;
    }

    @Override
    public Intent getApnSettingsIntent(final Context context, final int subId) {
        final Intent intent = new Intent(context, ApnSettingsActivity.class)
                .putExtra(UI_INTENT_EXTRA_SUB_ID, subId);
        return intent;
    }

    @Override
    public Intent getAdvancedSettingsIntent(final Context context) {
        return getPerSubscriptionSettingsIntent(context, ParticipantData.DEFAULT_SELF_SUB_ID, null);
    }

    @Override
    public Intent getChangeDefaultSmsAppIntent(final Activity activity) {
        final Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, activity.getPackageName());
        return intent;
    }

    @Override
    public void launchBrowserForUrl(final Context context, final String url) {
        final Intent intent = getViewUrlIntent(url);
        startExternalActivity(context, intent);
    }

    /**
     * Provides a safe way to handle external activities which may not exist.
     */
    private void startExternalActivity(final Context context, final Intent intent) {
        Navigations.startActivitySafely(context, intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
        }
    }

    private Intent getPerSubscriptionSettingsIntent(final Context context, final int subId, @Nullable final String settingTitle) {
        return new Intent(context, SettingAdvancedActivity.class)
                .putExtra(UI_INTENT_EXTRA_SUB_ID, subId)
                .putExtra(UI_INTENT_EXTRA_PER_SUBSCRIPTION_SETTING_TITLE, settingTitle);
    }

    @Override
    public Intent getLaunchConversationActivityIntent(final Context context) {
        final Intent intent = new Intent(context, LaunchConversationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
    }

    @Override
    public void kickMediaScanner(final Context context, final String volume) {
        final Intent intent = new Intent(MEDIA_SCANNER_SCAN_ACTION)
                .putExtra(MediaStore.MEDIA_SCANNER_VOLUME, volume)
                .setClassName(MEDIA_SCANNER_PACKAGE, MEDIA_SCANNER_CLASS);
        context.startService(intent);
    }

    @Override
    public PendingIntent getWidgetPendingIntentForConversationActivity(final Context context,
                                                                       final String conversationId, final int requestCode) {
        final Intent intent = getConversationActivityIntent(context, null, null,
                false /* withCustomTransition */);
        if (conversationId != null) {
            intent.putExtra(UI_INTENT_EXTRA_CONVERSATION_ID, conversationId);

            // Set the action to something unique to this conversation so if someone calls this
            // function again on a different conversation, they'll get a new PendingIntent instead
            // of the old one.
            intent.setAction(ACTION_WIDGET_CONVERSATION + conversationId);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return getPendingIntentWithParentStack(context, intent, requestCode);
    }

    @Override
    public PendingIntent getWidgetPendingIntentForContactPickerActivity(final Context context,
                                                                       final int requestCode) {
        final Intent intent = new Intent(context, ContactPickerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return getPendingIntentWithParentStack(context, intent, requestCode);
    }

    @Override
    public PendingIntent getWidgetPendingIntentForConversationListActivity(
            final Context context) {
        final Intent intent = getConversationListActivityIntent(context);
        return getPendingIntentWithParentStack(context, intent, 0);
    }

    @Override
    public PendingIntent getWidgetPendingIntentForConfigurationActivity(final Context context,
                                                                        final int appWidgetId) {
        final Intent configureIntent = new Intent(context, WidgetPickConversationActivity.class);
        configureIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        configureIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        configureIntent.setData(Uri.parse(configureIntent.toUri(Intent.URI_INTENT_SCHEME)));
        configureIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        return getPendingIntentWithParentStack(context, configureIntent, 0);
    }
}
