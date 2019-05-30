package com.android.messaging.ui.messagebox;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.datamodel.NoConfirmationSmsSendService;
import com.android.messaging.datamodel.data.MessageBoxItemData;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.appsettings.PrivacyModeSettings;
import com.android.messaging.ui.appsettings.SendDelaySettings;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.ToolbarDrawables;
import com.android.messaging.ui.customize.WallpaperDrawables;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.ui.emoji.EmojiInfo;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.Dates;
import com.android.messaging.util.ImeUtil;
import com.android.messaging.util.UiUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import static com.android.messaging.datamodel.NoConfirmationSmsSendService.EXTRA_SELF_ID;
import static com.android.messaging.ui.appsettings.PrivacyModeSettings.NONE;

public class MessageBoxConversationView extends FrameLayout {

    @ColorInt
    private int mPrimaryColor;

    private MessageBoxActivity mActivity;
    private ViewGroup mContent;
    private ViewGroup mMessageView;
    private MessageBoxInputActionView mInputActionView;
    private MessageBoxMessageListAdapter mAdapter;
    private TextView mConversationName;
    private RecyclerView mRecyclerView;
    private EditText mInputEditText;

    // privacy mode
    private TextView mPrivacyConversationName;
    private View mPrivacyContainer;
    private TextView mPrivacyTitle;
    private TextView mPrivacyTimestamp;

    private String mConversationId;
    private String mSelfId;
    private String mParticipantId;

    private boolean mHasSentMessages;
    private boolean mMarkAsRead;
    private int mInputEmojiCount;
    private Handler mSendDelayHandler;
    private Runnable mSendDelayRunnable;

    public MessageBoxConversationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (MessageBoxActivity) context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPrimaryColor = PrimaryColors.getPrimaryColor();
        initActionBarSimulation();
        mContent = findViewById(R.id.content);
        mInputActionView = findViewById(R.id.message_compose_view_container);
        mMessageView = findViewById(R.id.message_view);
        mPrivacyConversationName = findViewById(R.id.privacy_conversation_name);

        mInputEditText = mInputActionView.getComposeEditText();
        initInputAction();
        mSendDelayHandler = new Handler();
        mSendDelayRunnable = () -> {
            mInputActionView.resetDelaySendAnimation();
            sendMessage();
        };
    }

    void bind(MessageBoxItemData data) {
        mConversationName = findViewById(R.id.conversation_name);
        mConversationName.setText(data.getConversationName());

        mConversationId = data.getConversationId();
        mSelfId = data.getSelfId();

        mRecyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.addItemDecoration(new MessageItemDecoration());

        mAdapter = new MessageBoxMessageListAdapter(data);
        mRecyclerView.setAdapter(mAdapter);
        setTag(mConversationId);

        mParticipantId = data.getParticipantId();
        inflatePrivacyModePageIfNeeded();

        if (PrivacyModeSettings.getPrivacyMode(mConversationId) == NONE) {
            markAsRead();
        }

        ImageView background = findViewById(R.id.message_background);
        Drawable wallpaperDrawable = WallpaperDrawables.getListWallpaperBg();
        if (wallpaperDrawable != null) {
            background.setImageDrawable(wallpaperDrawable);
        } else {
            background.setImageDrawable(null);
        }
    }

    void requestEditTextFocus() {
        mInputEditText.requestFocus();
    }

    void updateTimestamp() {
        mAdapter.notifyDataSetChanged();
    }

    void addNewMessage(MessageBoxItemData data) {
        mAdapter.addNewIncomingMessage(data);
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
        updatePrivacyTitleAndTimestamp();
    }

    int getContentHeight() {
        return mContent.getHeight();
    }

    @SuppressLint("RestrictedApi")
    private void initActionBarSimulation() {
        ImageView closeActionImage = findViewById(R.id.action_close);
        LinearLayout openActionContainer = findViewById(R.id.action_open);

        openActionContainer.setOnClickListener(mActivity);
        closeActionImage.setOnClickListener(mActivity);

        ImageView background = findViewById(R.id.action_bar_simulation_background);
        Drawable toolbarBg = ToolbarDrawables.getToolbarBg();
        if (toolbarBg != null) {
            background.setImageDrawable(toolbarBg.mutate());
        } else {
            background.setImageDrawable(new ColorDrawable(mPrimaryColor));
        }
    }

    String getConversationId() {
        return mConversationId;
    }

    String getParticipantId() {
        return mParticipantId;
    }

    boolean hasSentMessage() {
        return mHasSentMessages;
    }

    void replyMessage() {
        if (TextUtils.isEmpty(mInputActionView.getMessage())) {
            return;
        }
        mHasSentMessages = true;

        if (SendDelaySettings.getSendDelayInSecs() != 0) {
            mInputActionView.sendDelayAnimation();
            mSendDelayHandler.postDelayed(mSendDelayRunnable, 1000 * SendDelaySettings.getSendDelayInSecs());
            mInputActionView.setOnCancelSmsSendingClickListener(clickedView -> {
                BugleAnalytics.logEvent("Popups_BtnCancel_Click");
                mSendDelayHandler.removeCallbacks(mSendDelayRunnable);
                mInputActionView.resetDelaySendAnimation();
            });
        } else {
            sendMessage();
            mInputActionView.performReply();
        }
    }

    private void sendMessage() {
        String message = mInputActionView.getMessage();
        if (TextUtils.isEmpty(message)) {
            return;
        }
        Context context = Factory.get().getApplicationContext();
        final Intent sendIntent = new Intent(context, NoConfirmationSmsSendService.class);
        sendIntent.setAction(TelephonyManager.ACTION_RESPOND_VIA_MESSAGE);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.putExtra(EXTRA_SELF_ID, mSelfId);
        sendIntent.putExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID, mConversationId);
        context.startService(sendIntent);

        boolean hasEmoji = false;
        boolean hasSms = false;
        if (mInputEditText.getText() != null) {
            hasEmoji = mInputEmojiCount > 0;
            hasSms = mInputEditText.getText().length() > mInputEmojiCount;
        }
        String type = "";
        if (hasSms) {
            type += "sms";
        }
        if (hasEmoji) {
            type += "emoji";
        }

        BugleAnalytics.logEvent("SMS_PopUp_Reply_BtnClick_Multifunction", false, true,
                "type", type, "type2", MessageBoxAnalytics.getConversationType(),
                "withTheme", String.valueOf(!ThemeUtils.isDefaultTheme()));
    }


    void emojiClick(EmojiInfo emojiInfo) {
        if (mInputEditText != null) {
            int start = mInputEditText.getSelectionStart();
            int end = mInputEditText.getSelectionEnd();
            mInputEditText.getText().replace(start, end, emojiInfo.mEmoji);
            mInputEmojiCount++;
        }
    }

    void deleteEmoji() {
        mInputEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        mInputEmojiCount--;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initInputAction() {
        mInputActionView.getEmojiIcon().setOnClickListener(v -> {
            if (mActivity.getIsEmojiVisible()) {
                mActivity.hideEmoji();
            } else {
                ImeUtil.get().hideImeKeyboard(getContext(), mInputEditText);
                mActivity.showEmoji();
            }
            MessageBoxAnalytics.logEvent("SMS_PopUp_Emoji_Click");
        });

        mInputEditText.setOnClickListener(v -> {
            mActivity.hideEmoji();
            post(() -> ImeUtil.get().showImeKeyboard(getContext(), mInputEditText));
            MessageBoxAnalytics.logEvent("SMS_PopUp_TextField_Click");
            BugleAnalytics.logEvent("SMS_PopUp_TextField_Click_Keyboard", "hasKeyboardHeight",
                    String.valueOf(UiUtils.getKeyboardHeight() > 0));
        });
    }

    private void inflatePrivacyModePageIfNeeded() {
        if (!hideMessagesForThisMessage()) {
            return;
        }

        mMessageView.setVisibility(INVISIBLE);
        mMessageView.setAlpha(0f);
        ViewStub stub = findViewById(R.id.privacy_stub);
        mPrivacyContainer = stub.inflate();
        mPrivacyContainer.setClickable(true);

        ImageView appIcon = mPrivacyContainer.findViewById(R.id.app_icon);
        appIcon.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(25), false));

        mPrivacyTitle = mPrivacyContainer.findViewById(R.id.privacy_title);
        mPrivacyTimestamp = mPrivacyContainer.findViewById(R.id.privacy_date);
        TextView showMessageTextView = mPrivacyContainer.findViewById(R.id.privacy_show_message);

        mPrivacyTitle.setTextColor(ConversationColors.get().getListTitleColor());
        mPrivacyTimestamp.setTextColor(ConversationColors.get().getListTimeColor());
        showMessageTextView.setTextColor(mPrimaryColor);
        showMessageTextView.setBackground(
                BackgroundDrawables.createBackgroundDrawable(Color.WHITE,
                        UiUtils.getColorDark(Color.WHITE),
                        Dimensions.pxFromDp(1), mPrimaryColor, Dimensions.pxFromDp(25), false, true));
        showMessageTextView.setOnClickListener(v -> {
            revealMessages();
            showMessageTextView.setClickable(false);
            MessageBoxAnalytics.logEvent("SMS_PrivacyPopUp_Show_Click");
        });

        if (hideContactForThisMessage()) {
            mConversationName.setVisibility(GONE);
            mConversationName.setAlpha(0f);
            mPrivacyConversationName.setVisibility(VISIBLE);
        }

        updatePrivacyTitleAndTimestamp();
    }

    private void updatePrivacyTitleAndTimestamp() {
        if (mPrivacyTitle != null) {
            String title = mActivity.getResources().getQuantityString(
                    R.plurals.notification_new_messages, mAdapter.getItemCount(), mAdapter.getItemCount());
            mPrivacyTitle.setText(title);
            mPrivacyTimestamp.setText(Dates.getConversationTimeString(System.currentTimeMillis()));
        }
    }

    private void revealMessages() {
        // height
        ValueAnimator heightAnimator = ValueAnimator.ofInt(0, Dimensions.pxFromDp(40));
        heightAnimator.setDuration(200L);
        heightAnimator.start();
        mPrivacyContainer.animate().alpha(0f).setDuration(120L).start();
        mMessageView.setVisibility(VISIBLE);

        // main content
        ValueAnimator revealAnimator = ValueAnimator.ofFloat(0f, 1f);
        revealAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private float alpha;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                alpha = (float) animation.getAnimatedValue();
                mMessageView.setAlpha(alpha);
            }
        });

        revealAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mPrivacyContainer.setVisibility(GONE);
                mInputEditText.requestFocus();
            }
        });
        revealAnimator.setStartDelay(120L);
        revealAnimator.setDuration(280L);
        revealAnimator.start();

        // action bar simulation
        if (hideContactForThisMessage()) {
            mPrivacyConversationName.animate().alpha(0f).setDuration(200L).start();
            mConversationName.setVisibility(VISIBLE);
            mConversationName.animate().alpha(1f).setDuration(200L).start();
        }
        markAsRead();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        if (visibility == VISIBLE) {
            if (PrivacyModeSettings.getPrivacyMode(mConversationId) == NONE) {
                markAsRead();
            }
        }

    }

    void markAsRead() {
        if (!TextUtils.isEmpty(mConversationId)) {
            mActivity.markAsRead(mConversationId);
        }
    }

    private boolean hideContactForThisMessage() {
        return PrivacyModeSettings.getPrivacyMode(mConversationId) == PrivacyModeSettings.HIDE_CONTACT_AND_MESSAGE;
    }

    private boolean hideMessagesForThisMessage() {
        return PrivacyModeSettings.getPrivacyMode(mConversationId) != NONE;
    }

    private static class MessageItemDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            int position = parent.getChildAdapterPosition(view);
            int count = parent.getAdapter().getItemCount();

            outRect.top = Dimensions.pxFromDp(14);
            if (position == count - 1) {
                outRect.bottom = Dimensions.pxFromDp(14);
            }
        }
    }

}
