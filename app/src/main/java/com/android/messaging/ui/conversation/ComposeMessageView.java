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
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.content.res.AppCompatResources;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.android.messaging.datamodel.data.SubscriptionListData;
import com.android.messaging.datamodel.data.SubscriptionListData.SubscriptionListEntry;
import com.android.messaging.font.FontUtils;
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
import com.android.messaging.ui.senddelaymessages.SendDelayMessagesManager;
import com.android.messaging.ui.signature.SignatureSettingDialog;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BugleActivityUtil;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleFirebaseAnalytics;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.ContentType;
import com.android.messaging.util.DefaultSMSUtils;
import com.android.messaging.util.ImeUtil;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.MediaUtil;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.TextViewUtil;
import com.android.messaging.util.UiUtils;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Compats;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This view contains the UI required to generate and send messages.
 */
public class ComposeMessageView extends LinearLayout
        implements TextView.OnEditorActionListener, DraftMessageDataListener, TextWatcher,
        ConversationInputSink {

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

        boolean shouldHideAttachmentsWhenSimSelectorShown();

        int getAttachmentsClearedFlags();

        boolean isCameraOrGalleryShowing();

        void onClickMediaOrEmoji();

        Activity getHostActivity();
    }

    private static final String TAG = ComposeMessageView.class.getSimpleName();

    private static final int DISTANCE_SLOP = Dimensions.pxFromDp(90);

    // There is no draft and there is no need for the SIM selector
    private static final int SEND_WIDGET_MODE_SELF_AVATAR = 1;
    // There is no draft but we need to show the SIM selector
    private static final int SEND_WIDGET_MODE_SIM_SELECTOR = 2;
    // There is a draft
    private static final int SEND_WIDGET_MODE_SEND_BUTTON = 3;

    private static final int DEFAULT_EMOJI_PICKER_HEIGHT = Dimensions.pxFromDp(243);

    private PlainTextEditText mComposeEditText;
    private ImageView mSendButton;
    private ImageView mSimButton;
    private ImageView mDelayCloseButton;
    private AttachmentPreview mAttachmentPreview;
    private SendDelayProgressBar mSendDelayProgressBar;
    private ImageView mAttachMediaButton;
    private ImageView mEmojiKeyboardBtn;
    private ImageView mEmojiGuideView;
    private LinearLayout mInputLayout;
    private FrameLayout mMediaPickerLayout;
    private FrameLayout mEmojiPickerLayout;

    private List<String> mEmojiLogCodeList;
    private List<String> mMagicStickerLogNameList;
    private List<String> mStickerLogNameList;

    private boolean mIsMediaPendingShow = false;
    private boolean mIsEmojiPendingShow = false;
    private boolean mHasGif;
    private boolean isFirstEmojiStart = true;

    private final Binding<DraftMessageData> mBinding;
    private IComposeMessageViewHost mHost;
    private final Context mOriginalContext;
    private int mSendWidgetMode = SEND_WIDGET_MODE_SELF_AVATAR;
    private String mSignatureStr;
    ForegroundColorSpan mSignatureSpan = new ForegroundColorSpan(0xb3222327);

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
        mHost = host;
        mBinding.bind(data);
        data.addListener(this);
        data.setSubscriptionDataProvider(host);
        resumeIncompleteWorkInThisConversation();
    }

    /**
     * Host calls this to unbind view
     */
    public void unbind() {
        mBinding.unbind();
        mHost = null;
        mInputManager.onDetach();
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
                BugleAnalytics.logEvent("SMS_DetailsPage_DialogBox_Click", true);
                BugleFirebaseAnalytics.logEvent("SMS_DetailsPage_DialogBox_Click");
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

        mDelayCloseButton = findViewById(R.id.delay_close_button);

        mSendButton = findViewById(R.id.send_message_button);
        mSendButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                PrimaryColors.getPrimaryColorDark(),
                Dimensions.pxFromDp(29), false, true));
        mSendButton.setOnClickListener(clickView -> {
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

            final boolean hasWorkingDraft = hasMessageText ||
                    (mBinding.getData() != null && mBinding.getData().hasAttachments());
            if (!hasWorkingDraft || !isDataLoadedForMessageSend()) {
                return;
            }

            startDelayedSendingMessageIndicatorAnimation(System.currentTimeMillis());
            sendMessageInternal(true);

            BugleAnalytics.logEvent("Detailpage_BtnSend_Click", true,
                    "SendDelay", "" + SendDelaySettings.getSendDelayInSecs(),
                    "IsDefaultSMS", String.valueOf(DefaultSMSUtils.isDefaultSmsApp()),
                    "SendSMSPermission", String.valueOf(RuntimePermissions.checkSelfPermission(getContext(),
                            Manifest.permission.SEND_SMS) == RuntimePermissions.PERMISSION_GRANTED),
                    "ReadSMSPermission", String.valueOf(RuntimePermissions.checkSelfPermission(getContext(),
                            Manifest.permission.READ_SMS) == RuntimePermissions.PERMISSION_GRANTED));
            BugleFirebaseAnalytics.logEvent("Detailpage_BtnSend_Click",
                    "SendDelay", "" + SendDelaySettings.getSendDelayInSecs(),
                    "IsDefaultSMS", String.valueOf(DefaultSMSUtils.isDefaultSmsApp()),
                    "SendSMSPermission", String.valueOf(RuntimePermissions.checkSelfPermission(getContext(),
                            Manifest.permission.SEND_SMS) == RuntimePermissions.PERMISSION_GRANTED),
                    "ReadSMSPermission", String.valueOf(RuntimePermissions.checkSelfPermission(getContext(),
                            Manifest.permission.READ_SMS) == RuntimePermissions.PERMISSION_GRANTED));
        });

        mSimButton = findViewById(R.id.sim_btn);
        mSimButton.setBackground(
                BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(15), true));
        mSimButton.setOnClickListener(v -> {
            if (mConversationDataModel.getData() == null
                    || mConversationDataModel.getData().getSubscriptionListData() == null
                    || mConversationDataModel.getData().getSubscriptionListData().getActiveSubscriptionEntriesExcludingDefault() == null) {
                return;
            }

            View mContentView = LayoutInflater.from(getContext()).inflate(
                    R.layout.dialog_select_sim, null);
            AlertDialog.Builder simBuilder = new AlertDialog.Builder(getContext(), R.style.TransparentDialog);
            simBuilder.setView(mContentView);
            AlertDialog dialog = simBuilder.create();
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
            dialog.setOnDismissListener(dialog1 -> updateOnSelfSubscriptionChange());

            List<SubscriptionListData.SubscriptionListEntry> data =
                    mConversationDataModel.getData().getSubscriptionListData().getActiveSubscriptionEntriesExcludingDefault();
            if (getSelfSubscriptionListEntry() == null) {
                return;
            }
            int currentSlotId = getSelfSubscriptionListEntry().slotId;
            ImageView ivTip1 = mContentView.findViewById(R.id.iv_tip_1);
            ivTip1.getDrawable().setColorFilter(PrimaryColors.getPrimaryColor(), PorterDuff.Mode.SRC_ATOP);
            ImageView ivTip2 = mContentView.findViewById(R.id.iv_tip_2);
            ivTip2.getDrawable().setColorFilter(PrimaryColors.getPrimaryColor(), PorterDuff.Mode.SRC_ATOP);

            for (int i = 0; i < 2 && i < data.size(); i++) {
                final SubscriptionListData.SubscriptionListEntry entry = data.get(i);
                if (entry.slotId == 1) {
                    ((TextView) mContentView.findViewById(R.id.carrier_1)).setText(entry.displayName);

                    final String displayDestination = TextUtils.isEmpty(entry.displayDestination) ?
                            getContext().getResources().getString(R.string.sim_settings_unknown_number) :
                            entry.displayDestination;
                    ((TextView) mContentView.findViewById(R.id.phone_number_1)).setText(displayDestination);
                    mContentView.findViewById(R.id.container_sim_1).setOnClickListener(v1 -> {
                        selectSim(entry);
                        dialog.dismiss();
                    });
                    mContentView.findViewById(R.id.container_sim_1).setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, 0, true));
                } else {
                    ((TextView) mContentView.findViewById(R.id.carrier_2)).setText(entry.displayName);
                    final String displayDestination = TextUtils.isEmpty(entry.displayDestination) ?
                            getContext().getResources().getString(R.string.sim_settings_unknown_number) :
                            entry.displayDestination;
                    ((TextView) mContentView.findViewById(R.id.phone_number_2)).setText(displayDestination);
                    mContentView.findViewById(R.id.container_sim_2).setOnClickListener(v2 -> {
                        selectSim(entry);
                        dialog.dismiss();
                    });
                    mContentView.findViewById(R.id.container_sim_2).setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, 0, true));
                }
            }

            Drawable unwrappedDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.iv_sim_selected);
            Drawable selectedDrawable = DrawableCompat.wrap(unwrappedDrawable);
            DrawableCompat.setTint(selectedDrawable, PrimaryColors.getPrimaryColor());
            if (currentSlotId == 1) {
                ImageView ivCheckSim1 = mContentView.findViewById(R.id.iv_check_sim_1);
                ivCheckSim1.setImageDrawable(selectedDrawable);
                ImageView ivCheckSim2 = mContentView.findViewById(R.id.iv_check_sim_2);
                ivCheckSim2.setImageResource(R.drawable.iv_sim_unselected);
            } else {
                ImageView ivCheckSim1 = mContentView.findViewById(R.id.iv_check_sim_1);
                ivCheckSim1.setImageResource(R.drawable.iv_sim_unselected);
                ImageView ivCheckSim2 = mContentView.findViewById(R.id.iv_check_sim_2);
                ivCheckSim2.setImageDrawable(selectedDrawable);
            }

            dialog.show();
        });

        mSendDelayProgressBar = findViewById(R.id.send_delay_circle_bar);
        mSendDelayProgressBar.setOnClickListener(clickedView -> {
            resetDelaySendAnimation();
            SendDelayMessagesManager.remove(mBinding.getData().getConversationId());
            BugleAnalytics.logEvent("Detailpage_BtnCancel_Click");
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
            BugleAnalytics.logEvent("SMS_DetailsPage_IconPlus_Click", true);
            BugleFirebaseAnalytics.logEvent("SMS_DetailsPage_IconPlus_Click");
        });

        mEmojiGuideView = findViewById(R.id.emoji_guide_view);
        if (EmojiManager.isShowEmojiGuide()) {
            mEmojiGuideView.setVisibility(VISIBLE);
        }

        mEmojiPickerLayout = findViewById(R.id.emoji_picker_container);
        mEmojiKeyboardBtn = findViewById(R.id.emoji_btn);
        mEmojiKeyboardBtn.setOnClickListener(v -> {
            mHost.onClickMediaOrEmoji();
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


    private void resumeIncompleteWorkInThisConversation() {
        String conversationId = mBinding.getData().getConversationId();
        SendDelayMessagesManager.SendMessageWork work = SendDelayMessagesManager.getIncompleteSendingDelayMessagesAction(conversationId);

        HSLog.d(TAG, "get last conversastion work is empty : " + (work == null));
        if (work != null) {
            long firstSendDelayActionStartSystemTime = work.getLastSendDelayActionStartSystemTime();
            startDelayedSendingMessageIndicatorAnimation(firstSendDelayActionStartSystemTime);
        }
    }

    private void startDelayedSendingMessageIndicatorAnimation(long sendDelayAnimationStartTime) {
        if (SendDelaySettings.getSendDelayInSecs() != 0) {
            long timePast = System.currentTimeMillis() - sendDelayAnimationStartTime;

            if (timePast > 1000 * SendDelaySettings.getSendDelayInSecs()) {
                timePast = 1000 * SendDelaySettings.getSendDelayInSecs() * 9 / 10;
            }
            mSendDelayProgressBar.setProgress(100 - (float) ((timePast * 100) / (1000 * SendDelaySettings.getSendDelayInSecs())));
            mDelayCloseButton.setVisibility(View.VISIBLE);
            mSendDelayProgressBar.setVisibility(View.VISIBLE);
            mSendButton.setVisibility(View.GONE);

            mDelayCloseButton.animate().alpha(1.0f).setDuration(160).setStartDelay(80).start();
            Interpolator scaleStartInterpolator =
                    PathInterpolatorCompat.create(0.0f, 0.0f, 0.58f, 1.0f);
            mSendDelayProgressBar.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration(160).setStartDelay(80).setInterpolator(scaleStartInterpolator).start();
            mSendDelayProgressBar.startAnimation(SendDelaySettings.getSendDelayInMills() - timePast);
        }
    }

    private void resetDelaySendAnimation() {
        mDelayCloseButton.setVisibility(View.GONE);
        mDelayCloseButton.setAlpha(0.0f);

        mSendDelayProgressBar.setVisibility(View.GONE);
        mSendDelayProgressBar.setProgress(100);
        mSendDelayProgressBar.setAlpha(0.0f);
        mSendDelayProgressBar.setScaleX(0.8f);
        mSendDelayProgressBar.setScaleY(0.8f);
        mSendDelayProgressBar.resetAnimation();

        mSendButton.setVisibility(VISIBLE);
        mSendButton.setScaleX(1.0f);
        mSendButton.setScaleY(1.0f);
        mSendButton.setAlpha(1.0f);
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
    }

    private void hideKeyboard() {
        ImeUtil.get().hideImeKeyboard(getContext(), mComposeEditText);
    }

    private void showEmojiPicker() {
        BugleAnalytics.logEvent("SMSEmoji_Chat_Emoji_Click", true);
        BugleFirebaseAnalytics.logEvent("SMSEmoji_Chat_Emoji_Click");
        if (EmojiManager.isShowEmojiGuide()) {
            EmojiManager.recordAlreadyShowEmojiGuide();
            mEmojiGuideView.setVisibility(GONE);
        }

        mInputManager.showEmojiPicker();
        mEmojiPickerLayout.setVisibility(VISIBLE);
        mEmojiKeyboardBtn.setImageResource(R.drawable.input_keyboard_icon);

        int keyboardHeight = UiUtils.getKeyboardHeight();
        int height = keyboardHeight == 0 ? DEFAULT_EMOJI_PICKER_HEIGHT : keyboardHeight;
        mEmojiPickerLayout.getLayoutParams().height = height;
        mEmojiPickerLayout.requestLayout();

        if (!mIsEmojiPendingShow) {
            startEmojiPickerAnimation(mEmojiPickerLayout, height);
        } else {
            isFirstEmojiStart = false;
            mInputManager.onEmojiAnimationFinished();
        }
    }

    private void startEmojiPickerAnimation(ViewGroup container, int startPos) {
        ObjectAnimator va = ObjectAnimator.ofFloat(container, "translationY",
                startPos, 0f);
        va.setDuration(520);
        va.setInterpolator(PathInterpolatorCompat.create(0.26f, 1, 0.48f, 1));

        va.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isFirstEmojiStart = false;
                mInputManager.onEmojiAnimationFinished();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                HSLog.e("emoji_picker", "onAnimationCancel: onAnimationCancelFinished not do");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        va.start();
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
                    BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_LittleEmoji_Send", true, "type", code);
                    BugleFirebaseAnalytics.logEvent("SMSEmoji_ChatEmoji_LittleEmoji_Send", "type", code);
                    hasLittleEmoji = true;
                }
            }
            mEmojiLogCodeList.clear();
        }
        boolean hasSticker = mStickerLogNameList != null && !mStickerLogNameList.isEmpty();
        boolean hasMagicSticker = mMagicStickerLogNameList != null && !mMagicStickerLogNameList.isEmpty();
        if (hasLittleEmoji && !hasSticker && !hasMagicSticker && !mHasGif) {
            BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Emoji_Send", true, "type", "emoji");
            BugleFirebaseAnalytics.logEvent("SMSEmoji_ChatEmoji_Emoji_Send", "type", "emoji");
        } else if (!hasLittleEmoji && hasSticker && !hasMagicSticker && !mHasGif) {
            BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Emoji_Send", true, "type", "sticker");
            BugleFirebaseAnalytics.logEvent("SMSEmoji_ChatEmoji_Emoji_Send", "type", "sticker");
        } else if (!hasLittleEmoji && !hasSticker && hasMagicSticker && !mHasGif) {
            BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Emoji_Send", true, "type", "magic");
            BugleFirebaseAnalytics.logEvent("SMSEmoji_ChatEmoji_Emoji_Send", "type", "magic");
        } else if (!hasLittleEmoji && !hasSticker && !hasMagicSticker && mHasGif) {
            BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Emoji_Send", true, "type", "gif");
            BugleFirebaseAnalytics.logEvent("SMSEmoji_ChatEmoji_Emoji_Send", "type", "gif");
        } else if (hasLittleEmoji || hasSticker || hasMagicSticker || mHasGif) {
            BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Emoji_Send", true, "type", "other");
            BugleFirebaseAnalytics.logEvent("SMSEmoji_ChatEmoji_Emoji_Send", "type", "other");
        }

        if (mHasGif) {
            BugleAnalytics.logEvent("SMSEmoji_GIF_Send");
        }
        mHasGif = false;
        logEvent("SMSEmoji_ChatEmoji_Tab_Send", mStickerLogNameList);
        logEvent("SMSEmoji_ChatEmoji_Magic_Send", mMagicStickerLogNameList);
    }

    private void logEvent(String eventName, List<String> data) {
        if (data != null && !data.isEmpty()) {
            for (String type : data) {
                if (eventName.contains("Send")) {
                    BugleAnalytics.logEvent(eventName, true, "type", type);
                    BugleFirebaseAnalytics.logEvent(eventName, "type", type);
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

    @Override
    public void logGif() {
        mHasGif = true;
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

    public void onSendMessageActionTriggered() {
        HSLog.d("ComposeMessageView", "onSendMessageActionTriggered");
        logEmojiEvent();
        String conversationId = mBinding.getData().getConversationId();

        mBinding.getData().clearLocalDraftAndNotifyChanged();
        updateVisualsOnDraftChanged();
        resetDelaySendAnimation();
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
            boolean includeSignature = false;
            Editable inputEditable = mComposeEditText.getText();

            checkEmojiEvent(inputEditable.toString());

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
                BugleAnalytics.logEvent("SMS_DetailsPage_IconSend_Click", true, "Type", "MMS",
                        "isContact", isContactValue);
                BugleFirebaseAnalytics.logEvent("SMS_DetailsPage_IconSend_Click", "Type", "MMS",
                        "isContact", isContactValue);
            } else {
                final String messageToSend = mComposeEditText.getText().toString();
                mBinding.getData().setMessageText(messageToSend);
                BugleAnalytics.logEvent("SMS_DetailsPage_IconSend_Click", true, "Type", "SMS",
                        "isContact", isContactValue);
                BugleFirebaseAnalytics.logEvent("SMS_DetailsPage_IconSend_Click", "Type", "SMS",
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
                                            // show Invite Friends dialog
                                            if (finalSendEmoji) {
                                                FiveStarRateDialog.showFiveStarWhenSendEmojiIfNeed(BugleActivityUtil.contextToActivitySafely(getContext()));
                                            } else {
                                                FiveStarRateDialog.showFiveStarWhenSendMsgIfNeed(BugleActivityUtil.contextToActivitySafely(getContext()));
                                            }
                                        }, 1600);

                                        playSentSound();
                                        mHost.sendMessage(message);
                                        if (!TextUtils.isEmpty(mSignatureStr)) {
                                            BugleAnalytics.logEvent("SMS_WithSignature_Send", true,
                                                    "deleteSignature", String.valueOf(!finalIncludeSignature));
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

    private void checkEmojiEvent(String input) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String EMOJI_REGEX = ".*(([\\ud83c\\udc00-\\ud83c\\udfff]|[\\ud83d\\udc00-\\ud83d\\udfff]|[\\u2600-\\u27ff]|[\\ue000-\\uf8ff])[\\uD83C\\uDFFB-\\uD83C\\uDFFF]" +
                        "|([\\ud83c\\udc00-\\ud83c\\udfff]|[\\ud83d\\udc00-\\ud83d\\udfff]|[\\u2600-\\u27ff]|[\\ue000-\\uf8ff])).*";
                Pattern emojiPattern = Pattern.compile(EMOJI_REGEX,
                        Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
                Matcher matcher = emojiPattern.matcher(input);
                if (matcher.matches()) {
                    BugleAnalytics.logEvent("Message_Emoji_Send");
                }
            }
        }).start();
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

        final String message = data.getMessageText();

        if ((changeFlags & DraftMessageData.MESSAGE_TEXT_CHANGED) ==
                DraftMessageData.MESSAGE_TEXT_CHANGED) {
            String signature = Preferences.getDefault().getString(SignatureSettingDialog.PREF_KEY_SIGNATURE_CONTENT, null);
            if (!TextUtils.isEmpty(signature)) {
                SpannableString sb = new SpannableString(message + "\n" + signature);
                sb.setSpan(mSignatureSpan, message.length() + 1, sb.length(), 0);
                sb.setSpan(new AbsoluteSizeSpan(13, true), message.length() + 1, sb.length(), 0);
                mComposeEditText.setText(sb, TextView.BufferType.SPANNABLE);
            } else {
                mComposeEditText.setText(message);
            }
            // Set the cursor selection to the end since setText resets it to the start
            mComposeEditText.setSelection(mComposeEditText.getText().length());
        }

        if ((changeFlags & DraftMessageData.ATTACHMENTS_CHANGED) ==
                DraftMessageData.ATTACHMENTS_CHANGED) {

            if (mAttachmentPreview == null) {
                // first load
                final List<MessagePartData> attachments = data.getReadOnlyAttachments();
                final List<PendingAttachmentData> pendingAttachments =
                        data.getReadOnlyPendingAttachments();
                if (!attachments.isEmpty() || !pendingAttachments.isEmpty()) {
                    ViewStub stub = findViewById(R.id.attachment_container_stub);
                    mAttachmentPreview = stub.inflate().findViewById(R.id.attachment_draft_view);
                    mAttachmentPreview.setComposeMessageView(this);
                }
            }
        }
        if (mAttachmentPreview != null) {
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
        mSimButton.setVisibility(shouldShowSimSelector(mConversationDataModel.getData()) ? View.VISIBLE : View.GONE);
        final SubscriptionListEntry subscriptionListEntry = getSelfSubscriptionListEntry();
        if (subscriptionListEntry != null) {
            mSimButton.setImageResource(
                    subscriptionListEntry.slotId == 1 ?
                            R.drawable.ic_dual_sim_small_1 : R.drawable.ic_dual_sim_small_2);
            mSimButton.getDrawable().setColorFilter(PrimaryColors.getPrimaryColor(), PorterDuff.Mode.SRC_ATOP);
        }
        findViewById(R.id.sim_message_space)
                .setVisibility(shouldShowSimSelector(mConversationDataModel.getData()) ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onMediaItemsSelected(final Collection<MessagePartData> items) {
        mBinding.getData().addAttachments(items);
    }

    @Override
    public boolean isContainMessagePartData(Uri uri) {
        return mBinding.getData().isContainMessagePartData(uri);
    }

    @Override
    public void onMediaItemsUnselected(final MessagePartData item) {
        mBinding.getData().removeAttachment(item);
    }

    @Override
    public void onPendingAttachmentAdded(final PendingAttachmentData pendingItem) {
        mBinding.getData().addPendingAttachment(pendingItem, mBinding);
        resumeComposeMessage(true);
    }

    @Override
    public void resumeComposeMessage(boolean showKeyboard) {
        if (!(Compats.IS_SAMSUNG_DEVICE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)) {
            mComposeEditText.requestFocus();
        }
        if (showKeyboard) {
            mInputManager.showHideImeKeyboard(true, true);
        } else {
            hideKeyboard();
        }
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

        mBinding.getData().saveToStorage(mBinding);
    }

    private void updateConversationSelfId(final String selfId, final boolean notify) {
        mBinding.getData().setSelfId(selfId, notify);
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
        final DraftMessageData draftMessageData = mBinding.getData();
        draftMessageData.setMessageText(messageText);

        mSendButton.setVisibility(View.VISIBLE);

        // Update the text hint on the message box depending on the attachment type.
        final List<MessagePartData> attachments = draftMessageData.getReadOnlyAttachments();
        final int attachmentCount = attachments.size();
        if (attachmentCount == 0) {
            mComposeEditText.setHint(R.string.compose_message_view_hint_text);
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

    @Override
    public void afterTextChanged(final Editable editable) {
    }

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count,
                                  final int after) {
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

    public boolean isCameraOrGalleryShowing() {
        return mHost.isCameraOrGalleryShowing();
    }
}
