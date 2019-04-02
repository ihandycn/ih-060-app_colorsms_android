package com.android.messaging.ui.messagebox;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.android.messaging.BaseActivity;
import com.android.messaging.BuildConfig;
import com.android.messaging.R;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.SyncManager;
import com.android.messaging.datamodel.action.DeleteMessageAction;
import com.android.messaging.datamodel.data.MessageBoxItemData;
import com.android.messaging.ui.BaseAlertDialog;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.CommonUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Commons;

import static com.android.messaging.ui.UIIntents.UI_INTENT_EXTRA_MESSAGE_BOX_ITEM;

public class MessageBoxActivity extends BaseActivity implements INotificationObserver,
        View.OnClickListener, ViewPager.OnPageChangeListener {

    public static final String NOTIFICATION_FINISH_MESSAGE_BOX = "finish_message_box";

    private static final boolean DEBUGGING_MULTI_CONVERSATIONS = false && BuildConfig.DEBUG;

    private ViewPager mPager;
    private DynamicalPagerAdapter mPagerAdapter;
    private MessageBoxIndicatorView mIndicator;

    private MessageBoxConversationView mCurrentConversationView;

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
                if (TextUtils.equals(data.getConversationId(), (String) view.getTag())) {
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
        }
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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

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

                BugleAnalytics.logEvent("SMS_PopUp_Call_Click");
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
                                    removeCurrentPage();
                                    BugleAnalytics.logEvent("SMS_PopUp_Delete_Alert_Delete");
                                })
                        .setNegativeButton(R.string.delete_conversation_decline_button,
                                (dialog, which) -> BugleAnalytics.logEvent("SMS_PopUp_Delete_Alert_Cancel"))
                        .show();
                BugleAnalytics.logEvent("SMS_PopUp_Delete_Click");
                break;

            case R.id.action_close:
                finish();
                break;
            case R.id.action_unread:
                mCurrentConversationView.markAsUnread();
                removeCurrentPage();
                BugleAnalytics.logEvent("SMS_PopUp_Unread_Click");
                break;
            case R.id.action_open:
                UIIntents.get().launchConversationActivity(this, mCurrentConversationView.getConversationId(), null);
                finish();
                BugleAnalytics.logEvent("SMS_PopUp_Open_Click");
                break;

            case R.id.self_send_icon:
                mCurrentConversationView.replyMessage();
                removeCurrentPage();
                break;
        }
    }

    private void removeCurrentPage() {
        int position  = mPager.getCurrentItem();
        if (position == mPagerAdapter.getCount() - 1) {
            finish();
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
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BugleAnalytics.logEvent("SMS_PopUp_Close", true);
        BugleNotifications.markAllMessagesAsSeen();
        HSGlobalNotificationCenter.removeObserver(this);
    }

}
