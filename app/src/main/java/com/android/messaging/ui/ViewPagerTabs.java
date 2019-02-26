/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.messaging.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.provider.FontRequest;
import android.support.v4.provider.FontsContractCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.OsUtil;
import com.ihs.app.framework.HSApplication;

import org.qcode.fontchange.FontManager;
import org.qcode.fontchange.impl.QueryBuilder;

/**
 * Lightweight implementation of ViewPager tabs. This looks similar to traditional actionBar tabs,
 * but allows for the view containing the tabs to be placed anywhere on screen. Text-related
 * attributes can also be assigned in XML - these will get propogated to the child TextViews
 * automatically.
 * <p>
 * Note: this file is taken from the AOSP /packages/apps/ContactsCommon/src/com/android/contacts/
 * common/list/ViewPagerTabs.java. Some platform specific API calls (e.g. ViewOutlineProvider which
 * assumes L and above) have been modified to support down to Api Level 16.
 */
public class ViewPagerTabs extends HorizontalScrollView implements ViewPager.OnPageChangeListener {

    ViewPager mPager;
    private ViewPagerTabStrip mTabStrip;

    /**
     * Linearlayout that will contain the TextViews serving as tabs. This is the only child
     * of the parent HorizontalScrollView.
     */
    final int mTextStyle;
    final ColorStateList mTextColor;
    final int mTextSize;
    final boolean mTextAllCaps;
    int mPrevSelected = -1;
    int mSidePadding;

    private static final int TAB_SIDE_PADDING_IN_DPS = 10;

    // TODO: This should use <declare-styleable> in the future
    private static final int[] ATTRS = new int[]{
            android.R.attr.textSize,
            android.R.attr.textStyle,
            android.R.attr.textAllCaps
    };
    private Handler mHandler = null;
    private Typeface mTypeface;

    /**
     * Shows a toast with the tab description when long-clicked.
     */
    private class OnTabLongClickListener implements OnLongClickListener {
        final String mTabDescription;

        public OnTabLongClickListener(String tabDescription) {
            mTabDescription = tabDescription;
        }

        @Override
        public boolean onLongClick(View v) {
            final int[] screenPos = new int[2];
            getLocationOnScreen(screenPos);

            final Context context = getContext();
            final int width = getWidth();
            final int height = getHeight();
            final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

            Toast toast = Toast.makeText(context, mTabDescription, Toast.LENGTH_SHORT);

            // Show the toast under the tab
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                    (screenPos[0] + width / 2) - screenWidth / 2, screenPos[1] + height);

            toast.show();
            return true;
        }
    }

    public ViewPagerTabs(Context context) {
        this(context, null);
    }

    public ViewPagerTabs(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewPagerTabs(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFillViewport(true);

        mSidePadding = (int) (getResources().getDisplayMetrics().density * TAB_SIDE_PADDING_IN_DPS);

        final TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);
        mTextSize = a.getDimensionPixelSize(0, 0);
        mTextStyle = a.getInt(1, 0);
        mTextAllCaps = a.getBoolean(2, false);

        mTabStrip = new ViewPagerTabStrip(context);
        mTextColor = getTextColor();
        addView(mTabStrip,
                new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        a.recycle();
    }

    public void setViewPager(ViewPager viewPager) {
        mPager = viewPager;
        addTabs(mPager.getAdapter());
    }

    private void addTabs(PagerAdapter adapter) {
        mTabStrip.removeAllViews();

        final int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            addTab(adapter.getPageTitle(i), i);
        }
    }

    private void addTab(CharSequence tabTitle, final int position) {
        final TextView textView = new TextView(getContext());
        initTypeface(textView);
        textView.setText(tabTitle);
        textView.setBackgroundResource(R.drawable.contact_picker_tab_background_selector);
        textView.setGravity(Gravity.CENTER);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(getRtlPosition(position));
            }
        });

        // Assign various text appearance related attributes to child views.
        if (mTextStyle > 0) {
            // change typeface to fit google font
            textView.setTypeface(textView.getTypeface(), mTextStyle);

        }
        if (mTextSize > 0) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        }
        if (mTextColor != null) {
            textView.setTextColor(mTextColor);
        }
        textView.setAllCaps(mTextAllCaps);
        textView.setPadding(mSidePadding, 0, mSidePadding, 0);
        mTabStrip.addView(textView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT, 1));
        // Default to the first child being selected
        if (position == 0) {
            mPrevSelected = 0;
            textView.setSelected(true);
        }
    }

    private ColorStateList getTextColor() {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_selected},
                new int[] { -android.R.attr.state_selected}
        };

        int[] colors = new int[] {
                PrimaryColors.getPrimaryColor(),
                getResources().getColor(R.color.pager_tab_state_normal)
        };
        return new ColorStateList(states, colors);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        position = getRtlPosition(position);
        int tabStripChildCount = mTabStrip.getChildCount();
        if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
            return;
        }

        mTabStrip.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
        position = getRtlPosition(position);
        int tabStripChildCount = mTabStrip.getChildCount();
        if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
            return;
        }

        if (mPrevSelected >= 0 && mPrevSelected < tabStripChildCount) {
            mTabStrip.getChildAt(mPrevSelected).setSelected(false);
        }
        final View selectedChild = mTabStrip.getChildAt(position);
        selectedChild.setSelected(true);

        // Update scroll position
        final int scrollPos = selectedChild.getLeft() - (getWidth() - selectedChild.getWidth()) / 2;
        smoothScrollTo(scrollPos, 0);
        mPrevSelected = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private int getRtlPosition(int position) {
        if (OsUtil.isAtLeastJB_MR2() && Factory.get().getApplicationContext().getResources()
                .getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            return mTabStrip.getChildCount() - 1 - position;
        }
        return position;
    }

    public int getSelectedItemPosition() {
        return mPrevSelected;
    }

    private void initTypeface(TextView textView) {
        String familyName = BuglePrefs.getApplicationPrefs().getString(FontManager.MESSAGE_FONT_FAMILY, "");
        if (familyName.isEmpty()) {
            return;
        }
        int weight = (mTextStyle + 6) * 100;    // bold
        QueryBuilder queryBuilder = new QueryBuilder(familyName)
                .withWidth(100f)
                .withWeight(weight)
                .withItalic(0.0f)
                .withBestEffort(true);
        final String query = queryBuilder.build();

        FontRequest request = new FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                query,
                com.iflytek.android_font_loader_lib.R.array.com_google_android_gms_fonts_certs);


        FontsContractCompat.FontRequestCallback callback = new FontsContractCompat
                .FontRequestCallback() {
            @Override
            public void onTypefaceRetrieved(Typeface typeface) {
                textView.setTypeface(typeface);
            }

            @Override
            public void onTypefaceRequestFailed(int reason) {
                Toast.makeText(HSApplication.getContext(),
                        HSApplication.getContext().getString(com.iflytek.android_font_loader_lib.R.string.request_failed, reason), Toast.LENGTH_LONG)
                        .show();
            }
        };
        FontsContractCompat
                .requestFont(getContext(), request, callback,
                        getHandlerThreadHandler());

    }

    private Handler getHandlerThreadHandler() {
        if (mHandler == null) {
            HandlerThread handlerThread = new HandlerThread("fonts");
            handlerThread.start();
            mHandler = new Handler(handlerThread.getLooper());
        }
        return mHandler;
    }

}
