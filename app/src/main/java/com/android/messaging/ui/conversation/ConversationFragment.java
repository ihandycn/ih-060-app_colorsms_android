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

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.text.BidiFormatter;
import android.support.v4.text.TextDirectionHeuristicsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.ActionMode;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ad.AdConfig;
import com.android.messaging.ad.AdPlacement;
import com.android.messaging.ad.BillingManager;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.MessagingContentProvider;
import com.android.messaging.datamodel.binding.Binding;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.binding.ImmutableBindingRef;
import com.android.messaging.datamodel.data.ConversationData;
import com.android.messaging.datamodel.data.ConversationData.ConversationDataListener;
import com.android.messaging.datamodel.data.ConversationMessageData;
import com.android.messaging.datamodel.data.ConversationParticipantsData;
import com.android.messaging.datamodel.data.DraftMessageData;
import com.android.messaging.datamodel.data.DraftMessageData.DraftMessageDataListener;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.datamodel.data.PendingAttachmentData;
import com.android.messaging.datamodel.data.SubscriptionListData.SubscriptionListEntry;
import com.android.messaging.privatebox.AppPrivateLockManager;
import com.android.messaging.ui.BaseAlertDialog;
import com.android.messaging.ui.BugleActionBarActivity;
import com.android.messaging.ui.ConversationDrawables;
import com.android.messaging.ui.SnackBar;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.conversation.ComposeMessageView.IComposeMessageViewHost;
import com.android.messaging.ui.conversation.ConversationInputManager.ConversationInputHost;
import com.android.messaging.ui.conversation.ConversationMessageView.ConversationMessageViewHost;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.theme.ThemeInfo;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.ui.dialog.FiveStarRateDialog;
import com.android.messaging.ui.emoji.EmojiPickerFragment;
import com.android.messaging.ui.mediapicker.CameraGalleryFragment;
import com.android.messaging.ui.mediapicker.MediaPickerFragment;
import com.android.messaging.ui.senddelaymessages.SendDelayMessagesManager;
import com.android.messaging.ui.wallpaper.WallpaperManager;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleFirebaseAnalytics;
import com.android.messaging.util.ChangeDefaultSmsAppHelper;
import com.android.messaging.util.ContentType;
import com.android.messaging.util.FabricUtils;
import com.android.messaging.util.ImeUtil;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.SafeAsyncTask;
import com.android.messaging.util.TextUtil;
import com.android.messaging.util.UiUtils;
import com.android.messaging.util.UriUtil;
import com.android.messaging.util.ViewUtils;
import com.google.common.annotations.VisibleForTesting;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Compats;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdIconView;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.nativead.AcbNativeAdLoader;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;
import net.appcloudbox.autopilot.AutopilotEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Shows a list of messages/parts comprising a conversation.
 */
public class ConversationFragment extends Fragment implements ConversationDataListener,
        IComposeMessageViewHost, ConversationMessageViewHost, ConversationInputHost,
        DraftMessageDataListener, INotificationObserver {

    public static final String EVENT_SHOW_OPTION_MENU = "event_show_option_menu";
    public static final String EVENT_HIDE_OPTION_MENU = "event_hide_option_menu";
    public static final String EVENT_HIDE_MEDIA_PICKER = "event_hide_media_picker";
    public static final String RESET_ITEM = "reset_item";

    private final String IS_FIRST_CLICK_ACTION_MENU = "is_first_click_action_menu";
    private boolean mIsFirstClickActionMenu = false;
    private ArrayList<ConversationMessageData> mSelectMessageDataList;

    public static ArrayList<String> getSelectMessageIds() {
        return selectMessageIds;
    }

    private static ArrayList<String> selectMessageIds = new ArrayList<>();
    public static final String EVENT_UPDATE_BUBBLE_DRAWABLE = "event_update_bubble_drawable";

    public interface ConversationFragmentHost extends ImeUtil.ImeStateHost {
        void onStartComposeMessage();

        void onConversationMetadataUpdated();

        boolean shouldResumeComposeMessage();

        void onFinishCurrentConversation();

        void invalidateActionBar();

        ActionMode startActionMode(ActionMode.Callback callback);

        void dismissActionMode();

        ActionMode getActionMode();

        void onConversationMessagesUpdated(int numberOfMessages);

        void onConversationParticipantDataLoaded(int numberOfParticipants);

        boolean isActiveAndFocused();

        boolean isFromCreateConversation();
    }

    public static final String FRAGMENT_TAG = "conversation";

    static final int REQUEST_CHOOSE_ATTACHMENTS = 2;
    private static final int JUMP_SCROLL_THRESHOLD = 15;
    // We animate the message from draft to message list, if we the message doesn't show up in the
    // list within this time limit, then we just do a fade in animation instead
    public static final int MESSAGE_ANIMATION_MAX_WAIT = 500;

    private ComposeMessageView mComposeMessageView;
    private RecyclerView mRecyclerView;
    private ConversationMessageAdapter mAdapter;
    private ConversationFastScroller mFastScroller;
    private ImageView mWallpaperView;
    private ImageView mThemeWallpaperView;

    private View mConversationComposeDivider;
    private ChangeDefaultSmsAppHelper mChangeDefaultSmsAppHelper;

    private String mConversationId;
    // If the fragment receives a draft as part of the invocation this is set
    private MessageData mIncomingDraft;

    private FrameLayout mMediaLayout;
    // This binding keeps track of our associated ConversationData instance
    // A binding should have the lifetime of the owning component,
    //  don't recreate, unbind and bind if you need new data
    @VisibleForTesting
    final Binding<ConversationData> mBinding = BindingBase.createBinding(this);

    // Saved Instance State Data - only for temporal data which is nice to maintain but not
    // critical for correctness.
    private static final String SAVED_INSTANCE_STATE_LIST_VIEW_STATE_KEY = "conversationViewState";
    private Parcelable mListState;

    private ConversationFragmentHost mHost;

    protected List<Integer> mFilterResults;

    // The minimum scrolling distance between RecyclerView's scroll change event beyong which
    // a fling motion is considered fast, in which case we'll delay load image attachments for
    // perf optimization.
    private int mFastFlingThreshold;

    // ConversationMessageView that is currently selected
    private ConversationMessageData mSelectedMessageData;

    // Attachment data for the attachment within the selected message that was long pressed
    private MessagePartData mSelectedAttachment;

    private boolean mIsPrivateConversation = false;

    private boolean mIsDestroyed = false;

    // Normally, as soon as draft message is loaded, we trust the UI state held in
    // ComposeMessageView to be the only source of truth (incl. the conversation self id). However,
    // there can be external events that forces the UI state to change, such as SIM state changes
    // or SIM auto-switching on receiving a message. This receiver is used to receive such
    // local broadcast messages and reflect the change in the UI.
    private final BroadcastReceiver mConversationSelfIdChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String conversationId =
                    intent.getStringExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID);
            final String selfId =
                    intent.getStringExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_SELF_ID);
            Assert.notNull(conversationId);
            Assert.notNull(selfId);
            if (TextUtils.equals(mBinding.getData().getConversationId(), conversationId)) {
                mComposeMessageView.updateConversationSelfIdOnExternalChange(selfId);
            }
        }
    };

    // Flag to prevent writing draft to DB on pause
    private boolean mSuppressWriteDraft;

    // Indicates whether local draft should be cleared due to external draft changes that must
    // be reloaded from db
    private boolean mClearLocalDraft;
    private ImmutableBindingRef<DraftMessageData> mDraftMessageDataModel;

    private boolean isScrolledToBottom() {
        if (mRecyclerView.getChildCount() == 0) {
            return true;
        }
        final View lastView = mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1);
        int lastVisibleItem = ((LinearLayoutManager) mRecyclerView
                .getLayoutManager()).findLastVisibleItemPosition();
        if (lastVisibleItem < 0) {
            // If the recyclerView height is 0, then the last visible item position is -1
            // Try to compute the position of the last item, even though it's not visible
            final long id = mRecyclerView.getChildItemId(lastView);
            final RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForItemId(id);
            if (holder != null) {
                lastVisibleItem = holder.getAdapterPosition();
            }
        }
        final int totalItemCount = mRecyclerView.getAdapter().getItemCount();
        final boolean isAtBottom = (lastVisibleItem + 1 == totalItemCount);
        return isAtBottom && lastView.getBottom() <= mRecyclerView.getHeight();
    }

    private void scrollToBottom(final boolean smoothScroll) {
        if (mAdapter.getItemCount() > 0) {
            scrollToPosition(mAdapter.getItemCount() - 1, smoothScroll);
        }
    }

    private void resetActionModeAndAnimation() {
        selectMessageIds.clear();
        mHost.dismissActionMode();
        mAdapter.setMultiSelectMode(false);
        mAdapter.closeItemAnimation();
    }

    private int mScrollToDismissThreshold;
    private final RecyclerView.OnScrollListener mListScrollListener =
            new RecyclerView.OnScrollListener() {
                // Keeps track of cumulative scroll delta during a scroll event, which we may use to
                // hide the media picker & co.
                private int mCumulativeScrollDelta;
                private boolean mScrollToDismissHandled;
                private boolean mWasScrolledToBottom = true;
                private int mScrollState = RecyclerView.SCROLL_STATE_IDLE;

                @Override
                public void onScrollStateChanged(final RecyclerView view, final int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        // Reset scroll states.
                        mCumulativeScrollDelta = 0;
                        mScrollToDismissHandled = false;
                    } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
//                        mRecyclerView.getItemAnimator().endAnimations();
                    }
                    mScrollState = newState;

                    if (!HSConfig.optBoolean(true, "Application", "SMSAd", "SMSDetailspageTopAd", "ShowAfterSlide")) {
                        hideTopBannerAd();
                    }
                }

                @Override
                public void onScrolled(final RecyclerView view, final int dx, final int dy) {
                    if (mScrollState == RecyclerView.SCROLL_STATE_DRAGGING &&
                            !mScrollToDismissHandled) {
                        mCumulativeScrollDelta += dy;
                        // Dismiss the keyboard only when the user scroll up (into the past).
                        if (mCumulativeScrollDelta < -mScrollToDismissThreshold) {
                            mComposeMessageView.hideAllComposeInputs(false /* animate */);
                            mScrollToDismissHandled = true;
                        }
                    }

                    if (mWasScrolledToBottom != isScrolledToBottom()) {
                        mConversationComposeDivider.animate().alpha(isScrolledToBottom() ? 0 : 1);
                        mWasScrolledToBottom = isScrolledToBottom();
                    }
                }
            };

    private final ActionMode.Callback mMessageActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(final ActionMode actionMode, final Menu menu) {
            if (getActivity() == null) {
                return false;
            }
            final MenuInflater menuInflater = getActivity().getMenuInflater();
            menuInflater.inflate(R.menu.conversation_fragment_select_menu, menu);
            if (singleMessageSelected()) {
                //  mSelectedMessageData = mSelectMessageDataList.get(0);
                if (mSelectedMessageData == null) {
                    return false;
                }
                final ConversationMessageData data = mSelectMessageDataList.get(0);
                menu.findItem(R.id.action_download).setVisible(data.getShowDownloadMessage());
                menu.findItem(R.id.action_send).setVisible(data.getShowResendMessage());

                // ShareActionProvider does not work with ActionMode. So we use a normal menu item.
                menu.findItem(R.id.share_message_menu).setVisible(data.getCanForwardMessage());
                menu.findItem(R.id.save_attachment).setVisible(mSelectedAttachment != null);
                menu.findItem(R.id.forward_message_menu).setVisible(data.getCanForwardMessage());

                if (data.getIsLocked()) {
                    menu.findItem(R.id.unlock_message_menu).setVisible(true);
                    menu.findItem(R.id.lock_message_menu).setVisible(false);
                } else {
                    menu.findItem(R.id.unlock_message_menu).setVisible(false);
                    menu.findItem(R.id.lock_message_menu).setVisible(true);
                }
                // TODO: We may want to support copying attachments in the future, but it's
                // unclear which attachment to pick when we make this context menu at the message level
                // instead of the part level
                menu.findItem(R.id.copy_text).setVisible(data.getCanCopyMessageToClipboard());
            } else {
                menu.findItem(R.id.action_download).setVisible(false);
                menu.findItem(R.id.action_send).setVisible(false);

                // ShareActionProvider does not work with ActionMode. So we use a normal menu item.
                menu.findItem(R.id.share_message_menu).setVisible(false);
                menu.findItem(R.id.save_attachment).setVisible(false);
                menu.findItem(R.id.forward_message_menu).setVisible(false);

                // TODO: We may want to support copying attachments in the future, but it's
                // unclear which attachment to pick when we make this context menu at the message level
                // instead of the part level
                menu.findItem(R.id.copy_text).setVisible(false);

                menu.findItem(R.id.action_menu).setVisible(true);

                menu.findItem(R.id.lock_message_menu).setVisible(false);
                menu.findItem(R.id.unlock_message_menu).setVisible(false);
                for (ConversationMessageData item : mSelectMessageDataList) {
                    if (item.getIsLocked()) {
                        menu.findItem(R.id.unlock_message_menu).setVisible(true);
                    } else {
                        menu.findItem(R.id.lock_message_menu).setVisible(true);
                    }
                }
            }

            Preferences preferences = Preferences.getDefault();
            mIsFirstClickActionMenu = preferences.getBoolean(IS_FIRST_CLICK_ACTION_MENU, true);
            if (mIsFirstClickActionMenu) {
                menu.findItem(R.id.action_menu).setIcon(R.drawable.ic_menu_with_red_point);
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(final ActionMode actionMode, final Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode actionMode, final MenuItem menuItem) {
            mSelectedMessageData = mSelectMessageDataList.get(0);
            final ConversationMessageData data = mSelectedMessageData;
            boolean result = true;
            final String messageId = data.getMessageId();
            switch (menuItem.getItemId()) {
                case R.id.save_attachment:
                    if (OsUtil.hasStoragePermission()) {
                        final SaveAttachmentTask saveAttachmentTask = new SaveAttachmentTask(
                                getActivity());
                        for (final MessagePartData part : data.getAttachments()) {
                            saveAttachmentTask.addAttachmentToSave(part.getContentUri(),
                                    part.getContentType());
                        }
                        if (saveAttachmentTask.getAttachmentCount() > 0) {
                            saveAttachmentTask.executeOnThreadPool();
                            resetActionModeAndAnimation();
                        }
                    } else {
                        getActivity().requestPermissions(
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                    }
                    BugleAnalytics.logEvent("SMS_DetailsPage_LongPress_Save", true);
                    break;
                case R.id.action_delete_message:
                    deleteMessage();
                    BugleAnalytics.logEvent("SMS_DetailsPage_LongPress_Delete", true, "numbers", String.valueOf(mSelectMessageDataList.size()));
                    break;
                case R.id.action_download:
                    if (mSelectedMessageData != null) {
                        retryDownload(messageId);
                        resetActionModeAndAnimation();
                    }
                    break;
                case R.id.action_send:
                    if (mSelectedMessageData != null) {
                        retrySend(messageId);
                        resetActionModeAndAnimation();
                    }
                    break;
                case R.id.copy_text:
                    Assert.isTrue(data.hasText());
                    final ClipboardManager clipboard = (ClipboardManager) getActivity()
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(
                            ClipData.newPlainText(null /* label */, data.getText()));
                    resetActionModeAndAnimation();
                    Toasts.showToast(R.string.message_copied);
                    BugleAnalytics.logEvent("SMS_DetailsPage_LongPress_Copy", true);
                    break;
                case R.id.details_menu:
                    MessageDetailsDialog.show(
                            getActivity(), data, mBinding.getData().getParticipants(),
                            mBinding.getData().getSelfParticipantById(data.getSelfParticipantId()));
                    resetActionModeAndAnimation();
                    BugleAnalytics.logEvent("SMS_DetailsPage_LongPress_Info", true);
                    break;
                case R.id.lock_message_menu:
                    resetActionModeAndAnimation();
                    Threads.postOnMainThreadDelayed(new Runnable() {
                        @Override
                        public void run() {
                            lockSelectedMessage();
                        }
                    }, 500);
                    break;
                case R.id.unlock_message_menu:
                    resetActionModeAndAnimation();
                    Threads.postOnMainThreadDelayed(new Runnable() {
                        @Override
                        public void run() {
                            unlockSelectedMessage();
                        }
                    }, 500);
                    break;
                case R.id.share_message_menu:
                    shareMessage(data);
                    break;
                case R.id.forward_message_menu:
                    // TODO: Currently we are forwarding one part at a time, instead of
                    // the entire message. Change this to forwarding the entire message when we
                    // use message-based cursor in conversation.
                    final MessageData message = mBinding.getData().createForwardedMessage(data);
                    UIIntents.get().launchForwardMessageActivity(getActivity(), message);
                    resetActionModeAndAnimation();
                    BugleAnalytics.logEvent("SMS_DetailsPage_LongPress_Forward", true);
                    break;
                case R.id.action_menu:
                    BugleAnalytics.logEvent("SMS_DetailsPage_LongPress_More", true);
                    if (mIsFirstClickActionMenu) {
                        menuItem.setIcon(R.drawable.ic_menu);
                        Preferences preferences = Preferences.getDefault();
                        preferences.putBoolean(IS_FIRST_CLICK_ACTION_MENU, false);
                    }
                    break;
                default:
                    result = false;
                    break;
            }

            return result;
        }

        private void shareMessage(final ConversationMessageData data) {
            // Figure out what to share.
            MessagePartData attachmentToShare = mSelectedAttachment;
            // If the user long-pressed on the background, we will share the text (if any)
            // or the first attachment.
            if (mSelectedAttachment == null
                    && TextUtil.isAllWhitespace(data.getText())) {
                final List<MessagePartData> attachments = data.getAttachments();
                if (attachments.size() > 0) {
                    attachmentToShare = attachments.get(0);
                }
            }

            final Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            if (attachmentToShare == null) {
                shareIntent.putExtra(Intent.EXTRA_TEXT, data.getText());
                shareIntent.setType("text/plain");
            } else {
                shareIntent.putExtra(
                        Intent.EXTRA_STREAM, attachmentToShare.getContentUri());
                shareIntent.setType(attachmentToShare.getContentType());
            }
            final CharSequence title = getResources().getText(R.string.action_share);
            startActivity(Intent.createChooser(shareIntent, title));
            Threads.postOnMainThreadDelayed(() -> resetActionModeAndAnimation(), 500);
        }

        @Override
        public void onDestroyActionMode(final ActionMode actionMode) {
            BugleAnalytics.logEvent("SMS_DetailsPage_LongPress_Close", true);
            BugleFirebaseAnalytics.logEvent("SMS_DetailsPage_LongPress_Close");
        }
    };

    private boolean mHasSentMessages;

    private boolean singleMessageSelected() {
        return mSelectMessageDataList.size() == 1;
    }

    private AcbNativeAdLoader mNativeAdLoader;
    private boolean isForeground;
    private Handler mAdRefreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (isForeground) {
                loadTopBannerAd();
            } else {
                sendEmptyMessageDelayed(0,
                        HSConfig.optInteger(60, "Application", "SMSAd", "SMSDetailspageTopAd", "RefreshInterval")
                                * DateUtils.SECOND_IN_MILLIS);
            }
        }
    };
    private AcbNativeAd mNativeAd;
    private ViewGroup mAdContainer;
    private AcbNativeAdContainerView mAdContentView;
    private int composeEditTextInitialPos = -1;
    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {

            int[] pos = new int[2];
            mComposeMessageView.getComposeEditText().getLocationOnScreen(pos);
            int bottomPos = pos[1] + mComposeMessageView.getComposeEditText().getMeasuredHeight();
            if (bottomPos == 0) {
                return;
            }
            HSLog.d("compose message view bottom position: " + bottomPos);
            if (composeEditTextInitialPos == -1) {
                composeEditTextInitialPos = bottomPos;
            }
            if (composeEditTextInitialPos != bottomPos) {
                hideTopBannerAd();
            }
        }
    };


    /**
     * {@inheritDoc} from Fragment
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFastFlingThreshold = getResources().getDimensionPixelOffset(
                R.dimen.conversation_fast_fling_threshold);
        mSelectMessageDataList = new ArrayList<>();
        if (selectMessageIds != null) {
            selectMessageIds.clear();
        }
        mAdapter = new ConversationMessageAdapter(this, null,
                // Sets the item click listener on the Recycler item views.
                new ConversationMessageAdapter.ConversationMessageClickListener() {
                    @Override
                    public void onConversationMessageClick(ConversationMessageData data) {
                        if (data != null && mAdapter.isMultiSelectMode()) {
                            String messageId = data.getMessageId();
                            if (selectMessageIds.contains(messageId)) {
                                selectMessageIds.remove(messageId);
                                mSelectMessageDataList.remove(data);
                            } else {
                                selectMessageIds.add(messageId);
                                mSelectMessageDataList.add(data);
                            }
                            mHost.startActionMode(mMessageActionModeCallback);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            handleMessageClick(data);
                        }
                        if (mSelectMessageDataList.isEmpty()) {
                            resetActionModeAndAnimation();
                        }
                    }

                    @Override
                    public void onConversationMessageLongClick(ConversationMessageData data) {
                        selectMessage(data);
                    }
                }
        );

        HSGlobalNotificationCenter.addObserver(EVENT_SHOW_OPTION_MENU, this);
        HSGlobalNotificationCenter.addObserver(EVENT_SHOW_OPTION_MENU, this);
        HSGlobalNotificationCenter.addObserver(EVENT_HIDE_MEDIA_PICKER, this);
        HSGlobalNotificationCenter.addObserver(RESET_ITEM, this);
        HSGlobalNotificationCenter.addObserver(EVENT_UPDATE_BUBBLE_DRAWABLE, this);
        BugleAnalytics.logEvent("SMS_DetailsPage_Show", true);
        BugleFirebaseAnalytics.logEvent("SMS_DetailsPage_Show");
        AutopilotEvent.logTopicEvent("topic-768lyi3sp", "detailspage_show");
    }

    private void loadTopBannerAd() {
        if (BillingManager.isPremiumUser()) {
            return;
        }

        BugleAnalytics.logEvent("Detailspage_TopAd_Should_Show", true);
        BugleFirebaseAnalytics.logEvent("Detailspage_TopAd_Should_Show");
        AutopilotEvent.logTopicEvent("topic-768lyi3sp", "topad_chance");

        List<AcbNativeAd> nativeAds = AcbNativeAdManager.fetch(AdPlacement.AD_DETAIL_NATIVE, 1);
        if (nativeAds.size() > 0) {
            if (mNativeAd != null) {
                mNativeAd.release();
            }
            mNativeAd = nativeAds.get(0);
            mNativeAd.setNativeClickListener(
                    acbAd -> BugleAnalytics.logEvent("Detailspage_TopAd_Click", true));
            showTopBannerAd();
        } else {
            mNativeAdLoader = AcbNativeAdManager.createLoaderWithPlacement(AdPlacement.AD_DETAIL_NATIVE);
            mNativeAdLoader.load(1, new AcbNativeAdLoader.AcbNativeAdLoadListener() {
                @Override
                public void onAdReceived(AcbNativeAdLoader acbNativeAdLoader, List<AcbNativeAd> list) {
                    if (list.size() > 0) {
                        if (mNativeAd != null) {
                            mNativeAd.release();
                        }
                        mNativeAd = list.get(0);
                        mNativeAd.setNativeClickListener(
                                acbAd -> BugleAnalytics.logEvent("Detailspage_TopAd_Click", true));
                        showTopBannerAd();
                    } else {
                        enqueueNextAd();
                    }
                }

                @Override
                public void onAdFinished(AcbNativeAdLoader acbNativeAdLoader, AcbError acbError) {
                    enqueueNextAd();
                }
            });
        }
    }

    public void showTopBannerAd() {
        if (((ConversationActivity) getActivity()).shouldShowContactPickerFragment()) {
            BugleAnalytics.logEvent("Detailspage_TopAd_Not_Show", "reason", "isFromContactPicker");
            return;
        }

        if (mNativeAd == null) {
            return;
        }

        if (mAdContainer.getVisibility() != View.VISIBLE) {
            BugleAnalytics.logEvent("Detailspage_TopAd_Not_Show", "reason", "alreadyInteract");
            return;
        }

        final View adView = LayoutInflater.from(getActivity()).inflate(R.layout.conversation_ad_view, mAdContainer, false);

        mAdContentView = new AcbNativeAdContainerView(mAdContainer.getContext());
        mAdContentView.addContentView(adView);

        AcbNativeAdIconView icon = ViewUtils.findViewById(adView, R.id.banner_icon_image);
        icon.setShapeMode(1);
        icon.setRadius(Dimensions.pxFromDp(20));
        mAdContentView.setAdIconView(icon);
        TextView title = ViewUtils.findViewById(adView, R.id.banner_title);
        title.setTextColor(ConversationColors.get().getListTitleColor());
        mAdContentView.setAdTitleView(title);
        TextView description = ViewUtils.findViewById(adView, R.id.banner_des);
        description.setTextColor(ConversationColors.get().getListSubtitleColor());
        mAdContentView.setAdBodyView(description);
        TextView actionBtn = ViewUtils.findViewById(adView, R.id.banner_action);
        mAdContentView.setAdActionView(actionBtn);
        actionBtn.setTextColor(Color.parseColor(ThemeInfo.getThemeInfo(ThemeUtils.getCurrentThemeName()).bannerAdActionTextColor));
        Drawable actionBg = getResources().getDrawable(R.drawable.conversation_list_ad_action_pressed_bg);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((LayerDrawable) actionBg).getDrawable(1)
                    .setColorFilter(
                            Color.parseColor(ThemeInfo.getThemeInfo(ThemeUtils.getCurrentThemeName()).bannerAdActionColor),
                            PorterDuff.Mode.SRC_IN);
        }
        actionBtn.setBackgroundDrawable(actionBg);
        if (HSConfig.optBoolean(true, "Application", "SMSAd", "SMSDetailspageTopAd", "FacebookEnabled")) {
            adView.setBackgroundColor(Color.parseColor(ThemeInfo.getThemeInfo(ThemeUtils.getCurrentThemeName()).bannerAdBgColor));
        }

        FrameLayout choice = ViewUtils.findViewById(adView, R.id.ad_choice);
        mAdContentView.setAdChoiceView(choice);
        mAdContainer.removeAllViews();
        mAdContainer.addView(mAdContentView);

        ImageView ivAdPreview = adView.findViewById(R.id.icon_ad_preview);
        ivAdPreview.getDrawable().setColorFilter(ConversationColors.get().getListTimeColor(), PorterDuff.Mode.SRC_ATOP);

        mAdContentView.hideAdCorner();
        mAdContentView.fillNativeAd(mNativeAd);

        mRecyclerView.setPadding(0, Dimensions.pxFromDp(53), 0, 0);
        mRecyclerView.setClipToPadding(true);

        if (WallpaperManager.getWallpaperPathByConversationId(mConversationId) != null) {
            int color = PrimaryColors.getPrimaryColor();
            mAdContainer.setBackground(BackgroundDrawables.createBackgroundDrawable(
                    Color.argb(40, Color.red(color), Color.green(color), Color.blue(color)), 0, false));
            title.setTextColor(0xffffffff);
            description.setTextColor(0xffffffff);
            ivAdPreview.getDrawable().setColorFilter(0xffffffff, PorterDuff.Mode.SRC_ATOP);
            actionBtn.setTextColor(0xffffffff);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ((LayerDrawable) actionBg).getDrawable(1)
                        .setColorFilter(0xffffffff, PorterDuff.Mode.SRC_IN);
            }
        }

        BugleAnalytics.logEvent("Detailspage_TopAd_Show", true);
        BugleFirebaseAnalytics.logEvent("Detailspage_TopAd_Show");
        AutopilotEvent.logTopicEvent("topic-768lyi3sp", "topad_show");

        enqueueNextAd();
    }

    private void enqueueNextAd() {
        if (!HSConfig.optBoolean(false, "Application", "SMSAd", "SMSDetailspageTopAd", "HideWhenKeyboardShow")) {
            AcbNativeAdManager.preload(1, AdPlacement.AD_DETAIL_NATIVE);
            mAdRefreshHandler.removeCallbacksAndMessages(null);
            mAdRefreshHandler.sendEmptyMessageDelayed(0,
                    HSConfig.optInteger(60, "Application", "SMSAd", "SMSDetailspageTopAd", "RefreshInterval")
                            * DateUtils.SECOND_IN_MILLIS);
        }
    }

    private void hideTopBannerAd() {
        if (HSConfig.optBoolean(false, "Application", "SMSAd", "SMSDetailspageTopAd", "HideWhenKeyboardShow")) {
            mAdContainer.setVisibility(View.GONE);
            mRecyclerView.setClipToPadding(false);
        }
    }

    /**
     * setConversationInfo() may be called before or after onCreate(). When a user initiate a
     * conversation from compose, the ConversationActivity creates this fragment and calls
     * setConversationInfo(), so it happens before onCreate(). However, when the activity is
     * restored from saved instance state, the ConversationFragment is created automatically by
     * the fragment, before ConversationActivity has a chance to call setConversationInfo(). Since
     * the ability to start loading data depends on both methods being called, we need to start
     * loading when onActivityCreated() is called, which is guaranteed to happen after both.
     */
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Delay showing the message list until the participant list is loaded.
        mRecyclerView.setVisibility(View.INVISIBLE);
        mBinding.ensureBound();
        mBinding.getData().init(getLoaderManager(), mBinding);
        // Build the input manager with all its required dependencies and pass it along to the
        // compose message view.
        final ConversationInputManager inputManager = new ConversationInputManager(
                getActivity(), this, mComposeMessageView, mHost, getFragmentManagerToUse(),
                mBinding, mComposeMessageView.getDraftDataModel(), savedInstanceState);
        mComposeMessageView.setInputManager(inputManager);
        mComposeMessageView.setConversationDataModel(BindingBase.createBindingReference(mBinding));
        mComposeMessageView.getComposeEditText().getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
        mHost.invalidateActionBar();

        mDraftMessageDataModel =
                BindingBase.createBindingReference(mComposeMessageView.getDraftDataModel());
        mDraftMessageDataModel.getData().addListener(this);
    }

    public void onActivityStart() {
        if (mIsPrivateConversation) {
            AppPrivateLockManager.getInstance().checkLockStateAndSelfVerify();
        }
    }

    public void onAttachmentChoosen() {
        // Attachment has been choosen in the AttachmentChooserActivity, so clear local draft
        // and reload draft on resume.
        mClearLocalDraft = true;
    }

    private int getScrollToMessagePosition() {
        final Activity activity = getActivity();
        if (activity == null) {
            return -1;
        }

        final Intent intent = activity.getIntent();
        if (intent == null) {
            return -1;
        }

        return intent.getIntExtra(UIIntents.UI_INTENT_EXTRA_MESSAGE_POSITION, -1);
    }

    private void clearScrollToMessagePosition() {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        final Intent intent = activity.getIntent();
        if (intent == null) {
            return;
        }
        intent.putExtra(UIIntents.UI_INTENT_EXTRA_MESSAGE_POSITION, -1);
    }

    private final Handler mHandler = new Handler();

    /**
     * {@inheritDoc} from Fragment
     */
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        HSLog.d("message list create : " + System.currentTimeMillis());
        final View view = inflater.inflate(R.layout.conversation_fragment, container, false);
        mRecyclerView = view.findViewById(android.R.id.list);
        mAdContainer = view.findViewById(R.id.top_banner_ad_container);
        final LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setStackFromEnd(true);
        manager.setReverseLayout(false);
//        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setItemAnimator(null);

        mRecyclerView.setAdapter(mAdapter);
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(SAVED_INSTANCE_STATE_LIST_VIEW_STATE_KEY);
        }

        mConversationComposeDivider = view.findViewById(R.id.conversation_compose_divider);
        mScrollToDismissThreshold = ViewConfiguration.get(getActivity()).getScaledTouchSlop();
        mRecyclerView.addOnScrollListener(mListScrollListener);
        mFastScroller = ConversationFastScroller.addTo(mRecyclerView,
                UiUtils.isRtlMode() ? ConversationFastScroller.POSITION_LEFT_SIDE :
                        ConversationFastScroller.POSITION_RIGHT_SIDE);

        mComposeMessageView = (ComposeMessageView)
                view.findViewById(R.id.message_compose_view_container);
        // Bind the compose message view to the DraftMessageData
        mComposeMessageView.bind(DataModel.get().createDraftMessageData(
                mBinding.getData().getConversationId()), this);
        if (!(Compats.IS_SAMSUNG_DEVICE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)) {
            mComposeMessageView.requestFocus();
        }

        mMediaLayout = view.findViewById(R.id.camera_photo_layout);
        mWallpaperView = view.findViewById(R.id.conversation_fragment_wallpaper);
        mThemeWallpaperView = view.findViewById(R.id.conversation_fragment_theme_wallpaper);

        mComposeMessageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mComposeMessageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                Threads.postOnMainThreadDelayed(() -> {
                    if (mIsDestroyed || getActivity() == null) {
                        return;
                    }
                    if (AdConfig.isDetailpageTopAdEnabled()
                            && !mHost.isFromCreateConversation()) {
                        loadTopBannerAd();
                    }
                    if (AdConfig.isHomepageBannerAdEnabled()) {
                        AcbNativeAdManager.preload(1, AdPlacement.AD_BANNER);
                    }
                }, 500);
            }
        });

        return view;
    }

    private void scrollToPosition(final int targetPosition, final boolean smoothScroll) {
        if (smoothScroll) {
            final int maxScrollDelta = JUMP_SCROLL_THRESHOLD;

            final LinearLayoutManager layoutManager =
                    (LinearLayoutManager) mRecyclerView.getLayoutManager();
            final int firstVisibleItemPosition =
                    layoutManager.findFirstVisibleItemPosition();
            final int delta = targetPosition - firstVisibleItemPosition;
            final int intermediatePosition;

            if (delta > maxScrollDelta) {
                intermediatePosition = Math.max(0, targetPosition - maxScrollDelta);
            } else if (delta < -maxScrollDelta) {
                final int count = layoutManager.getItemCount();
                intermediatePosition = Math.min(count - 1, targetPosition + maxScrollDelta);
            } else {
                intermediatePosition = -1;
            }
            if (intermediatePosition != -1) {
                mRecyclerView.scrollToPosition(intermediatePosition);
            }
            mRecyclerView.smoothScrollToPosition(targetPosition);
        } else {
            mRecyclerView.scrollToPosition(targetPosition);
        }
    }

    private int getScrollPositionFromBottom() {
        final LinearLayoutManager layoutManager =
                (LinearLayoutManager) mRecyclerView.getLayoutManager();
        final int lastVisibleItem =
                layoutManager.findLastVisibleItemPosition();
        return Math.max(mAdapter.getItemCount() - 1 - lastVisibleItem, 0);
    }

    /**
     * Display a photo using the Photoviewer component.
     */
    @Override
    public void displayPhoto(final Uri photoUri, final Rect imageBounds, final boolean isDraft) {
        displayPhoto(photoUri, imageBounds, isDraft, mConversationId, getActivity());
    }

    public static void displayPhoto(final Uri photoUri, final Rect imageBounds,
                                    final boolean isDraft, final String conversationId, final Activity activity) {
        final Uri imagesUri =
                isDraft ? MessagingContentProvider.buildDraftImagesUri(conversationId)
                        : MessagingContentProvider.buildConversationImagesUri(conversationId);
        UIIntents.get().launchFullScreenPhotoViewer(
                activity, photoUri, imageBounds, imagesUri);
    }

    private void selectMessage(final ConversationMessageData data) {
        selectMessage(data, null /* attachment */);
    }

    private void selectMessage(final ConversationMessageData data,
                               final MessagePartData attachment) {
        if (!mAdapter.isMultiSelectMode()) {
            mAdapter.setMultiSelectMode(true);
            mSelectedMessageData = data;
            if (mSelectedMessageData == null) {
                mAdapter.notifyDataSetChanged();
                mSelectedAttachment = null;
                return;
            }
            mSelectedAttachment = attachment;
            mAdapter.notifyDataSetChanged();
            mSelectMessageDataList.clear();
            mSelectMessageDataList.add(data);
            selectMessageIds.add(data.getMessageId());
            mAdapter.openItemAnimation();
            mHost.startActionMode(mMessageActionModeCallback);
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mListState != null) {
            outState.putParcelable(SAVED_INSTANCE_STATE_LIST_VIEW_STATE_KEY, mListState);
        }
        mComposeMessageView.saveInputState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        WallpaperManager.setConversationWallPaper(mWallpaperView, mThemeWallpaperView, mConversationId);

        if (mIncomingDraft == null) {
            mComposeMessageView.requestDraftMessage(mClearLocalDraft);
        } else {
            mComposeMessageView.setDraftMessage(mIncomingDraft);
            mIncomingDraft = null;
        }
        mClearLocalDraft = false;
        isForeground = true;

        // On resume, check if there's a pending request for resuming message compose. This
        // may happen when the user commits the contact selection for a group conversation and
        // goes from compose back to the conversation fragment.
        if (mHost.shouldResumeComposeMessage()) {
            mComposeMessageView.resumeComposeMessage(false);
        }

        setConversationFocus();
        Threads.postOnMainThreadDelayed(() -> BugleNotifications.markMessagesAsRead(mConversationId), 500);

        // On resume, invalidate all message views to show the updated timestamp.
        mAdapter.notifyDataSetChanged();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mConversationSelfIdChangeReceiver,
                new IntentFilter(UIIntents.CONVERSATION_SELF_ID_CHANGE_BROADCAST_ACTION));
    }

    public boolean hasSentMessages() {
        return mHasSentMessages;
    }

    void setConversationFocus() {
        if (mHost.isActiveAndFocused()) {
            mBinding.getData().setFocus();
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        if (mHost.getActionMode() != null) {
            return;
        }

        inflater.inflate(R.menu.conversation_menu, menu);

        boolean supportCallAction = false;
        try {
            final ConversationData data = mBinding.getData();
            supportCallAction = (PhoneUtils.getDefault().isVoiceCapable() &&
                    data.getParticipantPhoneNumber() != null);
        } catch (IllegalStateException e) {
        }
        menu.findItem(R.id.action_call).setVisible(supportCallAction);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu:
                BugleAnalytics.logEvent("SMS_DetailsPage_IconSettings_Click", true);
                BugleFirebaseAnalytics.logEvent("SMS_DetailsPage_IconSettings_Click");
                if (!mAdapter.isMultiSelectMode()) {
                    UIIntents.get().launchPeopleAndOptionsActivity(getActivity(), mConversationId);
                }
                return false;
            case R.id.action_call:
                final String phoneNumber = mBinding.getData().getParticipantPhoneNumber();
                Assert.notNull(phoneNumber);
                final View targetView = getActivity().findViewById(R.id.action_call);
                Point centerPoint;
                if (targetView != null) {
                    final int screenLocation[] = new int[2];
                    targetView.getLocationOnScreen(screenLocation);
                    final int centerX = screenLocation[0] + targetView.getWidth() / 2;
                    final int centerY = screenLocation[1] + targetView.getHeight() / 2;
                    centerPoint = new Point(centerX, centerY);
                } else {
                    // In the overflow menu, just use the center of the screen.
                    final Display display = getActivity().getWindowManager().getDefaultDisplay();
                    centerPoint = new Point(display.getWidth() / 2, display.getHeight() / 2);
                }
                UIIntents.get().launchPhoneCallActivity(getActivity(), phoneNumber, centerPoint);
                BugleAnalytics.logEvent("SMS_DetailsPage_IconCall_Click", true);
                BugleFirebaseAnalytics.logEvent("SMS_DetailsPage_IconCall_Click");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clusterConversationMessageData(final ConversationMessageData formerData, final ConversationMessageData latterData) {
        final String formerParticipantId = formerData.getParticipantId();
        final String latterParticipantId = latterData.getParticipantId();
        if (!TextUtils.equals(formerParticipantId, latterParticipantId)) {
            return;
        }

        final boolean formerStatus = formerData.getIsIncoming();
        final boolean latterStatus = latterData.getIsIncoming();
        if (latterStatus != formerStatus) {
            return;
        }

        final long formerReceivedTimestamp = formerData.getReceivedTimeStamp();
        final long latterReceivedTimestamp = latterData.getReceivedTimeStamp();
        final long timestampDeltaMillis = Math.abs(formerReceivedTimestamp - latterReceivedTimestamp);
        if (timestampDeltaMillis > DateUtils.MINUTE_IN_MILLIS) {
            return;
        }

        final String formerSelfId = formerData.getSelfParticipantId();
        final String latterSelfId = latterData.getSelfParticipantId();
        if (!TextUtils.equals(formerSelfId, latterSelfId)) {
            return;
        }

        formerData.setCanClusterWithNextMessage(true);
        latterData.setCanClusterWithPreviousMessage(true);
    }

    /**
     * {@inheritDoc} from ConversationDataListener
     */
    @Override
    public void onConversationMessagesCursorUpdated(final ConversationData data,
                                                    final Cursor cursor, final ConversationMessageData newestMessage,
                                                    final boolean isSync) {
        mBinding.ensureBound(data);
        // This needs to be determined before swapping cursor, which may change the scroll state.
        final boolean scrolledToBottom = isScrolledToBottom();
        final int positionFromBottom = getScrollPositionFromBottom();

        // If participants not loaded, assume 1:1 since that's the 99% case
        final boolean oneOnOne =
                !data.getParticipantsLoaded() || data.getOtherParticipant() != null;
        mAdapter.setOneOnOne(oneOnOne, false /* invalidate */);
        // Ensure that the action bar is updated with the current data.
        invalidateOptionsMenu();

        List<ConversationMessageData> messageDataList = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                ConversationMessageData messageData = new ConversationMessageData();
                messageData.bind(cursor);
                messageDataList.add(messageData);
            } while (cursor.moveToNext());
        }

        if (messageDataList.size() > 1) {
            for (int i = 0; i < messageDataList.size() - 1; i++) {
                clusterConversationMessageData(messageDataList.get(i), messageDataList.get(i + 1));
            }
        }

        if (mIsDestroyed) {
            return;
        }

        mAdapter.setDataList(messageDataList);

        if (isSync) {
            // This is a message sync. Syncing messages changes cursor item count, which would
            // implicitly change RV's scroll position. We'd like the RV to keep scrolled to the same
            // relative position from the bottom (because RV is stacked from bottom), so that it
            // stays relatively put as we sync.
            final int position = Math.max(mAdapter.getItemCount() - 1 - positionFromBottom, 0);
            scrollToPosition(position, false /* smoothScroll */);
        } else if (newestMessage != null) {
            // Show a snack bar notification if we are not scrolled to the bottom and the new
            // message is an incoming message.
            if (!scrolledToBottom && newestMessage.getIsIncoming()) {
                // If the conversation activity is started but not resumed (if another dialog
                // activity was in the foregrond), we will show a system notification instead of
                // the snack bar.
                if (mBinding.getData().isFocused()) {
                    UiUtils.showSnackBarWithCustomAction(getActivity(),
                            getView().getRootView(),
                            getString(R.string.in_conversation_notify_new_message_text),
                            SnackBar.Action.createCustomAction(() -> {
                                        scrollToBottom(true /* smoothScroll */);
                                        mComposeMessageView.hideAllComposeInputs(false /* animate */);
                                    },
                                    getString(R.string.in_conversation_notify_new_message_action)),
                            null /* interactions */,
                            SnackBar.Placement.above(mComposeMessageView));
                }
            } else {
                // We are either already scrolled to the bottom or this is an outgoing message,
                // scroll to the bottom to reveal it.
                // Don't smooth scroll if we were already at the bottom; instead, we scroll
                // immediately so RecyclerView's view animation will take place.
                scrollToBottom(!scrolledToBottom);
            }
        }

        if (cursor != null) {
            mHost.onConversationMessagesUpdated(cursor.getCount());

            // Are we coming from a widget click where we're told to scroll to a particular item?
            final int scrollToPos = getScrollToMessagePosition();
            if (scrollToPos >= 0) {
                if (LogUtil.isLoggable(LogUtil.BUGLE_TAG, LogUtil.VERBOSE)) {
                    LogUtil.v(LogUtil.BUGLE_TAG, "onConversationMessagesCursorUpdated " +
                            " scrollToPos: " + scrollToPos +
                            " cursorCount: " + cursor.getCount());
                }
                scrollToPosition(scrollToPos, true /*smoothScroll*/);
                clearScrollToMessagePosition();
            }
        }

        mHost.invalidateActionBar();
    }

    /**
     * {@inheritDoc} from ConversationDataListener
     */
    @Override
    public void onConversationMetadataUpdated(final ConversationData conversationData) {
        if (conversationData != null && conversationData.isPrivate()) {
            if (!getActivity().isFinishing() && !mIsPrivateConversation) {
                AppPrivateLockManager.getInstance().checkLockStateAndSelfVerify();
            }
            mIsPrivateConversation = true;
        }
        mBinding.ensureBound(conversationData);

        if (mSelectedMessageData != null && mSelectedAttachment != null) {
            // We may have just sent a message and the temp attachment we selected is now gone.
            // and it was replaced with some new attachment.  Since we don't know which one it
            // is we shouldn't reselect it (unless there is just one) In the multi-attachment
            // case we would just deselect the message and allow the user to reselect, otherwise we
            // may act on old temp data and may crash.
            final List<MessagePartData> currentAttachments = mSelectedMessageData.getAttachments();
            if (currentAttachments.size() == 1) {
                mSelectedAttachment = currentAttachments.get(0);
            } else if (!currentAttachments.contains(mSelectedAttachment)) {
            }
        }
        // Ensure that the action bar is updated with the current data.
        invalidateOptionsMenu();
        mHost.onConversationMetadataUpdated();
        mAdapter.notifyDataSetChanged();
    }

    public void setConversationInfo(final Context context, final String conversationId,
                                    final MessageData draftData) {
        // TODO: Eventually I would like the Factory to implement
        // Factory.get().bindConversationData(mBinding, getActivity(), this, conversationId));
        if (!mBinding.isBound()) {
            mConversationId = conversationId;
            mIncomingDraft = draftData;
            mBinding.bind(DataModel.get().createConversationData(context, this, conversationId));
        } else {
            Assert.isTrue(TextUtils.equals(mBinding.getData().getConversationId(), conversationId));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsDestroyed = true;
        // Unbind all the views that we bound to data

        if (mComposeMessageView != null) {
            // if we have message to send in a delay time, unbind data until message is sent
            String conversationId = mBinding.getData().getConversationId();
            if (!mComposeMessageView.getIsWaitingToSendMessageFlag()) {
                SendDelayMessagesManager.remove(conversationId);
                mComposeMessageView.unbind();
                mBinding.unbind();
            } else {
                mComposeMessageView.setOnActionEndListener(new SendDelayActionCompletedCallBack() {
                    @Override
                    public void onSendDelayActionEnd() {
                        mComposeMessageView.unbind();
                        mBinding.unbind();
                    }
                });
            }
            mComposeMessageView.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
        }

        mRecyclerView.setAdapter(null);

        if (mNativeAd != null) {
            mNativeAd.release();
        }
        if (mNativeAdLoader != null) {
            mNativeAdLoader.cancel();
        }
        if (AdConfig.isDetailpageTopAdEnabled()) {
            AcbNativeAdManager.preload(1, AdPlacement.AD_DETAIL_NATIVE);
        }

        mAdRefreshHandler.removeCallbacksAndMessages(null);

        HSGlobalNotificationCenter.removeObserver(this);
        FiveStarRateDialog.dismissDialogs();
    }

    void suppressWriteDraft() {
        mSuppressWriteDraft = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mComposeMessageView != null && !mSuppressWriteDraft) {
            mComposeMessageView.writeDraftMessage();
        }
        mSuppressWriteDraft = false;
        isForeground = false;
        mBinding.getData().unsetFocus();
        mListState = mRecyclerView.getLayoutManager().onSaveInstanceState();

        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(mConversationSelfIdChangeReceiver);
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // TODO: Remove isBound and replace it with ensureBound after b/15704674.
    public boolean isBound() {
        return mBinding.isBound();
    }

    private FragmentManager getFragmentManagerToUse() {
        return OsUtil.isAtLeastJB_MR1() ? getChildFragmentManager() : getFragmentManager();
    }

    public CameraGalleryFragment getMediaPicker() {
        return (CameraGalleryFragment) getFragmentManagerToUse().findFragmentByTag(
                CameraGalleryFragment.FRAGMENT_TAG);
    }

    @Override
    public void sendMessage(final MessageData message) {
        if (isReadyForAction()) {
            if (ensureKnownRecipients()) {
                String name = mBinding.getData().getConversationName();
                if (!TextUtils.isEmpty(name)) {
                    String[] count = name.split(",");
                    BugleAnalytics.logEvent("SMS_SendPeopleAmount_Statistics", true, "type", String.valueOf(count.length));
                    BugleFirebaseAnalytics.logEvent("SMS_SendPeopleAmount_Statistics", "type", String.valueOf(count.length));
                }

                // Merge the caption text from attachments into the text body of the messages
                message.consolidateText();

                mBinding.getData().sendMessage(mBinding, message);

                mHasSentMessages = true;
            } else {
                LogUtil.w(LogUtil.BUGLE_TAG, "Message can't be sent: conv participants not loaded");
            }
        } else {
            warnOfMissingActionConditions(true /*sending*/,
                    () -> sendMessage(message));
        }
    }

    public void setHost(final ConversationFragmentHost host) {
        mHost = host;
    }

    public String getConversationName() {
        if (mBinding.isBound()) {
            return mBinding.getData().getConversationName();
        } else {
            return "";
        }
    }

    @Override
    public void onComposeEditTextFocused() {
        mHost.onStartComposeMessage();
    }

    @Override
    public void onAttachmentsCleared() {
    }

    /**
     * Called to check if all conditions are nominal and a "go" for some action, such as deleting
     * a message, that requires this app to be the default app. This is also a precondition
     * required for sending a draft.
     *
     * @return true if all conditions are nominal and we're ready to send a message
     */
    @Override
    public boolean isReadyForAction() {
        return UiUtils.isReadyForAction();
    }

    /**
     * When there's some condition that prevents an operation, such as sending a message,
     * call warnOfMissingActionConditions to put up a snackbar and allow the user to repair
     * that condition.
     *
     * @param sending                                  - true if we're called during a sending operation
     * @param commandToRunAfterActionConditionResolved - a runnable to run after the user responds
     *                                                 positively to the condition prompt and resolves the condition. If null,
     *                                                 the user will be shown a toast to tap the send button again.
     */
    @Override
    public void warnOfMissingActionConditions(final boolean sending,
                                              final Runnable commandToRunAfterActionConditionResolved) {
        if (mChangeDefaultSmsAppHelper == null) {
            mChangeDefaultSmsAppHelper = new ChangeDefaultSmsAppHelper();
        }
        mChangeDefaultSmsAppHelper.warnOfMissingActionConditions(sending,
                commandToRunAfterActionConditionResolved, mComposeMessageView,
                getView().getRootView(),
                getActivity(), this);
    }

    private boolean ensureKnownRecipients() {
        final ConversationData conversationData = mBinding.getData();

        if (!conversationData.getParticipantsLoaded()) {
            // We can't tell yet whether or not we have an unknown recipient
            return false;
        }

        final ConversationParticipantsData participants = conversationData.getParticipants();
        for (final ParticipantData participant : participants) {
            if (participant.isUnknownSender()) {
                FabricUtils.logNonFatal("Send_Message_Unknown_Sender");
                UiUtils.showToast(R.string.unknown_sender);
                return false;
            }
        }

        return true;
    }

    public void retryDownload(final String messageId) {
        if (isReadyForAction()) {
            mBinding.getData().downloadMessage(mBinding, messageId);
        } else {
            warnOfMissingActionConditions(false /*sending*/,
                    null /*commandToRunAfterActionConditionResolved*/);
        }
    }

    public void retrySend(final String messageId) {
        if (isReadyForAction()) {
            if (ensureKnownRecipients()) {
                mBinding.getData().resendMessage(mBinding, messageId);
            }
        } else {
            warnOfMissingActionConditions(true /*sending*/,
                    new Runnable() {
                        @Override
                        public void run() {
                            retrySend(messageId);
                        }

                    });
        }
    }

    private void lockSelectedMessage() {
        BugleAnalytics.logEvent("Detailpage_Messages_Lock", true);
        for (ConversationMessageData data : mSelectMessageDataList) {
            mBinding.getData().lockMessage(mBinding, data.getMessageId());
        }
    }

    private void unlockSelectedMessage() {
        BugleAnalytics.logEvent("Detailpage_Messages_Unlock", false);
        for (ConversationMessageData data : mSelectMessageDataList) {
            mBinding.getData().unlockMessage(mBinding, data.getMessageId());
        }
    }

    void deleteMessage() {
        if (isReadyForAction()) {
            final BaseAlertDialog.Builder builder = new BaseAlertDialog.Builder(getActivity())
                    .setTitle(R.string.delete_message_confirmation_dialog_title)
                    .setMessage(R.string.delete_message_confirmation_dialog_text)
                    .setPositiveButton(R.string.delete_message_confirmation_button,
                            new OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    resetActionModeAndAnimation();
                                    Threads.postOnMainThreadDelayed(() -> deleteSelectMessage(), 500);
                                }
                            })
                    .setNegativeButton(android.R.string.cancel, null);

            builder.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                }
            });
            builder.show();
        } else {
            warnOfMissingActionConditions(false /*sending*/,
                    null /*commandToRunAfterActionConditionResolved*/);
        }
    }

    private void deleteSelectMessage() {
        for (ConversationMessageData data : mSelectMessageDataList) {
            mBinding.getData().deleteMessage(mBinding, data.getMessageId());
        }
    }

    public void deleteConversation() {
        if (isReadyForAction()) {
            final Context context = getActivity();
            mBinding.getData().deleteConversation(mBinding);
            closeConversation(mConversationId);
        } else {
            warnOfMissingActionConditions(false /*sending*/,
                    null /*commandToRunAfterActionConditionResolved*/);
        }
    }

    @Override
    public void closeConversation(final String conversationId) {
        if (TextUtils.equals(conversationId, mConversationId)) {
            mHost.onFinishCurrentConversation();
            // TODO: Explicitly transition to ConversationList (or just go back)?
        }
    }

    @Override
    public void onConversationParticipantDataLoaded(final ConversationData data) {
        mBinding.ensureBound(data);
        if (mBinding.getData().getParticipantsLoaded()) {
            final boolean oneOnOne = mBinding.getData().getOtherParticipant() != null;
            mAdapter.setOneOnOne(oneOnOne, true /* invalidate */);

            // refresh the options menu which will enable the "people & options" item.
            invalidateOptionsMenu();

            mHost.invalidateActionBar();

            mRecyclerView.setVisibility(View.VISIBLE);
            mHost.onConversationParticipantDataLoaded
                    (mBinding.getData().getNumberOfParticipantsExcludingSelf());
        }
    }

    @Override
    public void onSubscriptionListDataLoaded(final ConversationData data) {
        mBinding.ensureBound(data);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void promptForSelfPhoneNumber() {
        if (mComposeMessageView != null) {
            // Avoid bug in system which puts soft keyboard over dialog after orientation change
            ImeUtil.hideSoftInput(getActivity(), mComposeMessageView);
        }

        final FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        final EnterSelfPhoneNumberDialog dialog = EnterSelfPhoneNumberDialog
                .newInstance(getConversationSelfSubId());
        dialog.setTargetFragment(this, 0/*requestCode*/);
        dialog.show(ft, null/*tag*/);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (mChangeDefaultSmsAppHelper == null) {
            mChangeDefaultSmsAppHelper = new ChangeDefaultSmsAppHelper();
        }
        mChangeDefaultSmsAppHelper.handleChangeDefaultSmsResult(requestCode, resultCode, null);
    }

    public boolean hasMessages() {
        return mAdapter != null && mAdapter.getItemCount() > 0;
    }

    public boolean onBackPressed() {
        if (mAdapter.isMultiSelectMode()) {
            resetActionModeAndAnimation();
            return true;
        }
        if (mCameraGalleryFragment != null) {
            hideMediaPicker();
            return true;
        }
        return mComposeMessageView.onBackPressed();
    }

    public boolean onNavigationUpPressed() {
        if (mCameraGalleryFragment != null) {
            hideMediaPicker();
            return true;
        }
        return mComposeMessageView.onNavigationUpPressed();
    }

    @Override
    public boolean onAttachmentClick(final ConversationMessageData data,
                                     final MessagePartData attachment, final Rect imageBounds, final boolean longPress) {
        if (longPress) {
            selectMessage(data, attachment);
            return true;
        } else if (data.getOneClickResendMessage()) {
            handleMessageClick(data);
            return true;
        }

        if (attachment.isImage()) {
            displayPhoto(attachment.getContentUri(), imageBounds, false /* isDraft */);
        }

        if (attachment.isVCard()) {
            UIIntents.get().launchVCardDetailActivity(getActivity(), attachment.getContentUri());
        }

        return false;
    }

    private void handleMessageClick(final ConversationMessageData data) {
        if (data != mSelectedMessageData) {
            final boolean isReadyToSend = isReadyForAction();
            if (data.getOneClickResendMessage()) {
                // Directly resend the message on tap if it's failed
                retrySend(data.getMessageId());
            } else if (data.getShowResendMessage() && isReadyToSend) {
                // Select the message to show the resend/download/delete options
                selectMessage(data);
            } else if (data.getShowDownloadMessage() && isReadyToSend) {
                // Directly download the message on tap
                retryDownload(data.getMessageId());
            } else {
                // Let the toast from warnOfMissingActionConditions show and skip
                // selecting
                warnOfMissingActionConditions(false /*sending*/,
                        null /*commandToRunAfterActionConditionResolved*/);
            }
        }
    }

    private static class AttachmentToSave {
        public final Uri uri;
        public final String contentType;
        public Uri persistedUri;

        AttachmentToSave(final Uri uri, final String contentType) {
            this.uri = uri;
            this.contentType = contentType;
        }
    }

    public static class SaveAttachmentTask extends SafeAsyncTask<Void, Void, Void> {
        private final Context mContext;
        private final List<AttachmentToSave> mAttachmentsToSave = new ArrayList<>();

        public SaveAttachmentTask(final Context context, final Uri contentUri,
                                  final String contentType) {
            mContext = context;
            addAttachmentToSave(contentUri, contentType);
        }

        public SaveAttachmentTask(final Context context) {
            mContext = context;
        }

        public void addAttachmentToSave(final Uri contentUri, final String contentType) {
            mAttachmentsToSave.add(new AttachmentToSave(contentUri, contentType));
        }

        public int getAttachmentCount() {
            return mAttachmentsToSave.size();
        }

        @Override
        protected Void doInBackgroundTimed(final Void... arg) {
            final File appDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES),
                    mContext.getResources().getString(R.string.app_name));
            final File downloadDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            for (final AttachmentToSave attachment : mAttachmentsToSave) {
                final boolean isImageOrVideo = ContentType.isImageType(attachment.contentType)
                        || ContentType.isVideoType(attachment.contentType);
                attachment.persistedUri = UriUtil.persistContent(attachment.uri,
                        isImageOrVideo ? appDir : downloadDir, attachment.contentType);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            int failCount = 0;
            int imageCount = 0;
            int videoCount = 0;
            int otherCount = 0;
            for (final AttachmentToSave attachment : mAttachmentsToSave) {
                if (attachment.persistedUri == null) {
                    failCount++;
                    continue;
                }

                // Inform MediaScanner about the new file
                final Intent scanFileIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                scanFileIntent.setData(attachment.persistedUri);
                mContext.sendBroadcast(scanFileIntent);

                if (ContentType.isImageType(attachment.contentType)) {
                    imageCount++;
                } else if (ContentType.isVideoType(attachment.contentType)) {
                    videoCount++;
                } else {
                    otherCount++;
                    // Inform DownloadManager of the file so it will show in the "downloads" app
                    final DownloadManager downloadManager =
                            (DownloadManager) mContext.getSystemService(
                                    Context.DOWNLOAD_SERVICE);
                    final String filePath = attachment.persistedUri.getPath();
                    final File file = new File(filePath);

                    if (file.exists()) {
                        downloadManager.addCompletedDownload(
                                file.getName() /* title */,
                                mContext.getString(
                                        R.string.attachment_file_description) /* description */,
                                true /* isMediaScannerScannable */,
                                attachment.contentType,
                                file.getAbsolutePath(),
                                file.length(),
                                false /* showNotification */);
                    }
                }
            }

            String message;
            if (failCount > 0) {
                message = mContext.getResources().getQuantityString(
                        R.plurals.attachment_save_error, failCount, failCount);
            } else {
                int messageId = R.plurals.attachments_saved;
                if (otherCount > 0) {
                    if (imageCount + videoCount == 0) {
                        messageId = R.plurals.attachments_saved_to_downloads;
                    }
                } else {
                    if (videoCount == 0) {
                        messageId = R.plurals.photos_saved_to_album;
                    } else if (imageCount == 0) {
                        messageId = R.plurals.videos_saved_to_album;
                    } else {
                        messageId = R.plurals.attachments_saved_to_album;
                    }
                }
                final String appName = mContext.getResources().getString(R.string.app_name);
                final int count = imageCount + videoCount + otherCount;
                message = mContext.getResources().getQuantityString(
                        messageId, count, count, appName);
            }
            UiUtils.showToastAtBottom(message);
        }
    }

    private void invalidateOptionsMenu() {
        final Activity activity = getActivity();
        // TODO: Add the supportInvalidateOptionsMenu call to the host activity.
        if (activity == null || !(activity instanceof BugleActionBarActivity)) {
            return;
        }
        ((BugleActionBarActivity) activity).supportInvalidateOptionsMenu();
    }

    @Override
    public void setOptionsMenuVisibility(final boolean visible) {
        setHasOptionsMenu(visible);
    }

    @Override
    public int getConversationSelfSubId() {
        final String selfParticipantId = mComposeMessageView.getConversationSelfId();
        final ParticipantData self = mBinding.getData().getSelfParticipantById(selfParticipantId);
        // If the self id or the self participant data hasn't been loaded yet, fallback to
        // the default setting.
        return self == null ? ParticipantData.DEFAULT_SELF_SUB_ID : self.getSubId();
    }

    @Override
    public void invalidateActionBar() {
        mHost.invalidateActionBar();
    }

    @Override
    public void dismissActionMode() {
        mHost.dismissActionMode();
    }

    @Override
    public void selectSim(final SubscriptionListEntry subscriptionData) {
        mComposeMessageView.selectSim(subscriptionData);
        mHost.onStartComposeMessage();
    }

    @Override
    public void onStartComposeMessage() {
        mHost.onStartComposeMessage();
    }

    @Override
    public SubscriptionListEntry getSubscriptionEntryForSelfParticipant(
            final String selfParticipantId, final boolean excludeDefault) {
        // TODO: ConversationMessageView is the only one using this. We should probably
        // inject this into the view during binding in the ConversationMessageAdapter.
        return mBinding.getData().getSubscriptionEntryForSelfParticipant(selfParticipantId,
                excludeDefault);
    }

    @Override
    public SimSelectorView getSimSelectorView() {
        return (SimSelectorView) getView().findViewById(R.id.sim_selector);
    }

    @Override
    public MediaPickerFragment createMediaPicker() {
        return MediaPickerFragment.newInstance();
    }

    @Override
    public EmojiPickerFragment createEmojiPicker() {
        return EmojiPickerFragment.newInstance();
    }

    @Override
    public void notifyOfAttachmentLoadFailed() {
        UiUtils.showToastAtBottom(R.string.attachment_load_failed_dialog_message);
    }

    @Override
    public void warnOfExceedingMessageLimit(final boolean sending, final boolean tooManyVideos) {
        warnOfExceedingMessageLimit(sending, mComposeMessageView, mConversationId,
                getActivity(), tooManyVideos);
    }

    public static void warnOfExceedingMessageLimit(final boolean sending,
                                                   final ComposeMessageView composeMessageView, final String conversationId,
                                                   final Activity activity, final boolean tooManyVideos) {
        final BaseAlertDialog.Builder builder =
                new BaseAlertDialog.Builder(activity)
                        .setTitle(R.string.mms_attachment_limit_reached);

        if (sending) {
            if (tooManyVideos) {
                builder.setMessage(R.string.video_attachment_limit_exceeded_when_sending);
            } else {
                builder.setMessage(R.string.attachment_limit_reached_dialog_message_when_sending)
                        .setNegativeButton(R.string.attachment_limit_reached_send_anyway,
                                new OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog,
                                                        final int which) {
                                        composeMessageView.sendMessageIgnoreMessageSizeLimit();
                                    }
                                });
            }
            builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    showAttachmentChooser(conversationId, activity);
                }
            });
        } else {
            builder.setMessage(R.string.attachment_limit_reached_dialog_message_when_composing)
                    .setPositiveButton(android.R.string.ok, null);
        }
        builder.show();
    }

    @Override
    public void showAttachmentChooser() {
        showAttachmentChooser(mConversationId, getActivity());
    }

    public static void showAttachmentChooser(final String conversationId,
                                             final Activity activity) {
        UIIntents.get().launchAttachmentChooserActivity(activity,
                conversationId, REQUEST_CHOOSE_ATTACHMENTS);
    }


    public void updateActionBar(final ActionBar actionBar, final TextView tvTitle) {
        // We update this regardless of whether or not the action bar is showing so that we
        // don't get a race when it reappears.
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Reset the back arrow to its default
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back);

        if (!TextUtils.isEmpty(((ConversationActivity) getActivity()).getConversationName())) {
            tvTitle.setText(((ConversationActivity) getActivity()).getConversationName());
        } else {
            final String conversationName = getConversationName();
            if (!TextUtils.isEmpty(conversationName)) {
                // RTL : To format conversation title if it happens to be phone numbers.
                final BidiFormatter bidiFormatter = BidiFormatter.getInstance();
                final String formattedName = bidiFormatter.unicodeWrap(
                        UiUtils.commaEllipsize(
                                conversationName,
                                tvTitle.getPaint(),
                                tvTitle.getWidth(),
                                getString(R.string.plus_one),
                                getString(R.string.plus_n)).toString(),
                        TextDirectionHeuristicsCompat.LTR);
                tvTitle.setText(formattedName);
            } else {
                final String appName = getString(R.string.app_name);
                tvTitle.setText(appName);
            }
        }
        actionBar.show();
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean shouldHideAttachmentsWhenSimSelectorShown() {
        return false;
    }

    @Override
    public void showHideSimSelector(final boolean show) {
        // no-op for now
    }

    @Override
    public int getSimSelectorItemLayoutId() {
        return R.layout.sim_selector_item_view;
    }

    @Override
    public void onAttachmentsChanged(final boolean haveAttachments) {
        // no-op for now
    }

    @Override
    public void onClickMediaOrEmoji() {
    }

    @Override
    public Activity getHostActivity() {
        return getActivity();
    }

    @Override
    public void onDraftChanged(final DraftMessageData data, final int changeFlags) {
        mDraftMessageDataModel.ensureBound(data);
        // We're specifically only interested in ATTACHMENTS_CHANGED from the widget. Ignore
        // other changes. When the widget changes an attachment, we need to reload the draft.
        if (changeFlags ==
                (DraftMessageData.WIDGET_CHANGED | DraftMessageData.ATTACHMENTS_CHANGED)) {
            mClearLocalDraft = true;        // force a reload of the draft in onResume
        }
    }

    @Override
    public void onDraftAttachmentLimitReached(final DraftMessageData data) {
        // no-op for now
    }

    @Override
    public void onDraftAttachmentLoadFailed() {
        // no-op for now
    }

    @Override
    public boolean isCameraOrGalleryShowing() {
        return mCameraGalleryFragment != null;
    }

    @Override
    public int getAttachmentsClearedFlags() {
        return DraftMessageData.ATTACHMENTS_CHANGED;
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case EVENT_SHOW_OPTION_MENU:
                setOptionsMenuVisibility(true);
                break;
            case EVENT_HIDE_OPTION_MENU:
                setOptionsMenuVisibility(false);
                break;
            case EVENT_HIDE_MEDIA_PICKER:
                hideMediaPicker();
                break;
            case RESET_ITEM:
                resetActionModeAndAnimation();
                break;
            case EVENT_UPDATE_BUBBLE_DRAWABLE:
                // update all drawables
                mAdapter.notifyDataSetChanged();
                break;
        }
    }


    private CameraGalleryFragment mCameraGalleryFragment;

    @Override
    public void showCamera() {
        mMediaLayout.setVisibility(View.VISIBLE);
        if (mCameraGalleryFragment == null) {
            initMediaPicker(true);
        }

        getFragmentManagerToUse().beginTransaction().replace(
                R.id.camera_photo_layout,
                mCameraGalleryFragment,
                CameraGalleryFragment.FRAGMENT_TAG).commitAllowingStateLoss();
    }

    @Override
    public void showPhoto() {
        setOptionsMenuVisibility(false);

        mMediaLayout.setVisibility(View.VISIBLE);
        if (mCameraGalleryFragment == null) {
            initMediaPicker(false);
        }

        getFragmentManagerToUse().beginTransaction().replace(
                R.id.camera_photo_layout,
                mCameraGalleryFragment,
                CameraGalleryFragment.FRAGMENT_TAG).commitAllowingStateLoss();
    }

    private void initMediaPicker(boolean isCamera) {
        mCameraGalleryFragment = new CameraGalleryFragment(getActivity(), isCamera);
        setConversationThemeColor(ConversationDrawables.get().getConversationThemeColor());
        mCameraGalleryFragment.setSubscriptionDataProvider(this);
        ImmutableBindingRef<DraftMessageData> mDraftDataModel = BindingBase.createBindingReference(mComposeMessageView.getDraftDataModel());
        mCameraGalleryFragment.setDraftMessageDataModel(mDraftDataModel);
        mCameraGalleryFragment.setListener(new CameraGalleryFragment.MediaPickerListener() {
            @Override
            public void onDismissed() {
                // Re-enable accessibility on all controls now that the media picker is
                // going away.
                handleStateChange();
            }

            private void handleStateChange() {
                mHost.invalidateActionBar();
            }

            @Override
            public void onItemsSelected(final Collection<MessagePartData> items,
                                        final boolean resumeCompose) {
                mComposeMessageView.onMediaItemsSelected(items);
                mHost.invalidateActionBar();
                if (resumeCompose) {
                    hideMediaPicker();
                    mComposeMessageView.resumeComposeMessage(true);
                }
            }

            @Override
            public void onItemUnselected(final MessagePartData item) {
                mComposeMessageView.onMediaItemsUnselected(item);
                mHost.invalidateActionBar();
            }

            @Override
            public void onConfirmItemSelection() {
                hideMediaPicker();
                mComposeMessageView.resumeComposeMessage(true);
            }

            @Override
            public void onPendingItemAdded(final PendingAttachmentData pendingItem) {
                mComposeMessageView.onPendingAttachmentAdded(pendingItem);
            }

            @Override
            public void onChooserSelected(final int chooserIndex) {
                mHost.invalidateActionBar();
                mHost.dismissActionMode();
            }
        });
    }

    public void hideMediaPicker() {
        mMediaLayout.setVisibility(View.GONE);
        if (mCameraGalleryFragment != null) {
            mCameraGalleryFragment.dismiss(false);
            getFragmentManagerToUse()
                    .beginTransaction()
                    .remove(mCameraGalleryFragment)
                    .commit();
            mCameraGalleryFragment = null;
        }
    }

    public void setConversationThemeColor(final int themeColor) {
        if (mCameraGalleryFragment != null) {
            mCameraGalleryFragment.setConversationThemeColor(themeColor);
        }
    }
}
