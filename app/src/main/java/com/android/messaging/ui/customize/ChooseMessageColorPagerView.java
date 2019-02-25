package com.android.messaging.ui.customize;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.android.messaging.R;
import com.android.messaging.ui.CustomFooterViewPager;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.CustomViewPager;

public class ChooseMessageColorPagerView extends FrameLayout implements OnColorChangedListener {

    public ChooseMessageColorPagerView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.choose_custom_bubble_color_layout, this, true);

        initPager(context);
        setClickable(true);
    }

    public void initPager(Context context) {
        ChooseMessageColorRecommendViewHolder bubbleRecommendViewHolder = new ChooseMessageColorRecommendViewHolder(context);
        ChooseMessageColorAdvanceViewHolder bubbleColorViewHolder = new ChooseMessageColorAdvanceViewHolder(context);

        bubbleRecommendViewHolder.setOnColorChangedListener(this);
        bubbleColorViewHolder.setOnColorChangedListener(this);

        final CustomPagerViewHolder[] viewHolders = {
                bubbleRecommendViewHolder,
                bubbleColorViewHolder};

        CustomFooterViewPager customHeaderViewPager = findViewById(R.id.custom_footer_pager);
        customHeaderViewPager.setViewHolders(viewHolders);
        customHeaderViewPager.setViewPagerTabHeight(CustomViewPager.DEFAULT_TAB_STRIP_SIZE);
        customHeaderViewPager.setBackgroundColor(getResources().getColor(R.color.contact_picker_background));
        customHeaderViewPager.setCurrentItem(0);
    }

    @Override
    public void onColorChanged(int color) {

    }
}
