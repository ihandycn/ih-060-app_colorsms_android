package com.android.messaging.ui.messagebox;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.android.messaging.BuildConfig;
import com.android.messaging.R;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.SyncManager;
import com.android.messaging.datamodel.action.DeleteMessageAction;
import com.android.messaging.datamodel.data.MessageBoxItemData;
import com.android.messaging.ui.BaseAlertDialog;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.BugleAnalytics;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.Toasts;

import java.util.ArrayList;
import java.util.HashMap;

import static com.android.messaging.ui.UIIntents.UI_INTENT_EXTRA_MESSAGE_BOX_ITEM;

public class MessageBoxActivity extends AppCompatActivity implements INotificationObserver,
        View.OnClickListener, ViewPager.OnPageChangeListener {

    private static final String BACK = "back";
    private static final String OPEN = "open_btn";
    private static final String HOME = "home";
    private static final String CLOSE = "close";
    private static final String DELETE = "delete";
    private static final String UNREAD = "unread";
    private static final String REPLY = "reply";
    private static final String CLICK_CONTENT = "click_content";

    public static final String NOTIFICATION_FINISH_MESSAGE_BOX = "finish_message_box";

    private static final boolean DEBUGGING_MULTI_CONVERSATIONS = false && BuildConfig.DEBUG;

    private ViewPager mPager;
    private DynamicalPagerAdapter mPagerAdapter;
    private MessageBoxIndicatorView mIndicator;

    private MessageBoxConversationView mCurrentConversationView;

    private int mMessagesNum = 1;
    private int mContactsNum = 1;
    private boolean mHasSms;
    private boolean mHasMms;

    private HashMap<String, Boolean> mMarkAsUnReadMap = new HashMap<>(4);
    private ArrayList<String> mConversationIdList = new ArrayList<>(4);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.box_activity);

        mPager = findViewById(R.id.message_box_conversation_pager);
        mIndicator = findViewById(R.id.dot_indicator_view);

        MessageBoxItemData data = getIntent().getParcelableExtra(UI_INTENT_EXTRA_MESSAGE_BOX_ITEM);
        MessageBoxConversationView view = (MessageBoxConversationView) LayoutInflater.from(this).inflate(R.layout.message_box_conversation_view, null, false);
        view.bind(data);

        mPagerAdapter = new DynamicalPagerAdapter();
        mPagerAdapter.addView(view);
        mPager.addOnPageChangeListener(this);
        mPager.setAdapter(mPagerAdapter);

        mCurrentConversationView = view;
        MessageBoxAnalytics.setIsMultiConversation(false);
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_FINISH_MESSAGE_BOX, this);

        recordMessageType(data);
        mMarkAsUnReadMap.put(data.getConversationId(), false);
        mConversationIdList.add(data.getConversationId());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final MessageBoxItemData data = intent.getParcelableExtra(UI_INTENT_EXTRA_MESSAGE_BOX_ITEM);

        boolean isNewConversation = true;

        if (!DEBUGGING_MULTI_CONVERSATIONS) {
            int viewCount = mPagerAdapter.getCount();
            MessageBoxConversationView view;
            for (int i = 0; i < viewCount; i++) {
                view = (MessageBoxConversationView) mPagerAdapter.getViews().get(i);
                if (TextUtils.equals(data.getConversationId(), view.getConversationId())) {
                    isNewConversation = false;
                    view.addNewMessage(data);
                    break;
                }
            }
        }


        if (isNewConversation || DEBUGGING_MULTI_CONVERSATIONS) {
            MessageBoxConversationView newItem = (MessageBoxConversationView) LayoutInflater.from(this).inflate(R.layout.message_box_conversation_view, null, false);
            newItem.bind(data);

            mPager.removeOnPageChangeListener(mIndicator);
            mPagerAdapter.addView(newItem);
            mPagerAdapter.notifyDataSetChanged();

            mIndicator.removeAllViews();
            mIndicator.initDot(mPagerAdapter.getCount(), mPager.getCurrentItem());
            mPager.addOnPageChangeListener(mIndicator);

            MessageBoxAnalytics.setIsMultiConversation(true);
            mContactsNum++;
            mMarkAsUnReadMap.put(data.getConversationId(), false);
            mConversationIdList.add(data.getConversationId());
        }
        mMessagesNum++;

        recordMessageType(data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasWindowFocus()) {
            mIndicator.reveal();
            mCurrentConversationView.updateTimestamp();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            mIndicator.reveal();
        }
    }

    private boolean mLogScrollPaged;

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (!mLogScrollPaged && positionOffset > 0f) {
            BugleAnalytics.logEvent("SMS_PopUp_MultiUser_Slide");
            mLogScrollPaged = true;
        }
    }

    @Override
    public void onPageSelected(int position) {
        mCurrentConversationView = (MessageBoxConversationView) mPagerAdapter.getViews().get(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.action_call:
                mCurrentConversationView.call();
                MessageBoxAnalytics.logEvent("SMS_PopUp_Call_Click");
                break;

            case R.id.action_delete:
                new BaseAlertDialog.Builder(this)
                        .setTitle(getString(R.string.message_box_delete_alert_description))
                        .setPositiveButton(R.string.delete_conversation_confirmation_button,
                                (dialog, button) -> {
                                    SyncManager.sync();
                                    DeleteMessageAction.deleteMessage(mCurrentConversationView.getConversationId(),
                                            mCurrentConversationView.getParticipantId(),
                                            mCurrentConversationView.getOldestReceivedTimestamp());
                                    removeCurrentPage(DELETE);
                                    BugleAnalytics.logEvent("SMS_PopUp_Delete_Alert_Delete");
                                })
                        .setNegativeButton(R.string.delete_conversation_decline_button,
                                (dialog, which) -> BugleAnalytics.logEvent("SMS_PopUp_Delete_Alert_Cancel"))
                        .show();
                MessageBoxAnalytics.logEvent("SMS_PopUp_Delete_Click");
                break;
            case R.id.action_close:
                finish(CLOSE);
                break;
            case R.id.action_unread:
                mMarkAsUnReadMap.put(mCurrentConversationView.getConversationId(), true);
                Toasts.showToast(R.string.message_box_mark_as_unread);
                removeCurrentPage(UNREAD);
                MessageBoxAnalytics.logEvent("SMS_PopUp_Unread_Click");
                break;
            case R.id.action_open:
                UIIntents.get().launchConversationActivityWithParentStack(this, mCurrentConversationView.getConversationId(), null);
                finish(OPEN);
                MessageBoxAnalytics.logEvent("SMS_PopUp_Open_Click");
                break;

            case R.id.self_send_icon:
                mCurrentConversationView.replyMessage();
                removeCurrentPage(REPLY);
                break;
        }
    }

    private void removeCurrentPage(String source) {
        int position  = mPager.getCurrentItem();
        if (position == mPagerAdapter.getCount() - 1) {
            finish(source);
        } else {
            mPager.removeOnPageChangeListener(mIndicator);
            mPagerAdapter.removeView(mPager, mCurrentConversationView);

            mIndicator.removeAllViews();
            mPager.setCurrentItem(position);
            mIndicator.initDot(mPagerAdapter.getCount(), position);
            mPager.addOnPageChangeListener(mIndicator);
            mIndicator.reveal();
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (NOTIFICATION_FINISH_MESSAGE_BOX.equals(s)) {
            finish(CLICK_CONTENT);
        }
    }

    @Override
    public void onBackPressed() {
        if (mCurrentConversationView.getIsEmojiVisible()) {
            mCurrentConversationView.hideEmoji();
            return;
        }
        finish(BACK);
    }

    private void finish(String source) {
        finish();
        if (MessageBoxAnalytics.getIsMultiConversation()) {
            BugleAnalytics.logEvent("SMS_PopUp_Close_Multifunction_MultiUser", "closeType", source);
        } else {
            BugleAnalytics.logEvent("SMS_PopUp_Close_Multifunction_SingleUser", "closeType", source);
        }
    }

    private void recordMessageType(MessageBoxItemData data) {
        boolean isSms = !TextUtils.isEmpty(data.getContent());
        if (isSms) {
            mHasSms = true;
        } else {
            mHasMms = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BugleNotifications.markAllMessagesAsSeen();
        HSGlobalNotificationCenter.removeObserver(this);

        for (String conversationId : mConversationIdList) {
            if (!mMarkAsUnReadMap.get(conversationId)) {
                BugleNotifications.markMessagesAsRead(conversationId);
            }
        }

        String messageType = "";
        if (mHasMms) {
            messageType += "mms";
        }
        if (mHasSms) {
            messageType += "sms";
        }

        BugleAnalytics.logEvent("SMS_PopUp_Show_Multifunction",
                "msgNum", String.valueOf(mMessagesNum),
                "contactNum", String.valueOf(mContactsNum) ,
                "message type", messageType);
    }

}
