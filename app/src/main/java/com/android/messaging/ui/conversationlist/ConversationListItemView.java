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

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.annotation.VisibleForAnimation;
import com.android.messaging.datamodel.action.UpdateConversationArchiveStatusAction;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.font.FontUtils;
import com.android.messaging.sms.MmsUtils;
import com.android.messaging.ui.ContactIconView;
import com.android.messaging.ui.ConversationDrawables;
import com.android.messaging.ui.SnackBar;
import com.android.messaging.ui.SnackBarInteraction;
import com.android.messaging.ui.customize.AvatarBgDrawables;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.mainpage.ChatListCustomizeManager;
import com.android.messaging.ui.customize.mainpage.ChatListUtils;
import com.android.messaging.util.Assert;
import com.android.messaging.util.AvatarUriUtil;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.ContentType;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.UiUtils;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.util.List;

import hugo.weaving.DebugLog;

/**
 * The view for a single entry in a conversation list.
 */
public class ConversationListItemView extends FrameLayout implements OnClickListener,
        OnLongClickListener, OnLayoutChangeListener {
    static final int SNIPPET_LINE_COUNT = 1;
    static final int ERROR_MESSAGE_LINE_COUNT = 1;
    private int mConversationNameColor;
    private int mSnippetColor;
    private int mTimestampColor;
    private static String sPlusOneString;
    private static String sPlusNString;
    private boolean isLastMutiMode = false;

    public interface HostInterface {
        boolean isConversationSelected(final String conversationId);

        void onConversationClicked(final ConversationListItemData conversationListItemData,
                                   boolean isLongClick, final ConversationListItemView conversationView);

        boolean isSwipeAnimatable();

        List<SnackBarInteraction> getSnackBarInteractions();

        boolean isSelectionMode();

        boolean isArchived();
    }

    private ConversationListItemData mData;

    private int mAnimatingCount;
    private ViewGroup mSwipeableContainer;
    private TextView mConversationNameView;
    private TextView mSnippetTextView;
    private TextView mTimestampTextView;
    private ContactIconView mContactIconView;
    private ImageView mContactBackground;
    private ImageView mFailedStatusIconView;

    private View mCrossSwipeArchiveLeftContainer;
    private View mCrossSwipeArchiveRightContainer;
    private View mCrossSwipeBg;
    private HostInterface mHostInterface;
    private TextView mUnreadMessagesCountView;

    private boolean mIsFirstBind = true;

    public ConversationListItemView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mData = new ConversationListItemData();
        final Resources res = context.getResources();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSwipeableContainer = findViewById(R.id.conversation_item_swipeable_container);
        mConversationNameView = findViewById(R.id.conversation_name);
        FrameLayout.LayoutParams params = (LayoutParams) mConversationNameView.getLayoutParams();
        params.width = Dimensions.getPhoneWidth(HSApplication.getContext())
                - getResources().getDimensionPixelSize(R.dimen.conversation_list_item_content_margin_start)
                - Dimensions.pxFromDp(65);
        mConversationNameView.setLayoutParams(params);
        mSnippetTextView = findViewById(R.id.conversation_snippet);
        FrameLayout.LayoutParams paramSnippet = (LayoutParams) mSnippetTextView.getLayoutParams();
        paramSnippet.width = Dimensions.getPhoneWidth(HSApplication.getContext())
                - getResources().getDimensionPixelSize(R.dimen.conversation_list_item_content_margin_start)
                - Dimensions.pxFromDp(65);
        mSnippetTextView.setLayoutParams(paramSnippet);
        mTimestampTextView = findViewById(R.id.conversation_timestamp);
        mContactIconView = findViewById(R.id.conversation_icon);
        mFailedStatusIconView = findViewById(R.id.conversation_failed_status_icon);

        mCrossSwipeArchiveLeftContainer = findViewById(R.id.cross_swipe_archive_left_container);
        mCrossSwipeArchiveRightContainer = findViewById(R.id.cross_swipe_archive_right_container);

        mCrossSwipeBg = findViewById(R.id.cross_swipe_archive_background);
        mCrossSwipeBg.setBackgroundColor(PrimaryColors.getPrimaryColor());

        mUnreadMessagesCountView = findViewById(R.id.conversation_unread_messages_count);

        mConversationNameView.addOnLayoutChangeListener(this);
        mSnippetTextView.addOnLayoutChangeListener(this);

        mConversationNameColor = ConversationColors.get().getListTitleColor();
        mSnippetColor = ConversationColors.get().getListSubtitleColor();
        mTimestampColor = ConversationColors.get().getListTimeColor();

        mContactBackground = findViewById(R.id.conversation_icon_bg);
        mContactBackground.setImageDrawable(AvatarBgDrawables.getAvatarBg(false, ChatListCustomizeManager.hasCustomWallpaper()));

        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(200);
        //layoutTransition.disableTransitionType(LayoutTransition.DISAPPEARING);
        layoutTransition.setAnimateParentHierarchy(false);
        mSwipeableContainer.setLayoutTransition(layoutTransition);

        if (OsUtil.isAtLeastL()) {
            setTransitionGroup(true);
        }
    }

    @Override
    public void onLayoutChange(final View v, final int left, final int top, final int right,
                               final int bottom, final int oldLeft, final int oldTop, final int oldRight,
                               final int oldBottom) {
        if (v == mConversationNameView) {
            setConversationName();
            addSpannable();
            setContactImage();
        } else if (v == mSnippetTextView) {
            setSnippet();
        }
    }

    private void setConversationName() {
        mConversationNameView.setTextColor(mConversationNameColor);
        ChatListCustomizeManager.changeViewColorIfNeed(mConversationNameView);
        ChatListUtils.changeTextViewShadow(mConversationNameView);
        if (mData.getIsRead() || mData.getShowDraft()) {
            mConversationNameView.setTypeface(FontUtils.getTypeface(FontUtils.MEDIUM));
        } else {
            mConversationNameView.setTypeface(FontUtils.getTypeface(FontUtils.SEMI_BOLD));
        }

        final String conversationName = mData.getName();

        if (TextUtils.isEmpty(conversationName)) {
            mConversationNameView.setText("");
            return;
        }

        // For group conversations, ellipsize the group members that do not fit
        final CharSequence ellipsizedName = UiUtils.commaEllipsize(
                conversationName,
                mConversationNameView.getPaint(),
                mConversationNameView.getMeasuredWidth(),
                getPlusOneString(),
                getPlusNString());
        mConversationNameView.setText(ellipsizedName);
    }

    private void setContactImage() {
        Uri iconUri = null;
        String imgUri = mData.getIcon();
        if (!mData.getIsRead()) {
            //unread
            if (!TextUtils.isEmpty(imgUri)) {
                imgUri = imgUri.concat("unread");
            }
        }
        if (!TextUtils.isEmpty(imgUri)) {
            iconUri = Uri.parse(imgUri);
            String iconType = AvatarUriUtil.getAvatarType(iconUri);
            if (AvatarUriUtil.TYPE_LOCAL_RESOURCE_URI.equals(iconType)) {
                mContactBackground.setImageDrawable(null);
            } else {
                mContactBackground.setImageDrawable(AvatarBgDrawables.getAvatarBg(false, ChatListCustomizeManager.hasCustomWallpaper()));
            }
        } else {
            mContactBackground.setImageDrawable(AvatarBgDrawables.getAvatarBg(false, ChatListCustomizeManager.hasCustomWallpaper()));
        }
        mContactIconView.setImageResourceUri(iconUri, mData.getParticipantContactId(),
                mData.getParticipantLookupKey(), mData.getOtherParticipantNormalizedDestination(), Color.TRANSPARENT);
    }

    private static String getPlusOneString() {
        if (sPlusOneString == null) {
            sPlusOneString = Factory.get().getApplicationContext().getResources()
                    .getString(R.string.plus_one);
        }
        return sPlusOneString;
    }

    private static String getPlusNString() {
        if (sPlusNString == null) {
            sPlusNString = Factory.get().getApplicationContext().getResources()
                    .getString(R.string.plus_n);
        }
        return sPlusNString;
    }

    private void setSnippet() {
        if (mData.getIsFailedStatus()) {
            int failureMessageId = R.string.message_status_download_failed;

            if (mData.getIsMessageTypeOutgoing()) {
                failureMessageId = MmsUtils.mapRawStatusToErrorResourceId(mData.getMessageStatus(),
                        mData.getMessageRawTelephonyStatus());
            }
            mSnippetTextView.setText(getContext().getResources().getString(failureMessageId));
        } else {
            mSnippetTextView.setText(getSnippetText());
        }
    }

    /**
     * Fills in the data associated with this view.
     */
    @DebugLog
    public void bind(final ConversationListItemData data, final HostInterface hostInterface) {
        // Update our UI model
        mHostInterface = hostInterface;
        mData = data;

        resetAnimatingState();

        findViewById(R.id.conversation_item_ripple_view).setOnClickListener(this);
        findViewById(R.id.conversation_item_ripple_view).setOnLongClickListener(this);

        if (mIsFirstBind) {
            if (!mHostInterface.isArchived()) {
                ((ImageView) findViewById(R.id.cross_swipe_archive_icon_left))
                        .setImageDrawable(ConversationDrawables.get().getArchiveSwipeDrawable());
                ((ImageView) findViewById(R.id.cross_swipe_archive_icon_right))
                        .setImageDrawable(ConversationDrawables.get().getArchiveSwipeDrawable());
                ((TextView) findViewById(R.id.cross_swipe_archive_text_left)).setText(R.string.action_archive);
                ((TextView) findViewById(R.id.cross_swipe_archive_text_right)).setText(R.string.action_archive);
            } else {
                ((ImageView) findViewById(R.id.cross_swipe_archive_icon_left))
                        .setImageDrawable(ConversationDrawables.get().getUnarchiveSwipeDrawable());
                ((ImageView) findViewById(R.id.cross_swipe_archive_icon_right))
                        .setImageDrawable(ConversationDrawables.get().getUnarchiveSwipeDrawable());
                ((TextView) findViewById(R.id.cross_swipe_archive_text_left)).setText(R.string.action_unarchive);
                ((TextView) findViewById(R.id.cross_swipe_archive_text_right)).setText(R.string.action_unarchive);
            }
            mIsFirstBind = false;
        }

        final Resources resources = getContext().getResources();

        int color;
        final int maxLines;
        final String snippetText = getSnippetText();

        if (mData.getIsFailedStatus()) {
            color = resources.getColor(R.color.conversation_list_error);
            maxLines = ERROR_MESSAGE_LINE_COUNT;
            mSnippetTextView.setTextColor(color);
            ChatListUtils.changeTextViewShadow(mSnippetTextView, false);
        } else {
            maxLines = TextUtils.isEmpty(snippetText) ? 0 : SNIPPET_LINE_COUNT;
            color = mSnippetColor;
            mSnippetTextView.setTextColor(color);
            ChatListCustomizeManager.changeViewColorIfNeed(mSnippetTextView);
            ChatListUtils.changeTextViewShadow(mSnippetTextView);
        }

        mSnippetTextView.setMaxLines(maxLines);

        mTimestampTextView.setTextColor(mTimestampColor);
        ChatListCustomizeManager.changeViewColorIfNeed(mTimestampTextView);
        ChatListUtils.changeTextViewShadow(mTimestampTextView);

        setSnippet();
        setConversationName();
        addSpannable();


        if (mData.getShowDraft()
                || mData.getMessageStatus() == MessageData.BUGLE_STATUS_OUTGOING_DRAFT
                // also check for unknown status which we get because sometimes the conversation
                // row is left with a latest_message_id of a no longer existing message and
                // therefore the join values come back as null (or in this case zero).
                || mData.getMessageStatus() == MessageData.BUGLE_STATUS_UNKNOWN) {
            mTimestampTextView.setText(resources.getString(
                    R.string.conversation_list_item_view_draft_message));
        } else {
            final String formattedTimestamp = mData.getFormattedTimestamp();
            if (mData.getIsSendRequested()) {
                mTimestampTextView.setText(R.string.message_status_sending);
            } else {
                mTimestampTextView.setText(formattedTimestamp);
            }
        }

        mTimestampTextView.setTypeface(FontUtils.getTypeface());

        final boolean isSelected = mHostInterface.isConversationSelected(mData.getConversationId());
        setSelected(isSelected);

        ImageView checkbox = findViewById(R.id.check_box);

        if (mHostInterface.isSelectionMode()) {
            checkbox.setVisibility(View.VISIBLE);
            if (isSelected) {
                checkbox.setImageResource(R.drawable.conversation_check);
                checkbox.setBackground(BackgroundDrawables.createBackgroundDrawable(
                        PrimaryColors.getPrimaryColor(), Dimensions.pxFromDp(20), false));
            } else {
                checkbox.setImageDrawable(null);
                checkbox.setBackground(BackgroundDrawables.createBackgroundDrawable(0, 0, 4,
                        0xffbdc2c9, Dimensions.pxFromDp(20), false, false));
            }
            mTimestampTextView.setVisibility(GONE);
            mUnreadMessagesCountView.setVisibility(GONE);

            if (!isLastMutiMode) {
                isLastMutiMode = true;
            }
        } else {
            checkbox.setVisibility(View.GONE);
            mTimestampTextView.setVisibility(VISIBLE);

            boolean shouldShowUnreadMsgCount = mData.getUnreadMessagesNumber() > 0;
            int unreadMsgCountViewVisibility = shouldShowUnreadMsgCount ? VISIBLE : GONE;

            mUnreadMessagesCountView.setVisibility(unreadMsgCountViewVisibility);
            if (unreadMsgCountViewVisibility == VISIBLE) {
                mUnreadMessagesCountView.setBackground(
                        BackgroundDrawables.createBackgroundDrawable(0xffe35353,
                                Dimensions.pxFromDp(8.5f), false));
                mUnreadMessagesCountView.setText(String.valueOf(mData.getUnreadMessagesNumber()));
            }
        }

        int failStatusVisibility = GONE;
        // Only show the fail icon if it is not a group conversation.
        // And also require that we be the default sms app.
        if (mData.getIsFailedStatus() && !mData.getIsGroup()) {
            failStatusVisibility = VISIBLE;
        }

        setContactImage();
        mContactIconView.clearColorFilter();

        mFailedStatusIconView.setVisibility(failStatusVisibility);
    }

    private void addSpannable() {
        SpannableString sp = new SpannableString(mConversationNameView.getText());
        int bj = 0;
        if (mData.isPinned()) {
            sp = new SpannableString("  " + sp);
            bj++;
        }
        if (!mData.getNotificationEnabled()) {
            sp = new SpannableString("  " + sp);
            bj++;
        }
        // multiple imageSpans must be set at a invariable spannable
        if (mData.isPinned()) {
            Drawable pinDrawable = getResources().getDrawable(R.drawable.ic_small_pin);
            pinDrawable.setBounds(0, 0, pinDrawable.getIntrinsicWidth(), pinDrawable.getIntrinsicHeight());
            pinDrawable.setColorFilter(
                    ConversationColors.get().getListTimeColor(), PorterDuff.Mode.SRC_ATOP);
            CenteredImageSpan span = new CenteredImageSpan(pinDrawable);
            sp.setSpan(span, bj * 2 - 2, bj * 2 - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            bj--;
        }
        if (!mData.getNotificationEnabled()) {
            Drawable muteDrawable = getResources().getDrawable(R.drawable.ic_small_mute);
            muteDrawable.setBounds(0, 0, muteDrawable.getIntrinsicWidth(), muteDrawable.getIntrinsicHeight());
            muteDrawable.setColorFilter(
                    ConversationColors.get().getListTimeColor(), PorterDuff.Mode.SRC_ATOP);
            CenteredImageSpan span = new CenteredImageSpan(muteDrawable);
            sp.setSpan(span, bj * 2 - 2, bj * 2 - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            bj--;
        }
        mConversationNameView.setText(sp);
    }

    public boolean isSwipeAnimatable() {
        return mHostInterface.isSwipeAnimatable();
    }

    @VisibleForAnimation
    public float getSwipeTranslationX() {
        return mSwipeableContainer.getTranslationX();
    }

    @VisibleForAnimation
    public void setSwipeTranslationX(final float translationX) {
        mSwipeableContainer.setTranslationX(translationX);
        if (translationX == 0) {
            mCrossSwipeArchiveLeftContainer.setVisibility(GONE);
            mCrossSwipeArchiveRightContainer.setVisibility(GONE);
            mCrossSwipeBg.setVisibility(INVISIBLE);
            mCrossSwipeBg.setTranslationX(-getWidth());

            //mSwipeableContainer.setBackgroundResource(R.drawable.conversation_list_item_bg);
        } else {
            int padding = getResources().getDimensionPixelSize(R.dimen.conversation_item_view_swipe_padding);
            if (translationX > 0) {
                mCrossSwipeBg.setVisibility(VISIBLE);
                mCrossSwipeBg.setTranslationX(translationX - getWidth());
                if (translationX > padding) {
                    mCrossSwipeArchiveLeftContainer.setVisibility(VISIBLE);
                    Rect rect = new Rect(0, 0, (int) translationX - padding,
                            mCrossSwipeArchiveLeftContainer.getHeight());
                    mCrossSwipeArchiveLeftContainer.setClipBounds(rect);
                } else {
                    mCrossSwipeArchiveLeftContainer.setVisibility(GONE);
                }

                mCrossSwipeArchiveRightContainer.setVisibility(GONE);
            } else {
                mCrossSwipeBg.setVisibility(VISIBLE);
                mCrossSwipeBg.setTranslationX(getWidth() + translationX);

                if (-translationX > padding) {
                    mCrossSwipeArchiveRightContainer.setVisibility(VISIBLE);

                    Rect rect = new Rect(
                            (int) (mCrossSwipeArchiveRightContainer.getWidth() + padding + translationX),
                            0,
                            mCrossSwipeArchiveRightContainer.getWidth(),
                            mCrossSwipeArchiveRightContainer.getHeight());

                    mCrossSwipeArchiveRightContainer.setClipBounds(rect);
                } else {
                    mCrossSwipeArchiveRightContainer.setVisibility(GONE);
                }

                mCrossSwipeArchiveLeftContainer.setVisibility(GONE);
            }
        }
    }

    public void onSwipeComplete(boolean isLeft) {
        final String conversationId = mData.getConversationId();
        if (mHostInterface.isArchived()) {
            UpdateConversationArchiveStatusAction.unarchiveConversation(conversationId);
            BugleAnalytics.logEvent("SMS_Messages_Unarchive", true, "from",
                    isLeft ? "slide_left" : "slide_right");
        } else {
            UpdateConversationArchiveStatusAction.archiveConversation(conversationId);
            BugleAnalytics.logEvent("SMS_Messages_Archive", true, "from",
                    isLeft ? "slide_left" : "slide_right");
        }

        final int textId = !mHostInterface.isArchived() ? R.string.archived_toast_message : R.string.unarchived_toast_message;
        final Runnable undoRunnable = () -> {
            if (mHostInterface.isArchived()) {
                UpdateConversationArchiveStatusAction.archiveConversation(conversationId);
                BugleAnalytics.logEvent("SMS_Messages_Unarchive_Undo", true);
            } else {
                UpdateConversationArchiveStatusAction.unarchiveConversation(conversationId);
                BugleAnalytics.logEvent("SMS_Messages_Archive_Undo", true);
            }
        };
        final String message = getResources().getString(textId, 1);
        UiUtils.showSnackBar(UiUtils.getActivity(this), getRootView(), message, undoRunnable,
                SnackBar.Action.SNACK_BAR_UNDO,
                mHostInterface.getSnackBarInteractions());
    }

    private void setShortAndLongClickable(final boolean clickable) {
        setClickable(clickable);
        setLongClickable(clickable);
    }

    private void resetAnimatingState() {
        mAnimatingCount = 0;
        setShortAndLongClickable(true);
        setSwipeTranslationX(0);
    }

    /**
     * Notifies this view that it is undergoing animation. This view should disable its click
     * targets.
     * <p>
     * The animating counter is used to reset the swipe controller when the counter becomes 0. A
     * positive counter also makes the view not clickable.
     */
    public final void setAnimating(final boolean animating) {
        final int oldAnimatingCount = mAnimatingCount;
        if (animating) {
            mAnimatingCount++;
        } else {
            mAnimatingCount--;
            if (mAnimatingCount < 0) {
                mAnimatingCount = 0;
            }
        }

        if (mAnimatingCount == 0) {
            // New count is 0. All animations ended.
            setShortAndLongClickable(true);
        } else if (oldAnimatingCount == 0) {
            // New count is > 0. Waiting for some animations to end.
            setShortAndLongClickable(false);
        }
    }

    public boolean isAnimating() {
        return mAnimatingCount > 0;
    }

    /**
     * {@inheritDoc} from OnClickListener
     */
    @Override
    public void onClick(final View v) {
        ConversationListActivity.logFirstComeInClickEvent("messages");
        processClick(v, false);
    }

    /**
     * {@inheritDoc} from OnLongClickListener
     */
    @Override
    public boolean onLongClick(final View v) {
        ConversationListActivity.logFirstComeInClickEvent("longpress");
        return processClick(v, true);
    }

    private boolean processClick(final View v, final boolean isLongClick) {
        Assert.notNull(mData.getName());

        if (mHostInterface != null) {
            mHostInterface.onConversationClicked(mData, isLongClick, this);
            return true;
        }
        return false;
    }

    private String getSnippetText() {
        String snippetText = mData.getShowDraft() ?
                mData.getDraftSnippetText() : mData.getSnippetText();
        final String previewContentType = mData.getShowDraft() ?
                mData.getDraftPreviewContentType() : mData.getPreviewContentType();
        if (TextUtils.isEmpty(snippetText)) {
            Resources resources = getResources();
            // Use the attachment type as a snippet so the preview doesn't look odd
            if (ContentType.isAudioType(previewContentType)) {
                snippetText = resources.getString(R.string.conversation_list_snippet_audio_clip);
            } else if (ContentType.isImageType(previewContentType)) {
                snippetText = resources.getString(R.string.conversation_list_snippet_picture);
            } else if (ContentType.isVideoType(previewContentType)) {
                snippetText = resources.getString(R.string.conversation_list_snippet_video);
            } else if (ContentType.isVCardType(previewContentType)) {
                snippetText = resources.getString(R.string.conversation_list_snippet_vcard);
            } else if (mData.getMessageStatus() == MessageData.BUGLE_STATUS_INCOMING_YET_TO_MANUAL_DOWNLOAD) {
                snippetText = resources.getString(R.string.mms_text);
            }
        } else {
            snippetText = snippetText.trim().replace("\n", " ").replace("\r", " ");
        }
        return snippetText;
    }
}
