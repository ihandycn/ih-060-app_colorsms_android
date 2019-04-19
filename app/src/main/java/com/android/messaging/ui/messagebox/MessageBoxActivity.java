package com.android.messaging.ui.messagebox;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.messaging.BuildConfig;
import com.android.messaging.R;
import com.android.messaging.datamodel.SyncManager;
import com.android.messaging.datamodel.action.DeleteMessageAction;
import com.android.messaging.datamodel.action.MarkAsReadAction;
import com.android.messaging.datamodel.data.MessageBoxItemData;
import com.android.messaging.ui.BaseAlertDialog;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.emoji.BaseEmojiInfo;
import com.android.messaging.ui.emoji.EmojiInfo;
import com.android.messaging.ui.emoji.EmojiItemPagerAdapter;
import com.android.messaging.ui.emoji.EmojiPackagePagerAdapter;
import com.android.messaging.ui.emoji.StickerInfo;
import com.android.messaging.ui.emoji.ViewPagerDotIndicatorView;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.UiUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;
import com.superapps.view.ViewPagerFixed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.android.messaging.ui.UIIntents.UI_INTENT_EXTRA_MESSAGE_BOX_ITEM;

public class MessageBoxActivity extends AppCompatActivity implements INotificationObserver,
        View.OnClickListener, ViewPager.OnPageChangeListener {

    private static final String BACK = "back";
    private static final String OPEN = "open_btn";
    private static final String HOME = "home";
    private static final String CLOSE = "close";
    private static final String REPLY = "reply";
    private static final String CLICK_CONTENT = "click_content";

    public static final String NOTIFICATION_FINISH_MESSAGE_BOX = "finish_message_box";
    public static final String NOTIFICATION_MESSAGE_BOX_SEND_SMS_SUCCEDED = "message_box_send_sms_success";
    public static final String NOTIFICATION_MESSAGE_BOX_SEND_SMS_FAILED = "message_box_send_sms_failed";

    private static final boolean DEBUGGING_MULTI_CONVERSATIONS = false && BuildConfig.DEBUG;

    private ViewPager mPager;
    private DynamicalPagerAdapter mPagerAdapter;
    private MessageBoxIndicatorView mIndicator;
    private ViewGroup mEmojiContainer;

    private MessageBoxConversationView mCurrentConversationView;

    private int mMessagesNum = 1;
    private int mContactsNum = 1;
    private boolean mHasSms;
    private boolean mHasMms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.box_activity);

        mPager = findViewById(R.id.message_box_conversation_pager);
        mIndicator = findViewById(R.id.multi_conversation_indicator_view);
        mEmojiContainer = findViewById(R.id.emoji_picker_container);
        mEmojiContainer.setBackgroundColor(Color.WHITE);

        MessageBoxItemData data = getIntent().getParcelableExtra(UI_INTENT_EXTRA_MESSAGE_BOX_ITEM);
        MessageBoxConversationView view = (MessageBoxConversationView) LayoutInflater.from(this).inflate(R.layout.message_box_conversation_view, null, false);
        view.bind(data);

        mPagerAdapter = new DynamicalPagerAdapter();
        mPagerAdapter.addView(view);
        mPager.addOnPageChangeListener(this);
        mPager.setAdapter(mPagerAdapter);
        initEmojiKeyboradSimulation();

        mCurrentConversationView = view;
        MessageBoxAnalytics.setIsMultiConversation(false);

        recordMessageType(data);

        HSGlobalNotificationCenter.addObserver(NOTIFICATION_FINISH_MESSAGE_BOX, this);
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_MESSAGE_BOX_SEND_SMS_FAILED, this);
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_MESSAGE_BOX_SEND_SMS_SUCCEDED, this);

        mIndicator.setOnIndicatorClickListener(new MessageBoxIndicatorView.OnIndicatorClickListener() {
            @Override
            public void onClickLeft() {
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                mIndicator.updateIndicator(mPager.getCurrentItem(), mPagerAdapter.getCount());
            }

            @Override
            public void onClickRight() {
                mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                mIndicator.updateIndicator(mPager.getCurrentItem(), mPagerAdapter.getCount());
            }
        });
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

            mPagerAdapter.addView(newItem);
            mPagerAdapter.notifyDataSetChanged();
            MessageBoxAnalytics.setIsMultiConversation(true);
            mContactsNum++;
            mIndicator.updateIndicator(mPager.getCurrentItem(), mPagerAdapter.getCount());
        }
        mMessagesNum++;

        recordMessageType(data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasWindowFocus()) {
            mCurrentConversationView.updateTimestamp();
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
        reLayoutIndicatorView();
        mIndicator.updateIndicator(position, mPagerAdapter.getCount());
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    void reLayoutIndicatorView() {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mIndicator.getLayoutParams();
        params.topMargin = mCurrentConversationView.getContentHeight() + Dimensions.pxFromDp(42);
        mIndicator.setLayoutParams(params);
    }

    private void initEmojiKeyboradSimulation() {
        EmojiPackagePagerAdapter.OnEmojiClickListener listener = new EmojiPackagePagerAdapter.OnEmojiClickListener() {
            @Override
            public void emojiClick(EmojiInfo emojiInfo) {
                mCurrentConversationView.emojiClick(emojiInfo);
            }

            @Override
            public void stickerClickExcludeMagic(@NonNull StickerInfo info) {

            }

            @Override
            public void deleteEmoji() {
                mCurrentConversationView.deleteEmoji();
            }
        };

        ViewPagerFixed itemPager = findViewById(R.id.emoji_item_pager);
        ViewPagerDotIndicatorView dotIndicatorView = findViewById(R.id.dot_indicator_view);
        itemPager.addOnPageChangeListener(dotIndicatorView);
        PagerAdapter adapter = new EmojiItemPagerAdapter(getEmojiList(), listener);
        itemPager.setAdapter(adapter);
        dotIndicatorView.initDot(adapter.getCount(), 0);
    }

    private List<BaseEmojiInfo> getEmojiList() {
        List<BaseEmojiInfo> result = new ArrayList<>();
        String[] arrays = getResources().getStringArray(R.array.emoji_faces);
        for (String array : arrays) {
            EmojiInfo info = new EmojiInfo();
            info.mEmoji = new String((Character.toChars(Integer.parseInt(array, 16))));
            result.add(info);
        }
        return result;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.action_close:
                finish(CLOSE);
                break;
//            case R.id.action_open:
//                UIIntents.get().launchConversationActivityWithParentStack(this, mCurrentConversationView.getConversationId(), null);
//                finish(OPEN);
//                MessageBoxAnalytics.logEvent("SMS_PopUp_Open_Click");
//                break;

            case R.id.self_send_icon:
                mCurrentConversationView.replyMessage();
                break;
        }
    }

    boolean getIsEmojiVisible() {
        return mEmojiContainer.getVisibility() == View.VISIBLE;
    }

    boolean getIsEmojiVisibilityGone() {
        return mEmojiContainer.getVisibility() == View.GONE;
    }

    void hideEmoji() {
        adjustKeyboardGuideline(false);
        mEmojiContainer.setVisibility(View.INVISIBLE);
        mEmojiContainer.post(this::reLayoutIndicatorView);
    }

    void showEmoji() {
        adjustKeyboardGuideline(true);
        mEmojiContainer.setVisibility(View.VISIBLE);
        mEmojiContainer.post(this::reLayoutIndicatorView);
    }

    private void adjustKeyboardGuideline(boolean showEmoji) {
        if (getIsEmojiVisibilityGone()) {
            Guideline keyboradGuideline = findViewById(R.id.keyboard_guideline);
            int keyboardHeight = UiUtils.getKeyboardHeight();
            if (keyboardHeight > 0) {
                keyboradGuideline.setGuidelineEnd(keyboardHeight);
            } else if (showEmoji) {
                keyboradGuideline.setGuidelineEnd(Dimensions.dpFromPx(197));
            }
        }
    }

    private void removeCurrentPage(String source) {
        int position = mPager.getCurrentItem();
        if (position == mPagerAdapter.getCount() - 1) {
            finish(source);
        } else {
            mPagerAdapter.removeView(mPager, mCurrentConversationView);
            mPager.setCurrentItem(position);
            mCurrentConversationView = (MessageBoxConversationView) mPagerAdapter.getViews().get(position);
            mIndicator.updateIndicator(mPager.getCurrentItem(), mPagerAdapter.getCount());
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        Threads.postOnMainThread(() -> {
            if (NOTIFICATION_FINISH_MESSAGE_BOX.equals(s)) {
                finish(CLICK_CONTENT);
            } else if (NOTIFICATION_MESSAGE_BOX_SEND_SMS_FAILED.equals(s)) {
                Toasts.showToast(R.string.message_box_send_failed_toast);
                removeCurrentPage(REPLY);
            } else if (NOTIFICATION_MESSAGE_BOX_SEND_SMS_SUCCEDED.equals(s)) {
                Toasts.showToast(R.string.message_box_send_successfully_toast);
                removeCurrentPage(REPLY);
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (getIsEmojiVisible()) {
            hideEmoji();
            return;
        }
        finish(BACK);
    }

    private void finish(String source) {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        if (MessageBoxAnalytics.getIsMultiConversation()) {
            BugleAnalytics.logEvent("SMS_PopUp_Close_Multifunction_MultiUser", false, true, "closeType", source);
        } else {
            BugleAnalytics.logEvent("SMS_PopUp_Close_Multifunction_SingleUser", false, true, "closeType", source);
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
        HSGlobalNotificationCenter.removeObserver(this);

        String messageType = "";
        if (mHasMms) {
            messageType += "mms";
        }
        if (mHasSms) {
            messageType += "sms";
        }

        BugleAnalytics.logEvent("SMS_PopUp_Show_Multifunction", false, true,
                "msgNum", String.valueOf(mMessagesNum),
                "contactNum", String.valueOf(mContactsNum),
                "message type", messageType);
    }
}
