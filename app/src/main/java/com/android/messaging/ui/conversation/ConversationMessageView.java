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

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.action.DeleteMessageAction;
import com.android.messaging.datamodel.data.ConversationMessageData;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.datamodel.data.SubscriptionListData.SubscriptionListEntry;
import com.android.messaging.datamodel.media.ImageRequestDescriptor;
import com.android.messaging.datamodel.media.MessagePartImageRequestDescriptor;
import com.android.messaging.scheduledmessage.MessageScheduleManager;
import com.android.messaging.scheduledmessage.ScheduledEditChooseDialog;
import com.android.messaging.scheduledmessage.SendScheduledMessageAction;
import com.android.messaging.sms.MmsUtils;
import com.android.messaging.ui.AsyncImageView;
import com.android.messaging.ui.AsyncImageView.AsyncImageViewDelayLoader;
import com.android.messaging.ui.AttachmentVCardItemView;
import com.android.messaging.ui.AudioAttachmentView;
import com.android.messaging.ui.BaseAlertDialog;
import com.android.messaging.ui.ContactIconView;
import com.android.messaging.ui.ConversationDrawables;
import com.android.messaging.ui.MultiAttachmentLayout;
import com.android.messaging.ui.MultiAttachmentLayout.OnAttachmentClickListener;
import com.android.messaging.ui.PersonItemView;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.VideoThumbnailView;
import com.android.messaging.ui.customize.AvatarBgDrawables;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.ui.wallpaper.WallpaperManager;
import com.android.messaging.util.AccessibilityUtil;
import com.android.messaging.util.Assert;
import com.android.messaging.util.AvatarUriUtil;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ContentType;
import com.android.messaging.util.DefaultSMSUtils;
import com.android.messaging.util.ImageUtils;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.UiUtils;
import com.google.common.base.Predicate;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The view for a single entry in a conversation.
 */
public class ConversationMessageView extends RelativeLayout implements View.OnClickListener,
        View.OnLongClickListener, OnAttachmentClickListener {

    public interface ConversationMessageViewHost {
        boolean onAttachmentClick(ConversationMessageData view, MessagePartData attachment,
                                  Rect imageBounds, boolean longPress);

        SubscriptionListEntry getSubscriptionEntryForSelfParticipant(String selfParticipantId,
                                                                     boolean excludeDefault);

        void onScheduledEditClick(ConversationMessageData data, long scheduledTime);
    }

    private ConversationMessageData mData;
    private LinearLayout mMessageAttachmentsView;
    private MultiAttachmentLayout mMultiAttachmentView;
    private AsyncImageView mMessageImageView;
    private TextView mMessageTextView;
    private ImageView mMessageIsLockView;
    private ViewGroup mStatusContainer;
    private TextView mStatusTextView;
    @Nullable
    private ContactIconView mContactIconView;
    @Nullable
    private ViewGroup mContactIconContainer;
    @Nullable
    private ImageView mContactIconBg;
    private ConversationMessageBubbleView mMessageBubble;
    private ImageView mDeliveredBadge;
    private ViewGroup mMessageMetadataView;
    private ViewGroup mMessageTextAndInfoView;
    private ConversationMessageViewHost mHost;
    private ImageView mCheckBox;
    private View mScheduledEditView;
    private int mOffset;
    private boolean mHasCustomBackground;
    private boolean mIsRtl = Dimensions.isRtl();

    public ConversationMessageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        // TODO: we should switch to using Binding and DataModel factory methods.
        mData = new ConversationMessageData();

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContactIconView = findViewById(R.id.conversation_icon);
        mContactIconBg = findViewById(R.id.conversation_icon_bg);
        mContactIconContainer = findViewById(R.id.conversation_icon_container);

        if (mContactIconView != null) {
            mContactIconView.setOnLongClickListener(view -> {
                ConversationMessageView.this.performLongClick();
                return true;
            });

        }

        mMessageAttachmentsView = findViewById(R.id.message_attachments);
        if (mMessageAttachmentsView != null) {
            mMultiAttachmentView = findViewById(R.id.multiple_attachments);
            mMultiAttachmentView.setOnAttachmentClickListener(this);

            mMessageImageView = findViewById(R.id.message_image);
            mMessageImageView.setOnClickListener(this);
            mMessageImageView.setOnLongClickListener(this);
        }


        mMessageTextView = findViewById(R.id.message_text);
        mMessageTextView.setOnClickListener(this);
        IgnoreLinkLongClickHelper.ignoreLinkLongClick(mMessageTextView, this);

        mStatusContainer = findViewById(R.id.message_status_container);
        int color = PrimaryColors.getPrimaryColor();
        mStatusTextView = findViewById(R.id.message_status);
        mStatusContainer.setBackground(BackgroundDrawables.createBackgroundDrawable(
                Color.argb(51, Color.red(color), Color.green(color), Color.blue(color)), Dimensions.pxFromDp(16), false));

        mMessageIsLockView = findViewById(R.id.message_lock);

        mMessageBubble = findViewById(R.id.message_content);
        mDeliveredBadge = findViewById(R.id.smsDeliveredBadge);
        mMessageMetadataView = findViewById(R.id.message_metadata);
        mMessageTextAndInfoView = findViewById(R.id.message_text_and_info);
        mCheckBox = findViewById(R.id.check_box);
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.disableTransitionType(LayoutTransition.DISAPPEARING);
        this.setLayoutTransition(layoutTransition);
        setOffset(30);
    }

    public void setOffset(int offset) {
        mOffset = (int) (getContext().getResources().getDisplayMetrics().density * offset + 0.5f);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int horizontalSpace = MeasureSpec.getSize(widthMeasureSpec);
        final int iconContainerSize = getResources()
                .getDimensionPixelSize(R.dimen.conversation_message_contact_icon_container_size);

        final int unspecifiedMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        if (mContactIconContainer != null) {
            final int iconMeasureSpec = MeasureSpec.makeMeasureSpec(iconContainerSize, MeasureSpec.EXACTLY);
            mContactIconContainer.measure(iconMeasureSpec, iconMeasureSpec);
        }

        final int arrowWidth =
                getResources().getDimensionPixelSize(R.dimen.message_bubble_arrow_width);

        // We need to subtract contact icon width twice from the horizontal space to get
        // the max leftover space because we want the message bubble to extend no further than the
        // starting position of the message bubble in the opposite direction.
        final int iconSize = getResources()
                .getDimensionPixelSize(R.dimen.conversation_message_contact_icon_size);

        final int maxLeftoverSpace = horizontalSpace - iconSize * 2
                - arrowWidth - getPaddingLeft() - getPaddingRight();
        final int messageContentWidthMeasureSpec = MeasureSpec.makeMeasureSpec(maxLeftoverSpace,
                MeasureSpec.AT_MOST);

        mMessageBubble.measure(messageContentWidthMeasureSpec, unspecifiedMeasureSpec);

        int maxHeight;

        if (mData.getIsIncoming()) {
            maxHeight = Math.max(shouldShowSimplifiedVisualStyle() ? 0 : mContactIconContainer.getMeasuredHeight(),
                    mMessageBubble.getMeasuredHeight() + getTextTopPadding());
        } else {
            maxHeight = mMessageBubble.getMeasuredHeight();
        }

        setMeasuredDimension(horizontalSpace, maxHeight + getPaddingBottom() + getPaddingTop());
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right,
                            final int bottom) {
        final boolean isRtl = AccessibilityUtil.isLayoutRtl(this);

        final int iconSize = getResources()
                .getDimensionPixelSize(R.dimen.conversation_message_contact_icon_container_size);
        final int iconWidth = iconSize;
        final int iconHeight = iconSize;

        final int iconTop = getPaddingTop();
        final int iconBubbleMargin = getResources()
                .getDimensionPixelSize(R.dimen.conversation_message_contact_bubble_margin);
        final int contentWidth = (right - left) - iconWidth - getPaddingLeft() - getPaddingRight();
        final int contentHeight = mMessageBubble.getMeasuredHeight();
        final int contentTop = iconTop + (!mData.getIsIncoming() ? 0 : getTextTopPadding());

        final int iconLeft;
        final int contentLeft;
        if (mData.getIsIncoming()) {
            if (isRtl) {
                iconLeft = (right - left) - getPaddingRight() - iconWidth;
                contentLeft = iconLeft - contentWidth - iconBubbleMargin;
            } else {
                iconLeft = getPaddingLeft();
                contentLeft = iconLeft + iconWidth + iconBubbleMargin;
            }
        } else {
            if (isRtl) {
                iconLeft = getPaddingLeft();
                contentLeft = iconLeft;
            } else {
                iconLeft = (right - left) - getPaddingRight();
                contentLeft = iconLeft - contentWidth;
            }
        }

        if (mContactIconContainer != null) {
            mContactIconContainer.layout(iconLeft, iconTop, iconLeft + iconWidth, iconTop + iconHeight);
        }

        mMessageBubble.layout(contentLeft, contentTop, contentLeft + contentWidth,
                contentTop + contentHeight);

        int bubbleBgHeight = (mMessageTextAndInfoView.getVisibility() == View.VISIBLE ? mMessageTextAndInfoView.getMeasuredHeight() : 0)
                + (mMessageAttachmentsView != null && mMessageAttachmentsView.getVisibility() == View.VISIBLE ? mMessageAttachmentsView.getMeasuredHeight() : 0);
        mCheckBox.layout(!mIsRtl ? right - Dimensions.pxFromDp(37) : left + Dimensions.pxFromDp(17),
                contentTop + bubbleBgHeight / 2 - Dimensions.pxFromDp(20) / 2,
                !mIsRtl ? right - Dimensions.pxFromDp(17) : left + Dimensions.pxFromDp(37),
                contentTop + bubbleBgHeight / 2 + Dimensions.pxFromDp(20) / 2);
    }

    /**
     * Fills in the data associated with this view.
     */
    public void bind(final ConversationMessageData data, boolean isMultiSelected) {

        // Update our UI model
        mData = data;
        mHasCustomBackground = WallpaperManager.hasCustomWallpaper(mData.getConversationId());

        if (mContactIconBg != null) {
            mContactIconBg.setImageDrawable(AvatarBgDrawables.getAvatarBg(false, mHasCustomBackground));
        }

        if (mData.getIsMms() && !mData.hasText()) {
            mScheduledEditView = findViewById(R.id.scheduled_edit_icon_attachments);
        } else {
            mScheduledEditView = findViewById(R.id.scheduled_edit_icon);
        }
        // Update text and image content for the view.
        updateViewContent();

        // Update colors and layout parameters for the view.
        updateViewAppearance();
    }

    public void setHost(final ConversationMessageViewHost host) {
        mHost = host;
    }

    /**
     * Sets a delay loader instance to manage loading / resuming of image attachments.
     */
    public void setImageViewDelayLoader(final AsyncImageViewDelayLoader delayLoader) {
        if (mMessageImageView != null) {
            mMessageImageView.setDelayLoader(delayLoader);
            mMultiAttachmentView.setImageViewDelayLoader(delayLoader);
        }
    }

    public ConversationMessageData getData() {
        return mData;
    }

    /**
     * Returns whether we should show simplified visual style for the message view (i.e. hide the
     * avatar and bubble arrow, reduce padding).
     */
    private boolean shouldShowSimplifiedVisualStyle() {
        return mData.getCanClusterWithPreviousMessage();
    }

    /**
     * Returns whether we need to show message bubble arrow. We don't show arrow if the message
     * contains media attachments or if shouldShowSimplifiedVisualStyle() is true.
     */
    private boolean shouldShowMessageBubbleArrow() {
        return !shouldShowSimplifiedVisualStyle()
                && !(mData.hasAttachments());
    }

    /**
     * Returns whether we need to show a message bubble for text content.
     */
    private boolean shouldShowMessageTextBubble() {
        if (mData.hasText()) {
            return true;
        }
        final String subjectText = MmsUtils.cleanseMmsSubject(getResources(),
                mData.getMmsSubject());
        if (!TextUtils.isEmpty(subjectText)) {
            return true;
        }
        return false;
    }

    private int getTextTopPadding() {
        return shouldShowSimplifiedVisualStyle() ?
                (int) getResources().getDimension(R.dimen.conversation_message_bubble_incoming_simple_style_top_padding) :
                (int) getResources().getDimension(R.dimen.conversation_message_bubble_incoming_top_padding);
    }

    private void updateViewContent() {
        updateMessageContent();

        // Update the sim indicator.
        final SubscriptionListEntry subscriptionEntry =
                mHost.getSubscriptionEntryForSelfParticipant(mData.getSelfParticipantId(),
                        true /* excludeDefault */);
        final boolean simNameVisible = subscriptionEntry != null &&
                !TextUtils.isEmpty(subscriptionEntry.displayName) &&
                !mData.getCanClusterWithNextMessage();

        boolean deliveredBadgeVisible = false;
        boolean hasWallPaper = WallpaperManager.hasWallpaper(mData.getConversationId());
        if (mData.getIsDeliveryReportOpen()) {
            if (mData.getStatus() == MessageData.BUGLE_STATUS_OUTGOING_COMPLETE) {
                mDeliveredBadge.setVisibility(View.VISIBLE);
                mDeliveredBadge.setImageResource(R.drawable.ic_message_sent);
                if (!hasWallPaper) {
                    mDeliveredBadge.getDrawable().setColorFilter(
                            getResources().getColor(R.color.timestamp_text_outgoing), PorterDuff.Mode.SRC_ATOP);
                } else {
                    mDeliveredBadge.getDrawable().setColorFilter(
                            getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                }
                deliveredBadgeVisible = true;
            } else if (mData.getStatus() == MessageData.BUGLE_STATUS_OUTGOING_DELIVERED) {
                mDeliveredBadge.setVisibility(View.VISIBLE);
                mDeliveredBadge.setImageResource(R.drawable.ic_message_delivered);
                if (!hasWallPaper) {
                    mDeliveredBadge.getDrawable().setColorFilter(
                            getResources().getColor(R.color.timestamp_text_outgoing), PorterDuff.Mode.SRC_ATOP);
                } else {
                    mDeliveredBadge.getDrawable().setColorFilter(
                            getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                }
                deliveredBadgeVisible = true;
            } else {
                mDeliveredBadge.setVisibility(View.GONE);
            }
        } else {
            if (mData.getStatus() == MessageData.BUGLE_STATUS_OUTGOING_DELIVERED) {
                mDeliveredBadge.setVisibility(View.VISIBLE);
                mDeliveredBadge.setImageResource(R.drawable.ic_message_delivered);
                if (!hasWallPaper) {
                    mDeliveredBadge.getDrawable().setColorFilter(
                            getResources().getColor(R.color.timestamp_text_outgoing), PorterDuff.Mode.SRC_ATOP);
                } else {
                    mDeliveredBadge.getDrawable().setColorFilter(
                            getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                }
                deliveredBadgeVisible = true;
            } else {
                mDeliveredBadge.setVisibility(View.GONE);
            }
        }

        final boolean statusVisible = !mData.getCanClusterWithNextMessage()
                || mData.getIsLocked() || deliveredBadgeVisible;
        if (statusVisible) {
            int statusResId = -1;
            String statusText = null;
            switch (mData.getStatus()) {
                case MessageData.BUGLE_STATUS_INCOMING_AUTO_DOWNLOADING:
                case MessageData.BUGLE_STATUS_INCOMING_MANUAL_DOWNLOADING:
                case MessageData.BUGLE_STATUS_INCOMING_RETRYING_AUTO_DOWNLOAD:
                case MessageData.BUGLE_STATUS_INCOMING_RETRYING_MANUAL_DOWNLOAD:
                    statusResId = R.string.message_status_downloading;
                    break;

                case MessageData.BUGLE_STATUS_INCOMING_YET_TO_MANUAL_DOWNLOAD:
                    if (!OsUtil.isSecondaryUser()) {
                        if (isSelected()) {
                            statusResId = R.string.message_status_download_action;
                        } else {
                            statusResId = R.string.message_status_download;
                        }
                    }
                    break;

                case MessageData.BUGLE_STATUS_INCOMING_EXPIRED_OR_NOT_AVAILABLE:
                    if (!OsUtil.isSecondaryUser()) {
                        statusResId = R.string.message_status_download_error;
                    }
                    break;

                case MessageData.BUGLE_STATUS_INCOMING_DOWNLOAD_FAILED:
                    if (!OsUtil.isSecondaryUser()) {
                        if (isSelected()) {
                            statusResId = R.string.message_status_download_action;
                        } else {
                            statusResId = R.string.message_status_download;
                        }
                    }
                    break;

                case MessageData.BUGLE_STATUS_OUTGOING_YET_TO_SEND:
                case MessageData.BUGLE_STATUS_OUTGOING_SENDING:
                    if (getData().getIsSms()) {
                        statusText = mData.getFormattedReceivedTimeStamp();
                    } else {
                        statusResId = R.string.message_status_sending;
                    }
                    break;

                case MessageData.BUGLE_STATUS_OUTGOING_RESENDING:
                case MessageData.BUGLE_STATUS_OUTGOING_AWAITING_RETRY:
                    if (getData().getIsSms()) {
                        statusText = mData.getFormattedReceivedTimeStamp();
                    } else {
                        statusResId = R.string.message_status_send_retrying;
                    }
                    break;

                case MessageData.BUGLE_STATUS_OUTGOING_FAILED_EMERGENCY_NUMBER:
                    statusResId = R.string.message_status_send_failed_emergency_number;
                    break;

                case MessageData.BUGLE_STATUS_OUTGOING_SCHEDULED_FAILED:
                case MessageData.BUGLE_STATUS_OUTGOING_FAILED:
                    // don't show the error state unless we're the default sms app
                    if (DefaultSMSUtils.isDefaultSmsApp()) {
                        if (isSelected()) {
                            statusResId = R.string.message_status_resend;
                        } else {
                            statusResId = MmsUtils.mapRawStatusToErrorResourceId(
                                    mData.getStatus(), mData.getRawTelephonyStatus());
                        }
                        break;
                    }
                    // FALL THROUGH HERE

                case MessageData.BUGLE_STATUS_OUTGOING_COMPLETE:
                case MessageData.BUGLE_STATUS_INCOMING_COMPLETE:
                case MessageData.BUGLE_STATUS_OUTGOING_SCHEDULED:
                default:
                    statusText = mData.getFormattedReceivedTimeStamp();
                    break;
            }

            if (statusResId >= 0) {
                statusText = getResources().getString(statusResId);
            }

            if (simNameVisible) {
                final String simNameText = mData.getIsIncoming() ? getResources().getString(
                        R.string.incoming_sim_name_text, subscriptionEntry.displayName) :
                        subscriptionEntry.displayName;
                mStatusTextView.setText(statusText + "  " + simNameText);
            } else {
                mStatusTextView.setText(statusText);
            }
            mStatusTextView.setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setVisibility(View.GONE);
        }

        if (mData.getIsLocked()) {
            mMessageIsLockView.setVisibility(VISIBLE);
        } else {
            mMessageIsLockView.setVisibility(GONE);
        }

        if (!mData.getIsIncoming()
                && mData.isScheduledMessage()) {
            mScheduledEditView.setVisibility(VISIBLE);
            int bgColor;
            if (ThemeUtils.isDefaultTheme() && !hasWallPaper) {
                bgColor = 0xffc8d4dc;
            } else {
                bgColor = PrimaryColors.getPrimaryColor() & 0x00ffffff | 0x80000000;
            }
            mScheduledEditView.setBackground(BackgroundDrawables.createBackgroundDrawable(bgColor,
                    HSApplication.getContext().getResources().getColor(com.superapps.R.color.ripples_ripple_color),
                    Dimensions.pxFromDp(23.3f), true, true));
            mScheduledEditView.setOnClickListener(v -> {
                ScheduledEditChooseDialog dialog = new ScheduledEditChooseDialog(getContext());
                dialog.setOnButtonClickListener(new ScheduledEditChooseDialog.OnButtonClickListener() {
                    @Override
                    public void onSendNowClick() {
                        MessageScheduleManager.cancelScheduledTask(mData.getMessageId());
                        SendScheduledMessageAction.sendMessage(mData.getMessageId());
                        dialog.dismiss();
                        BugleAnalytics.logEvent("Schedule_Message_Edit_Click", "button", "send");
                    }

                    @Override
                    public void onDeleteClick() {
                        final BaseAlertDialog.Builder builder = new BaseAlertDialog.Builder(getContext())
                                .setTitle(R.string.delete_message_confirmation_dialog_title)
                                .setMessage(R.string.delete_message_confirmation_dialog_text)
                                .setPositiveButton(R.string.delete_message_confirmation_button,
                                        (dialog12, which) -> {
                                            MessageScheduleManager.cancelScheduledTask(mData.getMessageId());
                                            DeleteMessageAction.deleteMessage(mData.getMessageId());
                                        })
                                .setNegativeButton(android.R.string.cancel, null);
                        builder.setOnDismissListener(dialog1 -> {
                        });
                        builder.show();
                        dialog.dismiss();
                        BugleAnalytics.logEvent("Schedule_Message_Edit_Click", "button", "delete");
                    }

                    @Override
                    public void onEditClick() {
                        mHost.onScheduledEditClick(mData, mData.getScheduledTime());
                        DeleteMessageAction.deleteMessage(mData.getMessageId());
                        MessageScheduleManager.cancelScheduledTask(mData.getMessageId());
                        dialog.dismiss();
                        BugleAnalytics.logEvent("Schedule_Message_Edit_Click", "button", "edit");
                    }
                });
                dialog.show();
                BugleAnalytics.logEvent("Schedule_Message_Edit_Show");
            });
        } else {
            mScheduledEditView.setVisibility(GONE);
        }

        final boolean metadataVisible = statusVisible
                || deliveredBadgeVisible || simNameVisible;
        mMessageMetadataView.setVisibility(metadataVisible ? VISIBLE : GONE);

        final boolean messageTextAndOrInfoVisible = mData.hasText();
        mMessageTextAndInfoView.setVisibility(
                messageTextAndOrInfoVisible ? View.VISIBLE : View.GONE);

        if (!shouldShowSimplifiedVisualStyle() && mData.getIsIncoming()) {
            mContactIconContainer.setVisibility(View.VISIBLE);
            final Uri avatarUri = AvatarUriUtil.createAvatarUri(
                    mData.getSenderProfilePhotoUri(),
                    mData.getSenderFullName(),
                    mData.getSenderNormalizedDestination(),
                    mData.getSenderContactLookupKey());
            mContactIconView.setImageResourceUri(avatarUri, mData.getSenderContactId(),
                    mData.getSenderContactLookupKey(), mData.getSenderNormalizedDestination());
            if (avatarUri != null
                    && AvatarUriUtil.TYPE_LOCAL_RESOURCE_URI.equals(AvatarUriUtil.getAvatarType(avatarUri))) {
                mContactIconBg.setVisibility(GONE);
            } else {
                mContactIconBg.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateMessageContent() {
        // We must update the text before the attachments since we search the text to see if we
        // should make a preview youtube image in the attachments
        updateMessageText();
        updateMessageAttachments();
        mMessageBubble.bind(mData);
    }

    private void updateMessageAttachments() {
        if (mMessageAttachmentsView == null) {
            return;
        }

        // Bind video, audio, and VCard attachments. If there are multiple, they stack vertically.
        bindAttachmentsOfSameType(sVideoFilter,
                R.layout.message_video_attachment, mVideoViewBinder, VideoThumbnailView.class);
        bindAttachmentsOfSameType(sAudioFilter,
                R.layout.message_audio_attachment, mAudioViewBinder, AudioAttachmentView.class);
        bindAttachmentsOfSameType(sVCardFilter,
                R.layout.message_vcard_attachment, mVCardViewBinder, AttachmentVCardItemView.class);

        // Bind image attachments. If there are multiple, they are shown in a collage view.
        final List<MessagePartData> imageParts = mData.getAttachments(sImageFilter);
        if (imageParts.size() > 1) {
            Collections.sort(imageParts, sImageComparator);
            mMultiAttachmentView.bindAttachments(imageParts, null, imageParts.size());
            mMultiAttachmentView.setVisibility(View.VISIBLE);
        } else {
            mMultiAttachmentView.setVisibility(View.GONE);
        }

        // In the case that we have no image attachments and exactly one youtube link in a message
        // then we will show a preview.

        // We will show the message image view if there is one attachment or one youtube link
        if (imageParts.size() == 1) {
            // Get the display metrics for a hint for how large to pull the image data into
            final WindowManager windowManager = (WindowManager) getContext().
                    getSystemService(Context.WINDOW_SERVICE);
            final DisplayMetrics displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);

            final int iconSize = getResources()
                    .getDimensionPixelSize(R.dimen.conversation_message_contact_icon_size);
            final int desiredWidth = displayMetrics.widthPixels - iconSize - iconSize;

            if (imageParts.size() == 1) {
                final MessagePartData imagePart = imageParts.get(0);
                // If the image is big, we want to scale it down to save memory since we're going to
                // scale it down to fit into the bubble width. We don't constrain the height.
                final ImageRequestDescriptor imageRequest =
                        new MessagePartImageRequestDescriptor(imagePart,
                                desiredWidth,
                                MessagePartData.UNSPECIFIED_SIZE,
                                false);
                adjustImageViewBounds(imagePart);
                mMessageImageView.setImageResourceId(imageRequest);
                mMessageImageView.setTag(imagePart);
            }
            mMessageImageView.setVisibility(View.VISIBLE);
        } else {
            mMessageImageView.setImageResourceId(null);
            mMessageImageView.setVisibility(View.GONE);
        }

        // Show the message attachments container if any of its children are visible
        boolean attachmentsVisible = false;
        for (int i = 0, size = mMessageAttachmentsView.getChildCount(); i < size; i++) {
            final View attachmentView = mMessageAttachmentsView.getChildAt(i);
            if (attachmentView.getVisibility() == View.VISIBLE) {
                attachmentsVisible = true;
                break;
            }
        }
        mMessageAttachmentsView.setVisibility(attachmentsVisible ? View.VISIBLE : View.GONE);
    }

    private void bindAttachmentsOfSameType(final Predicate<MessagePartData> attachmentTypeFilter,
                                           final int attachmentViewLayoutRes, final AttachmentViewBinder viewBinder,
                                           final Class<?> attachmentViewClass) {
        final LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        // Iterate through all attachments of a particular type (video, audio, etc).
        // Find the first attachment index that matches the given type if possible.
        int attachmentViewIndex = -1;
        View existingAttachmentView;
        do {
            existingAttachmentView = mMessageAttachmentsView.getChildAt(++attachmentViewIndex);
        } while (existingAttachmentView != null &&
                !(attachmentViewClass.isInstance(existingAttachmentView)));

        for (final MessagePartData attachment : mData.getAttachments(attachmentTypeFilter)) {
            View attachmentView = mMessageAttachmentsView.getChildAt(attachmentViewIndex);
            if (!attachmentViewClass.isInstance(attachmentView)) {
                attachmentView = layoutInflater.inflate(attachmentViewLayoutRes,
                        mMessageAttachmentsView, false /* attachToRoot */);
                attachmentView.setOnClickListener(this);
                attachmentView.setOnLongClickListener(this);
                mMessageAttachmentsView.addView(attachmentView, attachmentViewIndex);
            }
            viewBinder.bindView(attachmentView, attachment);
            attachmentView.setTag(attachment);
            attachmentView.setVisibility(View.VISIBLE);
            attachmentViewIndex++;
        }
        // If there are unused views left over, unbind or remove them.
        while (attachmentViewIndex < mMessageAttachmentsView.getChildCount()) {
            final View attachmentView = mMessageAttachmentsView.getChildAt(attachmentViewIndex);
            if (attachmentViewClass.isInstance(attachmentView)) {
                mMessageAttachmentsView.removeViewAt(attachmentViewIndex);
            } else {
                // No more views of this type; we're done.
                break;
            }
        }
    }

    private void updateMessageText() {
        final String text = mData.getText();
        if (!TextUtils.isEmpty(text)) {
            mMessageTextView.setText(text.trim());
            // Linkify phone numbers, web urls, emails, and map addresses to allow users to
            // click on them and take the default intent.
            try {
                Linkify.addLinks(mMessageTextView, Linkify.ALL);
            } catch (Exception e) {
                // catch crash: https://fabric.io/smsgroup/android/apps/com.color.sms.messages.emoji/issues/5c5616d4f8b88c296372d310?time=last-seven-days
                // ignore framework error
            }
            mMessageTextView.setVisibility(View.VISIBLE);
        } else {
            mMessageTextView.setVisibility(View.GONE);
        }
    }

    private void updateViewAppearance() {
        final Resources res = getResources();
        final ConversationDrawables drawableProvider = ConversationDrawables.get();
        final boolean incoming = mData.getIsIncoming();
        final boolean outgoing = !incoming;
        final boolean showArrow = shouldShowMessageBubbleArrow();

        final int messageTopPaddingClustered =
                res.getDimensionPixelSize(R.dimen.message_padding_same_author);
        final int messageTopPaddingDefault =
                res.getDimensionPixelSize(R.dimen.message_padding_default);
        final int arrowWidth = res.getDimensionPixelOffset(R.dimen.message_bubble_arrow_width);
        final int messageTextMinHeightDefault = res.getDimensionPixelSize(
                R.dimen.conversation_message_info_min_height);
        final int messageTextLeftRightPadding = res.getDimensionPixelOffset(
                R.dimen.message_text_left_right_padding);
        final int textTopPaddingDefault = res.getDimensionPixelOffset(
                R.dimen.message_text_top_padding);
        final int textBottomPaddingDefault = res.getDimensionPixelOffset(
                R.dimen.message_text_bottom_padding);

        // These values depend on whether the message has text, attachments, or both.
        // We intentionally don't set defaults, so the compiler will tell us if we forget
        // to set one of them, or if we set one more than once.
        final Drawable textBackground;
        final int textMinHeight;
        final int textTopMargin;
        final int textTopPadding, textBottomPadding;
        final int textLeftPadding, textRightPadding;

        if (mData.hasAttachments()) {
            if (shouldShowMessageTextBubble()) {
                // Text and attachment(s)
                textBackground = drawableProvider.getBubbleDrawable(
                        isSelected(),
                        incoming,
                        true /* needArrow */,
                        mData.hasIncomingErrorStatus(),
                        mData.getConversationId(), mHasCustomBackground);
                textMinHeight = messageTextMinHeightDefault;
                textTopMargin = messageTopPaddingClustered;
                textTopPadding = textTopPaddingDefault;
                textBottomPadding = textBottomPaddingDefault;
                textLeftPadding = messageTextLeftRightPadding;
                textRightPadding = messageTextLeftRightPadding;
            } else {
                // Attachment(s) only
                textBackground = null;
                textMinHeight = 0;
                textTopMargin = 0;
                textTopPadding = 0;
                textBottomPadding = 0;
                textLeftPadding = 0;
                textRightPadding = 0;
            }
        } else {
            // Text only
            textBackground = drawableProvider.getBubbleDrawable(
                    isSelected(),
                    incoming,
                    true,
                    mData.hasIncomingErrorStatus(),
                    mData.getConversationId(), mHasCustomBackground);
            textMinHeight = messageTextMinHeightDefault;
            textTopMargin = 0;
            textTopPadding = textTopPaddingDefault;
            textBottomPadding = textBottomPaddingDefault;
            if (showArrow && incoming) {
                textLeftPadding = messageTextLeftRightPadding;
            } else {
                textLeftPadding = messageTextLeftRightPadding;
            }
            if (showArrow && outgoing) {
                textRightPadding = messageTextLeftRightPadding;
            } else {
                textRightPadding = messageTextLeftRightPadding;
            }
        }

        // These values do not depend on whether the message includes attachments
        final int gravity = incoming ? (Gravity.START | Gravity.CENTER_VERTICAL) :
                (Gravity.END | Gravity.CENTER_VERTICAL);
        final int messageTopPadding = shouldShowSimplifiedVisualStyle() ?
                messageTopPaddingClustered : messageTopPaddingDefault;
        final int metadataTopPadding = res.getDimensionPixelOffset(
                R.dimen.message_metadata_top_padding);

        // Update the message text/info views
        ImageUtils.setBackgroundDrawableOnView(mMessageTextAndInfoView, textBackground);
        if (Dimensions.isRtl()) {
            int leftPadding = mMessageTextAndInfoView.getPaddingLeft();
            int rightPadding = mMessageTextAndInfoView.getPaddingRight();
            int topPadding = mMessageTextAndInfoView.getPaddingTop();
            int bottomPadding = mMessageTextAndInfoView.getPaddingBottom();
            mMessageTextAndInfoView.setPadding(rightPadding, topPadding,
                    leftPadding, bottomPadding);
        }
        mMessageTextAndInfoView.setMinimumHeight(textMinHeight);

        // Update the message row and message bubble views
        setPadding(getPaddingLeft(), messageTopPadding, getPaddingRight(), 0);
        mMessageBubble.setGravity(gravity);
        ((LinearLayout) findViewById(R.id.message_text_and_scheduled_edit_container)).setGravity(gravity);
        updateMessageAttachmentsAppearance(gravity);

        mMessageMetadataView.setPadding(0, metadataTopPadding, 0, 0);

        updateTextAppearance();

        requestLayout();
    }

    private void updateMessageAttachmentsAppearance(final int gravity) {
        if (mMessageAttachmentsView == null) {
            return;
        }
        mMessageAttachmentsView.setGravity(gravity);

        // Tint image/video attachments when selected
        final int selectedImageTint = getResources().getColor(R.color.message_image_selected_tint);
        if (mMessageImageView.getVisibility() == View.VISIBLE) {
            if (isSelected()) {
                mMessageImageView.setColorFilter(selectedImageTint);
            } else {
                mMessageImageView.clearColorFilter();
            }
        }
        if (mMultiAttachmentView.getVisibility() == View.VISIBLE) {
            if (isSelected()) {
                mMultiAttachmentView.setColorFilter(selectedImageTint);
            } else {
                mMultiAttachmentView.clearColorFilter();
            }
        }
        for (int i = 0, size = mMessageAttachmentsView.getChildCount(); i < size; i++) {
            final View attachmentView = mMessageAttachmentsView.getChildAt(i);
            if (attachmentView instanceof VideoThumbnailView
                    && attachmentView.getVisibility() == View.VISIBLE) {
                final VideoThumbnailView videoView = (VideoThumbnailView) attachmentView;
                if (isSelected()) {
                    videoView.setColorFilter(selectedImageTint);
                } else {
                    videoView.clearColorFilter();
                }
            }
        }

        // If there are multiple attachment bubbles in a single message, add some separation.
        final int multipleAttachmentPadding =
                getResources().getDimensionPixelSize(R.dimen.message_padding_same_author);

        boolean previousVisibleView = false;
        for (int i = 0, size = mMessageAttachmentsView.getChildCount(); i < size; i++) {
            final View attachmentView = mMessageAttachmentsView.getChildAt(i);
            if (attachmentView.getVisibility() == View.VISIBLE) {
                final int margin = previousVisibleView ? multipleAttachmentPadding : 0;
                ((LinearLayout.LayoutParams) attachmentView.getLayoutParams()).topMargin = margin;
                // updateViewAppearance calls requestLayout() at the end, so we don't need to here
                previousVisibleView = true;
            }
        }
    }

    private void updateTextAppearance() {
        int messageColor;
        int timestampColorResId;
        boolean hasWallPaper = WallpaperManager.hasWallpaper(mData.getConversationId());

        Resources resources = getResources();
        messageColor = ConversationColors.get().getMessageTextColor(mData.getIsIncoming(), mData.getConversationId());
        if (isSelected()) {
            if (shouldShowMessageTextBubble()) {
                timestampColorResId = R.color.message_action_timestamp_text;
            } else {
                // If there's no text, the timestamp will be shown below the attachments,
                // against the conversation view background.
                timestampColorResId = R.color.timestamp_text_outgoing;
            }
        } else {
            switch (mData.getStatus()) {

                case MessageData.BUGLE_STATUS_OUTGOING_SCHEDULED_FAILED:
                case MessageData.BUGLE_STATUS_OUTGOING_FAILED:
                case MessageData.BUGLE_STATUS_OUTGOING_FAILED_EMERGENCY_NUMBER:
                    timestampColorResId = R.color.message_failed_timestamp_text;
                    break;

                case MessageData.BUGLE_STATUS_OUTGOING_YET_TO_SEND:
                case MessageData.BUGLE_STATUS_OUTGOING_SENDING:
                case MessageData.BUGLE_STATUS_OUTGOING_RESENDING:
                case MessageData.BUGLE_STATUS_OUTGOING_AWAITING_RETRY:
                case MessageData.BUGLE_STATUS_OUTGOING_COMPLETE:
                case MessageData.BUGLE_STATUS_OUTGOING_DELIVERED:
                    if (hasWallPaper) {
                        timestampColorResId = R.color.white;
                    } else {
                        timestampColorResId = R.color.timestamp_text_outgoing;
                    }
                    break;

                case MessageData.BUGLE_STATUS_INCOMING_EXPIRED_OR_NOT_AVAILABLE:
                case MessageData.BUGLE_STATUS_INCOMING_DOWNLOAD_FAILED:
                    messageColor = getResources().getColor(R.color.message_text_color_incoming_download_failed);
                    timestampColorResId = R.color.message_download_failed_timestamp_text;
                    break;

                case MessageData.BUGLE_STATUS_INCOMING_AUTO_DOWNLOADING:
                case MessageData.BUGLE_STATUS_INCOMING_MANUAL_DOWNLOADING:
                case MessageData.BUGLE_STATUS_INCOMING_RETRYING_AUTO_DOWNLOAD:
                case MessageData.BUGLE_STATUS_INCOMING_RETRYING_MANUAL_DOWNLOAD:
                case MessageData.BUGLE_STATUS_INCOMING_YET_TO_MANUAL_DOWNLOAD:
                    if (hasWallPaper) {
                        timestampColorResId = R.color.white;
                    } else {
                        timestampColorResId = R.color.timestamp_text_incoming;
                    }
                    break;

                case MessageData.BUGLE_STATUS_INCOMING_COMPLETE:
                default:
                    if (hasWallPaper) {
                        timestampColorResId = R.color.white;
                    } else {
                        timestampColorResId = R.color.timestamp_text_incoming;
                    }
                    break;
            }
        }
        mMessageTextView.setTextColor(messageColor);
        mMessageTextView.setLinkTextColor(messageColor);
        if (timestampColorResId == R.color.timestamp_text_incoming &&
                mData.hasAttachments() && !shouldShowMessageTextBubble()) {
            timestampColorResId = R.color.timestamp_text_outgoing;
        }

        if (!hasWallPaper) {
            mStatusContainer.setBackground(null);
        }
        if (mData.getIsLocked()) {
            if (!hasWallPaper) {
                mMessageIsLockView.setImageResource(R.drawable.message_lock_default);
            } else {
                mMessageIsLockView.setImageResource(R.drawable.message_lock_theme);
            }
        }

        mStatusTextView.setTextColor(resources.getColor(timestampColorResId));
    }

    /**
     * If we don't know the size of the image, we want to show it in a fixed-sized frame to
     * avoid janks when the image is loaded and resized. Otherwise, we can set the imageview to
     * take on normal layout params.
     */
    private void adjustImageViewBounds(final MessagePartData imageAttachment) {
        Assert.isTrue(ContentType.isImageType(imageAttachment.getContentType()));
        final ViewGroup.LayoutParams layoutParams = mMessageImageView.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        // ScaleType.CENTER_INSIDE and FIT_CENTER behave similarly for most images. However,
        // FIT_CENTER works better for small images as it enlarges the image such that the
        // minimum size ("android:minWidth" etc) is honored.
        mMessageImageView.setScaleType(ScaleType.FIT_CENTER);
    }

    @Override
    public void onClick(final View view) {
        final Object tag = view.getTag();
        if (tag instanceof MessagePartData) {
            final Rect bounds = UiUtils.getMeasuredBoundsOnScreen(view);
            onAttachmentClick((MessagePartData) tag, bounds, false /* longPress */);
        } else if (tag instanceof String) {
            // Currently the only object that would make a tag of a string is a youtube preview
            // image
            UIIntents.get().launchBrowserForUrl(getContext(), (String) tag);
        }
    }

    @Override
    public boolean onLongClick(final View view) {
        if (view == mMessageTextView) {
            // Preemptively handle the long click event on message text so it's not handled by
            // the link spans.
            return performLongClick();
        }

        final Object tag = view.getTag();
        if (tag instanceof MessagePartData) {
            final Rect bounds = UiUtils.getMeasuredBoundsOnScreen(view);
            return onAttachmentClick((MessagePartData) tag, bounds, true /* longPress */);
        }

        return false;
    }

    @Override
    public boolean onAttachmentClick(final MessagePartData attachment,
                                     final Rect viewBoundsOnScreen, final boolean longPress) {
        return mHost.onAttachmentClick(this.getData(), attachment, viewBoundsOnScreen, longPress);
    }

    // Sort photos in MultiAttachLayout in the same order as the ConversationImagePartsView
    static final Comparator<MessagePartData> sImageComparator = new Comparator<MessagePartData>() {
        @Override
        public int compare(final MessagePartData x, final MessagePartData y) {
            return x.getPartId().compareTo(y.getPartId());
        }
    };

    static final Predicate<MessagePartData> sVideoFilter = new Predicate<MessagePartData>() {
        @Override
        public boolean apply(final MessagePartData part) {
            return part.isVideo();
        }
    };

    static final Predicate<MessagePartData> sAudioFilter = new Predicate<MessagePartData>() {
        @Override
        public boolean apply(final MessagePartData part) {
            return part.isAudio();
        }
    };

    static final Predicate<MessagePartData> sVCardFilter = new Predicate<MessagePartData>() {
        @Override
        public boolean apply(final MessagePartData part) {
            return part.isVCard();
        }
    };

    static final Predicate<MessagePartData> sImageFilter = new Predicate<MessagePartData>() {
        @Override
        public boolean apply(final MessagePartData part) {
            return part.isImage();
        }
    };

    interface AttachmentViewBinder {
        void bindView(View view, MessagePartData attachment);

        void unbind(View view);
    }

    final AttachmentViewBinder mVideoViewBinder = new AttachmentViewBinder() {
        @Override
        public void bindView(final View view, final MessagePartData attachment) {
            ((VideoThumbnailView) view).setSource(attachment, mData.getIsIncoming());
        }

        @Override
        public void unbind(final View view) {
            ((VideoThumbnailView) view).setSource((Uri) null, mData.getIsIncoming());
        }
    };

    final AttachmentViewBinder mAudioViewBinder = new AttachmentViewBinder() {
        @Override
        public void bindView(final View view, final MessagePartData attachment) {
            final AudioAttachmentView audioView = (AudioAttachmentView) view;
            audioView.bindMessagePartData(attachment, mData.getIsIncoming(), isSelected());
            audioView.setBackground(ConversationDrawables.get().getAudioBackgroundDrawable(
                    isSelected(), mData.getIsIncoming(), mData.hasIncomingErrorStatus()));
        }

        @Override
        public void unbind(final View view) {
            ((AudioAttachmentView) view).bindMessagePartData(null, mData.getIsIncoming(), false);
        }
    };

    final AttachmentViewBinder mVCardViewBinder = new AttachmentViewBinder() {
        @Override
        public void bindView(final View view, final MessagePartData attachment) {
            final AttachmentVCardItemView personView = (AttachmentVCardItemView) view;
            personView.bind(DataModel.get().createVCardContactItemData(getContext(),
                    attachment));
        }

        @Override
        public void unbind(final View view) {
            ((PersonItemView) view).bind(null);
        }
    };

    /**
     * A helper class that allows us to handle long clicks on linkified message text view (i.e. to
     * select the message) so it's not handled by the link spans to launch apps for the links.
     */
    private static class IgnoreLinkLongClickHelper implements OnLongClickListener, OnTouchListener {
        private boolean mIsLongClick;
        private final OnLongClickListener mDelegateLongClickListener;

        /**
         * Ignore long clicks on linkified texts for a given text view.
         *
         * @param textView          the TextView to ignore long clicks on
         * @param longClickListener a delegate OnLongClickListener to be called when the view is
         *                          long clicked.
         */
        public static void ignoreLinkLongClick(final TextView textView,
                                               @Nullable final OnLongClickListener longClickListener) {
            final IgnoreLinkLongClickHelper helper =
                    new IgnoreLinkLongClickHelper(longClickListener);
            textView.setOnLongClickListener(helper);
            textView.setOnTouchListener(helper);
        }

        private IgnoreLinkLongClickHelper(@Nullable final OnLongClickListener longClickListener) {
            mDelegateLongClickListener = longClickListener;
        }

        @Override
        public boolean onLongClick(final View v) {
            // Record that this click is a long click.
            mIsLongClick = true;
            if (mDelegateLongClickListener != null) {
                return mDelegateLongClickListener.onLongClick(v);
            }
            return false;
        }

        @Override
        public boolean onTouch(final View v, final MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_UP && mIsLongClick) {
                // This touch event is a long click, preemptively handle this touch event so that
                // the link span won't get a onClicked() callback.
                mIsLongClick = false;
                return true;
            }

            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mIsLongClick = false;
            }
            return false;
        }
    }

    public void open() {
        if (mData.getIsIncoming()) {
            mMessageBubble.scrollTo(0, 0);
        } else {
            if (Dimensions.isRtl()) {
                mMessageBubble.scrollTo(-mOffset, 0);
            } else {
                mMessageBubble.scrollTo(mOffset, 0);
            }
        }
    }

    public void close() {
        mMessageBubble.scrollTo(0, 0);
    }
}
