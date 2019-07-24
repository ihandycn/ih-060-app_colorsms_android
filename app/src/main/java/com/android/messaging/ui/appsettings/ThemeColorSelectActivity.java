package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.ui.ConversationDrawables;
import com.android.messaging.ui.CustomFooterViewPager;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.CustomViewPager;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.customize.ChooseMessageColorAdvanceViewHolder;
import com.android.messaging.ui.customize.OnColorChangedListener;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleFirebaseAnalytics;
import com.android.messaging.util.UiUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

import static com.android.messaging.ui.appsettings.ChooseThemeColorRecommendViewHolder.getPrimaryColorType;

public class ThemeColorSelectActivity extends BaseActivity implements OnColorChangedListener {
    private static final int POSITION_RECOMMEND = 0;
    private static final int POSITION_ADVANCE = 1;

    private Toolbar mToolbar;
    private CustomFooterViewPager mCustomFooterViewPager;
    private ChooseThemeColorRecommendViewHolder mThemeColorRecommendViewHolder;
    private ChooseMessageColorAdvanceViewHolder mThemeColorAdvanceViewHolder;

    private int mPrePrimaryColor = PrimaryColors.getPrimaryColor();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting_theme_select);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("");

        TextView title = mToolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.menu_theme_color));
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initPager(this);

        BugleAnalytics.logEvent("Customize_ThemeColor_Show", true,
                "withTheme", String.valueOf(!ThemeUtils.isDefaultTheme()));
        BugleFirebaseAnalytics.logEvent("Customize_ThemeColor_Show",
                "withTheme", String.valueOf(!ThemeUtils.isDefaultTheme()));

        UiUtils.setTitleBarBackground(mToolbar, this);
    }

    private void initPager(Context context) {
        mThemeColorRecommendViewHolder = new ChooseThemeColorRecommendViewHolder(context);
        mThemeColorAdvanceViewHolder = new ChooseMessageColorAdvanceViewHolder(context);

        mThemeColorRecommendViewHolder.setOnColorChangedListener(this);
        mThemeColorAdvanceViewHolder.setOnColorChangedListener(this);

        final CustomPagerViewHolder[] viewHolders = {
                mThemeColorRecommendViewHolder,
                mThemeColorAdvanceViewHolder};

        mCustomFooterViewPager = findViewById(R.id.custom_footer_pager);
        mCustomFooterViewPager.setViewHolders(viewHolders);
        mCustomFooterViewPager.setViewPagerTabHeight(CustomViewPager.DEFAULT_TAB_STRIP_SIZE);
        mCustomFooterViewPager.setBackgroundColor(getResources().getColor(R.color.contact_picker_background));
        mCustomFooterViewPager.setCurrentItem(mThemeColorRecommendViewHolder.getIsPrimaryColorRecommendedColor() ? POSITION_RECOMMEND : POSITION_ADVANCE);
        mCustomFooterViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                BugleAnalytics.logEvent("Customize_ThemeColor_Tab_Click", true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onColorChanged(int color) {
        if (mCustomFooterViewPager.getSelectedItemPosition() == POSITION_RECOMMEND) {
            mThemeColorAdvanceViewHolder.setColor(color);
        } else {
            mThemeColorRecommendViewHolder.refreshSelectStatus();
        }

        PrimaryColors.changePrimaryColor(color);

        UiUtils.setTitleBarBackground(mToolbar, this);
        mCustomFooterViewPager.updatePrimaryColor();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mPrePrimaryColor != PrimaryColors.getPrimaryColor()) {
            // clear media caches
            Factory.get().reclaimMemory();
            // update drawable color cache
            ConversationDrawables.get().updateDrawables();
            BugleAnalytics.logEvent("Customize_ThemeColor_Change", true, "color",
                    String.valueOf(getPrimaryColorType()), "withTheme", String.valueOf(!ThemeUtils.isDefaultTheme()));

            BugleFirebaseAnalytics.logEvent("Customize_ThemeColor_Change","color",
                    String.valueOf(getPrimaryColorType()), "withTheme", String.valueOf(!ThemeUtils.isDefaultTheme()));

            HSGlobalNotificationCenter.sendNotification(ConversationListActivity.EVENT_MAINPAGE_RECREATE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
