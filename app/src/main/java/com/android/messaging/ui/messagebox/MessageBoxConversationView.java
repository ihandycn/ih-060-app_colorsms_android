package com.android.messaging.ui.messagebox;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.constraint.Group;
import android.support.constraint.Guideline;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.datamodel.NoConfirmationSmsSendService;
import com.android.messaging.datamodel.data.MessageBoxItemData;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.appsettings.PrivacyModeSettings;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.emoji.EmojiInfo;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.Dates;
import com.android.messaging.util.ImeUtil;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import static com.android.messaging.datamodel.NoConfirmationSmsSendService.EXTRA_SELF_ID;

public class MessageBoxConversationView extends FrameLayout {

    @ColorInt private int mPrimaryColor;
    @ColorInt private int mPrimaryColorDark;

    private MessageBoxActivity mActivity;
    private ViewGroup mContent;
    private ViewGroup mMessageView;
    private Guideline mBottomGuideline;
    private MessageBoxInputActionView mInputActionView;
    private MessageBoxMessageListAdapter mAdapter;
    private ImageView mCallImage;
    private TextView mConversationName;
    private RecyclerView mRecyclerView;
    private EditText mInputEditText;
    private Group mActionsGroup;

    // privacy mode
    private TextView mPrivacyConversationName;
    private View mPrivacyContainer;
    private TextView mPrivacyTitle;
    private TextView mPrivacyTimestamp;

    private String mConversationId;
    private String mSelfId;
    private String mPhoneNumber;
    private String mParticipantId;
    private long mOldestReceivedTimestamp;

    private int mInputEmojiCount;

    public MessageBoxConversationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (MessageBoxActivity) context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPrimaryColor = PrimaryColors.getPrimaryColor();
        mPrimaryColorDark = PrimaryColors.getPrimaryColorDark();

        initActionBarSimulation();
        initQuickActions();
        mContent = findViewById(R.id.content);
        mInputActionView = findViewById(R.id.message_compose_view_container);
        mActionsGroup = findViewById(R.id.action_group);
        mMessageView = findViewById(R.id.message_view);
        mBottomGuideline = findViewById(R.id.guideline_bottom);
        mPrivacyConversationName = findViewById(R.id.privacy_conversation_name);

        mInputEditText = mInputActionView.getComposeEditText();
        initInputAction();
    }

    void bind(MessageBoxItemData data) {
        mConversationName = findViewById(R.id.conversation_name);
        mConversationName.setText(data.getConversationName());

        mConversationId = data.getConversationId();
        mSelfId = data.getSelfId();
        mPhoneNumber = data.getPhoneNumber();

        mRecyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.addItemDecoration(new MessageItemDecoration());

        mAdapter = new MessageBoxMessageListAdapter(data);
        mRecyclerView.setAdapter(mAdapter);
        setTag(mConversationId);

        if (TextUtils.isEmpty(mPhoneNumber)) {
            mCallImage.setVisibility(GONE);
        }
        mOldestReceivedTimestamp = data.getReceivedTimestamp();
        mParticipantId = data.getParticipantId();
        inflatePrivacyModePageIfNeeded();
    }

    void updateTimestamp() {
        mAdapter.notifyDataSetChanged();
    }

    void addNewMessage(MessageBoxItemData data) {
        mAdapter.addNewIncomingMessage(data);
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    int getContentHeight() {
        return mContent.getHeight();
    }

    private void initActionBarSimulation() {
        mCallImage = findViewById(R.id.action_call);
        mCallImage.setOnClickListener(mActivity);
        mCallImage.setBackground(BackgroundDrawables.
                createBackgroundDrawable(mPrimaryColor, mPrimaryColorDark, Dimensions.pxFromDp(21), false, true));

        ImageView closeActionImage = findViewById(R.id.action_close);
        closeActionImage.setOnClickListener(mActivity);
        closeActionImage.setBackground(BackgroundDrawables.
                createBackgroundDrawable(mPrimaryColor, mPrimaryColorDark, Dimensions.pxFromDp(21), false, true));
        findViewById(R.id.action_bar_simulation).getBackground().setColorFilter(mPrimaryColor, PorterDuff.Mode.SRC_ATOP);
    }

    private void initQuickActions() {
        TextView actionDelete = findViewById(R.id.action_delete);
        TextView actionUnread = findViewById(R.id.action_unread);
        TextView actionOpen = findViewById(R.id.action_open);

        actionDelete.setOnClickListener(mActivity);
        actionUnread.setOnClickListener(mActivity);
        actionOpen.setOnClickListener(mActivity);

        actionDelete.setTextColor(mPrimaryColor);
        actionUnread.setTextColor(mPrimaryColor);
        actionOpen.setTextColor(mPrimaryColor);

        actionDelete.setText(actionDelete.getText().toString().toUpperCase());
        float radius = getResources().getDimension(R.dimen.message_box_background_radius);
        int rippleColor = getResources().getColor(com.superapps.R.color.ripples_ripple_color);
        actionDelete.setBackground(
                BackgroundDrawables.createBackgroundDrawable(
                        Color.WHITE, rippleColor, 0f, 0f, 0, radius,
                        false, true));
        actionUnread.setBackground(
                BackgroundDrawables.createBackgroundDrawable(
                        Color.WHITE, rippleColor, 0f, 0f, 0, 0f,
                        false, true));
        actionOpen.setBackground(
                BackgroundDrawables.createBackgroundDrawable(
                        Color.WHITE, rippleColor, 0f, 0f, radius, 0,
                        false, true));
    }

    String getConversationId() {
        return mConversationId;
    }

    long getOldestReceivedTimestamp() {
        return mOldestReceivedTimestamp;
    }

    String getParticipantId() {
        return mParticipantId;
    }

    void call() {
        final String phoneNumber = mPhoneNumber;
        final View targetView = findViewById(R.id.action_call);
        Point centerPoint;
        if (targetView != null) {
            final int screenLocation[] = new int[2];
            targetView.getLocationOnScreen(screenLocation);
            final int centerX = screenLocation[0] + targetView.getWidth() / 2;
            final int centerY = screenLocation[1] + targetView.getHeight() / 2;
            centerPoint = new Point(centerX, centerY);
        } else {
            // In the overflow menu, just use the center of the screen.
            final Display display = mActivity.getWindowManager().getDefaultDisplay();
            centerPoint = new Point(display.getWidth() / 2, display.getHeight() / 2);
        }
        UIIntents.get().launchPhoneCallActivity(mActivity, phoneNumber, centerPoint);
    }

    void replyMessage() {
        String message = mInputActionView.getMessage();
        if (TextUtils.isEmpty(message)) {
            return;
        }
        mInputActionView.performReply();
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

        BugleAnalytics.logEvent("SMS_PopUp_Reply_BtnClick_Multifunction",
                "type", type, "type2", MessageBoxAnalytics.getConversationType());
    }

    void emojiClick(EmojiInfo emojiInfo) {
        if (mInputEditText != null) {
            mInputEditText.getText().append(emojiInfo.mEmoji);
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
        });
    }

    private void inflatePrivacyModePageIfNeeded() {
        if (!hideMessagesForThisMessage()) {
            return;
        }

        mMessageView.setVisibility(INVISIBLE);
        mMessageView.setAlpha(0f);
        mActionsGroup.setVisibility(GONE);
        mActionsGroup.setAlpha(0f);
        ViewStub stub = findViewById(R.id.privacy_stub);
        mPrivacyContainer = stub.inflate();
        mPrivacyContainer.setClickable(true);

        mPrivacyTitle = mPrivacyContainer.findViewById(R.id.privacy_title);
        mPrivacyTimestamp = mPrivacyContainer.findViewById(R.id.privacy_date);
        TextView showMessageTextView = mPrivacyContainer.findViewById(R.id.privacy_show_message);

        showMessageTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                revealMessages();
                showMessageTextView.setClickable(false);
                BugleAnalytics.logEvent("SMS_PrivacyPopUp_Show_Click");
            }
        });

        if (hideContactForThisMessage()) {
            mConversationName.setVisibility(GONE);
            mConversationName.setAlpha(0f);
            mPrivacyConversationName.setVisibility(VISIBLE);
        }

        mCallImage.setAlpha(0f);

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
        heightAnimator.addUpdateListener(animation -> mBottomGuideline.setGuidelineEnd((Integer) animation.getAnimatedValue()));
        heightAnimator.setDuration(200L);
        heightAnimator.start();
        mPrivacyContainer.animate().alpha(0f).setDuration(120L).start();
        mMessageView.setVisibility(VISIBLE);

        // main content
        ValueAnimator revealAnimator = ValueAnimator.ofFloat(0f, 1f);
        revealAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private float alpha;
            private View[] actionGroupViews = getActionGroupViewArray();
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                alpha = (float) animation.getAnimatedValue();
                mMessageView.setAlpha(alpha);
                for (View view : actionGroupViews) {
                    view.setAlpha(alpha);
                }
            }
        });

        revealAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mActionsGroup.setVisibility(VISIBLE);
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
        mCallImage.animate().alpha(1f).setDuration(200L).start();
    }

    private View[] getActionGroupViewArray() {
        int[] ids = mActionsGroup.getReferencedIds();
        View[] views = new View[ids.length];
        for (int i = 0; i < ids.length; ++i) {
            int id = ids[i];
            View view = findViewById(id);
            views[i] = view;
        }
        return views;
    }

    private boolean hideContactForThisMessage() {
        return PrivacyModeSettings.getPrivacyMode(mConversationId) == PrivacyModeSettings.HIDE_CONTACT_AND_MESSAGE;
    }

    private boolean hideMessagesForThisMessage() {
        return PrivacyModeSettings.getPrivacyMode(mConversationId) != PrivacyModeSettings.NONE;
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
