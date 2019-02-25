package com.android.messaging.ui.customize;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.CustomFooterViewPager;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.CustomViewPager;

import static com.android.messaging.ui.customize.ChooseMessageColorEntryViewHolder.CustomColor.BUBBLE_COLOR_INCOMING;
import static com.android.messaging.ui.customize.ChooseMessageColorEntryViewHolder.CustomColor.BUBBLE_COLOR_OUTGOING;
import static com.android.messaging.ui.customize.ChooseMessageColorEntryViewHolder.CustomColor.TEXT_COLOR_INCOMING;
import static com.android.messaging.ui.customize.ChooseMessageColorEntryViewHolder.CustomColor.TEXT_COLOR_OUTGOING;

public class ChooseMessageColorPagerView extends FrameLayout implements OnColorChangedListener {

    private CustomMessageHost mHost;

    private TextView mTitle;

    public void setHost(CustomMessageHost host) {
        mHost = host;
    }

    public ChooseMessageColorPagerView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.choose_custom_bubble_color_layout, this, true);

        mTitle = findViewById(R.id.title);
        findViewById(R.id.close_button).setOnClickListener(v -> setVisibility(GONE));

        initPager(context);
        setClickable(true);
    }

    void updateTitle(@ChooseMessageColorEntryViewHolder.CustomColor int type) {

        String prefix = "";
        String suffix = "";

        switch (type) {
            case BUBBLE_COLOR_INCOMING:
                prefix = getContext().getString(R.string.bubble_customize_bubble_color);
                suffix = getContext().getString(R.string.bubble_customize_received);
                break;
            case BUBBLE_COLOR_OUTGOING:
                prefix = getContext().getString(R.string.bubble_customize_bubble_color);
                suffix = getContext().getString(R.string.bubble_customize_sent);
                break;
            case TEXT_COLOR_INCOMING:
                prefix = getContext().getString(R.string.bubble_customize_text_color);
                suffix = getContext().getString(R.string.bubble_customize_received);
                break;
            case TEXT_COLOR_OUTGOING:
                prefix = getContext().getString(R.string.bubble_customize_text_color);
                suffix = getContext().getString(R.string.bubble_customize_sent);
                break;
        }
        mTitle.setText(String.format(getContext().getString(R.string.bubble_customize_color_title)
                , prefix
                , suffix
        ));

    }

    private void initPager(Context context) {
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
        mHost.previewCustomColor(color);
    }
}
