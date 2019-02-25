package com.android.messaging.ui.customize;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.android.messaging.R;
import com.android.messaging.ui.CustomHeaderViewPager;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.CustomViewPager;

import static com.android.messaging.ui.customize.ChooseMessageColorEntryViewHolder.CustomColor.BUBBLE_COLOR_INCOMING;
import static com.android.messaging.ui.customize.ChooseMessageColorEntryViewHolder.CustomColor.BUBBLE_COLOR_OUTGOING;
import static com.android.messaging.ui.customize.ChooseMessageColorEntryViewHolder.CustomColor.TEXT_COLOR_INCOMING;
import static com.android.messaging.ui.customize.ChooseMessageColorEntryViewHolder.CustomColor.TEXT_COLOR_OUTGOING;


public class CustomBubblesActivity extends AppCompatActivity implements CustomMessageHost {

    private ChooseMessageColorPagerView mChooseMessageColorPagerView;
    private CustomMessagePreviewView mCustomMessagePreview;

    @ChooseMessageColorEntryViewHolder.CustomColor
    private int mColorType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customize_bubbles_activity);
        initActionBar();

        mChooseMessageColorPagerView = findViewById(R.id.choose_message_color_view);
        mCustomMessagePreview = findViewById(R.id.custom_message_preview);

        mChooseMessageColorPagerView.setHost(this);

        BubbleDrawableViewHolder bubbleDrawableViewHolder = new BubbleDrawableViewHolder(this);
        ChooseMessageColorEntryViewHolder chooseMessageColorEntryViewHolder = new ChooseMessageColorEntryViewHolder(this);

        bubbleDrawableViewHolder.setHost(this);
        chooseMessageColorEntryViewHolder.setHost(this);

        final CustomPagerViewHolder[] viewHolders = {
                bubbleDrawableViewHolder,
                chooseMessageColorEntryViewHolder};

        CustomHeaderViewPager customHeaderViewPager = findViewById(R.id.customize_pager);
        customHeaderViewPager.setViewHolders(viewHolders);
        customHeaderViewPager.setViewPagerTabHeight(CustomViewPager.DEFAULT_TAB_STRIP_SIZE);
        customHeaderViewPager.setBackgroundColor(getResources().getColor(R.color.contact_picker_background));
        customHeaderViewPager.setCurrentItem(0);
    }

    @Override
    public void openColorPickerView(int type) {
        mColorType = type;
        mChooseMessageColorPagerView.setVisibility(View.VISIBLE);
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
    }

    @Override
    public void previewCustomBubbleDrawable(int id) {
        mCustomMessagePreview.previewCustomBubbleDrawables(id);
    }

    private void initActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
