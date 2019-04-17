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
import com.android.messaging.ui.welcome.WelcomeChooseThemeActivity;
import com.android.messaging.util.BugleAnalytics;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class ChooseThemePagerView extends ConstraintLayout {

    public interface OnPageSelectedListener {
        void onPageSelected(int position, int themeColor);
    }

    private ViewPager mPager;
    private ThemePagerAdapter mAdapter;

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
        mAdapter = new ThemePagerAdapter(getContext());
        mPager.setAdapter(mAdapter);
        mPager.setPageTransformer(false, new ThemePagerTransformer(getContext()));
        mPager.setPageMargin(Dimensions.pxFromDp(16));
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


            }

            @Override
            public void onPageSelected(int position) {
                int themeColor = Color.parseColor(mAdapter.getThemeInfo(position).themeColor);
                applyTextView.setBackground(BackgroundDrawables.createBackgroundDrawable(
                        themeColor, Dimensions.pxFromDp(6.7f), true));

                if (mOnPageSelectedListener != null) {
                    mOnPageSelectedListener.onPageSelected(position, themeColor);
                }

                if (getContext() instanceof WelcomeChooseThemeActivity) {
                    BugleAnalytics.logEvent("Start_ChooseTheme_Slide", true);
                } else if (getContext() instanceof  ChooseThemeActivity) {
                    BugleAnalytics.logEvent("Customize_Theme_Slide", true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        applyTextView.setBackground(BackgroundDrawables.createBackgroundDrawable(
                Color.parseColor(mAdapter.getThemeInfo(0).themeColor), Dimensions.pxFromDp(6.7f), true));

        applyTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mApplyClickListener != null) {
                    mApplyClickListener.onClick(applyTextView);
                }

                String preTheme = ThemeUtils.getCurrentThemeName();
                ThemeUtils.applyTheme(mAdapter.getThemeInfo(mPager.getCurrentItem()));

                if (getContext() instanceof WelcomeChooseThemeActivity) {
                    BugleAnalytics.logEvent("Start_ChooseTheme_Apply", true, "theme", ThemeUtils.getCurrentThemeName());
                } else if (getContext() instanceof ChooseThemeActivity) {
                    BugleAnalytics.logEvent("Customize_Theme_Apply_Click", true, "theme", ThemeUtils.getCurrentThemeName());
                }
                if (!ThemeUtils.getCurrentThemeName().equals(preTheme)) {
                    if (getContext() instanceof ChooseThemeActivity) {
                        BugleAnalytics.logEvent("Customize_Theme_Change", true, "theme", ThemeUtils.getCurrentThemeName());
                    }
                }
            }
        });
    }

    public void setOnApplyClickListener(OnClickListener listener) {
        mApplyClickListener = listener;
    }


    public void setOnPageSelectedListener(OnPageSelectedListener listener) {
        mOnPageSelectedListener = listener;
        mOnPageSelectedListener.onPageSelected(0, Color.parseColor(mAdapter.getThemeInfo(0).themeColor));
    }

}
