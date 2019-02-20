package com.android.messaging.ui.customize;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.android.messaging.R;
import com.android.messaging.ui.CustomHeaderPagerViewHolder;
import com.android.messaging.ui.CustomHeaderViewPager;

public class CustomBubblesActivity extends Activity {

    private CustomHeaderViewPager mCustomHeaderViewPager;
    private BubbleStyleViewHolder mBubbleStyleViewHolder;
    private BubbleColorViewHolder mBubbleColorViewHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customize_bubbles_activity);
        mCustomHeaderViewPager = findViewById(R.id.customize_pager);

        mBubbleStyleViewHolder = new BubbleStyleViewHolder(this);
        mBubbleColorViewHolder = new BubbleColorViewHolder(this);

        final CustomHeaderPagerViewHolder[] viewHolders = {
                mBubbleStyleViewHolder,
                mBubbleColorViewHolder};

        mCustomHeaderViewPager = findViewById(R.id.customize_pager);
        mCustomHeaderViewPager.setViewHolders(viewHolders);
        mCustomHeaderViewPager.setViewPagerTabHeight(CustomHeaderViewPager.DEFAULT_TAB_STRIP_SIZE);
        mCustomHeaderViewPager.setBackgroundColor(getResources().getColor(R.color.contact_picker_background));
        mCustomHeaderViewPager.setCurrentItem(0);
    }
}
