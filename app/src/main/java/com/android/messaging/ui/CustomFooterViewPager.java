package com.android.messaging.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.android.messaging.R;
import com.superapps.util.Dimensions;

public class CustomFooterViewPager extends CustomViewPager {
    public CustomFooterViewPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mTabstrip.setUnderlineThickness(Dimensions.pxFromDp(5));
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.custom_footer_view_pager;
    }
}
