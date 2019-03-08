package com.android.messaging.smsshow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.util.UiUtils;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import static com.android.messaging.ui.UIIntents.UI_INTENT_EXTRA_ATTACHMENT_URI;
import static com.android.messaging.ui.UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID;
import static com.android.messaging.ui.UIIntents.UI_INTENT_EXTRA_CONVERSATION_NAME;
import static com.android.messaging.ui.UIIntents.UI_INTENT_EXTRA_MESSAGE;
import static com.android.messaging.ui.UIIntents.UI_INTENT_EXTRA_SELF_ID;

public class MessageBoxActivity extends BaseActivity {

    private ImageView mReplyIcon;
    private EditText mEditText;
    private ProgressBar mProgressBar;

    private View mOpenEditTextButton;
    private TextView mNextButton;

    private ViewGroup mEditTextContainer;
    private ViewGroup mActionButtonContainer;

    private ViewPager mConversationPager;
    private DynamicalPagerAdapter mConversationPagerAdapter;

    private Dialog mCloseDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_box_activity);
        UiUtils.setStatusAndNavigationBarTransparent(this);

        initConversationList();
        initEditView();
        initActionView();
        initMenu();

        // todo you must not pass Privacy Information between intents!
    }

    private void initMenu() {
        ImageView closeButton = findViewById(R.id.alert_close_btn);
        closeButton.setOnClickListener(v -> finish());
        ViewGroup mContainer = findViewById(R.id.alert_card_container);

        View turnOffContainer = getLayoutInflater().inflate(R.layout.message_box_menu_pop_up, (ViewGroup) mContainer, false);
        final TextView turnOff = turnOffContainer.findViewById(R.id.tv_turn_off);
        turnOff.setText(getString(R.string.message_box_disable));
        turnOff.measure(0, 0);
        final RipplePopupView popupView = new RipplePopupView(this);
        popupView.setOutSideBackgroundColor(Color.TRANSPARENT);
        popupView.setContentView(turnOffContainer);
        popupView.setOutSideClickListener(v -> popupView.dismiss());
        turnOff.setOnClickListener(v -> {
            popupView.dismiss();
            showCloseDialog();
        });

        final ImageView menuIv = findViewById(R.id.alert_menu_btn);
        menuIv.setOnClickListener(view -> {
            int closeW = menuIv.getWidth();
            int turnOffW = closeButton.getMeasuredWidth();
            int popW = turnOff.getMeasuredWidth() / 2;
            int offsetX = Dimensions.isRtl() ? menuIv.getPaddingLeft() / 2 + popW - 12 : closeW - turnOffW - menuIv.getPaddingRight() - popW + 12;
            int offsetY = -menuIv.getHeight() * 4 / 5;
            popupView.showAsDropDown(menuIv, offsetX, offsetY);
        });

    }

    private void showCloseDialog() {
        if (mCloseDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CloseDialogTheme);

            String title = getString(R.string.message_box_alert_title);
            SpannableString spannableStringTitle = new SpannableString(title);
            spannableStringTitle.setSpan(
                    new ForegroundColorSpan(0xDF000000),
                    0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setTitle(spannableStringTitle);

            String message = getString(R.string.message_box_alert_message);
            SpannableString spannableStringMessage = new SpannableString(message);
            spannableStringMessage.setSpan(
                    new ForegroundColorSpan(0x8A000000),
                    0, message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setMessage(spannableStringMessage);

            builder.setPositiveButton(getString(R.string.message_box_positive_action), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (mCloseDialog == null) {
                        return;
                    }
                    mCloseDialog.dismiss();
                    mCloseDialog = null;
                }
            });

            builder.setNegativeButton(getString(R.string.message_box_negative_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    if (mCloseDialog == null) {
                        return;
                    }
                    MessageBoxSettings.setSMSAssistantModuleEnabled(false);
                    mCloseDialog.dismiss();
                    mCloseDialog = null;
                    Toasts.showToast(R.string.message_box_disable_successfully);
                }
            });

            mCloseDialog = builder.create();

            mCloseDialog.setOnShowListener(dialog -> {
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                negativeButton.setTextColor(0xff999999);

                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setTextColor(0xff336bf3);
            });
        }
        mCloseDialog.show();
    }


    private void initConversationList() {
        mConversationPager = findViewById(R.id.conversation_pager);

        Intent intent = getIntent();
        String message = intent.getStringExtra(UI_INTENT_EXTRA_MESSAGE);
        String conversationName = intent.getStringExtra(UI_INTENT_EXTRA_CONVERSATION_NAME);
        String avatar = intent.getStringExtra(UI_INTENT_EXTRA_ATTACHMENT_URI);
        String selfId = intent.getStringExtra(UI_INTENT_EXTRA_SELF_ID);
        String conversationId = intent.getStringExtra(UI_INTENT_EXTRA_CONVERSATION_ID);

        MessageBoxConversationItemView item = new MessageBoxConversationItemView(this,
                selfId,
                conversationId,
                message,
                Uri.parse(avatar),
                conversationName);
        mConversationPagerAdapter = new DynamicalPagerAdapter();
        item.setTag(intent.getStringExtra(UI_INTENT_EXTRA_CONVERSATION_ID));
        mConversationPagerAdapter.addView(item);
        mConversationPager.setAdapter(mConversationPagerAdapter);
        mConversationPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // next button clickable, and update next button text
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String conversationId = intent.getStringExtra(UI_INTENT_EXTRA_CONVERSATION_ID);
        String message = intent.getStringExtra(UI_INTENT_EXTRA_MESSAGE);

        boolean isNewConversation = true;
        int viewCount = mConversationPagerAdapter.getCount();
        MessageBoxConversationItemView view;
        for (int i = 0; i < viewCount; i++) {
            view = mConversationPagerAdapter.getViews().get(i);
            if (TextUtils.equals(conversationId, (String) view.getTag())) {
                isNewConversation = false;
                view.addNewMessage(message);
                mConversationPager.setCurrentItem(i, true);
                break;
            }
        }
        if (isNewConversation) {
            MessageBoxConversationItemView newItem = new MessageBoxConversationItemView(this,
                    intent.getStringExtra(UI_INTENT_EXTRA_SELF_ID),
                    intent.getStringExtra(UI_INTENT_EXTRA_CONVERSATION_ID),
                    message,
                    Uri.parse(intent.getStringExtra(UI_INTENT_EXTRA_ATTACHMENT_URI)),
                    intent.getStringExtra(UI_INTENT_EXTRA_CONVERSATION_NAME));
            newItem.setTag(intent.getStringExtra(UI_INTENT_EXTRA_CONVERSATION_ID));
            mConversationPagerAdapter.addView(newItem);
            mConversationPagerAdapter.notifyDataSetChanged();
            mConversationPager.setCurrentItem(mConversationPagerAdapter.getCount() - 1, true);
            toggleNextButton();
        }
    }

    private void initActionView() {
        mActionButtonContainer = findViewById(R.id.actions_button_container);
        mEditTextContainer = findViewById(R.id.edit_text_container);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mOpenEditTextButton = findViewById(R.id.open_edit_text_button);
        mNextButton = findViewById(R.id.next_icon);
        mOpenEditTextButton.setOnClickListener(v -> openEditText());
        mNextButton.setOnClickListener(v -> {
            int currentItem = mConversationPager.getCurrentItem();
            if (currentItem < mConversationPagerAdapter.getCount()) {
                mConversationPager.setCurrentItem(currentItem + 1, true);
            } else {
                Toasts.showToast(R.string.message_box_no_message);
            }
        });
    }

    private void toggleNextButton() {
        mNextButton.setVisibility(View.VISIBLE);
        mNextButton.setClickable(true);
        int total = mConversationPagerAdapter.getCount();
        int currentPosition = mConversationPager.getCurrentItem();
        mNextButton.setText((getString(R.string.message_box_next) + String.format(getString(R.string.acb_message_next_num),
                total - currentPosition - 1)));
        if (currentPosition == total - 1) {
            mNextButton.setBackground(null);
            mNextButton.setClickable(false);
        } else {
            mNextButton.setBackgroundResource(R.drawable.message_box_next_btn_bg);
        }
    }

    private void openEditText() {
        mActionButtonContainer.setVisibility(View.GONE);
        mEditTextContainer.setVisibility(View.VISIBLE);
        mReplyIcon.setVisibility(View.VISIBLE);
        mReplyIcon.setClickable(true);
        mEditText.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(mEditText, 0);
    }

    private void closeEditText() {
        mActionButtonContainer.setVisibility(View.VISIBLE);
        mEditTextContainer.setVisibility(View.GONE);
        mReplyIcon.setVisibility(View.GONE);
        mEditText.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    private void initEditView() {
        mEditText = findViewById(R.id.edit_text);
        mReplyIcon = findViewById(R.id.reply_icon);
        mReplyIcon.setOnClickListener(v -> {
            if (TextUtils.isEmpty(mEditText.getText())) {
                return;
            }
            mReplyIcon.setClickable(false);
            mReplyIcon.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);

            Threads.postOnMainThreadDelayed(new Runnable() {
                @Override
                public void run() {
                    mConversationPagerAdapter.getView(mConversationPager.getCurrentItem()).replyMessage(mEditText.getText().toString());
                    if (mConversationPager.getCurrentItem() < mConversationPagerAdapter.getCount() - 1) {
                        mConversationPager.setCurrentItem(mConversationPager.getCurrentItem() + 1, true);
                    } else {
                        finish();
                    }
                    mProgressBar.setVisibility(View.GONE);
                    mReplyIcon.setClickable(true);
                    mReplyIcon.setVisibility(View.VISIBLE);
                }
            }, 1000L);

        });

        mProgressBar = findViewById(R.id.progress_bar);
        mEditText.addTextChangedListener(new TextWatcher() {

            private boolean replyIconEnabled;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int characterCount = s.toString().length();
                if (characterCount == 0) {
                    mReplyIcon.setEnabled(false);
                    mReplyIcon.getBackground().setColorFilter(0xffd7dfe9, PorterDuff.Mode.SRC_ATOP);
                    replyIconEnabled = false;
                } else if (!replyIconEnabled) {
                    mReplyIcon.setEnabled(true);
                    mReplyIcon.getBackground().setColorFilter(0x0, PorterDuff.Mode.SRC_ATOP);
                    replyIconEnabled = true;
                }

                int byteCount = s.toString().getBytes().length;
                int smsThresholdInByte = 137;

                if (byteCount >= smsThresholdInByte) {
                    mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(characterCount)});
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mReplyIcon.setEnabled(false);
        mReplyIcon.getBackground().setColorFilter(0xffd7dfe9, PorterDuff.Mode.SRC_ATOP);
        mProgressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.acb_phone_sms_alert_blue), PorterDuff.Mode.SRC_IN);
    }
}
