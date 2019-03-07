package com.android.messaging.smsshow;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.util.UiUtils;

import static com.android.messaging.ui.UIIntents.UI_INTENT_EXTRA_ATTACHMENT_URI;
import static com.android.messaging.ui.UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID;
import static com.android.messaging.ui.UIIntents.UI_INTENT_EXTRA_CONVERSATION_NAME;
import static com.android.messaging.ui.UIIntents.UI_INTENT_EXTRA_MESSAGE;

public class MessageBoxActivity extends BaseActivity {

    private boolean btnClickable = true;
    private ImageView replyIcon;
    private EditText editText;
    private ProgressBar progressBar;

    private ViewPager mConversationPager;
    private DynamicalPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_box_activity);
        UiUtils.setStatusAndNavigationBarTransparent(this);

        initConversationList();
        initEditView();
    }

    private void initConversationList() {
        mConversationPager = findViewById(R.id.conversation_pager);

        Intent intent = getIntent();
        String message = intent.getStringExtra(UI_INTENT_EXTRA_MESSAGE);
        String conversationName = intent.getStringExtra(UI_INTENT_EXTRA_CONVERSATION_NAME);
        String avatar = intent.getStringExtra(UI_INTENT_EXTRA_ATTACHMENT_URI);

        MessageBoxConversationItemView item = new MessageBoxConversationItemView(this,
                message,
                Uri.parse(avatar),
                conversationName);
        mPagerAdapter = new DynamicalPagerAdapter();
        item.setTag(intent.getStringExtra(UI_INTENT_EXTRA_CONVERSATION_ID));
        mPagerAdapter.addView(item);
        mConversationPager.setAdapter(mPagerAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String conversationId = intent.getStringExtra(UI_INTENT_EXTRA_CONVERSATION_ID);
        String message = intent.getStringExtra(UI_INTENT_EXTRA_MESSAGE);

        boolean isNewConversation = true;
        int viewCount = mPagerAdapter.getCount();
        MessageBoxConversationItemView view;
        for (int i = 0; i < viewCount; i++) {
            view = mPagerAdapter.getViews().get(i);
            if (TextUtils.equals(conversationId, (String) view.getTag())) {
                isNewConversation = false;
                view.addNewMessage(message);
                mConversationPager.setCurrentItem(i, true);
                break;
            }
        }
        if (isNewConversation) {
            MessageBoxConversationItemView newItem = new MessageBoxConversationItemView(this,
                    message,
                    Uri.parse(intent.getStringExtra(UI_INTENT_EXTRA_ATTACHMENT_URI)),
                    intent.getStringExtra(UI_INTENT_EXTRA_CONVERSATION_NAME));
            newItem.setTag(intent.getStringExtra(UI_INTENT_EXTRA_CONVERSATION_ID));
            mPagerAdapter.addView(newItem);
            mPagerAdapter.notifyDataSetChanged();
            mConversationPager.setCurrentItem(mPagerAdapter.getCount() - 1, true);
        }
    }

    private void initEditView() {
        editText = findViewById(R.id.edit_text);
        replyIcon = findViewById(R.id.reply_icon);

        findViewById(R.id.reply_button).setOnClickListener(v -> {

        });
        replyIcon.setOnClickListener(v -> {
            if (!btnClickable) {
                return;
            }
            if (TextUtils.isEmpty(editText.getText())) {
                return;
            }
            replyMessage();

        });

        progressBar = findViewById(R.id.progress_bar);

        editText.addTextChangedListener(new TextWatcher() {

            private boolean replyIconEnabled;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int characterCount = s.toString().length();
                if (characterCount == 0) {
                    replyIcon.setEnabled(false);
                    replyIcon.getBackground().setColorFilter(0xffd7dfe9, PorterDuff.Mode.SRC_ATOP);
                    replyIconEnabled = false;
                } else if (!replyIconEnabled) {
                    replyIcon.setEnabled(true);
                    replyIcon.getBackground().setColorFilter(0x0, PorterDuff.Mode.SRC_ATOP);
                    replyIconEnabled = true;
                }

                int byteCount = s.toString().getBytes().length;
                int smsThresholdInByte = 137;

                if (byteCount >= smsThresholdInByte) {
                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(characterCount)});
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        replyIcon.setEnabled(false);
        replyIcon.getBackground().setColorFilter(0xffd7dfe9, PorterDuff.Mode.SRC_ATOP);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(com.messagecenter.R.color.acb_phone_sms_alert_blue), PorterDuff.Mode.SRC_IN);
    }

    private void replyMessage() {

    }
}
