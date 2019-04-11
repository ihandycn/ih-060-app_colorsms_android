package com.android.messaging.ui.customize.theme;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class ChooseThemePagerView extends ConstraintLayout {
    private ViewPager mPager;

    public ChooseThemePagerView(Context context) {
        super(context);
    }

    public ChooseThemePagerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.choose_theme_main_layout, this, true);

        mPager = findViewById(R.id.pager);
        mPager.setAdapter(new ThemePagerAdapter(getContext()));
        mPager.setPageTransformer(false, new ThemePagerTransformer(getContext()));
        mPager.setPageMargin(Dimensions.pxFromDp(11));

        TextView applyTextView = findViewById(R.id.apply);
        applyTextView.setBackground(BackgroundDrawables.createBackgroundDrawable(
                PrimaryColors.getPrimaryColor(), Dimensions.pxFromDp(6.7f), true));
    }

}
