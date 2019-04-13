package com.android.messaging.ui.customize.theme;

import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.customize.PrimaryColors;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class ChooseThemePagerView extends ConstraintLayout {

    public interface OnPageSelectedListener {
        void onPageSelected(int position, int themeColor);
    }

    private ViewPager mPager;

    private OnClickListener mApplyClickListener;
    private OnPageSelectedListener mOnPageSelectedListener;

    public ChooseThemePagerView(Context context) {
        super(context);
    }

    public ChooseThemePagerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.choose_theme_main_layout, this, true);
        TextView applyTextView = findViewById(R.id.apply);

        mPager = findViewById(R.id.pager);
        ThemePagerAdapter adapter = new ThemePagerAdapter(getContext());
        mPager.setAdapter(adapter);
        mPager.setPageTransformer(false, new ThemePagerTransformer(getContext()));
        mPager.setPageMargin(Dimensions.pxFromDp(16));
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int themeColor = Color.parseColor(adapter.getThemeInfo(position).themeColor);
                applyTextView.setBackground(BackgroundDrawables.createBackgroundDrawable(
                        themeColor, Dimensions.pxFromDp(6.7f), true));

                if (mOnPageSelectedListener != null) {
                    mOnPageSelectedListener.onPageSelected(position, themeColor);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        applyTextView.setBackground(BackgroundDrawables.createBackgroundDrawable(
                PrimaryColors.getPrimaryColor(), Dimensions.pxFromDp(6.7f), true));

        applyTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ThemeUtils.applyTheme(adapter.getThemeInfo(mPager.getCurrentItem()));
                if (mApplyClickListener != null) {
                    mApplyClickListener.onClick(applyTextView);
                }
            }
        });
    }

    public void setOnApplyClickListener(OnClickListener listener) {
        mApplyClickListener = listener;
    }


    public void setOnPageSelectedListener(OnPageSelectedListener listener) {
        mOnPageSelectedListener = listener;
    }

}
