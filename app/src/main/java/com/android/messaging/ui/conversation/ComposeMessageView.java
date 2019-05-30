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

import android.animation.Animator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.BuildConfig;
import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.datamodel.binding.Binding;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.binding.ImmutableBindingRef;
import com.android.messaging.datamodel.data.ConversationData;
import com.android.messaging.datamodel.data.ConversationData.ConversationDataListener;
import com.android.messaging.datamodel.data.ConversationData.SimpleConversationDataListener;
import com.android.messaging.datamodel.data.DraftMessageData;
import com.android.messaging.datamodel.data.DraftMessageData.CheckDraftForSendTask;
import com.android.messaging.datamodel.data.DraftMessageData.CheckDraftTaskCallback;
import com.android.messaging.datamodel.data.DraftMessageData.DraftMessageDataListener;
import com.android.messaging.datamodel.data.MessageData;
import com.android.messaging.datamodel.data.MessagePartData;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.datamodel.data.PendingAttachmentData;
import com.android.messaging.datamodel.data.SubscriptionListData.SubscriptionListEntry;
import com.android.messaging.sms.MmsConfig;
import com.android.messaging.ui.AttachmentPreview;
import com.android.messaging.ui.BugleActionBarActivity;
import com.android.messaging.ui.PlainTextEditText;
import com.android.messaging.ui.SendDelayProgressBar;
import com.android.messaging.ui.appsettings.SendDelaySettings;
import com.android.messaging.ui.conversation.ConversationInputManager.ConversationInputSink;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.dialog.FiveStarRateDialog;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.ui.sendmessagesdelay.SendMessagesDelayManager;
import com.android.messaging.ui.signature.SignatureSettingDialog;
import com.android.messaging.util.AccessibilityUtil;
import com.android.messaging.util.Assert;
import com.android.messaging.util.AvatarUriUtil;
import com.android.messaging.util.BugleActivityUtil;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.ContentType;
import com.android.messaging.util.ImeUtil;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.MediaUtil;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.TextViewUtil;
import com.android.messaging.util.UiUtils;
import com.android.messaging.font.FontUtils;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This view contains the UI required to generate and send messages.
 */
public class ComposeMessageView extends LinearLayout
        implements TextView.OnEditorActionListener, DraftMessageDataListener, TextWatcher,
        ConversationInputSink{

    public interface IComposeMessageViewHost extends
            DraftMessageData.DraftMessageSubscriptionDataProvider {
        void sendMessage(MessageData message);

        void onComposeEditTextFocused();

        void onAttachmentsCleared();

        void onAttachmentsChanged(final boolean haveAttachments);

        void displayPhoto(Uri photoUri, Rect imageBounds, boolean isDraft);

        void promptForSelfPhoneNumber();

        boolean isReadyForAction();

        void warnOfMissingActionConditions(final boolean sending,
                                           final Runnable commandToRunAfterActionConditionResolved);

        void warnOfExceedingMessageLimit(final boolean showAttachmentChooser,
                                         boolean tooManyVideos);

        void notifyOfAttachmentLoadFailed();

        void showAttachmentChooser();

        boolean shouldShowSubjectEditor();

        boolean shouldHideAttachmentsWhenSimSelectorShown();

        Uri getSelfSendButtonIconUri();

        int getAttachmentsClearedFlags();

        boolean isCameraOrGalleryShowing();

        void onClickMediaOrEmoji();
    }

    private static final String TAG = ComposeMessageView.class.getSimpleName();

    private static final int DISTANCE_SLOP = Dimensions.pxFromDp(90);

    // There is no draft and there is no need for the SIM selector
    private static final int SEND_WIDGET_MODE_SELF_AVATAR = 1;
    // There is no draft but we need to show the SIM selector
    private static final int SEND_WIDGET_MODE_SIM_SELECTOR = 2;
    // There is a draft
    private static final int SEND_WIDGET_MODE_SEND_BUTTON = 3;

    private PlainTextEditText mComposeEditText;
    private PlainTextEditText mComposeSubjectText;
    private TextView mMmsIndicator;
    private SimIconView mSelfSendIcon;
    private ImageView mSendButton;
    private ImageView mDelayCloseButton;
    private SendDelayProgressBar mSendDelayProgressBar;
    private View mSubjectView;
    private ImageButton mDeleteSubjectButton;
    private AttachmentPreview mAttachmentPreview;
    private ImageView mAttachMediaButton;
    private ImageView mEmojiKeyboardBtn;
    private ImageView mEmojiGuideView;
    private LottieAnimationView mEmojiLottieGuideView;
    private LinearLayout mInputLayout;
    private FrameLayout mMediaPickerLayout;
    private FrameLayout mEmojiPickerLayout;

    private List<String> mEmojiLogCodeList;
    private List<String> mMagicStickerLogNameList;
    private List<String> mStickerLogNameList;

    private boolean mIsMediaPendingShow = false;
    private boolean mIsEmojiPendingShow = false;

    private final Binding<DraftMessageData> mBinding;
    private IComposeMessageViewHost mHost;
    private final Context mOriginalContext;
    private int mSendWidgetMode = SEND_WIDGET_MODE_SELF_AVATAR;
    private String mSignatureStr;
    private boolean mIsWaitingToSendMessage;
    private Runnable mSendDelayRunnable;
    private long mMillisecondsAnimated;
    ForegroundColorSpan mSignatureSpan = new ForegroundColorSpan(0xb3222327);

    private SendDelayActionCompletedCallBack mSendDelayActionCompletedCallBack;

    // Shared data model object binding from the conversation.
    private ImmutableBindingRef<ConversationData> mConversationDataModel;

    // Centrally manages all the mutual exclusive UI components accepting user input, i.e.
    // media picker, IME keyboard and SIM selector.
    private ConversationInputManager mInputManager;

    private final ConversationDataListener mDataListener = new SimpleConversationDataListener() {
        @Override
        public void onConversationMetadataUpdated(ConversationData data) {
            if (mConversationDataModel != null) {
                mConversationDataModel.ensureBound(data);
            }

            updateVisualsOnDraftChanged();
        }

        @Override
        public void onConversationParticipantDataLoaded(ConversationData data) {
            if (mConversationDataModel != null) {
                mConversationDataModel.ensureBound(data);
            }
            updateVisualsOnDraftChanged();
        }

        @Override
        public void onSubscriptionListDataLoaded(ConversationData data) {
            if (mConversationDataModel != null) {
                mConversationDataModel.ensureBound(data);
            }
            updateOnSelfSubscriptionChange();
            updateVisualsOnDraftChanged();
        }
    };

    public ComposeMessageView(final Context context, final AttributeSet attrs) {
        super(new ContextThemeWrapper(context, R.style.ColorAccentBlueOverrideStyle), attrs);
        mOriginalContext = context;
        mBinding = BindingBase.createBinding(this);
        mSignatureStr = Preferences.getDefault().getString(SignatureSettingDialog.PREF_KEY_SIGNATURE_CONTENT, null);
    }

    /**
     * Host calls this to bind view to DraftMessageData object
     */
    public void bind(final DraftMessageData data, final IComposeMessageViewHost host) {
        HSLog.d(TAG,"bind ");

        mHost = host;
        mBinding.bind(data);
        data.addListener(this);
        data.setSubscriptionDataProvider(host);

        long lastSendDelayActionStartSystemTime;
        String conversationId = mBinding.getData().getConversationId();
        SendMessagesDelayManager.SendMessagesDelayData globalSendMessagesDelayData = SendMessagesDelayManager.getSendMessagesDelayValue(conversationId);
        if (globalSendMessagesDelayData != null) {
            long firstSendDelayActionStartSystemTime = globalSendMessagesDelayData.getLastSendDelayActionStartSystemTime();
            HSLog.d(TAG,"" + globalSendMessagesDelayData);
            lastSendDelayActionStartSystemTime = globalSendMessagesDelayData.getLastSendDelayActionStartSystemTime();
            mMillisecondsAnimated = System.currentTimeMillis() - lastSendDelayActionStartSystemTime;
            HSLog.d(TAG, "" + mMillisecondsAnimated);
            Threads.removeOnMainThread(globalSendMessagesDelayData.getRunnable());
            SendMessagesDelayManager.remove(conversationId);
            startMessageSendDelayAction(firstSendDelayActionStartSystemTime);
        }
    }

    /**
     * Host calls this to unbind view
     */
    public void unbind() {
        HSLog.d(TAG, "unbind");
        mBinding.unbind();
        mHost = null;
        mInputManager.onDetach();
    }

    protected boolean getIsMessageSendFlag(){
        return mIsWaitingToSendMessage;
    }

    public void setOnActionEndListener(SendDelayActionCompletedCallBack listener) {
        this.mSendDelayActionCompletedCallBack = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mComposeEditText.setOnEditorActionListener(null);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mInputLayout = findViewById(R.id.input_layout);
        mComposeEditText = findViewById(R.id.compose_message_text);
        TextViewUtil.setCursorPointColor(mComposeEditText, PrimaryColors.DEFAULT_PRIMARY_COLOR);
        mComposeEditText.setTypeface(FontUtils.getTypeface());
        mComposeEditText.setOnEditorActionListener(this);
        mComposeEditText.addTextChangedListener(this);
        mComposeEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, final boolean hasFocus) {
                if (v == mComposeEditText && hasFocus) {
                    mHost.onComposeEditTextFocused();
                }
            }
        });
        mComposeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mHost.onClickMediaOrEmoji();

                if (mHost.shouldHideAttachmentsWhenSimSelectorShown()) {
                    hideSimSelector();
                }
                BugleAnalytics.logEvent("SMS_DetailsPage_DialogBox_Click", true, true);
            }
        });

        // onFinishInflate() is called before self is loaded from db. We set the default text
        // limit here, and apply the real limit later in updateOnSelfSubscriptionChange().
        mComposeEditText.setFilters(new InputFilter[]{
                new LengthFilter(MmsConfig.get(ParticipantData.DEFAULT_SELF_SUB_ID)
                        .getMaxTextLimit())});

        mComposeEditText.getViewTreeObserver().addOnPreDrawListener(() -> {
            if (mIsEmojiPendingShow || mIsMediaPendingShow) {
                if (isKeyboardVisible()) {
                    return false;
                } else if (mIsEmojiPendingShow) {
                    showEmojiPicker();
                    mIsEmojiPendingShow = false;
                    return false;
                } else if (mIsMediaPendingShow) {
                    showMediaPicker();
                    mIsMediaPendingShow = false;
                    return false;
                }
            } else {
                if (isKeyboardVisible()) {
                    if (isEmojiPickerShowing()) {
                        hideEmojiPicker();
                        return false;
                    } else if (isMediaPickerShowing()) {
                        hideMediaPicker();
                        return false;
                    }
                }
            }
            return true;
        });

        mSelfSendIcon = findViewById(R.id.self_send_icon);
        mSelfSendIcon.setOnClickListener(v -> {
            HSLog.d(TAG,"mSelfSendIcon.setOnClickListener Run");
            BugleAnalytics.logEvent("Detailpage_BtnSend_Click", "SendDelay", "" + SendDelaySettings.getSendDelayInSecs());
            SubscriptionListEntry entry = getSelfSubscriptionListEntry();
            boolean shown = false;
            if (entry != null) {
                shown = mInputManager.toggleSimSelector(true /* animate */, entry);
            }
            hideAttachmentsWhenShowingSims(shown);
        });
        mSelfSendIcon.setOnLongClickListener(v -> {
            if (mHost.shouldShowSubjectEditor()) {
                showSubjectEditor();
            } else {
                SubscriptionListEntry entry = getSelfSubscriptionListEntry();
                boolean shown = false;
                if (entry != null) {
                    shown = mInputManager.toggleSimSelector(true /* animate */, entry);
                }
                hideAttachmentsWhenShowingSims(shown);
            }
            return true;
        });

        mComposeSubjectText = (PlainTextEditText) findViewById(
                R.id.compose_subject_text);
        // We need the listener to change the avatar to the send button when the user starts
        // typing a subject without a message.
        mComposeSubjectText.addTextChangedListener(this);
        // onFinishInflate() is called before self is loaded from db. We set the default text
        // limit here, and apply the real limit later in updateOnSelfSubscriptionChange().
        mComposeSubjectText.setFilters(new InputFilter[]{
                new LengthFilter(MmsConfig.get(ParticipantData.DEFAULT_SELF_SUB_ID)
                        .getMaxSubjectLength())});

        mDeleteSubjectButton = (ImageButton) findViewById(R.id.delete_subject_button);
        mDeleteSubjectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View clickView) {
                hideSubjectEditor();
                mComposeSubjectText.setText(null);
                mBinding.getData().setMessageSubject(null);
            }
        });
        mSendDelayRunnable = () -> {

            HSLog.d(TAG, "mSendDelayRunnable");
            logEmojiEvent();
//            sendMessageInternal(true /* checkMessageSize */);

            final MessageData message = mBinding.getData()
                    .prepareMessageForSending(mBinding);
            playSentSound();
            mHost.sendMessage(message);

            mDelayCloseButton.setVisibility(View.GONE);
            mSendDelayProgressBar.setVisibility(View.GONE);
            mSelfSendIcon.setVisibility(View.VISIBLE);
            mIsWaitingToSendMessage = false;
            String conversationId = mBinding.getData().getConversationId();
            updateVisualsOnDraftChanged();
            resetDelaySendAnimation();
            SendMessagesDelayManager.remove(conversationId);
            if (mSendDelayActionCompletedCallBack != null) {
                mSendDelayActionCompletedCallBack.onSendDelayActionEnd();
            }
        };
        mSubjectView = findViewById(R.id.subject_view);
        mSendButton = findViewById(R.id.send_message_button);
        mDelayCloseButton = findViewById(R.id.delay_close_button);
        mSendDelayProgressBar = findViewById(R.id.send_delay_circle_bar);
        mSendButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                PrimaryColors.getPrimaryColorDark(),
                Dimensions.pxFromDp(29), false, true));
        mSendButton.setOnClickListener(clickView -> {
            HSLog.d(TAG, "mSendButton.setOnClickListener Run");
            BugleAnalytics.logEvent("Detailpage_BtnSend_Click", "SendDelay", "" + SendDelaySettings.getSendDelayInSecs());
            startMessageSendDelayAction(System.currentTimeMillis());
        });

        mSendButton.setOnLongClickListener(arg0 -> {
            SubscriptionListEntry entry = getSelfSubscriptionListEntry();
            boolean shown = false;
            if (entry != null) {
                shown = mInputManager.toggleSimSelector(true /* animate */, entry);
            }
            hideAttachmentsWhenShowingSims(shown);
            if (mHost.shouldShowSubjectEditor()) {
                showSubjectEditor();
            }
            return true;
        });
        mSendButton.setAccessibilityDelegate(new AccessibilityDelegate() {
            @Override
            public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
                super.onPopulateAccessibilityEvent(host, event);
                // When the send button is long clicked, we want TalkBack to announce the real
                // action (select SIM or edit subject), as opposed to "long press send button."
                if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
                    event.getText().clear();
                    event.getText().add(getResources()
                            .getText(mConversationDataModel != null && shouldShowSimSelector(mConversationDataModel.getData()) ?
                                    R.string.send_button_long_click_description_with_sim_selector :
                                    R.string.send_button_long_click_description_no_sim_selector));
                    // Make this an announcement so TalkBack will read our custom message.
                    event.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                }
            }
        });

        mMediaPickerLayout = findViewById(R.id.media_picker_container);
        mAttachMediaButton =
                findViewById(R.id.media_btn);
        mAttachMediaButton.setBackground(BackgroundDrawables.createBackgroundDrawable(0xfff4f7f9, 0x1935363b, Dimensions.pxFromDp(20), false, true));
        mAttachMediaButton.setOnClickListener(v -> {
            mHost.onClickMediaOrEmoji();

            if (isMediaPickerShowing()) {
                showKeyboard();
            } else if (isEmojiPickerShowing()) {
                hideEmojiPicker();
                showMediaPicker();
            } else if (isKeyboardVisible()) {
                mIsMediaPendingShow = true;
                hideKeyboard();
            } else {
                showMediaPicker();
            }
            BugleAnalytics.logEvent("SMS_DetailsPage_IconPlus_Click", true, true);
        });

        mAttachmentPreview = (AttachmentPreview) findViewById(R.id.attachment_draft_view);
        mAttachmentPreview.setComposeMessageView(this);

        mMmsIndicator = (TextView) findViewById(R.id.mms_indicator);
        mEmojiGuideView = findViewById(R.id.emoji_guide_view);
        mEmojiLottieGuideView = findViewById(R.id.emoji_lottie_guide_view);
        if (EmojiManager.isShowEmojiGuide()) {
            mEmojiGuideView.setVisibility(VISIBLE);
        }
        Preferences.getDefault().doLimitedTimes(new Runnable() {
            @Override
            public void run() {
                if (EmojiManager.isShowEmojiGuide()) {
                    mEmojiLottieGuideView.setVisibility(View.VISIBLE);
                    mEmojiLottieGuideView.setImageAssetsFolder("lottie/emoji_input_guide/");
                    mEmojiLottieGuideView.setAnimation("lottie/emoji_input_guide.json");
                    mEmojiLottieGuideView.addAnimatorListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mEmojiKeyboardBtn.setImageResource(android.R.color.transparent);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mEmojiKeyboardBtn.setImageResource(R.drawable.input_emoji_icon);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    new Handler().postDelayed(() -> mEmojiLottieGuideView.playAnimation(), 180);

                }
            }
        }, "pref_key_emoji_lottie_guide", 1);
        mEmojiPickerLayout = findViewById(R.id.emoji_picker_container);
        mEmojiKeyboardBtn = findViewById(R.id.emoji_btn);
        mEmojiKeyboardBtn.setOnClickListener(v -> {
            mHost.onClickMediaOrEmoji();

            mEmojiLottieGuideView.setVisibility(View.GONE);
            mEmojiGuideView.setVisibility(View.GONE);
            if (isEmojiPickerShowing()) {
                BugleAnalytics.logEvent("SMSEmoji_Chat_Keyboard_Click");
                showKeyboard();
            } else if (isMediaPickerShowing()) {
                hideMediaPicker();
                showEmojiPicker();
            } else if (isKeyboardVisible()) {
                mIsEmojiPendingShow = true;
                hideKeyboard();
            } else {
                showEmojiPicker();
            }
        });
    }

    private void resetDelaySendAnimation(){
        HSLog.d(TAG, "resetDelaySendAnimation()");
        mDelayCloseButton.setAlpha(0.0f);
        mSendDelayProgressBar.setAlpha(0.0f);
        mSendDelayProgressBar.setScaleX(0.8f);
        mSendDelayProgressBar.setScaleY(0.8f);
        mSendDelayProgressBar.resetAnimation();
        mSendButton.setScaleX(1.0f);
        mSendButton.setScaleY(1.0f);
        mSendButton.setAlpha(1.0f);
        mSendDelayProgressBar.setProgress(100);
    }

    private void startMessageSendDelayAction(long firstSendDelayActionStartSystemTime){
        mDelayCloseButton.setVisibility(View.VISIBLE);
        mSendDelayProgressBar.setVisibility(View.VISIBLE);
        mSelfSendIcon.setVisibility(View.GONE);

        mDelayCloseButton.animate().alpha(1.0f).setDuration(160).setStartDelay(80).start();
        Interpolator scaleStartInterpolator =
                PathInterpolatorCompat.create(0.0f, 0.0f, 0.58f, 1.0f);
        mSendDelayProgressBar.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration(160).setStartDelay(80).setInterpolator(scaleStartInterpolator).start();
        mIsWaitingToSendMessage = true;
        mSendButton.setVisibility(View.GONE);
        if (mMillisecondsAnimated != 0) {
            mSendDelayProgressBar.setProgress(100 - (float) ((mMillisecondsAnimated * 100) / (1000 * SendDelaySettings.getSendDelayInSecs())));
        }
        Threads.postOnMainThreadDelayed(mSendDelayRunnable,1000 * SendDelaySettings.getSendDelayInSecs() - mMillisecondsAnimated);

        String conversationId = mBinding.getData().getConversationId();
        SendMessagesDelayManager.SendMessagesDelayData globalSendMessagesDelayData = SendMessagesDelayManager.getSendMessagesDelayValue(conversationId);

        if(globalSendMessagesDelayData == null) {
            SendMessagesDelayManager.SendMessagesDelayData sendMessagesDelayData = new SendMessagesDelayManager.SendMessagesDelayData();
            sendMessagesDelayData.setRunnable(mSendDelayRunnable);
            SendMessagesDelayManager.putSendMessagesDelayValue((conversationId), sendMessagesDelayData);
        }
        SendMessagesDelayManager.getSendMessagesDelayValue(conversationId).setLastSendDelayActionStartSystemTime(firstSendDelayActionStartSystemTime);

        mSendDelayProgressBar.startAnimation(SendDelaySettings.getSendDelayInSecs() - (mMillisecondsAnimated / 1000));
        mMillisecondsAnimated = 0;
        mSendDelayProgressBar.setOnClickListener(clickedView -> {
            HSLog.d(TAG, "mDelayCloseButton.setOnClickListener");
            mDelayCloseButton.setVisibility(View.GONE);
            mSendDelayProgressBar.setVisibility(View.GONE);
            mSelfSendIcon.setVisibility(View.VISIBLE);
            mIsWaitingToSendMessage = false;
            Threads.removeOnMainThread(mSendDelayRunnable);
            updateVisualsOnDraftChanged();
            resetDelaySendAnimation();
            BugleAnalytics.logEvent("Detailpage_BtnCancel_Click");
        });
    }

    private boolean isMediaPickerShowing() {
        return mMediaPickerLayout.getVisibility() == VISIBLE;
    }

    private void showMediaPicker() {
        mMediaPickerLayout.setVisibility(VISIBLE);
        mAttachMediaButton.setImageResource(R.drawable.input_keyboard_black_icon);
        mInputManager.showMediaPicker();
    }

    @Override
    public void hideMediaPickerView() {
        mMediaPickerLayout.setVisibility(GONE);
        mAttachMediaButton.setImageResource(R.drawable.input_media_icon);
    }

    private void hideMediaPicker() {
        hideMediaPickerView();
        mInputManager.hideMediaPicker();
    }

    private boolean isKeyboardVisible() {
        return (getDistanceFromInputToBottom() > DISTANCE_SLOP && !isEmojiPickerShowing() && !isMediaPickerShowing())
                || (getDistanceFromInputToBottom() > (mEmojiPickerLayout.getHeight() + DISTANCE_SLOP) && isEmojiPickerShowing())
                || (getDistanceFromInputToBottom() > (mMediaPickerLayout.getHeight() + DISTANCE_SLOP) && isMediaPickerShowing());
    }

    private boolean isEmojiPickerShowing() {
        return mEmojiPickerLayout.getVisibility() == VISIBLE;
    }

    private int getDistanceFromInputToBottom() {
        return Dimensions.getPhoneHeight(getContext()) - getInputBottom();
    }

    private int getInputBottom() {
        Rect temp = new Rect();
        mInputLayout.getGlobalVisibleRect(temp);
        return temp.bottom;
    }

    private void showKeyboard() {
        ImeUtil.get().showImeKeyboard(getContext(), mComposeEditText);
        if (mHost.shouldHideAttachmentsWhenSimSelectorShown()) {
            hideSimSelector();
        }
    }

    private void hideKeyboard() {
        ImeUtil.get().hideImeKeyboard(getContext(), mComposeEditText);
    }

    private void showEmojiPicker() {
        BugleAnalytics.logEvent("SMSEmoji_Chat_Emoji_Click", true, true);
        if (EmojiManager.isShowEmojiGuide()) {
            EmojiManager.recordAlreadyShowEmojiGuide();
            mEmojiGuideView.setVisibility(GONE);
        }
        mEmojiPickerLayout.setVisibility(VISIBLE);
        mEmojiKeyboardBtn.setImageResource(R.drawable.input_keyboard_icon);
        mInputManager.showEmojiPicker();
    }

    @Override
    public void hideEmojiPickerView() {
        mEmojiPickerLayout.setVisibility(GONE);
        mEmojiKeyboardBtn.setImageResource(R.drawable.input_emoji_icon);
    }

    private void hideEmojiPicker() {
        hideEmojiPickerView();
        mInputManager.hideEmojiPicker();
    }

    private void logEmojiEvent() {
        boolean hasLittleEmoji = false;
        if (mEmojiLogCodeList != null && !mEmojiLogCodeList.isEmpty()) {
            String message = mComposeEditText.getText().toString();
            for (String code : mEmojiLogCodeList) {
                if (message.contains(code)) {
                    BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_LittleEmoji_Send", true, true, "type", code);
                    hasLittleEmoji = true;
                }
            }
            mEmojiLogCodeList.clear();
        }
        boolean hasSticker = mStickerLogNameList != null && !mStickerLogNameList.isEmpty();
        boolean hasMagicSticker = mMagicStickerLogNameList != null && !mMagicStickerLogNameList.isEmpty();
        if (hasLittleEmoji && !hasSticker && !hasMagicSticker) {
            BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Emoji_Send", true, true, "type", "emoji");
        } else if (!hasLittleEmoji && hasSticker && !hasMagicSticker) {
            BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Emoji_Send", true, true, "type", "sticker");
        } else if (!hasLittleEmoji && !hasSticker && hasMagicSticker) {
            BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Emoji_Send", true, true, "type", "magic");
        } else if (hasLittleEmoji || hasSticker || hasMagicSticker) {
            BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Emoji_Send", true, true, "type", "other");
        }
        logEvent("SMSEmoji_ChatEmoji_Tab_Send", mStickerLogNameList);
        logEvent("SMSEmoji_ChatEmoji_Magic_Send", mMagicStickerLogNameList);
    }

    private void logEvent(String eventName, List<String> data) {
        if (data != null && !data.isEmpty()) {
            for (String type : data) {
                if (eventName.contains("Send")) {
                    BugleAnalytics.logEvent(eventName, true, true, "type", type);
                } else {
                    BugleAnalytics.logEvent(eventName, true, "type", type);
                }
            }

            data.clear();
        }
    }

    private void removeEmojiEvent() {
        logEvent("SMSEmoji_ChatEmoji_Tab_Cancel", mStickerLogNameList);
        logEvent("SMSEmoji_ChatEmoji_Magic_Tab_Cancel", mMagicStickerLogNameList);
    }

    @Override
    public void logMagicSticker(String name) {
        if (mMagicStickerLogNameList == null) {
            mMagicStickerLogNameList = new ArrayList<>();
        }
        mMagicStickerLogNameList.add(name);
    }

    @Override
    public void logEmoji(String code) {
        if (mEmojiLogCodeList == null) {
            mEmojiLogCodeList = new ArrayList<>();
        }
        mEmojiLogCodeList.add(code);
    }

    @Override
    public void logSticker(String name) {
        if (mStickerLogNameList == null) {
            mStickerLogNameList = new ArrayList<>();
        }
        mStickerLogNameList.add(name);
    }

    private void hideAttachmentsWhenShowingSims(final boolean simPickerVisible) {
        if (!mHost.shouldHideAttachmentsWhenSimSelectorShown()) {
            return;
        }
        final boolean haveAttachments = mBinding.getData().hasAttachments();
        if (simPickerVisible && haveAttachments) {
            mHost.onAttachmentsChanged(false);
            mAttachmentPreview.hideAttachmentPreview();
        } else {
            mHost.onAttachmentsChanged(haveAttachments);
            mAttachmentPreview.onAttachmentsChanged(mBinding.getData());
        }
    }

    public void setInputManager(final ConversationInputManager inputManager) {
        mInputManager = inputManager;
    }

    public void setConversationDataModel(final ImmutableBindingRef<ConversationData> refDataModel) {
        mConversationDataModel = refDataModel;
        if (mConversationDataModel != null) {
            mConversationDataModel.getData().addConversationDataListener(mDataListener);
        }
    }

    ImmutableBindingRef<DraftMessageData> getDraftDataModel() {
        return BindingBase.createBindingReference(mBinding);
    }

    // returns true if it actually shows the subject editor and false if already showing
    private boolean showSubjectEditor() {
        // show the subject editor
        if (mSubjectView.getVisibility() == View.GONE) {
            mSubjectView.setVisibility(View.VISIBLE);
            mSubjectView.requestFocus();
            return true;
        }
        return false;
    }

    private void hideSubjectEditor() {
        mSubjectView.setVisibility(View.GONE);
        mComposeEditText.requestFocus();
    }

    /**
     * {@inheritDoc} from TextView.OnEditorActionListener
     */
    @Override // TextView.OnEditorActionListener.onEditorAction
    public boolean onEditorAction(final TextView view, final int actionId, final KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendMessageInternal(true /* checkMessageSize */);
            return true;
        }
        return false;
    }

    private void sendMessageInternal(final boolean checkMessageSize) {
        LogUtil.i(LogUtil.BUGLE_TAG, "UI initiated message sending in conversation " +
                mBinding.getData().getConversationId());
        if (mBinding.getData().isCheckingDraft()) {
            // Don't send message if we are currently checking draft for sending.
            LogUtil.w(LogUtil.BUGLE_TAG, "Message can't be sent: still checking draft");
            return;
        }
        // Check the host for pre-conditions about any action.
        if (mHost.isReadyForAction()) {
            mInputManager.showHideSimSelector(false /* show */, true /* animate */);
            final String subject = mComposeSubjectText.getText().toString();
            mBinding.getData().setMessageSubject(subject);

            boolean includeSignature = false;
            Editable inputEditable = mComposeEditText.getText();
            int signatureIndex = 0;
            if (!TextUtils.isEmpty(mSignatureStr)) {
                signatureIndex = inputEditable.getSpanStart(mSignatureSpan);
                if (signatureIndex >= 0 && signatureIndex < inputEditable.length() &&
                        inputEditable.toString().substring(signatureIndex, inputEditable.length()).contains(mSignatureStr)) {
                    includeSignature = true;
                }
            }

            String isContactValue = mBinding.getData().getIsGroupConversation() ? "group"
                    : mBinding.getData().getIsInContact() ? "true" : "false";

            if (mBinding.getData().getIsMms() && !TextUtils.isEmpty(mSignatureStr) && signatureIndex >= 0) {
                if (signatureIndex > 1
                        || !mSignatureStr.equals(inputEditable.toString().substring(signatureIndex, inputEditable.length()))) {
                    mBinding.getData().setMessageText(inputEditable.toString());
                } else {
                    mBinding.getData().setMessageText("");
                }
                BugleAnalytics.logEvent("SMS_DetailsPage_IconSend_Click", true, true, "Type", "MMS",
                        "isContact", isContactValue);
            } else {
                final String messageToSend = mComposeEditText.getText().toString();
                mBinding.getData().setMessageText(messageToSend);
                BugleAnalytics.logEvent("SMS_DetailsPage_IconSend_Click", true, true, "Type", "SMS",
                        "isContact", isContactValue);
            }
            // Asynchronously check the draft against various requirements before sending.
            boolean finalIncludeSignature = includeSignature;
            mBinding.getData().checkDraftForAction(checkMessageSize,
                    mHost.getConversationSelfSubId(), new CheckDraftTaskCallback() {
                        @Override
                        public void onDraftChecked(DraftMessageData data, int result) {
                            boolean sendEmoji = false;
                            mBinding.ensureBound(data);
                            List<MessagePartData> partDataList = data.getReadOnlyAttachments();
                            if (partDataList != null && !partDataList.isEmpty()) {
                                for (MessagePartData partData : partDataList) {
                                    if (ContentType.IMAGE_GIF.equals(partData.getContentType())) {
                                        sendEmoji = true;
                                        break;
                                    }
                                }
                            }
                            switch (result) {
                                case CheckDraftForSendTask.RESULT_PASSED:
                                    // Continue sending after check succeeded.
                                    final MessageData message = mBinding.getData()
                                            .prepareMessageForSending(mBinding);
                                    if (message != null && message.hasContent()) {
                                        boolean finalSendEmoji = sendEmoji;
                                        Threads.postOnMainThreadDelayed(() -> {
                                            if (finalSendEmoji) {
                                                FiveStarRateDialog.showFiveStarWhenSendEmojiIfNeed(BugleActivityUtil.contextToActivitySafely(getContext()));
                                            } else {
                                                FiveStarRateDialog.showFiveStarWhenSendMsgIfNeed(BugleActivityUtil.contextToActivitySafely(getContext()));
                                            }
                                        }, 1600);

                                        playSentSound();
                                        mHost.sendMessage(message);
                                        HSLog.d(TAG, "mHost.sendMessage(message)");
                                        if (!TextUtils.isEmpty(mSignatureStr)) {
                                            BugleAnalytics.logEvent("SMS_WithSignature_Send", true,
                                                    "deleteSignature", String.valueOf(!finalIncludeSignature));
                                        }

                                        hideSubjectEditor();
                                        if (AccessibilityUtil.isTouchExplorationEnabled(getContext())) {
                                            AccessibilityUtil.announceForAccessibilityCompat(
                                                    ComposeMessageView.this, null,
                                                    R.string.sending_message);
                                        }
                                    }
                                    break;

                                case CheckDraftForSendTask.RESULT_HAS_PENDING_ATTACHMENTS:
                                    // Cannot send while there's still attachment(s) being loaded.
                                    UiUtils.showToastAtBottom(
                                            R.string.cant_send_message_while_loading_attachments);
                                    break;

                                case CheckDraftForSendTask.RESULT_NO_SELF_PHONE_NUMBER_IN_GROUP_MMS:
                                    mHost.promptForSelfPhoneNumber();
                                    break;

                                case CheckDraftForSendTask.RESULT_MESSAGE_OVER_LIMIT:
                                    Assert.isTrue(checkMessageSize);
                                    mHost.warnOfExceedingMessageLimit(
                                            true /*sending*/, false /* tooManyVideos */);
                                    break;

                                case CheckDraftForSendTask.RESULT_VIDEO_ATTACHMENT_LIMIT_EXCEEDED:
                                    Assert.isTrue(checkMessageSize);
                                    mHost.warnOfExceedingMessageLimit(
                                            true /*sending*/, true /* tooManyVideos */);
                                    break;

                                case CheckDraftForSendTask.RESULT_SIM_NOT_READY:
                                    // Cannot send if there is no active subscription
                                    UiUtils.showToastAtBottom(
                                            R.string.cant_send_message_without_active_subscription);
                                    break;

                                default:
                                    break;
                            }
                        }
                    }, mBinding);
        } else {
            mHost.warnOfMissingActionConditions(true /*sending*/,
                    new Runnable() {
                        @Override
                        public void run() {
                            sendMessageInternal(checkMessageSize);
                        }

                    });
        }
    }

    public static void playSentSound() {
        // Check if this setting is enabled before playing
        final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();
        final Context context = Factory.get().getApplicationContext();
        final String prefKey = context.getString(R.string.send_sound_pref_key);
        final boolean defaultValue = context.getResources().getBoolean(
                R.bool.send_sound_pref_default);
        if (!prefs.getBoolean(prefKey, defaultValue)) {
            return;
        }
        MediaUtil.get().playSound(context, R.raw.message_sent, null /* completionListener */);
    }

    /**
     * {@inheritDoc} from DraftMessageDataListener
     */
    @Override // From DraftMessageDataListener
    public void onDraftChanged(final DraftMessageData data, final int changeFlags) {
        // As this is called asynchronously when message read check bound before updating text
        mBinding.ensureBound(data);

        // We have to cache the values of the DraftMessageData because when we set
        // mComposeEditText, its onTextChanged calls updateVisualsOnDraftChanged,
        // which immediately reloads the text from the subject and message fields and replaces
        // what's in the DraftMessageData.

        final String subject = data.getMessageSubject();
        final String message = data.getMessageText();
        if ((changeFlags & DraftMessageData.MESSAGE_SUBJECT_CHANGED) ==
                DraftMessageData.MESSAGE_SUBJECT_CHANGED) {
            mComposeSubjectText.setText(subject);

            // Set the cursor selection to the end since setText resets it to the start
            mComposeSubjectText.setSelection(mComposeSubjectText.getText().length());
        }

        if ((changeFlags & DraftMessageData.MESSAGE_TEXT_CHANGED) ==
                DraftMessageData.MESSAGE_TEXT_CHANGED) {
            String signature = Preferences.getDefault().getString(SignatureSettingDialog.PREF_KEY_SIGNATURE_CONTENT, null);
            if (!TextUtils.isEmpty(signature)) {
                SpannableString sb = new SpannableString(message + "\n" + signature);
                sb.setSpan(mSignatureSpan, message.length() + 1, sb.length(), 0);
                sb.setSpan(new AbsoluteSizeSpan(13, true), message.length() + 1, sb.length(), 0);
                mComposeEditText.setText(sb, TextView.BufferType.SPANNABLE);
                mComposeEditText.setSelection(message.length());
            } else {
                mComposeEditText.setText(message);
                // Set the cursor selection to the end since setText resets it to the start
                mComposeEditText.setSelection(mComposeEditText.getText().length());
            }
        }

        if ((changeFlags & DraftMessageData.ATTACHMENTS_CHANGED) ==
                DraftMessageData.ATTACHMENTS_CHANGED) {
            final boolean haveAttachments = mAttachmentPreview.onAttachmentsChanged(data);
            mHost.onAttachmentsChanged(haveAttachments);
        }

        if ((changeFlags & DraftMessageData.SELF_CHANGED) == DraftMessageData.SELF_CHANGED) {
            updateOnSelfSubscriptionChange();
        }
        updateVisualsOnDraftChanged();
    }

    @Override   // From DraftMessageDataListener
    public void onDraftAttachmentLimitReached(final DraftMessageData data) {
        mBinding.ensureBound(data);
        mHost.warnOfExceedingMessageLimit(false /* sending */, false /* tooManyVideos */);
    }

    private void updateOnSelfSubscriptionChange() {
        // Refresh the length filters according to the selected self's MmsConfig.
        mComposeEditText.setFilters(new InputFilter[]{
                new LengthFilter(MmsConfig.get(mBinding.getData().getSelfSubId())
                        .getMaxTextLimit())});
        mComposeSubjectText.setFilters(new InputFilter[]{
                new LengthFilter(MmsConfig.get(mBinding.getData().getSelfSubId())
                        .getMaxSubjectLength())});
    }

    @Override
    public void onMediaItemsSelected(final Collection<MessagePartData> items) {
        mBinding.getData().addAttachments(items);
        announceMediaItemState(true /*isSelected*/);
    }

    @Override
    public boolean isContainMessagePartData(Uri uri) {
        return mBinding.getData().isContainMessagePartData(uri);
    }

    @Override
    public void onMediaItemsUnselected(final MessagePartData item) {
        mBinding.getData().removeAttachment(item);
        announceMediaItemState(false /*isSelected*/);
    }

    @Override
    public void onPendingAttachmentAdded(final PendingAttachmentData pendingItem) {
        mBinding.getData().addPendingAttachment(pendingItem, mBinding);
        resumeComposeMessage(true);
    }

    private void announceMediaItemState(final boolean isSelected) {
        final Resources res = getContext().getResources();
        final String announcement = isSelected ? res.getString(
                R.string.mediapicker_gallery_item_selected_content_description) :
                res.getString(R.string.mediapicker_gallery_item_unselected_content_description);
        AccessibilityUtil.announceForAccessibilityCompat(
                this, null, announcement);
    }

    private void announceAttachmentState() {
        if (AccessibilityUtil.isTouchExplorationEnabled(getContext())) {
            int attachmentCount = mBinding.getData().getReadOnlyAttachments().size()
                    + mBinding.getData().getReadOnlyPendingAttachments().size();
            final String announcement = getContext().getResources().getQuantityString(
                    R.plurals.attachment_changed_accessibility_announcement,
                    attachmentCount, attachmentCount);
            AccessibilityUtil.announceForAccessibilityCompat(
                    this, null, announcement);
        }
    }

    @Override
    public void resumeComposeMessage(boolean showKeyboard) {
        mComposeEditText.requestFocus();
        if (showKeyboard) {
            mInputManager.showHideImeKeyboard(true, true);
        } else {
            hideKeyboard();
        }
        announceAttachmentState();
    }

    public void clearAttachments() {
        removeEmojiEvent();
        mBinding.getData().clearAttachments(mHost.getAttachmentsClearedFlags());
        mHost.onAttachmentsCleared();
    }

    public void requestDraftMessage(boolean clearLocalDraft) {
        mBinding.getData().loadFromStorage(mBinding, null, clearLocalDraft);
    }

    public void setDraftMessage(final MessageData message) {
        mBinding.getData().loadFromStorage(mBinding, message, false);
    }

    public void writeDraftMessage() {

        Editable e = mComposeEditText.getText();
        String signature = Preferences.getDefault().
                getString(SignatureSettingDialog.PREF_KEY_SIGNATURE_CONTENT, null);
        String messageText = e.toString();

        if (!TextUtils.isEmpty(signature)) {
            int index = e.getSpanStart(mSignatureSpan);
            if (index >= 0) {
                String messageStr = messageText.substring(0, index);
                String signatureStr = messageText.substring(index, e.length());
                if (signature.equals(signatureStr)) {
                    if (messageStr.length() == 0) {
                        messageText = signatureStr;
                    } else if (messageStr.charAt(messageStr.length() - 1) == '\n') {
                        messageText = messageStr.substring(0, messageStr.length() - 1);
                    } else {
                        messageText = messageStr;
                    }
                }
            }
        }
        mBinding.getData().setMessageText(messageText);

        final String subject = mComposeSubjectText.getText().toString();
        mBinding.getData().setMessageSubject(subject);

        mBinding.getData().saveToStorage(mBinding);
    }

    private void updateConversationSelfId(final String selfId, final boolean notify) {
        mBinding.getData().setSelfId(selfId, notify);
    }

    private boolean logIconSIMShow = false;

    private Uri getSelfSendButtonIconUri() {
        final Uri overridenSelfUri = mHost.getSelfSendButtonIconUri();
        if (overridenSelfUri != null) {
            return overridenSelfUri;
        }
        final SubscriptionListEntry subscriptionListEntry = getSelfSubscriptionListEntry();

        if (subscriptionListEntry != null) {
            if (!logIconSIMShow) {
                logIconSIMShow = true;
                BugleAnalytics.logEvent("SMS_DetailsPage_IconSIM_Show", true);
            }
            return subscriptionListEntry.selectedIconUri;
        }

        // Fall back to default self-avatar in the base case.
        return null;
    }

    private SubscriptionListEntry getSelfSubscriptionListEntry() {
        if (mConversationDataModel == null || mBinding == null) {
            return null;
        }
        return mConversationDataModel.getData().getSubscriptionEntryForSelfParticipant(
                mBinding.getData().getSelfId(), false /* excludeDefault */);
    }

    private boolean isDataLoadedForMessageSend() {
        // Check data loading prerequisites for sending a message.
        return mConversationDataModel != null && mConversationDataModel.isBound() &&
                mConversationDataModel.getData().getParticipantsLoaded();
    }

    private void updateVisualsOnDraftChanged() {
        final String messageText = mComposeEditText.getText().toString();
        boolean hasMessageText = (TextUtils.getTrimmedLength(messageText) > 0);

        if (!TextUtils.isEmpty(mSignatureStr)) {
            Editable inputEditable = mComposeEditText.getText();
            int index = inputEditable.getSpanStart(mSignatureSpan);
            if (index >= 0) {
                boolean signatureChanged = !mSignatureStr.equals(inputEditable.toString().substring(index, inputEditable.length()));
                if (index <= 1 && !signatureChanged) {
                    hasMessageText = false;
                }
            }
        }
        final DraftMessageData draftMessageData = mBinding.getData();
        draftMessageData.setMessageText(messageText);

        final String subject = mComposeSubjectText.getText().toString();
        draftMessageData.setMessageSubject(subject);
        if (!TextUtils.isEmpty(subject)) {
            mSubjectView.setVisibility(View.VISIBLE);
        }


        final boolean hasSubject = (TextUtils.getTrimmedLength(subject) > 0);
        final boolean hasWorkingDraft = hasMessageText || hasSubject ||
                mBinding.getData().hasAttachments();

        // Update the send message button. Self icon uri might be null if self participant data
        // and/or conversation metadata hasn't been loaded by the host.
        final Uri selfSendButtonUri = getSelfSendButtonIconUri();
        int sendWidgetMode = SEND_WIDGET_MODE_SELF_AVATAR;
        if (selfSendButtonUri != null || getSelfSubscriptionListEntry() == null) {
            if (hasWorkingDraft && isDataLoadedForMessageSend()) {
                if (selfSendButtonUri != null) {
                    UiUtils.revealOrHideViewWithAnimation(mSendButton, VISIBLE, null);
                    HSLog.d(TAG,"UiUtils.revealOrHideViewWithAnimation(mSendButton, VISIBLE, null);");
                } else if(!mIsWaitingToSendMessage) {
                        mSendButton.setVisibility(View.VISIBLE);
                        HSLog.d(TAG, "mSendButton.setVisibility(View.VISIBLE);");
                }
                if (isOverriddenAvatarAGroup()) {
                    // If the host has overriden the avatar to show a group avatar where the
                    // send button sits, we have to hide the group avatar because it can be larger
                    // than the send button and pieces of the avatar will stick out from behind
                    // the send button.
                    UiUtils.revealOrHideViewWithAnimation(mSelfSendIcon, GONE, null);
                    HSLog.d(TAG,"UiUtils.revealOrHideViewWithAnimation(mSelfSendIcon, GONE, null);");
                }
                mMmsIndicator.setVisibility(draftMessageData.getIsMms() ? VISIBLE : INVISIBLE);
                sendWidgetMode = SEND_WIDGET_MODE_SEND_BUTTON;
            } else {
                if (selfSendButtonUri != null) {
                    mSelfSendIcon.setImageResourceUri(selfSendButtonUri);
                } else {
                    mSelfSendIcon.setImageResource(R.drawable.input_send_message_icon);
                    mSelfSendIcon.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                            PrimaryColors.getPrimaryColorDark(),
                            Dimensions.pxFromDp(20), false, true));

                }
                if (isOverriddenAvatarAGroup()) {
                    UiUtils.revealOrHideViewWithAnimation(mSelfSendIcon, VISIBLE, null);
                    HSLog.d(TAG,"UiUtils.revealOrHideViewWithAnimation(mSelfSendIcon, VISIBLE, null);");
                }
                UiUtils.revealOrHideViewWithAnimation(mSendButton, GONE, null);
                HSLog.d(TAG,"UiUtils.revealOrHideViewWithAnimation(mSendButton, GONE, null);");
                mMmsIndicator.setVisibility(INVISIBLE);
                if (mConversationDataModel != null && shouldShowSimSelector(mConversationDataModel.getData())) {
                    sendWidgetMode = SEND_WIDGET_MODE_SIM_SELECTOR;
                }
            }
        } else {
            mSelfSendIcon.setImageResourceUri(null);
        }

        if (mSendWidgetMode != sendWidgetMode || sendWidgetMode == SEND_WIDGET_MODE_SIM_SELECTOR) {
            setSendButtonAccessibility(sendWidgetMode);
            mSendWidgetMode = sendWidgetMode;
        }

        // Update the text hint on the message box depending on the attachment type.
        final List<MessagePartData> attachments = draftMessageData.getReadOnlyAttachments();
        final int attachmentCount = attachments.size();
        if (attachmentCount == 0) {
            final SubscriptionListEntry subscriptionListEntry =
                    mConversationDataModel == null ? null : mConversationDataModel.getData().getSubscriptionEntryForSelfParticipant(
                            mBinding.getData().getSelfId(), false /* excludeDefault */);
            if (subscriptionListEntry == null) {
                mComposeEditText.setHint(R.string.compose_message_view_hint_text);
            } else {
                mComposeEditText.setHint(Html.fromHtml(getResources().getString(
                        R.string.compose_message_view_hint_text_multi_sim,
                        subscriptionListEntry.displayName)));
            }
        } else {
            int type = -1;
            for (final MessagePartData attachment : attachments) {
                int newType;
                if (attachment.isImage()) {
                    newType = ContentType.TYPE_IMAGE;
                } else if (attachment.isAudio()) {
                    newType = ContentType.TYPE_AUDIO;
                } else if (attachment.isVideo()) {
                    newType = ContentType.TYPE_VIDEO;
                } else if (attachment.isVCard()) {
                    newType = ContentType.TYPE_VCARD;
                } else {
                    newType = ContentType.TYPE_OTHER;
                }

                if (type == -1) {
                    type = newType;
                } else if (type != newType || type == ContentType.TYPE_OTHER) {
                    type = ContentType.TYPE_OTHER;
                    break;
                }
            }

            switch (type) {
                case ContentType.TYPE_IMAGE:
                    mComposeEditText.setHint(getResources().getQuantityString(
                            R.plurals.compose_message_view_hint_text_photo, attachmentCount));
                    break;

                case ContentType.TYPE_AUDIO:
                    mComposeEditText.setHint(getResources().getQuantityString(
                            R.plurals.compose_message_view_hint_text_audio, attachmentCount));
                    break;

                case ContentType.TYPE_VIDEO:
                    mComposeEditText.setHint(getResources().getQuantityString(
                            R.plurals.compose_message_view_hint_text_video, attachmentCount));
                    break;

                case ContentType.TYPE_VCARD:
                    mComposeEditText.setHint(getResources().getQuantityString(
                            R.plurals.compose_message_view_hint_text_vcard, attachmentCount));
                    break;

                case ContentType.TYPE_OTHER:
                    mComposeEditText.setHint(getResources().getQuantityString(
                            R.plurals.compose_message_view_hint_text_attachments, attachmentCount));
                    break;

                default:
                    Assert.fail("Unsupported attachment type!");
                    break;
            }
        }
    }

    private void setSendButtonAccessibility(final int sendWidgetMode) {
        switch (sendWidgetMode) {
            case SEND_WIDGET_MODE_SELF_AVATAR:
                // No send button and no SIM selector; the self send button is no longer
                // important for accessibility.
                mSelfSendIcon.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
                mSelfSendIcon.setContentDescription(null);
                mSendButton.setVisibility(View.GONE);
                setSendWidgetAccessibilityTraversalOrder(SEND_WIDGET_MODE_SELF_AVATAR);
                break;

            case SEND_WIDGET_MODE_SIM_SELECTOR:
                mSelfSendIcon.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
                mSelfSendIcon.setContentDescription(getSimContentDescription());
                setSendWidgetAccessibilityTraversalOrder(SEND_WIDGET_MODE_SIM_SELECTOR);
                break;

            case SEND_WIDGET_MODE_SEND_BUTTON:
                mMmsIndicator.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
                mMmsIndicator.setContentDescription(null);
                setSendWidgetAccessibilityTraversalOrder(SEND_WIDGET_MODE_SEND_BUTTON);
                break;
        }
    }

    private String getSimContentDescription() {
        final SubscriptionListEntry sub = getSelfSubscriptionListEntry();
        if (sub != null) {
            return getResources().getString(
                    R.string.sim_selector_button_content_description_with_selection,
                    sub.displayName);
        } else {
            return getResources().getString(
                    R.string.sim_selector_button_content_description);
        }
    }

    // Set accessibility traversal order of the components in the send widget.
    private void setSendWidgetAccessibilityTraversalOrder(final int mode) {
        if (OsUtil.isAtLeastL_MR1()) {
            mAttachMediaButton.setAccessibilityTraversalBefore(R.id.compose_message_text);
            switch (mode) {
                case SEND_WIDGET_MODE_SIM_SELECTOR:
                    break;
                case SEND_WIDGET_MODE_SEND_BUTTON:
                    mComposeEditText.setAccessibilityTraversalBefore(R.id.send_message_button);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void afterTextChanged(final Editable editable) {
    }

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count,
                                  final int after) {
        if (mHost.shouldHideAttachmentsWhenSimSelectorShown()) {
            hideSimSelector();
        }
    }

    private void hideSimSelector() {
        if (mInputManager.showHideSimSelector(false /* show */, true /* animate */)) {
            // Now that the sim selector has been hidden, reshow the attachments if they
            // have been hidden.
            hideAttachmentsWhenShowingSims(false /*simPickerVisible*/);
        }
    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before,
                              final int count) {
        final BugleActionBarActivity activity = (mOriginalContext instanceof BugleActionBarActivity)
                ? (BugleActionBarActivity) mOriginalContext : null;
        if (activity != null && activity.getIsDestroyed()) {
            LogUtil.v(LogUtil.BUGLE_TAG, "got onTextChanged after onDestroy");

            // if we get onTextChanged after the activity is destroyed then, ah, wtf
            // b/18176615
            // This appears to have occurred as the result of orientation change.
            return;
        }

        mBinding.ensureBound();
        updateVisualsOnDraftChanged();
    }

    @Override
    public PlainTextEditText getComposeEditText() {
        return mComposeEditText;
    }

    public void displayPhoto(final Uri photoUri, final Rect imageBounds) {
        mHost.displayPhoto(photoUri, imageBounds, true /* isDraft */);
    }

    public void updateConversationSelfIdOnExternalChange(final String selfId) {
        updateConversationSelfId(selfId, true /* notify */);
    }

    /**
     * The selfId of the conversation. As soon as the DraftMessageData successfully loads (i.e.
     * getSelfId() is non-null), the selfId in DraftMessageData is treated as the sole source
     * of truth for conversation self id since it reflects any pending self id change the user
     * makes in the UI.
     */
    public String getConversationSelfId() {
        return mBinding.getData().getSelfId();
    }

    public void selectSim(SubscriptionListEntry subscriptionData) {
        final String oldSelfId = getConversationSelfId();
        final String newSelfId = subscriptionData.selfParticipantId;
        Assert.notNull(newSelfId);
        // Don't attempt to change self if self hasn't been loaded, or if self hasn't changed.
        if (oldSelfId == null || TextUtils.equals(oldSelfId, newSelfId)) {
            return;
        }
        updateConversationSelfId(newSelfId, true /* notify */);
    }

    public void hideAllComposeInputs(final boolean animate) {
        mInputManager.hideAllInputs(animate);
    }

    public void saveInputState(final Bundle outState) {
        mInputManager.onSaveInputState(outState);
    }

    public boolean onBackPressed() {
        return mInputManager.onBackPressed();
    }

    public boolean onNavigationUpPressed() {
        return mInputManager.onNavigationUpPressed();
    }

    public boolean updateActionBar(final ActionBar actionBar) {
        return mInputManager != null ? mInputManager.updateActionBar(actionBar) : false;
    }

    public static boolean shouldShowSimSelector(final ConversationData convData) {
        return OsUtil.isAtLeastL_MR1() &&
                convData.getSelfParticipantsCountExcludingDefault(true /* activeOnly */) > 1;
    }

    public void sendMessageIgnoreMessageSizeLimit() {
        sendMessageInternal(false /* checkMessageSize */);
    }

    public void onAttachmentPreviewLongClicked() {
        mHost.showAttachmentChooser();
    }

    @Override
    public void onDraftAttachmentLoadFailed() {
        mHost.notifyOfAttachmentLoadFailed();
    }

    private boolean isOverriddenAvatarAGroup() {
        final Uri overridenSelfUri = mHost.getSelfSendButtonIconUri();
        if (overridenSelfUri == null) {
            return false;
        }
        return AvatarUriUtil.TYPE_GROUP_URI.equals(AvatarUriUtil.getAvatarType(overridenSelfUri));
    }

    public boolean isCameraOrGalleryShowing() {
        return mHost.isCameraOrGalleryShowing();
    }

    @Override
    public void setAccessibility(boolean enabled) {
        if (enabled) {
            mAttachMediaButton.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
            mComposeEditText.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
            mSendButton.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
            setSendButtonAccessibility(mSendWidgetMode);
        } else {
            mSelfSendIcon.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            mComposeEditText.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            mSendButton.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            mAttachMediaButton.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
    }
}
