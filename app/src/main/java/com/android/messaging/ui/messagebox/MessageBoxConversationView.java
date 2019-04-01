package com.android.messaging.ui.messagebox;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.support.annotation.ColorInt;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.NoConfirmationSmsSendService;
import com.android.messaging.datamodel.data.MessageBoxItemData;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.customize.PrimaryColors;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import static com.android.messaging.datamodel.NoConfirmationSmsSendService.EXTRA_SELF_ID;

public class MessageBoxConversationView extends FrameLayout {

    @ColorInt private int mPrimaryColor;
    @ColorInt private int mPrimaryColorDark;

    private ImageView mCallImage;

    private BoxActivity mActivity;
    private MessageBoxInputActionView mInputActionView;
    private MessageBoxListItemAdapter mAdapter;

    private String mConversationId;
    private String mSelfId;
    private String mPhoneNumber;
    private String mParticipantId;
    private long mOldestReceivedTimestamp;

    private boolean mMarkAsUnread;

    public MessageBoxConversationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (BoxActivity) context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPrimaryColor = PrimaryColors.getPrimaryColor();
        mPrimaryColorDark = PrimaryColors.getPrimaryColorDark();

        initActionBarSimulation();
        initQuickActions();
        mInputActionView = findViewById(R.id.message_compose_view_container);
    }

    void bind(MessageBoxItemData data) {
        TextView conversationName = findViewById(R.id.conversation_name);
        conversationName.setText(data.getConversationName());

        mConversationId = data.getConversationId();
        mSelfId = data.getSelfId();
        mPhoneNumber = data.getPhoneNumber();

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);

        mAdapter = new MessageBoxListItemAdapter(data);
        recyclerView.setAdapter(mAdapter);
        setTag(mConversationId);

        if (TextUtils.isEmpty(mPhoneNumber)) {
            mCallImage.setVisibility(GONE);
        }
        mOldestReceivedTimestamp = data.getReceivedTimestamp();
        mParticipantId = data.getParticipantId();
    }

    void addNewMessage(MessageBoxItemData data) {
        mAdapter.addNewIncomingMessage(data);
    }

    void markAsUnread() {
        mMarkAsUnread = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!mMarkAsUnread) {
            BugleNotifications.markMessagesAsRead(mConversationId);
        }
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
        Context context = Factory.get().getApplicationContext();
        final Intent sendIntent = new Intent(context, NoConfirmationSmsSendService.class);
        sendIntent.setAction(TelephonyManager.ACTION_RESPOND_VIA_MESSAGE);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.putExtra(EXTRA_SELF_ID, mSelfId);
        sendIntent.putExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID, mConversationId);
        context.startService(sendIntent);
    }

}
