package com.android.messaging.ui.customize;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.android.messaging.R;
import com.android.messaging.ui.CustomFooterViewPager;
import com.android.messaging.ui.CustomHeaderViewPager;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.CustomViewPager;
import com.android.messaging.ui.ViewPagerTabs;

public class ChooseCustomBubbleColorView extends FrameLayout {

    private CustomFooterViewPager mCustomHeaderViewPager;
    private ChooseBubbleColorRecommendViewHolder mBubbleStyleViewHolder;
    private ChooseBubbleColorAdvanceViewHolder mBubbleColorViewHolder;

    public ChooseCustomBubbleColorView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.choose_custom_bubble_color_layout, this, true);

        mBubbleStyleViewHolder = new ChooseBubbleColorRecommendViewHolder(context);
        mBubbleColorViewHolder = new ChooseBubbleColorAdvanceViewHolder(context);

        final CustomPagerViewHolder[] viewHolders = {
                mBubbleStyleViewHolder,
                mBubbleColorViewHolder};


        mCustomHeaderViewPager = findViewById(R.id.custom_footer_pager);
        mCustomHeaderViewPager.setViewHolders(viewHolders);
        mCustomHeaderViewPager.setViewPagerTabHeight(CustomViewPager.DEFAULT_TAB_STRIP_SIZE);
        mCustomHeaderViewPager.setBackgroundColor(getResources().getColor(R.color.contact_picker_background));
        mCustomHeaderViewPager.setCurrentItem(0);
        setClickable(true);
    }
}
