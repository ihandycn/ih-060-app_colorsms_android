package com.android.messaging.ui.customize.mainpage;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.android.messaging.R;
import com.android.messaging.ui.CustomFooterViewPager;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.CustomViewPager;
import com.android.messaging.ui.customize.OnColorChangedListener;
import com.android.messaging.util.UiUtils;

public class ChatListChooseColorView extends FrameLayout implements OnColorChangedListener {

    private OnColorChangedListener mListener;
    private ChatListChooseColorRecommendViewHolder mRecommendViewHolder;
    private ChatListChooseColorAdvanceViewHolder mAdvanceViewHolder;
    private CustomFooterViewPager mCustomHeaderViewPager;
    private int mRecommendColor;

    public ChatListChooseColorView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.chat_list_choose_color_layout, this, true);

        initPager(context);
        setClickable(true);
    }

    public void changeRecommendColor(@ColorInt int color) {
        mRecommendColor = color;
        mRecommendViewHolder.update(color, color);
        mAdvanceViewHolder.setColor(color);
    }

    public void setOnColorChangeListener(OnColorChangedListener listener) {
        mListener = listener;
    }

    private void initPager(Context context) {
        mRecommendViewHolder = new ChatListChooseColorRecommendViewHolder(context);
        mAdvanceViewHolder = new ChatListChooseColorAdvanceViewHolder(context);

        mRecommendViewHolder.setOnColorChangedListener(this);
        mAdvanceViewHolder.setOnColorChangedListener(this);

        final CustomPagerViewHolder[] viewHolders = {
                mRecommendViewHolder,
                mAdvanceViewHolder};

        mCustomHeaderViewPager = findViewById(R.id.custom_footer_pager);
        mCustomHeaderViewPager.setViewHolders(viewHolders);
        mCustomHeaderViewPager.setViewPagerTabHeight(CustomViewPager.DEFAULT_TAB_STRIP_SIZE);
        mCustomHeaderViewPager.setBackgroundColor(getResources().getColor(R.color.contact_picker_background));
        if (UiUtils.isRtlMode()) {
            mCustomHeaderViewPager.setCurrentItem(1);
        } else {
            mCustomHeaderViewPager.setCurrentItem(0);
        }
    }

    @Override
    public void onColorChanged(int color) {
        if (mListener != null) {
            mListener.onColorChanged(color);
        }
        if (mCustomHeaderViewPager.getSelectedItemPosition() == 0) {
            mAdvanceViewHolder.setColor(color);
        } else {
            mRecommendViewHolder.update(mRecommendColor, color);
        }
    }
}
