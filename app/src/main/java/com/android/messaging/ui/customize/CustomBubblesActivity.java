package com.android.messaging.ui.customize;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.BaseAlertDialog;
import com.android.messaging.ui.ConversationDrawables;
import com.android.messaging.ui.CustomHeaderViewPager;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.CustomViewPager;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.util.BugleAnalytics;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import static com.android.messaging.ui.customize.ChooseMessageColorEntryViewHolder.CustomColor.BUBBLE_COLOR_INCOMING;
import static com.android.messaging.ui.customize.ChooseMessageColorEntryViewHolder.CustomColor.BUBBLE_COLOR_OUTGOING;
import static com.android.messaging.ui.customize.ChooseMessageColorEntryViewHolder.CustomColor.TEXT_COLOR_INCOMING;
import static com.android.messaging.ui.customize.ChooseMessageColorEntryViewHolder.CustomColor.TEXT_COLOR_OUTGOING;


public class CustomBubblesActivity extends AppCompatActivity implements CustomMessageHost {

    private ChooseMessageColorPagerView mChooseMessageColorPagerView;
    private CustomMessagePreviewView mCustomMessagePreview;
    private ChooseMessageColorEntryViewHolder mChooseMessageColorEntryViewHolder;
    private BubbleDrawableViewHolder mBubbleDrawableViewHolder;
    private TextView mSaveButton;

    @ChooseMessageColorEntryViewHolder.CustomColor
    private int mColorType;
    private String mConversationId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customize_bubbles_activity);
        initActionBar();

        mChooseMessageColorPagerView = findViewById(R.id.choose_message_color_view);
        mCustomMessagePreview = findViewById(R.id.custom_message_preview);

        mChooseMessageColorPagerView.setHost(this);

        mBubbleDrawableViewHolder = new BubbleDrawableViewHolder(this);
        mChooseMessageColorEntryViewHolder = new ChooseMessageColorEntryViewHolder(this);

        mBubbleDrawableViewHolder.setHost(this);
        mChooseMessageColorEntryViewHolder.setHost(this);

        final CustomPagerViewHolder[] viewHolders = {
                mBubbleDrawableViewHolder,
                mChooseMessageColorEntryViewHolder};

        CustomHeaderViewPager customHeaderViewPager = findViewById(R.id.customize_pager);
        customHeaderViewPager.setViewHolders(viewHolders);
        customHeaderViewPager.setViewPagerTabHeight(CustomViewPager.DEFAULT_TAB_STRIP_SIZE);
        customHeaderViewPager.setBackgroundColor(getResources().getColor(R.color.contact_picker_background));
        customHeaderViewPager.setCurrentItem(0);

        mConversationId = getIntent().getStringExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID);

        BugleAnalytics.logEvent("Customize_Bubble_Show", "from", getOpenSourceType());
    }

    @Override
    public void openColorPickerView(int type) {
        mColorType = type;
        mChooseMessageColorPagerView.reveal();
        mChooseMessageColorPagerView.updateTitle(type);
        disableSaveButton();
    }

    @Override
    public void closeColorPickerView() {
        enableSaveButton();
    }

    @Override
    public void previewCustomColor(@ColorInt int color) {
        switch (mColorType) {
            case BUBBLE_COLOR_INCOMING:
                mCustomMessagePreview.previewCustomBubbleBackgroundColor(true, color);
                break;
            case BUBBLE_COLOR_OUTGOING:
                mCustomMessagePreview.previewCustomBubbleBackgroundColor(false, color);
                break;
            case TEXT_COLOR_INCOMING:
                mCustomMessagePreview.previewCustomTextColor(true, color);
                break;
            case TEXT_COLOR_OUTGOING:
                mCustomMessagePreview.previewCustomTextColor(false, color);
                break;
        }
        mChooseMessageColorEntryViewHolder.previewCustomColor(mColorType, color);
        enableSaveButton();
    }

    @Override
    public void previewCustomBubbleDrawable(int id) {
        mCustomMessagePreview.previewCustomBubbleDrawables(id);
        enableSaveButton();
    }

    private void initActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
        }

        mSaveButton = findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(v -> {
            save();
            BugleAnalytics.logEvent("Customize_Bubble_Save_Click");
            BugleAnalytics.logEvent("Customize_Bubble_SaveChange_Alert_Show");
        });
        disableSaveButton();
    }

    private void save() {
        mCustomMessagePreview.save(mConversationId);

        ConversationDrawables.get().updateDrawables();

        disableSaveButton();

        // notify main page recreate
        HSGlobalNotificationCenter.sendNotification(ConversationListActivity.EVENT_MAINPAGE_RECREATE);

        finish();
    }

    @Override
    public void onBackPressed() {
        if (mSaveButton.isEnabled()) {
            new BaseAlertDialog.Builder(CustomBubblesActivity.this)
                    .setTitle(R.string.bubble_customize_save_confirm_dialog_title)
                    .setMessage(R.string.bubble_customize_save_confirm_dialog_content)
                    .setPositiveButton(getString(R.string.save).toUpperCase(), (dialog, which) -> {
                        save();
                        BugleAnalytics.logEvent("Customize_Bubble_SaveChange_Alert_Click");
                    })
                    .setNegativeButton(getString(R.string.share_cancel).toUpperCase(), null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private void enableSaveButton() {
        if (!mSaveButton.isEnabled()) {
            mSaveButton.setBackground(BackgroundDrawables
                    .createBackgroundDrawable(PrimaryColors.getPrimaryColor(), Dimensions.pxFromDp(25), true));
            mSaveButton.setEnabled(true);
        }
    }

    private void disableSaveButton() {
        mSaveButton.setBackground(BackgroundDrawables
                .createBackgroundDrawable(0xffd8dce3, Dimensions.pxFromDp(25), false));
        mSaveButton.setEnabled(false);
    }

    private String getOpenSourceType() {
        return TextUtils.isEmpty(mConversationId) ? "settings" : "chat";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
