package com.android.messaging.ui;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.android.messaging.R;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BugleAnalytics;

import org.qcode.fontchange.impl.FontManagerImpl;

public abstract class CustomViewPager extends LinearLayout {
    public final static int DEFAULT_TAB_STRIP_SIZE = -1;
    private final int mDefaultTabStripSize;
    private ViewPager mViewPager;
    private ViewPagerTabs mTabstrip;

    public CustomViewPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(getLayoutRes(), this, true);
        setOrientation(LinearLayout.VERTICAL);

        mTabstrip = (ViewPagerTabs) findViewById(R.id.tab_strip);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        mDefaultTabStripSize = context.getResources().getDimensionPixelSize(tv.resourceId);
    }

    public void setCurrentItem(final int position) {
        mViewPager.setCurrentItem(position);
    }

    public void setViewPagerTabHeight(final int tabHeight) {
        mTabstrip.getLayoutParams().height = tabHeight == DEFAULT_TAB_STRIP_SIZE ?
                mDefaultTabStripSize : tabHeight;
    }

    public void setViewHolders(final CustomPagerViewHolder[] viewHolders) {
        Assert.notNull(mViewPager);
        final PagerAdapter adapter = new CustomViewPagerAdapter(viewHolders);
        mViewPager.setAdapter(adapter);
        mTabstrip.setViewPager(mViewPager);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int state) {
                mTabstrip.onPageScrollStateChanged(state);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
                mTabstrip.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                BugleAnalytics.logEvent("SMS_ContactsTabPage_Show", true, "type", position == 0 ? "Frequents" : "AllContacts");
                mTabstrip.onPageSelected(position);
            }
        });
        FontManagerImpl.getInstance().applyFont(mTabstrip, true);
    }

    public int getSelectedItemPosition() {
        return mTabstrip.getSelectedItemPosition();
    }

    @LayoutRes
    protected abstract int getLayoutRes();
}

